package kkt.com.joggers.board;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

class MarginItemDecoration extends RecyclerView.ItemDecoration {
    private int mVerticalSpaceHeight;

    MarginItemDecoration(int height) {
        this.mVerticalSpaceHeight = height;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = mVerticalSpaceHeight;
        outRect.top = mVerticalSpaceHeight;
        outRect.left = mVerticalSpaceHeight;
        outRect.right = mVerticalSpaceHeight;
    }

}
