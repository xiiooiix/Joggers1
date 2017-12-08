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
    private boolean ovalFrame;

    public OnSuccessGetImage(ImageView b_img, boolean ovalFrame) {
        this.b_img = b_img;
        this.ovalFrame = ovalFrame;
    }

    @Override
    public void onSuccess(byte[] bytes) {
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        b_img.setImageBitmap(bmp);
        /*
        if (ovalFrame) {
            b_img.setBackground(new ShapeDrawable(new OvalShape()));
            if (Build.VERSION.SDK_INT >= 21) {
                Log.i("ASDF", "DDDDDDDDDDDDDDDD");
                b_img.setClipToOutline(true);
            }
        }
        */
    }

}