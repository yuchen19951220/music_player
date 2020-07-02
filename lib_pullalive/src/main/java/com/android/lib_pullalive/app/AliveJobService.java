package com.android.lib_pullalive.app;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * 一个轻量级后台job service 利用空闲时间执行一些小事情 提高进程不被回收的概率
 *
 */
@TargetApi(value = Build.VERSION_CODES.LOLLIPOP)
public class AliveJobService extends JobService {
    private static final String TAG=AliveJobService.class.getName();

    private JobScheduler mJobScheduler; //任务调度类

    public static void start(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(context, AliveJobService.class);
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //获取系统任务调度类
        mJobScheduler=(JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    @Override
    //把JOB提交到系统
    public int onStartCommand(Intent intent, int flags, int startId) {
        JobInfo job=initJobInfo(startId);
        //提交自己的job到system process中
       if (mJobScheduler.schedule(job)<=0){
           Log.d(TAG, "AliveJobService failed");
       } else {
           Log.d(TAG, "AliveJobService success");
       }
        return START_STICKY;
    }
    //初始化jobInfo
    private JobInfo initJobInfo(int startId) {
        //参数多 提供了构建者模式
        JobInfo.Builder builder=new JobInfo.Builder(startId,
                new ComponentName(getPackageName(),AliveJobService.class.getName()));
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
           builder.setMinimumLatency(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS); //执行的最小延迟时间
           builder.setOverrideDeadline(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS);  //执行的最长延时时间
           builder.setBackoffCriteria(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS,
                   JobInfo.BACKOFF_POLICY_LINEAR);//线性重试方案
       } else {
           builder.setPeriodic(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS);
       }
        builder.setPersisted(false); //是否持久 应用关闭后是否运行
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        builder.setRequiresCharging(false); //是否充电时进行
        return builder.build();
    }

    //主线程的handler
    private Handler mHander=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case  0x01:
                    Log.d(TAG, "pull alive");
                    jobFinished((JobParameters) msg.obj,true);
                    break;
//                case  0x02:
//                    break;

            }
            return true;
        }
    });

    @Override
    //工作触发时回调
    public boolean onStartJob(JobParameters jobParameters) {
        mHander.sendMessage(Message.obtain(mHander,1,jobParameters));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        mHander.removeCallbacksAndMessages(null);
        return false;
    }
}
