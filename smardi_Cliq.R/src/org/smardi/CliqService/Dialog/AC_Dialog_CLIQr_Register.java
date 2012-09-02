package org.smardi.CliqService.Dialog;

import java.util.*;

import org.smardi.CliqService.*;
import org.smardi.Cooliq.R.*;
import org.smardi.Cooliq.R.Frequency.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class AC_Dialog_CLIQr_Register extends Activity {
	boolean D = false;
	String TAG = "smardi_FakeCall";
	
	ImageView img_cliqr = null;

	Button btn_cancel = null;
	
	TextView regist_progress = null;

	Frequency mFreq = null;
	
	private final int MAX_COUNT = 21;
	
	int final_CLIQr_freq = 0;
	
	ArrayList<CollectedFreqency> listFrequency = null;
	
	Manage_CLIQ_SharedPreference mPref = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ac_dialog_cliqr_register);

		initComponent();

		mPref = new Manage_CLIQ_SharedPreference(AC_Dialog_CLIQr_Register.this);
		
		mFreq = Frequency.getInstance(AC_Dialog_CLIQr_Register.this);
		mFreq.isCliqrRegistation = true;
		mFreq.startRecording();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Frequency.ACTION_CLIQ_REGIST_HIGHEST_FREQUENCY);
		registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		/*if(mPref.getisCLIQrMode() == true) {
			//클리커 모드가 켜져있을 때에는 끄지 않는다
		} else {
			mFreq.stopRecording();
		}*/
		mFreq.stopRecording();
		mFreq.isCliqrRegistation = false;
		unregisterReceiver(mReceiver);
	}

	private void initComponent() {
		img_cliqr = (ImageView) findViewById(R.id.img_cliqr);
		btn_cancel = (Button) findViewById(R.id.btn_regist_cancel);
		regist_progress = (TextView)findViewById(R.id.regist_progress);
		
		listFrequency = new ArrayList<CollectedFreqency>();
		
		registEvent();
	}

	private void registEvent() {
		btn_cancel.setOnClickListener(viewClickListener);
	}

	// -----------------------------------------------
	View.OnClickListener viewClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_regist_cancel:
				exit();
			}
		}
	};
	
	private void exit() {
		if(0 < final_CLIQr_freq) {
			//클리커 등록이 끝났을 때
			apply();
			finish();
		} else {
			//아직 등록하지 않았을 때
			finish();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
		}
		
		return super.onKeyDown(keyCode, event);
	}

	protected void apply() {
		Log.e("CC", "FFF:"+final_CLIQr_freq);
		mPref.setCliqFrequency(final_CLIQr_freq);
		mPref.setCliqFrequencyIndex(mFreq.convertFrequencyToIndex(final_CLIQr_freq));
	}

	
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Frequency.ACTION_CLIQ_REGIST_HIGHEST_FREQUENCY)) {
				int CLIQr_Freq = intent.getIntExtra("freq", 0);
				
				if(0 < CLIQr_Freq) {
					collectCLIQrFreq(CLIQr_Freq);
				} else {
					img_cliqr.setImageDrawable(getResources().getDrawable(R.drawable.cliqr_regist_0));
				}
			}
		}
	};
	
	
	
	private void collectCLIQrFreq(int CLIQr_Freq) {
		img_cliqr.setImageDrawable(getResources().getDrawable(R.drawable.cliqr_regist_1));
		
		boolean isCountAdded = false;
		
		for (int i=0; i < listFrequency.size(); i++) {
			if(listFrequency.get(i).getFrequency() == CLIQr_Freq) {
				listFrequency.get(i).addCount();
				isCountAdded = true;
				break;
			}
		}
		
		if(isCountAdded == false) {
			//기존에 등록되어있던 주파수가 아닐 경우
			listFrequency.add(new CollectedFreqency(CLIQr_Freq));
		}
		
		//-----------------
		int tempMaxCount = 0;
		int tempMaxIndex = 0;
		for (int i=0; i<listFrequency.size(); i++) {
			if(D) {
				Log.e(TAG, "freq:"+listFrequency.get(i).getFrequency()+"\t\t\tcount:"+listFrequency.get(i).getCount());
			}
			
			CollectedFreqency currentFreq = listFrequency.get(i);
			if(tempMaxCount < currentFreq.getCount()) {
				tempMaxCount = currentFreq.getCount();
				tempMaxIndex = i;
			}
		}
		
		int progress = tempMaxCount * 100/MAX_COUNT;
		if(100 < progress) {
			progress = 100;
		}
		regist_progress.setText(progress+"%");
		
		if(99 < progress) {
			final_CLIQr_freq = listFrequency.get(tempMaxIndex).getFrequency();
			registerComplete();
		}
		//-----------------
	}
	
	/**
	 * 수집된 주파수 정보를 저장하기 위한 클래스
	 * @author floro
	 *
	 */
	class CollectedFreqency {
		private int freq = 0;
		private int count = 0;
		
		public CollectedFreqency (int freq) {
			this.freq = freq;
			count = 0;
		}
		
		public void addCount() {
			count ++;
		}
		
		public int getFrequency() {
			return freq;
		}
		
		public int getCount() {
			return count;
		}
	}


	protected void registerComplete() {
		//녹음 정지
		mFreq.stopRecording();
		//버튼 색 초록색으로
		img_cliqr.setImageDrawable(getResources().getDrawable(R.drawable.cliqr_regist_2));
		//버튼 내용 바꾸기
		btn_cancel.setText("등록 완료");
	}

	
	Handler mHandler = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			
			
			return false;
		}
	});
	
}
