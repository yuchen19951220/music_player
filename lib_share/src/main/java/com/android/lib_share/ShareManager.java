package com.android.lib_share;

import android.content.Context;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class ShareManager  {
    //懒汉模式
    private static ShareManager mShareManger=null;
    /**
     * 要分享到的平台
     *
     */
    private Platform mCurrentPlatform;


    /**
     * 线程安全的单例模式
     */
    public static ShareManager getInstance(){

        if (mShareManger == null) {
            synchronized (ShareManager.class) {
                if (mShareManger == null) {
                    mShareManger= new ShareManager();
                }
            }
        }
        return mShareManger;
    }

    private ShareManager(){}

    /**
     * 第一个执行的方法 最好在程序入口执行
     */
    public static void initSDK(Context context){
        ShareSDK.initSDK(context);
    }

    /***
     *  接受分享的数据 调用API完成分享
     * @param shareData
     * @param listener
     */
    public void shareData(ShareData shareData, PlatformActionListener listener){

        switch (shareData.mPlatformType) {
            case QQ:
                mCurrentPlatform = ShareSDK.getPlatform(QQ.NAME);
                break;
            case QZone:
                mCurrentPlatform = ShareSDK.getPlatform(QZone.NAME);
                break;
            case WeChat:
                mCurrentPlatform = ShareSDK.getPlatform(Wechat.NAME);
                break;
            case WechatMoments:
                mCurrentPlatform = ShareSDK.getPlatform(WechatMoments.NAME);
                break;
            default:
                break;
        }
        mCurrentPlatform.setPlatformActionListener(listener); //由应用层去处理回调,分享平台不关心。
        mCurrentPlatform.share(shareData.mShareParams);
    }

//    @Override
//    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
//            //分享完成回调
//        if (mListenner!=null) mListenner.onComplete(i,hashMap);
//    }
//
//    @Override
//    public void onError(Platform platform, int i, Throwable throwable) {
//        if (mListenner!=null) mListenner.onError(i,throwable);
//    }
//
//    @Override
//    public void onCancel(Platform platform, int i) {
//        //分享取消回调
//        if (mListenner!=null) mListenner.onCancel(i);
//    }


    /**
     * @author 应用程序需要的平台
     */
    public enum PlatformType {
        QQ, QZone, WeChat, WechatMoments;
    }

    /**
     * 与业务层的接口回调 业务层自己实现监听 解耦和SDK的强依赖
     */
//    public interface PlatformShareListener{
//        void onComplete( int var2, HashMap<String, Object> var3);
//
//        void onError(int var2, Throwable var3);
//
//        void onCancel(int var2);
//    }
}


