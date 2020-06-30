package com.android.lib_network.okhttp;

import com.android.lib_network.okhttp.cookie.SimpleCookieJar;
import com.android.lib_network.okhttp.https.HttpsUtils;
import com.android.lib_network.okhttp.listener.DisposeDataHandle;
import com.android.lib_network.okhttp.response.CommonFileCallback;
import com.android.lib_network.okhttp.response.CommonJsonCallback;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * 发送get post 请求的工具类 包括设置一些请求的公共参数
 */
public class CommonOkHttpCilent {
    private static final int TIME_OUT=30;
   //使用单例模式 
    private static OkHttpClient mOkHttpClient;
    //静态初始化块
    static {
        OkHttpClient.Builder okHttpClientBuilder=new OkHttpClient.Builder();
        okHttpClientBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
        /**
         * 为所有请求添加请求头
         */
        //添加拦截器
        okHttpClientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request=chain.request().newBuilder().addHeader("User-Agent", "Imooc-Mobile")// 标明发送本次请求的客户端
                .build();
                return chain.proceed(request);
            }
        });
        //设置cookie
        okHttpClientBuilder.cookieJar(new SimpleCookieJar());
        //设置超时时间
        okHttpClientBuilder.connectTimeout(TIME_OUT, TimeUnit.SECONDS);
        okHttpClientBuilder.readTimeout(TIME_OUT, TimeUnit.SECONDS);
        okHttpClientBuilder.writeTimeout(TIME_OUT, TimeUnit.SECONDS);
        okHttpClientBuilder.followRedirects(true);
        /**
         * trust all the https point
         */
        okHttpClientBuilder.sslSocketFactory(HttpsUtils.initSSLSocketFactory(),
                HttpsUtils.initTrustManager());
        mOkHttpClient = okHttpClientBuilder.build();
    }

    /**
     * get请求
     */
    public static Call get(Request request, DisposeDataHandle handle){
        Call call=mOkHttpClient.newCall(request);
        //将request和响应 CommonJsonCallback联系起来
        call.enqueue(new CommonJsonCallback(handle));
        return call;
    }
    /**
     * post请求
     */
    public static Call post(Request request, DisposeDataHandle handle){
        Call call=mOkHttpClient.newCall(request);
        //将request和响应 CommonJsonCallback联系起来
        call.enqueue(new CommonJsonCallback(handle));
        return call;
    }
    /**
     * 文件下载请求
     */
    public static Call downloadFile(Request request, DisposeDataHandle handle){
        Call call=mOkHttpClient.newCall(request);
        call.enqueue(new CommonFileCallback(handle));
        return call;
    }


}
