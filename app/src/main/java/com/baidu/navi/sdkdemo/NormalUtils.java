/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.navi.sdkdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.baidu.navi.sdkdemo.activity.DemoDrivingActivity;
import com.baidu.navi.sdkdemo.activity.DemoNaviActivity;
import com.baidu.navi.sdkdemo.activity.DemoNaviSettingActivity;

public class NormalUtils {

    public static void gotoNavi(Activity activity) {
        Intent it = new Intent(activity, DemoNaviActivity.class);
        activity.startActivity(it);
    }

    public static void gotoSettings(Activity activity) {
        Intent it = new Intent(activity, DemoNaviSettingActivity.class);
        activity.startActivity(it);
    }

    public static void gotoDriving(Activity activity) {
        Intent it = new Intent(activity, DemoDrivingActivity.class);
        activity.startActivity(it);
    }

    public static String getTTSAppID() {
        return "11213224";
    }

    public static String getTTSAppKey() {
        return "gT2XSUgoMFysCzwLCUtrIItTUdclThsf";
    }

    public static String getTTSsecretKey() {
        return "MEokc3O8y95Lh9fOLX7lrxY1jD9OkWFf";
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
