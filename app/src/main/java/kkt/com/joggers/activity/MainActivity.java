package kkt.com.joggers.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;
import java.util.Vector;

import kkt.com.joggers.R;
import kkt.com.joggers.controller.SettingManager;
import kkt.com.joggers.fragment.BoardFragment;
import kkt.com.joggers.fragment.FitnessTipFragment;
import kkt.com.joggers.fragment.MainFragment;
import kkt.com.joggers.fragment.SettingFragment;
import kkt.com.joggers.model.Record;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, ValueEventListener {

    private static final String TAG = "joggers.MainActivity";
    private boolean exitFlag = false;
    private TextView totalDaysTv;
    private TextView totalKmTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /* 텍스트 뷰에 id 넣기*/
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            View headerView = navigationView.getHeaderView(0);
            ((TextView) headerView.findViewById(R.id.id)).setText(user.getDisplayName());
            String key = String.format(Locale.KOREAN, "record/%s", user.getDisplayName());
            FirebaseDatabase.getInstance().getReference(key)
                    .addValueEventListener(this);
            totalDaysTv = headerView.findViewById(R.id.total_days);
            totalKmTv = headerView.findViewById(R.id.total_km);
            headerView.findViewById(R.id.logout).setOnClickListener(this);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, new MainFragment()).commit();

        if (new SettingManager(this).isNotification())
            FirebaseMessaging.getInstance().subscribeToTopic("advice");
        else
            FirebaseMessaging.getInstance().unsubscribeFromTopic("advice");
    }

    @Override
    public void onBackPressed() {
        // Drawer이 열려있으면 닫고 return
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        // MainFragment가 아니거나 exitFlag가 true이면 Back 버튼을 동작시킨다
        if (getSupportFragmentManager().getBackStackEntryCount() != 0 || exitFlag) {
            super.onBackPressed();
            return;
        }

        // MainFragment이고 exitFlag가 false이면 앱 종료 확인을 위한 쓰레드를 작동시킨다
        Toast.makeText(this, "종료하려면 한번 더 눌러주세요", Toast.LENGTH_SHORT).show();
        new Thread() {
            @Override
            public void run() {
                exitFlag = true;
                try {
                    sleep(2000); //2초 안에 Back 누르면 앱이 종료된다
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                exitFlag = false;
            }
        }.start();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        getSupportFragmentManager().popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (id == R.id.nav_home) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_main, new MainFragment())
                    .commit();
        } else if (id == R.id.nav_board) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_main, new BoardFragment(), TAG)
                    .addToBackStack(TAG)
                    .commit();
        } else if (id == R.id.nav_friend) {
        } else if (id == R.id.nav_tip) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_main, new FitnessTipFragment(), TAG)
                    .addToBackStack(TAG)
                    .commit();
        } else if (id == R.id.nav_setting) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_main, new SettingFragment(), TAG)
                    .addToBackStack(TAG)
                    .commit();
        } else if (id == R.id.nav_exit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("앱을 종료합니다")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setNegativeButton("아니오", null).show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        // 현재 사용자의 매일운동데이터를 받아온다
        Vector<Record> records = new Vector<>();
        for (DataSnapshot snapshot : dataSnapshot.getChildren())
            records.add(snapshot.getValue(Record.class));

        // 달린 거리(m) 합을 구한다
        int sum = 0;
        for (Record record : records)
            sum += record.getDistance();

        // 운동 n 일차, 총 달린 거리(km)를 표시한다
        totalDaysTv.setText(String.format(Locale.KOREAN, "운동 %d 일차", records.size()));
        totalKmTv.setText(String.format(Locale.KOREAN, "총 %f KM", sum / 1000.0));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
