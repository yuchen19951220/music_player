package com.android.music_player.view.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.android.lib_commin_ui.base.BaseActivity;
import com.android.lib_network.okhttp.listener.DisposeDataListener;
import com.android.music_player.R;
import com.android.music_player.api.RequestCenter;
import com.android.music_player.model.login.LoginEvent;
import com.android.music_player.model.user.User;
import com.android.music_player.utils.UserManager;

import org.greenrobot.eventbus.EventBus;

public class LoginActivity extends BaseActivity implements DisposeDataListener {
    private static final String TAG = "LoginActivity";


    //启动活动方法
    public static void start(Context context){
        Intent intent =new Intent(context,LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_layout);
        findViewById(R.id.login_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //网络上传
                RequestCenter.login(LoginActivity.this);
            }
        });
    }

    @Override
    public void onSuccess(Object responseObj) {
        //处理正常逻辑
        User user=(User)responseObj;
        UserManager.getInstance().saveUser(user);
        //使用EventBus发布时间 更新UI
        //发布事件--成功 通知ui更新-homeActivy
        EventBus.getDefault().post(new LoginEvent());
        finish();
    }

    @Override
    public void onFailure(Object reasonObj) {
        //登录失败逻辑
        Log.d(TAG, "onFailure");
    }
}
