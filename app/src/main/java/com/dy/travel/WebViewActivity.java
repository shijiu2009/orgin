package com.dy.travel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.CordovaChromeClient;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alipay.sdk.app.PayTask;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.navi.sdkdemo.activity.DemoGuideActivity;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.baidu.navisdk.adapter.struct.BNTTsInitConfig;
import com.dy.travel.comm.Constant;
import com.dy.travel.comm.UCApplication;
import com.dy.travel.comm.UCPlugin;
import com.dy.travel.comm.UCService;
import com.dy.travel.util.ComUtil;
import com.dy.travel.util.NormalUtils;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class WebViewActivity extends Activity implements CordovaInterface, OnGetGeoCoderResultListener {

    private static final String TAG = WebViewActivity.class.getSimpleName();
    protected CordovaPlugin activityResultCallback = null;
    protected boolean activityResultKeepRunning;
    protected Handler handler = new Handler();
    protected boolean keepRunning = true;
    private UCPlugin ucPlugin = null;
    private UCApplication ucapplication;
    private CordovaWebView webview;
    private SharedPreferences settings;
    private Dialog mPdialog;
    private String url;
    private String tempUrl;
    private IWXAPI msgApi;
    //导航
    public MyLocationListenner myListener = new MyLocationListenner();
    protected LocationClient mLocClient;
    protected Map<String, Double> locMap = new HashMap<String, Double>();
    protected long lastLocTime;// 上次定位时间
    protected long intervalLocTime = 1000 * 60 * 2;// 2分钟
    protected String finallyName;
    protected String locCity;
    protected String locAddr;
    GeoCoder mSearch = null;

    private String mSDCardPath = null;
    private String APP_FOLDER_NAME = "travel";
    private static final String[] authBaseArr = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int authBaseRequestCode = 1;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 在使用SDK各组件之前初始化context信息，传入ApplicationContext/
        // 注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.webview_activity);
        ucapplication = (UCApplication) this.getApplication();
        ucPlugin = ucapplication.getUCPlugin();
        webview = (CordovaWebView) findViewById(R.id.cordova_webview);
        webview.addJavascriptInterface(this, "ucapp");
        msgApi = WXAPIFactory.createWXAPI(this, Constant.WX_APP_ID);
        msgApi.registerApp(Constant.WX_APP_ID);

        mLocClient = new LocationClient(getApplication());
        /*
         * 此处需要注意：LocationClient类必须在主线程中声明。需要Context类型的参数。 Context需要时全进程有效的context,推荐用getApplicationConext获取全进程有效的context
         */
        mLocClient.registerLocationListener(myListener);

        settings = getSharedPreferences(UCService.SETTING_PREFS_NAME, 0);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            url = bundle.getString("url");
        }
        webview.loadUrl(url);
        tempUrl = url;
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(false);
        webSettings.setUseWideViewPort(true);
        webview.setVerticalScrollBarEnabled(false);
        webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webview.setWebViewClient(new CordovaWebViewClient(this, webview) {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "shouldOverrideUrlLoading:url=" + url + ",from " + WebViewActivity.this);

                if (url.indexOf("file:///android_asset/app/setting/index.html") != -1) {
                    Intent i = new Intent(WebViewActivity.this, MainActivity.class);
                    i.putExtra("url", url);
                    startActivity(i);
                    finish();
                } else if (url.startsWith("tel:")) {
                    return super.shouldOverrideUrlLoading(view, url);
                }
                Intent t = new Intent(WebViewActivity.this, WebViewActivity.class);
                t.putExtra("url", url);
                startActivity(t);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        webview.setWebChromeClient(new CordovaChromeClient(this, webview) {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
//				topTitleTextView.setText(view.getTitle());
            }
        });
        //创建activity，加入队列
        ucapplication.getScreenManager().pushActivity(this);

        // 初始化百度搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
    }

    @JavascriptInterface
    public void goToBack() {
        Log.e("goToBack", "调用成功");

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!back()) {
                    //返回，踢出队列
                    ucapplication.getScreenManager().popActivity(WebViewActivity.this);
                }
            }
        });


    }

    @JavascriptInterface
    public void goToSetting() {
        Log.d("goToSetting", "调用成功");
        if (!"file:///android_asset/app/setting/index.html".equals(tempUrl)) {
            Intent i = new Intent(getActivity(), WebViewActivity.class);
            i.putExtra("url", "file:///android_asset/app/setting/index.html");
            startActivity(i);
        }
    }

    /**
     * 自定义返回
     *
     * @return
     */
    public synchronized boolean back() {
        Log.e("Back", "调用成功");
        Log.e("webview", "对象   " + webview);
        Log.e("webview.canGoBack", "canGoBack******" + webview.canGoBack());
        if (webview != null) {
            if (webview.canGoBack()) {
                Log.e(TAG, "CurrentIndex:" + webview.copyBackForwardList().getCurrentIndex());
                if (webview.getUrl().indexOf("app/indexs.html") != -1) {// 用户在首页按返回键时，�?出程序，而不是返回登陆页
                    return false;
                } else {
                    webview.goBack();
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 显示提示信息
     */
    @JavascriptInterface
    public void showTip(final String tip) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WebViewActivity.this, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @JavascriptInterface
    public void hideWatting() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mPdialog != null && mPdialog.isShowing()) {
                    mPdialog.dismiss();
                }
            }
        });
    }

    /**
     * 判断摄像头权限
     */

    @JavascriptInterface
    public static boolean isCameraUseable() {

        boolean canUse = true;

        Camera mCamera = null;

        try {

            mCamera = Camera.open();

            // setParameters 是针对魅族MX5。MX5通过Camera.open()拿到的Camera对象不为null

            Camera.Parameters mParameters = mCamera.getParameters();

            mCamera.setParameters(mParameters);

        } catch (Exception e) {

            canUse = false;

        }

        if (mCamera != null) {

            mCamera.release();

        }

        return canUse;

    }

    @JavascriptInterface
    public void openWeb(String durl) {
        //从其他浏览器打开
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(durl);
        intent.setData(content_url);
        startActivity(Intent.createChooser(intent, "请选择浏览器"));
    }

    @JavascriptInterface
    public String getImsi() {
        TelephonyManager tm = (TelephonyManager) WebViewActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String imsi = tm.getSubscriberId();// SIM卡串�?
        return imsi;
    }

    @JavascriptInterface
    public String getImei() {
        TelephonyManager tm = (TelephonyManager) WebViewActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String imei = tm.getDeviceId();// 机器编码
        return imei;
    }

    @JavascriptInterface
    public void showWatting() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mPdialog == null) {
                    mPdialog = new Dialog(WebViewActivity.this, R.style.tip_dialog);
                    mPdialog.setContentView(R.layout.watting_item);
                }
                if (!mPdialog.isShowing()) {
                    mPdialog.show();
                }
            }
        });
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
                    result = result.replace("\\\"", "\\\\\"").replaceAll("	", " ").replace("'", "\\'")  //后面那个处理评论有图片的问题，比如说：潭英往事  这本书的评论
                            .replace("\\r\\n", "<br/>")
                            .replace("\\n", "<br/>").replace("\\t", " ");
                }
                if (result.indexOf("overtime") != -1) {
                    Intent t = new Intent(getActivity(), LoginActivity.class);
                    //t.putExtra("url", url);
                    startActivity(t);
                } else {
                    // Log.e(TAG,"result="+result);
                    final StringBuilder sb = new StringBuilder("javascript:ucapp.callBack('");
                    sb.append(callId).append("','").append(result).append("')");
                    // Log.e(TAG,"result="+sb.toString());
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


            }
        }.start();
        return "";
    }


    /**
     * 检查登陆状态
     *
     * @param url
     */
    @JavascriptInterface
    public String checkStatus(final String callId, final String url) {
        Log.e("checkStatus", "调用成功！！");
        new Thread() {
            public void run() {
                String result = ucPlugin.post(url, "{}");
                Log.e("checkStatus-result:", result);
                if (result != null) {
                    boolean success = false;
                    try {
                        JSONObject json = new JSONObject(result);
                        success = json.optBoolean("success");
                        //自动登陆
                        if (!success && settings.getString("username", null) != null && settings.getString("password", null) != null) {
                            result = ucPlugin.login(settings.getString("username", null), settings.getString("password", null));
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    final StringBuilder sb = new StringBuilder("javascript:ucapp.callBack('");
                    sb.append(callId).append("','").append(result).append("')");
                    if (webview != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                String json = sb.toString();
                                webview.loadUrl(json);
                            }
                        });
                    }
                }
            }
        }.start();
        return "";
    }

    /**
     * 保存用户名密码
     *
     * @param username , password
     */
    @JavascriptInterface
    public void saveUser(final String username, final String password) {
        if (username != null && password != null) {
            settings.edit().putString("username", username).commit();
            settings.edit().putString("password", password).commit();
        }
    }

    /**
     * 保存搜索记录
     *
     * @param key
     */
    @JavascriptInterface
    public void saveSearch(final String key) {
        Log.e("saveSearchKey", key);
        if (key != null) {
            if (settings.getInt("keySize", -1) != -1) {
                int count = settings.getInt("keySize", -1);
                count++;
                settings.edit().putString("searchKey" + count, key).commit();
                settings.edit().putInt("keySize", count).commit();
            } else {
                settings.edit().putString("searchKey" + 0, key).commit();
                settings.edit().putInt("keySize", 0).commit();
            }
        }
    }

    /**
     * 获取搜索记录
     *
     * @param callId
     */
    @JavascriptInterface
    public void getSearch(final String callId) {
        Log.e("getSearch", "xxxxxx");
        if (settings.getInt("keySize", -1) != -1) {
            int count = settings.getInt("keySize", 0);
            List<String> list = new ArrayList<String>();
            for (int i = 0; i <= count; i++) {
                list.add(settings.getString("searchKey" + i, ""));
            }
            JSONArray jsonObj = new JSONArray(list);
            final StringBuilder sb = new StringBuilder("javascript:ucapp.callBack('");
            sb.append(callId).append("','").append(jsonObj.toString()).append("')");
            if (webview != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        webview.loadUrl(sb.toString());
                    }
                });
            }
        }
    }

    //注销
    @JavascriptInterface
    public void logout() {
        if (settings != null) {
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();
            editor.commit();
        }
        Intent i = new Intent(WebViewActivity.this, MainActivity.class);
        i.putExtra("loginback", true);
        startActivity(i);
        finish();
    }

    @JavascriptInterface
    public int getVerCode() {
        int versionCode = 0;
        try {
            PackageInfo packageInfo = WebViewActivity.this.getPackageManager().getPackageInfo(WebViewActivity.this.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    @JavascriptInterface
    public String getVerName() {

        String versionName = "1.0";
        try {
            PackageInfo packageInfo = WebViewActivity.this.getPackageManager().getPackageInfo(WebViewActivity.this.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * �?查更�?
     */
    @JavascriptInterface
    public void checkUpdate(boolean autocheck) {
        Log.e(TAG, "checkUpdate:" + autocheck);
        if (ucPlugin != null) {
            ucPlugin.checkUpdate(autocheck, WebViewActivity.this);
        }
    }

    /**
     * 登陆
     */
    @JavascriptInterface
    public void gotoLogin(String url) {
        Log.e(TAG, "gotoLogin:" + url);
        ((UCApplication) this.getApplication()).setLoginUser(null);
        Intent i = new Intent(WebViewActivity.this, LoginActivity.class);
        i.putExtra("url", url);
        startActivity(i);
    }

    /**
     * 跳转页面
     */
    @JavascriptInterface
    public void gotoUrl(String url) {
        Log.e(TAG, "gotoUrl:" + url);
        if (url.indexOf("tab=1") == -1) {
            Intent t = new Intent(WebViewActivity.this, WebViewActivity.class);
            t.putExtra("url", url);
            startActivity(t);
            //创建activity，加入队列
            ucapplication.getScreenManager().pushActivity(this);
        } else {
            Intent t = new Intent(WebViewActivity.this, MainActivity.class);
            t.putExtra("loginback", true);
            startActivity(t);
        }

    }

    /**
     * 拨打电话
     */
    @SuppressLint("MissingPermission")
    @JavascriptInterface
    public void call(final String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
        startActivity(intent);
    }

    public JSONArray getMyFavoriteValue(SharedPreferences prefs, String key) {
        String value = prefs.getString(key, "");
        JSONArray array = new JSONArray();
        if (!TextUtils.isEmpty(value)) {
            try {
                array = new JSONArray(value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    @JavascriptInterface
    public boolean hasFavorite(String key, String type) {
        @SuppressLint("WrongConstant") SharedPreferences pref = ucapplication.getApplicationContext().getSharedPreferences("myFavorite", Context.MODE_APPEND);
        boolean has = false;
        JSONArray array = getMyFavoriteValue(pref, key);
        if (array != null && array.length() > 0) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.optJSONObject(i);
                String oType = o.optString("type");
                if (!TextUtils.isEmpty(oType) && oType.equals(type)) {
                    has = true;
                    break;
                }
            }
        }
        return has;
    }


    /**
     * 添加或删�?"收藏"/"喜欢"
     */
    @JavascriptInterface
    public boolean addOrRemoveFavorite(String key, String type, String name, String icon) {
        @SuppressLint("WrongConstant") SharedPreferences pref = ucapplication.getApplicationContext().getSharedPreferences("myFavorite", Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        boolean has = hasFavorite(key, type);
        JSONArray array = getMyFavoriteValue(pref, key);
        if (has) {
            JSONArray oldArray = array;
            array = new JSONArray();
            for (int i = 0; i < oldArray.length(); i++) {
                JSONObject o = oldArray.optJSONObject(i);
                if (o != null) {
                    String oType = o.optString("type");
                    if (!TextUtils.isEmpty(oType) && !oType.equalsIgnoreCase(type)) {
                        array.put(o);
                    }
                }
            }
        } else {
            JSONObject o = new JSONObject();
            try {
                o.put("type", type);
                o.put("name", name);
                o.put("icon", icon);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(o);
        }
        editor.putString(key, array.toString());
        editor.putInt(key + "_count", array.length());// "收藏"/"喜欢"
        editor.commit();
        return !has;
    }

    /**
     * 获取"收藏"/"喜欢"
     *
     * @param key "收藏":"favorites";"喜欢":"hobby";
     * @return
     */
    @SuppressLint("WrongConstant")
    @JavascriptInterface
    public String myFavorite(String key) {
        return ucapplication.getApplicationContext().getSharedPreferences("myFavorite", Context.MODE_APPEND).getString(key, "");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goToBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public Activity getActivity() {

        return this;
    }

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    @Override
    public ExecutorService getThreadPool() {

        return threadPool;
    }

    @Override
    public Object onMessage(String arg0, Object arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {

        this.activityResultCallback = plugin;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webview != null) {
            Log.e(TAG, "reload()...");
            webview.loadUrl("javascript:reload();");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        url = getIntent().getStringExtra("url");
        if (url != null && !url.equals("")) {
            if (url.indexOf("file:///android_asset/app/attractions/point.html") != -1) {
                if (webview != null) {
                    webview.destroy();

                }
            }
        }
    }

    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {

        this.activityResultCallback = command;
        this.activityResultKeepRunning = this.keepRunning;
        if (command != null) {
            this.keepRunning = false;
        }
        Log.e(TAG, "startActivityForResult()...");
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        CordovaPlugin callback = this.activityResultCallback;
        if (callback != null) {
            callback.onActivityResult(requestCode, resultCode, intent);
        }
    }


    /**
     * 监听函数，有新位置的时候，格式化成字符串，输出到屏幕中
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.e(TAG, "onReceiveLocation()...");
            if (location == null || location.getLatitude() == 4.9E-324 || location.getLongitude() == 4.9E-324) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(WebViewActivity.this, "定位失败!", Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }

            final double latitude = location.getLatitude();
            final double longitude = location.getLongitude();
            locCity = location.getCity();
            locMap.put("latitude", latitude);
            locMap.put("longitude", longitude);
            locAddr = location.getAddrStr();

            // 获取位置之后停止
            mLocClient.stop();
            hideWatting();
            Log.e(TAG, "location=" + latitude + "," + longitude);
            if (isNaviLoc) {// 导航定位
                isNaviLoc = false;
                if (locMap.get("eX") != null && locMap.get("eY") != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (TextUtils.isEmpty(finallyName)) {
                                startNavi(Double.toString(latitude), Double.toString(longitude), "我的位置", Double.toString((Double) locMap.get("eX")), Double.toString((Double) locMap.get("eY")), "终点");
                            } else {
                                startNavi(Double.toString(latitude), Double.toString(longitude), "我的位置", Double.toString((Double) locMap.get("eX")), Double.toString((Double) locMap.get("eY")), finallyName);
                            }
                        }
                    });
                }
            } else if (isReqLocation) {// 页面请求定位
                isReqLocation = false;
                webview.loadUrl("javascript:ucapp.callLocBack(" + longitude + "," + latitude + ",'" + locAddr + "');");
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
            Log.e(TAG, "onReceivePoi()...");
            if (poiLocation == null) {
                return;
            }
        }
    }

    @JavascriptInterface
    public void startNavi(final String sX, final String sY, final String sName, final String eX, final String eY, final String eName) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                startNaviX(sX, sY, sName, eX, eY, eName);
            }
        });
    }

    private boolean isNaviLoc = false;// 导航定位

    @JavascriptInterface
    public void startNavi2(final String eXs, final String eYs, String ename) {

        if (initDirs()) {
            initNavi();
        }

        isNaviLoc = true;
        isReqLocation = false;
        final Double longitude = locMap.get("longitude");
        final Double latitude = locMap.get("latitude");
        final double eX = ComUtil.getDouble(eXs);
        final double eY = ComUtil.getDouble(eYs);
        locMap.put("eX", eX);
        locMap.put("eY", eY);
        // Log.d(TAG,"locMap:"+locMap);
        if (!TextUtils.isEmpty(ename)) {
            finallyName = ename;
        }
        if (longitude == null || latitude == null) {
            getLocation();

        } else if (longitude != null && latitude != null) {
            long nTime = System.currentTimeMillis();
            if (nTime - lastLocTime >= intervalLocTime) {
                lastLocTime = nTime;
                getLocation();
            } else {
                isNaviLoc = false;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideWatting();
                        if (TextUtils.isEmpty(finallyName)) {
                            startNavi(Double.toString(latitude), Double.toString(longitude), "我的位置", Double.toString((Double) locMap.get("eX")), Double.toString((Double) locMap.get("eY")), "终点");
                        } else {
                            startNavi(Double.toString(latitude), Double.toString(longitude), "我的位置", Double.toString((Double) locMap.get("eX")), Double.toString((Double) locMap.get("eY")), finallyName);
                        }

                    }
                });
            }
        }
    }

    /**
     * 启动GPS导航. 前置条件：导航引擎初始化成功
     *
     * @param sXs
     * @param sYs
     * @param sName
     * @param eXe
     * @param eYe
     * @param eName
     */
    protected void startNaviX(final String sXs, final String sYs, final String sName, final String eXe, final String eYe, final String eName) {
        Log.d("进入导航方法", "进入导航方法");
        Log.e(TAG, "startNavi:" + sName + ":sXs=" + sXs + "sYs=" + sYs + "," + eName + ":eXe=" + eXe + ",eYe=" + eYe + ",mainActivity=" + WebViewActivity.this);
        // sX=22.797556,sY=108.359375,eX=22.801243,eY=108.368549
        final double sX = ComUtil.getDouble(sXs);
        final double sY = ComUtil.getDouble(sYs);
        final double eX = ComUtil.getDouble(eXe);
        final double eY = ComUtil.getDouble(eYe);
        double[] start = bd_decrypt(sX, sY);
        double[] end = this.bd_decrypt(eX, eY);
        BNRoutePlanNode sNode = null, eNode = null;
        sNode = new BNRoutePlanNode.Builder()
                .latitude(start[1])
                .longitude(start[0])
                .name(sName)
                .description(sName)
                .coordinateType(BNRoutePlanNode.CoordinateType.GCJ02)
                .build();

        eNode = new BNRoutePlanNode.Builder()
                .latitude(end[1])
                .longitude(end[0])
                .name(eName)
                .description(eName)
                .coordinateType(BNRoutePlanNode.CoordinateType.GCJ02)
                .build();

        routePlanToNavi(sNode, eNode);
    }

    private void routePlanToNavi(BNRoutePlanNode sNode, BNRoutePlanNode eNode) {
        List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
        list.add(sNode);
        list.add(eNode);

        BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                list,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                null,
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                Toast.makeText(WebViewActivity.this.getApplicationContext(),
                                        "算路开始", Toast.LENGTH_SHORT).show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                Toast.makeText(WebViewActivity.this.getApplicationContext(),
                                        "算路成功", Toast.LENGTH_SHORT).show();
                                // 躲避限行消息
                                Bundle infoBundle = (Bundle) msg.obj;
                                if (infoBundle != null) {
                                    String info = infoBundle.getString(
                                            BNaviCommonParams.BNRouteInfoKey.TRAFFIC_LIMIT_INFO
                                    );
                                    Log.d("OnSdkDemo", "info = " + info);
                                }
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                Toast.makeText(WebViewActivity.this.getApplicationContext(),
                                        "算路失败", Toast.LENGTH_SHORT).show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                Toast.makeText(WebViewActivity.this.getApplicationContext(),
                                        "算路成功准备进入导航", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(WebViewActivity.this,
                                        DemoGuideActivity.class);

                                startActivity(intent);
                                break;
                            default:
                                // nothing
                                break;
                        }
                    }
                });
    }

    /**
     * 将百度经纬度(bd091) 转换�?gcj02
     */
    private double x_pi = 3.14159265358979324 * 3000.0 / 180.0;

    private double[] bd_decrypt(double bd_lat, double bd_lon) {
        double[] gg = new double[2];
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        gg[0] = z * Math.cos(theta); // 经度
        gg[1] = z * Math.sin(theta); // 纬度
        return gg;
    }

    /**
     * 获取经纬度
     *
     * @param reqAddr 是否返回地址信息
     */
    private boolean isReqLocation = false;// 是否页面请求经纬度

    @JavascriptInterface
    public void loc(boolean reqAddr) {
        isReqLocation = true;
        isNaviLoc = false;
        final Double longitude = locMap.get("longitude");
        final Double latitude = locMap.get("latitude");
        // Log.d(TAG,"==="+locMap);
        if (longitude == null || latitude == null) {
            getLocation();
        } else if (longitude != null && latitude != null) {
            long nTime = System.currentTimeMillis();
            if (nTime - lastLocTime > intervalLocTime) {
                lastLocTime = nTime;
                getLocation();
            } else {
                isReqLocation = false;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        webview.loadUrl("javascript:ucapp.callLocBack(" + longitude + "," + latitude + ",'" + locAddr + "');");
                    }
                });
            }
        }
    }

    /**
     * 获取位置
     */
    protected void getLocation() {
        Log.e(TAG, "getLocation()...." + mLocClient);
        showWatting();
        if (mLocClient != null) {
            setLocationOption();
            Log.e(TAG, "getLocation()...." + mLocClient.isStarted());
            if (!mLocClient.isStarted()) {
                Log.e(TAG, "isStarted()....");
                mLocClient.start();
            }
            //mLocClient.requestLocation();
        }
    }

    /**
     * 设置相关参数
     */
    private void setLocationOption() {
        // Log.e(TAG, "setLocationOption()....");
        // LocationClientOption option = new LocationClientOption();
        // option.setOpenGps(true);
        // option.setCoorType("bd09ll");
        // option.setServiceName("com.baidu.location.service_v2.9");
        // option.setAddrType("all");// 返回的定位结果包含地址信息
        // option.disableCache(true);// 禁止启用缓存定位
        // option.setScanSpan(3000);
        // // option.setPoiNumber(10);
        // option.setPriority(LocationClientOption.GpsFirst); // 不设置，默认是gps优先
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值bd09ll
        int span = 3000;
        option.setScanSpan(span);// 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);
        mLocClient.setLocOption(option);
    }


    @Override
    @JavascriptInterface
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR || result.getAddress() == null) {
            Toast.makeText(getActivity(), "没有解析到结果:" + addressStr, Toast.LENGTH_SHORT).show();
        } else {
            final String myLat = String.valueOf(result.getLocation().latitude);
            final String myLng = String.valueOf(result.getLocation().longitude);
            // 获取地理编码结果
            handler.post(new Runnable() {
                @Override
                public void run() {
                    webview.loadUrl("javascript:ucapp.callGetLatAndLng(" + myLat + "," + myLng + ");");
                }
            });
        }
    }

    // 反向地图编码:经纬度->地址
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            // 没有找到检索结果
            return;
        }
        // 获取反向地理编码结果
    }

    /**
     * 解析地址获取经纬度
     *
     * @param address
     */
    @JavascriptInterface
    public void getLatAndLng(final String address) {
        /*
         *  发起地理编码检索,示例:
         *  mSearch.geocode(new GeoCodeOption().city("北京").address("海淀区上地十街10号"));
         */
        addressStr = address;
        mSearch.geocode(new GeoCodeOption().city("崇左龙州").address(address));
    }

    String addressStr = "";


    //微信支付
    @JavascriptInterface
    public void wxpay(final String prepayid, final String appid, final String nonceStr,
                      final String packageValue, final String partnerid,
                      final String timeStamp, final String sign) {


        Log.e(TAG, "wxpay:prepayid=" + prepayid + ",appid=" + appid + ",nonceStr=" + nonceStr + ",package=" + packageValue +
                ",partnerid=" + partnerid + ",timeStamp=" + timeStamp + ",sign=" + sign);
        Runnable run = new Runnable() {
            @Override
            public void run() {
                ucwxpay(prepayid, appid, nonceStr, packageValue, partnerid, timeStamp, sign);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(run);
        payThread.start();
        //finish();

    }

    //支付宝
    @JavascriptInterface
    public void alipay(final String payInfo) {
        LOG.e("支付宝接口", "调用成功！");
        Runnable run = new Runnable() {
            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(WebViewActivity.this);
                LOG.e("支付宝接口参数payInfo", "" + payInfo);
                LOG.e("支付宝接口alipay", "" + alipay);
                // 调用支付接口，获取支付结果
                Map<String, String> result = alipay.payV2(payInfo, true);
                LOG.e("支付宝接口result", result.toString());
                Message msg = new Message();
                msg.what = 1;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(run);
        payThread.start();

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                     * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                     * docType=1) 建议商户依赖异步通知
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息

                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        Toast.makeText(WebViewActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(WebViewActivity.this, MainActivity.class);
                        intent.putExtra("isPaytrue", true);
                        startActivity(intent);
//					finish();
//					Log.e("支付宝支付成功", ""+webview);
//					webview.loadUrl("file:///android_asset/app/order/list.html"); // 跳转到支付成功页面中心
                    } else {
                        // 判断resultStatus 为非"9000"则代表可能支付失败
                        // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(WebViewActivity.this, "支付结果确认中", Toast.LENGTH_SHORT).show();

                        }
                        if (TextUtils.equals(resultStatus, "4000")) {
                            Toast.makeText(WebViewActivity.this, "系统繁忙，请稍后再试", Toast.LENGTH_SHORT).show();

                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
//							Toast.makeText(WebViewActivity.this, "支付失败", Toast.LENGTH_SHORT).show();

                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }

        ;
    };

    /***********************************微信支付Start***********************************************************
     /**
     * 微信支付
     *
     */
    public void ucwxpay(String prepayid, String appid, String nonceStr,
                        String packageValue, String partnerid, String timeStamp, String sign) {
        Log.e(TAG, "微信预支付ID:" + prepayid);
        PayReq req = new PayReq();
        req.appId = appid;
        req.partnerId = partnerid;
        req.prepayId = prepayid;
        req.packageValue = packageValue;
        req.nonceStr = nonceStr;
        req.timeStamp = timeStamp;
        req.sign = sign;
        //req.extData = "app data"; // optional
        boolean flag = msgApi.sendReq(req);
        Log.e(TAG, "调用微信支付完成：" + flag);
    }


    /*********************************微信支付End*************************************************************/


    @Override
    protected void onDestroy() {
        // 释放地理编码检索实例
        mSearch.destroy();
        super.onDestroy();
        if (webview != null) {
            webview.handleDestroy();
        }
    }

    private void initNavi() {

        //申请权限
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!hasBasePhoneAuth()) {
                this.requestPermissions(authBaseArr, authBaseRequestCode);
                if (this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                    showTip("请开启位置权限");
                }
                if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    showTip("请开启存储权限");
                }
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
                            Log.e("key", "成功");
                        } else {
                            result = "key校验失败, " + msg;
                            Log.e("key", "失败");
                        }
//						Toast.makeText(WebViewActivity.this, result, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void initStart() {
//						Toast.makeText(WebViewActivity.this.getApplicationContext(),
//								"百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
                        Log.e("map", "开始");
                    }

                    @Override
                    public void initSuccess() {
//						Toast.makeText(WebViewActivity.this.getApplicationContext(),
//								"百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                        Log.e("map", "成功");
                        // 初始化tts
                        initTTS();
                    }

                    @Override
                    public void initFailed(int errCode) {
//						Toast.makeText(WebViewActivity.this.getApplicationContext(),
//								"百度导航引擎初始化失败 " + errCode, Toast.LENGTH_SHORT).show();
                        Log.e("map", "百度导航引擎初始化失败 " + errCode);
                    }
                });


    }

    //初始化导航目录
    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        Log.e("path", mSDCardPath);
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

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

}
