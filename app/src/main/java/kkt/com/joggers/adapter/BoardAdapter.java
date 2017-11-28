package kkt.com.joggers.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import kkt.com.joggers.R;
import kkt.com.joggers.activity.CommentActivity;
import kkt.com.joggers.model.Board;
import kkt.com.joggers.model.Comment;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Board> boards;
    private Board board;
    private int count, num, position;

    public BoardAdapter(Context context, ArrayList<Board> boards) {
        this.context = context;
        this.boards = boards;
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

        holder.b_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ASD", "comment 클릭 ㅋㅋ");
                Intent intent = new Intent(parent.getContext(), CommentActivity.class);
                intent.putExtra("num", holder.getB_num());
                parent.getContext().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Board board = boards.get(position);

        holder.b_id.setText(board.getId());
        holder.b_time.setText(board.getTime());
        holder.b_content.setText(board.getContent());
        holder.b_heartNum.setText(String.valueOf(board.getHeartNum()));
        holder.b_comment.setText("댓글 보기");
        holder.b_num = board.getNum();

        // image_url로 FirebaseStorage 에 저장된 이미지를 가져온다
        FirebaseStorage.getInstance().getReferenceFromUrl(board.getImageUrl())
                .getBytes(Long.MAX_VALUE)
                .addOnSuccessListener(new OnSuccessGetImage(holder.b_img));

        //ㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁ
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

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView b_id, b_time, b_content, b_heartNum ,b_comment;
        private ImageView b_img;
        private Button b_btn;
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
            count = boards.size() - (position+1);
            FirebaseUser currentUser =  FirebaseAuth.getInstance().getCurrentUser();
            String id = currentUser.getDisplayName();

            // TODO 변경된 '좋아요' 값을 DB에 저장할 것!
            if (!board.getHeart()) {
                num++;
                board.setHeartNum(num);
                board.setHeart(true);
                holder.b_btn.setBackground(context.getResources().getDrawable(R.drawable.heart_full));
                holder.b_heartNum.setText(String.valueOf(boards.get(position).getHeartNum()));
                FirebaseDatabase.getInstance().getReference().child("heart").child(Integer.toString(count)).push().setValue(id);

            } else {
                num--;
                board.setHeartNum(num);
                board.setHeart(false);
                holder.b_btn.setBackground(context.getResources().getDrawable(R.drawable.heart_empty));
                holder.b_heartNum.setText(String.valueOf(boards.get(position).getHeartNum()));
            }

            Query query = FirebaseDatabase.getInstance().getReference().child("board");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        int n = child.child("num").getValue(Integer.class);
                        if(n == count){
                            child.child("heartNum").getRef().setValue(num);
                            Log.d("ASD", "잘되는지 모르겟네"+n);
                            //Fireda
                        }else Log.d("ASD", "잘되는지 모르겟네----"+n);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

        }
    }

    class OnSuccessGetImage implements OnSuccessListener<byte[]> {
        private ImageView b_img;

        private OnSuccessGetImage(ImageView b_img) {
            this.b_img = b_img;
        }

        @Override
        public void onSuccess(byte[] bytes) {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            b_img.setImageBitmap(bmp);
        }
    }
}