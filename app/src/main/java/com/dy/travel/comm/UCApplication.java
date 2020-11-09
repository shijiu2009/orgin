package com.dy.travel.comm;


import android.app.Application;
import android.util.Log;

import com.dy.travel.bean.LoginUser;
import com.dy.travel.util.ScreenManager;

public class UCApplication extends Application {

	private final static String TAG = UCApplication.class.getSimpleName();
	private ScreenManager screenManager = null;
	private UCPlugin ucPlugin;//网络访问类
	private LoginUser loginUser;//登录用户
	private String taburl;

	public UCPlugin getUCPlugin(){

		return ucPlugin;
	}


	@Override
	public void onCreate(){
		super.onCreate();
		Log.e(TAG,"onCreate...");
		ucPlugin = new UCPlugin(this);
		//初始化自定义Activity管理器
		screenManager = ScreenManager.getScreenManager();
	}

	public void setUCPlugin(UCPlugin ucPlugin) {
		this.ucPlugin = ucPlugin;
	}


	public LoginUser getLoginUser() {
		return loginUser;
	}


	public void setLoginUser(LoginUser loginUser) {
		this.loginUser = loginUser;
	}


	public UCPlugin getUcPlugin() {
		return ucPlugin;
	}


	public String getTaburl() {
		return taburl;
	}


	public void setTaburl(String taburl) {
		this.taburl = taburl;
	}


	public ScreenManager getScreenManager() {
		return screenManager;
	}




}