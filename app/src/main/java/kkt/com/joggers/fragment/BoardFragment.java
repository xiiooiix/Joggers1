package kkt.com.joggers.fragment;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kkt.com.joggers.R;
import kkt.com.joggers.activity.BoardWriteActivity;
import kkt.com.joggers.adapter.BoardAdapter;
import kkt.com.joggers.adapter.MarginItemDecoration;
import kkt.com.joggers.model.Board;

public class BoardFragment extends Fragment {
    private static final int REQ_WRITE = 0;
    private RecyclerView rcView;
    private BoardAdapter adapter;
    private int num=0;
    private FirebaseUser currentUser;
    private String id;
    private boolean a=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentUser =  FirebaseAuth.getInstance().getCurrentUser();
        id = currentUser.getDisplayName();

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

        Log.i("ASD", "view ㄱㄱ");
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
    public class ChildEventAdapter implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i("ASD", "onChildAdded3  + " + dataSnapshot.getValue(Board.class));
            Board b= dataSnapshot.getValue(Board.class);
            //b.set 사진
            adapter.addItem(dataSnapshot.getValue(Board.class));
            adapter.notifyDataSetChanged();


            //Log.i("ASD", "넌 뭐냐?  "+FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(num)).child("0").);
           // if(id.equals(FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(num)).child("0").getKey()))
            //    Log.i("ASD","내 게시물이다. + "+num);




            Query query = FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(num));
            Log.i("ASD","넘값 뭐냐?? . "+num);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("ASD", "흠;;; " + dataSnapshot.getValue());
                    List<String> s= (ArrayList<String>)dataSnapshot.getValue();

                    if(id.equals(s.get(0)))
                        Log.i("ASD","내 게시물이다. + "+dataSnapshot.getKey() +"   "+ s.size());

                    for(int i=1; i<s.size(); i++){
                        if(id.equals(s.get(i))){
                            a= true;
                            //adapter.//여기서부터 하면된다 뭘하냐면 여긴 좋아요 중에 내 아이디가 있다면 빨간하트로!!!

                        }

                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            num++;
            Log.i("ASD","넘값 증가합니다. "+num);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.i("ASD", "onChildRemoved ㄱㄱ");
            adapter.removeItem(dataSnapshot.getValue(Board.class));
            adapter.notifyDataSetChanged();

            num--;
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }

    }

}