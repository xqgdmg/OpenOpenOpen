package com.okhttp.daemon.okhttpdemo;

import android.app.Application;
import android.content.Context;

import com.okhttp.daemon.okhttpdemo.okhttp.OkHttpUtil;

import java.net.CookieManager;

/**
 * Created by h2h on 2015/8/17.
 */
public class MyApplication extends Application {

    private static Context mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = getBaseContext();

        //cookie策略 让OkHttp接受所有的cookie
        CookieManager cookieManager = new CookieManager();
        OkHttpUtil.setCookie(cookieManager);

    }



    public static Context getContext() {
        return mInstance;
    }

}
