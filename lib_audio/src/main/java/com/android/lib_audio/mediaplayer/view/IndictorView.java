package com.android.lib_audio.mediaplayer.view;

import android.animation.Animator;
import android.content.Context;

import androidx.viewpager.widget.ViewPager;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.lib_audio.R;
import com.android.lib_audio.mediaplayer.core.AudioController;
import com.android.lib_audio.mediaplayer.events.AudioLoadEvent;
import com.android.lib_audio.mediaplayer.events.AudioPauseEvent;
import com.android.lib_audio.mediaplayer.events.AudioStartEvent;
import com.android.lib_audio.mediaplayer.model.AudioBean;
import com.android.lib_audio.mediaplayer.view.adapter.MusicPagerAdapter;

import java.util.ArrayList;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 音乐播放页面唱针布局
 * 监听ViewPage的滑动状态
 */
public class IndictorView extends RelativeLayout implements ViewPager.OnPageChangeListener {
    private Context mContext;

    /*
     * view相关
     */
    private ImageView mImageView; //唱针
    private ViewPager mViewPager; //可切换的ViewPager
    private MusicPagerAdapter mMusicPagerAdapter;

    /*
     * data
     */
    private AudioBean mAudioBean; //当前播放歌曲
    private ArrayList<AudioBean> mQueue; //播放队列 用于填充viewPager


    public IndictorView(Context context) {
        this(context, null);
    }

    public IndictorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    //初始化view
    public IndictorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        EventBus.getDefault().register(this); //注册eventBus
        initData(); //初始化数据
    }

    /**
     * 结束视图文件加载后初始化view
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }


    //监听相关
    @Override
    //滑动 不用实现
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    //页面被选中后
    public void onPageSelected(int position) {
        //指定要播放的postion
        AudioController.getInstance().setPlayIndex(position);
    }

    @Override
    //改变视图
    public void onPageScrollStateChanged(int state) {
        switch (state){
            case ViewPager.SCROLL_STATE_IDLE:
                //滑动结束
                showPlayView();
                break;
            case ViewPager.SCROLL_STATE_DRAGGING:
                //滑动过程中 需要暂停动画
                showPauseView();
                break;
            case ViewPager.SCROLL_STATE_SETTLING:
                break;
        }
    }

    //订阅事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioLoadEvent(AudioLoadEvent event) {
        //更新viewpager为load状态
        mAudioBean = event.mAudioBean;
        showLoadView(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioPauseEvent(AudioPauseEvent event) {
        //更新activity为暂停状态
        showPauseView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioStartEvent(AudioStartEvent event) {
        //更新activity为播放状态
        showPlayView();
    }







    private void initView() {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.indictor_view, this);
        mImageView = rootView.findViewById(R.id.tip_view);
        mViewPager = rootView.findViewById(R.id.view_pager);
        mViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mMusicPagerAdapter = new MusicPagerAdapter(mQueue,mContext,null);
        mViewPager.setAdapter(mMusicPagerAdapter);
        showLoadView(false);
        //要在UI初始化完，否则会多一次listener响应
        mViewPager.addOnPageChangeListener(this);

    }

    private void showLoadView(boolean isSmooth) {
        mViewPager.setCurrentItem(mQueue.indexOf(mAudioBean),isSmooth); //平滑过渡
    }

    private void showPlayView(){
        Animator anim=mMusicPagerAdapter.getAnim(mViewPager.getCurrentItem());
        if (anim!=null){
            if (anim.isPaused()){
                anim.resume();
            } else {
                anim.start();
            }
        }
    }

    private void showPauseView(){
        Animator anim=mMusicPagerAdapter.getAnim(mViewPager.getCurrentItem());
        if (anim!=null) anim.pause();
    }


    private void initData() {
        mQueue=AudioController.getInstance().getQueue();
        mAudioBean=AudioController.getInstance().getNowPlaying();
    }



}
