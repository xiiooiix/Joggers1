package kkt.com.joggers.controller;

import android.support.v7.widget.RecyclerView;

import kkt.com.joggers.adapter.BoardAdapter;
import kkt.com.joggers.adapter.DataLoadLimitAdapter;

public class OnScrollEndListener extends RecyclerView.OnScrollListener {

    @Override
    public void onScrolled(RecyclerView rcView, int dx, int dy) {
        super.onScrolled(rcView, dx, dy);
        if (!rcView.canScrollVertically(1)) { // check able to scroll down
            DataLoadLimitAdapter adapter = (BoardAdapter) rcView.getAdapter();
            adapter.increaseLoadLimit();
        }
    }
}