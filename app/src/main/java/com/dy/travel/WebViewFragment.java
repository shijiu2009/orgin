package com.dy.travel;

import org.apache.cordova.CordovaChromeClient;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dy.travel.comm.UCApplication;
import com.dy.travel.comm.UCPlugin;
import com.dy.travel.comm.UCService;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;


public class WebViewFragment extends Fragment {

    private static final String TAG = WebViewFragment.class.getSimpleName();
    // protected WebView webview;
    protected CordovaWebView webview;

    protected View mView = null;
    protected Handler handler = new Handler();
    protected MainActivity mainActivity;
    protected Resources mResources;
    private UCApplication ucapplication;
    private UCPlugin ucPlugin = null;
    protected String finallyName;
    protected Dialog pdialog;
    protected PreferenceManager pManager;
    private SharedPreferences settings;
    private String url;

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private static final String[] authBaseArr = {
            android.Manifest.permission.CAMERA
    };
    private static final int authBaseRequestCode = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.web_view, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        mResources = getResources();
        ucapplication = (UCApplication) (getActivity().getApplication());
        settings = getActivity().getSharedPreferences(UCService.SETTING_PREFS_NAME, 0);
        ucPlugin = ucapplication.getUCPlugin();
        webview = (CordovaWebView) mView.findViewById(R.id.webView_view);
        webview.addJavascriptInterface(this, "ucapp");
        if (url != null) {
            if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")) {
                webview.loadUrl(url);
            }
        } else {
            url = "file:///android_asset/app/indexs.html?root=true";
            webview.loadUrl(url);
        }

        final Dialog pdialog = new Dialog(mView.getContext(), R.style.tip_dialog);
        pdialog.setContentView(R.layout.watting_item);
        pdialog.setCancelable(true);
        if (Build.VERSION.SDK_INT >= 19) {
            webview.getSettings().setLoadsImagesAutomatically(true);
        } else {
            webview.getSettings().setLoadsImagesAutomatically(false);
        }


        webview.getSettings().setUseWideViewPort(true);


        webview.setWebViewClient(new CordovaWebViewClient(mainActivity, webview) {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                boolean isload = true;
                Log.e(TAG, "shouldOverrideUrlLoading:" + url);
                // 页面间的调转前执行，访问的是html页面追加随机数，让页面返回最新的数据
//				if (url.indexOf("?") != -1) {
//					if (url.indexOf("r_t_x") == -1) {
//						url = url + "&r_t_x=" + random.nextInt(100000);
//					}
//				} else {
//					url = url + "?r_t_x=" + random.nextInt(100000);
//				}


                if (url.indexOf("tab=") != -1) {
                    if (mainActivity != null) {
                        mainActivity.startFragment(url);
                    } else {
                        view.loadUrl(url);
                    }
//					finish();
                } else {
                    Intent i = new Intent(getActivity(), WebViewActivity.class);
                    i.putExtra("url", url);
                    if (url.indexOf("file:///android_asset/app/search/index.html") != -1) {
                        i.putExtra("showtop", false);
                    }
                    startActivity(i);
                }
                return isload;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.e(TAG, "onPageStarted:" + url);
                pdialog.show();
                super.onPageStarted(view, url, favicon);
                if (mainActivity != null) {
                    if (url.indexOf("root=true") == -1) {
                    }
                    if (url.indexOf("/app/index.html") != -1) {
                    }
                    if (url.indexOf("search=true") != -1) {
                    }
                    if (url.indexOf("share=true") != -1) {
                    }
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.e(TAG, "onPageFinished:" + url);
                if (pdialog.isShowing()) {
                    pdialog.dismiss();
                }
                super.onPageFinished(view, url);

                if (url.indexOf("root=true") != -1) {// 用户在首页按返回键时,清除历史记录
                    webview.clearHistory();
                }

                if (!webview.getSettings().getLoadsImagesAutomatically()) {
                    webview.getSettings().setLoadsImagesAutomatically(true);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                if (!isReload) {

                }
            }
        });

        WebSettings webSettings = webview.getSettings();
        // webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(false);
        webview.setVerticalScrollBarEnabled(false);
        webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webview.setWebChromeClient(new CordovaChromeClient(mainActivity, webview) {

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("对话").setMessage(message).setPositiveButton("确定", null);
                // 不需要绑定按键事�?
                builder.setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        return true;
                    }
                });
                // 禁止响应按back键的事件
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
                result.confirm();// 因为没有绑定事件，需要强行confirm,否则页面会变黑显示不了内�??
                return true;
            }

            // 设置标题
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);

            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("请登").setMessage(message).setPositiveButton("确定", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                }).setNeutralButton("取消", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                // 不需要绑定按键事�?
                builder.setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        return true;
                    }
                });
                // 禁止响应按back键的事件
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        // 实现下载功能
        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private Dialog mPdialog;

    @JavascriptInterface
    public void showWatting() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mPdialog == null) {
                    mPdialog = new Dialog(mView.getContext(), R.style.tip_dialog);
                    mPdialog.setContentView(R.layout.watting_item);
                }
                if (!mPdialog.isShowing()) {
                    mPdialog.show();
                }
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

    @JavascriptInterface
    public String getImsi() {
        TelephonyManager tm = (TelephonyManager) mView.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String imsi = tm.getSubscriberId();// SIM卡串�?
        return imsi;
    }

    @JavascriptInterface
    public String getImei() {
        TelephonyManager tm = (TelephonyManager) mView.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String imei = tm.getDeviceId();// 机器编码
        return imei;
    }

    /**
     * 扫描二维码
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @JavascriptInterface
    public void scanning() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!hasBasePhoneAuth()) {
				if (mainActivity.checkSelfPermission(android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_DENIED){
					showTip("请开启相机权限");
				}
                this.requestPermissions(authBaseArr, authBaseRequestCode);
                return;
            }else{
                Intent i = new Intent(mainActivity, CaptureActivity.class);
                mainActivity.startActivityForResult(i, 0);
            }
        }else{
			if (isCameraUseable()) {
				Intent i = new Intent(mainActivity, CaptureActivity.class);
				mainActivity.startActivityForResult(i, 0);
			} else {
				handler.post(new Runnable() {
					@Override
					public void run() {
						showTip("请开启相机权限");
					}
				});
			}
		}
    }

    ;

    @JavascriptInterface
    public void callScanBack(String content) {
        final StringBuilder sb = new StringBuilder("javascript:ucapp.callScanBack('");
        sb.append(content).append("')");
        Log.e(TAG, "回调扫码结果：" + sb.toString());

        if (webview != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String json = sb.toString();
                    json = json.replace("\\", "\\\\");
                    json = json.replace("\r\n", "<br>");
                    json = json.replace("\n", "<br>");
                    webview.loadUrl(json);
                }
            });
        }
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

    public void loadUrl(String url) {
        // 访问的是html页面追加随机数，让页面返回最新的数据
//		if (url.indexOf("?") != -1) {
//			if (url.indexOf("r_t_x") == -1) {
//				url = url + "&r_t_x=" + random.nextInt(100000);
//			}
//		} else {
//			url = url + "?r_t_x=" + random.nextInt(100000);
//		}
        webview.loadUrl(url);
    }

    ;

    public boolean canBack() {
        Log.e("webview", "" + webview.canGoBack());
        if (webview != null) {
            if (webview.canGoBack()) {
                Log.e(TAG, "CurrentIndex:" + webview.copyBackForwardList().getCurrentIndex());
                if (webview.getUrl().indexOf("app/index.html") != -1) {// 用户在首页按返回键时，�?出程序，而不是返回登陆页
                    Log.e("webviewUrl", "" + webview.getUrl());
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

    @JavascriptInterface
    public String post(final String callId, final String url, final String params) {
        Log.e(TAG, url + "," + params);
        new Thread() {
            public void run() {
                String result = ucPlugin.post(url, params);
                if (result == null) {
                    result = "{}";
                } else {
                    result = result.replace("\\\"", "\\\\\"").replace("\\r\\n", "<br/>").replace("\\n", "<br/>");// .replace("'", "\\'")
                }
                // Log.e(TAG,"result="+result);

                if (result.indexOf("overtime") != -1) {
                    Intent t = new Intent(getActivity(), LoginActivity.class);
                    //t.putExtra("url", url);
                    startActivity(t);
                } else {
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
                if (result != null) {
                    //Log.e("checkStatus-result:", result);
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
                                json = json.replace("\r\n", "<br>");
                                json = json.replace("\n", "<br>");
//								Log.e("checkStatus-result-json:", json);
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
     * 显示提示信息
     */
    @JavascriptInterface
    public void showTip(final String tip) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mView.getContext(), tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 拨打电话
     */
    @JavascriptInterface
    public void call(final String phoneNum) {
        //Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ phoneNum));
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + phoneNum));

        startActivity(intent);
    }

    public void onResume() {
        super.onResume();
        refurbishUserInfo();
    }

    public void onPause() {
        super.onPause();
    }

    /**
     * 返回
     */
    @JavascriptInterface
    public void goToBack() {
        Log.e(TAG, "goToBack");
        handler.post(new Runnable() {
            @Override
            public void run() {
                mainActivity.doCustomBack();
            }
        });
    }

    @JavascriptInterface
    public int getVerCode() {
        int versionCode = 0;
        try {
            PackageInfo packageInfo = mView.getContext().getPackageManager().getPackageInfo(mView.getContext().getPackageName(), 0);
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
            PackageInfo packageInfo = mView.getContext().getPackageManager().getPackageInfo(mView.getContext().getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    @JavascriptInterface
    public void logout(String url) {
        Log.e("logout", "logout：调用");
        mainActivity.startFragment(url);
    }

    /**
     * �?查更�?
     */
    @JavascriptInterface
    public void checkUpdate(boolean autocheck) {
        Log.e(TAG, "checkUpdate:" + autocheck);
        if (ucPlugin != null) {
            ucPlugin.checkUpdate(autocheck, mView.getContext());
        }
    }

    /**
     * 登陆
     */
    @JavascriptInterface
    public void gotoLogin(String url) {
        Log.e(TAG, "gotoLogin:" + url);
        mainActivity.gotoLogin(url);
    }

    /**
     * 我的 刷新
     */
    public void refurbishUserInfo() {
        if (mainActivity != null && (mainActivity.getTabIndex() == 2 || mainActivity.getTabIndex() == 3) && webview != null) {
            Log.e("refurbishUserInfo", "调用成功");
            webview.loadUrl("javascript:reload();");
        }
    }

    // 调转到设置页
    @JavascriptInterface
    public void goToSetting() {
        Log.d("goToSetting", "调用成功");
        Intent i = new Intent(getActivity(), WebViewActivity.class);
        i.putExtra("url", "file:///android_asset/app/setting/index.html");
        startActivity(i);
        //loadUrl("file:///android_asset/app/setting/index.html");
    }

    // 调转到首�?
    public void goHome() {
        loadUrl("file:///android_asset/app/index.html?root=true");
    }

    // 调转到资讯页
    public void goInformati() {
        loadUrl("file:///android_asset/app/informati/index.html?root=true");
    }

    // 调转到订购页�?
    public void goBooking() {
        loadUrl("file:///android_asset/app/booking/index.html?root=true");
    }

    // 调转互动页面
    public void goInteraction() {
        loadUrl("file:///android_asset/app/interaction/list.html?root=true");
    }

    // 调转个人中心页面
    public void goPersCenter() {
        loadUrl("file:///android_asset/app/persCenter/index.html?root=true");
    }

    // 调转到搜索页�?
    public void goSearch() {
        loadUrl("file:///android_asset/app/search/index.html?search=true");
    }

    // 执行搜索
    public void doSearch(String keyword) {
        Log.e(TAG, "doSearch:" + keyword);
        loadUrl("file:///android_asset/app/search/list.html?title=" + keyword);
    }

    // 跳转到注册页�?
    public void goRegister() {
        loadUrl("file:///android_asset/app/register.html?search=true");
    }

    JSONArray getMyFavoriteValue(SharedPreferences prefs, String key) {
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

    /*
     * 添加或删�?"收藏"/"喜欢"
     */
    @JavascriptInterface
    public boolean addOrRemoveFavorite(String key, String type, String name, String icon) {
        SharedPreferences pref = ucapplication.getApplicationContext().getSharedPreferences("myFavorite", Context.MODE_APPEND);
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
        editor.putInt(key + "_count", array.length());// "收藏"�?"喜欢"的个�?
        editor.commit();
        return !has;
    }

    /**
     * 获取"收藏"�?"喜欢"的个�?
     *
     * @param key "收藏":"favorites";"喜欢":"hobby";
     * @return
     */
    @JavascriptInterface
    public int myFavoriteCount(String key) {
        return ucapplication.getApplicationContext().getSharedPreferences("myFavorite", Context.MODE_APPEND).getInt(key + "_count", 0);
    }

    /**
     * 获取"收藏"�?"喜欢"
     *
     * @param key "收藏":"favorites";"喜欢":"hobby";
     * @return
     */
    @JavascriptInterface
    public String myFavorite(String key) {
        return ucapplication.getApplicationContext().getSharedPreferences("myFavorite", Context.MODE_APPEND).getString(key, "");
    }

    @JavascriptInterface
    public void wxpay(final String prepayid) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mainActivity.ucwxpay(prepayid);
            }
        });
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

	private boolean hasBasePhoneAuth() {
		PackageManager pm = mainActivity.getPackageManager();
		for (String auth : authBaseArr) {
			if (pm.checkPermission(auth, mainActivity.getPackageName()) != PackageManager
					.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

}
