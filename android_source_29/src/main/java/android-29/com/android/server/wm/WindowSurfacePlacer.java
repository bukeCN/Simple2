/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.server.wm;

import static com.android.server.wm.WindowManagerDebugConfig.DEBUG;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WITH_CLASS_NAME;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WM;
import static com.android.server.wm.WindowManagerService.H.REPORT_WINDOWS_CHANGE;
import static com.android.server.wm.WindowManagerService.LAYOUT_REPEAT_THRESHOLD;

import android.os.Debug;
import android.os.Trace;
import android.util.Slog;
import android.util.SparseIntArray;

import java.io.PrintWriter;

/**
 * Positions windows and their surfaces.
 *
 * It sets positions of windows by calculating their frames and then applies this by positioning
 * surfaces according to these frames. Z layer is still assigned withing WindowManagerService.
 */
class WindowSurfacePlacer {
    private static final String TAG = TAG_WITH_CLASS_NAME ? "WindowSurfacePlacer" : TAG_WM;
    private final com.android.server.wm.WindowManagerService mService;

    private boolean mInLayout = false;

    /** Only do a maximum of 6 repeated layouts. After that quit */
    private int mLayoutRepeatCount;

    static final int SET_UPDATE_ROTATION                = 1 << 0;
    static final int SET_ORIENTATION_CHANGE_COMPLETE    = 1 << 2;
    static final int SET_WALLPAPER_ACTION_PENDING       = 1 << 3;

    private boolean mTraversalScheduled;
    private int mDeferDepth = 0;

    private final SparseIntArray mTempTransitionReasons = new SparseIntArray();

    private final Runnable mPerformSurfacePlacement;

    public WindowSurfacePlacer(com.android.server.wm.WindowManagerService service) {
        mService = service;
        mPerformSurfacePlacement = () -> {
            synchronized (mService.mGlobalLock) {
                performSurfacePlacement();
            }
        };
    }

    /**
     * See {@link WindowManagerService#deferSurfaceLayout()}
     */
    void deferLayout() {
        mDeferDepth++;
    }

    /**
     * See {@link WindowManagerService#continueSurfaceLayout()}
     */
    void continueLayout() {
        mDeferDepth--;
        if (mDeferDepth <= 0) {
            performSurfacePlacement();
        }
    }

    boolean isLayoutDeferred() {
        return mDeferDepth > 0;
    }

    void performSurfacePlacementIfScheduled() {
        if (mTraversalScheduled) {
            performSurfacePlacement();
        }
    }

    final void performSurfacePlacement() {
        performSurfacePlacement(false /* force */);
    }

    final void performSurfacePlacement(boolean force) {
        if (mDeferDepth > 0 && !force) {
            return;
        }
        // 为什么这里会有六次循环？一次可能不会完成布局，但是不能超过 6 次。
        int loopCount = 6;
        do {
            mTraversalScheduled = false;
            performSurfacePlacementLoop();
            mService.mAnimationHandler.removeCallbacks(mPerformSurfacePlacement);
            loopCount--;
        } while (mTraversalScheduled && loopCount > 0);
        mService.mRoot.mWallpaperActionPending = false;
    }

    private void performSurfacePlacementLoop() {
        if (mInLayout) {
            if (DEBUG) {
                throw new RuntimeException("Recursive call!");
            }
            Slog.w(TAG, "performLayoutAndPlaceSurfacesLocked called while in layout. Callers="
                    + Debug.getCallers(3));
            return;
        }

        // TODO(multi-display):
        final com.android.server.wm.DisplayContent defaultDisplay = mService.getDefaultDisplayContentLocked();
        if (defaultDisplay.mWaitingForConfig) {
            // Our configuration has changed (most likely rotation), but we
            // don't yet have the complete configuration to report to
            // applications.  Don't do any window layout until we have it.
            return;
        }

        if (!mService.mDisplayReady) {
            // Not yet initialized, nothing to do.
            return;
        }

        Trace.traceBegin(Trace.TRACE_TAG_WINDOW_MANAGER, "wmLayout");
        mInLayout = true;

        // 在重新布局之前，清理僵尸窗口，释放它们所持有的 Surface。 和 Java GC 很类型，
        boolean recoveringMemory = false;
        if (!mService.mForceRemoves.isEmpty()) {
            recoveringMemory = true;// 标记此次布局已经完成内存回收
            // Wait a little bit for things to settle down, and off we go.
            while (!mService.mForceRemoves.isEmpty()) {
                final WindowState ws = mService.mForceRemoves.remove(0);
                Slog.i(TAG, "Force removing: " + ws);
                ws.removeImmediately();
            }
            Slog.w(TAG, "Due to memory failure, waiting a bit for next layout");
            Object tmp = new Object();
            synchronized (tmp) {
                try {
                    tmp.wait(250);
                } catch (InterruptedException e) {
                }
            }
        }

        try {
            // 重点哦! 继续布局
            mService.mRoot.performSurfacePlacement(recoveringMemory);

            mInLayout = false;

            // 遍历 DisplayContent ，只要有一个 DisplayContent 需要布局，这里就会返回 true. 可能就是在布局中有屏幕发生变化了
            if (mService.mRoot.isLayoutNeeded()) {
                if (++mLayoutRepeatCount < 6) {
                    // 注意此方法调用哦，会改变上一步循环的节奏
                    requestTraversal();
                } else {
                    Slog.e(TAG, "Performed 6 layouts in a row. Skipping");
                    mLayoutRepeatCount = 0;
                }
            } else {
                mLayoutRepeatCount = 0;
            }

            // 通知监听者，窗口布局发生了变化。
            if (mService.mWindowsChanged && !mService.mWindowChangeListeners.isEmpty()) {
                mService.mH.removeMessages(REPORT_WINDOWS_CHANGE);
                mService.mH.sendEmptyMessage(REPORT_WINDOWS_CHANGE);
            }
        } catch (RuntimeException e) {
            mInLayout = false;
            Slog.wtf(TAG, "Unhandled exception while laying out windows", e);
        }

        Trace.traceEnd(Trace.TRACE_TAG_WINDOW_MANAGER);
    }

    void debugLayoutRepeats(final String msg, int pendingLayoutChanges) {
        if (mLayoutRepeatCount >= LAYOUT_REPEAT_THRESHOLD) {
            Slog.v(TAG, "Layouts looping: " + msg +
                    ", mPendingLayoutChanges = 0x" + Integer.toHexString(pendingLayoutChanges));
        }
    }

    boolean isInLayout() {
        return mInLayout;
    }

    void requestTraversal() {
        if (!mTraversalScheduled) {
            mTraversalScheduled = true;
            mService.mAnimationHandler.post(mPerformSurfacePlacement);
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "mTraversalScheduled=" + mTraversalScheduled);
        pw.println(prefix + "mHoldScreenWindow=" + mService.mRoot.mHoldScreenWindow);
        pw.println(prefix + "mObscuringWindow=" + mService.mRoot.mObscuringWindow);
    }
}
