package kkt.com.joggers.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Year;
import java.util.Calendar;
import java.util.Locale;

import kkt.com.joggers.R;
import kkt.com.joggers.controller.UserProfileManager;
import kkt.com.joggers.model.Record;

public class TodayRecordActivity extends AppCompatActivity implements ValueEventListener, View.OnClickListener {

    private TextView distanceView, stepCountView, timeView;
    private TextView ageView, heightView, weightView;
    private TextView bmiView, calorieView;
    private Button editProfileBtn;

    private UserProfileManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_record);

        distanceView = findViewById(R.id.distance);
        stepCountView = findViewById(R.id.stepCount);
        timeView = findViewById(R.id.runningTime);
        ageView = findViewById(R.id.age);
        heightView = findViewById(R.id.height);
        weightView = findViewById(R.id.weight);
        bmiView = findViewById(R.id.bmi);
        calorieView = findViewById(R.id.calorie);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        editProfileBtn.setOnClickListener(this);

        // 친구 데이터를 받아오는 경우...
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");

        if (id == null) {
            // 오늘 운동량 데이터 생성 OR 가져오기
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null)
                id = user.getDisplayName();
        }

        Calendar calendar = Calendar.getInstance();
        String key = String.format(Locale.KOREAN, "record/%s/%d|%d|%d", id, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        FirebaseDatabase.getInstance().getReference(key)
                .addListenerForSingleValueEvent(this);

        manager = new UserProfileManager(this);
        int birthYear = Integer.parseInt(manager.getBirth().split("|")[0]);
        int age = calendar.get(Calendar.YEAR) - birthYear + 1;
        ageView.setText(String.valueOf(age));
        heightView.setText(String.valueOf(manager.getHeight()));
        weightView.setText(String.valueOf(manager.getWeight()));
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Record record = dataSnapshot.getValue(Record.class);
        if (record == null) {
            Toast.makeText(this, "오늘 운동 기록이 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        distanceView.setText(String.valueOf(record.getDistance()));
        stepCountView.setText(String.valueOf(record.getStepCount()));
        long hour = record.getTime() / 3600000;
        long min = record.getTime() % 3600000 / 60000;
        long sec = record.getTime() % 60000 / 1000;
        timeView.setText(String.format(Locale.KOREAN, "%02d:%02d:%02d", hour, min, sec));

        bmiView.setText(String.format(Locale.KOREAN, "%f", calcBMI(record.getDistance(), record.getTime())));
        calorieView.setText(String.format(Locale.KOREAN, "%f", calcCalorie(record.getDistance(), record.getTime())));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }

    private float calcBMI(float distance, long time) {
        float weight = (manager.getWeight() != 0) ? manager.getWeight() : 80; // 체중
        float height = (manager.getHeight() != 0) ? manager.getHeight() : 175; // 키

        float bmi = 0;
        // TODO bmi 지수 구하기

        return bmi;
    }

    private float calcCalorie(float distance, long time) {
        float weight = (manager.getWeight() != 0) ? manager.getWeight() : 80; // 체중
        float height = (manager.getHeight() != 0) ? manager.getHeight() : 175; // 키

        float calorie = 0;
        // TODO 칼로리 구하기

        return calorie;
    }

    @Override
    public void onClick(View v) {
        if (v == editProfileBtn) {
            // 새로운 프로필을 작성하는 Activity 실행
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
        }
    }
}
