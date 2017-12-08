package kkt.com.joggers.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import kkt.com.joggers.R;
import kkt.com.joggers.activity.BoardWriteActivity;
import kkt.com.joggers.adapter.BoardAdapter;
import kkt.com.joggers.adapter.MarginItemDecoration;
import kkt.com.joggers.model.Board;

public class BoardFragment extends Fragment implements View.OnClickListener, ChildEventListener {
    private static final int REQ_WRITE = 0;
    private static final String TAG = "joggers.BoardFragment";
    private RecyclerView rcView;
    private BoardAdapter adapter;
    private FloatingActionButton writeBtn;
    private FloatingActionButton myBoardBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /* 레이아웃 설정 */
        View view = inflater.inflate(R.layout.fragment_board, container, false);
        rcView = view.findViewById(R.id.rcView);
        rcView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rcView.addItemDecoration(new MarginItemDecoration(20));

        /* 게시판 리스트 생성 */
        adapter = new BoardAdapter(getContext(), new ArrayList<Board>(), false);
        rcView.setAdapter(adapter);

        /* 글쓰기 버튼 */
        writeBtn = view.findViewById(R.id.btn_write);
        writeBtn.setOnClickListener(this);

        /* 마이 보드 버튼 */
        myBoardBtn = view.findViewById(R.id.btn_myBoard);
        myBoardBtn.setOnClickListener(this);

        /* 게시판 데이터 생성 */
        FirebaseDatabase.getInstance().getReference("board")
                .orderByChild("time")
                .addChildEventListener(this);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_WRITE && resultCode == Activity.RESULT_OK)
            rcView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (v == writeBtn) {
            Intent intent = new Intent(getContext(), BoardWriteActivity.class);
            startActivityForResult(intent, REQ_WRITE);
        } else if (v == myBoardBtn) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_main, new MyBoardFragment(), TAG)
                    .addToBackStack(TAG)
                    .commit();
        }
    }

    /* Firebase RealTime Database으로부터
     * 1 데이터를 받거나 -> onChildAdded
     * 2 변경사항이 생기면 -> onChildAdded, onChildRemoved
     * Listener에 해당하는 Method가 호출된다 */
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