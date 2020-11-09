package com.dy.travel.comm;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.dy.travel.R;
import com.dy.travel.util.HttpBase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;


public class UCPlugin {



	private static final String TAG = UCPlugin.class.getSimpleName();
	private Context mContext;
	// cookie
	private Map<String, List<String>> cookieMap = null;

	private final String charSet = "UTF-8";
	private String baseUrl = null;// ip+port+project
	private String hostUrl = null;// ip+port
	private String verUrl = null;// 检查版本
	private String loginUrl = null;// 登录
	private String autoLoginUrl = null;//自动登录

	private static final String VERSION_LAST_UPDATE_TIME = "com.dy.travel.VERSION_LAST_UPDATE_TIME";

	public UCPlugin(Context context) {
		this.mContext = context;
		initUrl();
	}

	private void initUrl() {
		baseUrl = mContext.getString(R.string.baseUrl);
		hostUrl = mContext.getString(R.string.hostUrl);
		loginUrl = baseUrl + mContext.getString(R.string.loginUrl);
		//autoLoginUrl = baseUrl + mContext.getString(R.string.autoLoginUrl);
		verUrl = baseUrl + mContext.getString(R.string.verUrl);
	}

	public Map<String, List<String>> getCookieMap() {
		return cookieMap;
	}

	public void setCookieMap(Map<String, List<String>> cookieMap) {
		this.cookieMap = cookieMap;
	}

	/**
	 * 返回基本URL
	 *
	 * @return
	 */
	public String getBaseUrl() {
		return this.baseUrl;
	}

	public String getVerUrl() {
		return this.verUrl;
	}

	public String getLastVersion() {
		String result = null;
		HttpBase httpBase = new HttpBase(verUrl);
		httpBase.setCharset("UTF-8");
		httpBase.get();
		if (httpBase.getResponseCode() == 200) {
			result = httpBase.getResponseText();
		}
		Log.e(TAG, "LastVersion:" + result);
		return result;

	}

	private String getToken() {
		SharedPreferences settings = mContext.getSharedPreferences(UCService.SETTING_PREFS_NAME, 0);
		String token = settings.getString("token", null);
		return token;
	}




	private Handler mHandler = null;

	/**
	 * 升级文件
	 */
	public void updateApk(final String apkName) {
		if (mHandler == null) {
			mHandler = new Handler(mContext.getMainLooper());
		}
		if (apkName != null) {
			new Thread() {
				@Override
				public void run() {
					// 先查询是否已经下载有升级文件
					File sdDir = Environment.getExternalStorageDirectory();

					final File pathDir = new File(sdDir, ".ucdata/canteen");
					if (!pathDir.exists()) {
						if (pathDir.mkdirs()) {
							// Log.d("ucapp", "mkdir chaceDir success");
						} else {
							Log.d("ucapp", "mkdir chaceDir failure");
						}
					}
					final File file = new File(pathDir.getPath(), apkName);
					if (file.exists()) {
						// 文件存在，先删除
						file.delete();
					}
					// if(!file.exists()){
					// }else{
					// //文件存在,发送广播,提示安装
					// Log.d(MainActivity.TAG, "new verson file exists!this="+this+",file="+file.getPath());
					// Intent intent = new Intent();
					// intent.setAction(MobileButlerService.SERV_ACTION);
					// intent.putExtra("type", 1);//1为文件已下载,更新。
					// intent.putExtra("apkFile", file.getPath());
					// sendBroadcast(intent);
					// }
					// 在通知栏显示下载进度
					PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
					final RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.down_item);
					remoteViews.setProgressBar(R.id.progressBar_down_item, 100, 0, false);
					final Notification notification = new NotificationCompat.Builder(mContext).setTicker("正在下载...")// 状态栏名字
							.setSmallIcon(R.drawable.ic_launcher)// 图标
							.setWhen(System.currentTimeMillis())// 通知时间
							// .setAutoCancel(false)//该通知能被状态栏的清除按钮给清除掉
							.setContent(remoteViews).setContentTitle(mContext.getString(R.string.down_item_tip))// 下拉显示的标题
							// .setContentText(content)//下拉显示的内容
							.setContentIntent(contentIntent)// 针对2.x系列,示意图必须设置有一个值
							.build();
					notification.flags = Notification.FLAG_NO_CLEAR;// 该通知不能被清楚
					final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
					notificationManager.notify(UCService.NOTIFY_VERSION_ID + 300, notification);

					// 文件不存在,下载
					String appUrl = getBaseUrl() + apkName;
					HttpBase httpBase = new HttpBase(appUrl);
					httpBase.setListen(new HttpBase.DownListener() {
						@Override
						public void progress(long totalByte, long downByte) {
							final int per = (int) (downByte * 1.0 / totalByte * 100);
							// Log.d(MainActivity.TAG, "down file ing...,per="+per+",downByte="+downByte+",totalByte="+totalByte+",downByte/totalByte="+(downByte/totalByte));
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									remoteViews.setTextViewText(R.id.textView_down_item, mContext.getString(R.string.down_item_tip) + "(" + per + "%)");
									remoteViews.setProgressBar(R.id.progressBar_down_item, 100, per, false);
									// 必须重新设置一下contentView,否则2.x无法显示
									notification.contentView = remoteViews;
									// 还要重新通知,否则无法更新
									notificationManager.notify(UCService.NOTIFY_VERSION_ID + 300, notification);
								}
							});
						}

						@Override
						public void onError(int err) {
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									notification.tickerText = "下载失败!";
									remoteViews.setTextViewText(R.id.textView_down_item, "更新下载失败,请稍后再试！");
									remoteViews.setViewVisibility(R.id.progressBar_down_item, View.GONE);
									// remoteViews.setProgressBar(R.id.progressBar_down_item, 100, 100, false);
									// 必须重新设置一下contentView,否则2.x无法显示
									notification.flags = Notification.FLAG_AUTO_CANCEL;// 可取消
									notification.contentView = remoteViews;
									// 还要重新通知,否则无法更新
									notificationManager.notify(UCService.NOTIFY_VERSION_ID + 300, notification);
								}
							});
							Intent intent = new Intent();
							intent.setAction(UCService.VERSION_ACTION);
							intent.putExtra("type", -1);// -1为下载文件失败
							mContext.sendBroadcast(intent);
						}

						@Override
						public void onComplete() {
							Log.d(TAG, "down new verson file complete!this=" + this);
							notification.tickerText = "下载完成!";
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(new File(pathDir.getPath(), apkName)), "application/vnd.android.package-archive");
							PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
							notification.contentIntent = contentIntent;
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									remoteViews.setTextViewText(R.id.textView_down_item, "下载完成点击安装");
									remoteViews.setProgressBar(R.id.progressBar_down_item, 100, 100, false);
									// 必须重新设置一下contentView,否则2.x无法显示
									notification.flags = Notification.FLAG_AUTO_CANCEL;// 可取消
									notification.contentView = remoteViews;
									// 还要重新通知,否则无法更新
									notificationManager.notify(UCService.NOTIFY_VERSION_ID + 300, notification);
								}
							});
							// 下载完成,发送广播,提示安装
							intent = new Intent();
							intent.setAction(UCService.VERSION_ACTION);
							intent.putExtra("type", 1);// 1为文件已下载成功,提示更新。
							intent.putExtra("apkFile", new File(pathDir.getPath(), apkName).getPath());
							mContext.sendBroadcast(intent);
						}

						@Override
						public void onCancel() {
						}
					});
					httpBase.getFileAndSave(file.getPath(), 0);
				}
			}.start();
		}

	}

	/**
	 * 检查软件版本是否有更新
	 */
	public void checkUpdate(final boolean autoCheck, final Context winContext) {
		try {
			final SharedPreferences settings = mContext.getSharedPreferences(VERSION_LAST_UPDATE_TIME, 0);
			// long lastTime = settings.getLong("checkTime", -1);//获取上次检查时间
			final long curTime = System.currentTimeMillis();// 当前时间
			// if(!autoCheck || (curTime-lastTime)> 60* 60 * 1000*3){//上次提醒3小时内的不重复提醒
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			if (packageInfo != null) {
				final int versionCode = packageInfo.versionCode;
				final String verName = packageInfo.versionName;
				new Thread() {
					@Override
					public void run() {
						String result = getLastVersion();
						if (result != null) {
							result = result.replace("\n", "").replace("\r", "");
							final String tmps[] = result.split("\\|");
							if (tmps.length >= 3) {
								int lastCode = 0;
								try {
									lastCode = Integer.parseInt(tmps[0]);
								} catch (Exception e) {
									Log.e(TAG, null, e);
								}
								final String lastName = tmps[1];
								final String apkFile = tmps[2];
								if (versionCode < lastCode) {// 有可升级版本,发送可升级广播
									Intent intent = new Intent();
									intent.setAction(UCService.VERSION_ACTION);
									intent.putExtra("type", 0);// 0为有可用升级
									intent.putExtra("verName", verName);
									intent.putExtra("lastName", lastName);
									intent.putExtra("apkFile", apkFile);
									if (tmps.length > 3) {
										intent.putExtra("msg", tmps[3].replace("\\n", "\n"));
									}
									// 更新本次提醒时间
									SharedPreferences.Editor editor = settings.edit();
									editor.putLong("checkTime", curTime);
									editor.commit();
									mContext.sendBroadcast(intent);
									Log.e(TAG, "sendBroadcast new version!");
								} else if (winContext != null && !autoCheck) {
									Handler handler = new Handler(winContext.getMainLooper());
									handler.post(new Runnable() {
										@Override
										public void run() {
											Toast.makeText(winContext, "当前已经是最新版本", Toast.LENGTH_SHORT).show();
										}
									});
								}
							}
						}
					}
				}.start();
			}

		} catch (NameNotFoundException e) {
			Log.d(TAG, "checkUpdate exception:" + e.getMessage());
		}
	}

	/**
	 * post 请求
	 * @param url 请求路径
	 * @param params 参数
	 */
	public String post(String url, String params) {

		String result = null;
		if (url.indexOf("http://") == -1 || url.indexOf("https://") == -1) {
			url = hostUrl + url;
		}
		HttpBase httpBase = new HttpBase(url);
		httpBase.setCharset(charSet);
		if (cookieMap != null) {
			httpBase.setCookie(cookieMap);
		}
		if (params != null && params.startsWith("{") && params.endsWith("}")) {
			try {
				JSONObject objJson = new JSONObject(params);
				if (objJson != null && objJson.length() > 0) {
					Iterator<String> it = objJson.keys();
					String key = null;
					while (it.hasNext()) {
						key = it.next();
						httpBase.addParam(key, objJson.optString(key));
					}
				}
			} catch (JSONException e) {
				Log.e(TAG, "请求:" + url + "时参数发生错误：" + e.getMessage());
			}
		}

		//获取token,如果有，添加到hearder里
		String token = getToken();
		if(token!=null){
			httpBase.addHander("token",token);
		}

		httpBase.post();
		int errCount = 0;
		Log.e(TAG, "请求:" + url + ",ResponseCode：" + httpBase.getResponseCode());
		while (httpBase.getResponseCode() != 200 && errCount < 3) {
			httpBase.post();
			errCount++;
			Log.e(TAG, "请求:" + url + ",ResponseCode：" + httpBase.getResponseCode());
		}

		if (httpBase.getResponseCode() == 200) {
			result = httpBase.getResponseText();
		}
		Log.e(TAG, "请求:" + url + ",result：" + result);
		cookieMap = httpBase.getCookies();
//		if (cookieMap != null && cookieMap.size() > 1) {// 保持jsessionid是最新的那个(截取)
//			List<String> path = cookieMap.get(" Path");
//			List<String> jsessionId = cookieMap.get("JSESSIONID");
//			String lastElement4Path = path.get(path.size() - 1);
//			String lastElement4JS = jsessionId.get(jsessionId.size() - 1);
//			path.clear();
//			jsessionId.clear();
//			jsessionId.add(lastElement4JS);
//			path.add(lastElement4Path);
//			cookieMap.put(" Path", path);
//			cookieMap.put("JSESSIONID", jsessionId);
//		}



		return result;
	}

	/**
	 * 登录
	 * @param mobile
	 * @param password
	 * @return
	 */
	public String login(String mobile, String password) {
		String result = null;
		HttpBase httpBase = new HttpBase(loginUrl);
		httpBase.setCharset(charSet);
		// httpBase.setCookie(cookieMap);
		httpBase.addParam("username", mobile);
		httpBase.addParam("password", password);
		httpBase.post();
		int errCount = 0;
		Log.e(TAG,"loginUrl:"+loginUrl);
		Log.e(TAG,"ResponseCode:"+httpBase.getResponseCode());
		while (httpBase.getResponseCode() != 200 && errCount < 3) {
			httpBase.post();
			errCount++;
		}
		if (httpBase.getResponseCode() == 200) {
			result = httpBase.getResponseText();
		}
		cookieMap = httpBase.getCookies();
		return result;
	}
	
	/**
	 * 自动登录
	 * @param token
	 * @return
	 */
	public String autoLogin(String token) {
		String result = null;
		HttpBase httpBase = new HttpBase(autoLoginUrl);
		httpBase.setCharset(charSet);
		httpBase.addParam("token", token);
		httpBase.post();
		int errCount = 0;
		while (httpBase.getResponseCode() != 200 && errCount < 3) {
			httpBase.post();
			errCount++;
		}
		if (httpBase.getResponseCode() == 200) {
			result = httpBase.getResponseText();
		}
		return result;
	}

}
