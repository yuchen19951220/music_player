package com.android.music_player.view.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.android.lib_audio.app.AudioHelper;
import com.android.lib_audio.mediaplayer.core.AudioController;
import com.android.lib_audio.mediaplayer.model.AudioBean;
import com.android.lib_commin_ui.base.BaseActivity;
import com.android.lib_commin_ui.pager_indictor.ScaleTransitionPagerTitleView;
import com.android.lib_image_ui.app.ImageLoaderManager;
import com.android.music_player.R;
import com.android.music_player.model.login.LoginEvent;
import com.android.music_player.utils.UserManager;
import com.android.music_player.view.home.adpater.HomePagerAdapter;
import com.android.music_player.model.CHANNEL;
import com.android.music_player.view.login.LoginActivity;


import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class HomeActivity extends BaseActivity implements View.OnClickListener{
    //指定首页要显示的卡片
    private static final CHANNEL[] CHANNELS =
            new CHANNEL[]{CHANNEL.MY, CHANNEL.DISCORY, CHANNEL.FRIEND};

    private DrawerLayout mDrawerLayout;
    private View mToggleView;
    private View mSearchView;
    private ViewPager mViewPager;
    private HomePagerAdapter mAdapter;

    private View mDrawerQrcodeView;
    private View mDrawerShareView;
    private View unLogginLayout;
    private ImageView mPhotoView;


    /*
     * data
     */
    private ArrayList<AudioBean> mLists = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注册eventBus事件
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_home);
        initView();
        //初始化列表数据 模拟播放列表
        initData();
    }

    private void initData() {
        mLists.add(new AudioBean("100001", "http://sp-sycdn.kuwo.cn/resource/n2/85/58/433900159.mp3",
                "以你的名字喊我", "周杰伦", "七里香", "电影《不能说的秘密》主题曲,尤其以最美的不是下雨天,是与你一起躲过雨的屋檐最为经典",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1559698076304&di=e6e99aa943b72ef57b97f0be3e0d2446&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fblog%2F201401%2F04%2F20140104170315_XdG38.jpeg",
                "4:30"));
        mLists.add(
                new AudioBean("100002", "http://sq-sycdn.kuwo.cn/resource/n1/98/51/3777061809.mp3", "勇气",
                        "梁静茹", "勇气", "电影《不能说的秘密》主题曲,尤其以最美的不是下雨天,是与你一起躲过雨的屋檐最为经典",
                        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1559698193627&di=711751f16fefddbf4cbf71da7d8e6d66&imgtype=jpg&src=http%3A%2F%2Fimg0.imgtn.bdimg.com%2Fit%2Fu%3D213168965%2C1040740194%26fm%3D214%26gp%3D0.jpg",
                        "4:40"));
        mLists.add(
                new AudioBean("100003", "http://sp-sycdn.kuwo.cn/resource/n2/52/80/2933081485.mp3", "灿烂如你",
                        "汪峰", "春天里", "电影《不能说的秘密》主题曲,尤其以最美的不是下雨天,是与你一起躲过雨的屋檐最为经典",
                        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1559698239736&di=3433a1d95c589e31a36dd7b4c176d13a&imgtype=0&src=http%3A%2F%2Fpic.zdface.com%2Fupload%2F201051814737725.jpg",
                        "3:20"));
        mLists.add(
                new AudioBean("100004", "http://sr-sycdn.kuwo.cn/resource/n2/33/25/2629654819.mp3", "小情歌",
                        "五月天", "小幸运", "电影《不能说的秘密》主题曲,尤其以最美的不是下雨天,是与你一起躲过雨的屋檐最为经典",
                        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1559698289780&di=5146d48002250bf38acfb4c9b4bb6e4e&imgtype=0&src=http%3A%2F%2Fpic.baike.soso.com%2Fp%2F20131220%2Fbki-20131220170401-1254350944.jpg",
                        "2:45"));
        //加入到播放列表
        AudioHelper.startMusicService(mLists);
    }



    //初始化视图
    private void initView(){
        mDrawerLayout=findViewById(R.id.drawer_layout);
        mToggleView=findViewById(R.id.toggle_view);
        mToggleView.setOnClickListener(this);
        mSearchView=findViewById(R.id.search_view);
        mSearchView.setOnClickListener(this);
        mViewPager=findViewById(R.id.view_pager);
        //适配器
        mAdapter=new HomePagerAdapter(getSupportFragmentManager(),CHANNELS);
        mViewPager=findViewById(R.id.view_pager);
        mViewPager.setAdapter(mAdapter);
        //初始化指示器
        initMagciIndicator();

        //登录时的ui
        unLogginLayout=findViewById(R.id.unloggin_layout);
        unLogginLayout.setOnClickListener(this);
        //登录时的图片
        mPhotoView=findViewById(R.id.avatr_view);



    }
    //初始化指示器
    private void initMagciIndicator() {
        MagicIndicator magicIndicator=findViewById(R.id.magic_indicator);
        magicIndicator.setBackgroundColor(Color.WHITE);
        CommonNavigator commonNavigator=new CommonNavigator(this);
        commonNavigator.setAdjustMode(true);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return CHANNELS==null?0: CHANNELS.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new ScaleTransitionPagerTitleView(context);
                simplePagerTitleView.setText(CHANNELS[index].getKey());
                simplePagerTitleView.setTextSize(19);
                simplePagerTitleView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                simplePagerTitleView.setNormalColor(Color.parseColor("#999999"));
                simplePagerTitleView.setSelectedColor(Color.parseColor("#333333"));
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setCurrentItem(index);
                    }
                });
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                return null;
            }

            @Override
            public float getTitleWeight(Context context, int index) {
                return super.getTitleWeight(context, index);
            }

        });
        magicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(magicIndicator,mViewPager);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销evenBus
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.unloggin_layout:
                //已经登录
                if (!UserManager.getInstance().hasLogined()){
                    LoginActivity.start(this);
                } else {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                }
                break;

        }


    }

    //执行在主线程中
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginEvent event){
        //显示未登录布局
        unLogginLayout.setVisibility(View.GONE);
        //显示头像 加载
        mPhotoView.setVisibility(View.VISIBLE);
        ImageLoaderManager.getInstance().displayImageForCircle(mPhotoView,
                                    UserManager.getInstance().getUser().data.photoUrl);

    }
}
