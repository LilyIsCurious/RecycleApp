package com.example.myrecycle;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.llw.mvplibrary.BaseApplication;
import com.llw.mvplibrary.network.NetworkApi;

/**
 * 自定义Application
 */
public class TrashApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化网络框架 Initialize network framework
        NetworkApi.init(new NetworkRequiredInfo(this));

        //配置讯飞语音SDK Configure iFLYTEK voice SDK
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=c997c34f");
    }
}
