package com.okhttp.daemon.okhttpdemo.base;

import android.app.Activity;
import android.os.Bundle;

import com.okhttp.daemon.okhttpdemo.okhttp.OkHttpUtil;

/**
 * Created by h2h on 2015/8/17.
 */
public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        OkHttpUtil.mOkHttpClient.cancel(this);
    }
}
