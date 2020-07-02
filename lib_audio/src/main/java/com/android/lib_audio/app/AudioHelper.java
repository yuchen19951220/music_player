package com.android.lib_audio.app;

import android.app.Activity;
import android.content.Context;

import com.android.lib_audio.mediaplayer.core.AudioController;
import com.android.lib_audio.mediaplayer.core.MusicService;
import com.android.lib_audio.mediaplayer.db.GreenDaoHelper;
import com.android.lib_audio.mediaplayer.model.AudioBean;
import com.android.lib_audio.mediaplayer.view.MusicPlayerActivity;

import java.util.ArrayList;

/**
 * 唯一与外界通信的帮助类
 * 外观模式类
 */
public class AudioHelper {

    //SDK全局Context, 供子模块用
    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
        //初始化本地数据库
        GreenDaoHelper.initDatabase();
    }

    //外部启动MusicService方法
    public static void startMusicService(ArrayList<AudioBean> audios) {
        MusicService.startMusicService(audios);
    }
    //外部音乐控制接口
    //播放音乐
    public static void addAudio(Activity activity, AudioBean bean){
        AudioController.getInstance().addAudio(bean);
        MusicPlayerActivity.start(activity);
    }
    //暂停音乐
    public static void pauseAudio() {
        AudioController.getInstance().pause();
    }
    //回复播放
    public static void resumeAudio() {
        AudioController.getInstance().resume();
    }


    //获取全局context
    public static Context getContext() {
        return mContext;
    }

}
