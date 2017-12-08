package kkt.com.joggers.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    private FirebaseUser currentUser;
    private String id;
    private int count, num, position;
    private Map<String, String> map;

    private static final int REQ_WRITE = 0;

    public BoardAdapter(Context context, ArrayList<Board> boards, boolean myBoard) {
        this.context = context;
        this.boards = boards;
        this.myBoard = myBoard;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        // 부모 Context 로부터 inflater 가져오기....
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate layout to View
        View view = inflater.inflate(R.layout.item_board, parent, false);

        // View를 담은 ViewHolder 생성
        final ViewHolder holder = new ViewHolder(view);
        holder.b_btn.setOnClickListener(new OnClickBtn(holder)); //'좋아요' 버튼 onClickListener 설정
        holder.b_del.setOnClickListener(new OnClickDel(holder)); //'삭제' 버튼 onClickListener 설정
        holder.b_re.setOnClickListener(new OnClickRe(holder)); //'수정' 버튼 onClickListener 설정
        holder.b_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(parent.getContext(), CommentActivity.class);
                intent.putExtra("num", holder.getB_num());
                parent.getContext().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //final Board board = boards.get(position);
        board = boards.get(position);       //문제가 생긴다면 위로 바꿔라!!
        holder.b_id.setText(board.getId());
        holder.b_time.setText(board.getTime());
        holder.b_content.setText(board.getContent());
        //holder.b_heartNum.setText(String.valueOf(board.getHeartNum()));
        holder.b_comment.setText("댓글 보기");
        holder.b_num = board.getNum();

        // image_url로 FirebaseStorage 에 저장된 이미지를 가져온다
        FirebaseStorage.getInstance().getReferenceFromUrl(board.getImageUrl())
                .getBytes(Long.MAX_VALUE)
                .addOnSuccessListener(new OnSuccessGetImage(holder.b_img, false));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        id = currentUser.getDisplayName();

        final ViewHolder h = holder;
        Query query = FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(board.getNum()));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("ASD", "흠;;; " + dataSnapshot.getValue());

                if (dataSnapshot.getValue() != null) {

                    List<String> s = (ArrayList<String>) dataSnapshot.getValue();
                    Log.i("ASDG", "sAAA : " + s + "heart:  " + dataSnapshot.getValue());

                    h.b_heartNum.setText(String.valueOf(s.size() - 1));

                    if (id.equals(s.get(0))) {
                        Log.i("ASD", "내 게시물이다. + " + dataSnapshot.getKey() + "   " + s.size());
                    }

                    for (int i = 1; i < s.size(); i++) {
                        if (id.equals(s.get(i))) {
                            board.setHeart(true);
                            Log.i("ASD", "좋아요에 내가 있다!! " + i);
                            h.b_btn.setBackground(context.getResources().getDrawable(R.drawable.heart_full));
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        if (!board.isHeart()) { // 좋아요에 내가 없다!!
            holder.b_btn.setBackground(context.getResources().getDrawable(R.drawable.heart_empty));
        }
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

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView b_id, b_time, b_content, b_heartNum, b_comment;
        private ImageView b_img;
        private Button b_btn, b_del, b_re;
        private int b_num;

        private ViewHolder(View itemVivew) {
            super(itemVivew);
            final View view = itemVivew;
            b_id = itemVivew.findViewById(R.id.board_id);
            b_time = itemVivew.findViewById(R.id.board_time);
            b_content = itemVivew.findViewById(R.id.board_content);
            b_heartNum = itemVivew.findViewById(R.id.board_heartNum);
            b_img = itemVivew.findViewById(R.id.board_imageView);
            b_btn = itemVivew.findViewById(R.id.board_heart);
            b_comment = itemVivew.findViewById(R.id.board_comment);
            b_del = itemVivew.findViewById(R.id.board_delete);
            b_re = itemVivew.findViewById(R.id.board_revise);

            if (myBoard == true) {
                b_del.setVisibility(View.VISIBLE);
                b_re.setVisibility(View.VISIBLE);
            } else {
                b_del.setVisibility(View.INVISIBLE);
                b_re.setVisibility(View.INVISIBLE);
            }
        }

        public int getB_num() {
            return b_num;
        }

        public void setB_num(int b_num) {
            this.b_num = b_num;
        }
    }

    class OnClickBtn implements View.OnClickListener {
        private ViewHolder holder;

        private OnClickBtn(ViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onClick(View v) {
            position = holder.getAdapterPosition();
            num = boards.get(position).getHeartNum();
            board = boards.get(position);
            count = boards.size() - (position + 1);

            /* 하트 누를 때 (증가) */
            if (!board.isHeart()) {
                Log.i("ASD", "num증가~~~~~~" + board.isHeart());
                board.setHeart(true);
                num++;
                board.setHeartNum(num);
                holder.b_btn.setBackground(context.getResources().getDrawable(R.drawable.heart_full));
                holder.b_heartNum.setText(String.valueOf(boards.get(position).getHeartNum()));
                Map<String, Object> m = new HashMap<>();
                m.put(Integer.toString(num), id);
                FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(count)).updateChildren(m);

            }
            /* 하트 누를 때 (감소) */
            else {
                Log.i("ASD", "num감소~~~~~~" + board.isHeart());
                board.setHeart(false);
                num--;
                board.setHeartNum(num);
                holder.b_btn.setBackground(context.getResources().getDrawable(R.drawable.heart_empty));
                holder.b_heartNum.setText(String.valueOf(boards.get(position).getHeartNum()));


                /* heart 리스트 수정 */
                Query query = FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(board.getNum()));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i("ASD", "ssssssssssss : " + dataSnapshot.getValue());
                        List<String> s = (ArrayList<String>) dataSnapshot.getValue();

                        for (int i = 1, k = 1; i < s.size(); i++) {
                            map = new HashMap<>();
                            map.put(Integer.toString(0), s.get(0));
                            if (!s.get(i).equals(id)) {
                                Log.i("ASD", "ssssssssssss : " + s.size() + " dd" + k);
                                map.put(Integer.toString(k), s.get(i));
                                k++;
                            } else
                                Log.i("ASD", "dddddddddddddd " + s.get(i));
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

                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        int n = child.child("num").getValue(Integer.class);

                        if (n == count) {
                            child.child("heartNum").getRef().setValue(num);
                            Log.d("ASD", "잘되는지 모르겟네" + n + " num " + num);
                        } else Log.d("ASD", "잘되는지 모르겟네----" + n);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }


    /* 문제점 리스트 사이즈 해결해야함 */
    class OnClickDel implements View.OnClickListener {
        private ViewHolder holder;

        private OnClickDel(ViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onClick(View v) {
            position = holder.getAdapterPosition();
            board = boards.get(position);
            Log.i("ASDF", " --: " + board.getNum());

            Query query = FirebaseDatabase.getInstance().getReference().child("board");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        int n = child.child("num").getValue(Integer.class);

                        if (n == board.getNum()) {
                            child.getRef().removeValue();
                            FirebaseDatabase.getInstance().getReference().child("comment").child(Integer.toString(n)).removeValue();
                            FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(n)).removeValue();
                            Log.d("ASDF", "ddddd 잘돼  " + n);
                        } else Log.d("ASDF", "ddddd 안돼  " + n);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            boards.remove(board);
            notifyDataSetChanged();

        }
    }

    class OnClickRe implements View.OnClickListener {
        private ViewHolder holder;

        private OnClickRe(ViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onClick(View v) {
            position = holder.getAdapterPosition();
            board = boards.get(position);

            Intent intent = new Intent(v.getContext(), BoardWriteActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("num", board.getNum());
            bundle.putString("content", board.getContent());
            holder.b_img.setDrawingCacheEnabled(true);
            bundle.putParcelable("img", holder.b_img.getDrawingCache());
            intent.putExtras(bundle);

            v.getContext().startActivity(intent);

        }

    }

}