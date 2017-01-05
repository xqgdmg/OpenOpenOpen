package com.okhttp.daemon.okhttpdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.apkfuns.logutils.LogUtils;
import com.okhttp.daemon.okhttpdemo.base.BaseActivity;
import com.okhttp.daemon.okhttpdemo.okhttp.OkHttpUtil;
import com.okhttp.daemon.okhttpdemo.okhttp.ProgressHelper;
import com.okhttp.daemon.okhttpdemo.okhttp.ProgressRequestBody;
import com.okhttp.daemon.okhttpdemo.okhttp.UIProgressRequestListener;
import com.okhttp.daemon.okhttpdemo.okhttp.UIProgressResponseListener;
import com.okhttp.daemon.okhttpdemo.util.ToastUtil;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    private android.widget.EditText tvshow;
    private android.widget.Button btgetData;
    private android.widget.Button btpostData;
    private android.widget.Button btpostMultiData;
    private Button btdownload;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.btdownload = (Button) findViewById(R.id.bt_download);

        this.btpostMultiData = (Button) findViewById(R.id.bt_postMultiData);
        this.btpostData = (Button) findViewById(R.id.bt_postData);
        this.btgetData = (Button) findViewById(R.id.bt_getData);
        this.tvshow = (EditText) findViewById(R.id.tv_show);

        btpostMultiData.setOnClickListener(this);
        btpostData.setOnClickListener(this);
        btgetData.setOnClickListener(this);
        btdownload.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_getData:
                getData();
                break;

            case R.id.bt_postData:
                postData();
                break;

            case R.id.bt_postMultiData:
                postMulitiData();
                break;

            case R.id.bt_download:
                downloadPic();
                break;
        }
    }


    /**
     * 图文上传
     */

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    private void postMulitiData() {
        if (file == null) {
            ToastUtil.showToast("先下载图片在试着上传吧");
            return;
        }
        tvshow.setText("");

        MultipartBuilder mb = new MultipartBuilder();
        mb.type(MultipartBuilder.FORM);
        mb.addFormDataPart("user_id", "74");
        mb.addFormDataPart("user_head", "user_head.jpg", RequestBody.create(MEDIA_TYPE_PNG, file));
        RequestBody requestBody = mb.build();

        UIProgressRequestListener progressListener = new UIProgressRequestListener() {

            @Override
            public void onUIRequestProgress(long bytesWrite, long contentLength, boolean done) {
                Log.e("TAG", "bytesWrite:" + bytesWrite);
                Log.e("TAG", "contentLength" + contentLength);
                Log.e("TAG", (100 * bytesWrite) / contentLength + " % done ");
                Log.e("TAG", "done:" + done);
                Log.e("TAG", "================================");

                tvshow.setText("上传进度 " + ((100 * bytesWrite) / contentLength) + "%");

            }
        };

        Request request = new Request.Builder()
                .url("上传接口")
                .post(new ProgressRequestBody(requestBody, progressListener))
                .tag(this)
                .build();

        OkHttpUtil.postData2Server(this, request, new OkHttpUtil.MyCallBack() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(String json) {

            }
        });

    }

    /**
     * 数据上传 Post请求
     */
    private void postData() {

        RequestBody requestBody = new FormEncodingBuilder()
                .add("action_flag", "user_login")
                .add("mobile_phone", "18566757335")
                .add("user_password", "liubo123")
                .build();

        Request request = new Request.Builder()
                .post(requestBody)
                .url("登陆接口")
                .tag(this)
                .build();

        OkHttpUtil.postData2Server(this, request, new OkHttpUtil.MyCallBack() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(String json) {
                showData(json);
            }
        });

    }

    /**
     * 数据获取Get请求
     */
    private void getData() {
        Request request
                = new Request.Builder().url("http://www.baidu.com")
                .tag(this)
                .build();

        OkHttpUtil.postData2Server(this, request, new OkHttpUtil.MyCallBack() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(String json) {
                showData(json);

            }
        });

    }

    /**
     * 图片下载
     */
    private void downloadPic() {

        UIProgressResponseListener uiProgressResponseListener = new UIProgressResponseListener() {
            @Override
            public void onUIResponseProgress(long bytesRead, long contentLength, boolean done) {
                LogUtils.e("bytesRead:" + bytesRead);
                LogUtils.e("contentLength:" + contentLength);
                LogUtils.e("done:" + done);

                if (contentLength != -1) {
                    // LogUtils.e( (100 * bytesRead) / contentLength + "% done");
                }
                LogUtils.e("================================");
                //ui层回调
                tvshow.setText("下载进度 " + ((100 * bytesRead) / contentLength) + "%");
            }
        };

        //构造请求
        final Request request1 = new Request.Builder()
                .url("网上资源接口")
                .build();

        //包装Response使其支持进度回调
        ProgressHelper.addProgressResponseListener(OkHttpUtil.mOkHttpClient, uiProgressResponseListener)
                .newCall(request1)
                .enqueue(new Callback() {
                             @Override
                             public void onFailure(Request request, IOException e) {
                                 Log.e("TAG", "error ", e);
                             }

                             @Override
                             public void onResponse(Response response) throws IOException {
                                 InputStream inputStream = null;
                                 OutputStream output = null;
                                 try {
                                     inputStream = response.body().byteStream();
                                     file = new File(getCacheDir(), "download.jpg");
                                     output = new FileOutputStream(file);
                                     byte[] buff = new byte[1024 * 4];
                                     while (true) {
                                         int readed = inputStream.read(buff);
                                         if (readed == -1) {
                                             break;
                                         }
                                         //write buff
                                         output.write(buff, 0, readed);
                                     }
                                     output.flush();
                                 } catch (IOException e) {
                                     file = null;
                                     e.printStackTrace();
                                 } finally {
                                     if (inputStream != null) {
                                         inputStream.close();
                                     }
                                     if (output != null) {
                                         output.close();
                                     }
                                 }
                             }
                         }
                );
    }

    private void showData(String json) {
        tvshow.setText("");
        tvshow.setText(json);
    }
}
