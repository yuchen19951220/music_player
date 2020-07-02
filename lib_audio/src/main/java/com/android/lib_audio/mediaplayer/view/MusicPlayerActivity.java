package com.android.lib_audio.mediaplayer.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.android.lib_audio.R;
import com.android.lib_audio.mediaplayer.core.AudioController;
import com.android.lib_audio.mediaplayer.core.CustomMediaPlayer;
import com.android.lib_audio.mediaplayer.db.GreenDaoHelper;
import com.android.lib_audio.mediaplayer.events.AudioFavouriteEvent;
import com.android.lib_audio.mediaplayer.events.AudioLoadEvent;
import com.android.lib_audio.mediaplayer.events.AudioPauseEvent;
import com.android.lib_audio.mediaplayer.events.AudioPlayModeEvent;
import com.android.lib_audio.mediaplayer.events.AudioProgressEvent;
import com.android.lib_audio.mediaplayer.events.AudioStartEvent;
import com.android.lib_audio.mediaplayer.model.AudioBean;
import com.android.lib_commin_ui.base.BaseActivity;
import com.android.lib_image_ui.app.ImageLoaderManager;
import com.android.lib_audio.mediaplayer.utils.Utils;
import com.android.lib_share.ShareDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.android.lib_audio.mediaplayer.core.AudioController.PlayMode.LOOP;
import static com.android.lib_audio.mediaplayer.core.AudioController.PlayMode.RANDOM;
import static com.android.lib_audio.mediaplayer.core.AudioController.PlayMode.REPEAT;

/**
 * 播放音乐activity
 */
public class MusicPlayerActivity extends BaseActivity {

    //各种view
    private RelativeLayout mBgView;
    private TextView mInfoView;
    private TextView mAuthorView;

    private ImageView mFavouriteView;

    private SeekBar mProgressView;
    private TextView mStartTimeView;
    private TextView mTotalTimeView;

    private ImageView mPlayModeView;
    private ImageView mPlayView;
    private ImageView mNextView;
    private ImageView mPreViousView;

    private Animator animator;
    /*
    data
     */
    private AudioBean mAudioBean;// 当前播放的歌曲
    private AudioController.PlayMode mPlayMode; //播放模式

    /**
     * 对外提供启动活动的接口
     *
     * @param context
     */
    public static void start(Activity context) {
        Intent intent = new Intent(context, MusicPlayerActivity.class);
        ActivityCompat.startActivity(context,intent, ActivityOptionsCompat.makeSceneTransitionAnimation(context).toBundle());
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //添加转场动画
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setEnterTransition(
                    TransitionInflater.from(this).inflateTransition(R.transition.transition_bottom2top) //从xml文件中解析参数
            );
        }
        EventBus.getDefault().register(this);//注册eventBus
        setContentView(R.layout.activity_music_service_layout);
        initData();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this); //注销evenBus
    }

    private void initData() {
        mAudioBean = AudioController.getInstance().getNowPlaying();
        mPlayMode = AudioController.getInstance().getPlayMode();
    }

    private void initView() {
        mBgView = findViewById(R.id.root_layout); //最外层的背景
        //给背景设置模糊效果 并加载背景图
        ImageLoaderManager.getInstance()
                .displayImageForViewGroup(mBgView, mAudioBean.albumPic);
        //返回键
        findViewById(R.id.back_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed(); //不能直接finish 使用backPresed调用出厂动画
            }
        });

        //标题页面调到歌单页面(未实现)
        findViewById(R.id.title_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //分享页面
        findViewById(R.id.share_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //分享逻辑
                shareMusic(mAudioBean.mUrl, mAudioBean.name);
            }
        });

        //显示歌单
        findViewById(R.id.show_list_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出歌单列表
                MusicListDialog dialog = new MusicListDialog(MusicPlayerActivity.this);
                dialog.show();
            }
        });

        //设置歌曲信息
        mInfoView = findViewById(R.id.album_view);
        mInfoView.setText(mAudioBean.albumInfo);
        mInfoView.requestFocus();//  跑马灯 获得焦点
        mAuthorView = findViewById(R.id.author_view);
        mAuthorView.setText(mAudioBean.author);

        //收藏相关逻辑
        mFavouriteView = findViewById(R.id.favourite_view);
        mFavouriteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //收藏与否
                AudioController.getInstance().changeFavourite();
            }
        });

        changeFavouriteStatus(false); //初始化收藏状态 布置图片
        //歌曲进度相关
        mStartTimeView = findViewById(R.id.start_time_view);
        mTotalTimeView = findViewById(R.id.total_time_view);
        mProgressView = findViewById(R.id.progress_view);
        mProgressView.setProgress(0);
        mProgressView.setEnabled(false);
        //播放模式设置
        mPlayModeView = findViewById(R.id.play_mode_view);
        mPlayModeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换播放模式
                switch (mPlayMode) {
                    case LOOP:
                        //该函数该表状态 并发送通知
                        AudioController.getInstance().setPlayMode(RANDOM);
                        break;
                    case RANDOM:
                        AudioController.getInstance().setPlayMode(REPEAT);
                        break;
                    case REPEAT:
                        AudioController.getInstance().setPlayMode(AudioController.PlayMode.LOOP);
                        break;
                }
            }
        });
        updatePlayModeView();//更新当前播放模式
        //上一首
        mPreViousView = findViewById(R.id.previous_view);
        mPreViousView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioController.getInstance().previous();
            }
        });
        //播放暂停
        mPlayView = findViewById(R.id.play_view);
        mPlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioController.getInstance().playOrPause();
            }
        });
        mNextView = findViewById(R.id.next_view);
        mNextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioController.getInstance().next();
            }
        });
    }

    //订阅的事件

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioLoadEvent(AudioLoadEvent event) {
        //更新notifacation为load状态
        mAudioBean = event.mAudioBean;
        ImageLoaderManager.getInstance().displayImageForViewGroup(mBgView, mAudioBean.albumPic);
        //可以与初始化时的封装一个方法
        mInfoView.setText(mAudioBean.albumInfo);
        mAuthorView.setText(mAudioBean.author);
        changeFavouriteStatus(false);
        mProgressView.setProgress(0);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioFavouriteEvent(AudioFavouriteEvent event) {
        //更新activity收藏状态
        changeFavouriteStatus(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioPlayModeEvent(AudioPlayModeEvent event) {
        mPlayMode = event.mPlayMode;
        //更新播放模式
        updatePlayModeView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioProgessEvent(AudioProgressEvent event) {
        int totalTime = event.maxLength;
        int currentTime = event.progress;
        //更新时间
        mStartTimeView.setText(Utils.formatTime(currentTime));
        mTotalTimeView.setText(Utils.formatTime(totalTime));
        mProgressView.setProgress(currentTime);
        mProgressView.setMax(totalTime);
        if (event.mStatus == CustomMediaPlayer.Status.PAUSED) {
            showPauseView();
        } else {
            showPlayView();
        }
    }





    private void updatePlayModeView() {
        switch (mPlayMode) {
            case LOOP:
                mPlayModeView.setImageResource(R.mipmap.player_loop);
                break;
            case RANDOM:
                mPlayModeView.setImageResource(R.mipmap.player_random);
                break;
            case REPEAT:
                mPlayModeView.setImageResource(R.mipmap.player_once);
                break;
        }
    }

    /**
     * 在初始化和接受到事件时使用
     * @param anim
     */
    private void changeFavouriteStatus ( boolean anim){
        if (GreenDaoHelper.selectFavourite(mAudioBean) != null) {
            mFavouriteView.setImageResource(R.mipmap.audio_aeh);
        } else {
            mFavouriteView.setImageResource(R.mipmap.audio_aef);
        }
        //是否产生动画效果
        if (anim) {
            if (animator != null) animator.end(); //上次动画是否还在
            //x轴上的动画 先放大再缩小(属性动画)
            PropertyValuesHolder animX =
                    PropertyValuesHolder.ofFloat(View.SCALE_X.getName(), 1.0f, 1.2f, 1.0f);
            //y轴上的动画
            PropertyValuesHolder animY =
                    PropertyValuesHolder.ofFloat(View.SCALE_Y.getName(), 1.0f, 1.2f, 1.0f);
            //初始化动画
            animator = ObjectAnimator.ofPropertyValuesHolder(mFavouriteView, animX, animY);
            //设置插值器
            animator.setInterpolator(new AccelerateInterpolator());
            animator.setDuration(300); //动画时间
            animator.start();
        }
    }

    private void showPlayView() {
        mPlayView.setImageResource(R.mipmap.audio_aj6);
    }

    private void showPauseView() {
        mPlayView.setImageResource(R.mipmap.audio_aj7);
    }



    private void shareMusic(String url, String name){
        ShareDialog dialog = new ShareDialog(this, false);
        dialog.setShareType(5);
        dialog.setShareTitle(name);
        dialog.setShareTitleUrl(url);
        dialog.setShareText("慕课网");
        dialog.setShareSite("imooc");
        dialog.setShareSiteUrl("http://www.imooc.com");
        dialog.show();
    }
}