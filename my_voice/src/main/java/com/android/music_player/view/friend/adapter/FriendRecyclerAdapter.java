package com.android.music_player.view.friend.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.lib_audio.app.AudioHelper;
import com.android.lib_commin_ui.MultiImageViewLayout;
import com.android.lib_commin_ui.recyclerview.MultiItemTypeAdapter;
import com.android.lib_commin_ui.recyclerview.base.ItemViewDelegate;
import com.android.lib_commin_ui.recyclerview.base.ViewHolder;
import com.android.lib_image_ui.app.ImageLoaderManager;
import com.android.lib_video.videoplayer.core.VideoAdContext;
import com.android.music_player.R;
import com.android.music_player.model.friend.FriendBodyValue;
import com.android.music_player.utils.UserManager;
import com.android.music_player.view.login.LoginActivity;

import java.util.List;

public class FriendRecyclerAdapter extends MultiItemTypeAdapter {//继承开源MutiItemTpeAdapter 可添加多种item类型

    public static final int MUSIC_TYPE=0x01;
    public static final int VIDEO_TYPE=0x02;

    private Context mContext;


    public FriendRecyclerAdapter(Context context, List<FriendBodyValue> datas) {
        super(context, datas);
        mContext=context;
        addItemViewDelegate(MUSIC_TYPE,new MusicItemDelegate());
        addItemViewDelegate(VIDEO_TYPE,new VideoItemDelegate());
    }


    //音乐类item
    private class MusicItemDelegate implements ItemViewDelegate<FriendBodyValue> {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.item_friend_list_picture_layout; //获取布局
        }

        @Override
        public boolean isForViewType(FriendBodyValue item, int position) {
            return item.type==FriendRecyclerAdapter.MUSIC_TYPE; //确认类型
        }

        /**
         * 为view绑定数据
         * @param holder
         * @param recommandBodyValue,
         * @param position
         */
        @Override
        public void convert(ViewHolder holder, final FriendBodyValue recommandBodyValue, int position) {
            holder.setText(R.id.name_view, recommandBodyValue.name + " 分享单曲:");
            holder.setText(R.id.fansi_view, recommandBodyValue.fans + "粉丝");
            holder.setText(R.id.text_view, recommandBodyValue.text);
            holder.setText(R.id.zan_view, recommandBodyValue.zan);
            holder.setText(R.id.message_view, recommandBodyValue.msg);
            holder.setText(R.id.audio_name_view, recommandBodyValue.audioBean.name);
            holder.setText(R.id.audio_author_view, recommandBodyValue.audioBean.album);
            //设置监听事件 点击跳转播放
            holder.setOnClickListener(R.id.album_layout, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //调用音频播放器方法
                    AudioHelper.addAudio((Activity) mContext,recommandBodyValue.audioBean);
                }
            });
            //关注逻辑
            holder.setOnClickListener(R.id.guanzhu_view, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!UserManager.getInstance().hasLogined()) {
                        //goto login
                        LoginActivity.start(mContext);
                    }
                }
            });
            ImageView avatar = holder.getView(R.id.photo_view);
            ImageLoaderManager.getInstance().displayImageForCircle(avatar, recommandBodyValue.avatr);
            ImageView albumPicView = holder.getView(R.id.album_view);
            ImageLoaderManager.getInstance()
                    .displayImageForView(albumPicView, recommandBodyValue.audioBean.albumPic);

            MultiImageViewLayout imageViewLayout = holder.getView(R.id.image_layout);
            imageViewLayout.setList(recommandBodyValue.pics);


        }
    }


    /**
     * 视屏类item
     */
    private class  VideoItemDelegate implements ItemViewDelegate<FriendBodyValue>{

        @Override
        public int getItemViewLayoutId() {
            return R.layout.item_friend_list_video_layout;
        }

        @Override
        public boolean isForViewType(FriendBodyValue item, int position) {
            return item.type == FriendRecyclerAdapter.VIDEO_TYPE;
        }

        @Override
        public void convert(ViewHolder holder, FriendBodyValue recommandBodyValue, int position) {
            RelativeLayout videoGroup = holder.getView(R.id.video_layout);
            VideoAdContext mAdsdkContext = new VideoAdContext(videoGroup, recommandBodyValue.videoUrl);
            holder.setText(R.id.fansi_view, recommandBodyValue.fans + "粉丝");
            holder.setText(R.id.name_view, recommandBodyValue.name + " 分享视频");
            holder.setText(R.id.text_view, recommandBodyValue.text);
            holder.setOnClickListener(R.id.guanzhu_view, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!UserManager.getInstance().hasLogined()) {
                        //goto login
                        LoginActivity.start(mContext);
                    }
                }
            });
            ImageView avatar = holder.getView(R.id.photo_view);
            ImageLoaderManager.getInstance().displayImageForCircle(avatar, recommandBodyValue.avatr);
        }
    }



}


