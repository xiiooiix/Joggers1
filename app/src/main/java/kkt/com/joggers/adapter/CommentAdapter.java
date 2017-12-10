package kkt.com.joggers.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import kkt.com.joggers.R;
import kkt.com.joggers.model.Comment;

public class CommentAdapter extends DataLoadLimitAdapter {
    private final String boardKey;
    private final List<String> keys = new ArrayList<>();
    private final List<Comment> comments = new ArrayList<>();

    public CommentAdapter(Context context, String boardKey) {
        super(context);
        this.boardKey = boardKey;
        update();
    }

    @Override
    protected void update() {
        FirebaseDatabase.getInstance().getReference("board/" + boardKey + "/comment")
                .removeEventListener(this);
        FirebaseDatabase.getInstance().getReference("board/" + boardKey + "/comment")
                .orderByChild("time")
                .limitToLast(loadLimit)
                .addValueEventListener(this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder mHolder = (ViewHolder) holder;
        Comment comment = comments.get(position);

        mHolder.id.setText(comment.getId());
        mHolder.time.setText(comment.getTime());
        ((TextView) mHolder.content).setText(comment.getContent());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;
        if (comment.getId().equals(user.getDisplayName())) {
            mHolder.editBtn.setVisibility(View.VISIBLE);
            mHolder.deleteBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        keys.clear();
        comments.clear();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getDisplayName() == null)
            return;
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            Comment comment = snapshot.getValue(Comment.class);
            if (comment == null) // 잘못된 comment를 받아오면 무시
                continue;

            keys.add(0, snapshot.getKey());
            comments.add(0, comment);
        }
        notifyDataSetChanged();
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView id;
        private final TextView time;
        private View content;
        private Button modifyBtn;
        private final ImageView editBtn;
        private final ImageView deleteBtn;

        private ViewHolder(View view) {
            super(view);

            id = view.findViewById(R.id.id);
            time = view.findViewById(R.id.time);
            content = view.findViewById(R.id.content);
            editBtn = view.findViewById(R.id.edit);
            deleteBtn = view.findViewById(R.id.delete);

            editBtn.setOnClickListener(this);
            deleteBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Comment comment = comments.get(getAdapterPosition());
            int index = comments.indexOf(comment);
            String key = keys.get(index);

            if (v == editBtn) {
                LinearLayout layout = itemView.findViewById(R.id.itemLayout);
                layout.removeView(content);
                content = new EditText(context);
                LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                contentParams.topMargin = 30;
                content.setLayoutParams(contentParams);
                layout.addView(content);

                modifyBtn = new Button(context);
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                btnParams.setMargins(5, 5, 5, 5);
                btnParams.gravity = Gravity.END;
                modifyBtn.setText("수정");
                modifyBtn.setLayoutParams(btnParams);
                layout.addView(modifyBtn);
            } else if (v == deleteBtn) {
                FirebaseDatabase.getInstance().getReference("board/" + key).removeValue();
            } else if (v == modifyBtn) {
                // 댓글 수정
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null)
                    return;
                comment.setCotent(((EditText) content).getText().toString());
                FirebaseDatabase.getInstance().getReference("board" + boardKey + "/comment/" + key).setValue(comment);

                // 아이템뷰 원상복귀
                LinearLayout layout = itemView.findViewById(R.id.itemLayout);
                layout.removeView(content);
                layout.removeView(modifyBtn);
                content = new TextView(context);
                LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                contentParams.topMargin = 30;
                content.setLayoutParams(contentParams);
                layout.addView(content);
            }
        }
    }
}
