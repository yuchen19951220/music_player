package com.android.lib_image_ui.app;


import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.android.lib_image_ui.R;
import com.android.lib_image_ui.image.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.util.Util;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;


/***
 * 图片加载类 与外界的唯一调用类 支持为view notifaication appwidget加载图片
 */
public class ImageLoaderManager {

    //单例模式 构造器私有化
    private ImageLoaderManager(){

    }

    //使用内部类实现单例模式
    private static class SingletonHolder{
        private static ImageLoaderManager instance =new ImageLoaderManager();
    }

    public static ImageLoaderManager getInstance(){
        return SingletonHolder.instance;
    }

    /**
     * 为ImageView加载图片
     */
    public void displayImageForView(ImageView imageView, String url){
        //使用Glide加载进ImageView
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(url)
                //选项配置
                .apply(initCommonRequestOptions())
                //加入加载过度效果
                .transition(withCrossFade())
                .into(imageView);
    }

    /**
     * 加载圆形图片
     * @return
     */

    public void displayImageForCircle(final ImageView imageView, String url){
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(url)
                .apply(initCommonRequestOptions())
                .into(new BitmapImageViewTarget(imageView){
                    //将imageview包装成target (可以为任何Target加载图片)
                    @Override
                    protected void setResource(Bitmap resource) {
                        //创建一个圆形图片
                        RoundedBitmapDrawable drawable= RoundedBitmapDrawableFactory.create(imageView.getResources(),resource);
                        drawable.setCircular(true);
                        imageView.setImageDrawable(drawable);
                    }
                });//不直接加载到imageView中
    }

    /***
     * 完成对viewgroup设置背景并进行模糊处理
     * @param group
     * @param url
     */

    public void displayImageForViewGroup(final ViewGroup group, String url){
        Glide.with(group.getContext())
                .asBitmap()
                .load(url)
                .apply(initCommonRequestOptions())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        final Bitmap res=resource;
                        //使用Rxjava分发任务 为背景添加图片提高性能 异步处理
                        Observable.just(resource)
                                //map映射
                            .map(new Function<Bitmap, Drawable>() {
                            @Override
                            public Drawable apply(Bitmap bitmap) throws Exception {
                               //将bitmap进行模糊处理并转为drawable
                                Drawable drawable =new BitmapDrawable(Utils.doBlur(res, 100, true));
                                return drawable;
                            }
                        })
                            .subscribeOn(Schedulers.io()) //耗时任务指定子线程中IO线程
                            .observeOn(AndroidSchedulers.mainThread()) //消费者线程为主线程
                            .subscribe(new Consumer<Drawable>() {
                                    @Override
                                    public void accept(Drawable drawable) throws Exception {
                                       //对控件设置背景
                                        group.setBackground(drawable);
                                    }
                                });
                    }
                });
    }

    /***
     *
     * @param context
     * @param rv notification的布局
     * @param id :要加载图片的控件Id
     * @param notification
     * @param NOTIFICATION_ID notification id号
     * @param url
     */

    public void displayImageForNotification(Context context, RemoteViews rv,
                                            int id, Notification notification,
                                            int NOTIFICATION_ID, String url){
        this.displayImageForTarget(context,
                initNotificationTarget(context,id,rv,notification,NOTIFICATION_ID),url);

    }


    /**
     * 为非view加载图片
     * @return
     */
    private void displayImageForTarget(Context context, Target target, String url){
        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(initCommonRequestOptions())
                .transition(BitmapTransitionOptions.withCrossFade())
                .fitCenter()
                .into(target);
    }


    /*
     * 初始化Notification Target
     */
    private NotificationTarget initNotificationTarget(Context context, int id, RemoteViews rv,
                                                      Notification notification, int NOTIFICATION_ID) {
        NotificationTarget notificationTarget =
                new NotificationTarget(context, id, rv, notification, NOTIFICATION_ID);
        return notificationTarget;
    }


    //初始化选项配置
    private RequestOptions initCommonRequestOptions(){
        RequestOptions options=new RequestOptions();
        options.placeholder(R.mipmap.b4y) //设置占位图
                .error(R.mipmap.b4y) //错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) //磁盘缓存策略
                .skipMemoryCache(false)     //是否需要缓存
                .priority(Priority.NORMAL); //图片下载线程的优先级
        return options;
    }
}
