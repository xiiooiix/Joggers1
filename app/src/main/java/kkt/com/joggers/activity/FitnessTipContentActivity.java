package kkt.com.joggers.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import kkt.com.joggers.R;
import kkt.com.joggers.fragment.FitnessTipContentFragment;

public class FitnessTipContentActivity extends AppCompatActivity {
    private ViewPager vp;
    private ArrayList<FitnessTipContentFragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_tip_content);

        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0); // 사용자가 선택한 팁 세트 번호

        // 뷰페이저, 어댑터
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void add(FitnessTipContentFragment fragment) {
            fragments.add(fragment);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
