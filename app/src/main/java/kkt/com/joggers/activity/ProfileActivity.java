package kkt.com.joggers.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import kkt.com.joggers.R;
import kkt.com.joggers.model.Friend;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private String id;

    private Button btn;
    private Button showDataBtn;
    private TextView userId;
    private boolean flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        Bitmap img = bundle.getParcelable("img");
        flag = bundle.getBoolean("flag", false);
        Log.i("ASDF" , "DDKKKK " +id);
        Log.i("ASDF" , "DDKKKK " +img.toString());
        userId = findViewById(R.id.userid_text_view);
        ImageView userImg = findViewById(R.id.user_image_view);
        btn = findViewById(R.id.add_del_btn);
        showDataBtn = findViewById(R.id.show_data_btn);
        btn.setOnClickListener(this);
        showDataBtn.setOnClickListener(this);
        userId.setText(id);
        userImg.setImageBitmap(img);
        String text = (flag) ? "삭제하기" : "추가하기";
        btn.setText(text);
    }

    @Override
    public void onClick(View v) {
        if (v == btn) {
            final String user = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

            if(flag){   //삭제하기
                Query query = FirebaseDatabase.getInstance().getReference().child("friend").child(user);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null) {
                            Log.d("ASDF", "친구가 없음");
                        } else {
                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                Friend friend = child.getValue(Friend.class);
                                Log.d("ASDF", "id: " + friend.getId());
                                if (friend.getId().equals(id)) {
                                    FirebaseDatabase.getInstance().getReference().child("friend").child(user).child(child.getKey()).removeValue();
                                    return;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                Toast.makeText(this, "친구 목록에서 삭제하였습니다.", Toast.LENGTH_SHORT).show();
            }
            else{   //추가하기
                Friend friend =new Friend(id);
                FirebaseDatabase.getInstance().getReference("friend").child(user).push().setValue(friend);
                Toast.makeText(this, "친구 목록에서 추가하였습니다.", Toast.LENGTH_SHORT).show();
            }

        } else if (v == showDataBtn) {
            Intent intent = new Intent(this, FriendProfileActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);
        }
    }

}
