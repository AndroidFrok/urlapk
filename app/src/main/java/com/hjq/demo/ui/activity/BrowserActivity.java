package com.hjq.demo.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hjq.demo.R;
import com.hjq.demo.action.StatusAction;
import com.hjq.demo.aop.CheckNet;
import com.hjq.demo.aop.Log;
import com.hjq.demo.app.AppActivity;
import com.hjq.demo.manager.ThreadPoolManager;
import com.hjq.demo.ui.adapter.JSHook;
import com.hjq.demo.widget.BrowserView;
import com.hjq.demo.widget.StatusLayout;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2018/10/18
 * desc   : 浏览器界面
 */
public final class BrowserActivity extends AppActivity implements StatusAction, OnRefreshListener, Runnable {

    private static final String INTENT_KEY_IN_URL = "url";

    @CheckNet
    @Log
    public static void start(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Intent intent = new Intent(context, BrowserActivity.class);
        intent.putExtra(INTENT_KEY_IN_URL, url);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    private StatusLayout mStatusLayout;
    private ProgressBar mProgressBar;
    private SmartRefreshLayout mRefreshLayout;
    private BrowserView mBrowserView;

    @Override
    protected int getLayoutId() {
        return R.layout.browser_activity;
    }

    @Override
    protected void initView() {
        mStatusLayout = findViewById(R.id.hl_browser_hint);
        mProgressBar = findViewById(R.id.pb_browser_progress);
        mRefreshLayout = findViewById(R.id.sl_browser_refresh);
        mBrowserView = findViewById(R.id.wv_browser_view);
        mBrowserView.addJavascriptInterface(new JSHook(), "hook");

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                mBrowserView.loadUrl(u);
            }
        });

        // 设置 WebView 生命管控
        mBrowserView.setLifecycleOwner(this);
        // 设置网页刷新监听
        mRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected void initData() {
        showLoading();

        mBrowserView.setBrowserViewClient(new AppBrowserViewClient());
        mBrowserView.setBrowserChromeClient(new AppBrowserChromeClient(mBrowserView));
        mBrowserView.loadUrl(getString(INTENT_KEY_IN_URL));
    }

    @Override
    public StatusLayout getStatusLayout() {
        return mStatusLayout;
    }

    @Override
    public void onLeftClick(View view) {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mBrowserView.canGoBack()) {
            // 后退网页并且拦截该事件
            mBrowserView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 重新加载当前页
     */
    @CheckNet
    private void reload() {
        mBrowserView.reload();
    }

    /**
     * {@link OnRefreshListener}
     */

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        reload();
    }

    @Override
    public void run() {

    }

    private class AppBrowserViewClient extends BrowserView.BrowserViewClient {

        /**
         * 网页加载错误时回调，这个方法会在 onPageFinished 之前调用
         */
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            // 这里为什么要用延迟呢？因为加载出错之后会先调用 onReceivedError 再调用 onPageFinished
            post(() -> showError(listener -> reload()));
        }

        private void doTakePhoto() {
            XXPermissions.with(getActivity()).permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE).request(new OnPermissionCallback() {
                @Override
                public void onGranted(List<String> permissions, boolean all) {
                    CameraActivity.start(BrowserActivity.this, new CameraActivity.OnCameraListener() {
                        @Override
                        public void onSelected(File file) {
                            if (file == null || !file.exists()) {
                                ToastUtils.show("文件不存在");
                                return;
                            }
                            sendPic(file);
                            // 当前选中图片的数量必须小于最大选中数
                            // 这里需要延迟刷新，否则可能会找不到拍照的图片
//                                mBrowserView.loadUrl("javascript:isMobile()");
//                                view.loadUrl("http://www.baidu.com");
                        }

                        @Override
                        public void onError(String details) {
                            toast(details);
                        }
                    });
                }

                @Override
                public void onDenied(List<String> permissions, boolean never) {
                    OnPermissionCallback.super.onDenied(permissions, never);
                }
            });

        }

        private void sendPic(File file) {
            File fileCompress = getCompress(file);
//                                ToastUtils.show("" + file.getPath());

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Timber.tag("拦截").d("拦截到的url：" + url);
            //url如果包含doScan参数值，就是h5和我们定义的传值协议
            if (url.contains("doScan")) {
//                mBrowserView.loadUrl("javascript:showPic(" + a + ");");
//                url = URLEncoder.encode(url);
                doTakePhoto();
                return true;
            } else {
                view.loadUrl(url);
            }
            return true;
        }

        private File getCompress(File file) {
            final File[] f = {null};
            ThreadPoolManager.getInstance().execute(() -> {
                try {
//                    f[0] = Glide.with(getActivity()).asFile().load(file).submit(200, 200).get();
                    Bitmap bitmap = Glide.with(getActivity()).asBitmap().load(file).submit(200, 200).get();

                    String path2 = Environment.getExternalStorageDirectory().getPath() + "/NAME_PIC_FILE.jpg";
                    savePicToSdcard(path2, bitmap);
                    try {

                        //                                图片压缩
//                                    FileOutputStream fileOutputStream = new FileOutputStream(fileCompress.getPath());
                        FileInputStream fileInputStream = new FileInputStream(path2);
                        File compressed = new File(path2);
                        byte[] buf = new byte[(int) compressed.length()];
                        Timber.d("压缩后文件  " + compressed.getAbsolutePath());
                        fileInputStream.read(buf);
                        fileInputStream.close();
                        String s = Base64.encodeToString(buf, Base64.DEFAULT);
//                                    Timber.d("base64:" + s);
//                        String path1 = "\"" + compressed.getAbsolutePath() + "\"";
                        String s1 = "\"" + s + "\"";
//                                    path1 = "\"https://img-blog.csdnimg.cn/20210317155344671.png\"";
//                        String u = "javascript:showPic(" + path1 + ");";
                        String u1 = "javascript:showPic(" + s1 + ");";
                        Timber.d(u1);
                        runOnUiThread(() -> mBrowserView.loadUrl(u1));
                        //                                    mBrowserView.loadUrl("javascript:showPic(" + s + ");");
//                                    mBrowserView.loadUrl("javascript:showPic(" + "" + ");");
//                                    view.loadUrl("javascript:showPic('s')");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            return f[0];
        }

        /**
         * 开始加载网页
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        /**
         * 完成加载网页
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            mProgressBar.setVisibility(View.GONE);
            mRefreshLayout.finishRefresh();
            showComplete();
        }
    }

    /**
     * 保存图片到sdcard
     *
     * @param bitmap
     */
    public static void savePicToSdcard(String path, Bitmap bitmap) {
        if (bitmap != null) {
            try {
                FileOutputStream out = new FileOutputStream(path);
                bitmap.compress(Bitmap.CompressFormat.PNG, 10, out);
                out.flush();
                out.close();
                bitmap.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class AppBrowserChromeClient extends BrowserView.BrowserChromeClient {

        private AppBrowserChromeClient(BrowserView view) {
            super(view);
        }

        /**
         * 收到网页标题
         */
        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (title == null) {
                return;
            }
            setTitle(title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            if (icon == null) {
                return;
            }
            setRightIcon(new BitmapDrawable(getResources(), icon));
        }

        /**
         * 收到加载进度变化
         */
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            mProgressBar.setProgress(newProgress);
        }
    }
}