package com.android.music_player.application;

import android.app.Application;

import com.android.lib_audio.app.AudioHelper;

public class ImoocVoiceApplication extends Application {
    private static ImoocVoiceApplication mApplication = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication=this;
        //初始化音频sdk
        AudioHelper.init(this); //设置audio子件的全局上下文
    }
    public static ImoocVoiceApplication getInstance() {
        return mApplication;
    }
}
