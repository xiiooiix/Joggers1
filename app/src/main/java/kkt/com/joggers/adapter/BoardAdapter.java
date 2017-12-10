package kkt.com.joggers.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import kkt.com.joggers.R;
import kkt.com.joggers.activity.BoardWriteActivity;
import kkt.com.joggers.activity.CommentActivity;
import kkt.com.joggers.controller.OnSuccessGetImage;
import kkt.com.joggers.model.Board;

public class BoardAdapter extends DataLoadLimitAdapter {
    private final List<String> keys = new ArrayList<>();
    private final List<Board> boards = new ArrayList<>();
    private boolean myBoardFilter = false;
    private boolean myHeartFilter = false;

    public BoardAdapter(Context context) {
        super(context);
        update();
    }

    @Override
    protected void update() {
        FirebaseDatabase.getInstance().getReference("board")
                .removeEventListener(this);
        FirebaseDatabase.getInstance().getReference("board")
                .orderByChild("time")
                .limitToLast(loadLimit)
                .addValueEventListener(this);
    }

    public void changeMyBoardFilter() {
        myBoardFilter = !myBoardFilter;
        update();
    }

    public void changeMyHeartFilter() {
        myHeartFilter = !myHeartFilter;
        update();
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BoardAdapter.ViewHolder mHolder = (ViewHolder) holder;
        Board board = boards.get(position);

        mHolder.b_id.setText(board.getId());
        mHolder.b_time.setText(board.getTime());
        mHolder.b_content.setText(board.getContent());

        // 이미지 삽입
        if (board.getImageUrl() != null)
            FirebaseStorage.getInstance().getReferenceFromUrl(board.getImageUrl())
                    .getBytes(Long.MAX_VALUE)
                    .addOnSuccessListener(new OnSuccessGetImage(mHolder.b_img));
        else
            mHolder.b_img.setImageDrawable(null);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;

        // 좋아요 여부, 개수 확인
        int count = 0;
        boolean like = false;
        int res = R.drawable.heart_empty;
        if (board.getHeart() != null) {
            like = board.getHeart().containsKey(user.getDisplayName());
            res = (like) ? R.drawable.heart_full : R.drawable.heart_empty;
            count = board.getHeart().size();
        }
        mHolder.b_heart.setImageDrawable(context.getResources().getDrawable(res, null));
        mHolder.b_heart.setTag(like);
        mHolder.b_heartNum.setText(String.valueOf(count));

        // 댓글 개수 확인
        count = 0;
        if (board.getComment() != null)
            count = board.getComment().size();
        mHolder.b_commentNum.setText(String.valueOf(count));

        // 내 글이면 수정/삭제 버튼 Visible
        if (!board.getId().equals(user.getDisplayName())) {
            mHolder.b_edit.setVisibility(View.INVISIBLE);
            mHolder.b_delete.setVisibility(View.INVISIBLE);
        } else {
            mHolder.b_edit.setVisibility(View.VISIBLE);
            mHolder.b_delete.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return boards.size();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        keys.clear();
        boards.clear();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getDisplayName() == null)
            return;
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            Board board = snapshot.getValue(Board.class);
            if (board == null) // 잘못된 board를 받아오면 무시
                continue;
            // Filter가 true이고 내 게시물이 아니면 무시
            if (myBoardFilter && !board.getId().equals(user.getDisplayName()))
                continue;
            // Filter가 true이고 내가 좋아요하지 않았으면 무시
            if (myHeartFilter &&
                    snapshot.child("heart").child(user.getDisplayName()).getValue(String.class) == null)
                continue;

            keys.add(0, snapshot.getKey());
            boards.add(0, board);
        }
        notifyDataSetChanged();
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView b_id;
        private final TextView b_time;
        private final TextView b_content;
        private final ImageView b_img;
        private final ImageView b_heart;
        private final ImageView b_comment;
        private final TextView b_heartNum;
        private final TextView b_commentNum;
        private final ImageView b_edit;
        private final ImageView b_delete;

        private ViewHolder(View view) {
            super(view);
            b_id = view.findViewById(R.id.board_id);
            b_time = view.findViewById(R.id.board_time);
            b_content = view.findViewById(R.id.board_content);
            b_img = view.findViewById(R.id.board_image);
            b_heart = view.findViewById(R.id.board_heart);
            b_comment = view.findViewById(R.id.board_comment);
            b_heartNum = view.findViewById(R.id.board_heart_num);
            b_commentNum = view.findViewById(R.id.board_comment_num);
            b_edit = view.findViewById(R.id.board_edit);
            b_delete = view.findViewById(R.id.board_delete);

            b_heart.setOnClickListener(this);
            b_heart.setTag(false);
            b_comment.setOnClickListener(this);
            b_edit.setOnClickListener(this);
            b_delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Board board = boards.get(getAdapterPosition());
            int index = boards.indexOf(board);
            String key = keys.get(index);

            if (v == b_heart) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null || user.getDisplayName() == null)
                    return;
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                if ((boolean) b_heart.getTag()) // 좋아요 해제
                    database.getReference("board/" + key + "/heart/" + user.getDisplayName()).removeValue();
                else // 좋아요 설정
                    database.getReference("board/" + key + "/heart/").child(user.getDisplayName()).setValue(user.getDisplayName());
            } else if (v == b_comment) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("key", key);
                context.startActivity(intent);
            } else if (v == b_edit) {
                Intent intent = new Intent(context, BoardWriteActivity.class);
                intent.putExtra("key", key);
                context.startActivity(intent);
            } else if (v == b_delete) {
                FirebaseDatabase.getInstance().getReference("board/" + key).removeValue();
            }
            notifyItemChanged(getAdapterPosition());
        }
    }

}