package kkt.com.joggers.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kkt.com.joggers.R;
import kkt.com.joggers.adapter.FitnessTipAdapter;
import kkt.com.joggers.adapter.MarginItemDecoration;

public class FitnessTipFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fitness_tip, container, false);

        RecyclerView rcView = view.findViewById(R.id.rcView);
        rcView.setAdapter(new FitnessTipAdapter(getContext()));

        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rcView.setLayoutManager(gridLayoutManager);

        RecyclerView.ItemDecoration itemDecoration = new MarginItemDecoration(20);
        rcView.addItemDecoration(itemDecoration);

        return view;
    }
}
