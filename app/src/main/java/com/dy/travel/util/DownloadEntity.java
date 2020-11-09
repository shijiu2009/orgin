package com.dy.travel.util;

import android.content.Intent;

public class DownloadEntity {

	private String url;//下载地址URL
	private DownloadManager.DownListener downListener;//下载监听
	private boolean showNotify;//通知栏是否显示进度
	private int notifyId;//通知id号
	private String title;//显示通知时标题
	private String completeTitle;//下载完成的标题
	private String completeContent;//下载完成的文字内容
	private String errorTitle;//下载错误的标题
	private String errorContent;//下载错误的文字内容
	private Intent intent;//下载完成后执行的intent
	private String downPath;//下载路径

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public DownloadManager.DownListener getDownListener() {
		return downListener;
	}
	public void setDownListener(DownloadManager.DownListener downListener) {
		this.downListener = downListener;
	}
	public boolean isShowNotify() {
		return showNotify;
	}
	public void setShowNotify(boolean showNotify) {
		this.showNotify = showNotify;
	}
	public int getNotifyId() {
		return notifyId;
	}
	public void setNotifyId(int notifyId) {
		this.notifyId = notifyId;
	}
	public String getTitle() {
		return title==null?"正在下载...":title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCompleteTitle() {
		return completeTitle==null?"下载完成!":completeTitle;
	}
	public void setCompleteTitle(String completeTitle) {
		this.completeTitle = completeTitle;
	}
	public String getCompleteContent() {
		return completeContent==null?"下载完成点击安装":completeContent;
	}
	public void setCompleteContent(String completeContent) {
		this.completeContent = completeContent;
	}
	public String getErrorTitle() {
		return errorTitle==null?"下载失败!":errorTitle;
	}
	public void setErrorTitle(String errorTitle) {
		this.errorTitle = errorTitle;
	}
	public String getErrorContent() {
		return errorContent==null?"下载失败,请稍后再试！":errorContent;
	}
	public void setErrorContent(String errorContent) {
		this.errorContent = errorContent;
	}
	public Intent getIntent() {
		return intent;
	}
	public void setIntent(Intent intent) {
		this.intent = intent;
	}
	public String getDownPath() {
		return downPath;
	}
	public void setDownPath(String downPath) {
		this.downPath = downPath;
	}

}
