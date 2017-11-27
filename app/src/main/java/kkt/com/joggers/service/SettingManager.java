package kkt.com.joggers.service;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingManager {
    private SharedPreferences preferences;
    private boolean stopOnCalling; //전화 수신 시 운동중지
    private boolean vibration; //진동 사용
    private boolean notification; //알림 허용
    private boolean musicAutoPlay; //음악 자동재생

    public SettingManager(Context context) {
        preferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        stopOnCalling = preferences.getBoolean("stopOnCalling", true);
        vibration = preferences.getBoolean("vibration", true);
        notification = preferences.getBoolean("notification", true);
        musicAutoPlay = preferences.getBoolean("musicAutoPlay", true);
    }

    public void save() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("stopOnCalling", stopOnCalling);
        editor.putBoolean("vibration", vibration);
        editor.putBoolean("notification", notification);
        editor.putBoolean("musicAutoPlay", musicAutoPlay);
        editor.apply();
    }

    public boolean isStopOnCalling() {
        return stopOnCalling;
    }

    public void setStopOnCalling(boolean stopOnCalling) {
        this.stopOnCalling = stopOnCalling;
    }

    public boolean isVibration() {
        return vibration;
    }

    public void setVibration(boolean vibration) {
        this.vibration = vibration;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public boolean isMusicAutoPlay() {
        return musicAutoPlay;
    }

    public void setMusicAutoPlay(boolean musicAutoPlay) {
        this.musicAutoPlay = musicAutoPlay;
    }

}
