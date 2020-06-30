package com.android.lib_audio.mediaplayer.core;


import android.util.Log;

import com.android.lib_audio.mediaplayer.db.GreenDaoHelper;
import com.android.lib_audio.mediaplayer.events.AudioCompleteEvent;
import com.android.lib_audio.mediaplayer.events.AudioErrorEvent;
import com.android.lib_audio.mediaplayer.events.AudioFavouriteEvent;
import com.android.lib_audio.mediaplayer.events.AudioPlayModeEvent;
import com.android.lib_audio.mediaplayer.exception.AudioQueueEmptyException;
import com.android.lib_audio.mediaplayer.model.AudioBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Random;

import static android.content.ContentValues.TAG;

/***
 * 实现具体的播放逻辑
 * 控制播放逻辑类，注意添加一些控制方法时，要考虑是否需要增加Event,来更新UI
 */
public class AudioController {

    private static final String TAG = "AudioController";


    /**
     * 播放方式
     */
    public enum PlayMode {
        /**
         * 列表循环
         */
        LOOP,
        /**
         * 随机
         */
        RANDOM,
        /**
         * 单曲循环
         */
        REPEAT
    }
    //核心音频播放器
    private AudioPlayer mAudioPlayer;
    //播放列表
    private ArrayList<AudioBean> mQueue = new ArrayList<>();
    //当前播放索引
    private int mQueueIndex = 0;
    //播放模式
    private PlayMode mPlayMode = PlayMode.LOOP;



    private AudioController(){
        EventBus.getDefault().register(this);
        mAudioPlayer = new AudioPlayer();
    }

    //添加歌曲到播放列表
    private void addCustomAudio(int index, AudioBean bean) {
        if (mQueue == null) {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
            mQueue.add(index, bean);
    }

    //查询歌曲在列表的位置
    private int queryAudio(AudioBean bean){
        return mQueue.indexOf(bean);
    }


    /*
    加载歌曲
 */
    private void load(AudioBean bean) {
        mAudioPlayer.load(bean);
    }


    /*
     * 获取播放器当前状态
     */
    private CustomMediaPlayer.Status getStatus() {
        return mAudioPlayer.getStatus();
    }


    //获取上一首歌曲
    private AudioBean getPreviousPlaying() {
        //不同播放模式
        switch (mPlayMode){
            case LOOP:
                mQueueIndex=(mQueueIndex+mQueue.size()-1)%mQueue.size();
                return getPlaying(mQueueIndex);
            case RANDOM:
                mQueueIndex=new Random().nextInt(mQueue.size())%mQueue.size();
                return getPlaying(mQueueIndex);
            case REPEAT:
                return getPlaying(mQueueIndex);
        }
        return null;
    }

    //获取下一首
    private AudioBean getNextPlaying(){
        //不同播放模式
        switch (mPlayMode){
            case LOOP:
                mQueueIndex=(mQueueIndex+1)%mQueue.size();
                return getPlaying(mQueueIndex);
            case RANDOM:
                mQueueIndex=new Random().nextInt(mQueue.size())%mQueue.size();
                return getPlaying(mQueueIndex);
            case REPEAT:
                return getPlaying(mQueueIndex);
        }
        return null;
    }

    /***
     * 单例方法
     * @return
     */
    public static AudioController getInstance() {
        return AudioController.SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static AudioController instance = new AudioController();
    }



    /**
     * 对外提供是否播放中状态
     */
    public boolean isStartState() {
        return CustomMediaPlayer.Status.STARTED == getStatus();
    }

    /**
     * 对外提提供是否暂停状态
     */
    public boolean isPauseState() {
        return CustomMediaPlayer.Status.PAUSED == getStatus();
    }


    public void changeFavourite(){
        if (GreenDaoHelper.selectFavourite(getNowPlaying())!=null){
            //取消收藏
            GreenDaoHelper.removeFavourite(getNowPlaying());
            EventBus.getDefault().post(new AudioFavouriteEvent(false));
        } else {
            GreenDaoHelper.addFavourite(getNowPlaying());
            EventBus.getDefault().post(new AudioFavouriteEvent(true));
        }
    }


    public void setQueue(ArrayList<AudioBean> queue){
        this.setQueue(queue,0);
        Log.d(TAG, "setQueue: ");
    }

    /**
     * 加入列表到播放列表 并指定当前播放索引
     * @param queue
     * @param queueIndex
     */
    public void setQueue(ArrayList<AudioBean> queue,int queueIndex){
        mQueue.addAll(queue);
        mQueueIndex=queueIndex;
    }


    /**
     * 添加一首歌曲
     * @return
     */
    public void addAudio(AudioBean bean){
        addAudio(0,bean);
    }

    public void addAudio(int index, AudioBean bean){
        if (mQueue == null) {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
        //查看是否存在该歌曲
        int query = queryAudio(bean);
        if (query <= -1){
            //没添加过此id的歌曲，添加且直接播放
            addCustomAudio(index, bean);
            setPlayIndex(index);
        } else {
            //若当前播放歌曲不是此歌曲
            if (!getNowPlaying().id.equals(bean.id)) {
                //添加过且不是当前播放，播，否则什么也不干
                setPlayIndex(query);
            }
        }

    }


    public void setPlayIndex(int index) {
        if (mQueue == null) {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
        mQueueIndex = index;
        play();
    }



    /*播放模式相关方法*/

    public PlayMode getPlayMode() {
        return mPlayMode;
    }


    /*与当前播放项相关方法*/
    public void setPlayMode(PlayMode playMode) {
        mPlayMode = playMode;
        //还要对外发送切换事件，更新UI
        EventBus.getDefault().post(new AudioPlayModeEvent(mPlayMode));
    }

    public int getQueueIndex() {
        return mQueueIndex;
    }



    //获取当前播放的歌曲bean
    private AudioBean getPlaying(int index) {
        if (mQueue != null && !mQueue.isEmpty() && index >= 0 && index < mQueue.size()) {
            return mQueue.get(index);
        } else {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
    }



    /*播放列表相关方法*/
    public ArrayList<AudioBean> getQueue(){
        return mQueue==null? new ArrayList<AudioBean>():mQueue;
    }



    /*操作播放器相关方法*/
    /**
     * 加载当前index歌曲
     */
    public void play() {
        //获取要播放的歌曲
        AudioBean bean = getPlaying(mQueueIndex);
        //加载歌曲
        load(bean);
    }

    /**
     * 加载next index歌曲
     */
    public void next() {
        AudioBean bean = getNextPlaying();
        load(bean);
    }

    /**
     * 加载previous index歌曲
     */
    public void previous() {
        AudioBean bean = getPreviousPlaying();
        load(bean);
    }

    public void resume() {
        mAudioPlayer.resume();
    }

    public void pause() {
        mAudioPlayer.pause();
    }

    public void release() {
        mAudioPlayer.release();
        EventBus.getDefault().unregister(this);
    }

    /***
     * 播放/暂停切换
     *
     */
    public void playOrPause(){
        if (isStartState()) {
            pause();
        } else if (isPauseState()) {
            resume();
        }
    }


    /**
     * 对外提供的获取当前歌曲信息
     */
    public AudioBean getNowPlaying() {
        return getPlaying(mQueueIndex);
    }



    //插放完毕事件处理
    @Subscribe(threadMode = ThreadMode.MAIN) public void onAudioCompleteEvent(
            AudioCompleteEvent event) {
        next();
    }

    //播放出错事件处理
    @Subscribe(threadMode = ThreadMode.MAIN) public void onAudioErrorEvent(AudioErrorEvent event) {
        next();
    }

}


