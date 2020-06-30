package com.android.lib_share;

import android.content.Context;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class ShareManager {
    //懒汉模式
    private static ShareManager mShareManger=new ShareManager();;
    /**
     * 要分享到的平台
     *
     */
    private Platform mCurrentPlatform;
    /**
     * 线程安全的单例模式
     */
    public static ShareManager getInstance(){

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
     * 分享数据到不同平台
     */
    public void shareData(ShareData shareData, PlatformActionListener listener){
        switch (shareData.mPlatfornType){
            case QQ:
                mCurrentPlatform=ShareSDK.getPlatform(QQ.NAME);
                break;
            case QZone:
                mCurrentPlatform=ShareSDK.getPlatform(QZone.NAME);
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


    /**
     * @author 应用程序需要的平台
     */
    public enum PlatformType {
        QQ, QZone, WeChat, WechatMoments;
    }
}
