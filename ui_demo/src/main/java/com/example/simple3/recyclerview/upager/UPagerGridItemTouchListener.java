package com.example.simple3.recyclerview.upager;

import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 处理滑动冲突
 *
 * @author ShenBen
 * @date 2021/9/28 09:24
 * @email 714081644@qq.com
 */
class UPagerGridItemTouchListener extends RecyclerView.SimpleOnItemTouchListener {
    private static final String TAG = "ItemTouchListener";

    private final UPagerGridLayoutManager layoutManager;
    private final RecyclerView recyclerView;
    private float startX;
    private float startY;

    UPagerGridItemTouchListener(UPagerGridLayoutManager layoutManager, RecyclerView recyclerView) {
        this.layoutManager = layoutManager;
        this.recyclerView = recyclerView;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        if (UPagerGridLayoutManager.DEBUG) {
            Log.i(TAG, "onInterceptTouchEvent-action: " + e.getAction());
        }
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startX = e.getX();
                startY = e.getY();
                recyclerView.getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float x = e.getX();
                float y = e.getY();

                float disX = Math.abs(x - startX);
                float disY = Math.abs(y - startY);
                if (disX > disY) {
                    if (layoutManager.canScrollHorizontally()) {
                        recyclerView.getParent().requestDisallowInterceptTouchEvent(recyclerView.canScrollHorizontally((int) (startX - x)));
                    } else {
                        recyclerView.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                } else {
                    if (layoutManager.canScrollVertically()) {
                        recyclerView.getParent().requestDisallowInterceptTouchEvent(recyclerView.canScrollVertically((int) (startY - y)));
                    } else {
                        recyclerView.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
                startX = x;
                startY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                recyclerView.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return false;
    }
}
