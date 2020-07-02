package com.android.music_player.api;

import com.android.lib_network.okhttp.CommonOkHttpCilent;
import com.android.lib_network.okhttp.listener.DisposeDataHandle;
import com.android.lib_network.okhttp.listener.DisposeDataListener;
import com.android.lib_network.okhttp.request.CommonRequest;
import com.android.lib_network.okhttp.request.RequestParams;
import com.android.music_player.model.friend.BaseFriendModel;
import com.android.music_player.model.user.User;

public class RequestCenter {
    //常量内部类 定义一些常量接口
    static class HttpConstants {
//        private static final String ROOT_URL = "http://imooc.com/api";
        private static final String ROOT_URL = "http://39.97.122.129";

        /**
         * 首页请求接口
         */
        private static String HOME_RECOMMAND = ROOT_URL + "/module_voice/home_recommand";

        private static String HOME_RECOMMAND_MORE = ROOT_URL + "/module_voice/home_recommand_more";

        private static String HOME_FRIEND = ROOT_URL + "/module_voice/home_friend";

        /**
         * 登陆接口
         */
        public static String LOGIN = ROOT_URL + "/module_voice/login_phone";
    }

    //根据参数发送所有Post请求
    public static void postRequest(String url, RequestParams params, DisposeDataListener listener, Class<?> clazz){
        CommonOkHttpCilent.post(CommonRequest.createPostRequest(url,params),new DisposeDataHandle(listener,clazz));
    }

    //根据参数发送所有get请求
    public static void getRequest(String url, RequestParams params, DisposeDataListener listener,
                                  Class<?> clazz) {
        CommonOkHttpCilent.get(CommonRequest.
                createGetRequest(url, params), new DisposeDataHandle(listener, clazz));
    }
    /**
     * 用户登录请求
     *
     */
    public static void login(DisposeDataListener listener){
        RequestParams params=new RequestParams();
        params.put("mb", "18734924592");
        params.put("pwd", "999999q");
        RequestCenter.getRequest(HttpConstants.LOGIN,params,listener, User.class);
    }

    /***
     * 朋友页面请求
     * @param listener
     */
    public static void requestFriendData(DisposeDataListener listener) {
        RequestCenter.getRequest(HttpConstants.HOME_FRIEND, null, listener, BaseFriendModel.class);
    }

}
