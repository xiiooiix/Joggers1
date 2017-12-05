package kkt.com.joggers.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import kkt.com.joggers.R;
import kkt.com.joggers.adapter.BoardAdapter;
import kkt.com.joggers.adapter.MarginItemDecoration;
import kkt.com.joggers.model.Board;

public class MyBoardFragment extends Fragment {


    private static final int REQ_WRITE = 0;
    private RecyclerView rcView;
    private BoardAdapter adapter;

    private FirebaseUser currentUser;
    private String id;

    public MyBoardFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /* 레이아웃 설정 */
        View view = inflater.inflate(R.layout.fragment_my_board, container, false);
        rcView = view.findViewById(R.id.rcView2);
        rcView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rcView.addItemDecoration(new MarginItemDecoration(20));

        /* 게시판 리스트 생성 */
        adapter = new BoardAdapter(getContext(), new ArrayList<Board>(), true);
        rcView.setAdapter(adapter);
        // Inflate the layout for this fragment


        FirebaseDatabase.getInstance().getReference()
                .child("board")
                .orderByChild("time")
                .addChildEventListener(new ChildEventAdapter());

        currentUser =  FirebaseAuth.getInstance().getCurrentUser();
        id = currentUser.getDisplayName();
        return view;
    }

    public class ChildEventAdapter implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i("ASDF", "onChildAdd ㄱㄱ " + dataSnapshot.child("id").getValue());
            if(id.equals(dataSnapshot.child("id").getValue())==true) {
                adapter.addItem(dataSnapshot.getValue(Board.class));
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.i("ASD", "onChildRemoved ㄱㄱ");
            if(id.equals(dataSnapshot.child("id").getValue())==true) {
                adapter.removeItem(dataSnapshot.getValue(Board.class));
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }

    }

}
