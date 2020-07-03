package com.android.lib_video.videoplayer.core;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.launcher.ARouter;
import com.android.lib_base.audio.AudioService;
import com.android.lib_video.videoplayer.core.view.CustomVideoView;
import com.android.lib_video.videoplayer.core.view.VideoFullDialog;
import com.android.lib_video.videoplayer.utils.Utils;

/**
 * 视频业务逻辑层
 * 实现接口 使得底层的事件能通知到VideoAdSlot
 */
public class VideoAdSlot implements CustomVideoView.ADVideoPlayerListener {

    private Context mContext;

    /**
     * UI
     */
    private CustomVideoView mVideoView;
    private ViewGroup mParentView; //视图容器
//
//    @Autowired(name = "/audio/audio_service")
//    protected AudioService mAudioService;
    /**
     * Data
     */
    @Autowired(name = "/audio/audio_service")
    protected AudioService mAudioService;
    private String mXAdInstance; //要播放的实例地址url
    //与context层的事件回调
    private SDKSlotListener mSlotListener;

    public VideoAdSlot(String adInstance, SDKSlotListener slotLitener) {
        ARouter.getInstance().inject(this); //注册arout
        mXAdInstance = adInstance;
        mSlotListener = slotLitener;
        mParentView = slotLitener.getAdParent();
        mContext = mParentView.getContext();
        initVideoView();
    }

    private void initVideoView() {
        mVideoView = new CustomVideoView(mContext);
        if (mXAdInstance != null) {
            mVideoView.setDataSource(mXAdInstance);
            mVideoView.setListener(this);
        }
        RelativeLayout paddingView = new RelativeLayout(mContext);
        paddingView.setBackgroundColor(mContext.getResources().getColor(android.R.color.black));
        paddingView.setLayoutParams(mVideoView.getLayoutParams());
        mParentView.addView(paddingView);
        mParentView.addView(mVideoView); //将videoview 添加到容器中
    }

    public void destroy() {
        mVideoView.destroy();
        mVideoView = null;
        mContext = null;
        mXAdInstance = null;
    }

    /**
     * 实现play层接口
     */
    @Override
    public void onClickFullScreenBtn() {
        //获取videoview在当前界面的属性
        Bundle bundle = Utils.getViewProperty(mParentView);
        mParentView.removeView(mVideoView); //从容器中移除 --一个view只有一个父容器
        //创建全屏
        VideoFullDialog dialog =
                new VideoFullDialog(mContext, mVideoView, mXAdInstance, mVideoView.getCurrentPosition());
        dialog.setListener(new VideoFullDialog.FullToSmallListener() {
            @Override
            //全屏到小屏
            public void getCurrentPlayPosition(int position) {
                backToSmallMode(position);
            }

            @Override
            //全屏播放完毕到小屏
            public void playComplete() {
                bigPlayComplete();
            }
        });
        dialog.setViewBundle(bundle); //为Dialog设置播放器数据Bundle对象
        dialog.setSlotListener(mSlotListener);
        dialog.show();
        //全屏暂停音乐播放
        mAudioService.pauseAudio();
    }

    //全屏回到小屏
    private void backToSmallMode(int position) {
        if (mVideoView.getParent() == null) {
            mParentView.addView(mVideoView);
        }
        mVideoView.setTranslationY(0); //防止动画导致偏离父容器
        mVideoView.isShowFullBtn(true);
        mVideoView.mute(true);
        mVideoView.setListener(this);
        mVideoView.seekAndResume(position); //调到指定位置继续播放
        //小屏恢复音乐播放
        mAudioService.resumeAudio();
    }

    //全屏播放完毕回到小屏事件
    private void bigPlayComplete() {
        if (mVideoView.getParent() == null) {
            mParentView.addView(mVideoView);
        }
        mVideoView.setTranslationY(0); //防止动画导致偏离父容器
        mVideoView.isShowFullBtn(true);
        mVideoView.mute(true);
        mVideoView.setListener(this);
        mVideoView.seekAndPause(0); //播放完毕进入暂停状态
    }

    @Override
    public void onClickVideo() {
    }

    @Override
    public void onClickBackBtn() {
    }

    @Override
    public void onClickPlay() {
    }

    @Override
    public void onAdVideoLoadSuccess() {
        if (mSlotListener != null) {
            mSlotListener.onVideoLoadSuccess();
        }
    }

    @Override
    public void onAdVideoLoadFailed() {
        if (mSlotListener != null) {
            mSlotListener.onVideoFailed();
        }
    }

    @Override
    public void onAdVideoLoadComplete() {
        if (mSlotListener != null) {
            mSlotListener.onVideoComplete();
        }
        mVideoView.setIsRealPause(true);
    }

    @Override
    public void onBufferUpdate(int time) {

    }

    //传递消息到appcontext层
    public interface SDKSlotListener {

        ViewGroup getAdParent();

        void onVideoLoadSuccess();

        void onVideoFailed();

        void onVideoComplete();
    }

}
