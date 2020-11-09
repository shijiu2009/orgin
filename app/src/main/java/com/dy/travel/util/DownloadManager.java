package com.dy.travel.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.dy.travel.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;


public class DownloadManager {

	private Map<String,DownloadEntity> downMap;//正在下载的任务
	private Context mContext = null;
	private Handler mHandler = null;

	public DownloadManager(Context context){
		mContext = context;
		downMap = new HashMap<String,DownloadEntity>();
		mHandler = new Handler(mContext.getMainLooper());
	}

	public void downFile(final DownloadEntity entity){

		final String url = entity.getUrl();
		if(url!=null){
			int start = url.lastIndexOf("/");
			if(start!=-1 && downMap.get(url)==null){
				System.out.println("url="+url);
				downMap.put(url, entity);
				final String fileName = url.substring(start);
				new Thread(){
					@Override
					public void run(){
						File sdDir = Environment.getExternalStorageDirectory();
						final File pathDir = new File(sdDir,entity.getDownPath());
						if(!pathDir.exists()){
							pathDir.mkdirs();
						}
						final File file = new File(pathDir.getPath(),fileName);
						if(file.exists()){
							//文件存在，先删除
							file.delete();
						}
						//在通知栏显示下载进度
						PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
						final RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.down_item);
						remoteViews.setProgressBar(R.id.progressBar_down_item, 100, 0, false);
						final Notification notification = new NotificationCompat.Builder(mContext)
								.setTicker(entity.getTitle())//状态栏名字
								.setSmallIcon(R.drawable.ic_launcher)//图标
								.setWhen(System.currentTimeMillis())//通知时间
								//.setAutoCancel(false)//该通知能被状态栏的清除按钮给清除掉
								.setContent(remoteViews)
								.setContentTitle(entity.getTitle())//下拉显示的标题
								//.setContentText(content)//下拉显示的内容
								.setContentIntent(contentIntent)//针对2.x系列,示意图必须设置有一个值
								.build();
						notification.flags = Notification.FLAG_NO_CLEAR;//该通知不能被清除
						final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
						if(entity.isShowNotify()){
							notificationManager.notify(entity.getNotifyId(), notification);
						}

						HttpBase httpBase = new HttpBase(url);
						//设置监听
						httpBase.setListen(new HttpBase.DownListener(){

							@Override
							public void progress(long totalByte, long downByte) {
								if(entity.isShowNotify()){
									final int per = (int)(downByte*1.0/totalByte*100);
									mHandler.post(new Runnable(){
										@Override
										public void run() {
											remoteViews.setTextViewText(R.id.textView_down_item, "已下载("+per+"%)");
											remoteViews.setProgressBar(R.id.progressBar_down_item, 100, per, false);
											//必须重新设置一下contentView,否则2.x无法显示
											notification.contentView = remoteViews;
											//还要重新通知,否则无法更新
											notificationManager.notify(entity.getNotifyId(), notification);
										}
									});
								}
								if(entity.getDownListener()!=null){
									entity.getDownListener().progress(entity,totalByte, downByte);
								}
							}
							@Override
							public void onError(int err) {
								if(entity.isShowNotify()){
									mHandler.post(new Runnable(){
										@Override
										public void run() {
											notification.tickerText = entity.getErrorTitle();
											remoteViews.setTextViewText(R.id.textView_down_item, entity.getErrorContent());
											remoteViews.setViewVisibility(R.id.progressBar_down_item, View.GONE);
											//remoteViews.setProgressBar(R.id.progressBar_down_item, 100, 100, false);
											//必须重新设置一下contentView,否则2.x无法显示
											notification.flags = Notification.FLAG_AUTO_CANCEL;//可取消
											notification.contentView = remoteViews;
											//还要重新通知,否则无法更新
											notificationManager.notify(entity.getNotifyId(), notification);
										}
									});
								}
								//移除下载任务
								downMap.remove(url);
								if(entity.getDownListener()!=null){
									entity.getDownListener().onError(entity,err);
								}
							}
							@Override
							public void onComplete() {
								if(entity.isShowNotify()){
									notification.tickerText = entity.getCompleteTitle();
									Intent intent = null;

									if(entity.getDownListener()!=null){
										intent = entity.getDownListener().getCompleteIntent(entity, pathDir, fileName);
									}

									if(intent!=null){
										PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
										notification.contentIntent = contentIntent;
									}else{
										PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
										notification.contentIntent = contentIntent;
									}

									mHandler.post(new Runnable(){
										@Override
										public void run() {
											remoteViews.setTextViewText(R.id.textView_down_item, entity.getCompleteContent());
											remoteViews.setProgressBar(R.id.progressBar_down_item, 100, 100, false);
											//必须重新设置一下contentView,否则2.x无法显示
											notification.flags = Notification.FLAG_AUTO_CANCEL;//可取消
											notification.contentView = remoteViews;
											//还要重新通知,否则无法更新
											notificationManager.notify(entity.getNotifyId(), notification);
										}
									});
								}
								//移除下载任务
								downMap.remove(url);
								if(entity.getDownListener()!=null){
									entity.getDownListener().onComplete(entity,pathDir,fileName);
								}
							}
							@Override
							public void onCancel() {
								//移除下载任务
								downMap.remove(url);
								if(entity.getDownListener()!=null){
									entity.getDownListener().onCancel(entity);
								}
							}
						});
						//开始下载
						httpBase.getFileAndSave(file.getPath(),0);
					}
				}.start();
			}
		}
	}

	public interface DownListener{

		public void progress(DownloadEntity entity,long totalByte, long downByte);
		public void onError(DownloadEntity entity,int err);
		/**
		 * 下载完成前调用
		 * @param entity
		 * @param pathDir
		 * @param fileName
		 * @return
		 */
		public Intent getCompleteIntent(DownloadEntity entity,File pathDir,String fileName);
		public void onComplete(DownloadEntity entity,File pathDir,String fileName);
		public void onCancel(DownloadEntity entity);
	}

}
