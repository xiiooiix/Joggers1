package kkt.com.joggers.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kkt.com.joggers.R;
import kkt.com.joggers.adapter.CommentAdapter;
import kkt.com.joggers.model.Comment;

public class CommentActivity extends AppCompatActivity {

    private TextView textView;
    private EditText editText;
    private Button button;
    private ListView listView;
    private int num;
    private CommentAdapter adapter;
    private ArrayList<Comment> comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        textView = findViewById(R.id.c_textview);
        editText = findViewById(R.id.c_edittext);
        button = findViewById(R.id.c_button);
        listView = findViewById(R.id.c_listview);


        comments = new ArrayList<Comment>();
        adapter = new CommentAdapter(CommentActivity.this, comments);

        listView.setAdapter(adapter);

        num = getIntent().getIntExtra("num",-1);
        Log.d("ASD", "num ===== " + num);

        Query lastQuery = FirebaseDatabase.getInstance().getReference().child("board");
        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    int count = child.child("num").getValue(Integer.class);
                    if(num == count){
                        textView.setText(child.child("content").getValue().toString());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        FirebaseDatabase.getInstance().getReference()
                .child("comment")
                .child(Integer.toString(num))
                .orderByChild("time")
                .addChildEventListener(new ChildEventAdapter());

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser =  FirebaseAuth.getInstance().getCurrentUser();
                String id = currentUser.getDisplayName();
                String time = SimpleDateFormat.getDateTimeInstance().format(new Date());
                Comment comment = new Comment(1,id,editText.getText().toString(), time);
                FirebaseDatabase.getInstance().getReference().child("comment").child(Integer.toString(num)).push().setValue(comment);
                editText.setText("");
            }
        });
    }

    class ChildEventAdapter implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Comment c= dataSnapshot.getValue(Comment.class);
            if(c.getNum()!=0) {
                adapter.addItem(c);
                Log.d("ASD", "숫자가 몇이길래?  " + c.getNum() + c.getContent());
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.i("ASD", "onChildRemoved ㄱㄱ");
            adapter.removeItem(dataSnapshot.getValue(Comment.class));
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
