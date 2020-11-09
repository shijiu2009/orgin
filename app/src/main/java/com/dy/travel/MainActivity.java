package com.dy.travel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.baidu.navisdk.adapter.struct.BNTTsInitConfig;
import com.dy.travel.comm.Constant;
import com.dy.travel.comm.Constants;
import com.dy.travel.comm.MD5;
import com.dy.travel.comm.UCApplication;
import com.dy.travel.comm.UCPlugin;
import com.dy.travel.comm.UCService;
import com.dy.travel.util.NormalUtils;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends FragmentActivity implements CordovaInterface {

	public final static String TAG = MainActivity.class.getSimpleName();

	protected Handler handler = new Handler();
	private LinearLayout footerLinearLayout;
	private boolean isExit = false;
	private UCService ucService;// 服务
	private SharedPreferences settings;
	private UCApplication ucapplication;
	private UCPlugin ucPlugin = null;
	private int tabIndex = 0;
	private boolean mBound = false;// 是否连接有服务连接器
	private WebViewFragment webViewFragment;
	private Fragment curHomeFragment;
	private RadioGroup tabRadioGroup;
	private RadioButton homeRadioButton;
	private RadioButton bookingRadioButton;
	private RadioButton persCenterRadioButton;
	private Drawable homeDefaultDraw;
	private Drawable informationDefaultDraw;
	private Drawable userDefaultDraw;

	private String url = null;
	private String tab = null;
	private String password = null;
	private String userName = null;
	private long createDate = 0l;
	private IWXAPI msgApi = null;
	private PayReq req;

	private String mSDCardPath = null;
	private String APP_FOLDER_NAME = "travel";
	private static final String[] authBaseArr = {
			android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
			android.Manifest.permission.ACCESS_FINE_LOCATION,
			android.Manifest.permission.CAMERA
	};
	private static final int authBaseRequestCode = 1;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//解决安卓7.0以上版本cordova拍照问题
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
		builder.detectFileUriExposure();
		
		setContentView(R.layout.main);
		// 底部菜单
		footerLinearLayout = (LinearLayout) this.findViewById(R.id.linearLayout_footer);
		Log.d(TAG, "onCreate(),this=" + this);
		ucapplication = (UCApplication) this.getApplication();
		ucPlugin = ucapplication.getUCPlugin();
		settings = getSharedPreferences(UCService.SETTING_PREFS_NAME, 0);
		createDate = settings.getLong("createDate", 2l);
		Long loginDate = System.currentTimeMillis();
		if((loginDate-createDate) > UCService.LOGIN_OVERTIME) {
			settings.edit().clear();
		}
		password = settings.getString("password", "");
		userName = settings.getString("username", "");
		//settings.edit().clear();
		Log.e("userName",userName);


		msgApi = WXAPIFactory.createWXAPI(this, Constant.WX_APP_ID);
		msgApi.registerApp(Constant.WX_APP_ID);

		// 调转到第一个页面
		final FragmentManager fm = this.getSupportFragmentManager();
		webViewFragment = new WebViewFragment();
		webViewFragment.setMainActivity(this);
		curHomeFragment = webViewFragment;
		String tab="file:///android_asset/app/indexs.html?root=true&tab=1";
		if (!webViewFragment.isAdded()) {
			fm.beginTransaction().add(R.id.fragment_container, webViewFragment,tab).commit();
		}
		createButton();

		autoLogin();

		initPush();
//		if(initDirs()){
//			initNavi();
//		}
		requestPermission();

	}

	private void requestPermission(){
		//申请权限
		if (android.os.Build.VERSION.SDK_INT >= 23) {
			if (!hasBasePhoneAuth()) {
				this.requestPermissions(authBaseArr, authBaseRequestCode);
			}
		}
	}

	private void initPush(){
		XGPushConfig.enableDebug(this,true);
		XGPushManager.registerPush(this, new XGIOperateCallback() {
			@Override
			public void onSuccess(Object data, int flag) {
				//token在设备卸载重装的时候有可能会变
				Log.d("TPush", "注册成功，设备token为：" + data);
			}

			@Override
			public void onFail(Object data, int errCode, String msg) {
				Log.d("TPush", "注册失败，错误码：" + errCode + ",错误信息：" + msg);
			}
		});
	}

	//初始化导航目录
	private boolean initDirs() {
		mSDCardPath = getSdcardDir();
		if (mSDCardPath == null) {
			return false;
		}
		Log.e("path",mSDCardPath);
		File f = new File(mSDCardPath, "");
		if (!f.exists()) {
			try {
				f.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	private void initNavi() {
		//申请权限
		if (android.os.Build.VERSION.SDK_INT >= 23) {
			if (!hasBasePhoneAuth()) {
				this.requestPermissions(authBaseArr, authBaseRequestCode);
				return;
			}
		}

		if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
			return;
		}

		// 初始化导航引擎
		BaiduNaviManagerFactory.getBaiduNaviManager().init(this,
				mSDCardPath, APP_FOLDER_NAME, new IBaiduNaviManager.INaviInitListener() {

					@Override
					public void onAuthResult(int status, String msg) {
						String result;
						if (0 == status) {
							result = "key校验成功!";
							Log.e("key","成功");
						} else {
							result = "key校验失败, " + msg;
							Log.e("key","失败");
						}
						Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
					}

					@Override
					public void initStart() {
						Toast.makeText(MainActivity.this.getApplicationContext(),
								"百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
						Log.e("map","开始");
					}

					@Override
					public void initSuccess() {
						Toast.makeText(MainActivity.this.getApplicationContext(),
								"百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
						Log.e("map","成功");
						// 初始化tts
						initTTS();
					}

					@Override
					public void initFailed(int errCode) {
						Toast.makeText(MainActivity.this.getApplicationContext(),
								"百度导航引擎初始化失败 " + errCode, Toast.LENGTH_SHORT).show();
						Log.e("map","百度导航引擎初始化失败 " + errCode);
					}
				});
	}

	private void initTTS() {
		// 使用内置TTS
		BNTTsInitConfig config = new BNTTsInitConfig.Builder()
				.context(getApplicationContext())
				.sdcardRootPath(getSdcardDir())
				.appFolderName(APP_FOLDER_NAME)
				.appId(NormalUtils.getTTSAppID())
				.appKey(NormalUtils.getTTSAppKey())
				.secretKey(NormalUtils.getTTSsecretKey())
				.build();
		BaiduNaviManagerFactory.getTTSManager().initTTS(config);
	}

	private boolean hasBasePhoneAuth() {
		PackageManager pm = this.getPackageManager();
		for (String auth : authBaseArr) {
			if (pm.checkPermission(auth, this.getPackageName()) != PackageManager
					.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	private void createButton(){

		tabRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_tab);
		homeRadioButton = (RadioButton) this.findViewById(R.id.radioButton_home);
		bookingRadioButton = (RadioButton) this.findViewById(R.id.radioButton_booking);
		persCenterRadioButton = (RadioButton) this.findViewById(R.id.radioButton_persCenter);


		final int px32 = getResources().getDimensionPixelSize(R.dimen.px32);
		final int px5 = getResources().getDimensionPixelSize(R.dimen.px2);

		homeDefaultDraw = getResources().getDrawable(R.drawable.n1);
		informationDefaultDraw = getResources().getDrawable(R.drawable.n2);
		userDefaultDraw = getResources().getDrawable(R.drawable.n3);
		homeDefaultDraw.setBounds(0, px5,px32,px32);
		informationDefaultDraw.setBounds(0, px5,px32,px32);
		userDefaultDraw.setBounds(0, px5,px32,px32);


		homeRadioButton.setCompoundDrawables(null, homeDefaultDraw, null, null);
		homeRadioButton.setCompoundDrawablePadding(px5);
		bookingRadioButton.setCompoundDrawables(null, informationDefaultDraw, null, null);
		bookingRadioButton.setCompoundDrawablePadding(px5);
		persCenterRadioButton.setCompoundDrawables(null, userDefaultDraw, null, null);
		persCenterRadioButton.setCompoundDrawablePadding(px5);

		tabRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			String tab = null;
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton selectedRadioButton = null;
				if (homeRadioButton.getId() == checkedId) {
					selectedRadioButton = homeRadioButton;
					tabIndex = 1;
					tab="file:///android_asset/app/indexs.html?root=true&tab=1";
					startFragment(tab);
//					webViewFragment.goHome();
				} else if (bookingRadioButton.getId() == checkedId) {
					selectedRadioButton = bookingRadioButton;
					tabIndex = 2;
					tab="file:///android_asset/app/fuwu/index.html";
					startFragment(tab);
//					webViewFragment.goBooking();
				} else if (persCenterRadioButton.getId() == checkedId) {
					selectedRadioButton = persCenterRadioButton;
					tabIndex = 3;
					selectedRadioButton.setOnClickListener(null);
					tab="file:///android_asset/app/my/index.html?root=true&tab=1";
					startFragment(tab);
//					webViewFragment.goPersCenter();

				}
				if (selectedRadioButton != null) {
					selectedRadioButton.setChecked(false);
				}
			}
		});
	}

	public void startFragment(String tab){
		FragmentManager fm = this.getSupportFragmentManager();
		Fragment oldFragment = fm.findFragmentByTag(tab);
		if(oldFragment==null){
			WebViewFragment newFragment = new WebViewFragment();
			newFragment.setMainActivity(this);
			newFragment.setUrl(tab);
			if(!newFragment.isAdded()){
				fm.beginTransaction().add(R.id.fragment_container, newFragment,tab)
						.hide(curHomeFragment).show(newFragment).commit();
			}
			curHomeFragment = newFragment;
		}else{
			if(curHomeFragment!=oldFragment){
				Log.d(TAG, "hide="+curHomeFragment+",show="+oldFragment);
				fm.beginTransaction().hide(curHomeFragment).show(oldFragment).commit();
				curHomeFragment = oldFragment;
			}
		}

		// 个人中心页面，刷新用户信息
		if (curHomeFragment != null && curHomeFragment instanceof WebViewFragment) {
			Log.e(TAG, "MainActivity.curHomeFragment");
			WebViewFragment tmpFragment = (WebViewFragment) curHomeFragment;
			tmpFragment.refurbishUserInfo();
		}

	}

	private void autoLogin(){
		if(userName != null && password != null){
			new Thread() {
				public void run() {
					final String logresult = ucPlugin.login(userName,password);
					handler.post(new Runnable(){
						@Override
						public void run() {
							Log.e(TAG, "自动登录："+logresult);
						}});

				}
			}.start();
		}
	}

	@Override
	protected void onNewIntent (Intent intent){
		super.onNewIntent(intent);
		// setIntent(intent);
		// Bundle bundle = intent.getExtras();
		Log.e(TAG, "MainActivity.onNewIntent");
		// if(bundle!=null){
		// ucapplication.setTaburl(bundle.getString("url"));
		// }
		if (intent.getBooleanExtra("loginback", false)) {
			logout();
		}
		if (intent.getBooleanExtra("isPaytrue", false)) {
			if (tabRadioGroup != null && persCenterRadioButton != null) {
				tabRadioGroup.check(persCenterRadioButton.getId());
			}
		}
	}

	/**
	 * 未登录返回
	 * */
	public void logout() {
		Log.e(TAG, "-----logout--------");
		//清除缓存
//		settings.edit().remove("username").remove("password").commit();
		//调转到第一tab
		if(tabRadioGroup!=null&&homeRadioButton!=null){
			tabRadioGroup.check(homeRadioButton.getId());
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		Log.e(TAG, "onStart()...");
		// 绑定服务
		Intent intent = new Intent(this, UCService.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		// 启动服务
		startService(intent);
		// 绑定服务
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		url = getIntent().getStringExtra("url");
		if (url != null && !url.equals("")) {
			if(url.indexOf("file:///android_asset/app/persCenter/index.html?root=true&tab=1")!=-1){
				FragmentManager fm = this.getSupportFragmentManager();
				WebViewFragment newFragment = new WebViewFragment();
				newFragment.setMainActivity(this);
				newFragment.setUrl(tab);
				if(!newFragment.isAdded()){
					fm.beginTransaction().add(R.id.fragment_container, newFragment,tab)
							.hide(curHomeFragment).show(newFragment).commit();
				}
				curHomeFragment = newFragment;
			}else{
				webViewFragment.loadUrl(url);
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "onStop()...");
		if (mBound) {// 取消绑定
			unbindService(mConnection);
			mBound = false;
			Log.d(TAG, "unbindService mConnection");
		}
		if (ucService != null) {
			ucService.setWinContext(null);
		}
	}

	/**
	 * 绑定服务连接器
	 */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			UCService.LocalBinder binder = (UCService.LocalBinder) service;
			mBound = true;
			ucService = binder.getService();
			if (ucService != null) {
				ucService.setWinContext(MainActivity.this);
			}
			Log.d(TAG, "MainActivity,onServiceConnected()");
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
			if (ucService != null) {
				ucService.setWinContext(null);
			}
			Log.d(TAG, "MainActivity,onServiceDisconnected()");
		}
	};

	/**
	 * 显示或隐藏底部菜单
	 */
	public void showFooter(final boolean isshow) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (isshow) {
					footerLinearLayout.setVisibility(View.VISIBLE);
				} else {
					footerLinearLayout.setVisibility(View.GONE);
				}
			}
		});
	}


	/**
	 * 去登录页面
	 */
	public void gotoLogin(final String url) {
		((UCApplication) this.getApplication()).setLoginUser(null);
		Intent i = new Intent(MainActivity.this, LoginActivity.class);
		i.putExtra("url", url);
		startActivity(i);
//		finish();
	}

	/**
	 * 自定义返回
	 *
	 * @return
	 */
	public synchronized boolean doCustomBack() {
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
		Log.e(TAG, "doCustomBack:1");
		if (fragment != null && fragment instanceof WebViewFragment) {
			WebViewFragment baseFragment = (WebViewFragment) fragment;
			Log.e(TAG, "doCustomBack:2");
			// fragment有自己的返回
			if (baseFragment.canBack()) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (doCustomBack()) {
				return true;
			} else {
				exitApp();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void exitApp() {
		if (!isExit) {
			isExit = true;
			Toast.makeText(this, "再按一次返回退出程序", Toast.LENGTH_SHORT).show();
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false;
				}
			}, 2000);
		} else {
			// 获取自己的PID来结束
			// android.os.Process.killProcess(android.os.Process.myPid());
			finish();
		}
	}


	// //////////////////////Cordova使用 start////////////////////
	@Override
	public Activity getActivity() {

		return this;
	}

	public int getTabIndex() {
		return tabIndex;
	}

	private final ExecutorService threadPool = Executors.newCachedThreadPool();

	@Override
	public ExecutorService getThreadPool() {

		return threadPool;
	}

	@Override
	public Object onMessage(String arg0, Object arg1) {

		return null;
	}

	protected CordovaPlugin activityResultCallback = null;

	@Override
	public void setActivityResultCallback(CordovaPlugin plugin) {
		this.activityResultCallback = plugin;
	}

	protected boolean activityResultKeepRunning;
	protected boolean keepRunning = true;

	@Override
	public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
		this.activityResultCallback = command;
		this.activityResultKeepRunning = this.keepRunning;

		// If multitasking turned on, then disable it for activities that return
		// results
		if (command != null) {
			this.keepRunning = false;
		}
		// Start activity
		super.startActivityForResult(intent, requestCode);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		CordovaPlugin callback = this.activityResultCallback;
		if (callback != null) {
			callback.onActivityResult(requestCode, resultCode, intent);
		}
	}
	// //////////////////////Cordova使用 end////////////////////

	/*****************************************微信原生支付start********************************************************/
	/**
	 * 微信支付
	 *
	 */
	public void ucwxpay(String prepayid) {
		genPayReq(prepayid);
		msgApi.sendReq(req);
	}

	private void genPayReq(String prepayId) {
		req.appId = Constants.WX_APP_ID;
		req.partnerId = Constants.wxPayMchId;
		req.prepayId = prepayId;
		req.packageValue = "Sign=WXPay";
		req.nonceStr = genNonceStr();
		req.timeStamp = String.valueOf(genTimeStamp());

		List<NameValuePair> signParams = new LinkedList<NameValuePair>();
		signParams.add(new BasicNameValuePair("appid", req.appId));
		signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
		signParams.add(new BasicNameValuePair("package", req.packageValue));
		signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
		signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
		signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));

		req.sign = genAppSign(signParams);

		Log.e("orion", signParams.toString());

	}

	private String genNonceStr() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
	}

	private long genTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}

	private String genAppSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(Constants.wxPartnerKey);
		// String appSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
		String appSign = MD5.getMD5ForWX(sb.toString());
		Log.e("appSign", appSign);
		return appSign;
	}
	/*****************************************微信原生支付end********************************************************/


	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e(TAG, "MainActivity.onPause(),this=" + this);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			if (ucService != null) {
				ucService.setWinContext(null);
			}
		} catch (Exception ex) {
			Log.d(TAG, null, ex);
		}
		// ucapplication.getScreenManager().popAllActivityExceptOne(null);
		Log.e(TAG, "MainActivity.onDestroy()");
	}

}
