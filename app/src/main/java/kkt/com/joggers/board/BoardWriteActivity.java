package kkt.com.joggers.board;

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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

import kkt.com.joggers.R;

public class BoardWriteActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int MY_PERMISSION_CAMERA = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;
    private static final String TAG = "Board";

    private Uri imageUri;
    private ImageView write_img;
    private EditText write_content;

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
        int viewId = v.getId();

        if (viewId == R.id.write_write) { //작성
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
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user == null)
                                return;
                            String time = SimpleDateFormat.getDateTimeInstance().format(new Date());
                            Uri imageUrl = taskSnapshot.getDownloadUrl();

                            /* 게시글 data 생성 & INSERT */
                            Board board = new Board(user.getDisplayName(), time, String.valueOf(imageUrl), String.valueOf(write_content.getText()), false, 0);
                            FirebaseDatabase.getInstance().getReference().child("board").push().setValue(board);
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
        cropIntent.putExtra("outputX", 1000); // crop한 이미지의 x축 크기, 결과물의 크기
        cropIntent.putExtra("outputY", 1000); // crop한 이미지의 y축 크기
        //cropIntent.putExtra("aspectX", 1); // crop 박스의 x축 비율, 1&1이면 정사각형
        //cropIntent.putExtra("aspectY", 1); // crop 박스의 y축 비율
        cropIntent.putExtra("scale", true);
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
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
