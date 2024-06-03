package com.hjq.demo.ui.adapter;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.hjq.toast.ToastUtils;

public class JSHook {
    String tag = "JSHook";

    @JavascriptInterface
    public void javaMethod(String p) {
        Log.d(tag, "JSHook.JavaMethod() called! + " + p);
    }

    @JavascriptInterface
    public void showAndroid(WebView webView) {
        String info = "来自手机内的内容！！！";
        webView.loadUrl("javascript:show('" + info + "')");
    }

    @JavascriptInterface
    public String getInfo() {
        return "获取手机内的信息！！";
    }

    @JavascriptInterface
    public void toast(String msg) {
        ToastUtils.show(msg);
    }
}
