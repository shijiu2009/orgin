package com.dy.travel.wxapi;

import com.dy.travel.MainActivity;
import com.dy.travel.R;
import com.dy.travel.comm.Constant;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
	
	private static final String TAG = "com.dy.travel.wxapi.WXPayEntryActivity";
	
    private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);
        
    	api = WXAPIFactory.createWXAPI(this, Constant.WX_APP_ID);
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@SuppressLint("LongLogTag")
	@Override
	public void onResp(BaseResp resp) {
		Log.e(TAG, "onPayFinish, errCode = " + resp.errCode);

		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			if (resp.errCode == 0) {// 用户支付成功
				Toast.makeText(this, "支付成功", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(this , MainActivity. class);
				intent.putExtra("isPaytrue", true);
				startActivity(intent);
			} else if (resp.errCode == -2) {// 用户取消支付
				Toast.makeText(this, "取消支付", Toast.LENGTH_SHORT).show();
			} else if (resp.errCode == -1) {// 支付失败
				Toast.makeText(this, "支付失败:"+resp.errCode+","+resp.errStr, Toast.LENGTH_SHORT).show();
			}
			finish();
		}
	}
}