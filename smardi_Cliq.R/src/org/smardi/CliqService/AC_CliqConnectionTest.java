package org.smardi.CliqService;


import org.smardi.Cooliq.R.*;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

public class AC_CliqConnectionTest extends Activity {
	
	Button btn_back;
	ImageView test_circle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cliq_connection_test);
		
		btn_back = (Button)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(mOnClickListener);
		
		test_circle = (ImageView)findViewById(R.id.test_circle);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Service_Cliq.ACTION_CLIQ_TEST_CLICK_TRIGERED);
		filter.addAction(Service_Cliq.ACTION_CLIQ_TEST_RELEASE_TRIGERED);
		
		registerReceiver(mBroadcastReceiver, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		finish();
	}
	
	@Override
	protected void onDestroy() {
		Intent intent = new Intent();
		intent.setAction(Service_Cliq.ACTION_CLIQ_TEST_END);
		sendBroadcast(intent);
		
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}


	View.OnClickListener mOnClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			}
		}
	};
	
	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if(action.equals(Service_Cliq.ACTION_CLIQ_TEST_CLICK_TRIGERED)) {
				test_circle.setImageDrawable(getResources().getDrawable(R.drawable.test_green));
			} else if(action.equals(Service_Cliq.ACTION_CLIQ_TEST_RELEASE_TRIGERED)) {
				test_circle.setImageDrawable(getResources().getDrawable(R.drawable.test_gray));
			}  
		}
	};
}
