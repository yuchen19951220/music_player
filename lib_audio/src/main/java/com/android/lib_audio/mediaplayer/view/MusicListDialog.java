package com.android.lib_audio.mediaplayer.view;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.lib_audio.R;
import com.android.lib_audio.mediaplayer.core.AudioController;
import com.android.lib_audio.mediaplayer.core.AudioController.PlayMode;
import com.android.lib_audio.mediaplayer.events.AudioLoadEvent;
import com.android.lib_audio.mediaplayer.events.AudioPlayModeEvent;
import com.android.lib_audio.mediaplayer.model.AudioBean;
import com.android.lib_audio.mediaplayer.view.adapter.MusicListAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class MusicListDialog extends BottomSheetDialog {
    private Context mContext;
    private DisplayMetrics dm;

    /*view
     */
    private ImageView mTipView;
    private TextView mPlayModeView;
    private RecyclerView mRecycleView;
    private MusicListAdapter mMusicListAdapter;

    /*
     * data
     */
    private ArrayList<AudioBean> mQueue; //播放队列
    private AudioBean mAudioBean; //当前正在播放歌曲
    private PlayMode mPlayMode;



    public MusicListDialog(@NonNull Context context) {
        super(context);
        mContext=context;
        EventBus.getDefault().register(this); //注册
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dm=mContext.getResources().getDisplayMetrics();
        setContentView(R.layout.dialog_bottom_sheet);
        initData();
        initView();
    }

    private void initView() {
        /***
         * 充满宽度 在style中定义
         */
        Window dialogWindow=getWindow();
        WindowManager.LayoutParams lp=dialogWindow.getAttributes();
        lp.width=dm.widthPixels;//设置宽度
        dialogWindow.setAttributes(lp);

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        mTipView=findViewById(R.id.mode_image_view);
        mPlayModeView=findViewById(R.id.mode_text_view);
        mPlayModeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //调用切换播放模式事件
                switch (mPlayMode) {
                    case LOOP:
                        AudioController.getInstance().setPlayMode(PlayMode.RANDOM);
                        break;
                    case RANDOM:
                        AudioController.getInstance().setPlayMode(PlayMode.REPEAT);
                        break;
                    case REPEAT:
                        AudioController.getInstance().setPlayMode(PlayMode.LOOP);
                        break;
                }
            }
        });

        //更新界面
        updatePlayModeView();
        //初始化recycler
        mRecycleView=findViewById(R.id.recycler);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecycleView.setLayoutManager(linearLayoutManager);
        mMusicListAdapter=new MusicListAdapter(mQueue,mAudioBean,mContext);
        mRecycleView.setAdapter(mMusicListAdapter);


    }

    private void initData() {
        //当前播放歌曲 用于初始化UI
        mQueue=AudioController.getInstance().getQueue();
        mAudioBean=AudioController.getInstance().getNowPlaying();
        mPlayMode=AudioController.getInstance().getPlayMode();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioLoadEvent(AudioLoadEvent event) {
        mAudioBean = event.mAudioBean;
        //更新列表
        updateList();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioPlayModeEvent(AudioPlayModeEvent event) {
        mPlayMode = event.mPlayMode;
        //更新播放模式
        updatePlayModeView();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        EventBus.getDefault().unregister(this);
    }



    /**
     * 更新视图
     */
    private void updatePlayModeView() {
        switch (mPlayMode) {
            case LOOP:
                mTipView.setImageResource(R.mipmap.loop);
                mPlayModeView.setText("列表循环");
                break;
            case RANDOM:
                mTipView.setImageResource(R.mipmap.random);
                mPlayModeView.setText("随机播放");
                break;
            case REPEAT:
                mTipView.setImageResource(R.mipmap.once);
                mPlayModeView.setText("单曲循环");
                break;
        }
    }

    private void updateList() {
        mMusicListAdapter.updateAdapter(mAudioBean);
    }


}
