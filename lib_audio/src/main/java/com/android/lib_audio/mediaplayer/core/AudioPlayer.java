package com.android.lib_audio.mediaplayer.core;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Looper;

import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.android.lib_audio.app.AudioHelper;
import com.android.lib_audio.mediaplayer.events.AudioCompleteEvent;
import com.android.lib_audio.mediaplayer.events.AudioErrorEvent;
import com.android.lib_audio.mediaplayer.events.AudioLoadEvent;
import com.android.lib_audio.mediaplayer.events.AudioPauseEvent;
import com.android.lib_audio.mediaplayer.events.AudioProgressEvent;
import com.android.lib_audio.mediaplayer.events.AudioReleaseEvent;
import com.android.lib_audio.mediaplayer.events.AudioStartEvent;
import com.android.lib_audio.mediaplayer.model.AudioBean;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

/***
 * 1、播放 音频
 * 2、对外发送各种类型事件---处理各种回调方法
 *
 * */
//通过实现接口监听事件
public class AudioPlayer implements MediaPlayer.OnCompletionListener
                    ,MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener
                    ,MediaPlayer.OnErrorListener,AudioFocusManager.AudioFocusListener {

    private static final String TAG = "AudioPlayer";

    private static final int TIME_MSG = 0x01;
    private static final int TIME_INVAL = 100;

    //真正负责音频的播放
    private  CustomMediaPlayer mMediaPlayer;
    //增强后台保活能力
    private WifiManager.WifiLock mWifiLock;


    private boolean isPausedByFocusLossTransient;//是否因为失去焦点而停止

    //音频焦点监听器
    private AudioFocusManager mAudioFocusManager;
    //定义在主线程中的handler
    private Handler mHandle=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TIME_MSG:
                    //暂停也要更新进度 防止UI不同步 进度一直一样
                    if (getStatus()==CustomMediaPlayer.Status.STARTED||getStatus()==CustomMediaPlayer.Status.PAUSED){
                        //发送进度更新事件
                        EventBus.getDefault()
                                .post(new AudioProgressEvent(getStatus(),getCurrentPosition(),getDuration()));
                        sendEmptyMessageDelayed(TIME_MSG,TIME_INVAL);//延迟发送空消息1  0.1s更新一次进度
                    }
                    break;
            }
        }
    };

    public AudioPlayer() {
        init();
    }

    /*
    回调方法
     */

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        //缓存进度回调
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //播放完毕回调 发送事件
        EventBus.getDefault().post(new AudioCompleteEvent());
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        //为true 不再回调onCompletion方法 自行处理异常
        //播放出错回调
        EventBus.getDefault().post(new AudioErrorEvent());
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //准备完毕 进入播放状态
        start();
    }


    /**
     * audio focus相关的回调方法
     */
    @Override
    public void audioFocusGrant() {
        //再次获取的焦点
        setVolumn(1.0f, 1.0f);//设置音量
        if (isPausedByFocusLossTransient){
            resume(); //因为失去焦点暂停 重新获得焦点恢复
        }
        isPausedByFocusLossTransient=false;
    }

    @Override
    public void audioFocusLoss() {
        //永久失去焦点
        if (mMediaPlayer != null)  pause();
    }

    @Override
    public void audioFocusLossTransient() {
        //短暂失去焦点，暂停
        if (mMediaPlayer != null) pause();
        isPausedByFocusLossTransient = true;
    }

    @Override
    public void audioFocusLossDuck() {
        //瞬间失去焦点,
        setVolumn(0.5f, 0.5f);//降低音量 不影响其他应用
    }





    //初始化
    private void init() {
        //变量初始化
        mMediaPlayer=new CustomMediaPlayer();
        mMediaPlayer.setWakeMode(AudioHelper.getContext(), PowerManager.PARTIAL_WAKE_LOCK); //保证电量低的时候也能够播放
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);//数据流类型
        //注册监听器
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnErrorListener(this);
        //初始话wifilock类
        mWifiLock= ((WifiManager)AudioHelper.getContext().getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL,TAG);
        //初始话AudioManager
        mAudioFocusManager=new AudioFocusManager(AudioHelper.getContext(),this);
    }

    /**
     * 获取播放器状态
     */
    //获取播放器状态
    public CustomMediaPlayer.Status getStatus() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getState();
        } else {
            return CustomMediaPlayer.Status.STOPPED;
        }
    }


    /***
     * 实现播放相关的方法
     * 对外提供的加载
     * @param audioBean
     */
    public void load(AudioBean audioBean){
        try {
            mMediaPlayer.reset();//清空上次数据 重启
            mMediaPlayer.setDataSource(audioBean.mUrl);//设置播放源
            mMediaPlayer.prepareAsync(); //进入异步准备状态 （在另一个线程,准备完成后回调onprepare方法（这个方法中start））
            //对外发送load事件
            EventBus.getDefault().post(new AudioLoadEvent(audioBean));
        } catch (IOException e) {
            //对外发送error事件
            EventBus.getDefault().post(new AudioErrorEvent());
        }
    }


    /***
     * 对外提供暂停
     */
    public void pause(){
        if (getStatus()==CustomMediaPlayer.Status.STARTED){
            mMediaPlayer.pause();
            if (mWifiLock.isHeld()){
                mWifiLock.release();//释放wifi锁
            }
            if (mAudioFocusManager!=null){
                mAudioFocusManager.abandonAudioFocus();//释放音频焦点 让其他应用程序能正常运行
            }

            //停止发送暂停事件
            EventBus.getDefault().post(new AudioPauseEvent());
        }
    }

    /**
     * 对外提供恢复
     */
    public void resume(){
        if (getStatus() == CustomMediaPlayer.Status.PAUSED) {
            start();
        }
    }

    /***
     * 销毁唯一的mediaplayer对象 只有在退出app时使用 释放资源
     */
    public void release(){
        if (mMediaPlayer==null){
            return;
        }
        mMediaPlayer.release();
        mMediaPlayer=null;
        //释放wifi锁
        if (mWifiLock.isHeld()){
            mWifiLock.release();
        }
        mWifiLock = null;
        //释放音频焦点 让其他应用程序能正常运行
        if (mAudioFocusManager!=null){
            mAudioFocusManager.abandonAudioFocus();
        }
        mAudioFocusManager = null;
        //停止发送handler消息 停止进度更新
        mHandle.removeCallbacksAndMessages(null);

        //发送销毁播放器事件,清除通知等
        EventBus.getDefault().post(new AudioReleaseEvent());

    }


    /**
     * 获取当前音乐总时长,更新进度用
     */
    public int getDuration() {
        if (getStatus() == CustomMediaPlayer.Status.STARTED
                || getStatus() == CustomMediaPlayer.Status.PAUSED) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentPosition() {
        if (getStatus() == CustomMediaPlayer.Status.STARTED
                || getStatus() == CustomMediaPlayer.Status.PAUSED) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }





    //内部开始播放
    private void start(){
        //是否有焦点
        if (!mAudioFocusManager.requestAudioFocus()){
            Log.e(TAG, "获取音频焦点失败" );
        }
        mMediaPlayer.start(); //开始播放
        mWifiLock.acquire();//保证稳定 获取wifi锁
        //更新进度 向主线程的handler发送消息
        mHandle.sendEmptyMessage(TIME_MSG);

        //对外发送start事件
        EventBus.getDefault().post(new AudioStartEvent());
    }

    //设置音量
    private void setVolumn(float leftVol, float rightVol){
        if (mMediaPlayer != null) mMediaPlayer.setVolume(leftVol, rightVol);
    }



}
