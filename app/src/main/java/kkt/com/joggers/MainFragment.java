package kkt.com.joggers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kkt.com.joggers.share.MarginItemDecoration;

public class MainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* 레이아웃 설정 */
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        RecyclerView rcView = view.findViewById(R.id.rcView);
        rcView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rcView.addItemDecoration(new MarginItemDecoration(20));

        /* 리스트 생성 */
        MainAdapter adapter = new MainAdapter();
        rcView.setAdapter(adapter);

        return view;
    }

}
