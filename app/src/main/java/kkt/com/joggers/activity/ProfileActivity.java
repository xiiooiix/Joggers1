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

import kkt.com.joggers.R;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private String id;

    private Button btn;
    private Button showDataBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        Bitmap img = bundle.getParcelable("img");
        boolean flag = bundle.getBoolean("flag", false);
        Log.i("ASDF" , "DDKKKK " +id);
        Log.i("ASDF" , "DDKKKK " +img.toString());
        TextView userId = findViewById(R.id.userid_text_view);
        ImageView userImg = findViewById(R.id.user_image_view);
        btn = findViewById(R.id.add_del_btn);
        showDataBtn = findViewById(R.id.show_data_btn);

        userId.setText(id);
        userImg.setImageBitmap(img);
        String text = (flag) ? "추가하기" : "삭제하기";
        btn.setText(text);

    }

    @Override
    public void onClick(View v) {
        if (v == btn) {
            // if (flag)
            // TODO 추가or삭제
        } else if (v == showDataBtn) {
            Intent intent = new Intent(this, TodayRecordActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);
        }
    }
}
