package org.smardi.CliqService;

import org.smardi.Cooliq.R.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class AC_SetCliqFrequency extends Activity {

	ImageView btn_next;
	TextView txt_dialogContent;
	ProgressDialog dialog;
	
	Manage_CLIQ_SharedPreference mPref = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.dialog_regist);

		btn_next = (ImageView) findViewById(R.id.btn_next);
		btn_next.setOnClickListener(onClickListener);
		
		mPref = new Manage_CLIQ_SharedPreference(AC_SetCliqFrequency.this);
		
		txt_dialogContent = (TextView)findViewById(R.id.txt_dialogContent);

		Intent intent = new Intent(AC_SetCliqFrequency.this, Service_Cliq.class);
		startService(intent);
		
		initCTimer();
	}

	@Override
	protected void onDestroy() {
		
		Intent intent = new Intent();
		intent.setAction(Service_Cliq.ACTION_CLIQ_REGISTRATION_CLOSE);	
		//intent.setAction(Service_Cliq.ACTION_CLIQ_STOP);	
		sendBroadcast(intent);
		//stopService(intent);
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Service_Cliq.ACTION_SEND_CLIQ_FREQUENCY);
		filter.addAction(Service_Cliq.ACTION_CLIQ_REGISTRATION_FAIL);
		
		registerReceiver(mBroadcastReceiver, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		unregisterReceiver(mBroadcastReceiver);
		cTimer.cancel();
		finish();
	}

	private View.OnClickListener onClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.btn_next:
				cTimer.start();
				break;
			}
			
		}
	};
	
	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(Service_Cliq.ACTION_SEND_CLIQ_FREQUENCY)) {
				dialog.cancel();
				
				txt_dialogContent.setText(getResources().getString(R.string.dialog_regist_content_done));
				
				btn_next.setImageDrawable(getResources().getDrawable(R.drawable.c_btn_done));
				btn_next.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						finish();
					}
				});
			}
			else if(action.equals(Service_Cliq.ACTION_CLIQ_REGISTRATION_FAIL)) {
				dialog.cancel();
				
				mPref.setCliqFrequency(0);	//실패하면 0으로 초기화 함
				
				txt_dialogContent.setText(getResources().getString(R.string.dialog_regist_fail));
				
				btn_next.setImageDrawable(getResources().getDrawable(R.drawable.c_btn_next));
				btn_next.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						initCTimer();
						cTimer.start();
						sendBroadcast(new Intent()
						.setAction(Service_Cliq.ACTION_CLIQ_REGISTRATION_RESTART));
					}
				});
			}
		}
	};
	
	
	
	
	
	
	//1초에 한번씩 깜빡이는 동그라미를 위한 타이머
	CountDownTimer cTimer = null;
	
	private void initCTimer() {
		cTimer = new CountDownTimer(10000, 1000) {
			int count = 0;
			Intent intent;

			@Override
			public void onTick(long millisUntilFinished) {
				
				Log.e("test", "count:"+count);
				switch (count) {
				case 0:
					btn_next.setOnClickListener(null);
					btn_next.setImageDrawable(getResources().getDrawable(
							R.drawable.count_3));
					break;
				case 1:
					btn_next.setImageDrawable(getResources().getDrawable(
							R.drawable.count_2));
					break;
				case 2:
					btn_next.setImageDrawable(getResources().getDrawable(
							R.drawable.count_1));
					break;/*
				case 3:
					btn_next.setImageDrawable(getResources().getDrawable(
							R.drawable.count_0));
					break;*/
				case 3:
				case 5:
				case 7:
				case 9:
					btn_next.setImageDrawable(getResources().getDrawable(
							R.drawable.test_green));
					intent = new Intent();
					intent.setAction(Service_Cliq.ACTION_GREEN_BUTTON_IS_ON);
					sendBroadcast(intent);
					break;
				case 4:
				case 6:
				case 8:
				case 10:
					btn_next.setImageDrawable(getResources().getDrawable(
							R.drawable.test_gray));
					intent = new Intent();
					intent.setAction(Service_Cliq.ACTION_GRAY_BUTTON_IS_ON);
					sendBroadcast(intent);
					break;
				}
				count++;
			}

			@Override
			public void onFinish() {
				btn_next.setImageDrawable(getResources().getDrawable(
						R.drawable.test_gray));
				
				dialog = ProgressDialog.show(AC_SetCliqFrequency.this,
						getResources().getString(R.string.progressdialog_title),
						getResources().getString(R.string.progressdialog_msg));
				
				Intent intent = new Intent();
				intent.setAction(Service_Cliq.ACTION_CLIQ_REGISTRATION_END);
				sendBroadcast(intent);
			}
		};
	}
}
