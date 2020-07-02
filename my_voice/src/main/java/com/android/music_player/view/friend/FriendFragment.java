package com.android.music_player.view.friend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.lib_commin_ui.recyclerview.wrapper.LoadMoreWrapper;
import com.android.lib_network.okhttp.listener.DisposeDataListener;
import com.android.music_player.R;
import com.android.music_player.api.RequestCenter;
import com.android.music_player.model.friend.BaseFriendModel;
import com.android.music_player.model.friend.FriendBodyValue;
import com.android.music_player.view.friend.adapter.FriendRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class FriendFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener , LoadMoreWrapper.OnLoadMoreListener {
    private Context mContext;

    /*
    UI
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private FriendRecyclerAdapter mAdapter; //适配器
    private LoadMoreWrapper mLoadMoreWrapper;

   /*
   data
    */
   private BaseFriendModel mRecommandData;
    private List<FriendBodyValue> mDatas = new ArrayList<>();

    public static Fragment newInstance() {
        FriendFragment fragment = new FriendFragment();
        return fragment;
    }

    //fragment回调方法


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=getActivity();
    }

    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View rootView=inflater.inflate(R.layout.fragment_friend_layout,null);
        mSwipeRefreshLayout=rootView.findViewById(R.id.refresh_layout); //获取刷新布局
//        mSwipeRefreshLayout.setColorSchemeResources(getResources().getColor(android.R.color.holo_red_light));
        mSwipeRefreshLayout.setOnRefreshListener(this); //设置下拉刷新监听

        mRecyclerView=rootView.findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext)); //使用线性布局
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //发送请求更新Ui;
        requestData();
    }

    private void requestData() {
        RequestCenter.requestFriendData(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                //显示数据
                mRecommandData=(BaseFriendModel)responseObj;
                upadateUI(); //更新界面
            }
            @Override
            public void onFailure(Object reasonObj) {
                //错误数据
            }
        });
    }


    private void upadateUI() {
        mSwipeRefreshLayout.setRefreshing(false);
        mDatas=mRecommandData.data.list;
        mAdapter=new FriendRecyclerAdapter(mContext,mDatas);
        //加载更多初始化化
        mLoadMoreWrapper=new LoadMoreWrapper(mAdapter); //包装器 包装adapter 给adapter最后的数据加上loadMore效果
        mLoadMoreWrapper.setLoadMoreView(R.layout.default_loading);
        mLoadMoreWrapper.setOnLoadMoreListener(this); //设置加载更多监听

        mRecyclerView.setAdapter(mLoadMoreWrapper);

    }

    @Override
    //加载更多
    public void onLoadMoreRequested() {
        loadMore();
    }

    private void loadMore() {
        RequestCenter.requestFriendData(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseFriendModel moreData=(BaseFriendModel) responseObj;
                //追加到adapter数据中
                mDatas.addAll(moreData.data.list);
                mLoadMoreWrapper.notifyDataSetChanged(); //提示数据更新
            }

            @Override
            public void onFailure(Object reasonObj) {
                //显示氢气失败 显示mock数据
                //onSuccess(ResponseEntityToModule.parseJsonToModule(MockData.FRIEND_DATA, BaseFriendModel.class));
            }
        });
    }

    @Override
    public void onRefresh() {
        requestData();
    }



}
