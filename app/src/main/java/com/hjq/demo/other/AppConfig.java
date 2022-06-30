package com.hjq.demo.other;

import com.hjq.demo.BuildConfig;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2019/09/02
 *    desc   : App 配置管理类
 */
public final class AppConfig {

    /**
     * 当前是否为调试模式
     */
    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    /**
     * 获取当前构建的模式
     */
    public static String getBuildType() {
        return BuildConfig.BUILD_TYPE;
    }

    /**
     * 当前是否要开启日志打印功能
     */
    public static boolean isLogEnable() {
        return BuildConfig.LOG_ENABLE;
    }

    /**
     * 获取当前应用的包名
     */
    public static String getPackageName() {
        return BuildConfig.APPLICATION_ID;
    }

    /**
     * 获取当前应用的版本名
     */
    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 获取当前应用的版本码
     */
    public static int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    /**
     * 获取 Bugly Id
     */
    public static String getBuglyId() {
        return BuildConfig.BUGLY_ID;
    }

    /**
     * 获取服务器主机地址
     */
    public static String getHostUrl() {
        return BuildConfig.HOST_URL;
    }
}