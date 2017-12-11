package kkt.com.joggers.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import kkt.com.joggers.R;
import kkt.com.joggers.activity.ProfileActivity;
import kkt.com.joggers.controller.OnSuccessGetImage;
import kkt.com.joggers.model.Friend;

public class FriendAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Friend> friends;
    private boolean flag;

    public FriendAdapter(Context context, ArrayList<Friend> friends, boolean flag) {
        this.context = context;
        this.friends = friends;
        this.flag = flag;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
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
        final Holder holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.friend_listview, parent, false);
            holder = new Holder();
            holder.imageView = view.findViewById(R.id.f_img);
            holder.textView = view.findViewById(R.id.f_text);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        Friend friend = (Friend) getItem(position);
        holder.textView.setText(friend.getId());
        String user = holder.textView.getText().toString();

        // image_url로 FirebaseStorage 에 저장된 이미지를 가져온다
        FirebaseStorage.getInstance().getReferenceFromUrl("gs://joggers-699c4.appspot.com/user/" + user + ".jpg")
                .getBytes(Long.MAX_VALUE)
                .addOnSuccessListener(new OnSuccessGetImage(holder.imageView))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.imageView.setImageDrawable(null);
                    }
                });


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
                holder.imageView.setDrawingCacheEnabled(true);
                bundle.putParcelable("img", holder.imageView.getDrawingCache());
                bundle.putBoolean("flag", flag);
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });

        return view;
    }

    class Holder {
        TextView textView;
        ImageView imageView;
    }


    public void setItems(ArrayList<Friend> friends) {
        this.friends = friends;
    }

    public void addItem(Friend friend) { friends.add(0, friend);  }

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
