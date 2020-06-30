package com.android.lib_network.okhttp.listener;

/**
 * 真正处理业务逻辑的地方 Java层异常和业务层异常
 */
public interface DisposeDataListener {
    /**
     * 请求成功回调时间处理
     */
    void onSuccess(Object responseObj);

    /**
     * 请求失败回调时间处理
     */
    void onFailure(Object reasonObj);

}
