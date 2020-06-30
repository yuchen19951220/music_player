package com.android.lib_audio.mediaplayer.db;

import android.database.sqlite.SQLiteDatabase;

import com.android.lib_audio.app.AudioHelper;
import com.android.lib_audio.mediaplayer.model.AudioBean;
import com.android.lib_audio.mediaplayer.model.Favourite;

/**
 *
 * 操作greenDao数据据的帮助类
 * 1、初始化数据库
 * 2、为业务层提供各种增删改查
 */
public class GreenDaoHelper {
    private static final String DB_NAME="music_db";
    //数据库创建的工具类 用于创建数据库和升级数据库
    private static DaoMaster.DevOpenHelper mHelper;
    //数据库
    private static SQLiteDatabase mDb;
    //管理数据库 greenDao的annotation自动生成的类
    private static DaoMaster mDaoMaster;
    //管理各种实体DAO 不让业务层闹到session直接操作数据库 ,统一由此类提供方法
    private static DaoSession mDaoSession;

    /**
     * 设置greenDao
     */
    public static void initDatabase(){
        mHelper=new DaoMaster.DevOpenHelper(AudioHelper.getContext(),DB_NAME,null);
        mDb=mHelper.getWritableDatabase();//获得数据库
        mDaoMaster=new DaoMaster(mDb);
        mDaoSession=mDaoMaster.newSession();
    }

    /**
     * 添加感兴趣收藏
     */
    public static void addFavourite(AudioBean audioBean){
        FavouriteDao dao = mDaoSession.getFavouriteDao();//获取DaoSession对象
        Favourite favourite=new Favourite(); //创建favourite对象
        favourite.setAudioId(audioBean.id);//设置favourite对象属性
        favourite.setAudioBean(audioBean);
        favourite.setAudioName(audioBean.name);
        dao.insertOrReplace(favourite); //使用dao操作数据库添加
    }

    /**
     * 移除感兴趣的收藏
     */
    public static void removeFavourite(AudioBean audioBean){
        FavouriteDao dao = mDaoSession.getFavouriteDao();//获取DaoSession对象
        //查询对应id 首先通过queryBuilder构造查询条件 查找
        Favourite favourite= dao.queryBuilder().where(FavouriteDao.Properties.AudioId.eq(audioBean.id)).unique();
        dao.delete(favourite);
    }

    /**
     * 查找感兴趣的收藏
     */
    public static  Favourite selectFavourite(AudioBean audioBean){
        FavouriteDao dao = mDaoSession.getFavouriteDao();//获取DaoSession对象
        Favourite favourite= dao.queryBuilder().where(FavouriteDao.Properties.AudioId.eq(audioBean.id)).unique();
        return favourite;
    }

}
