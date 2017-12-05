package kkt.com.joggers.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import kkt.com.joggers.R;
import kkt.com.joggers.activity.DailyRecordActivity;
import kkt.com.joggers.activity.RunningActivity;
import kkt.com.joggers.activity.TodayRecordActivity;

public class MainFragment extends Fragment implements View.OnClickListener {
    private CardView running;
    private CardView todayRecord;
    private CardView dailyRecord;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* 레이아웃 설정 */
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        running = view.findViewById(R.id.running);
        todayRecord = view.findViewById(R.id.today_record);
        dailyRecord = view.findViewById(R.id.daily_record);

        running.setOnClickListener(this);
        todayRecord.setOnClickListener(this);
        dailyRecord.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == running) {
            startActivity(new Intent(getContext(), RunningActivity.class));
        } else if (v == todayRecord) {
            startActivity(new Intent(getContext(), TodayRecordActivity.class));
        } else if (v == dailyRecord) {
            startActivity(new Intent(getContext(), DailyRecordActivity.class));
        }
    }

}
