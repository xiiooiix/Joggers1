package kkt.com.joggers.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import kkt.com.joggers.R;
import kkt.com.joggers.fragment.BoardFragment;
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
    private int count=0;
    private Board board;
    private Comment comment;

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

        /* 권한설정 */
        checkPermission();
    }

    /* Activity 내의 모든 OnClickEvent를 처리한다 */
    @Override
    public void onClick(View v) {
        Log.d("ASD","onclick 들어왔따. ");
        int viewId = v.getId();
        Log.d("ASD","oncli1231213   따. ");

        if (viewId == R.id.write_write) { //작성
            Log.d("ASD","write_write 들어왔따.1 "+ FirebaseStorage.getInstance().getReference().child("board"));
            Log.d("ASD","write_write 들어왔따.2 "+ imageUri);
            Log.d("ASD","write_write 들어왔따.3 "+ FirebaseStorage.getInstance().getReference().child("board").child(String.valueOf(imageUri.hashCode())));




            /* Storage에 이미지 업로드 */
            FirebaseStorage.getInstance().getReference()
                    .child("board")
                    .child(String.valueOf(imageUri.hashCode()))
                    .putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("ASD","sucess????. ");
                            // 작성한 글을 RealTime DB의 Board 테이블에 추가한다
                            /* 작성자, 작성시간 */
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user == null) {
                                Log.d("ASD","user가 null");
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
                                            if(dataSnapshot.getValue() == null) {
                                                Log.d("ASD", "aa = 이건 처음일때");
                                            }
                                            else{
                                                for (DataSnapshot child: dataSnapshot.getChildren()) {
                                                    Log.d("ASD", "aa = 이건 처음이 아닐닐때");
                                                   count = child.child("num").getValue(Integer.class) + 1;
                                                }
                                            }

                                            /* 게시글 data 생성 & INSERT */
                                            List<String> s = new ArrayList<String>();
                                            s.add(user.getDisplayName());
                                            s.add("123");
                                            s.add("ddds");
                                            board = new Board(user.getDisplayName(), time, String.valueOf(imageUrl), String.valueOf(write_content.getText()), false, 0, count);
                                            comment = new Comment(0, "작성자", "작성자", time);

                                            FirebaseDatabase.getInstance().getReference().child("board").push().setValue(board);
                                            FirebaseDatabase.getInstance().getReference().child("comment").child(Integer.toString(count)).push().setValue(comment);
                                            FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(count)).setValue(s);
                                            //FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(count)).setValue("ASD");
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                    //mutableData.child("board/count").setValue(board);
                                    //mutableData.child("comment/count").setValue("123");
                                    return  Transaction.success(mutableData);
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
                            Log.d("ASD","실패~~~ "+123);
                            Toast.makeText(BoardWriteActivity.this, "작성 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
            Log.d("ASD","먼가 이상해 "+123);
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
        Log.d("ASD","사진자르기 시작: "+imageUri);
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(imageUri, "image/*");
        cropIntent.putExtra("outputX", 1000); // crop한 이미지의 x축 크기, 결과물의 크기
        cropIntent.putExtra("outputY", 1000); // crop한 이미지의 y축 크기
        //cropIntent.putExtra("aspectX", 1); // crop 박스의 x축 비율, 1&1이면 정사각형
        //cropIntent.putExtra("aspectY", 1); // crop 박스의 y축 비율
        cropIntent.putExtra("scale", true);
        Log.d("ASD","사진자르기에서 이미지 저장: "+imageUri);
        write_img.setImageURI(imageUri);	//crop이미지 저장//영재
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
            case REQUEST_TAKE_ALBUM:
                if (resultCode == Activity.RESULT_OK) {
                    imageUri = data.getData();
                    cropImage(imageUri);
                }
                break;
            case REQUEST_IMAGE_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d("ASD","사진자르기에서 이미지 저장:123 ");
                   // write_img.setImageURI(data.getData());	//crop이미지 저장//영재
                    Log.d("ASD","사진자르기에서 이미지 저장:111111 ");
                }
                break;
        }
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
