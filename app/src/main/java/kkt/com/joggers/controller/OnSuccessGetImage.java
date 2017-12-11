package kkt.com.joggers.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;

public class OnSuccessGetImage implements OnSuccessListener<byte[]> {
    private final ImageView imageView;

    public OnSuccessGetImage(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    public void onSuccess(byte[] bytes) {
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageView.setImageBitmap(bmp);
    }

}