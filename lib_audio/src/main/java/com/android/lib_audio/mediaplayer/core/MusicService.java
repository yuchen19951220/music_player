package com.android.lib_audio.mediaplayer.core;


import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.android.lib_audio.app.AudioHelper;
import com.android.lib_audio.mediaplayer.events.AudioFavouriteEvent;
import com.android.lib_audio.mediaplayer.events.AudioLoadEvent;
import com.android.lib_audio.mediaplayer.events.AudioPauseEvent;
import com.android.lib_audio.mediaplayer.events.AudioStartEvent;
import com.android.lib_audio.mediaplayer.model.AudioBean;
import com.android.lib_audio.mediaplayer.view.NotificationHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static com.android.lib_audio.mediaplayer.view.NotificationHelper.NOTIFICATION_ID;

/***
 * 音乐后台服务，接收事件并更新notification状态
 * 启动为前台service 不会被回收
 *
 * 内部广播类 接收广播 处理广播
 */
public class MusicService extends Service implements NotificationHelper.NotificationHelperListener {

    private static String DATA_AUDIOS = "AUDIOS";
    //actions
    private static String ACTION_START = "ACTION_START";
    //播放队列
    private ArrayList<AudioBean> mAudioBeans;

    //广播接收器
    private NotificationReceiver mReceiver;

    /***
     * 外部启动服务的方法
     * @param audioBeans 播放列表
     */
    public static void startMusicService(ArrayList<AudioBean> audioBeans){
        Intent intent=new Intent(AudioHelper.getContext(),MusicService.class);
        intent.setAction(ACTION_START); //设置action
        intent.putExtra(DATA_AUDIOS,audioBeans);//传进list数据
        AudioHelper.getContext().startService(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this); //注册eventBus
        registerBroadcastReceiver(); //注册广播
    }

    //服务被创建启动 ---播放音乐 船舰notification
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mAudioBeans=(ArrayList<AudioBean>) intent.getSerializableExtra(DATA_AUDIOS); //从intent中获取播放列表
        if (ACTION_START.equals(intent.getAction())){
            //开始播放
            playMusic();
            //初始化前台Notification 并设置回调
            NotificationHelper.getInstance().init(this);

        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void playMusic(){
        AudioController.getInstance().setQueue(mAudioBeans);
        AudioController.getInstance().play();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this); //注销eventBus
        unRegisterBroadcastReceiver(); //注销广播接收器
    }

    //注册和反注册Receiver
    private void registerBroadcastReceiver(){
        if (mReceiver==null){
            mReceiver=new NotificationReceiver();
            IntentFilter filter =new IntentFilter();
            filter.addAction(NotificationReceiver.ACTION_STATUS_BAR); //过滤器设置为通知广播
            registerReceiver(mReceiver,filter);
        }
    }

    private void unRegisterBroadcastReceiver(){
        if (mReceiver!=null){
            unregisterReceiver(mReceiver);
        }
    }



    //监听器相关的响应函数 完成notification初始化后执行
    @Override
    public void onNotificationInit() {
        //Notification绑定前台服务
        startForeground(NOTIFICATION_ID,NotificationHelper.getInstance().getNotification());
    }

    /*
        eventBus订阅的服务
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioLoadEvent(AudioLoadEvent event){
        //更新notification状态为加载
        NotificationHelper.getInstance().showLoadStatus(event.mAudioBean);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioStartEvent(AudioStartEvent event){
        //更新notification状态为开始
        NotificationHelper.getInstance().showPlayStatus();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioPauseEvent(AudioPauseEvent event){
        //更新notification状态为暂停
        NotificationHelper.getInstance().showPauseStatus();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioFavouriteEvent(AudioFavouriteEvent event){
        //更新notification状态为暂停
        NotificationHelper.getInstance().changeFavouriteStatus(event.isFavourite);
    }

    /**
     * 接收Notification发送的广播 并作出处理
     */
    public static class NotificationReceiver extends BroadcastReceiver {
        public static final String ACTION_STATUS_BAR =
                AudioHelper.getContext().getPackageName() + ".NOTIFICATION_ACTIONS";
        public static final String EXTRA = "extra";
        public static final String EXTRA_PLAY = "play_pause";
        public static final String EXTRA_NEXT = "play_next";
        public static final String EXTRA_PRE = "play_previous";
        public static final String EXTRA_FAV = "play_favourite";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || TextUtils.isEmpty(intent.getAction())) {
                return;
            }
            String extra = intent.getStringExtra(EXTRA);
            switch (extra) {
                case EXTRA_PLAY:
                    //处理播放暂停事件,可以封到AudioController中
                    AudioController.getInstance().playOrPause();
                    break;
                case EXTRA_PRE:
                    AudioController.getInstance().previous(); //不管当前状态，直接播放
                    break;
                case EXTRA_NEXT:
                    AudioController.getInstance().next();
                    break;
                case EXTRA_FAV:
                    //收藏的广播处理
                    AudioController.getInstance().changeFavourite();
                    break;
            }
        }
    }

}
