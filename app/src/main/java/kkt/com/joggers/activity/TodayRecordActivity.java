package kkt.com.joggers.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;

import kkt.com.joggers.R;
import kkt.com.joggers.model.Record;

public class TodayRecordActivity extends AppCompatActivity implements ValueEventListener {

    private TextView distanceView;
    private TextView stepCountView;
    private TextView timeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_record);

        distanceView = findViewById(R.id.distance);
        stepCountView = findViewById(R.id.stepCount);
        timeView = findViewById(R.id.runningTime);

        // 오늘 운동량 데이터 생성 OR 가져오기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Calendar calendar = Calendar.getInstance();
            String key = String.format(Locale.KOREAN, "record/%s/%d|%d|%d", user.getDisplayName(), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            FirebaseDatabase.getInstance().getReference(key)
                    .addListenerForSingleValueEvent(this);
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Record record = dataSnapshot.getValue(Record.class);
        if (record == null) {
            Toast.makeText(this, "오늘 운동 기록이 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = String.valueOf(record.getDistance()) + " M";
        distanceView.setText(text);
        text = String.valueOf(record.getStepCount()) + " 회";
        stepCountView.setText(text);
        timeView.setText(String.valueOf(record.getTime()));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }
}
