package com.dy.travel.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ComUtil {



	public static boolean isConnect(Context context) {
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		boolean isConnect = false;
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				// 获取网络连接管理的对象
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null&& info.isConnected()) {
					// 判断当前网络是否已经连接
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						isConnect = true;
					}
				}
			}
		} catch (Exception e) {
			Log.v("error",e.toString());
		}
		return isConnect;
	}

	/**
	 * 字符是否为空
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str){
		if(str==null || str.length()==0){
			return true;
		}else{
			return false;
		}
	}

	public static double getDouble(String str) {
		double d = 0;
		if (str != null) {
			try {
				d = Double.parseDouble(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return d;
	}

}
	