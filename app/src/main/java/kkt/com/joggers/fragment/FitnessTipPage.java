package kkt.com.joggers.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import kkt.com.joggers.R;

public class FitnessTipPage extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fitness_tip_page, container, false);

        ImageView imageView = view.findViewById(R.id.imageView);
        int resId = getArguments().getInt("resId");
        imageView.setImageDrawable(getResources().getDrawable(resId, null));

        return view;
    }

}