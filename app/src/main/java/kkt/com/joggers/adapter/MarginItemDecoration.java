package kkt.com.joggers.adapter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;

public class MarginItemDecoration extends RecyclerView.ItemDecoration {
    private int mVerticalSpaceHeight;

    public MarginItemDecoration(int height) {
        this.mVerticalSpaceHeight = height;
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        outRect.bottom = mVerticalSpaceHeight;
        outRect.top = mVerticalSpaceHeight;
        outRect.left = mVerticalSpaceHeight;
        outRect.right = mVerticalSpaceHeight;
    }
}
