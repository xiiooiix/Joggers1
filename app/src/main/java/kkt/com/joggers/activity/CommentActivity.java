package kkt.com.joggers.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Date;

import kkt.com.joggers.R;
import kkt.com.joggers.adapter.CommentAdapter;
import kkt.com.joggers.adapter.MarginItemDecoration;
import kkt.com.joggers.controller.OnScrollEndListener;
import kkt.com.joggers.controller.OnSuccessGetImage;
import kkt.com.joggers.model.Board;
import kkt.com.joggers.model.Comment;

public class CommentActivity extends AppCompatActivity implements View.OnClickListener, ValueEventListener {

    private ImageView boardImage;
    private TextView boardContent;
    private EditText contentEditText;
    private Button writeBtn, cancelBtn;

    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        boardImage = findViewById(R.id.board_image);
        boardContent = findViewById(R.id.board_content);
        RecyclerView rcView = findViewById(R.id.rcView);
        contentEditText = findViewById(R.id.c_content);
        writeBtn = findViewById(R.id.c_write);
        cancelBtn = findViewById(R.id.c_cancel);

        writeBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        /* key에 해당하는 게시글에 댓글을 작성하거나 볼 수 있다 */
        key = getIntent().getStringExtra("key");
        if (key == null)
            finish();
        FirebaseDatabase.getInstance().getReference("board/" + key)
                .addListenerForSingleValueEvent(this);

        /* 댓글 RecyclerView */
        rcView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rcView.addItemDecoration(new MarginItemDecoration());
        CommentAdapter adapter = new CommentAdapter(this, key);
        rcView.setAdapter(adapter);
        rcView.addOnScrollListener(new OnScrollEndListener());
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Board board = dataSnapshot.getValue(Board.class);
        if (board == null)
            return;
        if (board.getImageUrl() != null)
            FirebaseStorage.getInstance().getReferenceFromUrl(board.getImageUrl())
                    .getBytes(Long.MAX_VALUE)
                    .addOnSuccessListener(new OnSuccessGetImage(boardImage));
        boardContent.setText(board.getContent());
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }

    @Override
    public void onClick(View v) {
        if (v == writeBtn) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null)
                return;
            String time = SimpleDateFormat.getDateTimeInstance().format(new Date());
            Comment comment = new Comment(user.getDisplayName(), time, contentEditText.getText().toString());
            FirebaseDatabase.getInstance().getReference("board/" + key + "/comment").push().setValue(comment);
            contentEditText.setText("");
        } else if (v == cancelBtn) {
            finish();
        }
    }

}
