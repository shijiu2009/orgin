package com.dy.travel.util;

import java.util.Stack;

import android.app.Activity;
public class ScreenManager {
    private static Stack<Activity> activityStack;
    private static ScreenManager instance;
    private ScreenManager() {
    }
    public static ScreenManager getScreenManager() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }
    //弹出栈顶Activity
    public void popActivity(Activity activity) {
//    	 Log.e("ScreenManager","popActivity..."+activity);
        if (activity != null) {
            //在从自定义集合中取出当前Activity时，也进行了Activity的关闭操�?
            activity.finish();
            activityStack.remove(activity);
            activity = null;
        }
    }
    //获得当前栈顶Activity
    public Activity currentActivity() {
        Activity activity = null;
        if(!activityStack.empty())
            activity= activityStack.lastElement();
        return activity;
    }
    //将当前Activity推入栈中
    public void pushActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
//        Log.e("ScreenManager","pushActivity..."+activity);

    }
    //弹出栈中所有Activity
    public void popAllActivityExceptOne(Activity exceptOne) {
        while (true) {
            Activity activity = currentActivity();
//            Log.e("ScreenManager","popAllActivityExceptOne..."+activity);

            if (activity == null) {
                break;
            }
            if (exceptOne!=null&&exceptOne==activity) {
                break;
            }
            popActivity(activity);
        }
    }

} 

