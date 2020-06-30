package com.android.lib_network.okhttp.listener;

/**
 * 包装类 包装要解析成的对象 和路径 已经接受回调对象参数listener
 */
public class DisposeDataHandle {
    public DisposeDataListener mListener =null;
    public Class<?> mClass=null;
    public String mSource=null;//文件保存路径
    public DisposeDataHandle(DisposeDataListener listener){ this.mListener=listener;}

    public DisposeDataHandle(DisposeDataListener listener, Class<?>clazz){
        this.mListener=listener;
        this.mClass=clazz;
    }

    public DisposeDataHandle(DisposeDataListener listener,String source){
        this.mListener=listener;
        this.mSource=source;
    }
}
