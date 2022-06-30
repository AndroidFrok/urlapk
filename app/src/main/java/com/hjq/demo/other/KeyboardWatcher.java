package com.hjq.demo.other;

import android.app.Activity;
import android.app.Application;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2019/07/04
 *    desc   : 软键盘监听类
 */
public final class KeyboardWatcher implements
        ViewTreeObserver.OnGlobalLayoutListener,
        Application.ActivityLifecycleCallbacks {

    private Activity mActivity;
    private View mContentView;
    @Nullable
    private SoftKeyboardStateListener mListeners;
    private boolean mSoftKeyboardOpened;
    private int mStatusBarHeight;

    public static KeyboardWatcher with(Activity activity) {
        return new KeyboardWatcher(activity);
    }

    private KeyboardWatcher(Activity activity) {
        mActivity = activity;
        mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mActivity.registerActivityLifecycleCallbacks(this);
        } else {
            mActivity.getApplication().registerActivityLifecycleCallbacks(this);
        }
        mContentView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        // 获取 status_bar_height 资源的 ID
        int resourceId = mActivity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源 ID 获取响应的尺寸值
            mStatusBarHeight = mActivity.getResources().getDimensionPixelSize(resourceId);
        }
    }

    /**
     * {@link ViewTreeObserver.OnGlobalLayoutListener}
     */

    @Override
    public void onGlobalLayout() {
        final Rect r = new Rect();
        //r will be populated with the coordinates of your view that area still visible.
        mContentView.getWindowVisibleDisplayFrame(r);

        final int heightDiff = mContentView.getRootView().getHeight() - (r.bottom - r.top);
        if (!mSoftKeyboardOpened && heightDiff > mContentView.getRootView().getHeight() / 4) {
            mSoftKeyboardOpened = true;
            if (mListeners == null) {
                return;
            }
            if ((mActivity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != WindowManager.LayoutParams.FLAG_FULLSCREEN) {
                mListeners.onSoftKeyboardOpened(heightDiff - mStatusBarHeight);
            } else {
                mListeners.onSoftKeyboardOpened(heightDiff);
            }
        } else if (mSoftKeyboardOpened && heightDiff < mContentView.getRootView().getHeight() / 4) {
            mSoftKeyboardOpened = false;
            if (mListeners == null) {
                return;
            }
            mListeners.onSoftKeyboardClosed();
        }
    }

    /**
     * 设置软键盘弹出监听
     */
    public void setListener(@Nullable SoftKeyboardStateListener listener) {
        mListeners = listener;
    }

    /**
     * {@link Application.ActivityLifecycleCallbacks}
     */

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {}

    @Override
    public void onActivityResumed(@NonNull Activity activity) {}

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (mActivity == activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mActivity.unregisterActivityLifecycleCallbacks(this);
            } else {
                mActivity.getApplication().unregisterActivityLifecycleCallbacks(this);
            }
            mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            mActivity = null;
            mContentView = null;
            mListeners = null;
        }
    }

    /**
     * 软键盘状态监听器
     */
    public interface SoftKeyboardStateListener {

        /**
         * 软键盘弹出了
         *
         * @param keyboardHeight            软键盘高度
         */
        void onSoftKeyboardOpened(int keyboardHeight);

        /**
         * 软键盘收起了
         */
        void onSoftKeyboardClosed();
    }
}