package com.android.lib_audio.mediaplayer.view;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.android.lib_audio.R;
import com.android.lib_audio.app.AudioHelper;
import com.android.lib_audio.mediaplayer.core.AudioController;
import com.android.lib_audio.mediaplayer.core.MusicService;
import com.android.lib_audio.mediaplayer.db.GreenDaoHelper;
import com.android.lib_audio.mediaplayer.model.AudioBean;
import com.android.lib_image_ui.app.ImageLoaderManager;

/**
 *音乐Notification帮助类
 * 1、完成Notification的初始化
 * 2、对外提供更新notification的方法
 *
 */
public class NotificationHelper {

    public static final String CHANNEL_ID = "channel_id_audio"; //notification channel id
    public static final String CHANNEL_NAME = "channel_name_audio";//notification channel name
    public static final int NOTIFICATION_ID = 0x111;  //唯一区别notification的id

    /***
     * UI相关
     */
    private Notification mNotification;
    private RemoteViews mRemoteViews;//大布局
    private RemoteViews mSmallRemoteViews; //小布局
    private NotificationManager mNotificationManager;  //系统服务

    /***
     * data
     */
    private NotificationHelperListener mListener;//与service通信的回调接口
    private String packageName;//包名
    private AudioBean mAudioBean;// 当前播放实体

    /**
     * 单例实现NotificationHelper
     */
    public static NotificationHelper getInstance(){ return SingletonHolder.instance;}

    private static class SingletonHolder{
        private static NotificationHelper instance=new NotificationHelper();
    }
    private NotificationHelper(){
    }

    //初始化并创建notification 并设置监听
    public void init(NotificationHelperListener listener){
        mNotificationManager = (NotificationManager) AudioHelper.getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        packageName = AudioHelper.getContext().getPackageName();
        mAudioBean = AudioController.getInstance().getNowPlaying();
        initNotification();
        mListener = listener;
        //notification初始化完成后 使用回调函数执行回调方法
        if (mListener != null) mListener.onNotificationInit();
    }

    /**
     * 创建Notification
     */
    private void initNotification(){
        if (mNotification==null){
            //创建布局
            initRemoteView();
            //创建Notification


        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel channel=new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(false);
            channel.enableVibration(false);
            mNotificationManager.createNotificationChannel(channel);
        }
        //设置notification的跳转时间
        Intent intent=new Intent(AudioHelper.getContext(),MusicPlayerActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(AudioHelper.getContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder=new NotificationCompat.Builder(AudioHelper.getContext(),CHANNEL_ID)
                    .setContentIntent(pendingIntent) //跳转到music
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setCustomBigContentView(mRemoteViews) //大布局
                    .setContent(mSmallRemoteViews);//正常布局 两个布局切换
            mNotification=builder.build();//创建Notification

            //通知初始化后显示load状态
            showLoadStatus(mAudioBean);
        }
    }

    /***
     * 创建Notification的布局，默认布局为loading状态
     */
    private void initRemoteView() {
        int layoutId=R.layout.notification_big_layout;//layout布局id
        //由于跨进程 必须使用remoteView显示notification视图
        mRemoteViews=new RemoteViews(packageName,layoutId);
        //使用remoteView的特有方法设置 由于是跨进程不能直接通过findbyid获得
        mRemoteViews.setTextViewText(R.id.title_view,mAudioBean.name);
        mRemoteViews.setTextViewText(R.id.tip_view,mAudioBean.album);
        //判断当前歌曲是否为收藏歌曲
        if (GreenDaoHelper.selectFavourite(mAudioBean)!=null){
            mRemoteViews.setImageViewResource(R.id.favourite_view,R.mipmap.note_btn_loved);
        } else {
            mRemoteViews.setImageViewResource(R.id.favourite_view,R.mipmap.note_btn_love_white);
        }


        int smalllayoutId=R.layout.notification_small_layout;
        mSmallRemoteViews=new RemoteViews(packageName,smalllayoutId);
        mSmallRemoteViews.setTextViewText(R.id.title_view,mAudioBean.name);
        mSmallRemoteViews.setTextViewText(R.id.tip_view,mAudioBean.album);

        //点击播放按钮发送广播 主进程接受到后进行音频操作
        Intent playIntent=new Intent(MusicService.NotificationReceiver.ACTION_STATUS_BAR);
        playIntent.putExtra(MusicService.NotificationReceiver.EXTRA,
                MusicService.NotificationReceiver.EXTRA_PLAY); //设置跳转携带内容
        //使用PendingIntent包装 Intent 这样notification可以发送
        PendingIntent playPendingIntent=PendingIntent.getBroadcast(AudioHelper.getContext(),1,playIntent
                        ,PendingIntent.FLAG_UPDATE_CURRENT);
        //设置mRemoteView控件的点击事件
        mRemoteViews.setOnClickPendingIntent(R.id.play_view,playPendingIntent);
        //改变图标
        mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
        mSmallRemoteViews.setOnClickPendingIntent(R.id.play_view,playPendingIntent);
        //改变图标
        mSmallRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);

        //点击上一首发送的广播
        Intent previousIntent=new Intent(MusicService.NotificationReceiver.ACTION_STATUS_BAR);
        previousIntent.putExtra(MusicService.NotificationReceiver.EXTRA,
                MusicService.NotificationReceiver.EXTRA_PRE); //设置跳转携带内容
        //使用PendingIntent包装 Intent 这样notification可以发送
        PendingIntent previousPendingIntent=PendingIntent.getBroadcast(AudioHelper.getContext(),2,previousIntent
                ,PendingIntent.FLAG_UPDATE_CURRENT);
        //设置mRemoteView控件的点击事件
        mRemoteViews.setOnClickPendingIntent(R.id.previous_view,previousPendingIntent);
        //设置图标
        mRemoteViews.setImageViewResource(R.id.previous_view, R.mipmap.note_btn_pre_white);

        //点击下一首按钮广播
        Intent nextIntent = new Intent(MusicService.NotificationReceiver.ACTION_STATUS_BAR);
        nextIntent.putExtra(MusicService.NotificationReceiver.EXTRA,
                MusicService.NotificationReceiver.EXTRA_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(AudioHelper.getContext(), 3, nextIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.next_view, nextPendingIntent);
        mRemoteViews.setImageViewResource(R.id.next_view, R.mipmap.note_btn_next_white);
        mSmallRemoteViews.setOnClickPendingIntent(R.id.next_view, nextPendingIntent);
        mSmallRemoteViews.setImageViewResource(R.id.next_view, R.mipmap.note_btn_next_white);

        //点击收藏按钮广播
        Intent favouriteIntent = new Intent(MusicService.NotificationReceiver.ACTION_STATUS_BAR);
        favouriteIntent.putExtra(MusicService.NotificationReceiver.EXTRA,
                MusicService.NotificationReceiver.EXTRA_FAV);
        PendingIntent favouritePendingIntent =
                PendingIntent.getBroadcast(AudioHelper.getContext(), 4, favouriteIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.favourite_view, favouritePendingIntent);
    }

    public Notification getNotification(){
        return mNotification;
    }
/*
    对外提供更新状态的接口
 */
    /***
     * 显示Notification为加载状态
     */
    public void showLoadStatus(AudioBean bean){
        mAudioBean=bean;
        if(mRemoteViews!=null){
            mRemoteViews.setImageViewResource(R.id.play_view,R.mipmap.note_btn_pause_white);
            mRemoteViews.setTextViewText(R.id.title_view,mAudioBean.name);
            mRemoteViews.setTextViewText(R.id.tip_view,mAudioBean.album);
            //使用图片加载组件加载图片 为notification中的remoteView布局中的控件imageview加载图片
            ImageLoaderManager.getInstance()
                    .displayImageForNotification(AudioHelper.getContext(),mRemoteViews,R.id.image_view,mNotification,NOTIFICATION_ID,mAudioBean.albumPic);
       //更新收藏view
            //判断当前歌曲是否为收藏歌曲
            if (GreenDaoHelper.selectFavourite(mAudioBean)!=null){
                mRemoteViews.setImageViewResource(R.id.favourite_view,R.mipmap.note_btn_loved);
            } else {
                mRemoteViews.setImageViewResource(R.id.favourite_view,R.mipmap.note_btn_love_white);
            }
        //更新小布局
            mSmallRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_pause_white);
            mSmallRemoteViews.setTextViewText(R.id.title_view, mAudioBean.name);
            mSmallRemoteViews.setTextViewText(R.id.tip_view, mAudioBean.album);
            //加载图片
            ImageLoaderManager.getInstance()
                    .displayImageForNotification(AudioHelper.getContext(), mSmallRemoteViews, R.id.image_view,
                            mNotification, NOTIFICATION_ID, mAudioBean.albumPic);
            //通知系统 通知状态栏更新
            mNotificationManager.notify(NOTIFICATION_ID,mNotification);

        }
    }

    /***
     * 显示Notification为播放状态
     */
    public void showPlayStatus(){
        if (mRemoteViews != null) {
            mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_pause_white);
            mSmallRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_pause_white);
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }
    }

    /***
     * 显示Notification为暂停状态
     */
    public void showPauseStatus(){
        if (mRemoteViews != null) {
            mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
            mSmallRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }

    }

    public void changeFavouriteStatus(boolean isFavourite){
        if (mRemoteViews!=null){
            mRemoteViews.setImageViewResource(R.id.favourite_view,
                    isFavourite?R.mipmap.note_btn_loved:R.mipmap.note_btn_love_white);
            mNotificationManager.notify(NOTIFICATION_ID,mNotification);
        }
    }


    /**
     * 与音乐service的回调通信
     */
    public interface NotificationHelperListener {
        void onNotificationInit();
    }
}
