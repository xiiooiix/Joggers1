package kkt.com.joggers.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import kkt.com.joggers.R;
import kkt.com.joggers.fragment.FitnessTipPage;
import kkt.com.joggers.model.FitnessTipResImgs;

/**
 * 화면역할: FitnessTipFragment에서 선택한 팁을 볼 수 있는 Activity
 * 화면위치: FitnessTipFragment에서 팁을 터치하면 FitnessTipContentActivity로 진입한다
 */
public class FitnessTipContentActivity extends AppCompatActivity {
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_tip_content);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0); // 사용자가 선택한 팁 세트 번호

        // 뷰페이저, 어댑터
        ViewPager vp = findViewById(R.id.vp);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        vp.setAdapter(pagerAdapter);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragmentList = new ArrayList<>();

        MyPagerAdapter(FragmentManager fm) {
            super(fm);

            for (int resId : FitnessTipResImgs.contentImgs[position]) {
                // 프래그먼트 생성
                Fragment fragment = new FitnessTipPage();
                Bundle bundle = new Bundle();
                bundle.putInt("resId", resId);
                fragment.setArguments(bundle);
                fragmentList.add(fragment);
            }
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }
}