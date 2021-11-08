package com.zc.uniappmodule;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.zc.uniappmodule.update.MyView;
import com.zc.uniappmodule.update.ProgressResponseBody;
import com.zc.uniappmodule.update.bean.UpdateBean;
import com.zc.uniappmodule.update.view.LoadingDialog;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import dc.squareup.okhttp3.Call;
import dc.squareup.okhttp3.Callback;
import dc.squareup.okhttp3.OkHttpClient;
import dc.squareup.okhttp3.Request;
import dc.squareup.okhttp3.Response;
import io.dcloud.feature.sdk.DCSDKInitConfig;
import io.dcloud.feature.sdk.DCUniMPSDK;
import io.dcloud.feature.sdk.Interface.IDCUniMPPreInitCallback;
import io.dcloud.feature.sdk.Interface.IMenuButtonClickCallBack;
import io.dcloud.feature.sdk.Interface.IUniMP;

public class UniAppCenter {
    private static UniAppCenter instance;
    private Context context;
    private OkHttpClient okHttpClient;
    private static final String TAG = "UniAppCenter";

    public static UniAppCenter getInstance() {
        if (instance == null) {
            instance = new UniAppCenter();
            return instance;
        }
        return instance;
    }


    public void initialize(Context mContext, DCSDKInitConfig config, IDCUniMPPreInitCallback callback) {
        DCUniMPSDK.getInstance().initialize(mContext, config, callback);
        context = mContext;
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30L, TimeUnit.SECONDS)
                .readTimeout(30L, TimeUnit.SECONDS)
                .writeTimeout(30L, TimeUnit.SECONDS)
                .build();
    }

    private interface UpdateListener {
        void update(String uniappName, String uniappid, int versionCode, String versionName, String download);
        void noUpdate(String uniappid);
        void error(Throwable e);
    }

    public interface UpdateListener2 {
        void complete();
        void completeFail(Throwable e);
    }


    /**
     * 打开小程序
     *
     * @param context  上下文
     * @param uniAppId uniAppId
     * @return IUniMP
     * @throws Exception 异常
     */
    public IUniMP openUniMP(Context context, String uniAppId) throws Exception {
        IUniMP uniMP = DCUniMPSDK.getInstance().openUniMP(context, uniAppId);
        JSONObject jsonObject = DCUniMPSDK.getInstance().getAppVersionInfo(uniAppId);
        if (jsonObject != null && !jsonObject.isNull("name")) {
            setVersionName(jsonObject.getString("name"));
            setVersionCode(Integer.parseInt(jsonObject.getString("code")));
        }
        return uniMP;
    }


    /**
     * 检测更新小程序版本
     *
     * @param updateUrl 更新地址
     * @param listener  回调监听
     */
    public void checkUpdate(Context context, String updateUrl, UpdateListener2 listener) {


        LoadingDialog ld = new LoadingDialog(context);
        ld.setLoadingText("加载中")
                .setSuccessText("加载成功")//显示加载成功时的文字
                //.setFailedText("加载失败")
                .setInterceptBack(true)
                .setLoadSpeed(LoadingDialog.Speed.SPEED_ONE);


        checkUpdate(updateUrl, new UpdateListener() {
            @Override
            public void update(String uniappName, String uniappid, int versionCode, String versionName, String download) {
                ((Activity)context).runOnUiThread(ld::show);
                downloadUniApp(download, context.getCacheDir() + "/" + uniappid + ".wgt", new MyView() {
                    @Override
                    public void onDownload(int progress) {

                    }

                    @Override
                    public void onDownloadFinish(String path) {
                        DCUniMPSDK.getInstance().releaseWgtToRunPathFromePath(uniappid, path, (i, o) -> {
                            listener.complete();
                            ((Activity)context).runOnUiThread(ld::close);
                            if (i == 1) {
                                setVersionName(versionName);
                                setVersionCode(versionCode);
                            }
                            return null;
                        });
                    }

                    @Override
                    public void onDownloadError(Exception e) {
                        listener.completeFail(e);
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void noUpdate(String uniappid) {
                listener.complete();
            }

            @Override
            public void error(Throwable e) {
                listener.completeFail(e);
            }
        });
    }

    /***
     *
     * 检查小程序版本号
     *
     * @param updateUrl 更新地址
     * @param listener 回调监听
     */
    private void checkUpdate(String updateUrl, UpdateListener listener) {
        okHttpClient.newCall(new Request.Builder().url(updateUrl).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.error(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200 ) {
                    UpdateBean updateBean = JSON.parseObject(response.body().string(), UpdateBean.class);
                    if (updateBean.getData().getVersionCode() > getVersionCode()) {
                        listener.update(updateBean.getData().getUniappName(), updateBean.getData().getUniappid(), updateBean.getData().getVersionCode(), updateBean.getData().getVersionName(), updateBean.getData().getDownload());
                        return;
                    }
                    if (DCUniMPSDK.getInstance().isExistsApp(updateBean.getData().getUniappid())) {
                        listener.noUpdate(updateBean.getData().getUniappid());
                    } else {
                        listener.update(updateBean.getData().getUniappName(), updateBean.getData().getUniappid(), updateBean.getData().getVersionCode(), updateBean.getData().getVersionName(), updateBean.getData().getDownload());
                    }
                } else {
                    listener.error(new Exception(response.body().string()));
                }
            }
        });
    }


    /**
     * 获取小程序版本名
     */
    public String getVersionName() {
        SharedPreferences sp = context.getSharedPreferences("uniapp_config", Context.MODE_PRIVATE);
        String versionName=sp.getString("versionName", "0");
        Log.d(TAG, "versionName: "+versionName);
        return versionName;
    }

    /**
     * 获取小程序版本号
     *
     */
    public int getVersionCode() {
        SharedPreferences sp = context.getSharedPreferences("uniapp_config", Context.MODE_PRIVATE);
        int versionCode=sp.getInt("versionCode", 0);
        Log.d(TAG, "versionName: "+versionCode);
        return versionCode;
    }

    /**
     * 设置小程序版本名
     */
    public void setVersionName(String versionName) {
        SharedPreferences sp = context.getSharedPreferences("uniapp_config", Context.MODE_PRIVATE);
        Log.d(TAG, "versionName: "+versionName);
        sp.edit().putString("versionName", versionName).apply();
    }

    /**
     * 设置小程序版本号
     */
    public void setVersionCode(int versionCode) {
        SharedPreferences sp = context.getSharedPreferences("uniapp_config", Context.MODE_PRIVATE);
        Log.d(TAG, "setVersionCode: "+versionCode);
        sp.edit().putInt("versionCode", versionCode).apply();
    }


    public void setDefMenuButtonClickCallBack(IMenuButtonClickCallBack callBack) {
        DCUniMPSDK.getInstance().setDefMenuButtonClickCallBack(callBack);
    }


    public void downloadUniApp(String url, String path, MyView myView) {
        okHttpClient = okHttpClient.newBuilder().addNetworkInterceptor(chain -> {
            Response response = chain.proceed(chain.request());
            return response.newBuilder()
                    .body(new ProgressResponseBody(response.body(), (bytesRead, contentLength, done) -> {
                        int percent = (int) (100 * bytesRead / contentLength);
                        myView.onDownload(percent);
                    }))
                    .build();
        }).build();
        Call call = okHttpClient.newCall(new Request.Builder()
                .url(url)
                .build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                myView.onDownloadError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //从响应体读取字节流
                final byte[] data = response.body().bytes();
                OutputStream out = new FileOutputStream(path);
                InputStream is = new ByteArrayInputStream(data);
                byte[] buff = new byte[1024];
                int len = 0;
                while ((len = is.read(buff)) != -1) {
                    out.write(buff, 0, len);
                }
                is.close();
                out.close();
                myView.onDownloadFinish(path);
            }
        });
    }

}
