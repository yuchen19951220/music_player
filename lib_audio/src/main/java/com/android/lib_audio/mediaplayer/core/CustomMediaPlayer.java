package com.android.lib_audio.mediaplayer.core;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * 带状态的MediaPlayer
 * 获取准确的状态
 */
public class CustomMediaPlayer extends MediaPlayer implements MediaPlayer.OnCompletionListener {

    //当前状态
    private Status mState;


    //定义media状态的枚举变量
    public enum Status {
        IDLE, INITIALIZED, STARTED, PAUSED, STOPPED, COMPLETED
    }


    private OnCompletionListener mOnCompletionListener;

    public CustomMediaPlayer() {
        super();
        mState = Status.IDLE; //初始化空闲状态
        //将播放器(父类)注册到到监听器（子类）
        super.setOnCompletionListener(this);
    }

    @Override
    public void reset() {
        super.reset();
        mState=Status.IDLE;
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        super.setDataSource(path);
        mState = Status.INITIALIZED;
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        mState = Status.STARTED;
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        mState=Status.PAUSED;
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        mState=Status.STOPPED;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mState=Status.COMPLETED;
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(mediaPlayer);
        }
    }

   public boolean isComplete(){
        return mState==Status.COMPLETED;
    }
    //获取状态
    public Status getState(){
        return mState;
    }

    public void setCompleteListener(OnCompletionListener listener){
        mOnCompletionListener=listener;
    }


}
