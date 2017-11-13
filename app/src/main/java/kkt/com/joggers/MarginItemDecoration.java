package kkt.com.joggers;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;

class MarginItemDecoration extends RecyclerView.ItemDecoration {
    private int mVerticalSpaceHeight;

    MarginItemDecoration(int height) {
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
