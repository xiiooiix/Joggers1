package kkt.com.joggers.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kkt.com.joggers.R;
import kkt.com.joggers.model.Record;

public class RunningActivity extends AppCompatActivity implements OnCompleteListener<LocationSettingsResponse>, SensorEventListener, ValueEventListener {
    private static final int REQ_PERM = 0;
    private static final int REQ_SETTING = 1;

    private TextView distanceView;
    private TextView stepCountView;
    private TextView timeView;
    private TextView actionBtn;

    private FusedLocationProviderClient locClient;
    private LocationCallback locCallback;
    private Location lastLoc;
    private LocationRequest request;
    private LocationSettingsRequest.Builder settingReqBuilder;
    private boolean locPermState = false;
    private boolean locSettingState = false;

    private SensorManager sensorManager;
    private Sensor accSensor;

    private float totalDistance;
    private long lastLocTimeMillis;
    private int stepCount;
    private int lastX, lastY, lastZ;
    private long lastSensorTimeMillis;
    private long accMillis;
    private boolean isRunning = false;
    private Calendar calendar;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        distanceView = findViewById(R.id.distance);
        stepCountView = findViewById(R.id.stepCount);
        timeView = findViewById(R.id.runningTime);
        actionBtn = findViewById(R.id.actionBtn);
        // TODO 이미지 버튼으로 바꿀 것...
        actionBtn.setText("운동 시작");

        // 위치 측정 : Google Play Service Location
        locClient = LocationServices.getFusedLocationProviderClient(RunningActivity.this);
        locCallback = new RunningLocationCallback();
        request = new LocationRequest();
        request.setInterval(8000);
        request.setFastestInterval(3000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        settingReqBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(request);

        // 걸음수 측정
        sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        if (sensorManager != null)
            accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLocationPermission(); // 위치 권한 확인
        checkLocationSettings(); // 위치 설정 확인
    }

    @Override
    protected void onDestroy() {
        if (isRunning)
            onStopRunning();

        // 오늘 운동량 데이터 생성 OR 가져오기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            calendar = Calendar.getInstance();
            key = String.format(Locale.KOREAN, "record/%s/%d|%d|%d", user.getDisplayName(), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            FirebaseDatabase.getInstance().getReference(key)
                    .addListenerForSingleValueEvent(this);
        }

        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) // 알림 메뉴를 닫았을 때 '위치설정'을 확인한다
            checkLocationSettings();
    }

    /* 위치 '권한' 확인 */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locPermState = true;
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERM);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_PERM)
            locPermState = grantResults[0] == 0;
    }

    /* 위치 '설정' 확인 */
    private void checkLocationSettings() {
        LocationServices.getSettingsClient(this)
                .checkLocationSettings(settingReqBuilder.build())
                .addOnCompleteListener(this, this);
    }

    @Override
    public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
        try { // 위치 설정되어 있음
            task.getResult(ApiException.class);
            locSettingState = true;
        } catch (ApiException e) { // 위치 설정 안되어 있음
            if (e.getStatusCode() != LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                locSettingState = false;
                return;
            }
            try {
                // 위치 설정창 활성화
                ((ResolvableApiException) e).startResolutionForResult(RunningActivity.this, REQ_SETTING);
            } catch (IntentSender.SendIntentException e1) {
                e1.printStackTrace();
                finish();
            }
        } finally { // 운동시작 버튼 활성화
            actionBtn.setEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_SETTING)
            locSettingState = resultCode == RESULT_OK;
    }

    /* 위치, 달린 거리 측정하는 callback class */
    private class RunningLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (lastLoc == null) {
                lastLoc = locationResult.getLastLocation();
                return;
            }
            long currentTimeMillis = System.currentTimeMillis();
            if (lastLocTimeMillis > 0) {
                float[] distance = new float[1];
                Location curLoc = locationResult.getLastLocation();
                Location.distanceBetween(lastLoc.getLatitude(), lastLoc.getLongitude(), curLoc.getLatitude(), curLoc.getLongitude(), distance);

                float speed = distance[0] * 1000 / (currentTimeMillis - lastLocTimeMillis); // Meter/sec
                if (speed > 1.3 && speed < 17.8) {
                    Log.i("TAG", "거리: " + distance[0] + ", 속도: " + speed);
                    totalDistance += distance[0];
                    String text = String.valueOf(totalDistance) + " M";
                    distanceView.setText(text);
                    lastLoc = curLoc;
                }
            }
            lastLocTimeMillis = currentTimeMillis;
        }
    }

    /* 걸음수 측정 */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int x = (int) event.values[0];
            int y = (int) event.values[1];
            int z = (int) event.values[2];
            long currentTimeMillis = System.currentTimeMillis();
            if (lastSensorTimeMillis > 0) {
                long dTimeMillis = currentTimeMillis - lastSensorTimeMillis;
                float speed = Math.abs(x + y + z + lastX + lastY + lastZ) * dTimeMillis / 2; // Meter/msec
                if (speed > 2900) {
                    String text = String.valueOf(++stepCount) + " 회";
                    stepCountView.setText(text);
                }
            }
            lastX = x;
            lastY = y;
            lastZ = z;
            lastSensorTimeMillis = currentTimeMillis;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /* 달린 시간 측정 */
    private class TimeTask extends AsyncTask<Void, Long, Void> {
        private long startMillis;

        private TimeTask() {
            startMillis = System.currentTimeMillis();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (isRunning) {
                long currentMillis = System.currentTimeMillis();
                accMillis += currentMillis - startMillis;
                startMillis = currentMillis;
                long hour = accMillis / 3600000;
                long min = accMillis % 3600000 / 60000;
                long sec = accMillis % 60000 / 1000;
                publishProgress(hour, min, sec);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            timeView.setText(String.format(Locale.KOREAN, "%02d:%02d:%02d", values[0], values[1], values[2]));
        }
    }

    /* RTDB Data Access */
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Record record = dataSnapshot.getValue(Record.class);
        if (record == null)
            record = new Record(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));

        record.setDistance(record.getDistance() + totalDistance);
        record.setStepCount(record.getStepCount() + stepCount);
        record.setTime(record.getTime() + accMillis);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, record.toMap());
        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }

    /* 운동시작 Button */
    public void onClickActionButton(View view) {
        if (!isRunning)
            onStartRunning();
        else
            onStopRunning();
    }

    @SuppressLint("MissingPermission")
    private void onStartRunning() {
        if (locPermState && locSettingState)
            locClient.requestLocationUpdates(request, locCallback, Looper.myLooper());
        if (accSensor != null)
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        new TimeTask().execute();
        isRunning = true;
        // TODO 이미지 버튼으로 바꿀 것...
        actionBtn.setText("운동 정지");
    }

    private void onStopRunning() {
        locClient.removeLocationUpdates(locCallback);
        lastLoc = null; // 운동정지한 동안에는 거리측정하지 않으므로 마지막 위치 초기화
        if (accSensor != null)
            sensorManager.unregisterListener(this, accSensor);
        // TODO 이미지 버튼으로 바꿀 것...
        isRunning = false;
        actionBtn.setText("운동 시작");
    }

}
