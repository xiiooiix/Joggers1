package kkt.com.joggers.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.graphics.BitmapCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import kkt.com.joggers.R;
import kkt.com.joggers.activity.ProfileActivity;
import kkt.com.joggers.controller.OnSuccessGetImage;
import kkt.com.joggers.model.Friend;

/**
 * Created by youngjae on 2017-12-07.
 */

public class FriendAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Friend> friends;
    private boolean flag;

    public FriendAdapter(Context context, ArrayList<Friend> friends, boolean flag) {
        this.context = context;
        this.friends = friends;
        this.flag = flag;
    }

    @Override
    public int getCount() {
        return friends.size();
    }

    @Override
    public Object getItem(int position) {
        return friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.friend_listview, parent, false);
        }


        Friend friend = (Friend) getItem(position);
        final TextView textView = (TextView) view.findViewById(R.id.f_text);
        final ImageView imageView = view.findViewById(R.id.f_img);

        textView.setText(friend.getId());
        String user = textView.getText().toString();


        final View finalView = view;
        /*
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.i("ASDF", "사진 있습니당. :  " + uri);
                ((ImageView)finalView.findViewById(R.id.f_img)).setImageURI(uri);
                //((ImageView)finalView.findViewById(R.id.f_img))
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("ASDF", "사진 없습니당 ㅠㅠ");
            }
        });
*/

        // image_url로 FirebaseStorage 에 저장된 이미지를 가져온다
        FirebaseStorage.getInstance().getReferenceFromUrl("gs://joggers-699c4.appspot.com/user/" + user + ".jpg")
                .getBytes(Long.MAX_VALUE)
                .addOnSuccessListener(new OnSuccessGetImage(imageView, true));


        /*
        // 이미지뷰 동그랗게 하기
        ((ImageView) view.findViewById(R.id.f_img)).setBackground(new ShapeDrawable(new OvalShape()));
        if (Build.VERSION.SDK_INT >= 21) {
            ((ImageView) view.findViewById(R.id.f_img)).setClipToOutline(true);
            //  ((ImageView)view.findViewById(R.id.f_img)).setImageURI();
        }
        */


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", friends.get(position).getId());
                imageView.setDrawingCacheEnabled(true);
                bundle.putParcelable("img", imageView.getDrawingCache());
                bundle.putBoolean("flag", flag);
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });


        //이미지뷰도 해야한다.

        return view;
    }

    public void setItems(ArrayList<Friend> friends) {
        this.friends = friends;
    }

    public void addItem(Friend friend) {
        friends.add(0, friend);
    }

    public void removeItem(Friend friend) {
        friends.remove(friend);
    }

    public void removeAllItem() {
        friends.removeAll(friends);
    }

    public void subList(ArrayList<Friend> list) {
        friends.removeAll(list);
    }

    public boolean findItem(Friend friend) {
        for (int i = 0; i < friends.size(); i++) {
            if (friends.get(i).getId().equals(friend.getId()))
                return false;
        }
        return true;
    }
}
