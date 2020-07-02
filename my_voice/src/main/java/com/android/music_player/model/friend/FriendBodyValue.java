package com.android.music_player.model.friend;


import android.content.Context;

import com.android.lib_audio.mediaplayer.model.AudioBean;
import com.android.lib_commin_ui.recyclerview.MultiItemTypeAdapter;

import java.util.ArrayList;
import java.util.List;

/***
 * 朋友实体
 */
public class FriendBodyValue {
    public int type;
    public String avatr;
    public String name;
    public String fans;
    public String text;
    public ArrayList<String> pics;
    public String videoUrl;
    public String zan;
    public String msg;
    public AudioBean audioBean;


}
