package kkt.com.joggers.share;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class MarginItemDecoration extends RecyclerView.ItemDecoration {
    private int mVerticalSpaceHeight;

    public MarginItemDecoration(int height) {
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
