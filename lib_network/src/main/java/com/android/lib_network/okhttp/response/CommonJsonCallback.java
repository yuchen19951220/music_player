package com.android.lib_network.okhttp.response;

import android.os.Handler;
import android.os.Looper;

import com.android.lib_network.okhttp.exception.OkHttpException;
import com.android.lib_network.okhttp.listener.DisposeDataHandle;
import com.android.lib_network.okhttp.listener.DisposeDataListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.EmptyStackException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 处理Json类型的响应
 */
public class CommonJsonCallback implements Callback {


    /**
     * 携带信息
     * the logic layer exception, may alter in different app
     */
    protected final String RESULT_CODE = "ecode"; // 有返回则对于http请求来说是成功的，但还有可能是业务逻辑上的错误
    protected final int RESULT_CODE_VALUE = 0;
    protected final String ERROR_MSG = "emsg";
    protected final String EMPTY_MSG = "";

    /**
     * the java layer exception, do not same to the logic error
     */
    protected final int NETWORK_ERROR = -1; // the network relative error
    protected final int JSON_ERROR = -2; // the JSON relative error（JSon解析异常）
    protected final int OTHER_ERROR = -3; // the unknow error (配置类型的异常)

    //过程监听回调
    private DisposeDataListener mListener; //包装类 包装要解析成的对象 和路径
    private Handler mDeliveryHandler;//将子线程中的数据发送到UI线程（主线程）
    private Class<?> mClass;//要解析成的对象

    public CommonJsonCallback(DisposeDataHandle handle){
        mListener=handle.mListener;
        this.mClass=handle.mClass;
        this.mDeliveryHandler=new Handler(Looper.getMainLooper());//创建一个在主线程的Handler
    }


    @Override
    public void onFailure(Call call, final IOException e) {
       //传递到子线程
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onFailure(new OkHttpException(NETWORK_ERROR,e));
            }
        });

    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        final String result=response.body().string();//获取返回字符串
        //传递到子线程
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
                handleResponse(result);
            }
        });
    }

    private void handleResponse(String result) {
        if (result==null||result.trim().equals("")){
            mListener.onFailure(new OkHttpException(NETWORK_ERROR, EMPTY_MSG));
            return;
        }
        try {
            //解析JSON
            if (mClass==null){
                //不需要解析数据 获取原始数据
                mListener.onSuccess(result);
            } else {
                //从结果中解析对象
                Object obj=new Gson().fromJson(result,mClass);
                if (obj!=null){
                    mListener.onSuccess(obj);
                } else {
                    mListener.onFailure(new OkHttpException(JSON_ERROR,EMPTY_MSG));
                }
            }
        } catch (JsonSyntaxException e) {
            mListener.onFailure(new OkHttpException(OTHER_ERROR,e));
            e.printStackTrace();
        }
    }
}
