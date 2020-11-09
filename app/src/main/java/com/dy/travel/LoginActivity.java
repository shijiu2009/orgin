package com.dy.travel;

import java.util.List;
import java.util.Map;

import org.apache.cordova.LOG;
import org.json.JSONException;
import org.json.JSONObject;

import com.dy.travel.comm.MD5Util;
import com.dy.travel.comm.UCApplication;
import com.dy.travel.comm.UCPlugin;
import com.dy.travel.comm.UCService;
import com.dy.travel.util.ComUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {


	//	private CheckBox checkBox;
	protected Handler handler = new Handler();
	private UCPlugin ucPlugin = null;
	private SharedPreferences settings;
	protected MainActivity mainActivity;
	private Handler mHandler = new Handler();
	private UCApplication ucapplication;
	private String url;
	private static final String TAG = LoginActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		ucapplication = (UCApplication) this.getApplication();
		ucPlugin = ucapplication.getUCPlugin();
//		checkBox = (CheckBox) findViewById(R.id.remember_me);
		final EditText mobileEditText = (EditText) findViewById(R.id.edit_login_mobile);// 电话号码
		final EditText passwordEditText = (EditText) findViewById(R.id.edit_login_password);// 密码
//		settings = getSharedPreferences(UCService.SETTING_PREFS_NAME, 0);
//		final String username = settings.getString("username", "");
//		final String password = settings.getString("password", "");
//		boolean rememberMe = settings.getBoolean("rememberMe", false);


		Bundle bundle = getIntent().getExtras();
		if(bundle!=null){
			url = bundle.getString("url");
		}
//		mobileEditText.setText(username);
//		passwordEditText.setText(password);
//		checkBox.setChecked(rememberMe);

		// 监听登录按钮
		Button loginButton = (Button) findViewById(R.id.button_login);
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mobileEditText.getText().length() > 0 && passwordEditText.getText().length() > 0) {
					String Password = passwordEditText.getText().toString();
					Password = MD5Util.MD5(Password);
					Password = MD5Util.MD5(Password);
					login(mobileEditText.getText().toString(), Password);
				} else {
					new AlertDialog.Builder(LoginActivity.this).setTitle("错误").setMessage("请输入用户名和密码!").setPositiveButton("确定", null).show();
				}
			}
		});

		// 监听注册按钮
		Button registerButton = (Button) findViewById(R.id.button_register);
		registerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO 用户注册
				Intent i = new Intent(LoginActivity.this,WebViewActivity.class);
				i.putExtra("url","file:///android_asset/app/register/register.html");
				startActivity(i);
			}
		});

		// 监听返回按钮
		ImageView returnLogo = (ImageView) findViewById(R.id.imageView_top_back);// 返回按钮
		returnLogo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				goBack();
			}
		});
		//监听忘记密码
		TextView forgot = (TextView) findViewById(R.id.forget);
		forgot.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {// 跳转到邮箱验证页面
				Intent i = new Intent(LoginActivity.this,WebViewActivity.class);
				i.putExtra("url","file:///android_asset/app/register/forget.html");
				startActivity(i);
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@JavascriptInterface
	public void goBack() {
		Log.e(TAG, "goBack()...");
		handler.post(new Runnable() {
			@Override
			public void run() {
				// 返回，踢出队列
				Intent i = new Intent(LoginActivity.this,MainActivity.class);
				i.putExtra("loginback", true);
				startActivity(i);
//					if(ComUtil.isEmpty(isMain)){
////						ucapplication.getScreenManager().popActivity(LoginActivity.this);
//					}
				finish();

			}
		});
	}

	/**
	 * 显示提示信息
	 */
	@JavascriptInterface
	public void showTip(final String tip) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(LoginActivity.this, tip, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 登录
	 *
	 * @param username
	 * @param password
	 * @return
	 */
	private void login(final String username, final String password) {

		final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
		pd.setTitle("正在登录");
		pd.show();
		new Thread() {
			@Override
			public void run() {
				String result = ucPlugin.login(username, password);
				LOG.e("Login", result);
				Map<String, List<String>> cookieMap = ucPlugin.getCookieMap();
				pd.dismiss();
				if (result != null) {
					int overtime = 0;
					String msg = null;
					boolean success = false;
					try {
						JSONObject json = new JSONObject(result);
						success = json.optBoolean("success");
						if (success) {// 登录成功
							//保存token到本地
//							String token = json.optString("token");
//							if(!ComUtil.isEmpty(token)){
								settings = getSharedPreferences(UCService.SETTING_PREFS_NAME, 0);
								Editor userInfo = settings.edit();
//								userInfo.putString("token", token);
								userInfo.putString("username", username);
								userInfo.putString("password", password);
								userInfo.putLong("createDate", System.currentTimeMillis());
								userInfo.commit();
//							}
						} else {
							msg = json.optString("msg");
							if (msg == null) {
								msg = "登录失败!";
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
						msg = "登录发生异常";
					}
					// 登录成功
					if (success) {
						if(url != null&&url.length()>0&&!"undefined".equals(url)){
							if(url.indexOf("tab=1") == -1){
								Intent t = new Intent(LoginActivity.this, WebViewActivity.class);
								t.putExtra("url", url);
								startActivity(t);
							}else{
								Intent t = new Intent(LoginActivity.this, MainActivity.class);
								t.putExtra("refurbishUserInfo", true);
								startActivity(t);
							}
						}
						finish();
					} else {
						final String amsg = msg;
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								new AlertDialog.Builder(LoginActivity.this).setTitle("登录失败").setMessage(amsg).setPositiveButton("确定", null).show();
							}
						});
					}
				} else {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							new AlertDialog.Builder(LoginActivity.this).setTitle("登录失败").setMessage("网络或服务器异常").setPositiveButton("确定", null).show();
						}
					});
				}
			}
		}.start();
	}

}
