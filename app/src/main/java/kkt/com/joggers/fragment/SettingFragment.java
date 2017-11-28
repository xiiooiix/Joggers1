package kkt.com.joggers.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import kkt.com.joggers.R;
import kkt.com.joggers.controller.SettingManager;

public class SettingFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    private SettingManager manager;
    private Switch stopOnCalling;
    private Switch vibration;
    private Switch notification;
    private Switch musicAutoPlay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        stopOnCalling = view.findViewById(R.id.stopOnCalling);
        vibration = view.findViewById(R.id.vibration);
        notification = view.findViewById(R.id.notification);
        musicAutoPlay = view.findViewById(R.id.musicAutoPlay);

        manager = new SettingManager(getContext());
        stopOnCalling.setChecked(manager.isStopOnCalling());
        vibration.setChecked(manager.isVibration());
        notification.setChecked(manager.isNotification());
        musicAutoPlay.setChecked(manager.isMusicAutoPlay());

        stopOnCalling.setOnCheckedChangeListener(this);
        vibration.setOnCheckedChangeListener(this);
        notification.setOnCheckedChangeListener(this);
        musicAutoPlay.setOnCheckedChangeListener(this);

        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == stopOnCalling)
            manager.setStopOnCalling(isChecked);
        else if (buttonView == vibration)
            manager.setVibration(isChecked);
        else if (buttonView == notification)
            manager.setNotification(isChecked);
        else if (buttonView == musicAutoPlay)
            manager.setMusicAutoPlay(isChecked);
        manager.save();
    }

}
