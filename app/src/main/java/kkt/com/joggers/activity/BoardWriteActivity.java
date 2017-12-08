package kkt.com.joggers.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import java.io.File;
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
    private static final String TAG = "Board";

    private Uri imageUri;
    private ImageView write_img;
    private EditText write_content;
    private int count = 0;
    private Board board;
    private Comment comment;
    private String re_imageUrl;
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
                            Log.d("ASD", "sucess????. ");
                            // 작성한 글을 RealTime DB의 Board 테이블에 추가한다
                            /* 작성자, 작성시간 */
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user == null) {
                                Log.d("ASD", "user가 null");
                                return;
                            }

                            final String time = SimpleDateFormat.getDateTimeInstance().format(new Date());
                            final Uri imageUrl = taskSnapshot.getDownloadUrl();

                            FirebaseDatabase.getInstance().getReference().child("board").runTransaction(new Transaction.Handler() {
                                @Override
                                public Transaction.Result doTransaction(final MutableData mutableData) {

                                    Query lastQuery = FirebaseDatabase.getInstance().getReference().child("board").orderByKey().limitToLast(1);
                                    lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue() == null) {
                                                Log.d("ASD", "aa = 이건 처음일때");
                                            } else {
                                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                                    count = child.child("num").getValue(Integer.class) + 1;
                                                }
                                            }
                                             /* 게시글 data 생성 & INSERT */
                                            board = new Board(user.getDisplayName(), time, String.valueOf(imageUrl), String.valueOf(write_content.getText()), 0, count);
                                            //board = new Board("df", time, String.valueOf(imageUrl), String.valueOf(write_content.getText()), 0, count);
                                            comment = new Comment(0, "작성자", "작성자", time);
                                            Map<String, String> map = new HashMap<>();
                                            map.put("0", user.getDisplayName());
                                            //map.put("0","df");
                                            //map.put("1", "dddd");
                                            //map.put("2", "Cvd");
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
                                    Log.d(TAG, "Transaction:onComplete:" + databaseError);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("ASD", "실패~~~ " + 123);
                            Toast.makeText(BoardWriteActivity.this, "작성 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
            Log.d("ASD", "먼가 이상해 " + 123);
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
        }

        /* 수정일때 */
        else if (viewId == R.id.write_write) {
            Log.i("ASDF", "크크크킄하하하하하하 " + re_num);


            /* 그림 추가할 경우 */
            FirebaseStorage.getInstance().getReference()
                    .child("board")
                    .child(String.valueOf(imageUri.hashCode()))
                    .putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("ASD", "sucess????. ");
                            // 작성한 글을 RealTime DB의 Board 테이블에 추가한다
                            /* 작성자, 작성시간 */
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user == null) {
                                Log.d("ASD", "user가 null");
                                return;
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("ASD", "실패~~~ " + 123);
                            Toast.makeText(BoardWriteActivity.this, "작성 실패", Toast.LENGTH_SHORT).show();
                        }
                    });


            Query lastQuery = FirebaseDatabase.getInstance().getReference().child("board");
            lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        Log.d("ASDF", "ㄱㄱㄱㄱㄱㄱㄱaa = 이건 처음일때");
                    } else {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            Log.d("ASDF", "ㄱㄱㄱㄱ -- 들어옵니다. ㄱㄱ" + child.child("num").getValue());
                            //int nn = (int) child.child("num").getValue();

                            if (re_num == child.child("num").getValue(Integer.class)) {
                                Log.d("ASDF", "ㄱㄱㄱㄱㄱ = 이게 되나요//??" + child.child("content").getValue() + " :zzz  ");

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
        Log.d("ASD", "사진자르기 시작: " + imageUri);
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(imageUri, "image/*");
        cropIntent.putExtra("outputX", 1000); // crop한 이미지의 x축 크기, 결과물의 크기
        cropIntent.putExtra("outputY", 1000); // crop한 이미지의 y축 크기
        //cropIntent.putExtra("aspectX", 1); // crop 박스의 x축 비율, 1&1이면 정사각형
        //cropIntent.putExtra("aspectY", 1); // crop 박스의 y축 비율
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

    /* 작성한 게시글을 서버에 저장 */
    private void writeBoard(Board board) {
        /* 게시글 data 생성 & INSERT */
        FirebaseDatabase.getInstance().getReference().child("board").push().setValue(board);
        if (imageUri != null)
            new File(imageUri.getPath()).delete();
    }

    /* 권한 설정 */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 처음 호출시엔 if()안의 부분은 false로 리턴 됨 -> else{..}의 요청으로 넘어감
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_CAMERA) {
            for (int grantResult : grantResults)
                if (grantResult < 0)
                    Log.i(TAG, "권한획득 실패:" + grantResult);

            // TODO 모두 허용했다면 이 부분에서..
        }
    }

}