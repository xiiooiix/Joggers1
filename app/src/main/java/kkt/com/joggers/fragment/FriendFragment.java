package kkt.com.joggers.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kkt.com.joggers.R;
import kkt.com.joggers.activity.FindActivity;
import kkt.com.joggers.adapter.FriendAdapter;
import kkt.com.joggers.model.Friend;

public class FriendFragment extends Fragment {

    private static final int REQ_WRITE = 0;
    private EditText editText;
    private ListView listView;
    private ArrayList<Friend> friends;
    private FriendAdapter adapter;

    public FriendFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        listView = view.findViewById(R.id.f_listview);
        editText = view.findViewById(R.id.f_edittext);
        editText.setCursorVisible(false);
        editText.setShowSoftInputOnFocus(false);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("ASD", "검색 ㄱㄱㄱㄱ");
                Intent intent = new Intent(getContext(), FindActivity.class);
                startActivityForResult(intent, REQ_WRITE);
            }
        });


        friends = new ArrayList<Friend>();
        adapter = new FriendAdapter(getContext(), friends ,true);
        listView.setAdapter(adapter);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String id = user.getDisplayName();
        /*
        Friend a =new Friend("a");
        Friend b =new Friend("b");
        Friend c =new Friend("c");
        FirebaseDatabase.getInstance().getReference("friend").child(id).push().setValue(a);
        FirebaseDatabase.getInstance().getReference("friend").child(id).push().setValue(b);
        FirebaseDatabase.getInstance().getReference("friend").child(id).push().setValue(c);
        */


        if(FirebaseDatabase.getInstance().getReference("friend").child(id) != null) {
            FirebaseDatabase.getInstance().getReference("friend")
                    .child(id)
                    .addChildEventListener(new ChildEventAdapter());
        }
        else
            Log.i("ASD", "프랜드 널인교" );


        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_WRITE && resultCode == Activity.RESULT_OK) {
            adapter.notifyDataSetChanged();
        }
        adapter.notifyDataSetChanged();
        Log.i("ASDF", "ㄴㄴ프랜드 리셋!!" );
    }

    public class ChildEventAdapter implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            adapter.addItem(dataSnapshot.getValue(Friend.class));
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Friend friend = dataSnapshot.getValue(Friend.class);
            for (int i = 0; i < adapter.getCount(); i++) {
                Friend mFriend = (Friend) adapter.getItem(i);
                if (mFriend.getId().equals(friend.getId())) {
                    adapter.removeItem(mFriend);
                }
            }

            adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }

    }


}
