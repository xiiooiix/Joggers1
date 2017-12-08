package kkt.com.joggers.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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
import kkt.com.joggers.controller.SettingManager;
import kkt.com.joggers.model.Record;

public class RunningActivity extends AppCompatActivity implements OnCompleteListener<LocationSettingsResponse>, OnMapReadyCallback, SensorEventListener, ValueEventListener, GoogleMap.OnMapClickListener {
    private static final int REQ_PERM = 0;
    private static final int REQ_SETTING = 1;

    private SupportMapFragment mapFragment;
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
    private GoogleMap googleMap;

    private SensorManager sensorManager;
    private Sensor accSensor;

    private float totalDistance;
    private long lastLocTimeMillis;
    private int stepCount;
    private float lastx, lasty, lastz;
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

        // 위치 측정 : Google Play Service Location
        locClient = LocationServices.getFusedLocationProviderClient(this);
        locCallback = new RunningLocationCallback();
        request = new LocationRequest();
        request.setInterval(8000);
        request.setFastestInterval(3000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        settingReqBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(request);

        // 지도 표시
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 걸음수 측정
        sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        if (sensorManager != null)
            accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 전화 수신 Listener 등록.... 전화수신 시 운동정지한다
        if (new SettingManager(this).isStopOnCalling()) { // 설정 체크
            @SuppressLint("ServiceCast")
            TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (telManager != null)
                telManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
        }
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
            actionBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_SETTING)
            locSettingState = resultCode == RESULT_OK;
    }

    /* 지도 사용 */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        requestLastLocation();
    }

    @SuppressLint("MissingPermission")
    private void requestLastLocation() {
        locClient.requestLocationUpdates(request, locCallback, Looper.myLooper());
    }

    @Override
    public void onMapClick(LatLng latLng) {
    }

    /* 위치, 달린 거리 측정하는 callback class */
    private class RunningLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            // 구글 맵에 현재 위치 그리기
            Location curLoc = locationResult.getLastLocation();
            LatLng curLatLng = new LatLng(curLoc.getLatitude(), curLoc.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 17));
            googleMap.addMarker(new MarkerOptions().position(curLatLng));

            if (lastLoc == null) { // 이전 위치가 없으면
                lastLoc = curLoc;
                return;
            }

            // 구글 맵에 이전 위치부터 현재 위치까지 PolyLine 그리기
            if (googleMap != null) {
                LatLng lastLatLng = new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude());
                drawMapPath(lastLatLng, curLatLng);
            }

            // 거리계산
            long currentTimeMillis = System.currentTimeMillis();
            if (lastLocTimeMillis > 0) {
                double distance = distanceBetween(lastLoc.getLatitude(), lastLoc.getLongitude(), curLoc.getLatitude(), curLoc.getLongitude());
                double speed = distance * 1000 / (currentTimeMillis - lastLocTimeMillis); // Meter/sec
                //Log.i("TAG", "거리: " + distance + ", 속도: " + speed);
                if (speed > 1.3 && speed < 17.8) {
                    totalDistance += distance;
                    String text = String.valueOf(totalDistance) + " M";
                    distanceView.setText(text);
                    lastLoc = curLoc;
                }
            }
            lastLocTimeMillis = currentTimeMillis;
        }

        private void drawMapPath(LatLng start, LatLng end) {
            PolylineOptions rectOptions = new PolylineOptions()
                    .add(start).add(end).width(12).color(Color.BLUE);
            googleMap.addPolyline(rectOptions);
        }

        private double distanceBetween(double P1_latitude, double P1_longitude, double P2_latitude, double P2_longitude) {
            if ((P1_latitude == P2_latitude) && (P1_longitude == P2_longitude))
                return 0;

            double e10 = P1_latitude * Math.PI / 180;
            double e11 = P1_longitude * Math.PI / 180;
            double e12 = P2_latitude * Math.PI / 180;
            double e13 = P2_longitude * Math.PI / 180;

            /* 타원체 GRS80 */
            double c16 = 6356752.314140910;
            double c15 = 6378137.000000000;
            double c17 = 0.0033528107;
            double c18 = e13 - e11;
            double c20 = (1 - c17) * Math.tan(e10);
            double c21 = Math.atan(c20);
            double c22 = Math.sin(c21);
            double c23 = Math.cos(c21);
            double c24 = (1 - c17) * Math.tan(e12);
            double c25 = Math.atan(c24);
            double c26 = Math.sin(c25);
            double c27 = Math.cos(c25);
            double c31 = (c27 * Math.sin(c18) * c27 * Math.sin(c18)) + (c23 * c26 - c22 * c27 * Math.cos(c18)) * (c23 * c26 - c22 * c27 * Math.cos(c18));
            double c33 = (c22 * c26) + (c23 * c27 * Math.cos(c18));
            double c35 = Math.sqrt(c31) / c33;
            double c38 = (c31 != 0) ? c23 * c27 * Math.sin(c18) / Math.sqrt(c31) : 0;
            double c40 = ((Math.cos(Math.asin(c38)) * Math.cos(Math.asin(c38))) != 0) ? c33 - 2 * c22 * c26 / (Math.cos(Math.asin(c38)) * Math.cos(Math.asin(c38))) : 0;
            double c41 = Math.cos(Math.asin(c38)) * Math.cos(Math.asin(c38)) * (c15 * c15 - c16 * c16) / (c16 * c16);
            double c43 = 1 + c41 / 16384 * (4096 + c41 * (-768 + c41 * (320 - 175 * c41)));
            double c45 = c41 / 1024 * (256 + c41 * (-128 + c41 * (74 - 47 * c41)));
            double c47 = c45 * Math.sqrt(c31) * (c40 + c45 / 4 * (c33 * (-1 + 2 * c40 * c40) - c45 / 6 * c40 * (-3 + 4 * c31) * (-3 + 4 * c40 * c40)));

            // return distance in meter
            return c16 * c43 * (Math.atan(c35) - c47);
        }
    }

    /* 걸음수 측정 */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            long currentTimeMillis = System.currentTimeMillis();
            if (lastSensorTimeMillis > 0) {
                long dTimeMillis = currentTimeMillis - lastSensorTimeMillis;
                float speed = Math.abs(x + y + z - lastx - lasty - lastz) / dTimeMillis * 10000; // Meter/msec
                if (speed > 800) {
                    String text = String.valueOf(++stepCount / 2) + " 회";
                    stepCountView.setText(text);
                }
            }
            lastx = x;
            lasty = y;
            lastz = z;
            lastSensorTimeMillis = currentTimeMillis;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /* 달린 시간 측정 */
    @SuppressLint("StaticFieldLeak")
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
        record.setStepCount(record.getStepCount() + stepCount / 2);
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

    private void onStartRunning() {
        if (locPermState && locSettingState)
            requestLastLocation();
        if (accSensor != null)
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        new TimeTask().execute();

        isRunning = true;
        actionBtn.setText("운동 정지");
    }

    private void onStopRunning() {
        locClient.removeLocationUpdates(locCallback);
        lastLoc = null; // 운동정지한 동안에는 거리측정하지 않으므로 마지막 위치 초기화
        if (accSensor != null)
            sensorManager.unregisterListener(this, accSensor);
        isRunning = false;
        actionBtn.setText("운동 시작");
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            onStopRunning(); // 운동정지
        }
    }
}
