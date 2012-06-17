package org.smardi.Cooliq.R;

import java.io.*;
import java.text.*;
import java.util.*;

import org.smardi.CliqService.*;

import android.app.*;
import android.content.*;
import android.database.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.hardware.*;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;

public class AC_Main extends Activity {

	// 디버깅
	private final boolean D = false;

	// 프리퍼런스
	private Manage_Camera_SharedPreference mCameraPref;

	// 컴포넌트
	Surface_Picture_Preview mSurface;

	// 튜토리얼
	private RelativeLayout tutorial;
	private ImageView tutorial_left;
	private ImageView tutorial_center;
	private ImageView tutorial_right;

	// 흰색 스크린
	private LinearLayout whiteScreen;

	// 오른편 컨트롤 패널
	private LinearLayout controlLayout;
	private int controlLayoutWidth = 0;
	private RelativeLayout.LayoutParams controlLayout_linearParams = null;
	private boolean isControlLayoutShow = true;
	private LinearLayout btn_slide;
	private ImageView btn_change_camera;
	private ImageView btn_flash;
	private ImageView btn_recording_mode;
	private ImageView btn_timer;
	private ImageView btn_setting;
	private ImageView btn_gallary;
	private TextView txt_time;
	// 왼편 컨트롤 패널
	private ImageView btn_mode_change;
	private ImageView btn_shutter;
	private ImageView light_onAir;
	private ImageView light_onCliq;
	private ImageView img_slide;

	// 가운데 사운드 게이지
	private FrameLayout soundSensitivityWrap = null;
	private ImageView soundSensitivityBar = null,
			soundSensitivitySeekbar = null;
	private int sensitivityBarOriginalSize = 0;
	private int initSeekbarMargin = 0; // 최초 seekbar의 왼쪽 여백
	private boolean isShowSoundPowerBar = true; // 중앙 하단의 사운드 게이지를 나타낼 것인지.
	private Bitmap sensitivityBarBitmap = null;

	// 사운드 관리자
	SoundManager mSoundManager;

	// 클리커에서 날라온 클릭관련 정보 저장용
	long timeCliqPressed = 0;
	long timeCliqReleased = 0;
	boolean isPressed = false;
	boolean isAutoFocused = false; // 롱버큰 클릭이 되서 오토포커싱이 되었는지

	// 스레드를 관리한다.
	boolean isRunThread = false;

	// 사진 찍을 수 있는 상황인지 판단한다.
	boolean isCanTakePicture = true; // 설정 중일 때에는 촬영이 안되도록 한다.

	// Handler
	private final int WHAT_AUTOFOCUSING = 0;
	private final int WHAT_CHANGE_POSITION_CONTROL_LAYOUT = 1;
	private final int WHAT_TIMING_TAKE_PHOTO = 2;
	private final int WHAT_REMAIN_TIME = 3;
	private final int WHAT_SOUND_GAGE_MOVE = 4;
	private final int WHAT_CLEAN_SCREEN = 5;

	// Constant
	private final int TIME_SHORTCLICK = 0;
	private final int TIME_LONGCLICK = 700; // 롱클릭으로 인지하는데 걸리는 시간(ms)

	// State
	private int stateCamera = 0;
	private final int STATE_CAMERA_READY = 0; // 준비 된 상태
	private final int STATE_CAMERA_FOCUSING = 1; // 포커싱 중
	private final int STATE_CAMERA_TAKING_PHOTO = 2; // 사진 촬영 중
	private final int STATE_CAMERA_TAKING_TIMING_PHOTO = 3; // 타이머 사진 촬영 중
	private final int STATE_VIDEO_READY = 4; // 비디오 촬영 준비
	private final int STATE_VIDEO_RECORDING = 5; // 비디오 촬영 중

	// 촬영 셔터 모드 선택
	private int stateShutter = 1;
	private final int STATE_SHUTTER_NONE = 0;
	private final int STATE_SHUTTER_CLIQ = 1;
	private final int STATE_SHUTTER_SOUND = 2;
	private boolean isCliqSoundShutterON = false; // 클리커 모드에서 클리커를 켰는지 껐는지
	private boolean isInTimingShot = false;

	// Flash 모드
	private String flashMode = "auto";

	// 카메라 전환
	private int whichCamera = 0;
	private boolean isInitiated = false; // 카메라 파라미터가 설정되었는지.

	// 포커스 관련
	private boolean isFocusing = false; // 포커스 조절 중인지

	// 클리커에 의해 오토포커싱이 됐는지
	private boolean isFocusedByCliq = false;

	public final static int ACTIVITY_SETTING = 0; // 설정화면으로 전달할 request code
	public final static int ACTIVITY_GALLARY = 1; // 갤러리로 전달할 request code

	// Log
	private final String TAG = "Cliq.R";

	// File
	private static final String PHOTO_FILE_EXT_JPG = ".jpg";
	private static final String PHOTO_FILE_EXT_3GP = ".3gp";
	private static final String PHOTO_FILE_EXT_MP4 = ".mp4";
	private static final String PHOTO_SAVE_FOLDER = "DCIM/CAMERA";

	// Camera parameters
	private CameraParameters mCameraParametes = null;
	private boolean isSetCameraParameters = false; // 카메라 관련 변수를 설정했는지.
	private Manage_CLIQ_SharedPreference mSharedPreference = null;

	// Video Capture 관련
	private MediaRecorder mMediaRecorder = new MediaRecorder();
	private boolean isRecording = false; // 녹화중인지.

	// 타이머 연속으로 안되게
	private boolean isTimerON = false;

	// 두번 눌러야 나가지게
	long lastBackPressTime = 0;
	Toast toast = null;

	// 갤러리로 나갔을 때 이전 정보를 저장
	private boolean wasCliqPowerOn = false; // 이전에 클리커가 켜져있었는지.

	boolean isTakingPicture = false;

	// 튜토리얼이 떠있는지 확인
	private boolean isTutorialShow = true; // 처음에는 튜토리얼이 나타나있음.

	// 사진의 방향을 알아내기 위한 리스너
	private OrientationEventListener oel = null;
	private int mAngle = 0;
	
	//갤러리가 비어있는지 확인하기 위한...
	private boolean isGalleryEmpty = false;
	
	private int countPastTime = 0; // 타이머가 켜진 상태에서 셔터가 눌리고 지나간 시간
	int old_remainTime = Integer.MAX_VALUE;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_main);

		// Camera type setting
		mCameraPref = new Manage_Camera_SharedPreference(AC_Main.this);
		mCameraPref.setWhichCamera(whichCamera);

		componentInitiation();
		eventRegist();
		loadSoundFiles();
		changeFont();
		applyTutorialEnglish();

		// 썸네일 새로고침
		updateThumbnail();

		mThread.start();
		isRunThread = true;

		// 클리커 서비스 시작
		Intent service = new Intent(AC_Main.this, Service_Cliq.class);
		startService(service);

		// 튜토리얼 나타내기
		if (getIntent().getBooleanExtra("tutorial", false) == false) {
			tutorial.setVisibility(View.GONE);
		}
		/*
		 * Intent intent = new Intent();
		 * intent.setAction(Service_Cliq.ACTION_CLIQ_START);
		 * sendBroadcast(intent);
		 * intent.setAction(Service_Cliq.ACTION_MODE_CLIQING_ON);
		 * sendBroadcast(intent);
		 */
	}

	private void applyTutorialEnglish() {
		if(getString(R.string.language).equals("en")== true) {
			((ImageView)tutorial.getChildAt(0)).setImageDrawable(getResources().getDrawable(R.drawable.tutorial_left_en));
			((ImageView)tutorial.getChildAt(1)).setImageDrawable(getResources().getDrawable(R.drawable.tutorial_center_en));
			((ImageView)tutorial.getChildAt(2)).setImageDrawable(getResources().getDrawable(R.drawable.tutorial_right_en));
			
		}
	}

	// =======================================================
	private boolean isRestart = false;

	@Override
	protected void onPause() {
		super.onPause();
		sendBroadcast(new Intent().setAction(Service_Cliq.ACTION_CLIQ_STOP));

		unregisterReceiver(mBroadcastReceiver);
		oel.disable();

		isRestart = true;
		
		//타이머 정지
		resetTimer();

		Log.e(TAG, "onPause");
	}
	
	private void resetTimer() {
		isInTimingShot = false;	
		countPastTime = 0;
		old_remainTime = Integer.MAX_VALUE;
		txt_time.setVisibility(View.GONE);
	}

	// =======================================================
	@Override
	protected void onResume() {
		super.onResume();

		Log.e(TAG, "onResume");

		isFocusing = false;

		if (isCliqSoundShutterON == true) {
			sendBroadcast(new Intent()
					.setAction(Service_Cliq.ACTION_CLIQ_START));
		}

		registBroadcastReceiver();

		resetTimer();
		
		if(isRestart == true) {
			tutorial.setVisibility(View.GONE);
			isRestart = false;
		}
		
		oel = new OrientationEventListener(AC_Main.this) {

			@Override
			public void onOrientationChanged(int orientation) {
				Log.e("test", "angle:" + orientation);
				mAngle = orientation;
			}
		};
		/*
		 * if (isRestart == true) { finish(); startActivity(new Intent(this,
		 * AC_Main.class)); }
		 */

		// oel.enable();
	}

	// =========================================================

	@Override
	protected void onDestroy() {
		// 클리커 서비스 종료
		Intent intent = new Intent();
		intent.setAction(Service_Cliq.ACTION_CLIQ_STOP);
		sendBroadcast(intent);

		Intent service = new Intent(AC_Main.this, Service_Cliq.class);
		stopService(service);

		isRunThread = false;
		mThread = null;

		super.onDestroy();
	}

	// =======================================================
	private void initCameraBySharedPreference() {
		// 셔터를 원래대로
		btn_shutter.setImageDrawable(getResources().getDrawable(
				R.drawable.c_shutter));

		try {
			Parameters params = mSurface.mCamera.getParameters();

			// 플래시 모드가 같은지 확인, 없으면 처음 것 적용

			try {
				if (mCameraParametes.getFlashMode() == null) {
					mCameraPref.setFlashMode("off");
				}

				if (mCameraParametes.getFlashMode().indexOf(
						mCameraPref.getFlashMode()) < 0) {
					if (whichCamera == mSurface.CAMERA_FACE) {
						mCameraPref.setFlashMode(mCameraParametes
								.getFlashMode().get(0));
					} else {
						mCameraPref.setFlashMode("auto");
					}
				}
			} catch (Exception e) {
				/*
				 * Log.e(TAG, "mCameraParametes.getFlashMode() == null:"+
				 * (mCameraParametes.getFlashMode() == null)); Log.e(TAG,
				 * "mCameraParametes.getFlashMode().size():"
				 * +mCameraParametes.getFlashMode().size());
				 * 
				 * for(int i=0; i<mCameraParametes.getFlashMode().size(); i++) {
				 * Log.e(TAG,
				 * "mCameraParametes.getFlashMode().get("+i+"):"+mCameraParametes
				 * .getFlashMode().get(i)); }
				 * 
				 * Log.e(TAG,
				 * "mCameraPref.setFlashMode():"+mCameraPref.getFlashMode());
				 */
				Log.e(TAG, "mCameraParametes.getFlashMode()");
				Log.e(TAG, "mCameraPref == null :" + (mCameraPref == null));
				Log.e(TAG, "mCameraPref.getFlashMode() == null :"
						+ (mCameraPref.getFlashMode() == null));
				Log.e(TAG, "Error:" + e.getLocalizedMessage());

				return;
			}

			try {
				// 초점 모드가 있는지 확인, 없으면 처음 것 적용
				if (mCameraParametes.getFocusMode().indexOf(
						mCameraPref.getFocusMode()) < 0) {
					mCameraPref.setFocusMode(mCameraParametes.getFocusMode()
							.get(0));
				}
			} catch (Exception e) {
				if (whichCamera == mSurface.CAMERA_FACE) {
					mCameraPref.setFocusMode("off");
					Log.e(TAG, "mCameraPref.setFocusMode(\"off\");");
				} else {
					mCameraPref.setFocusMode("auto");
					Log.e(TAG, "mCameraPref.setFocusMode(\"auto\");");
				}

				Log.e(TAG, "Error:" + e.getLocalizedMessage());
				return;
			}

			// 사진 크기가 있는지 확인, 없으면 처음 것 적용
			/*
			 * if(whichCamera == mSurface.CAMERA_BACK) {
			 * mCameraPref.setPictureSizes
			 * (mCameraPref.getPictureSizes_BACK()[0],
			 * mCameraPref.getPictureSizes_BACK()[1]); } else {
			 * mCameraPref.setPictureSizes
			 * (mCameraPref.getPictureSizes_FRONT()[0],
			 * mCameraPref.getPictureSizes_FRONT()[1]); }
			 */

			if (whichCamera == mSurface.CAMERA_BACK) {
				mCameraPref.setFocusMode("auto");
				mCameraPref.setFlashMode("auto");
				// isSetCameraParameters = true;
			} else {
				mCameraPref.setFocusMode("infinity");
				mCameraPref.setFlashMode("off");
			}

			if (whichCamera == mSurface.CAMERA_BACK) {
				params.setFlashMode(mCameraPref.getFlashMode());
				params.setFocusMode(mCameraPref.getFocusMode());
			}
			params.setColorEffect(mCameraPref.getColorEffect());
			params.setSceneMode(mCameraPref.getSceneMode());
			params.setJpegQuality(100);
			params.setWhiteBalance(mCameraPref.getWhiteBalance());
			int[] pictureSize = new int[2];
			if (whichCamera == mSurface.CAMERA_BACK) {
				pictureSize = mCameraPref.getPictureSizes_BACK();
			} else {
				pictureSize = mCameraPref.getPictureSizes_FRONT();
			}

			params.setPictureSize(pictureSize[0], pictureSize[1]);
			if (D) {
				Log.i("smardi.Cliq", whichCamera + " width:" + pictureSize[0]
						+ " height:" + pictureSize[1]);
			}
			mSurface.mCamera.setParameters(params);

			changePreviewRatio();

		} catch (Exception e) {
			Log.e(TAG, "Error in initCameraBySharedPreference:" + e.getLocalizedMessage());
			Log.v(TAG, "Error in initCameraBySharedPreference:" + e.getLocalizedMessage());
			Log.i(TAG, "Error in initCameraBySharedPreference:" + e.getLocalizedMessage());
		}
	}

	// 효과음들을 불러온다.
	private void loadSoundFiles() {
		mSoundManager.Init(AC_Main.this);
		mSoundManager.addSound(1, R.raw.camera_shutter);
		mSoundManager.addSound(2, R.raw.camera_focus);
		mSoundManager.addSound(3, R.raw.camera_beep);
		mSoundManager.addSound(4, R.raw.focus_complete);
		mSoundManager.addSound(5, R.raw.camera_shutter_cliq);
		mSoundManager.addSound(6, R.raw.focus_start);
		mSoundManager.addSound(7, R.raw.focus_start_2);
	}

	private void registBroadcastReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Service_Cliq.ACTION_CLIQ_CLICK_TRIGERED);
		filter.addAction(Service_Cliq.ACTION_CLIQ_RELEASE_TRIGERED);
		filter.addAction(Service_Cliq.ACTION_CLIQ_REGISTRATION_END);
		filter.addAction(Service_Cliq.ACTION_CLIQ_REGISTRATION_END_ERROR);
		filter.addAction(Service_Cliq.ACTION_CLIQ_TEST_END);
		filter.addAction(Service_Cliq.ACTION_CLIQ_SOUNDPOWER);
		filter.addAction(Service_Cliq.ACTION_SEND_THREADHOLD_POWER);

		filter.addAction(mSurface.ACTION_SURFACE_CHANGED);

		registerReceiver(mBroadcastReceiver, filter);
	}

	@SuppressWarnings("static-access")
	private void componentInitiation() {
		mSurface = (Surface_Picture_Preview) findViewById(R.id.surfacePreview);

		mSoundManager = new SoundManager().getInstance();

		mSharedPreference = new Manage_CLIQ_SharedPreference(AC_Main.this);

		// 튜토리얼
		tutorial = (RelativeLayout) findViewById(R.id.tutorial);
		//tutorial_left = (ImageView)findViewById(R.id.tutorial_left);

		// 흰색 이미지
		whiteScreen = (LinearLayout) findViewById(R.id.whiteScreen);

		// 오른편 컨트롤
		controlLayout = (LinearLayout) findViewById(R.id.control_layout);
		btn_slide = (LinearLayout) findViewById(R.id.btn_slide);
		btn_change_camera = (ImageView) findViewById(R.id.btn_change_camera);
		btn_flash = (ImageView) findViewById(R.id.btn_flash);
		btn_recording_mode = (ImageView) findViewById(R.id.btn_record_mode);
		btn_timer = (ImageView) findViewById(R.id.btn_timer);
		btn_setting = (ImageView) findViewById(R.id.btn_setting);
		btn_gallary = (ImageView) findViewById(R.id.btn_gallary);
		img_slide = (ImageView) findViewById(R.id.img_slide);

		// 왼편 컨트롤
		btn_mode_change = (ImageView) findViewById(R.id.btn_mode_change);
		btn_shutter = (ImageView) findViewById(R.id.btn_shutter);
		light_onAir = (ImageView) findViewById(R.id.light_on_air);
		light_onCliq = (ImageView) findViewById(R.id.light_cliq);
		txt_time = (TextView) findViewById(R.id.txt_time);

		// 가운데 사운드
		soundSensitivityWrap = (FrameLayout) findViewById(R.id.sound_sensitivity_wrap);
		soundSensitivityBar = (ImageView) findViewById(R.id.sound_sensitivity_bar);
		soundSensitivitySeekbar = (ImageView) findViewById(R.id.sound_sensitivity_seeker);
		sensitivityBarOriginalSize = soundSensitivityBar.getLayoutParams().width;
		BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources()
				.getDrawable(R.drawable.sound_sensitivity_bar);
		sensitivityBarBitmap = bitmapDrawable.getBitmap();

		// CLIQ.r 모드를 NONE으로 저장함
		mCameraPref.setModeCLIQ(0);
	}

	private void eventRegist() {
		// mSurface.setOnClickListener(viewClickListener);

		/*
		 * btn_test_open.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { isCanTakePicture = false; //
		 * 사진 촬영 할 수 없다. Intent intent = new Intent();
		 * intent.setAction(Service_Cliq.ACTION_CLIQ_TEST_START);
		 * sendBroadcast(intent); } });
		 * 
		 * btn_regist.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { isCanTakePicture = false; //
		 * 사진 촬영 할 수 없다. Intent intent = new Intent();
		 * intent.setAction(Service_Cliq.ACTION_CLIQ_REGISTRATION_START);
		 * sendBroadcast(intent); } });
		 */

		// 튜토리얼
		tutorial.setOnClickListener(viewClickListener);

		// 오른편 컨트롤
		btn_slide.setOnClickListener(viewClickListener);
		btn_change_camera.setOnClickListener(viewClickListener);
		btn_flash.setOnClickListener(viewClickListener);
		btn_recording_mode.setOnClickListener(viewClickListener);
		btn_timer.setOnClickListener(viewClickListener);
		btn_setting.setOnClickListener(viewClickListener);
		btn_gallary.setOnClickListener(viewClickListener);

		// 왼편 컨트롤
		btn_mode_change.setOnClickListener(viewClickListener);
		btn_shutter.setOnTouchListener(viewTouchListener);
		light_onCliq.setOnClickListener(viewClickListener);

		// 가운데 화면
		soundSensitivitySeekbar.setOnTouchListener(viewTouchListener);
		soundSensitivitySeekbar.setOnClickListener(viewClickListener);

		// Preview 화면 클릭 시
		mSurface.setOnClickListener(viewClickListener);
	}

	View.OnClickListener viewClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.surfacePreview:
				if (whichCamera == mSurface.CAMERA_BACK) {
					autoFocus();
				} else {
					// 전면카메라 일 때는 화면이 클릭되도 아무것도 안함
				}
				break;

			// 튜토리얼
			case R.id.tutorial:
				v.setVisibility(View.INVISIBLE);
				sendBroadcast(new Intent()
						.setAction(Service_Cliq.ACTION_GET_THREADHOLD_POWER));
				isShowSoundPowerBar = false;
				isTutorialShow = false;
				break;

			// 오른편 컨트롤
			case R.id.btn_slide:
				slideControlLayout();
				break;
			case R.id.btn_change_camera:
				setChangeCameraButton();
				break;
			case R.id.btn_flash:
				setFlashButton();
				break;
			case R.id.btn_record_mode:
				setChangeShutterButton();
				break;
			case R.id.btn_timer:
				setTimerButton();
				break;
			case R.id.btn_setting:
				openSettingDialog();

				break;
			case R.id.btn_gallary:
				if(isGalleryEmpty == true) {
					//TODO 갤러리에 이미지가 없다는 메시지 출력
					Toast.makeText(AC_Main.this, getString(R.string.there_is_no_picture), Toast.LENGTH_SHORT).show();
					return;
				}
				try {
					/*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent = new Intent(Intent.ACTION_VIEW, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(intent, ACTIVITY_GALLARY);
					 */
					sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
							Uri.parse("file://" + getFilesDir())));
					Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
					String targetDir = Environment
							.getExternalStorageDirectory().toString()
							+ "/DCIM/Camera"; // 특정 경로!!
					uri = uri
							.buildUpon()
							.appendQueryParameter(
									"bucketId",
									String.valueOf(targetDir.toLowerCase()
											.hashCode())).build();
					
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivityForResult(intent, ACTIVITY_GALLARY);
				} catch (ActivityNotFoundException e) {

					Toast.makeText(
							AC_Main.this,
							"본 기기에서는 갤러리 기능을 지원할 수 없습니다.\n자체 갤러리 기능을 이용 해 주세요.",
							1000).show();
					Log.e(TAG, "Error load Gallary:"+e.getLocalizedMessage());
				}
				/*
				 * Intent intent = new Intent();
				 * intent.setAction(Intent.ACTION_VIEW);
				 * intent.setType("image/*"); startActivity(intent);
				 */

				break;

			// 오른편 컨트롤 (클리커 모드 켜기, 셔터, 갤러리)
			case R.id.btn_mode_change:
				/*
				 * if (stateShutter == STATE_SHUTTER_SOUND) { stateShutter =
				 * STATE_SHUTTER_CLIQ;
				 * btn_mode_change.setImageDrawable(getResources()
				 * .getDrawable(R.drawable.button_mode_cliq));
				 * isShowSoundPowerBar = false; } else { stateShutter =
				 * STATE_SHUTTER_SOUND;
				 * btn_mode_change.setImageDrawable(getResources()
				 * .getDrawable(R.drawable.button_mode_sound));
				 * isShowSoundPowerBar = true; } setShutterMode();
				 * setCliqSoundShutterONOFF(); break;
				 */
			case R.id.light_cliq:

				// 만약 최초에 클리커 등록이 안된 상태에서 눌리게 되면
				// 클리커를 등록 해 달라고 경고창을 띄움
				if (mSharedPreference.getCliqFrequency() == 0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							AC_Main.this);
					builder.setTitle(getString(R.string.alert_no_regist_cliq_title));
					builder.setMessage(getString(R.string.alert_no_regist_cliq_message));
					builder.setNegativeButton("확인",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									openSettingDialog();
								}
							});
					builder.show();

					return;
				}

				if (isCliqSoundShutterON == true) {
					isCliqSoundShutterON = false;

					mCameraPref.setModeCLIQ(0); // 꺼져있는 상태로 저장
					getWindow().clearFlags(
							WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				} else {
					isCliqSoundShutterON = true;
					getWindow().addFlags(
							WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

					if (stateShutter == STATE_SHUTTER_CLIQ) {
						mCameraPref.setModeCLIQ(1); // CLIQ.r 모드로 저장
					} else if (stateShutter == STATE_SHUTTER_SOUND) {
						mCameraPref.setModeCLIQ(2); // Sound 모드로 저장
					}
				}

				setShutterMode();
				setCliqSoundShutterONOFF();
				break;
			}
		}

	};

	private void openSettingDialog() {
		startActivityForResult(new Intent(AC_Main.this, PA_Setting.class),
				ACTIVITY_SETTING);
	}

	View.OnTouchListener viewTouchListener = new View.OnTouchListener() {
		int seekbar_init_X = 0;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (v.getId()) {
			case R.id.btn_shutter:
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// Toast.makeText(AC_Main.this, "DOWN", 1000).show();
					// setShutterPress(); //2012.05.20 주석처리함
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// Toast.makeText(AC_Main.this, "UP", 1000).show();
					// setShutterRelease(); //2012.05.20 주석처리함

					if (mCameraParametes.getTimerTime() > 0) {
						isInTimingShot = true;
					} else {
						mSoundManager.play(1);
						takePicture();
					}
				}
				break;

			case R.id.sound_sensitivity_seeker:

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					seekbar_init_X = (int) event.getRawX();
					// Log.e("smardi.Cliq", "first:"+seekbar_init_X);
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					int movement = (int) (seekbar_init_X - event.getRawX());
					// Log.e("smardi.Cliq", "movement:"+movement);
					moveSeekbar(movement);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) soundSensitivitySeekbar
							.getLayoutParams();

					initSeekbarMargin = params.leftMargin;

					Intent intent = new Intent();
					intent.putExtra(
							"ThreadholdPower",
							(params.leftMargin * 100 / sensitivityBarOriginalSize));
					intent.setAction(Service_Cliq.ACTION_SET_THREADHOLD_POWER);

					sendBroadcast(intent);
				}
				break;
			}
			return false;
		}
	};

	// 포커싱 성공하면 촬영 허가 (아직 촬영 아님)
	AutoFocusCallback mAutoFocus = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			// mShutter.setEnabled(success);
			// stateCamera = STATE_CAMERA_READY;
			isFocusing = false;

			if (isFocusedByCliq == true) {
				mSoundManager.play(5);
				takePicture(); // 2012년 5월 19일 수정
				isFocusedByCliq = false;
			} else {
				mSoundManager.play(4);
			}
		}
	};

	// ===================================================================
	// Key Press listener
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isTutorialShow == true) {
				tutorial.setVisibility(View.GONE);
				isTutorialShow = false;
				return false;
			} else {
				return exitApp();
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (isTutorialShow == false) {
				// 튜토리얼이 떠있지 않은 상태에서만 설정 매뉴를 띄움
				openSettingDialog();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private boolean exitApp() {
		if (this.lastBackPressTime < System.currentTimeMillis() - 2000) {
			toast = Toast.makeText(this, getString(R.string.backButtonToast),
					2000);
			toast.show();
			this.lastBackPressTime = System.currentTimeMillis();
			return false;
		} else {
			if (toast != null) {
				toast.cancel();
			}

			finish();
		}
		return true;
	}

	// --=================================================================

	PictureCallback mPicture = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			File file = new File(getPhotoFilename());

			// whiteScreen.setVisibility(View.VISIBLE);
			try {
				// 방향에 맞게 이미지 회전하기
				// Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0,
				// data.length);

				// tempBitmap = tempBitmap.createBitmap(tempBitmap, 0, 0,
				// tempBitmap.getWidth(), tempBitmap.getHeight(), mat, true);

				FileOutputStream fos = new FileOutputStream(file);
				// tempBitmap.compress(CompressFormat.JPEG, 100, fos);

				fos.write(data);
				fos.flush();
				fos.close();

				// 찍은 날짜 수정
				ExifInterface mExif = new ExifInterface(getPhotoFilename());
				mExif.setAttribute(ExifInterface.TAG_DATETIME,
						new Date(System.currentTimeMillis()).toString());
				/*
				 * if((0 <= mAngle && 60 < mAngle) || (300 <= mAngle && 360 <
				 * mAngle)) { // 90도 회전
				 * mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
				 * ""+ExifInterface.ORIENTATION_ROTATE_90); } else if(60 <=
				 * mAngle && mAngle < 120) { // 180도 회전
				 * mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
				 * ""+ExifInterface.ORIENTATION_ROTATE_180); } else if(120 <=
				 * mAngle && mAngle < 240) { // 270도 회전
				 * mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
				 * ""+ExifInterface.ORIENTATION_ROTATE_270); } else if(240 <=
				 * mAngle && mAngle < 300) { // 그대로
				 * mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
				 * ""+ExifInterface.ORIENTATION_NORMAL); }
				 */
				mExif.saveAttributes();

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 8;
				Bitmap tempBmp = BitmapFactory.decodeByteArray(data, 0,
						data.length, options);
				//사진이 생겼다고 표시한다
				isGalleryEmpty = false;
				updateThumbnail(tempBmp);
				tempBmp = null;
			} catch (IOException e) {
				Toast.makeText(AC_Main.this,
						"Error occured while Saving the picture", 1000).show();
				Log.e(TAG,
						"Error occured while Saving the picture: "
								+ e.getLocalizedMessage());
			}

			// 셔터 버튼 이미지를 원래대로
			btn_shutter.setImageDrawable(getResources().getDrawable(
					R.drawable.c_shutter));
			// whiteScreen.setVisibility(View.GONE); //스레드에서 없애는걸로 수정
			mSurface.resumePreview();
			stateCamera = STATE_CAMERA_READY;
			isTakingPicture = false;
		}
	};

	private String getPhotoFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, PHOTO_SAVE_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		// return (file.getAbsolutePath() + "/" + System.currentTimeMillis() +
		// AUDIO_RECORDER_FILE_EXT_WAV);
		Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String str = formatter.format(new Date());

		return (file.getAbsolutePath() + "/CliQ_R_" + str + PHOTO_FILE_EXT_JPG);
	}

	protected void moveSeekbar(int movement) {
		// movement = (int)(movement * 100 / sensitivityBarOriginalSize);
		int newMargin = initSeekbarMargin - movement;

		// Log.i("smardi.Cliq", "newMargin:"+newMargin);
		if (0 < newMargin && newMargin < sensitivityBarOriginalSize) {
			android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) soundSensitivitySeekbar
					.getLayoutParams();
			params.setMargins(newMargin, params.topMargin, params.rightMargin,
					params.bottomMargin);
			soundSensitivitySeekbar.setLayoutParams(params);
		}
	}

	private String getVideoFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, PHOTO_SAVE_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		// return (file.getAbsolutePath() + "/" + System.currentTimeMillis() +
		// AUDIO_RECORDER_FILE_EXT_WAV);
		Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String str = formatter.format(new Date());

		return (file.getAbsolutePath() + "/CliQ_R_" + str + PHOTO_FILE_EXT_MP4);
	}

	private void setChangeShutterButton() {
		// ImageView에 있는 description을 가져온다.
		boolean isPictureMode = btn_recording_mode.getContentDescription()
				.equals("picture");

		if (isPictureMode == true && stateCamera == STATE_CAMERA_READY) {
			btn_recording_mode.setContentDescription("video");
			btn_recording_mode.setImageDrawable(getResources().getDrawable(
					R.drawable.c_mode_video));

			stateCamera = STATE_VIDEO_READY;

		} else if (isPictureMode == false && stateCamera == STATE_VIDEO_READY) {
			btn_recording_mode.setContentDescription("picture");
			btn_recording_mode.setImageDrawable(getResources().getDrawable(
					R.drawable.c_mode_picture));

			stateCamera = STATE_CAMERA_READY;
		}

	}

	@SuppressWarnings("static-access")
	protected void setChangeCameraButton() {
		Drawable drawable = null;

		if (whichCamera == mSurface.CAMERA_BACK) {
			whichCamera = mSurface.CAMERA_FACE;
			mCameraPref.setWhichCamera(mSurface.CAMERA_FACE);
			drawable = getResources().getDrawable(R.drawable.c_front_camera);
			// mCameraPref.setPictureSizes_BACK(mCameraPref.getPictureSizes()[0],
			// mCameraPref.getPictureSizes()[1]);
			// mCameraPref.setPictureSizes(mCameraPref.getPictureSizes_FRONT()[0],
			// mCameraPref.getPictureSizes_FRONT()[1]);
		} else {
			whichCamera = mSurface.CAMERA_BACK;
			mCameraPref.setWhichCamera(mSurface.CAMERA_BACK);
			drawable = getResources().getDrawable(R.drawable.c_back_camera);
			// mCameraPref.setPictureSizes_FRONT(mCameraPref.getPictureSizes()[0],
			// mCameraPref.getPictureSizes()[1]);
			// mCameraPref.setPictureSizes(mCameraPref.getPictureSizes_BACK()[0],
			// mCameraPref.getPictureSizes_BACK()[1]);
		}

		btn_change_camera.setImageDrawable(drawable);
		mSurface.openCamera_BackOrFront(whichCamera);

		// Surface view 화면을 변화시켜서 화면 비율을 정상화하기 위한 부분----------------------
		/*
		 * Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
		 * .getDefaultDisplay(); int screen_width = display.getWidth(); int
		 * screen_height = display.getHeight();
		 * 
		 * LinearLayout.LayoutParams surfParams = (LayoutParams) mSurface
		 * .getLayoutParams(); int surface_width = surfParams.width;
		 * 
		 * int new_width = 0; if (surface_width > screen_width) { new_width =
		 * screen_width - 1; } else { new_width = screen_width + 1; }
		 * Log.e("smardi.Cliq", "screen width:" + screen_width +
		 * " surface width:" + surface_width + " new:" + new_width);
		 */
		changePreviewRatio();
		/*
		 * mSurface.setLayoutParams(new LinearLayout.LayoutParams(new_width,
		 * LayoutParams.MATCH_PARENT));
		 */
		// -------------------------------------------------------------------------
		// 플래시 버튼 비활성화
		setFlashButton();

		// stateCamera = STATE_CAMERA_READY;

		// 포커싱 중이 아님으로 변환
		isFocusing = false;
	}

	/**
	 * 미리보기 해상도 비율을 조절한다.
	 */
	private void changePreviewRatio() {
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
				.getDefaultDisplay();
		int screen_w = display.getWidth();
		int screen_h = display.getHeight();

		int[] camera_size = new int[2];

		if (whichCamera == mSurface.CAMERA_FACE) {
			camera_size = mCameraPref.getPictureSizes_FRONT();
		} else {
			camera_size = mCameraPref.getPictureSizes_BACK();
		}

		int camera_w = camera_size[0];
		int camera_h = camera_size[1];

		int preview_w = 0;
		int preview_h = 0;

		if ((float) camera_w / (float) camera_h > (float) screen_w
				/ (float) screen_h) {
			// 사진 해상도가 화면보다 가로로 더 길 때
			preview_w = screen_w;
			preview_h = (int) Math.round(camera_h * screen_w / camera_w);
		} else {
			// 사진 해상도가 화면보다 세로로 더 길 때
			preview_h = screen_h;
			preview_w = (int) Math.round(camera_w * screen_h / camera_h);
		}

		if(D) {
			Log.e("smardi.Cliq", "cw:" + camera_w + " ch:" + camera_h);
			Log.e("smardi.Cliq", "sw:" + screen_w + " sh:" + screen_h);
			Log.e("smardi.Cliq", "pw:" + preview_w + " ph:" + preview_h);
		}

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				preview_w, preview_h);
		int margin_horizon = (int) Math.round((screen_w - preview_w) / 2);
		int margin_vertical = (int) Math.round((screen_h - preview_h) / 2);
		params.setMargins(margin_horizon, margin_vertical, margin_horizon,
				margin_vertical);

		mSurface.setLayoutParams(params);

		params = (LayoutParams) mSurface.getLayoutParams();
	}

	@SuppressWarnings("static-access")
	protected void setFlashButton() {
		String flashMode = mCameraPref.getFlashMode();
		Parameters params = mSurface.mCamera.getParameters();
		Drawable drawable = null;
		if (whichCamera == mSurface.CAMERA_BACK) {
			// 후면 카메라
			if (flashMode.equals("auto")) {
				flashMode = "on";
				drawable = getResources().getDrawable(R.drawable.c_flash_on);
			} else if (flashMode.equals("on")) {
				flashMode = "off";
				drawable = getResources().getDrawable(R.drawable.c_flash_off);
			} else if (flashMode.equals("off")) {
				flashMode = "auto";
				drawable = getResources().getDrawable(R.drawable.c_flash_auto);
			}
		} else {
			// 전면 카메라
			flashMode = "off";
			drawable = getResources().getDrawable(R.drawable.c_flash_off);
		}
		// 버튼 이미지 교체
		btn_flash.setImageDrawable(drawable);
		// 설정 저장
		mCameraPref.setFlashMode(flashMode);
		// 카메라에 적용
		params.setFlashMode(flashMode);
		mSurface.mCamera.setParameters(params);
	}

	protected void setTimerButton() {
		if(isInTimingShot == true) {
			return;
		}
		int delayTime = mCameraParametes.getTimerTime();
		Drawable timerDrawable = null;
		switch (delayTime) {
		case 0:
			delayTime = 3;
			timerDrawable = getResources().getDrawable(R.drawable.c_timer_3);
			break;
		case 3:
			delayTime = 5;
			timerDrawable = getResources().getDrawable(R.drawable.c_timer_5);
			break;
		case 5:
			delayTime = 15;
			timerDrawable = getResources().getDrawable(R.drawable.c_timer_15);
			break;
		case 15:
			delayTime = 0;
			timerDrawable = getResources().getDrawable(R.drawable.c_timer_0);
			break;
		}
		mCameraParametes.setTimerTime(delayTime);
		btn_timer.setImageDrawable(timerDrawable);
	}

	protected void setCliqSoundShutterONOFF() {
		Intent intent = new Intent();
		if (isCliqSoundShutterON == true) {
			intent.setAction(Service_Cliq.ACTION_CLIQ_START);
			light_onCliq.setImageDrawable(getResources().getDrawable(
					R.drawable.cliq_on));
			btn_mode_change.setImageDrawable(getResources().getDrawable(
					R.drawable.button_mode_on));
		} else {
			intent.setAction(Service_Cliq.ACTION_CLIQ_STOP);
			light_onCliq.setImageDrawable(getResources().getDrawable(
					R.drawable.cliq_off));
			btn_mode_change.setImageDrawable(getResources().getDrawable(
					R.drawable.button_mode_off));
		}
		sendBroadcast(intent);
	}

	protected void setShutterMode() {
		Intent intent = new Intent();

		switch (stateShutter) {
		case STATE_SHUTTER_NONE:
			intent.setAction(Service_Cliq.ACTION_MODE_NONE_ON);
			break;
		case STATE_SHUTTER_CLIQ:
			intent.setAction(Service_Cliq.ACTION_MODE_CLIQING_ON);
			break;
		case STATE_SHUTTER_SOUND:
			intent.setAction(Service_Cliq.ACTION_MODE_AMPLITUDE_ON);
			break;
		}
		sendBroadcast(intent);
	}

	protected void slideControlLayout() {
		controlLayout_linearParams = (RelativeLayout.LayoutParams) controlLayout
				.getLayoutParams();
		controlLayoutWidth = controlLayout.getMeasuredWidth()
				- controlLayout.getPaddingRight()
				- controlLayout.getPaddingLeft() - 10;

		if (isControlLayoutShow == true) {
			isControlLayoutShow = false;

			img_slide.setImageDrawable(getResources().getDrawable(
					R.drawable.slide_control_in));
		} else {
			isControlLayoutShow = true;
			img_slide.setImageDrawable(getResources().getDrawable(
					R.drawable.slide_control_out));
		}
	}

	/**
	 * 갤러리 버튼의 썸네일을 업데이트 한다.
	 */
	private void updateThumbnail() {
		String[] proj = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA,
				MediaStore.Images.Media.DISPLAY_NAME,
				MediaStore.Images.Media.SIZE,
				MediaStore.Images.Media.DATE_TAKEN };

		Bitmap bitmap = null;
		
		/*
		 * 이 방식은 DCIM 폴더 외의 이미지도 불러오므로 안씀
		 * 
		Cursor imageCursor = managedQuery(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null,
				MediaStore.Images.Media.DATE_TAKEN + " DESC");*/

		Cursor imageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, "bucket_display_name='Camera'", null, null);

		//만약 갤러리에 사진이 없을 경우
		if(imageCursor.getCount() == 0) {
			isGalleryEmpty = true;
			
			updateThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.img_none));
		} else {
			isGalleryEmpty = false;
		}
		
		if (imageCursor != null && imageCursor.moveToFirst()) {
			String thumbsImageID;
			String thumbsData;

			int thumbsDataCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media.DATA);
			int thumbsImageIDCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);

			thumbsData = imageCursor.getString(thumbsDataCol);
			thumbsImageID = imageCursor.getString(thumbsImageIDCol);

			if (thumbsImageID != null) {
				try {
					BitmapFactory.Options bo = new BitmapFactory.Options();
					bo.inSampleSize = 8;
					bitmap = BitmapFactory.decodeFile(thumbsData, bo);

					Bitmap resized = Bitmap.createScaledBitmap(bitmap, 122,
							122, true);

					btn_gallary.setImageBitmap(resized);
				} catch (NullPointerException e) {

				}
			}

		}
	}

	/**
	 * 갤러리 버튼의 썸네일을 업데이트 한다.
	 * 
	 * 카메라 촬영 된 다음 bitmap을 받으면 이걸로 표시한다.
	 */
	private void updateThumbnail(Bitmap tempBmp) {
		Bitmap resized = Bitmap.createScaledBitmap(tempBmp, 122, 122, true);

		btn_gallary.setImageBitmap(resized);

		Log.d(TAG, "Media Scanner!!!!!");
		sendBroadcast(new Intent(
				Intent.ACTION_MEDIA_MOUNTED,
				Uri.parse("file://" + Environment.getExternalStorageDirectory())));

	}

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// 클리커가 눌렸음이 감지 됐을 때
			if (action.equals(Service_Cliq.ACTION_CLIQ_CLICK_TRIGERED)) {
				btn_shutter.setImageDrawable(getResources().getDrawable(
						R.drawable.shutter_0_click_5));
				setShutterPress();
			}
			// 눌렸던 버튼이 풀렸음이 감지됐을 때
			else if (action.equals(Service_Cliq.ACTION_CLIQ_RELEASE_TRIGERED)) {
				if (isPressed == true) {
					// btn_shutter.setImageDrawable(getResources().getDrawable(R.drawable.c_shutter));
					setShutterRelease();
				}
			}
			// 클리커 등록이 끝났을 때
			else if (action.equals(Service_Cliq.ACTION_CLIQ_REGISTRATION_END)) {
				Log.e(TAG, "Registration: END");
				isCanTakePicture = true;
			} else if (action
					.equals(Service_Cliq.ACTION_CLIQ_REGISTRATION_END_ERROR)) {
				Log.e(TAG, "Registration: END_ERROR");
				isCanTakePicture = true;
			}
			// 클리커 테스트가 끝났을 때
			else if (action.equals(Service_Cliq.ACTION_CLIQ_TEST_END)) {
				Log.e(TAG, "Cliq test: END");
				isCanTakePicture = true;
			}
			// 음량의 크기를 전달받았을 때
			else if (action.equals(Service_Cliq.ACTION_CLIQ_SOUNDPOWER)) {
				changeSoundPowerGage(intent.getExtras().getDouble("power", 0));
			}
			// 음량 크기의 기준을 전달받았을 때
			else if (action.equals(Service_Cliq.ACTION_SEND_THREADHOLD_POWER)) {
				setSoundSensitivitySeekbar(intent.getExtras().getInt(
						"threadhold"));
			}

			// SurfaceView 갱신이 끝났을 때.
			else if (action.equals(mSurface.ACTION_SURFACE_CHANGED)) {
				if (mSurface.isLoadCameraparameterSuccese == true) {
					initCameraBySharedPreference();
				}
			}
		}
	};

	// 설정 activity가 종료되면 설정을 갱신한다.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Bundle extras = data.getExtras();
		switch (requestCode) {
		case ACTIVITY_SETTING:
			initCameraBySharedPreference();
			break;
		case ACTIVITY_GALLARY:
			updateThumbnail();
			break;
		}
	}

	protected void setSoundSensitivitySeekbar(int threadhold) {
		initSeekbarMargin = (int) (sensitivityBarOriginalSize * threadhold / 100);

		android.widget.RelativeLayout.LayoutParams seekParams = (android.widget.RelativeLayout.LayoutParams) soundSensitivitySeekbar
				.getLayoutParams();
		seekParams.setMargins(initSeekbarMargin, seekParams.topMargin,
				seekParams.rightMargin, seekParams.bottomMargin);
		soundSensitivitySeekbar.setLayoutParams(seekParams);
	}

	protected void changeSoundPowerGage(double power) {
		LayoutParams barParams = (LayoutParams) soundSensitivityBar
				.getLayoutParams();

		barParams.width = (int) (sensitivityBarOriginalSize * power / 100);
		soundSensitivityBar.setLayoutParams(barParams);

		// 비트맵 크기를 수정함
		Bitmap newBmp = Bitmap.createBitmap(sensitivityBarBitmap, 0, 0,
				barParams.width, sensitivityBarBitmap.getHeight());
		soundSensitivityBar.setImageBitmap(newBmp);
	}

	/**
	 * 셔터 버튼(클리커 버튼)이 눌렸을 때 처리
	 */
	private void setShutterPress() {
		timeCliqPressed = new Date().getTime();
		isPressed = true;
		isAutoFocused = false;
	}

	/**
	 * 셔터 버튼(클리커 버튼)이 해제됐을 때 처리
	 */
	private void setShutterRelease() {
		if (isPressed == true && isCanTakePicture == true) {
			timeCliqReleased = new Date().getTime();
			long timeClicked = timeCliqReleased - timeCliqPressed;
			Log.i(TAG, "Pressed Time:" + timeClicked + "(Click detecting in "
					+ TIME_LONGCLICK + "ms");
			if (timeClicked < TIME_LONGCLICK) {
				// 숏 클릭
				takePictureByTrigger();
			} else {
				// 롱 클릭
				if (isAutoFocused == false) {
					// autoFocus();
				}
			}

			isPressed = false;
		}
	}

	// 카메라 관련
	@SuppressWarnings("static-access")
	private void autoFocus() {
		if (isFocusing == true) {
			// 포커싱 중이면 빠져나간다.
			return;
		}

		if (isSetCameraParameters == false) {
			isSetCameraParameters = true;
		}

		// 포커스 중인 것으로 표기
		isFocusing = true;

		if (stateCamera == STATE_CAMERA_READY
				&& whichCamera == mSurface.CAMERA_BACK) {
			// stateCamera = STATE_CAMERA_FOCUSING;
			// mSoundManager.play(2);
			mSoundManager.play(6);
			mSurface.mCamera.autoFocus(mAutoFocus);
			if (D) {
				Toast.makeText(AC_Main.this, "Focus!", 1000).show();
			}
		} else if (stateCamera == STATE_VIDEO_READY
				&& whichCamera == mSurface.CAMERA_BACK) {
			// stateCamera = STATE_CAMERA_FOCUSING;
			// mSoundManager.play(2);
			mSoundManager.play(6);
			mSurface.mCamera.autoFocus(mAutoFocus);
			if (D) {
				Toast.makeText(AC_Main.this, "Focus!", 1000).show();
			}
		} else if (stateCamera == STATE_VIDEO_RECORDING
				&& whichCamera == mSurface.CAMERA_BACK) {
			// stateCamera = STATE_CAMERA_FOCUSING;
			// mSoundManager.play(2);
			try {
				mSoundManager.play(6);
				mSurface.mCamera.autoFocus(mAutoFocus);
				if (D) {
					Toast.makeText(AC_Main.this, "Focus!", 1000).show();
				}
			} catch (Exception e) {
				Log.e("smardi.Cliq", "ERROR:" + e.getLocalizedMessage());
			}
		} else {
			isFocusing = false;
			mSoundManager.play(1);
			takePicture();

			Log.e(TAG, "User commanded AUTOFOCUS, but camera not ready"
					+ stateCamera);
			isFocusing = false;
		}
	}

	private void takePictureByTrigger() {
		if (stateCamera == STATE_CAMERA_READY) {
			if (mCameraParametes.getTimerTime() > 0) {
				isInTimingShot = true;
			} else {
				// takePicture(); 수정 5월 19일
				autoFocus();
				isFocusedByCliq = true;
			}
		} else if (stateCamera == STATE_VIDEO_READY && isRecording == false) {
			if (mCameraParametes.getTimerTime() > 0) {
				isInTimingShot = true;
			} else {
				takeVideo();
			}
		} else if (stateCamera == STATE_VIDEO_RECORDING || isRecording == true) {
			stopRecording();
		} else {
			stateCamera = STATE_CAMERA_READY;
			autoFocus();
			isFocusedByCliq = true;

			/*
			 * Log.e(TAG, "commanded takePicture(), but camera is not ready:" +
			 * stateCamera);
			 */
		}
	}

	private void takeTimingPicture() {
		if (isTimerON == true) {
			if (stateCamera == STATE_CAMERA_READY) {
				mSoundManager.play(5);
				takePicture();
				isTimerON = false;
			} else if (stateCamera == STATE_VIDEO_READY) {
				takeVideo();
				isTimerON = false;
			} else {
				Log.e(TAG,
						"commanded takeTimingPicture(), but camera is not ready:"
								+ stateCamera);
			}
		}

	}

	private void takePicture() {
		Log.e(TAG, "isFocusing:" + isFocusing);

		if (isFocusing == false && isTakingPicture == false) {
			stateCamera = STATE_CAMERA_TAKING_TIMING_PHOTO;

			try {
				whiteScreen.setVisibility(View.VISIBLE);
				mSurface.mCamera.takePicture(null, null, mPicture);
				isTakingPicture = true;
			} catch (Exception e) {
				Log.e(TAG, "Error in takePicture():" + e.getLocalizedMessage());
			}
			txt_time.setVisibility(View.GONE);
			if (D) {
				Toast.makeText(AC_Main.this, "Shot!", 1000).show();
			}
		}
	}

	private void takeVideo() {
		stateCamera = STATE_VIDEO_RECORDING;
		txt_time.setVisibility(View.GONE);
		/*
		 * light_onAir.setImageDrawable(getResources().getDrawable(
		 * R.drawable.on_air_on));
		 */
		startRecording();
	}

	// 클리커 모드이면서 클리커 상태등이 클릭되면 켜고 끈다.
	private void changeFont() {
		Typeface tf = Typeface.createFromAsset(getAssets(),
				"fonts/MuseoSans_700.otf");

		txt_time.setTypeface(tf);
	}

	Handler mHandler = new Handler(new Handler.Callback() {
		
		private int targetBottomMargin = -70;
		private float currentBottomMargin = 0f;
		private float speed = 1.5f;
		private boolean isMoveStarted = false; // 움직이는 것이 시작됐는지
		private long time_movingStart = 0; // 움직임이 시작된 시간
		private float time_movingFinish = 0.8f; // 움직임이 끝나는 시간(경과한 시간), 초(sec)단위
		private final int GAGE_HIDE = 0;
		private final int GAGE_SHOW = 1;
		private int stateGage = GAGE_SHOW;

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_AUTOFOCUSING:
				autoFocus();
				isFocusedByCliq = true;
				isAutoFocused = true;
				break;
			case WHAT_CHANGE_POSITION_CONTROL_LAYOUT:
				if (isControlLayoutShow == true
						&& controlLayout_linearParams.leftMargin < 0) {
					controlLayout_linearParams.leftMargin += 20;
					// TODO
					controlLayout.setLayoutParams(controlLayout_linearParams);
				} else if (isControlLayoutShow == false
						&& controlLayout_linearParams.leftMargin > -controlLayoutWidth) {
					controlLayout_linearParams.leftMargin -= 20;
					controlLayout.setLayoutParams(controlLayout_linearParams);
				}
				break;
			case WHAT_REMAIN_TIME:
				txt_time.setVisibility(View.VISIBLE);
				int remainTime = (int) Math.floor(msg.arg1 / 1000.) + 1;
				if (old_remainTime > remainTime && remainTime > 0) {
					mSoundManager.play(3);
					txt_time.setText(String.valueOf(remainTime));
					old_remainTime = remainTime;
				}
				break;
			case WHAT_TIMING_TAKE_PHOTO:
				isTimerON = true;
				takeTimingPicture();
				old_remainTime = Integer.MAX_VALUE;
				break;
			case WHAT_SOUND_GAGE_MOVE:
				// Log.e("smardi.Cliq", "isShow:"+isShowSoundPowerBar+ " state:"
				// + stateGage);
				if (isShowSoundPowerBar == false && stateGage == GAGE_SHOW) {
					if (isMoveStarted == false) {
						isMoveStarted = true;
						time_movingStart = new Date().getTime();
					}
					float pastTime = (new Date().getTime() - time_movingStart) / 1000f;

					if (pastTime <= time_movingFinish) {
						FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) soundSensitivityWrap
								.getLayoutParams();
						params.setMargins(params.leftMargin, params.topMargin,
								params.rightMargin,
								(int) getSoundGageMargin(pastTime));
						soundSensitivityWrap.setLayoutParams(params);
					} else {
						isMoveStarted = false;
						stateGage = GAGE_HIDE;
					}
				} else if (isShowSoundPowerBar == true
						&& stateGage == GAGE_HIDE) { // 튜토리얼 화면이 아닐 때에만
					if (tutorial.getVisibility() == View.INVISIBLE) {

						if (isMoveStarted == false) {
							isMoveStarted = true;
							time_movingStart = new Date().getTime();
						}
						float pastTime = time_movingFinish
								- (new Date().getTime() - time_movingStart)
								/ 1000f;
						// Log.e("smardi.Cliq", "pastTime:"+pastTime);

						if (pastTime <= time_movingFinish && 0 < pastTime) {
							FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) soundSensitivityWrap
									.getLayoutParams();
							params.setMargins(params.leftMargin,
									params.topMargin, params.rightMargin,
									(int) getSoundGageMargin(pastTime));
							soundSensitivityWrap.setLayoutParams(params);
						} else {
							isMoveStarted = false;
							stateGage = GAGE_SHOW;
						}
						/*
						 * FrameLayout.LayoutParams params =
						 * (FrameLayout.LayoutParams)
						 * soundSensitivityWrap.getLayoutParams();
						 * if(currentBottomMargin <= 0) { currentBottomMargin +=
						 * speed; params.setMargins(params.leftMargin,
						 * params.topMargin, params.rightMargin, (int)
						 * currentBottomMargin);
						 * soundSensitivityWrap.setLayoutParams(params); }
						 */
					}
				}
				break;
			case WHAT_CLEAN_SCREEN:
				whiteScreen.setVisibility(View.GONE);
				// 셔터 원래대로
				btn_shutter.setImageDrawable(getResources().getDrawable(
						R.drawable.c_shutter));
				break;
			}

			return false;
		}
	});

	private int getSoundGageMargin(float time) {
		int margin = 0;
		double[] a = { 13020.8333307260, -25260.4166616223, 16041.6666633067,
				-3739.5833324458, 237.4999999255, 0.0000000000 };

		margin = (int) (a[0] * Math.pow(time, 5) + a[1] * Math.pow(time, 4)
				+ a[2] * Math.pow(time, 3) + a[3] * Math.pow(time, 2) + a[4]
				* Math.pow(time, 1) + a[5] * Math.pow(time, 0));

		return margin;
	}

	// 스레드
	Thread mThread = new Thread(new Runnable() {
		private final int sleepTime = 25;
		private int countBlackScreen = 0;

		private boolean oldIsShowSoundPowerBar = true;

		@Override
		public void run() {
			while (isRunThread) {
				if (isPressed == true && isAutoFocused == false) {
					// 클리커가 눌린 것이 감지되면
					if (new Date().getTime() - timeCliqPressed > TIME_LONGCLICK) {
						
						if(mCameraParametes.getTimerTime() > 0) {
							isInTimingShot = true;
						} else if(isInTimingShot == false) {
							mHandler.sendEmptyMessage(WHAT_AUTOFOCUSING);
						}
						//TODO
						// isPressed = false;
					}
				}

				if (mSurface.isSetCameraParameters == true
						&& isSetCameraParameters == false) {
					if (mCameraParametes == null) {
						mCameraParametes = mSurface.mCameraParameters;
						//타이머를 0으로 초기화
						mCameraParametes.setTimerTime(0);
					}

					isSetCameraParameters = true;
					if (D) {
						Log.e(TAG, "We read cameraparameters complete!");
					}
				} else if (isSetCameraParameters == false) {
					if (D) {
						Log.e(TAG, "We cannot read cameraparameters.");
					}
				}

				// 블랙 또는 화이트 스크린을 사라지게 한다.
				if (whiteScreen.getVisibility() == View.VISIBLE) {
					if (countBlackScreen == 12) {
						countBlackScreen = 0;
						mHandler.sendEmptyMessage(WHAT_CLEAN_SCREEN);
					}
					countBlackScreen++;
				}

				// 우측의 컨트롤 패널에 애니메이션을 적용한다.
				if (controlLayout_linearParams != null) {
					mHandler.sendEmptyMessage(WHAT_CHANGE_POSITION_CONTROL_LAYOUT);
				}
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// 타이머 설정을 적용한다.
				if (isInTimingShot == true) {
					Log.e(TAG, "time:" + countPastTime + " timer:"
							+ mCameraParametes.getTimerTime());

					countPastTime += sleepTime;
					int remainTime = mCameraParametes.getTimerTime() * 1000
							- countPastTime;
					// 남은 시간을 나타내준다.
					Message msg = new Message();
					msg.what = WHAT_REMAIN_TIME;
					msg.arg1 = remainTime;
					mHandler.sendMessage(msg);

					if (remainTime < 0) {
						// 시간이 되었으므로 사진을 촬영한다.
						mHandler.sendEmptyMessage(WHAT_TIMING_TAKE_PHOTO);
						isInTimingShot = false;
						countPastTime = 0;
					}
				}

				// 사운드 파워 게이지를 나타내고 숨긴다.
				if (soundSensitivityWrap != null) {
					Message msg = new Message();
					msg.what = WHAT_SOUND_GAGE_MOVE;
					mHandler.sendMessage(msg);
				}
			}
		}
	});

	// ----------------------------------------------------------------------
	// Video 캡쳐
	// ----------------------------------------------------------------------
	@SuppressWarnings("static-access")
	protected void startRecording() {
		if (!Build.DEVICE.equals("SHW-M250S")) { // 갤스2 일 경우
			return;
		}

		// try {
		mMediaRecorder = new MediaRecorder(); // Works well
		mSurface.mCamera.unlock();

		mMediaRecorder.setCamera(mSurface.mCamera);

		// mMediaRecorder.setPreviewDisplay(mSurface.getHolder().getSurface());
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		// mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		// mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

		if (whichCamera == mSurface.CAMERA_BACK) {
			mMediaRecorder.setProfile(CamcorderProfile
					.get(CamcorderProfile.QUALITY_HIGH));
		} else {
			mMediaRecorder.setProfile(CamcorderProfile
					.get(CamcorderProfile.QUALITY_LOW));
		}
		mMediaRecorder.setPreviewDisplay(mSurface.getHolder().getSurface());
		mMediaRecorder.setOutputFile(getVideoFilename());

		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.e(TAG, "IllegalStateException:" + e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "IOException:" + e.getLocalizedMessage());
			e.printStackTrace();
		}

		mMediaRecorder.start();

		isRecording = true;
		// } catch (Exception e) {
		// Log.e("smardi.Cliq", "ERROR:"+e.getLocalizedMessage());
		// }
	}

	protected void stopRecording() {
		mMediaRecorder.stop();
		mMediaRecorder.release();
		mSurface.mCamera.release();
		mSurface.mCamera = null;
		mSurface.openCamera_BackOrFront(whichCamera);
		mSurface.destroyDrawingCache();
		mSurface.resumePreview();

		stateCamera = STATE_VIDEO_READY;
		isRecording = false;
		/*
		 * light_onAir.setImageDrawable(getResources().getDrawable(
		 * R.drawable.on_air_off));
		 */
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset(); // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			mSurface.mCamera.lock(); // lock camera for later use
		}
	}

	private void releaseCamera() {
		if (mSurface.mCamera != null) {
			mSurface.mCamera.release(); // release the camera for other
										// applications
			mSurface.mCamera = null;
		}
	}
}

