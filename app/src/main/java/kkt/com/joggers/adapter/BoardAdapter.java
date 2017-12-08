package kkt.com.joggers.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kkt.com.joggers.R;
import kkt.com.joggers.activity.BoardWriteActivity;
import kkt.com.joggers.activity.CommentActivity;
import kkt.com.joggers.controller.OnSuccessGetImage;
import kkt.com.joggers.model.Board;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Board> boards;
    private boolean myBoard;

    private Board board;
    private int count;
    private int num;
    private Map<String, String> map;

    public BoardAdapter(Context context, ArrayList<Board> boards, boolean myBoard) {
        this.context = context;
        this.boards = boards;
        this.myBoard = myBoard;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext()); // 부모 Context 로부터 inflater 가져오기....
        View view = inflater.inflate(R.layout.item_board, parent, false); // Inflate layout to View
        return new ViewHolder(view); // View를 담은 ViewHolder 생성
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        board = boards.get(position);
        holder.b_id.setText(board.getId());
        holder.b_time.setText(board.getTime());
        holder.b_content.setText(board.getContent());
        holder.b_comment.setText("댓글 보기");
        holder.b_num = board.getNum();

        if (board.getImageUrl() != null) {
            // image_url로 FirebaseStorage 에 저장된 이미지를 가져온다
            FirebaseStorage.getInstance().getReferenceFromUrl(board.getImageUrl())
                    .getBytes(Long.MAX_VALUE)
                    .addOnSuccessListener(new OnSuccessGetImage(holder.b_img));
        }

        Query query = FirebaseDatabase.getInstance().getReference("heart").child(String.valueOf(board.getNum()));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;
                List<String> s = (ArrayList<String>) dataSnapshot.getValue();
                holder.b_heartNum.setText(String.valueOf(s.size() - 1));
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null)
                    return;
                for (int i = 1; i < s.size(); i++) {
                    if (s.get(i).equals(currentUser.getDisplayName())) {
                        board.setHeart(true);
                        holder.b_btn.setBackground(context.getResources().getDrawable(R.drawable.heart_full));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        if (!board.isHeart()) // 좋아요에 내가 없다!!
            holder.b_btn.setBackground(context.getResources().getDrawable(R.drawable.heart_empty));
    }

    @Override
    public int getItemCount() {
        return boards.size();
    }

    public void addItem(Board board) {
        boards.add(0, board);
    }

    public void removeItem(Board board) {
        boards.remove(board);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView b_id, b_time, b_content, b_heartNum, b_comment;
        private ImageView b_img;
        private Button b_btn, b_del, b_re;
        private int b_num;

        private ViewHolder(View itemVivew) {
            super(itemVivew);
            b_id = itemVivew.findViewById(R.id.board_id);
            b_time = itemVivew.findViewById(R.id.board_time);
            b_content = itemVivew.findViewById(R.id.board_content);
            b_heartNum = itemVivew.findViewById(R.id.board_heartNum);
            b_img = itemVivew.findViewById(R.id.board_imageView);
            b_btn = itemVivew.findViewById(R.id.board_heart);
            b_comment = itemVivew.findViewById(R.id.board_comment);

            b_btn.setOnClickListener(this); //'좋아요' 버튼 onClickListener 설정
            b_comment.setOnClickListener(this);

            if (myBoard) { // 나의 게시물 보기
                b_del = itemVivew.findViewById(R.id.board_delete);
                b_re = itemVivew.findViewById(R.id.board_revise);
                b_del.setVisibility(View.VISIBLE);
                b_re.setVisibility(View.VISIBLE);
                b_del.setOnClickListener(this); //'삭제' 버튼 onClickListener 설정
                b_re.setOnClickListener(this); //'수정' 버튼 onClickListener 설정
            }
        }

        @Override
        public void onClick(View v) {
            if (v == b_btn) {
                int position = getAdapterPosition();
                num = boards.get(position).getHeartNum();
                board = boards.get(position);
                count = boards.size() - (position + 1);

                if (!board.isHeart()) { // 하트 누를 때 (증가)
                    board.setHeart(true);
                    num++;
                    board.setHeartNum(num);
                    b_btn.setBackground(context.getResources().getDrawable(R.drawable.heart_full));
                    b_heartNum.setText(String.valueOf(boards.get(position).getHeartNum()));
                    Map<String, Object> m = new HashMap<>();
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser == null)
                        return;
                    m.put(Integer.toString(num), currentUser.getDisplayName());
                    FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(count)).updateChildren(m);
                } else { // 하트 누를 때 (감소)
                    board.setHeart(false);
                    num--;
                    board.setHeartNum(num);
                    b_btn.setBackground(context.getResources().getDrawable(R.drawable.heart_empty));
                    b_heartNum.setText(String.valueOf(boards.get(position).getHeartNum()));

                    /* heart 리스트 수정 */
                    Query query = FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(board.getNum()));
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<String> s = (ArrayList<String>) dataSnapshot.getValue();
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (currentUser == null)
                                return;
                            for (int i = 1, k = 1; i < s.size(); i++) {
                                map = new HashMap<>();
                                map.put(Integer.toString(0), s.get(0));
                                if (!s.get(i).equals(currentUser.getDisplayName())) {
                                    map.put(Integer.toString(k), s.get(i));
                                    k++;
                                }
                            }
                            FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(count)).setValue(map);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }

            /* 보드의 heartnum 수정 */
                Query query = FirebaseDatabase.getInstance().getReference().child("board");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren())
                            if (child.child("num").getValue(Integer.class) == count)
                                child.child("heartNum").getRef().setValue(num);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            } else if (v == b_del) {
                board = boards.get(getAdapterPosition());
                Query query = FirebaseDatabase.getInstance().getReference().child("board");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (child.child("num").getValue(Integer.class) == board.getNum()) {
                                child.getRef().removeValue();
                                FirebaseDatabase.getInstance().getReference().child("comment").child(Integer.toString(board.getNum())).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(board.getNum())).removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                boards.remove(board);
                notifyDataSetChanged();
            } else if (v == b_re) {
                int position = getAdapterPosition();
                board = boards.get(position);

                Intent intent = new Intent(v.getContext(), BoardWriteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("num", board.getNum());
                bundle.putString("content", board.getContent());
                b_img.setDrawingCacheEnabled(true);
                bundle.putParcelable("img", b_img.getDrawingCache());
                intent.putExtras(bundle);

                v.getContext().startActivity(intent);
            } else if (v == b_comment) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("num", b_num);
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
    }

}