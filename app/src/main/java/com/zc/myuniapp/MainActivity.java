package com.zc.myuniapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.zc.myuniapp.databinding.ActivityMainBinding;
import com.zc.uniappmodule.UniAppCenter;
import io.dcloud.feature.sdk.DCUniMPSDK;
import io.dcloud.feature.sdk.Interface.IUniMP;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityMainBinding binding;
    private String uniAppId="__UNI__095A990";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.button1.setOnClickListener(this);
        UniAppCenter.getInstance().checkUpdate(this, "https://www.qmdbao.com/checkVersion.json", new UniAppCenter.UpdateListener2() {
            @Override
            public void complete() {
                Log.d(TAG, "complete: 更新完成");
            }

            @Override
            public void completeFail(Throwable e) {
                Log.d(TAG, "error: 更新失败---"+e.getMessage());
            }
        });
    }


    private static final String TAG = "MainActivity";


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                try {
                    IUniMP iUniMP = UniAppCenter.getInstance().openUniMP(MainActivity.this, uniAppId);
                    App.uniMapCaches.put(uniAppId, iUniMP);
                    Log.d(TAG, "onClick: "+DCUniMPSDK.getInstance().getAppVersionInfo(uniAppId).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onClick: 111");
                break;
            default:
                break;
        }
    }

}