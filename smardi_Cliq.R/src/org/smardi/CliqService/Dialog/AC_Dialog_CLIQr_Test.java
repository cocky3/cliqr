package org.smardi.CliqService.Dialog;

import org.smardi.CliqService.*;
import org.smardi.Cooliq.R.*;
import org.smardi.Cooliq.R.Frequency.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class AC_Dialog_CLIQr_Test extends Activity {

	Frequency mFreq = null;
	
	Manage_CLIQ_SharedPreference mPref = null;
	
	Button btn_cancel = null;
	ImageView img_cliq = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ac_dialog_cliqr_test);
		
		mFreq  = Frequency.getInstance(AC_Dialog_CLIQr_Test.this);
		mFreq.startRecording();
		mFreq.isCliqrTesting = true;
		
		mPref = new Manage_CLIQ_SharedPreference(AC_Dialog_CLIQr_Test.this);
		
		btn_cancel = (Button)findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(viewClickListener);
		img_cliq = (ImageView)findViewById(R.id.img_cliqr);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Frequency.ACTION_CLIQ_DETECTED);
		filter.addAction(Frequency.ACTION_CLIQ_NOT_DETECTED);
		registerReceiver(mReceiver, filter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		/*if(mPref.isCLIQrMode() == true) {
			//클리커 모드가 켜져있을 때에는 끄지 않는다
		} else {
			mFreq.stopRecording();
		}*/
		mFreq.stopRecording();
		mFreq.isCliqrTesting = false;
		unregisterReceiver(mReceiver);
	}

	
	View.OnClickListener viewClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.btn_cancel:
				finish();
				break;
			}
		}
	};

	
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Frequency.ACTION_CLIQ_DETECTED)) {
				img_cliq.setImageDrawable(getResources().getDrawable(R.drawable.cliqr_regist_1));
			}
			
			else if(intent.getAction().equals(Frequency.ACTION_CLIQ_NOT_DETECTED)) {
				img_cliq.setImageDrawable(getResources().getDrawable(R.drawable.cliqr_regist_0));
			}
		}
	};
	
}
