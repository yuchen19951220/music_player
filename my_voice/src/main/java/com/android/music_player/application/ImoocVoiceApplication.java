package com.android.music_player.application;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;
import com.android.lib_audio.app.AudioHelper;
import com.android.lib_share.ShareManager;

public class ImoocVoiceApplication extends Application {
    private static ImoocVoiceApplication mApplication = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication=this;
        //初始化音频sdk
        AudioHelper.init(this); //设置audio子件的全局上下文

        //分享组件初始化
        ShareManager.initSDK(this);

        //注册
        ARouter.init(this);
    }
    public static ImoocVoiceApplication getInstance() {
        return mApplication;
    }
}
