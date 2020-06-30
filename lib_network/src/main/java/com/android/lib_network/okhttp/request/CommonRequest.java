package com.android.lib_network.okhttp.request;

import java.io.File;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

//对外提供get/post/文件上传请求


public class CommonRequest {
    //不定义请求头
    public static Request createPostRequest(String url,RequestParams params){
        return createPostRequest(url,params,null);
    }

    /**
     *带请求头的Post请求
     * @param url
     * @param params
     * @param headers
     * @return
     */
    public static Request createPostRequest(String url,RequestParams params, RequestParams headers){
        //请求体
        FormBody.Builder mFormBodyBuilder=new FormBody.Builder();
        if (params!=null){
            for(Map.Entry<String,String> entry:params.urlParams.entrySet()){
                //遍历参数
                mFormBodyBuilder.add(entry.getKey(),entry.getValue());
            }
        }
        //添加请求头
        Headers.Builder mHeaderBuilder = new Headers.Builder();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.urlParams.entrySet()) {
                mHeaderBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        //获得请求头和请求体
        FormBody mFormBody = mFormBodyBuilder.build();
        Headers mHeader = mHeaderBuilder.build();
        //获取Request对象
        Request request=new Request.Builder().url(url)
                .post(mFormBody)
                .headers(mHeader)
                .build();
        return request;
    }


    //不带请求头的get请求
    public static Request createGetRequest(String url, RequestParams params) {

        return createGetRequest(url, params, null);
    }


    /**
     * 带请求头的get请求
     * @param url
     * @param params
     * @param headers
     * @return
     */
    public static Request createGetRequest(String url, RequestParams params, RequestParams headers) {
        StringBuilder urlBuilder = new StringBuilder(url).append("?");
        if (params != null) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        //添加请求头
        Headers.Builder mHeaderBuild = new Headers.Builder();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.urlParams.entrySet()) {
                mHeaderBuild.add(entry.getKey(), entry.getValue());
            }
        }
        Headers mHeader = mHeaderBuild.build();
        return new Request.Builder().
                url(urlBuilder.substring(0, urlBuilder.length() - 1))
                .get()
                .headers(mHeader)
                .build();
    }


    /**
     * 文件上传请求
     *
     * @return
     */
    //请求类型 定义文件类型
    private static final MediaType FILE_TYPE = MediaType.parse("application/octet-stream");

    public static Request createMultiPostRequest(String url, RequestParams params) {
        MultipartBody.Builder requestBody = new MultipartBody.Builder();
        //指定请求体类型
        requestBody.setType(MultipartBody.FORM);
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.fileParams.entrySet()) {
               //如果是文件
                if (entry.getValue() instanceof File) {
                    //指定文件头 通过Header.of直接产生
                    requestBody.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                            RequestBody.create(FILE_TYPE, (File) entry.getValue()));
                } else if (entry.getValue() instanceof String) {
                    //如果是json字符串
                    requestBody.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                            RequestBody.create(null, (String) entry.getValue()));
                }
            }
        }
        return new Request.Builder().url(url).post(requestBody.build()).build();
    }



}
