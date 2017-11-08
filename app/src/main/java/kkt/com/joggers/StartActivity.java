package kkt.com.joggers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class StartActivity extends AppCompatActivity {
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        new StartTask().execute();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // id를 Intent에 담아서 Main Activity 시작
        id = data.getStringExtra("id");
        startMainActivity(id);
    }

    void startMainActivity(String id) {
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }


    class StartTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            // 자동 로그인 여부
            SharedPreferences sp = getSharedPreferences("login", MODE_PRIVATE);
            String id = sp.getString("id", null);
            return id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // TODO 이미지 ImageView에 표시
        }

        @Override
        protected void onPostExecute(String id) {
            super.onPostExecute(id);
            if (id != null) {
                startMainActivity(id);
            } else {
                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                StartActivity.this.startActivityForResult(intent, 0);
            }
        }

    }


}
