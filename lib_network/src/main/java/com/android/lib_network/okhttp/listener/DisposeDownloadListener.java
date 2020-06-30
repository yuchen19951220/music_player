package com.android.lib_network.okhttp.listener;

/***
 * 监听回调接口
 * 监听下载进度
 */
public interface DisposeDownloadListener extends DisposeDataListener{
    void onProgress(int progress);
}
