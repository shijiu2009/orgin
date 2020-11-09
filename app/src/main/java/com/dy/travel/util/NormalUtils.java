/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.dy.travel.util;

import android.content.Context;

//import com.baidu.navi.sdkdemo.activity.DemoDrivingActivity;
//import com.baidu.navi.sdkdemo.activity.DemoNaviActivity;
//import com.baidu.navi.sdkdemo.activity.DemoNaviSettingActivity;

public class NormalUtils {

//    public static void gotoNavi(Activity activity) {
//        Intent it = new Intent(activity, DemoNaviActivity.class);
//        activity.startActivity(it);
//    }
//
//    public static void gotoSettings(Activity activity) {
//        Intent it = new Intent(activity, DemoNaviSettingActivity.class);
//        activity.startActivity(it);
//    }
//
//    public static void gotoDriving(Activity activity) {
//        Intent it = new Intent(activity, DemoDrivingActivity.class);
//        activity.startActivity(it);
//    }

    public static String getTTSAppID() {
        return "22208385";
    }//21441073

    public static String getTTSAppKey() {
        return "X3laKG9GO2fjSQavNl91d7NY";//SDLhlO0ysDuHcVcB0viA6pWWHbXu7Fz0
    }

    public static String getTTSsecretKey() {
        return "CXjcGRodDaK8REIUBa1sdgLj9yjA17fA";//C5Fud1BG6xRyUL6wZ736d1piStHaTHuZ
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
