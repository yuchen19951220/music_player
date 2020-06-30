package com.android.lib_network.okhttp.exception;

public class OkHttpException extends Exception {
    private static final long serialVersionUID=1L;
    /**
     * 服务器返回码
     */
    private int ecode;
    /**
     * 服务器返回错误信息
     *
     */
    private Object emsg;

    public OkHttpException(int ecode, Object emsg){
        this.ecode=ecode;
        this.emsg=emsg;
    }
    public int getEcode(){
        return ecode;
    }
    public Object getEmsg(){
        return emsg;
    }
}
