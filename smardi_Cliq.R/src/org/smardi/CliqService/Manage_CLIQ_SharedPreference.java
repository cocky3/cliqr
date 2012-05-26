package org.smardi.CliqService;

import android.content.*;

public class Manage_CLIQ_SharedPreference {
	private Context mContext;
	private SharedPreferences mPref;
	private SharedPreferences.Editor mEditor;
	final String preferenceName = "smardiFFTService_wireless";
	
		
	public Manage_CLIQ_SharedPreference(Context context) {
		mContext = context;
		mPref = mContext.getSharedPreferences(preferenceName, Context.MODE_WORLD_WRITEABLE);
		mEditor = mPref.edit();
	}

	public int getCliqFrequency() {
		return mPref.getInt("coolickerFreq", 0);
	}
	
	public void setCliqFrequency(int freq) {
		mEditor.putInt("coolickerFreq", freq);
		mEditor.commit();
	}
	
	public int getCliqFrequencyIndex() {
		return mPref.getInt("coolickerFreqIndex", 0);
	}
	
	public void setCliqFrequencyIndex(int freq) {
		mEditor.putInt("coolickerFreqIndex", freq);
		mEditor.commit();
	}
	
	
	public void setCliqTempFrequencyIndex(int index) {
		mEditor.putInt("coolickerTempFreqIndex", index);
		mEditor.commit();
	}
	
	public int getCLIQTempFrequencyIndex() {
		return mPref.getInt("coolickerTempFreqIndex", 0);
	}
	
	
	public int getThreadholdPower() {
		return mPref.getInt("threadholdPower", 80);
	}
	
	public void setThreadholdPower(int power) {
		mEditor.putInt("threadholdPower", power);
		mEditor.commit();
	}
	
	
	/**
	 * Preference 다이얼로그에서 입력받은 감도를 저장
	 * 
	 * @param sensitivity 다이얼로그에서 입력받는 민감도 (0~100)
	 */
	public void setCliqSensitivity(int sensitivity) {
		mEditor.putInt("CliqSensitivity", sensitivity);
		mEditor.commit();
	}
	
	public int getCliqSensitivity () {
		return mPref.getInt("CliqSensitivity", 40);
	}
}
