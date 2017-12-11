package kkt.com.joggers.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import kkt.com.joggers.R;
import kkt.com.joggers.model.Record;

public class FriendProfileActivity extends AppCompatActivity implements ValueEventListener, AdapterView.OnItemSelectedListener {
    private TextView distanceView, stepCountView, timeView;

    private Spinner dateSpinner;
    private Spinner ySpinner;
    private LineChart chart;

    private int year, month;
    private int numOfDay;
    private final List<Record> recordList = new ArrayList<>();
    private final String[] yItems = new String[]{"달린거리", "걸음수", "달린시간"};
    private final String[] yUnits = new String[]{"M", "회", "분"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        distanceView = findViewById(R.id.distance);
        stepCountView = findViewById(R.id.stepCount);
        timeView = findViewById(R.id.runningTime);

        // 친구 데이터
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        String key = String.format(Locale.KOREAN, "record/%s/%d|%d|%d", id, year, month, calendar.get(Calendar.DAY_OF_MONTH));
        FirebaseDatabase.getInstance().getReference(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Record record = dataSnapshot.getValue(Record.class);
                        if (record == null) {
                            Toast.makeText(FriendProfileActivity.this, "친구의 오늘 운동 기록이 없습니다", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        distanceView.setText(String.valueOf(record.getDistance()));
                        stepCountView.setText(String.valueOf(record.getStepCount()));
                        long hour = record.getTime() / 3600000;
                        long min = record.getTime() % 3600000 / 60000;
                        long sec = record.getTime() % 60000 / 1000;
                        timeView.setText(String.format(Locale.KOREAN, "%02d:%02d:%02d", hour, min, sec));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        /* 오늘 날짜를 가져온다 */
        setNumOfDay();

        /* Spinner */
        dateSpinner = findViewById(R.id.dateSpinner);
        ySpinner = findViewById(R.id.ySpinner);
        List<String> dateItems = new ArrayList<>();
        int temp = year * 12 + month + 1;
        int nyear = (temp - 1) / 12;
        int nmonth = (temp - 1) % 12 + 1;
        for (int i = 0; i < 6; i++) {
            nmonth = (nmonth + 10) % 12 + 1;
            if (nmonth == 12)
                nyear--;
            String text = String.format(Locale.KOREAN, "%d년 %02d월", nyear, nmonth);
            dateItems.add(text);
        }
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dateItems);
        ArrayAdapter<String> yAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yItems);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(dateAdapter);
        ySpinner.setAdapter(yAdapter);
        dateSpinner.setOnItemSelectedListener(this);
        ySpinner.setOnItemSelectedListener(this);

        /* 차트 */
        chart = findViewById(R.id.lineChart);
        chart.setDragEnabled(true);

        /* 오늘 운동량 데이터 가져오기 */
        String recordsKey = String.format(Locale.KOREAN, "record/%s", id);
        FirebaseDatabase.getInstance().getReference(recordsKey)
                .orderByKey()
                .addValueEventListener(this);
    }


    private void setNumOfDay() {
        switch (month) {
            case 1:case 3:case 5:case 7:case 8:case 10:case 12:
                numOfDay = 31;
                break;
            case 4:case 6:case 9:case 11:
                numOfDay = 30;
                break;
            case 2: // 윤년계산
                numOfDay = ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) ? 29 : 28;
                break;
        }
    }

    /* Spinner Listener */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == dateSpinner) {
            String date = (String) dateSpinner.getAdapter().getItem(position);
            year = Integer.parseInt(date.substring(0, 4));
            month = Integer.parseInt(date.substring(6, 8));
            setNumOfDay();
        }
        updateChart();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /* FirebaseDatabase Listener */
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        recordList.clear();
        for (DataSnapshot snapshot : dataSnapshot.getChildren())
            recordList.add(snapshot.getValue(Record.class));
        updateChart();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }

    /* Draw Chart */
    private void updateChart() {
        int position = ySpinner.getSelectedItemPosition();

        // DataSet
        List<String> dayList = new ArrayList<>();
        List<Entry> entryList = new ArrayList<>();
        int j;
        for (j = 0; j < recordList.size(); j++) {
            Record record = recordList.get(j);
            if (record.getYear() == year && record.getMonth() == month)
                break;
        }
        for (int i = 0; i < numOfDay; i++) {
            float yVal = 0;
            if (j < recordList.size()) {
                Record record = recordList.get(j);
                if (record.getYear() == year && record.getMonth() == month && record.getDay() == i + 1) {
                    if (position == 0)
                        yVal = record.getDistance();
                    else if (position == 1)
                        yVal = record.getStepCount();
                    else
                        yVal = record.getTime() / 1000 / 60;
                    j++;
                }
            }
            dayList.add(String.valueOf(i + 1));
            entryList.add(new Entry(i + 1, yVal));
        }

        // Legend
        ILineDataSet dataSet = new LineDataSet(entryList, yItems[position]);

        // Set data sets
        LineData data = new LineData(dataSet);
        data.setValueFormatter(new IntegerFormatter());
        chart.setData(data);
        chart.getXAxis().setValueFormatter(new XaxisValueFormatter(dayList));
        Description description = new Description();
        description.setText(yUnits[position]);
        description.setTextSize(14f);
        chart.setDescription(description);
        chart.invalidate();
    }

    private class XaxisValueFormatter implements IAxisValueFormatter {
        private final List<String> dayList;

        private XaxisValueFormatter(List<String> dayList) {
            this.dayList = dayList;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return dayList.get((int) value - 1);
        }
    }

    private class IntegerFormatter implements IValueFormatter {

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return String.valueOf(Math.round(value));
        }
    }
}
