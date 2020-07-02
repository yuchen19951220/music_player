package com.android.lib_share;


import cn.sharesdk.framework.Platform;
import cn.sharesdk.tencent.qq.QQ;

/**
 * @author 要分享的数据实体
 *
 */
public class ShareData {

    /**
     * 要分享到的平台
     */
    public ShareManager.PlatformType mPlatformType;

    /**
     * 要分享到的平台的参数
     */
    public Platform.ShareParams mShareParams;
}
