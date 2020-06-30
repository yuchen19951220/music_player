package com.android.lib_share;


import cn.sharesdk.framework.Platform;
import cn.sharesdk.tencent.qq.QQ;

/**
 * 分享的数据实体
 */
public class ShareData {
    /**
     * 要分享的平台
     */
    public ShareManager.PlatformType mPlatfornType;
    /**
     * 要分享的品台参数
     */
    public Platform.ShareParams mShareParams;
}
