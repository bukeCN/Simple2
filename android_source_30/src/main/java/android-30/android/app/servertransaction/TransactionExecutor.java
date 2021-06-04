/*
 * Copyright 2017 The Android Open Source Project
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
 * limitations under the License.
 */

package android.app.servertransaction;

import static android.app.servertransaction.ActivityLifecycleItem.ON_CREATE;
import static android.app.servertransaction.ActivityLifecycleItem.ON_DESTROY;
import static android.app.servertransaction.ActivityLifecycleItem.ON_PAUSE;
import static android.app.servertransaction.ActivityLifecycleItem.ON_RESTART;
import static android.app.servertransaction.ActivityLifecycleItem.ON_RESUME;
import static android.app.servertransaction.ActivityLifecycleItem.ON_START;
import static android.app.servertransaction.ActivityLifecycleItem.ON_STOP;
import static android.app.servertransaction.ActivityLifecycleItem.UNDEFINED;
import static android.app.servertransaction.TransactionExecutorHelper.getShortActivityName;
import static android.app.servertransaction.TransactionExecutorHelper.getStateName;
import static android.app.servertransaction.TransactionExecutorHelper.lastCallbackRequestingState;
import static android.app.servertransaction.TransactionExecutorHelper.tId;
import static android.app.servertransaction.TransactionExecutorHelper.transactionToString;

import android.app.ActivityThread.ActivityClientRecord;
import android.app.ClientTransactionHandler;
import android.os.IBinder;
import android.util.IntArray;
import android.util.Slog;

import com.android.internal.annotations.VisibleForTesting;

import java.util.List;
import java.util.Map;

/**
 * Class that manages transaction execution in the correct order.
 * @hide
 */
public class TransactionExecutor {

    private static final boolean DEBUG_RESOLVER = false;
    private static final String TAG = "TransactionExecutor";

    private ClientTransactionHandler mTransactionHandler;// 实现者为ActivityThread
    private android.app.servertransaction.PendingTransactionActions mPendingActions = new android.app.servertransaction.PendingTransactionActions();
    private android.app.servertransaction.TransactionExecutorHelper mHelper = new android.app.servertransaction.TransactionExecutorHelper();

    /** Initialize an instance with transaction handler, that will execute all requested actions. */
    public TransactionExecutor(ClientTransactionHandler clientTransactionHandler) {
        mTransactionHandler = clientTransactionHandler;
    }

    /**
     * Resolve transaction.
     * First all callbacks will be executed in the order they appear in the list. If a callback
     * requires a certain pre- or post-execution state, the client will be transitioned accordingly.
     * Then the client will cycle to the final lifecycle state if provided. Otherwise, it will
     * either remain in the initial state, or last state needed by a callback.
     */
    public void execute(android.app.servertransaction.ClientTransaction transaction) {
        if (DEBUG_RESOLVER) Slog.d(TAG, tId(transaction) + "Start resolving transaction");

        final IBinder token = transaction.getActivityToken();
        if (token != null) {
            // 获取要销毁的 activity
            final Map<IBinder, android.app.servertransaction.ClientTransactionItem> activitiesToBeDestroyed =
                    mTransactionHandler.getActivitiesToBeDestroyed();
            final android.app.servertransaction.ClientTransactionItem destroyItem = activitiesToBeDestroyed.get(token);
            // 如果要销毁的 activity 不为 null
            if (destroyItem != null) {
                if (transaction.getLifecycleStateRequest() == destroyItem) {
                    // It is going to execute the transaction that will destroy activity with the
                    // token, so the corresponding to-be-destroyed record can be removed.
                    // 移除处理
                    activitiesToBeDestroyed.remove(token);
                }
                if (mTransactionHandler.getActivityClient(token) == null) {
                    // The activity has not been created but has been requested to destroy, so all
                    // transactions for the token are just like being cancelled.
                    Slog.w(TAG, tId(transaction) + "Skip pre-destroyed transaction:\n"
                            + transactionToString(transaction, mTransactionHandler));
                    return;
                }
            }
        }

        if (DEBUG_RESOLVER) Slog.d(TAG, transactionToString(transaction, mTransactionHandler));

        executeCallbacks(transaction);

        executeLifecycleState(transaction);
        mPendingActions.clear();
        if (DEBUG_RESOLVER) Slog.d(TAG, tId(transaction) + "End resolving transaction");
    }

    /** Cycle through all states requested by callbacks and execute them at proper times. */
    @VisibleForTesting
    public void executeCallbacks(android.app.servertransaction.ClientTransaction transaction) {
        final List<android.app.servertransaction.ClientTransactionItem> callbacks = transaction.getCallbacks();
        // callback 对于具体的 LifecycItem
        if (callbacks == null || callbacks.isEmpty()) {
            // No callbacks to execute, return early.
            return;
        }
        if (DEBUG_RESOLVER) Slog.d(TAG, tId(transaction) + "Resolving callbacks in transaction");

        final IBinder token = transaction.getActivityToken();
        // 问题？ActivityThread 中的 ActivityClientRecord 值什么时候添加进来的？？
        // 1. transaction = LaunchActivityItem 时，会在下面步骤创建
        ActivityClientRecord r = mTransactionHandler.getActivityClient(token);

        // In case when post-execution state of the last callback matches the final state requested
        // for the activity in this transaction, we won't do the last transition here and do it when
        // moving to final state instead (because it may contain additional parameters from server).
        final android.app.servertransaction.ActivityLifecycleItem finalStateRequest = transaction.getLifecycleStateRequest();
        final int finalState = finalStateRequest != null ? finalStateRequest.getTargetState()
                : UNDEFINED;
        // Index of the last callback that requests some post-execution state.
        final int lastCallbackRequestingState = lastCallbackRequestingState(transaction);

        final int size = callbacks.size();
        for (int i = 0; i < size; ++i) {
            final android.app.servertransaction.ClientTransactionItem item = callbacks.get(i);
            if (DEBUG_RESOLVER) Slog.d(TAG, tId(transaction) + "Resolving callback: " + item);
            final int postExecutionState = item.getPostExecutionState();
            final int closestPreExecutionState = mHelper.getClosestPreExecutionState(r,
                    item.getPostExecutionState());
            if (closestPreExecutionState != UNDEFINED) {
                cycleToPath(r, closestPreExecutionState, transaction);
            }
            // 1. 重点，LaunchActivityItem 这里会创建 ActivityClientRecord。
            //    这里会调用 mTransactionHandler.handleLaunchActivity() 函数启动 activity。
            // 2. 不同的 ClientTransaction 有不同的动作
            item.execute(mTransactionHandler, token, mPendingActions);
            item.postExecute(mTransactionHandler, token, mPendingActions);
            if (r == null) {
                // Launch activity request will create an activity record.
                r = mTransactionHandler.getActivityClient(token);
            }

            if (postExecutionState != UNDEFINED && r != null) {
                // Skip the very last transition and perform it by explicit state request instead.
                final boolean shouldExcludeLastTransition =
                        i == lastCallbackRequestingState && finalState == postExecutionState;
                // 执行后续的生命周期, 生命周期是根据执行路径来的，从开始到结束。
                cycleToPath(r, postExecutionState, shouldExcludeLastTransition, transaction);
            }
        }
    }

    /** Transition to the final state if requested by the transaction. */
    private void executeLifecycleState(android.app.servertransaction.ClientTransaction transaction) {
        final android.app.servertransaction.ActivityLifecycleItem lifecycleItem = transaction.getLifecycleStateRequest();
        if (lifecycleItem == null) {
            // No lifecycle request, return early.
            return;
        }

        final IBinder token = transaction.getActivityToken();
        final ActivityClientRecord r = mTransactionHandler.getActivityClient(token);
        if (DEBUG_RESOLVER) {
            Slog.d(TAG, tId(transaction) + "Resolving lifecycle state: "
                    + lifecycleItem + " for activity: "
                    + getShortActivityName(token, mTransactionHandler));
        }

        if (r == null) {
            // Ignore requests for non-existent client records for now.
            return;
        }

        // Cycle to the state right before the final requested state.
        cycleToPath(r, lifecycleItem.getTargetState(), true /* excludeLastState */, transaction);

        // Execute the final transition with proper parameters.
        lifecycleItem.execute(mTransactionHandler, token, mPendingActions);
        lifecycleItem.postExecute(mTransactionHandler, token, mPendingActions);
    }

    /** Transition the client between states. */
    @VisibleForTesting
    public void cycleToPath(ActivityClientRecord r, int finish, android.app.servertransaction.ClientTransaction transaction) {
        cycleToPath(r, finish, false /* excludeLastState */, transaction);
    }

    /**
     * Transition the client between states with an option not to perform the last hop in the
     * sequence. This is used when resolving lifecycle state request, when the last transition must
     * be performed with some specific parameters.
     */
    private void cycleToPath(ActivityClientRecord r, int finish, boolean excludeLastState,
            android.app.servertransaction.ClientTransaction transaction) {
        // 获取开始的生命周期开始
        final int start = r.getLifecycleState();
        if (DEBUG_RESOLVER) {
            Slog.d(TAG, tId(transaction) + "Cycle activity: "
                    + getShortActivityName(r.token, mTransactionHandler)
                    + " from: " + getStateName(start) + " to: " + getStateName(finish)
                    + " excludeLastState: " + excludeLastState);
        }
        // 计算路径
        final IntArray path = mHelper.getLifecyclePath(start, finish, excludeLastState);
        // 循环执行后续的生命周期
        performLifecycleSequence(r, path, transaction);
    }

    /** Transition the client through previously initialized state sequence. */
    private void performLifecycleSequence(ActivityClientRecord r, IntArray path,
            android.app.servertransaction.ClientTransaction transaction) {
        final int size = path.size();
        for (int i = 0, state; i < size; i++) {
            state = path.get(i);
            if (DEBUG_RESOLVER) {
                Slog.d(TAG, tId(transaction) + "Transitioning activity: "
                        + getShortActivityName(r.token, mTransactionHandler)
                        + " to state: " + getStateName(state));
            }
            switch (state) {
                case ON_CREATE:
                    mTransactionHandler.handleLaunchActivity(r, mPendingActions,
                            null /* customIntent */);
                    break;
                case ON_START:
                    mTransactionHandler.handleStartActivity(r.token, mPendingActions);
                    break;
                case ON_RESUME:
                    mTransactionHandler.handleResumeActivity(r.token, false /* finalStateRequest */,
                            r.isForward, "LIFECYCLER_RESUME_ACTIVITY");
                    break;
                case ON_PAUSE:
                    mTransactionHandler.handlePauseActivity(r.token, false /* finished */,
                            false /* userLeaving */, 0 /* configChanges */, mPendingActions,
                            "LIFECYCLER_PAUSE_ACTIVITY");
                    break;
                case ON_STOP:
                    mTransactionHandler.handleStopActivity(r.token, 0 /* configChanges */,
                            mPendingActions, false /* finalStateRequest */,
                            "LIFECYCLER_STOP_ACTIVITY");
                    break;
                case ON_DESTROY:
                    mTransactionHandler.handleDestroyActivity(r.token, false /* finishing */,
                            0 /* configChanges */, false /* getNonConfigInstance */,
                            "performLifecycleSequence. cycling to:" + path.get(size - 1));
                    break;
                case ON_RESTART:
                    mTransactionHandler.performRestartActivity(r.token, false /* start */);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected lifecycle state: " + state);
            }
        }
    }
}
