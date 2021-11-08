package com.zc.myuniapp;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.zc.uniappmodule.UniAppCenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.dcloud.common.util.RuningAcitvityUtil;
import io.dcloud.feature.sdk.DCSDKInitConfig;
import io.dcloud.feature.sdk.Interface.IDCUniMPPreInitCallback;
import io.dcloud.feature.sdk.Interface.IMenuButtonClickCallBack;
import io.dcloud.feature.sdk.Interface.IUniMP;
import io.dcloud.feature.sdk.MenuActionSheetItem;


public class App extends Application {
    public static HashMap<String, IUniMP> uniMapCaches = new HashMap<>();
    private static final String TAG = "App";
    @Override
    public void onCreate() {
        super.onCreate();
        MenuActionSheetItem item = new MenuActionSheetItem("关于", "gy");
        MenuActionSheetItem item1 = new MenuActionSheetItem("关闭小程序", "close");
        List<MenuActionSheetItem> sheetItems = new ArrayList<>();
        sheetItems.add(item);
        sheetItems.add(item1);
        DCSDKInitConfig config = new DCSDKInitConfig.Builder()
                .setCapsule(true)
                .setMenuDefFontSize("16px")
                .setMenuDefFontColor("#000000")
                .setMenuDefFontWeight("normal")
                .setMenuActionSheetItems(sheetItems)
                .setUniMPFromRecents(false)
                .setEnableBackground(false)
                .build();
        UniAppCenter.getInstance().initialize(this, config, b -> Log.i("unimp","onInitFinished----"+b));
        UniAppCenter.getInstance().setDefMenuButtonClickCallBack((s, s1) -> {
            switch (s1){
                case "close":
                    if(uniMapCaches.containsKey(s)) {
                        IUniMP uniMP = uniMapCaches.get(s);
                        if(uniMP != null && uniMP.isRuning()) {//检测获取到的小程序实例是否运行中
                            //uniMP.hideUniMP();
                            uniMP.closeUniMP();
                            Log.d(TAG, "onClick: 关闭小程序");
                        }
                    }
                    break;
                case "gy":
                    Toast.makeText(App.this,"点击了关于",Toast.LENGTH_SHORT).show();
                    break;
            }
        });


        if(!RuningAcitvityUtil.getAppName(getBaseContext()).contains("io.dcloud.unimp")) {
            //请在此处初始化其他三方SDK

        }
    }
}
