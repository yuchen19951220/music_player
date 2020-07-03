package com.android.lib_video.videoplayer.core;

import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.android.lib_base.audio.AudioService;
import com.android.lib_video.videoplayer.VideoContextInterface;

/**
 * 与外界进行通信
 */
public class VideoAdContext implements VideoAdSlot.SDKSlotListener {

    private ViewGroup mParentView;

    private VideoAdSlot mAdSlot;

    private String mInstance; //视屏地址

    private VideoContextInterface mListener; //与外界交互的listener




    public VideoAdContext(ViewGroup parentView, String instance){
        this.mParentView=parentView;
        this.mInstance=instance;
        load();
    }

    /**
     * 初始化 ad 创建viedeoView
     */
    private void load() {
        if (mInstance!=null){
            mAdSlot=new VideoAdSlot(mInstance,this);
        } else {
            mAdSlot=new VideoAdSlot(null,this);
            if (mListener!=null) {
                mListener.onVideoComplete();
            }
        }
    }

    /**
     *
     * release the ad
     */
    public void destory(){
        mAdSlot.destroy();
    }

    //设置监听
    public void setAdResultListener(VideoContextInterface listener){
        this.mListener=listener;
    }

    @Override
    public ViewGroup getAdParent() {
        return mParentView;
    }

    @Override
    public void onVideoLoadSuccess() {
        //回调到上一层
        if (mListener != null) {
            mListener.onVideoSuccess();
        }
    }

    @Override
    public void onVideoFailed() {
        //回调到上一层
        if (mListener != null) {
            mListener.onVideoFailed();
        }
    }

    @Override
    public void onVideoComplete() {
        //回调到上一层
        if (mListener != null) {
            mListener.onVideoComplete();
        }
    }

}
