package org.smardi.Cooliq.R;

import org.smardi.CliqService.*;

import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.*;
import android.graphics.drawable.*;
import android.hardware.Camera.Size;
import android.os.*;
import android.preference.*;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.*;

import com.mnm.seekbarpreference.*;

public class PA_Setting extends PreferenceActivity {

	private final boolean D = true;
	private final String TAG = "smardi.Cliq";

	Manage_Camera_SharedPreference mCameraPref;
	Manage_CLIQ_SharedPreference mCliqPref;

	CameraParameters mCameraParams;

	ListPreference list_Camera_SceneMode;
	ListPreference list_Camera_WhiteBalance;
	ListPreference list_Camera_ColorEffect;
	ListPreference list_Camera_PictureSize_BACK;
	ListPreference list_Camera_PictureSize_FRONT;

	private final String KEY_CLIQ_REGIST = "org.smardi.cliq.regist";
	private final String KEY_CLIQ_TEST = "org.smardi.cliq.test";
	private final String KEY_CLIQ_SENSITIVITY = "org.smardi.cliq.sensitivity";

	private final String KEY_CAMERA_SCENEMODE = "org.smardi.cliq.scenemode";
	private final String KEY_CAMERA_WHITEBALANCE = "org.smardi.cliq.whitebalance";
	private final String KEY_CAMERA_COLOREFFECT = "org.smardi.cliq.coloreffect";
	private final String KEY_CAMERA_PICTURESIZE_BACK = "org.smardi.cliq.picturesize.back";
	private final String KEY_CAMERA_PICTURESIZE_FRONT = "org.smardi.cliq.picturesize.front";
	
	private final String KEY_TUTORIAL = "org.smardi.cliq.tutorial";
	private final String KEY_ExplainComponent = "org.smardi.cliq.explainComponent";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCameraPref = new Manage_Camera_SharedPreference(this);
		mCliqPref = new Manage_CLIQ_SharedPreference(this);

		mCameraParams = CameraParameters.getInstance(null);

		setPreferenceScreen(createPreferenceHierarchy());

		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(
						onSharedChangeListener);
		
		
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0x66, 0x00, 0x00, 0x00)));
		getWindow().setLayout(getWindowManager().getDefaultDisplay().getWidth()*3/5, getWindowManager().getDefaultDisplay().getHeight()*4/5);
	}
	
	

	private PreferenceScreen createPreferenceHierarchy() {

		// Root
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(
				this);

		// CLIQ.r 주파수 등록

		for (int menuIdx = 0; menuIdx < 2; menuIdx++) {
			if((mCliqPref.getCliqFrequency() == 0 && menuIdx == 0) || (0 < mCliqPref.getCliqFrequency() && menuIdx == 1)) {
				
				// Inline preferences
				PreferenceCategory CliqPrefCat = new PreferenceCategory(this);
				CliqPrefCat.setTitle(getString(R.string.setting_category_1st_title));
				root.addPreference(CliqPrefCat);
		
				// CLIQ.r 주파수 등록 dialog 띄우기
				Preference preference_CLIQ_regist = new Preference(this);
				preference_CLIQ_regist.setKey(KEY_CLIQ_REGIST);
				preference_CLIQ_regist
						.setTitle(getString(R.string.setting_registration_title));
				preference_CLIQ_regist
						.setSummary(getString(R.string.setting_registration_content));
				preference_CLIQ_regist
						.setOnPreferenceClickListener(onPreferenceClickListener);
				CliqPrefCat.addPreference(preference_CLIQ_regist);
		
				// CLIQ.r 등록 확인 테스트 dialog 띄우기
				Preference preference_CLIQ_test = new Preference(this);
				preference_CLIQ_test.setKey(KEY_CLIQ_TEST);
				preference_CLIQ_test
						.setTitle(getString(R.string.setting_communication_test_title));
				preference_CLIQ_test
						.setSummary(getString(R.string.setting_communication_test_content));
				preference_CLIQ_test
						.setOnPreferenceClickListener(onPreferenceClickListener);
				CliqPrefCat.addPreference(preference_CLIQ_test);
		
				// CLIQ.r 수신감도 Preference
				SeekBarPreference preference_CLIQ_sensitivity = new SeekBarPreference(
						this, null);
				preference_CLIQ_sensitivity.setKey(KEY_CLIQ_SENSITIVITY);
				preference_CLIQ_sensitivity
						.setTitle(getString(R.string.setting_change_sensitivity_title));
				preference_CLIQ_sensitivity
						.setSummary(getString(R.string.setting_change_sensitivity_content));
				CliqPrefCat.addPreference(preference_CLIQ_sensitivity);
		
				
			}
			
			if(menuIdx == 0) {
				// --------------------------------------------------------
				// Camera 설정
				PreferenceCategory cameraPrefCat = new PreferenceCategory(this);
				cameraPrefCat.setTitle(getString(R.string.setting_category_2nd_title));
				root.addPreference(cameraPrefCat);
		
				//사진 해상도 설정++++++++
				if (mCameraPref.getWhichCamera() == Surface_Picture_Preview.CAMERA_BACK) {
					// Back 사진 크기
					int width = mCameraPref.getPictureSizes_BACK()[0];
					int height = mCameraPref.getPictureSizes_BACK()[1];
					String pixel = String.format("%.1f", Math.round(width*height/100000f)/10f);
					String currentBackSize = width+ " x " + height + " ("+pixel+"M)";
					
					list_Camera_PictureSize_BACK = new ListPreference(this);
					list_Camera_PictureSize_BACK.setKey(KEY_CAMERA_PICTURESIZE_BACK);
					list_Camera_PictureSize_BACK.setEntries(mCameraPref
							.getPictureSizeBackList());
					list_Camera_PictureSize_BACK.setEntryValues(mCameraPref
							.getPictureSizeBackList());
					list_Camera_PictureSize_BACK
							.setDialogTitle(getString(R.string.setting_rear_resolution_title));
					list_Camera_PictureSize_BACK
							.setTitle(getString(R.string.setting_rear_resolution_title));
					list_Camera_PictureSize_BACK.setSummary(currentBackSize);
					cameraPrefCat.addPreference(list_Camera_PictureSize_BACK);
				} else {
					// Front 사진 크기
					String currentFrontSize = mCameraPref.getPictureSizes_FRONT()[0]
							+ " x " + mCameraPref.getPictureSizes_FRONT()[1];
					list_Camera_PictureSize_FRONT = new ListPreference(this);
					list_Camera_PictureSize_FRONT.setKey(KEY_CAMERA_PICTURESIZE_FRONT);
					list_Camera_PictureSize_FRONT.setEntries(mCameraPref
							.getPictureSizeFrontList());
					list_Camera_PictureSize_FRONT.setEntryValues(mCameraPref
							.getPictureSizeFrontList());
					list_Camera_PictureSize_FRONT
							.setDialogTitle(getString(R.string.setting_front_resolution_title));
					list_Camera_PictureSize_FRONT
							.setTitle(getString(R.string.setting_front_resolution_title));
					list_Camera_PictureSize_FRONT.setSummary(currentFrontSize);
					cameraPrefCat.addPreference(list_Camera_PictureSize_FRONT);
				}
				
				
				// 컬러이팩트++++++++
				list_Camera_ColorEffect = new ListPreference(this);
				list_Camera_ColorEffect.setKey(KEY_CAMERA_COLOREFFECT);
		
				// 컬러이팩트를 한글로 바꾼다
				String[] translatedColorEffectList = mCameraPref.getColorEffectList();
				String[] colorEffect_en = getResources().getStringArray(
						R.array.colorEffect_en);
				String[] colorEffect_ko = getResources().getStringArray(
						R.array.colorEffect_ko);
		
				for (int i = 0; i < translatedColorEffectList.length; i++) {
					for (int j = 0; j < colorEffect_en.length; j++) {
						if (translatedColorEffectList[i].equals(colorEffect_en[j]) == true) {
							translatedColorEffectList[i] = colorEffect_ko[j];
							break;
						}
					}
				}
				
				if(getString(R.string.language).equals("ko")== true) {
					list_Camera_ColorEffect.setEntries(translatedColorEffectList);
				} else {
					list_Camera_ColorEffect.setEntries(mCameraPref.getColorEffectList());
				}
				list_Camera_ColorEffect
						.setEntryValues(mCameraPref.getColorEffectList());
				list_Camera_ColorEffect
						.setDialogTitle(getString(R.string.setting_color_effect_title));
				list_Camera_ColorEffect
						.setTitle(getString(R.string.setting_color_effect_title));
		
				String summaryColorEffect = mCameraPref.getColorEffect();
				if(getString(R.string.language).equals("ko")== true) {
					summaryColorEffect = translateColorEffet(summaryColorEffect);
				}
		
				list_Camera_ColorEffect.setSummary(summaryColorEffect);
				cameraPrefCat.addPreference(list_Camera_ColorEffect);
		
				//장면 모드가 Auto가 아니면 화이트벨런스를 disable 한다.
				if(mCameraPref.getSceneMode().toLowerCase().equals("auto")) {
					list_Camera_ColorEffect.setEnabled(true);
				} else {
					list_Camera_ColorEffect.setEnabled(false);
				}
				
				
				// 화이트 벨런스++++++++
				list_Camera_WhiteBalance = new ListPreference(this);
				list_Camera_WhiteBalance.setKey(KEY_CAMERA_WHITEBALANCE);
		
				// 화이트 벨런스를 한글로 바꾼다
				String[] translatedWhiteBalanceList = mCameraPref.getWhiteBalanceList();
				String[] whiteBalance_en = getResources().getStringArray(
						R.array.whiteBalance_en);
				String[] whiteBalance_ko = getResources().getStringArray(
						R.array.whiteBalance_ko);
		
				for (int i = 0; i < translatedWhiteBalanceList.length; i++) {
					for (int j = 0; j < whiteBalance_en.length; j++) {
						if (translatedWhiteBalanceList[i].equals(whiteBalance_en[j]) == true) {
							translatedWhiteBalanceList[i] = whiteBalance_ko[j];
							break;
						}
					}
				}
				
				if(getString(R.string.language).equals("ko")== true) {
					list_Camera_WhiteBalance.setEntries(translatedWhiteBalanceList);
				} else {
					list_Camera_WhiteBalance.setEntries(mCameraPref
							.getWhiteBalanceList());
				}
				
				list_Camera_WhiteBalance.setEntryValues(mCameraPref
						.getWhiteBalanceList());
				list_Camera_WhiteBalance
						.setDialogTitle(getString(R.string.setting_white_balance_title));
				list_Camera_WhiteBalance
						.setTitle(getString(R.string.setting_white_balance_title));
		
				String summaryWhiteBalance = mCameraPref.getWhiteBalance();
				
				if(getString(R.string.language).equals("ko")== true) {
					summaryWhiteBalance = translateWhiteBalance(summaryWhiteBalance);
				}
		
				list_Camera_WhiteBalance.setSummary(summaryWhiteBalance);
				cameraPrefCat.addPreference(list_Camera_WhiteBalance);
		
				//장면 모드가 Auto가 아니면 화이트벨런스를 disable 한다.
				if(mCameraPref.getSceneMode().toLowerCase().equals("auto")) {
					list_Camera_WhiteBalance.setEnabled(true);
				} else {
					list_Camera_WhiteBalance.setEnabled(false);
				}
				
				
				
				
				
				
				
				// 카메라 장면모드++++++++
				list_Camera_SceneMode = new ListPreference(this);
				list_Camera_SceneMode.setKey(KEY_CAMERA_SCENEMODE);
		
				// 모드를 한글로 바꾼다
				String[] translatedSceneModeList = mCameraPref.getSceneModeList();
				String[] sceneMode_en = getResources().getStringArray(
						R.array.sceneMode_en);
				String[] sceneMode_ko = getResources().getStringArray(
						R.array.sceneMode_ko);
		
				for (int i = 0; i < translatedSceneModeList.length; i++) {
					for (int j = 0; j < sceneMode_en.length; j++) {
						if (translatedSceneModeList[i].equals(sceneMode_en[j]) == true) {
							translatedSceneModeList[i] = sceneMode_ko[j];
							break;
						}
					}
				}
				
				if(getString(R.string.language).equals("ko")== true) {
					list_Camera_SceneMode.setEntries(translatedSceneModeList);
				} else {
					mCameraPref.getSceneModeList();
				}
				
				list_Camera_SceneMode.setEntryValues(mCameraPref.getSceneModeList());
				list_Camera_SceneMode
						.setDialogTitle(getString(R.string.setting_scene_mode_title));
				list_Camera_SceneMode
						.setTitle(getString(R.string.setting_scene_mode_title));
		
				String summarySceneMode = mCameraPref.getSceneMode();
				
				if(getString(R.string.language).equals("ko")== true) {
					summarySceneMode = translateSceneMode(summarySceneMode);
				}
		
				list_Camera_SceneMode.setSummary(summarySceneMode);
				cameraPrefCat.addPreference(list_Camera_SceneMode);
			}
		}
		
		
		
		
		
		//---도움말
		// Inline preferences
		PreferenceCategory HelpCat = new PreferenceCategory(this);
		HelpCat.setTitle(getString(R.string.setting_category_help));
		root.addPreference(HelpCat);
		
		// 튜토리얼
		Preference preference_Tutorial = new Preference(this);
		preference_Tutorial.setKey(KEY_TUTORIAL);
		preference_Tutorial
				.setTitle(getString(R.string.setting_title_tutorial));
		preference_Tutorial
				.setSummary(getString(R.string.setting_content_tutorial));
		preference_Tutorial
				.setOnPreferenceClickListener(onPreferenceClickListener);
		HelpCat.addPreference(preference_Tutorial);
		
		
		//화면 구성 보기
		Preference preference_ExplainComponent = new Preference(this);
		preference_ExplainComponent.setKey(KEY_ExplainComponent);
		preference_ExplainComponent
				.setTitle(getString(R.string.setting_title_explain_component));
		preference_ExplainComponent
				.setSummary(getString(R.string.setting_content_explain_component));
		preference_ExplainComponent
				.setOnPreferenceClickListener(onPreferenceClickListener);
		HelpCat.addPreference(preference_ExplainComponent);
		
		/*
		 * // Edit text preference EditTextPreference editTextPref = new
		 * EditTextPreference(this);
		 * editTextPref.setDialogTitle(R.string.dialog_title_edittext_preference
		 * ); editTextPref.setKey("edittext_preference");
		 * editTextPref.setTitle(R.string.title_edittext_preference);
		 * editTextPref.setSummary(R.string.summary_edittext_preference);
		 * dialogBasedPrefCat.addPreference(editTextPref);
		 * 
		 * // List Preference ListPreference listPref = new
		 * ListPreference(this); listPref.setEntries(new String[] { "A", "B",
		 * "C", "D", "E" }); listPref.setEntryValues(new String[] { "a", "b",
		 * "c", "d", "e" });
		 * listPref.setDialogTitle(R.string.dialog_title_list_preference);
		 * listPref.setSummary(R.string.summary_list_preference);
		 * dialogBasedPrefCat.addPreference(listPref);
		 */
		/*
		 * // Launch preference PreferenceCategory launchPrefCat = new
		 * PreferenceCategory(this);
		 * launchPrefCat.setTitle(R.string.launch_preferences);
		 * root.addPreference(launchPrefCat);
		 * 
		 * // Screen preference PreferenceScreen screenPref =
		 * getPreferenceManager() .createPreferenceScreen(this);
		 * screenPref.setKey("screen_preference");
		 * screenPref.setTitle(R.string.title_screen_preference);
		 * screenPref.setSummary(R.string.summary_screen_preference);
		 * launchPrefCat.addPreference(screenPref);
		 * 
		 * CheckBoxPreference nextScreenCheckBoxPref = new
		 * CheckBoxPreference(this);
		 * nextScreenCheckBoxPref.setKey("next_screentoggle_preference");
		 * nextScreenCheckBoxPref
		 * .setTitle(R.string.title_next_screen_toggle_preference);
		 * nextScreenCheckBoxPref
		 * .setSummary(R.string.summary_next_screen_toggle_preference);
		 * screenPref.addPreference(nextScreenCheckBoxPref);
		 * 
		 * // Intent preference PreferenceScreen intentPref =
		 * getPreferenceManager() .createPreferenceScreen(this);
		 * intentPref.setIntent(new Intent().setAction(Intent.ACTION_VIEW)
		 * .setData(Uri.parse("http://www.android.com")));
		 * intentPref.setTitle(R.string.title_intent_preference);
		 * intentPref.setSummary(R.string.summary_intent_preference);
		 * launchPrefCat.addPreference(intentPref);
		 * 
		 * // Preference attributes PreferenceCategory prefAttrsCat = new
		 * PreferenceCategory(this);
		 * prefAttrsCat.setTitle(R.string.preference_attributes);
		 * root.addPreference(prefAttrsCat);
		 * 
		 * // Visual parent toggle preference CheckBoxPreference
		 * parentCheckBoxPref = new CheckBoxPreference(this);
		 * parentCheckBoxPref.setTitle(R.string.title_parent_preference);
		 * parentCheckBoxPref.setSummary(R.string.summary_parent_preference);
		 * prefAttrsCat.addPreference(parentCheckBoxPref);
		 */
		return root;
	}

	OnPreferenceClickListener onPreferenceClickListener = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			String key = preference.getKey();

			if (key.equals(KEY_CLIQ_REGIST)) {
				sendBroadcast(new Intent()
						.setAction(Service_Cliq.ACTION_CLIQ_REGISTRATION_START));
			} else if (key.equals(KEY_CLIQ_TEST)) {
				sendBroadcast(new Intent()
						.setAction(Service_Cliq.ACTION_CLIQ_TEST_START));
			} else if (key.equals(KEY_CAMERA_PICTURESIZE_BACK)) {
				String currentSize = mCameraPref.getPictureSizes()[0] + " x "
						+ mCameraPref.getPictureSizes()[1];
				for (int i = 0; i < mCameraPref.getPictureSizeBackList().length; i++) {
					if (currentSize
							.equals(mCameraPref.getPictureSizeBackList()[i])) {
						list_Camera_PictureSize_BACK.setValueIndex(i);
						Log.e("smardi.Cliq", "index:" + i);
						break;
					}
				}
			} else if(key.equals(KEY_TUTORIAL)) {
				startActivity(new Intent(PA_Setting.this, AC_Help_tutorial.class));
			}
			
			else if(key.equals(KEY_ExplainComponent)) {
				Intent intent = new Intent();
				intent.putExtra("tutorial", true);
				PA_Setting.this.setResult(2, intent);
				PA_Setting.this.finish();
			}
			return false;
		}
	};

	OnSharedPreferenceChangeListener onSharedChangeListener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {

			if (key.equals(KEY_CLIQ_SENSITIVITY)) {
				mCliqPref.setCliqSensitivity(sharedPreferences.getInt(
						KEY_CLIQ_SENSITIVITY, 20));
				if (D) {
					Log.i(TAG,
							"sensitivity:"
									+ sharedPreferences.getInt(
											KEY_CLIQ_SENSITIVITY, 20));
				}
			}

			// 장면모드
			else if (key.equals(KEY_CAMERA_SCENEMODE)) {
				String value = sharedPreferences.getString(
						KEY_CAMERA_SCENEMODE, "");

				list_Camera_SceneMode.setSummary(translateSceneMode(value));

				mCameraPref.setSceneMode(sharedPreferences.getString(
						KEY_CAMERA_SCENEMODE, ""));
				
				//카메라에 장면모드 설정
				if(AC_Main.params != null) {
					AC_Main.params.setSceneMode(mCameraPref.getSceneMode());
					AC_Main.mSurface.mCamera.setParameters(AC_Main.params);
				}
				
				if (D) {
					Log.i(TAG,
							"scene mode:"
									+ sharedPreferences.getString(
											KEY_CAMERA_SCENEMODE, ""));
				}
				
				
				//장면 모드가 Auto가 아니면 화이트벨런스를 disable 한다.
				if(mCameraPref.getSceneMode().toLowerCase().equals("auto")) {
					list_Camera_ColorEffect.setEnabled(true);
					list_Camera_WhiteBalance.setEnabled(true);
				} else {
					list_Camera_ColorEffect.setEnabled(false);
					list_Camera_WhiteBalance.setEnabled(false);
				}
			}

			// 화이트 벨런스
			else if (key.equals(KEY_CAMERA_WHITEBALANCE)) {
				String value = sharedPreferences.getString(
						KEY_CAMERA_WHITEBALANCE, "");
				list_Camera_WhiteBalance
						.setSummary(translateWhiteBalance(value));

				mCameraPref.setWhiteBalance(value);
				
				//카메라에 화이트벨런스 설정
				if(AC_Main.params != null) {
					AC_Main.params.setWhiteBalance(mCameraPref.getWhiteBalance());
					AC_Main.mSurface.mCamera.setParameters(AC_Main.params);
				}
				
				if (D) {
					Log.i(TAG, "white balance:" + value);
				}
			}

			// 컬러이팩트
			else if (key.equals(KEY_CAMERA_COLOREFFECT)) {
				String value = sharedPreferences.getString(
						KEY_CAMERA_COLOREFFECT, "");
				list_Camera_ColorEffect.setSummary(translateColorEffet(value));

				mCameraPref.setColorEffect(value);
				
				//카메라에 컬러이펙트 설정
				if(AC_Main.params != null) {
					AC_Main.params.setColorEffect(mCameraPref.getColorEffect());
					AC_Main.mSurface.mCamera.setParameters(AC_Main.params);
				}
				
				if (D) {
					Log.i(TAG, "Color effect:" + value);
				}
			}

			// 후면 카메라 사진 크기
			else if (key.equals(KEY_CAMERA_PICTURESIZE_BACK)) {
				String value = sharedPreferences.getString(
						KEY_CAMERA_PICTURESIZE_BACK, "");
				list_Camera_PictureSize_BACK.setSummary(value);

				String[] tempSize = value.split(" ");
				for (Size size : mCameraParams.getPictureSizes()) {
					if (size.width == Integer.parseInt(tempSize[0])
							&& size.height == Integer.parseInt(tempSize[2])) {
						mCameraPref.setPictureSizes_BACK(size);
					}
				}
			}
			// 전면 카메라 사진 크기
			else if (key.equals(KEY_CAMERA_PICTURESIZE_FRONT)) {
				String value = sharedPreferences.getString(
						KEY_CAMERA_PICTURESIZE_FRONT, "");
				list_Camera_PictureSize_FRONT.setSummary(value);

				String[] tempSize = value.split(" ");
				for (Size size : mCameraParams.getPictureSizes()) {
					if (size.width == Integer.parseInt(tempSize[0])
							&& size.height == Integer.parseInt(tempSize[2])) {
						mCameraPref.setPictureSizes_FRONT(size);
					}
				}
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// 이전에 CLIQ.r 모드가 켜져있었는지 확인하고 적용한다.
		switch (mCameraPref.getModeCLIQ()) {
		case 0:// NONE
			sendBroadcast(new Intent().setAction(Service_Cliq.ACTION_CLIQ_STOP));
			break;
		case 1:// CLIQ.r
		case 2:// Sound
			sendBroadcast(new Intent()
					.setAction(Service_Cliq.ACTION_CLIQ_START));
			break;
		}

		Intent mainIntent = new Intent();
		setResult(AC_Main.ACTIVITY_SETTING, mainIntent);
		finish();
	}

	private String translateWhiteBalance(String value) {

		String[] whiteBalance_en = getResources().getStringArray(
				R.array.whiteBalance_en);
		String[] whiteBalance_ko = getResources().getStringArray(
				R.array.whiteBalance_ko);

		for (int i = 0; i < whiteBalance_en.length; i++) {
			if (value.equals(whiteBalance_en[i]) == true) {
				value = whiteBalance_ko[i];
			}
		}

		return value;
	}

	private String translateSceneMode(String value) {

		String[] sceneMode_en = getResources().getStringArray(
				R.array.sceneMode_en);
		String[] sceneMode_ko = getResources().getStringArray(
				R.array.sceneMode_ko);

		for (int i = 0; i < sceneMode_en.length; i++) {
			if (value.equals(sceneMode_en[i]) == true) {
				value = sceneMode_ko[i];
			}
		}

		return value;
	}

	private String translateColorEffet(String value) {

		String[] colorEffect_en = getResources().getStringArray(
				R.array.colorEffect_en);
		String[] colorEffect_ko = getResources().getStringArray(
				R.array.colorEffect_ko);

		for (int i = 0; i < colorEffect_en.length; i++) {
			if (value.equals(colorEffect_en[i]) == true) {
				value = colorEffect_ko[i];
			}
		}

		return value;
	}
}
