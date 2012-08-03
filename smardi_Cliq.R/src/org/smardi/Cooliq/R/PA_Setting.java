package org.smardi.Cooliq.R;

import org.smardi.CliqService.*;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.*;
import android.graphics.drawable.*;
import android.hardware.Camera.Size;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.*;
import android.view.*;
import android.view.View.OnTouchListener;
import android.view.ViewDebug.FlagToString;
import android.widget.*;

import com.mnm.seekbarpreference.*;

public class PA_Setting extends PreferenceActivity {

	private final boolean D = true;
	private final String TAG = "CLIQ.r::PA_Setting";

	Manage_Camera_SharedPreference mCameraPref;
	Manage_CLIQ_SharedPreference mCliqPref;

	private int whichCamera = -1;
	
	CameraParameters mCameraParams;

	ListPreference list_Camera_ContinuousShooting;
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
	private final String KEY_SendEmail = "org.smardi.cliq.sendEmail";

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
		
		whichCamera = this.getIntent().getIntExtra("whichCamera", -1);
		
		Window window = this.getWindow();
		
		window.setBackgroundDrawable(new ColorDrawable(Color.argb(0x66, 0x00, 0x00, 0x00)));
		window.setLayout(getWindowManager().getDefaultDisplay().getWidth()*3/5, getWindowManager().getDefaultDisplay().getHeight()*4/5);
		
		//바깥을 클릭하면 다이얼로그 닫음
		//바깥을 터치할 수 있게 함(모달을 해제)
		window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
		
		//바깥이 터치 된 것을 인식하게 함
		window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if(MotionEvent.ACTION_OUTSIDE == event.getAction()) {
			finish();
			return false;
		}
		
		return super.onTouchEvent(event);
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
					list_Camera_PictureSize_BACK.setOnPreferenceClickListener(onPreferenceClickListener);
				} else {
					// Front 사진 크기
					int width = mCameraPref.getPictureSizes_FRONT()[0];
					int height = mCameraPref.getPictureSizes_FRONT()[1];
					String pixel = String.format("%.1f", Math.round(width*height/100000f)/10f);
					String currentFrontSize = width+ " x " + height + " ("+pixel+"M)";
					
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
					list_Camera_PictureSize_FRONT.setOnPreferenceClickListener(onPreferenceClickListener);
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
				list_Camera_ColorEffect.setOnPreferenceClickListener(onPreferenceClickListener);
				
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
				
				list_Camera_WhiteBalance.setOnPreferenceClickListener(onPreferenceClickListener);
				
				
				
				
				
				
				
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
					list_Camera_SceneMode.setEntries(mCameraPref.getSceneModeList());
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
				
				list_Camera_SceneMode.setOnPreferenceClickListener(onPreferenceClickListener);
				
				
				// 카메라 장면모드++++++++
				list_Camera_ContinuousShooting = new ListPreference(this);
				list_Camera_ContinuousShooting.setKey(KEY_CAMERA_SCENEMODE);
		
				// 모드를 한글로 바꾼다
				String[] translatedContinuousShootingList = mCameraPref.getContinuousShooting();
				String[] continuousShooting_en = getResources().getStringArray(
						R.array.continuousShooting_en);
				String[] continuousShooting_ko = getResources().getStringArray(
						R.array.continuousShooting_ko);
		
				for (int i = 0; i < translatedContinuousShootingList.length; i++) {
					for (int j = 0; j < sceneMode_en.length; j++) {
						if (translatedContinuousShootingList[i].equals(sceneMode_en[j]) == true) {
							translatedContinuousShootingList[i] = sceneMode_ko[j];
							break;
						}
					}
				}
				
				if(getString(R.string.language).equals("ko")== true) {
					list_Camera_ContinuousShooting.setEntries(translatedContinuousShootingList);
				} else {
					list_Camera_ContinuousShooting.setEntries(mCameraPref.getSceneModeList());
				}
				
				list_Camera_ContinuousShooting.setEntryValues(mCameraPref.getSceneModeList());
				list_Camera_ContinuousShooting
						.setDialogTitle(getString(R.string.setting_scene_mode_title));
				list_Camera_ContinuousShooting
						.setTitle(getString(R.string.setting_scene_mode_title));
		
				String summarySceneMode = mCameraPref.getSceneMode();
				
				if(getString(R.string.language).equals("ko")== true) {
					summarySceneMode = translateSceneMode(summarySceneMode);
				}
		
				list_Camera_SceneMode.setSummary(summarySceneMode);
				cameraPrefCat.addPreference(list_Camera_SceneMode);
				
				list_Camera_SceneMode.setOnPreferenceClickListener(onPreferenceClickListener);
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
		
		//개발자에게 이메일 보내기
		Preference preference_sendEmail = new Preference(this);
		preference_sendEmail.setKey(KEY_SendEmail);
		preference_sendEmail
				.setTitle(getString(R.string.setting_title_send_email));
		preference_sendEmail
				.setSummary(getString(R.string.setting_content_send_email));
		preference_sendEmail
				.setOnPreferenceClickListener(onPreferenceClickListener);
		HelpCat.addPreference(preference_sendEmail);
		
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
						break;
					}
				}
				
				Dialog dialog_color = list_Camera_PictureSize_BACK.getDialog();
				if(dialog_color.isShowing() == true) {
					dialog_color.getWindow().setLayout(getWindowManager().getDefaultDisplay().getWidth()*3/5, getWindowManager().getDefaultDisplay().getHeight()*4/5);
				}
			} else if (key.equals(KEY_CAMERA_PICTURESIZE_FRONT)) {
				String currentSize = mCameraPref.getPictureSizes()[0] + " x "
						+ mCameraPref.getPictureSizes()[1];
				for (int i = 0; i < mCameraPref.getPictureSizeBackList().length; i++) {
					if (currentSize
							.equals(mCameraPref.getPictureSizeBackList()[i])) {
						list_Camera_PictureSize_FRONT.setValueIndex(i);
						break;
					}
				}

				Dialog dialog_color = list_Camera_PictureSize_FRONT.getDialog();
				if (dialog_color.isShowing() == true) {
					dialog_color.getWindow()
							.setLayout(
									getWindowManager().getDefaultDisplay()
											.getWidth() * 3 / 5,
									getWindowManager().getDefaultDisplay()
											.getHeight() * 4 / 5);
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
			else if(key.equals(KEY_SendEmail)) {
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:smardi.cliqr@gmail.com"));
				intent.putExtra(Intent.EXTRA_SUBJECT, "CLIQ.r에 대해 보고합니다.");
				startActivity(intent);
				
				mCameraPref.setStateSendEmail(true);
			}
			
			
			else if(key.equals(KEY_CAMERA_COLOREFFECT)) {
				Dialog dialog_color = list_Camera_ColorEffect.getDialog();
				if(dialog_color.isShowing() == true) {
					
					dialog_color.getContext().setTheme(android.R.style.Theme_Translucent_NoTitleBar);
					dialog_color.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
					dialog_color.getWindow().setLayout(getWindowManager().getDefaultDisplay().getWidth()*3/5, getWindowManager().getDefaultDisplay().getHeight()*4/5);
				}
			}
			else if(key.equals(KEY_CAMERA_PICTURESIZE_FRONT)) {
				Dialog dialog_color = list_Camera_PictureSize_FRONT.getDialog();
				if(dialog_color.isShowing() == true) {
					dialog_color.getWindow().setLayout(getWindowManager().getDefaultDisplay().getWidth()*3/5, getWindowManager().getDefaultDisplay().getHeight()*4/5);
				}
			}
			else if(key.equals(KEY_CAMERA_SCENEMODE)) {
				Dialog dialog_color = list_Camera_SceneMode.getDialog();
				if(dialog_color.isShowing() == true) {
					dialog_color.getWindow().setLayout(getWindowManager().getDefaultDisplay().getWidth()*3/5, getWindowManager().getDefaultDisplay().getHeight()*4/5);
				}
			}
			else if(key.equals(KEY_CAMERA_WHITEBALANCE)) {
				Dialog dialog_color = list_Camera_WhiteBalance.getDialog();
				if(dialog_color.isShowing() == true) {
					dialog_color.getWindow().setLayout(getWindowManager().getDefaultDisplay().getWidth()*3/5, getWindowManager().getDefaultDisplay().getHeight()*4/5);
				}
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
				for (Size size : mCameraParams.getPictureBackSizes()) {
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
				for (Size size : mCameraParams.getPictureFrontSizes()) {
					
					Log.e(TAG, "tempSize:" + size.width + " x " + size.height + "("+tempSize[0]+" x "+ tempSize[2]+")");
					
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
