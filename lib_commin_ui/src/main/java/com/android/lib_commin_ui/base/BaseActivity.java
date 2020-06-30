package com.android.lib_commin_ui.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.android.lib_commin_ui.utils.StatusBarUtil;

//在这里实现沉浸式效果 作为基类
public class BaseActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //使用工具类实现沉浸式效果
        StatusBarUtil.statusBarLightMode(this);
    }
}
