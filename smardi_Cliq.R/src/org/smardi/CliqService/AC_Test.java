package org.smardi.CliqService;


import org.smardi.Cooliq.R.*;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AC_Test extends Activity {

	Button btn_start;
	Button btn_test_start;
	Button btn_gray_on;
	Button btn_end;
	Button btn_mode_none, btn_mode_amplitude, btn_mode_frequency;
	
	TextView txt_clicked;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		btn_start = (Button)findViewById(R.id.btn_start);
		btn_test_start = (Button)findViewById(R.id.btn_test_start);
		btn_gray_on = (Button)findViewById(R.id.btn_gray_on);
		btn_end = (Button)findViewById(R.id.btn_end);
		btn_mode_amplitude = (Button)findViewById(R.id.btn_mode_amplitude);
		btn_mode_frequency = (Button)findViewById(R.id.btn_mode_frequency);
		btn_mode_none = (Button)findViewById(R.id.btn_mode_none);
		
		btn_start.setOnClickListener(onClickListener);
		btn_test_start.setOnClickListener(onClickListener);
		btn_gray_on.setOnClickListener(onClickListener);
		btn_end.setOnClickListener(onClickListener);
		btn_mode_amplitude.setOnClickListener(onClickListener);
		btn_mode_frequency.setOnClickListener(onClickListener);
		btn_mode_none.setOnClickListener(onClickListener);
		
		Intent intent = new Intent(AC_Test.this, Service_Cliq.class);
		startService(intent);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Service_Cliq.ACTION_CLIQ_CLICK_TRIGERED);
		filter.addAction(Service_Cliq.ACTION_CLIQ_DOUBLECLICK_TRIGERED);
		filter.addAction(Service_Cliq.ACTION_CLIQ_LONGCLICK_TRIGERED);
		filter.addAction(Service_Cliq.ACTION_CLIQ_RELEASE_TRIGERED);
		filter.addAction(Service_Cliq.ACTION_SEND_CLIQ_FREQUENCY);
		registerReceiver(mBroadcastReceiver, filter);
		
		//cTimer.start();
		txt_clicked = (TextView)findViewById(R.id.txt_clicked);
	}
	
	
	
	@Override
	protected void onDestroy() {
		Intent intent = new Intent(AC_Test.this, Service_Cliq.class);
		stopService(intent);
		
		unregisterReceiver(mBroadcastReceiver);
		
		intent = new Intent();
		intent.setAction(Service_Cliq.ACTION_CLIQ_TEST_END);
		sendBroadcast(intent);
		
		cTimer.cancel();
		finish();
		super.onDestroy();
	}



	View.OnClickListener onClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent();
			switch(v.getId()) {
			case R.id.btn_start:
				intent.setAction(Service_Cliq.ACTION_CLIQ_REGISTRATION_START);
				break;
			case R.id.btn_test_start:
				intent.setAction(Service_Cliq.ACTION_CLIQ_TEST_START);
				break;
			case R.id.btn_gray_on:
				intent.setAction(Service_Cliq.ACTION_GRAY_BUTTON_IS_ON);
				break;
			case R.id.btn_end:
				intent.setAction(Service_Cliq.ACTION_CLIQ_REGISTRATION_END);
				break;
			case R.id.btn_mode_none:
				intent.setAction(Service_Cliq.ACTION_MODE_NONE_ON);
				break;
			case R.id.btn_mode_amplitude:
				intent.setAction(Service_Cliq.ACTION_MODE_AMPLITUDE_ON);
				break;
			case R.id.btn_mode_frequency:
				intent.setAction(Service_Cliq.ACTION_MODE_CLIQING_ON);
				break;
				
			}
			sendBroadcast(intent);
		}
	};
	
	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(Service_Cliq.ACTION_CLIQ_CLICK_TRIGERED)) {
				//Toast.makeText(BoradCastTest.this, "Ŭ��!", 1000).show();
				txt_clicked.setText("Click!");
			} else if(action.equals(Service_Cliq.ACTION_CLIQ_DOUBLECLICK_TRIGERED)) {
				txt_clicked.setText("DoubleClick!");
			} else if(action.equals(Service_Cliq.ACTION_CLIQ_LONGCLICK_TRIGERED)) {
				txt_clicked.setText("LongClick!");
			} else if(action.equals(Service_Cliq.ACTION_CLIQ_RELEASE_TRIGERED)) {
				txt_clicked.setText("Release!");
			}
		}
	};
	
	CountDownTimer cTimer = new CountDownTimer(999999999, 100) {
		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			txt_clicked.setText("XXXXXXXXXXXXXXXXX");
		}
		
		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			
		}
	};
}
