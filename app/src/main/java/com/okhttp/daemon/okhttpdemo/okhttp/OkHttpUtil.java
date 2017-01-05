package com.okhttp.daemon.okhttpdemo.okhttp;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.apkfuns.logutils.LogUtils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class OkHttpUtil {
    public static final OkHttpClient mOkHttpClient = new OkHttpClient();

    static {
        mOkHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        mOkHttpClient.setWriteTimeout(20, TimeUnit.SECONDS);
        mOkHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
    }

    public interface MyCallBack {
        void onFailure(Request request, IOException e);

        void onResponse(String json);

    }

    public static void setCookie(CookieManager cookieManager) {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        mOkHttpClient.setCookieHandler(cookieManager);
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * 异步回调方式 请求  自定义回调接口  结果运行在UI线程
     *
     * //如果json body这么构建
     * RequestBody body = RequestBody.create(JSON, json);
     *
     * 如果获取sessionID
    Map<String, List<String>> map = new HashMap<String, List<String>>();
    //获取cookie 然后打印  绑定
    try {
    BaseRequest.Cookies = response.headers().get("Set-Cookie");
    Logger.getLogger().e(response.headers().toString());
    map = mOkHttpClient.getCookieHandler().get(URI.create(url), response.headers().toMultimap());
    for (String key : map.keySet()) {
    syso(key + " --  " + map.get(key).toString());
    if ("Cookie".equals(key)) {
    sessionId = map.get(key).get(0);
    }
    }
    } catch (IOException e) {
    e.printStackTrace();
    }
     在请求的时候带上cookie  在Request构建的时候加上头部就行
    .addHeader("cookie", sessionId)

     */
    public static void postData2Server(final Activity activity, Request request, final MyCallBack myCallBack) {

        final String url = request.urlString();
        try {
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(final Request request, final IOException e) {
                    if (activity == null) {
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myCallBack.onFailure(request, e);
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    final String json = response.body().string();

                    if (activity == null) {
                        return;
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myCallBack.onResponse(json);
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Daemon
     * -----------------一开始没有发现好的封装对于上传和下载  就用的下面的两个方法  后面发现在OKHttp的sample中有相关的例子 然后网上也有人写 就借鉴过来了
     * --------------------所以这两个就没用了-----------------------------------------------
     */

    /**
     * 下载显示进度的方法1
     *
     * @param url
     */
    public static void dowloadProgress(Context context, final String url) {

        Request request = new Request.Builder().url(url)
                .build();
        new AsyncDownloader(context).execute(request);

    }

    private static class AsyncDownloader extends AsyncTask<Request, Long, Boolean> {

        private Context context;

        public AsyncDownloader(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Request... params) {

            Call call = mOkHttpClient.newCall(params[0]);
            try {
                Response response = call.execute();
                if (response.code() == 200) {
                    InputStream inputStream = null;
                    OutputStream output = null;
                    try {
                        inputStream = response.body().byteStream();
                        File file = new File(context.getCacheDir(), "download.liubo");
                        output = new FileOutputStream(file);

                        byte[] buff = new byte[1024 * 4];
                        long downloaded = 0;
                        long target = response.body().contentLength();

                        publishProgress(0L, target);
                        while (true) {
                            int readed = inputStream.read(buff);
                            if (readed == -1) {
                                break;
                            }
                            //write buff
                            output.write(buff, 0, readed);

                            downloaded += readed;
                            publishProgress(downloaded, target);

                            if (isCancelled()) {
                                return false;
                            }
                        }
                        output.flush();

                        return downloaded == target;
                    } catch (IOException ignore) {
                        return false;
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (output != null) {
                            output.close();
                        }
                    }
                } else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Long... values) {

            LogUtils.e(String.format("%d / %d", values[0], values[1]));

        }

        @Override
        protected void onPostExecute(Boolean result) {
            //下载完成后
        }
    }

    /**
     * 上传文件方法1   进度显示
     */
    public static RequestBody createCustomRequestBody(final MediaType contentType, final File file) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return file.length();
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(file);
                    //sink.writeAll(source);
                    Buffer buf = new Buffer();
                    Long remaining = contentLength();
                    for (long readCount; (readCount = source.read(buf, 2048)) != -1; ) {
                        sink.write(buf, readCount);
                        LogUtils.e("source size: " + contentLength() + " remaining bytes: " + (remaining -= readCount));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

}