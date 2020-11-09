package com.dy.travel;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;



public class LoadingActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        new Thread(){
            public void run(){
                try {Thread.sleep(1000);}catch (InterruptedException e){}
        		 //跳转到主Activity
                Intent i = new Intent(LoadingActivity.this,MainActivity.class);
                startActivity(i);
                //刷新，使得返回键不能回到本Activity
                finish();
            }
        }.start();
    }


}
