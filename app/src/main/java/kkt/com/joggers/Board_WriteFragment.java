package kkt.com.joggers;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Board_WriteFragment extends Fragment {
    private static final int MY_PERMISSION_CAMERA = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;

    private static FirebaseAuth mAuth;

    private BoardAdapter adapter;
    private BoardFragment boardFragment;

    ImageView img;

    String mCurrentPhotoPath;
    Uri imageUri;
    Uri photoURI, albumURI;
    Uri Uri;
    String imageFileName;
    boolean cropflag;
    public Board_WriteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board__write, container, false);
        Button btn_write = (Button)view.findViewById(R.id.write_wrtie);
        Button btn_cancel = (Button)view.findViewById(R.id.write_cancel);
        Button btn_camera = (Button)view.findViewById(R.id.write_camera);
        Button btn_album = (Button)view.findViewById(R.id.write_album);
        final EditText editText = (EditText)view.findViewById(R.id.wrtie_context);
        img = view.findViewById(R.id.wrtie_image);
        btn_write.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();

                boardFragment = new BoardFragment();

                String context = editText.getText().toString();
                String id = currentUser.getDisplayName();

                boardFragment.setId(id);
                if(img.getDrawable() != null) { //게시글 이미지 있을 때
                    Log.i("ASDF", "WriteFragment img not null: ");
                    boardFragment.setImg(img.getDrawable());
                }
                else{   //게시글 이미지 없을 때
                    Log.i("ASDF", "WriteFragment img null: ");
                    //boardFragment.setImg(null);
                }
                boardFragment.setContext(context);
                boardFragment.setTime("123");
                boardFragment.setWrite(true);


                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_main, boardFragment).commit();

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_main, new BoardFragment()).commit();

            }
        });

        btn_camera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                captureCamera();

            }
        });

        btn_album.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                getAlbum();

            }
        });

        checkPermission();

        return view;
    }

    private void captureCamera(){
        String state = Environment.getExternalStorageState();
        //외장 메모리 검사.
        if(Environment.MEDIA_MOUNTED.equals(state)){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if(takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null){
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
                        providerURI = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", photoFile);
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
            Toast.makeText(getActivity(),"저장공간이 접근 불가능한 기기입니다.",Toast.LENGTH_SHORT).show();
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
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }

    private void getAlbum(){
        Log.d("ASD", "getAlbum - Call");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }

    private void galleryAddPic(){
        Log.d("ASD", "galleryAddPic - Call");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        //해당 경로에 있는 파일을 객체화(새로 파일을 만드는 것)
        File f =new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
        Toast.makeText(getActivity(), "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show();

    }

    public void cropImage1() {
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

    public void cropImage(){
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == getActivity().RESULT_OK) {
                    try {
                        Log.d("ASD", "cropImage - Call");
                        Log.i("ASD", "REQUEST_TAKE_PHOTO OK");
                        galleryAddPic();
                        cropImage1();

                    } catch (Exception e) {
                        Log.e("ASD", "REQUEST_TAKE_PHOTO -" +e.toString());
                    }
                } else {
                    Toast.makeText(getActivity(), "사진찍기를 취소하였습니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_TAKE_ALBUM:
                if (resultCode == getActivity().RESULT_OK) {

                    if(data.getData() != null){
                        try {
                            File albumFile = null;
                            albumFile = createImageFile();
                            photoURI = data.getData();
                            albumURI = Uri.fromFile(albumFile);

                            cropImage();
                        }catch (Exception e){
                            Log.e("ASD", "TAKE_ALBUM_SINGLE - " + e.toString());
                        }
                    }
                }
                break;

            case REQUEST_IMAGE_CROP:
                if (resultCode == getActivity().RESULT_OK) {

                    galleryAddPic();
                    if(cropflag) {
                        img.setImageURI(albumURI);
                        Uri = albumURI;
                    }else {
                        img.setImageURI(imageUri);
                        Uri = imageUri;
                    }
                }
                break;
        }
    }

    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 처음 호출시엔 if()안의 부분은 false로 리턴 됨 -> else{..}의 요청으로 넘어감
            if ((ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) ||
                    (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.CAMERA))) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("알림")
                        .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getActivity().finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_CAMERA:
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                    if (grantResults[i] < 0) {
                        Toast.makeText(getActivity(), "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // 허용했다면 이 부분에서..

                break;
        }
    }

}
