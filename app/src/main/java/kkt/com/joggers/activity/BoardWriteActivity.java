package kkt.com.joggers.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kkt.com.joggers.R;
import kkt.com.joggers.model.Board;
import kkt.com.joggers.model.Comment;

public class BoardWriteActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int MY_PERMISSION_CAMERA = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;

    private Uri imageUri;
    private ImageView write_img;
    private EditText write_content;
    private int count = 0;
    private Board board;
    private Comment comment;
    private int re_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_write);

        /* 입력폼 */
        write_img = findViewById(R.id.write_image);
        write_content = findViewById(R.id.write_content);

        /* OnClickListener 설정 */
        findViewById(R.id.write_write).setOnClickListener(this);
        findViewById(R.id.write_cancel).setOnClickListener(this);
        findViewById(R.id.write_camera).setOnClickListener(this);
        findViewById(R.id.write_album).setOnClickListener(this);
        findViewById(R.id.remove_img).setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            re_num = bundle.getInt("num");
            if (re_num != -1) {
                write_content.setText(bundle.getString("content"));
                write_img.setImageBitmap((Bitmap) bundle.getParcelable("img"));
            }
        }

        /* 권한설정 */
        checkPermission();
    }

    /* Activity 내의 모든 OnClickEvent를 처리한다 */
    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.write_write && re_num == -1) { //작성
            /* Storage에 이미지 업로드 */
            FirebaseStorage.getInstance().getReference()
                    .child("board")
                    .child(String.valueOf(imageUri.hashCode()))
                    .putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // 작성한 글을 RealTime DB의 Board 테이블에 추가한다
                            /* 작성자, 작성시간 */
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user == null)
                                return;
                            final String time = SimpleDateFormat.getDateTimeInstance().format(new Date());
                            final Uri imageUrl = taskSnapshot.getDownloadUrl();

                            FirebaseDatabase.getInstance().getReference().child("board").runTransaction(new Transaction.Handler() {
                                @Override
                                public Transaction.Result doTransaction(final MutableData mutableData) {

                                    Query lastQuery = FirebaseDatabase.getInstance().getReference().child("board").orderByKey().limitToLast(1);
                                    lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue() != null)
                                                for (DataSnapshot child : dataSnapshot.getChildren())
                                                    count = child.child("num").getValue(Integer.class) + 1;

                                             /* 게시글 data 생성 & INSERT */
                                            board = new Board(user.getDisplayName(), time, String.valueOf(imageUrl), String.valueOf(write_content.getText()), 0, count);
                                            comment = new Comment(0, "작성자", "작성자", time);
                                            Map<String, String> map = new HashMap<>();
                                            map.put("0", user.getDisplayName());
                                            FirebaseDatabase.getInstance().getReference().child("board").push().setValue(board);
                                            FirebaseDatabase.getInstance().getReference().child("comment").child(Integer.toString(count)).push().setValue(comment);
                                            FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(count)).setValue(map);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });

                                    //mutableData.child("board/count").setValue(board);
                                    //mutableData.child("comment/count").setValue("123");
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BoardWriteActivity.this, "작성 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
            /* MainActivity로 */
            setResult(RESULT_OK);
            finish();
        } else if (viewId == R.id.write_cancel) { //취소
            setResult(RESULT_CANCELED);
            finish();
        } else if (viewId == R.id.write_camera) { //사진 찍어서 업로드
            captureCamera();
        } else if (viewId == R.id.write_album) { //앨범에서 찾아서 업로드
            getAlbum();
        } else if (viewId == R.id.remove_img) {
            removeImage();
        }

        /* 수정일때 */
        else if (viewId == R.id.write_write) {
            /* 그림 추가할 경우 */
            FirebaseStorage.getInstance().getReference()
                    .child("board")
                    .child(String.valueOf(imageUri.hashCode()))
                    .putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BoardWriteActivity.this, "작성 실패", Toast.LENGTH_SHORT).show();
                        }
                    });

            Query query = FirebaseDatabase.getInstance().getReference().child("board");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (re_num == child.child("num").getValue(Integer.class)) {
                                String ss = write_content.getText().toString();
                                child.child("content").getRef().setValue(ss);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

    }

    /* 이미지 파일 로드 & Storage에 저장 */
    private void captureCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    private void getAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }

    public void cropImage(Uri imageUri) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(imageUri, "image/*");
        cropIntent.putExtra("outputX", 1280); // crop한 이미지의 x축 크기, 결과물의 크기
        cropIntent.putExtra("outputY", 720); // crop한 이미지의 y축 크기
        cropIntent.putExtra("scale", true);
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
    }

    private void removeImage() {
        imageUri = null;
        write_img.setImageDrawable(getDrawable(R.drawable.icon_image));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
            case REQUEST_TAKE_ALBUM:
                if (resultCode == Activity.RESULT_OK) {
                    cropImage(data.getData());
                }
                break;
            case REQUEST_IMAGE_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    imageUri = data.getData();
                    write_img.setImageURI(imageUri);
                }
                break;
        }
    }

    /* 권한 설정 */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_CAMERA && grantResults[0] == android.content.pm.PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "권한이 부족합니다", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}