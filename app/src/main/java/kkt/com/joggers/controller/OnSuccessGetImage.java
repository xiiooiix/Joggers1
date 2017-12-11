package kkt.com.joggers.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;

public class OnSuccessGetImage implements OnSuccessListener<byte[]> {
    private ImageView b_img;

    public OnSuccessGetImage(ImageView b_img) {
        this.b_img = b_img;
    }

    @Override
    public void onSuccess(byte[] bytes) {
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        b_img.setImageBitmap(bmp);
    }

}