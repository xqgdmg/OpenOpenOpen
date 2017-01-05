package com.okhttp.daemon.okhttpdemo.okhttp;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by h2h on 2015/7/29.
 */
public interface OkhttpLister {

    public void requestOk(Response response);
    public void requestError(Request request, IOException e);

}
