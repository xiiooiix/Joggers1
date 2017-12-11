package kkt.com.joggers.fragment;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kkt.com.joggers.R;
import kkt.com.joggers.activity.BoardWriteActivity;
import kkt.com.joggers.adapter.BoardAdapter;
import kkt.com.joggers.adapter.MarginItemDecoration;
import kkt.com.joggers.controller.OnScrollEndListener;

public class BoardFragment extends Fragment implements View.OnClickListener, ValueAnimator.AnimatorUpdateListener {
    private RecyclerView rcView;
    private FloatingActionButton floatingActionButton;
    private FloatingActionButton writeButton;
    private FloatingActionButton myBoardButton;
    private FloatingActionButton myHeartButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /* 레이아웃 설정 */
        View view = inflater.inflate(R.layout.fragment_board, container, false);
        rcView = view.findViewById(R.id.rcView);
        rcView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rcView.addItemDecoration(new MarginItemDecoration());

        /* 게시판 리스트 생성 */
        BoardAdapter adapter = new BoardAdapter(getContext());
        rcView.setAdapter(adapter);
        rcView.addOnScrollListener(new OnScrollEndListener());

        /* 플로팅 액션 버튼 활성화/비활성화 */
        floatingActionButton = view.findViewById(R.id.floating_action);
        floatingActionButton.setOnClickListener(this);

        /* 글쓰기 버튼 */
        writeButton = view.findViewById(R.id.write_btn);
        writeButton.setOnClickListener(this);

        /* 내 게시물 버튼 */
        myBoardButton = view.findViewById(R.id.my_board_btn);
        myBoardButton.setOnClickListener(this);

        /* '좋아요' 게시물 버튼 */
        myHeartButton = view.findViewById(R.id.my_heart_btn);
        myHeartButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == floatingActionButton) {
            ValueAnimator animator;
            if (floatingActionButton.getRotation() != 0) { // 활성화->비활성화
                animator = ValueAnimator.ofFloat(1, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    floatingActionButton.setForeground(getContext().getResources().getDrawable(R.drawable.ic_menu, null));
            } else { // 비활성화->활성화
                animator = ValueAnimator.ofFloat(0, 1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    floatingActionButton.setForeground(getContext().getResources().getDrawable(R.drawable.ic_close, null));
            }
            animator.addUpdateListener(this);
            animator.setDuration(500).start();
        } else if (v == writeButton) {
            Intent intent = new Intent(getContext(), BoardWriteActivity.class);
            startActivity(intent);
        } else if (v == myBoardButton) {
            BoardAdapter adapter = (BoardAdapter) rcView.getAdapter();
            adapter.changeMyBoardFilter();
        } else if (v == myHeartButton) {
            BoardAdapter adapter = (BoardAdapter) rcView.getAdapter();
            adapter.changeMyHeartFilter();
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float value = (float) animation.getAnimatedValue();
        float scale = (float) Math.pow(value - 0.5, 2) * 2 + 0.8f;
        floatingActionButton.setScaleX(scale);
        floatingActionButton.setScaleY(scale);
        floatingActionButton.setRotation(value * 360);
        writeButton.setScaleX(value);
        writeButton.setScaleY(value);
        writeButton.setTranslationY(-value * 140);
        writeButton.setElevation(value * 8);
        myBoardButton.setScaleX(value);
        myBoardButton.setScaleY(value);
        myBoardButton.setTranslationX(-value * 140);
        myBoardButton.setElevation(value * 8);
        myHeartButton.setScaleX(value);
        myHeartButton.setScaleY(value);
        myHeartButton.setTranslationX(-value * 140);
        myHeartButton.setTranslationY(-value * 140);
        myHeartButton.setElevation(value * 8);
    }

}