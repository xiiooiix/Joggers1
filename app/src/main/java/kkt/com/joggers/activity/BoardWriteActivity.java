package kkt.com.joggers.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kkt.com.joggers.R;
import kkt.com.joggers.controller.OnSuccessGetImage;
import kkt.com.joggers.model.Board;
import kkt.com.joggers.model.Comment;

public class BoardWriteActivity extends AppCompatActivity implements ValueEventListener, View.OnClickListener {
    private static final int MY_PERMISSION_CAMERA = 0;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_TAKE_ALBUM = 2;
    private static final int REQUEST_IMAGE_CROP = 3;

    private Button cameraBtn, albumBtn, removeBtn;
    private ImageView imageView;
    private EditText contentEditText;
    private Button writeBtn;
    private Button cancelBtn;

    private String key;
    private Board oldBoard;
    private boolean imagePerm = false; // 사진, 스토리지 사용 권한
    private Uri imageUri;
    private Uri Uri, photoURI, albumURI;
    private String imageUrl;
    private String imageFileName;
    private boolean cropflag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_write);

        /* 입력폼 */
        cameraBtn = findViewById(R.id.write_camera);
        albumBtn = findViewById(R.id.write_album);
        removeBtn = findViewById(R.id.remove_img);
        imageView = findViewById(R.id.write_image);
        contentEditText = findViewById(R.id.write_content);
        writeBtn = findViewById(R.id.write_write);
        cancelBtn = findViewById(R.id.write_cancel);

        /* OnClickListener 설정 */
        cameraBtn.setOnClickListener(this);
        albumBtn.setOnClickListener(this);
        removeBtn.setOnClickListener(this);
        writeBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        /* key가 넘어오면 게시글을 수정한다 */
        key = getIntent().getStringExtra("key");
        if (key == null)
            return;
        writeBtn.setText("수정");
        FirebaseDatabase.getInstance().getReference("board/" + key)
                .addListenerForSingleValueEvent(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        oldBoard = dataSnapshot.getValue(Board.class);
        if (oldBoard == null)
            return;
        if (oldBoard.getImageUrl() != null)
            FirebaseStorage.getInstance().getReferenceFromUrl(oldBoard.getImageUrl())
                    .getBytes(Long.MAX_VALUE)
                    .addOnSuccessListener(new OnSuccessGetImage(imageView));
        imageUrl = oldBoard.getImageUrl();
        contentEditText.setText(oldBoard.getContent());
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }

    /* Activity 내의 모든 OnClickEvent를 처리한다 */
    @Override
    public void onClick(View view) {
        if (view == cameraBtn) { // 사진 찍어서 업로드
            checkPermission();
            if (imagePerm)
                captureCamera();
        } else if (view == albumBtn) { // 앨범에서 찾아서 업로드
            checkPermission();
            if (imagePerm)
                getAlbum();
        } else if (view == removeBtn) // 올린 사진 제거
            removeImage();

        else if (view == writeBtn) { // 작성
            if (Uri != null)
                uploadImage();
            else
                writeBoard();
            finish();
        } else if (view == cancelBtn) { //취소
            if (Uri != null) {
                getContentResolver().delete(Uri, null, null);
            }
            finish();
        }
    }

    /* 이미지 파일 로드 & 기기 Storage에 저장 */

    private void captureCamera(){
        String state = Environment.getExternalStorageState();
        //외장 메모리 검사.
        if(Environment.MEDIA_MOUNTED.equals(state)){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if(takePictureIntent.resolveActivity(getPackageManager()) != null){
                File photoFile = null;

                try {
                    photoFile = createImageFile();
                }catch (IOException e){
                    Log.d("ASD", "captureCamera - " + e.toString());
                }

                if(photoFile != null){
                    int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                    Uri providerURI;
                    if(currentapiVersion >= 24) {
                        providerURI = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                        // providerURI=FileProvider.getUriForFile()
                    }
                    else{
                        providerURI = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), imageFileName));
                    }
                    imageUri = providerURI;

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);
                    startActivityForResult(takePictureIntent,REQUEST_TAKE_PHOTO);
                }
            }
        }
        else{
            Toast.makeText(this,"저장공간이 접근 불가능한 기기입니다.",Toast.LENGTH_SHORT).show();
            return;
        }
    }


    public File createImageFile() throws IOException {
        //create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //String imageFileName = "JPEG_" + timeStamp + ".jpg";
        imageFileName = "JPEG_" + timeStamp + ".jpg";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "gyeom");

        if(!storageDir.exists()){
            Log.d("ASD", "createImageFile + "+ storageDir.toString());
            storageDir.mkdirs();
        }

        imageFile = new File(storageDir, imageFileName);

        return imageFile;
    }

    private void getAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }

    public void cropImage1() {  //사진 찍고 무조건 잘라내기
        cropflag =false;
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(imageUri, "image/*");
        //cropIntent.putExtra("outputX", 200); // crop한 이미지의 x축 크기, 결과물의 크기
        //cropIntent.putExtra("outputY", 200); // crop한 이미지의 y축 크기
        cropIntent.putExtra("aspectX", 1); // crop 박스의 x축 비율, 1&1이면 정사각형
        cropIntent.putExtra("aspectY", 1); // crop 박스의 y축 비율
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("output", imageUri); // 크랍된 이미지를 해당 경로에 저장
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
    }

    public void cropImage(){    //ㅅ앨번 선택하고 잘라내기
        cropflag = true;
        Log.d("ASD", "cropImage - Call");
        Log.d("ASD", "photoURI : " + photoURI + " / albumURI : " + albumURI);

        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        // 50x50픽셀미만은 편집할 수 없다는 문구 처리 + 갤러리, 포토 둘다 호환하는 방법
        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoURI, "image/*");
        //cropIntent.putExtra("outputX", 200); // crop한 이미지의 x축 크기, 결과물의 크기
        //cropIntent.putExtra("outputY", 200); // crop한 이미지의 y축 크기
        cropIntent.putExtra("aspectX", 1); // crop 박스의 x축 비율, 1&1이면 정사각형
        cropIntent.putExtra("aspectY", 1); // crop 박스의 y축 비율
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("output", albumURI); // 크랍된 이미지를 해당 경로에 저장
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
    }


    private void removeImage() {
        imageUri = null;
        imageUrl = null;
        imageView.setImageDrawable(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                cropImage1();
                break;
            case REQUEST_TAKE_ALBUM:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        File albumFile = null;
                        albumFile = createImageFile();
                        photoURI = data.getData();
                        albumURI = imageUri.fromFile(albumFile);

                        cropImage();
                    }catch (Exception e){
                        Log.e("ASD", "TAKE_ALBUM_SINGLE - " + e.toString());
                    }
                }
                break;
            case REQUEST_IMAGE_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    if(cropflag) {
                        imageView.setImageURI(albumURI);
                        Uri = albumURI;
                    }else {
                        imageView.setImageURI(imageUri);
                        Uri = imageUri;
                    }
                }
                break;
        }
    }

    /* Storage에 이미지 업로드 */
    private void uploadImage() {
        FirebaseStorage.getInstance().getReference("board/" + Uri.hashCode())
                .putFile(Uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //getContentResolver().delete(imageUri, null, null);
                        imageUrl = String.valueOf(taskSnapshot.getDownloadUrl());
                        writeBoard();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BoardWriteActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /* 작성한 게시글을 서버에 저장 */
    private void writeBoard() {
        if (oldBoard == null) { // 게시글 새로 작성
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null)
                return;
            String time = SimpleDateFormat.getDateTimeInstance().format(new Date());
            oldBoard = new Board(user.getDisplayName(), time, imageUrl, contentEditText.getText().toString());
            FirebaseDatabase.getInstance().getReference("board").push().setValue(oldBoard);
        } else { // 게시글 수정
            if (oldBoard.getImageUrl() != null && !oldBoard.getImageUrl().equals(imageUrl))
                FirebaseStorage.getInstance().getReferenceFromUrl(oldBoard.getImageUrl()).delete();
            oldBoard.setImageUrl(imageUrl);
            oldBoard.setContent(contentEditText.getText().toString());
            FirebaseDatabase.getInstance().getReference("board/" + key).setValue(oldBoard);
        }
    }

    /* 권한 설정 */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            imagePerm = true;
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_CAMERA && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            imagePerm = true;
    }

}
