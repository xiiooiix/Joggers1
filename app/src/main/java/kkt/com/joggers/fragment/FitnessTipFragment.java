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

/**
 * 화면역할: 모든 팁들을 볼 수 있는 프래그먼트
 * 화면위치: Drawer에서 '팁'을 선택하면 FitnessTipFragment로 진입한다
 */
public class FitnessTipFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fitness_tip, container, false);

        RecyclerView rcView = view.findViewById(R.id.rcView);
        rcView.setAdapter(new FitnessTipAdapter(getContext()));

        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rcView.setLayoutManager(gridLayoutManager);

        RecyclerView.ItemDecoration itemDecoration = new MarginItemDecoration();
        rcView.addItemDecoration(itemDecoration);

        return view;
    }
}
