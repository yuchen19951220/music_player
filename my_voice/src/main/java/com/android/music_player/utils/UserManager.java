package com.android.music_player.utils;


import com.android.music_player.model.user.User;

/**
 * @description 单例管理登陆用户信息
 */
public class UserManager {

  private static UserManager mInstance;
  private User mUser=null;
  /**
   * 双检查模式实现单例模式
   */
  public static  UserManager getInstance(){
    if(mInstance==null){
      synchronized (UserManager.class){
        if (mInstance==null){
          mInstance=new UserManager();
        }
        return mInstance;
      }
    } else {
      return mInstance;
    }
  }

  /**
   * 保存信息到内存
   * @param user
   */
  public void saveUser(User user) {

    this.mUser = user;
    saveLocal(user);
  }

  /**
   * 保存信息到数据库
   * @param user
   */
  private void saveLocal(User user){

  }

  public boolean hasLogined() {

    return mUser == null ? false : true;
  }

  /**
   * 获取用户信息
   */
  public User getUser() {

    return this.mUser;
  }

  /**
   * 从本地获取
   * @return
   */
  public User getLocal(){
    return this.mUser;
  }
  /**
   * remove the user info
   */
  public void removeUser() {

    this.mUser = null;
    removeLocal();
  }
  private void removeLocal(){

  }

}
