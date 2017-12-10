package kkt.com.joggers.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import kkt.com.joggers.R;
import kkt.com.joggers.model.FitnessTipResImgs;

/**
 * 화면역할: FitnessTipFragment에서 선택한 팁을 볼 수 있는 Activity
 * 화면위치: FitnessTipFragment에서 팁을 터치하면 FitnessTipContentActivity로 진입한다
 */
public class FitnessTipContentActivity extends AppCompatActivity {
    private final ArrayList<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_tip_content);

        // 뷰페이저, 어댑터
        ViewPager vp = findViewById(R.id.vp);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        vp.setAdapter(pagerAdapter);

        // 프래그먼트 생성
        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0); // 사용자가 선택한 팁 세트 번호
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        for (int resId : FitnessTipResImgs.contentImgs[position]) {
            Fragment fragment = new Fragment();
            LinearLayout layout = new LinearLayout(this);
            layout.setLayoutParams(containerParams);
            layout.setBackground(getResources().getDrawable(R.drawable.default_background, null));
            layout.setOrientation(LinearLayout.VERTICAL);

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(imageViewParams);
            imageView.setImageDrawable(getResources().getDrawable(resId, null));
            imageView.setAdjustViewBounds(true);

            layout.addView(imageView);
            fragment.getLayoutInflater().inflate(resId, layout);
            fragments.add(fragment);
        }
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void add(Fragment fragment) {
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
