package com.android.music_player.view.home.adpater;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.android.music_player.view.VideoFragment;
import com.android.music_player.view.discory.DiscoryFragment;
import com.android.music_player.model.friend.FriendFragment;
import com.android.music_player.model.CHANNEL;
import com.android.music_player.view.mine.MineFragment;

//首页ViewPager的adapter
public class HomePagerAdapter extends FragmentPagerAdapter {
    private CHANNEL[] mList;

    public HomePagerAdapter(FragmentManager fm, CHANNEL[] datas) {
        super(fm);
        mList = datas;
    }

    //这种方式，避免一次性创建所有的framgent(传值而不是传fragment)
    @Override
    public Fragment getItem(int position) {
        int type = mList[position].getValue();
        switch (type) {
            case CHANNEL.MINE_ID:
                    return MineFragment.newInstance();
            case CHANNEL.DISCORY_ID:
                    return DiscoryFragment.newInstance();
            case CHANNEL.FRIEND_ID:
                    return FriendFragment.newInstance();
            case CHANNEL.VIDEO_ID:
                    return VideoFragment.newInstance();

        }
        return null;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.length;
    }
}
