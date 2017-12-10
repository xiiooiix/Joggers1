package kkt.com.joggers.adapter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class MarginItemDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = 20;
        outRect.top = 20;
        outRect.left = 20;
        outRect.right = 20;
    }

}
