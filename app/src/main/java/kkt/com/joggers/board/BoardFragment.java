package kkt.com.joggers.board;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import kkt.com.joggers.R;
import kkt.com.joggers.share.MarginItemDecoration;

public class BoardFragment extends Fragment {
    private static final int REQ_WRITE = 0;
    private RecyclerView rcView;
    private BoardAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* 레이아웃 설정 */
        View view = inflater.inflate(R.layout.fragment_board, container, false);
        rcView = view.findViewById(R.id.rcView);
        rcView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rcView.addItemDecoration(new MarginItemDecoration(20));

        /* 게시판 리스트 생성 */
        adapter = new BoardAdapter(getContext(), new ArrayList<Board>());
        rcView.setAdapter(adapter);

        /* 글쓰기 버튼 */
        view.findViewById(R.id.btn_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("ASD", "글작성 ㄱㄱ");
                Intent intent = new Intent(getContext(), BoardWriteActivity.class);
                startActivityForResult(intent, REQ_WRITE);
            }
        });

        /* 게시판 데이터 생성 */
        FirebaseDatabase.getInstance().getReference()
                .child("board")
                .orderByChild("time")
                .addChildEventListener(new ChildEventAdapter());

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_WRITE && resultCode == Activity.RESULT_OK)
            rcView.getAdapter().notifyDataSetChanged();
    }

    /* Firebase RealTime Database으로부터
     * 1 데이터를 받거나 -> onChildAdded
     * 2 변경사항이 생기면 -> onChildAdded, onChildRemoved
     * Listener에 해당하는 Method가 호출된다 */
    class ChildEventAdapter implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            adapter.addItem(dataSnapshot.getValue(Board.class));
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            adapter.removeItem(dataSnapshot.getValue(Board.class));
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
