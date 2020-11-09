package com.dy.travel;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.dy.travel.bean.LoginUser;
import com.dy.travel.comm.UCApplication;
import com.dy.travel.comm.UCPlugin;
import com.dy.travel.comm.UCService;

@SuppressLint("SetJavaScriptEnabled")
public class RegisterActivity extends Activity {

	public final static String TAG = RegisterActivity.class.getSimpleName();
	private WebView webview;
	private UCPlugin ucPlugin = null;
	private UCApplication ucapplication;
	protected Handler handler = new Handler();
	private SharedPreferences settings;
	private boolean isLogin = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		ucapplication = (UCApplication) this.getApplication();
		ucPlugin = ucapplication.getUCPlugin();
		webview = (WebView) findViewById(R.id.register_web_view);
		webview.getSettings().setJavaScriptEnabled(true);
		WebSettings webSettings = webview.getSettings();
		webSettings.setJavaScriptEnabled(true); // 启用JS脚本
		settings = getSharedPreferences(UCService.SETTING_PREFS_NAME, 0);
		final Dialog pdialog = new Dialog(webview.getContext(), R.style.tip_dialog);
		pdialog.setContentView(R.layout.watting_item);
		pdialog.setCancelable(true);
		webview.setWebViewClient(new WebViewClient() {
			// 当点击链接时,希望覆盖而不是打开新窗口
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url); // 加载新的url
				return true; // 返回true,代表事件已处理,事件流到此终止
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				pdialog.show();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (url.indexOf("/index.do") != -1) {// 用户在首页按返回键时,清楚历史记录
					webview.clearHistory();
				}
				if (pdialog.isShowing()) {
					pdialog.dismiss();
				}
			}
		});
		webview.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {

				final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
				builder.setTitle("提示").setMessage(message).setPositiveButton("确定", null);
				// 不需要绑定按键事件
				builder.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						return true;
					}
				});
				// 禁止响应按back键的事件
				builder.setCancelable(false);
				AlertDialog dialog = builder.create();
				dialog.show();
				result.confirm();// 因为没有绑定事件，需要强行confirm,否则页面会变黑显示不了内容。
				return true;
			}
		});

		webview.addJavascriptInterface(this, "ucapp");
		// 顶部返回按钮的点击事件
		ImageView backImageView = (ImageView) findViewById(R.id.register_top_back);
		if (backImageView != null) {
			backImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onBackPressed();
				}
			});
		}
		webview.loadUrl("file:///android_asset/app/register.html?search=true");

	}

	@JavascriptInterface
	public void gotoHome(String url) {
		Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
		if (url != null && !url.equals("")) {
			intent.putExtra("url", url);
		}
		intent.putExtra("isLogin", isLogin);
		startActivity(intent);
		finish();
	}

	@JavascriptInterface
	public String post(final String callId, final String url, final String params) {
		Log.e(TAG, url + "," + params);
		new Thread() {
			public void run() {
				String result = ucPlugin.post(url, params);
				if (result == null) {
					result = "{}";
				} else {
					try {
						JSONObject json = new JSONObject(result);
						Boolean success = json.optBoolean("success");
						Map<String, List<String>> cookieMap = ucPlugin.getCookieMap();
						if (success) {// 注册成功
							LoginUser user = new LoginUser();
							JSONObject juser = json.optJSONObject("appuser");
							if (juser != null) {
								user.setId(juser.optInt("id"));
								user.setPasswd(juser.optString("password"));
								user.setPhone(juser.optString("username"));
								user.setCookieMap(cookieMap);
								((UCApplication) getApplication()).setLoginUser(user);
								settings.edit().putString("mobile", user.getPhone()).commit();
								isLogin = true;
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					result = result.replace("\\\"", "\\\\\"").replace("\\r\\n", "<br/>").replace("\\n", "<br/>");// .replace("'", "\\'")
				}

				Log.e(TAG, "result=" + result);
				final StringBuilder sb = new StringBuilder("javascript:ucapp.callBack('");
				sb.append(callId).append("','").append(result).append("')");
				Log.e(TAG, "result=" + sb.toString());
				if (webview != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							String json = sb.toString();
							// json = json.replace("\\", "\\\\");
							json = json.replace("\r\n", "<br>");
							json = json.replace("\n", "<br>");
							// Log.e(TAG,"json="+json);
							webview.loadUrl(json);
						}
					});
				}
			}
		}.start();
		return "";
	}


}
