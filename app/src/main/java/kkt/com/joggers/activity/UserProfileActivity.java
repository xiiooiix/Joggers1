package kkt.com.joggers.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import kkt.com.joggers.R;
import kkt.com.joggers.containers.DisableSwapViewPager;
import kkt.com.joggers.service.UserProfileManager;

/* 처음 로그인했을 때 사용자 프로필을 작성하는 Activity */
public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private int position;
    private UserProfileManager manager;

    private DisableSwapViewPager viewPager;
    private TextView skipBtn;
    private TextView nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        manager = new UserProfileManager(this);

        UserProfileViewPagerAdapter adapter = new UserProfileViewPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        skipBtn = findViewById(R.id.skipBtn);
        nextBtn = findViewById(R.id.nextBtn);
        skipBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == skipBtn) {
            startMain();
        } else if (v == nextBtn) {
            if (position < 1) {
                DatePicker datePicker = findViewById(R.id.datePicker);
                String birth = String.format(Locale.KOREAN, "%d년 %d월 %d일", datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                manager.setBirth(birth);

                viewPager.setCurrentItem(++position);
            } else if (position < 2) {
                RadioGroup radioGroup = findViewById(R.id.gender);
                EditText weightEt = findViewById(R.id.weight);
                EditText heightEt = findViewById(R.id.height);

                int radioButtonId = radioGroup.getCheckedRadioButtonId();
                if (radioButtonId == -1) {
                    Toast.makeText(this, "성별을 선택하지 않았습니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                String weight = weightEt.getText().toString();
                String height = heightEt.getText().toString();
                if (weight.equals("") || height.equals("")) {
                    Toast.makeText(this, "신장/체중이 입력되지 않았습니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                String gender = (radioButtonId == R.id.male) ? "남" : "여";
                Float w = Float.valueOf(weight);
                Float h = Float.valueOf(height);
                if (w < 0 || h < 0) {
                    Toast.makeText(this, "신장/체중이 잘못되었습니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                manager.setGender(gender);
                manager.setWeight(w);
                manager.setHeight(h);
                viewPager.setCurrentItem(++position);
            } else {
                startMain();
            }
        }
    }

    private void startMain() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "세션이 만료되었습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        manager.setId(user.getUid());
        manager.save();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /* 뷰페이저 관련 클래스 */
    public static class UserProfileFragment extends Fragment {
        public UserProfileFragment() {
        }

        public static UserProfileFragment newInstance(int position) {
            UserProfileFragment fragment = new UserProfileFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            int position = getArguments().getInt("position");
            int layoutId;
            if (position == 0)
                layoutId = R.layout.fragment_user_profile_1;
            else
                layoutId = R.layout.fragment_user_profile_2;
            return inflater.inflate(layoutId, container, false);
        }
    }

    private class UserProfileViewPagerAdapter extends FragmentPagerAdapter {

        UserProfileViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return UserProfileFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

}
