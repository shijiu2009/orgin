package com.dy.travel.comm;

import java.io.File;

import com.dy.travel.BuildConfig;
import com.dy.travel.R;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;


public class UCService extends Service {


	private String TAG = this.getClass().getSimpleName();
	public final static String SETTING_PREFS_NAME = "com.dy.travel.settings";
	public final static String VERSION_ACTION = "com.dy.travel.VERSION_ACTION";
	public final static String SYS_SETTING = "com.dy.travel.SysSetting";//系统设置
	public final static String BAI_DU_MAP_KEY = "nhxB4easOk2mNVac8SKtGcg3pIEzDQU1";
	public final static long LOGIN_OVERTIME = 1000 * 60 * 60 * 24*10l;
	private LocalBinder binder = new LocalBinder();//服务连接
	//通知管理器
	private NotificationManager mNotificationManager;
	public static final int NOTIFY_MSG_ID = 220000;
	public static final int NOTIFY_VERSION_ID = 210000;
	private Context winContext = null;//用于弹出窗使用的上下文
	private Context mContext = this;
	private UCPlugin ucPlugin=null;
	private Uri uri;

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind()");
		return binder;
	}

	public void setWinContext(Context winContext) {
		this.winContext = winContext;
	}

	@Override
	public boolean onUnbind(Intent intent){
		Log.d(TAG, "onUnbind()");
		winContext = null;
		return super.onUnbind(intent);
	}
	@Override
	public void onDestroy(){
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
		unregisterReceiver(baseReceiver);//移除监听广播

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand()");
		if(ucPlugin!=null){
			//TODO 暂不自动提示升级
			//ucPlugin.checkUpdate(true, winContext);
		}
		Log.e(TAG,"onStartCommand...");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
		//注册升级提醒的广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(UCService.VERSION_ACTION);
		ucPlugin = ((UCApplication)getApplication()).getUCPlugin();


		registerReceiver(baseReceiver, filter);
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
	}

	private BroadcastReceiver baseReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive:"+intent.getAction());
			if(intent.getAction().equals(UCService.VERSION_ACTION)){//升级通知广播
				Bundle bundle = intent.getExtras();
				int type = bundle.getInt("type");
				if(type==0){
					String verName = bundle.getString("verName");
					String newVerName = bundle.getString("lastName");
					final String apkFile = "travel.apk";
					String msg = bundle.getString("msg");
					StringBuffer sb = new StringBuffer();
					sb.append("当前版本:").append(verName);
					sb.append("\n新版本:").append(newVerName);
					if(msg!=null){
						sb.append("\n").append("版本更新内容:\n").append(msg);
					}
					sb.append("\n是否更新?");
					//如果在主界面直接通知
					if(winContext!=null){
						new AlertDialog.Builder(winContext)
								.setTitle("软件更新")
								.setMessage(sb.toString())
								.setPositiveButton("更新",// 设置确定按钮
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog,
																int which) {
												Intent intent = new Intent(Intent.ACTION_VIEW);
												intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
												Uri content_url = Uri.parse(mContext.getString(R.string.baseUrl)+"web/download.do");
												intent.setData(content_url);
												startActivity(intent);
//												ucPlugin.updateApk(apkFile);
//												Toast.makeText(winContext,"请稍后,系统正在下载新版本.", Toast.LENGTH_SHORT).show();
											}
										})
								.setNegativeButton("暂不更新",null).show();
					}else{//否则通知栏
						sb = new StringBuffer();
						sb.append("当前版本:").append(verName);
						sb.append("\n新版本:").append(newVerName);
						intent = new Intent(UCService.VERSION_ACTION);
						intent.putExtra("type", 2);//下载
						intent.putExtra("apkFile", apkFile);//下载文件
						PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
						String title = mContext.getString(R.string.app_name)+"有新版本";
						Notification notification = createNotification(title,title
								,sb.toString(),contentIntent);
						mNotificationManager.notify(NOTIFY_VERSION_ID, notification);
					}
				}else if(type==1){
					final String apkFile = bundle.getString("apkFile");
					if (Build.VERSION.SDK_INT >= 24) {//android 7.0以上
						uri = FileProvider.getUriForFile(context, "com.babu.travel.fileprovider", new File(apkFile));
					} else {
						uri = Uri.fromFile(new File(apkFile));
					}
					Log.e("apkFile",apkFile);
					if(apkFile!=null){
						if(winContext!=null){//在主界面时提示用户
							new AlertDialog.Builder(winContext)
									.setTitle("软件更新")
									.setMessage("新版本下载完成,是否立即更新?")
									.setPositiveButton("更新",// 设置确定按钮
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog,
																	int which) {

													Intent intent = new Intent(Intent.ACTION_VIEW);
													intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
													if (Build.VERSION.SDK_INT >= 24) {
														intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
													}
													intent.setDataAndType(uri,
															"application/vnd.android.package-archive");

													intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
													startActivity(intent);

												}
											})
									.setNegativeButton("暂不更新",null).show();
						}else{//否则,直接跳转到更新
							intent = new Intent(Intent.ACTION_VIEW);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.setDataAndType(Uri.fromFile(new File(apkFile)),
									"application/vnd.android.package-archive");
							startActivity(intent);
						}
					}
				}else if(type==2){//接收到通知栏下载的广播
					String apkFile = bundle.getString("apkFile");
					if(apkFile!=null && apkFile.length()>0){
						ucPlugin.updateApk(apkFile);
					}
				}
			}
		}
	};
	public class LocalBinder extends Binder {
		public UCService getService() {
			//用于回调返回服务本身
			return UCService.this;
		}
	}
	/**
	 * 生成通知
	 * @param ticker
	 * @param title
	 * @param contentText
	 * @param contentIntent
	 * @return
	 */
	private Notification createNotification(String ticker,String title,
											String contentText,PendingIntent contentIntent){

		if(contentIntent==null){
			contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
		}

		return new NotificationCompat.Builder(mContext)
				.setTicker(ticker)//状态栏名称
				.setSmallIcon(R.drawable.ic_launcher)//图标
				.setWhen(System.currentTimeMillis())//通知时间
				.setAutoCancel(true)//该通知能被状态栏的清除按钮给清除
				.setContentTitle(title)//下拉显示的标题
				.setContentText(contentText)//下拉显示的内容
				.setContentIntent(contentIntent)
				.build();
	}

}
