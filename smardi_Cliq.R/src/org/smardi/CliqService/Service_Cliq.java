package org.smardi.CliqService;

import java.io.*;
import java.text.*;
import java.util.*;

import org.hermit.audalyzer.AudioReader;
import org.hermit.audalyzer.FFTTransformer;
import org.hermit.audalyzer.SignalPower;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

public class Service_Cliq extends Service {

	// 시작 ID
	private int mStartID;

	private final String TAG = "FFTService";
	private final boolean D = false;

	// AlertDialog
	AlertDialog.Builder builder;
	AlertDialog alertDialog;

	// Our audio input device.
	private AudioReader audioReader;

	// Fourier Transform calculator we use for calculating the spectrum and
	// sonagram.
	private FFTTransformer spectrumAnalyser;

	// Analysed audio spectrum data; history data for each frequency
	// in the spectrum; index into the history data; and buffer for
	// peak frequencies.
	private float[] spectrumData;
	private float[][] spectrumHist;
	private int spectrumIndex;

	// The desired sampling rate for this analyser, in samples/sec.
	private int sampleRate = 44100;

	// Audio input block size, in samples.
	private int inputBlockSize = 1024;

	// The selected windowing function.
	private org.hermit.audalyzer.Window.Function windowFunction = org.hermit.audalyzer.Window.Function.BLACKMAN_HARRIS;

	// The desired decimation rate for this analyser. Only 1 in
	// sampleDecimate blocks will actually be processed.
	private int sampleDecimate = 1;

	// The desired histogram averaging window. 1 means no averaging.
	private int historyLen = 1;

	// Temp. buffer for calculated bias and range.
	private float[] biasRange = null;

	// Sequence number of the last block we processed.
	private long audioProcessed = 0;

	// Buffered audio data, and sequence number of the latest block.
	private short[] audioData;
	private long audioSequence = 0;

	// If we got a read error, the error code.
	private int readError = AudioReader.Listener.ERR_OK;

	// Current signal power level, in dB relative to max. input power.
	private double currentPower = 0f;

	// Message
	Message mMsg = new Message();

	// ==============================================================
	private final int WHAT_SPECTRUMDATA = 0;
	private final int WHAT_CURRENTPOWER = 1;

	// ==============================================================
	public static Manage_CLIQ_SharedPreference mSharedPreference;

	public static final String ACTION_CLIQ_START = "org.smardi.FFT_Broadcaster.coolickerStart";
	public static final String ACTION_CLIQ_STOP = "org.smardi.FFT_Broadcaster.coolickerStop";

	public static final String ACTION_CLIQ_REGISTRATION_START = "org.smardi.FFT_Broadcaster.registrationStart";
	public static final String ACTION_CLIQ_REGISTRATION_RESTART = "org.smardi.FFT_Broadcaster.registrationReStart";
	public static final String ACTION_CLIQ_REGISTRATION_END = "org.smardi.FFT_Broadcaster.registrationEnd";
	public static final String ACTION_CLIQ_REGISTRATION_END_ERROR = "org.smardi.FFT_Broadcaster.registrationEndError";
	public static final String ACTION_CLIQ_REGISTRATION_FAIL = "org.smardi.FFT_Broadcaster.registrationFail";
	public static final String ACTION_CLIQ_REGISTRATION_CLOSE = "org.smardi.FFT_Broadcaster.registrationClose";

	public static final String ACTION_CLIQ_TEST_START = "org.smardi.FFT_Broadcaster.testStart";
	public static final String ACTION_CLIQ_TEST_END = "org.smardi.FFT_Broadcaster.testEnd";

	public static final String ACTION_SET_THREADHOLD_POWER = "org.smardi.FFT_Broadcaster.setThreadholdPower";
	public static final String ACTION_GET_THREADHOLD_POWER = "org.smardi.FFT_Broadcaster.getThreadholdPower";
	public static final String ACTION_SEND_THREADHOLD_POWER = "org.smardi.FFT_Broadcaster.sendThreadholdPower";
	public static final String ACTION_GET_CLIQ_FREQUENCY = "org.smardi.FFT_Broadcaster.getCoolickerFrequency";
	public static final String ACTION_SEND_CLIQ_FREQUENCY = "org.smardi.FFT_Broadcaster.sendCoolickerFrequency";
	public static final String ACTION_GREEN_BUTTON_IS_ON = "org.smardi.FFT_Broadcaster.greenButtonIsOn";
	public static final String ACTION_GRAY_BUTTON_IS_ON = "org.smardi.FFT_Broadcaster.grayButtonIsOn";
	public static final String ACTION_CLIQ_CLICK_TRIGERED = "org.smardi.FFT_Broadcaster.clickTrigered";
	public static final String ACTION_CLIQ_PRESS_TRIGERED = "org.smardi.FFT_Broadcaster.pressTrigered";
	public static final String ACTION_CLIQ_RELEASE_TRIGERED = "org.smardi.FFT_Broadcaster.releaseTrigered";
	public static final String ACTION_CLIQ_TEST_CLICK_TRIGERED = "org.smardi.FFT_Broadcaster.testclickTrigered";
	public static final String ACTION_CLIQ_TEST_PRESS_TRIGERED = "org.smardi.FFT_Broadcaster.testpressTrigered";
	public static final String ACTION_CLIQ_TEST_RELEASE_TRIGERED = "org.smardi.FFT_Broadcaster.testreleaseTrigered";
	public static final String ACTION_CLIQ_DOUBLECLICK_TRIGERED = "org.smardi.FFT_Broadcaster.doubleclickTrigered";
	public static final String ACTION_CLIQ_LONGCLICK_TRIGERED = "org.smardi.FFT_Broadcaster.longclickTrigered";
	public static final String ACTION_SOUND_POWER_TRIGERED = "org.smardi.FFT_Broadcaster.soundPowerTrigered";

	public static final String ACTION_MODE_NONE_ON = "org.smardi.FFT_Broadcaster.noneModeOn";
	public static final String ACTION_MODE_CLIQING_ON = "org.smardi.FFT_Broadcaster.coolickingModeOn";
	public static final String ACTION_MODE_AMPLITUDE_ON = "org.smardi.FFT_Broadcaster.amplitudeModeOn";

	public static final String ACTION_CLIQ_SOUNDPOWER = "org.smardi.FFT_Broadcast.soundpower";
	// public static final String ACTION_

	// 계산을 위한 변수들 선언
	int[] mMaxData; // 주파수 분석 결과의 최대값 저장(0: index, 1: value)
	ArrayList<Integer> list_maxIndexGreen = null; // 초록 버튼이 켜졌을 당시에 가장 높은 값을 가졌던
													// 인덱스들
	ArrayList<Integer> list_maxValueGreen = null; // 초록 버튼이 켜졌을 당시에 가장 높은 값들
	ArrayList<float[]> list_spectrumDataGreen = null; // 초록 버튼이 켜졌을 당시에 로우데이터들
	ArrayList<Integer> list_maxIndexGray = null; // 회색 버튼이 켜졌을 당시에 가장 높은 값을 가졌던
													// 인덱스들
	ArrayList<Integer> list_maxValueGray = null; // 회색 버튼이 켜졌을 당시에 가장 높은 값들
	ArrayList<float[]> list_spectrumDataGray = null; // 회색 버튼이 켜졌을 당시에 로우데이터들

	ArrayList<Integer> sortedFrequencyIndex = null; // 빈도수 별로 내림차순 정렬 된 인덱스
	ArrayList<Integer> sortedFrequencyRepeat = null; // 빈도수 별로 내림차순 정렬 된 빈도수

	private boolean isRegistrating = false; // 주파수를 측정하고 있는 중인지 (등록하는 과정이 진행
											// 중인지)
	private boolean isTesting = false; // 테스트를 하는 중인지
	private boolean isGreenButtonON = false; // 초록 버튼이 활성화 되어있는지

	private boolean isRecordingRunning = false; // 녹음 중인지

	// ------------------------------------------
	private boolean isCliqRunning = false; // 쿨리커 감청이 시작되었는지.
	// ------------------------------------------
	private int cliqMode = -1; // 클리커 모드
	private final int MODE_NONE = -1;
	private final int MODE_CLIQING = 0;
	private final int MODE_AMPLITUDE = 1;

	// ------------------------------------------
	private int cliqState = 0;
	private final int STATE_READY = 0;
	private final int STATE_PRESS = 1;
	private final int STATE_RELEASE = 2;
	private long time_soundDetect = 0;
	private long time_oldReleased = 0;
	private long time_Pressed = 0;
	private long time_Released = 0;
	private boolean isTrigered = false; // 이벤트를 전송했는지

	private final int DETECTING_MIN_FREQ = 15000;

	private long time_lastCalculated = 0;

	// -------------------------------------
	// 노이즈의 경우 CLIQ.r과 유사한 주파수를 발생할 수 있으나
	// 대부분 1회성 신호에 그침.
	// 3회에 걸쳐서 동일한 신호가 들어오는 경우에는 클리커에서 신호가 발생한 것으로 감지하도록 수정
	private int count_cliq_occured = 0; // 클리커가 눌렸음을 감지한 횟수
	private int CRIT_CLIQ_OCCURED = 3; // 노이즈와 클리커의 신호를 구분하기 위한 변수

	// -------------------------------------

	@Override
	public void onCreate() {
		makeLog("FFTService created.");
		super.onCreate();

		mMaxData = new int[2];

		// -----------------------------------------
		audioReader = new AudioReader();
		spectrumAnalyser = new FFTTransformer(inputBlockSize, windowFunction);

		// Allocate the spectrum data.
		spectrumData = new float[inputBlockSize / 2];
		spectrumHist = new float[inputBlockSize / 2][historyLen];
		spectrumIndex = 0;

		biasRange = new float[2];

		/**
		 * We are starting the main run; start measurements.
		 */
		audioProcessed = audioSequence = 0;
		// readError = Listener.ERR_OK;

		// txt_msg = (TextView)findViewById(R.id.txt_msg);
		// sufView = new SufView(Main.this);
		// setContentView(sufView);

		mSharedPreference = new Manage_CLIQ_SharedPreference(Service_Cliq.this);

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_CLIQ_START);
		filter.addAction(ACTION_CLIQ_STOP);
		filter.addAction(ACTION_CLIQ_REGISTRATION_START);
		filter.addAction(ACTION_CLIQ_REGISTRATION_RESTART);
		filter.addAction(ACTION_CLIQ_REGISTRATION_END);
		filter.addAction(ACTION_CLIQ_REGISTRATION_END_ERROR);
		filter.addAction(ACTION_CLIQ_REGISTRATION_CLOSE);

		filter.addAction(ACTION_CLIQ_TEST_START);
		filter.addAction(ACTION_CLIQ_TEST_END);

		filter.addAction(ACTION_SET_THREADHOLD_POWER);
		filter.addAction(ACTION_GREEN_BUTTON_IS_ON);
		filter.addAction(ACTION_GRAY_BUTTON_IS_ON);

		filter.addAction(ACTION_MODE_NONE_ON);
		filter.addAction(ACTION_MODE_CLIQING_ON);
		filter.addAction(ACTION_MODE_AMPLITUDE_ON);
		filter.addAction(ACTION_GET_CLIQ_FREQUENCY);
		filter.addAction(ACTION_GET_THREADHOLD_POWER);

		// filter.addAction(ACTION_COOLICKER_CLICK_TRIGERED);
		// filter.addAction(ACTION_COOLICKER_DOUBLECLICK_TRIGERED);
		// filter.addAction(ACTION_COOLICKER_LONGCLICK_TRIGERED);
		// filter.addAction(ACTION_SOUND_POWER_TRIGERED);

		registerReceiver(mBroadcastReceiver, filter);

		// 클리커를 켜고 끄는 스레드 작동
		mThreadCheckCliqState.start();
		isThreadCheckCliqState_running = true;
	}

	private void startRecording() {
		if (isRecordingRunning == false) {
			makeLog("startRecording");
			// Log.e("Test", "startRecording inside");
			audioReader.startReader(sampleRate,
					inputBlockSize * sampleDecimate,
					new AudioReader.Listener() {
						@Override
						public final void onReadComplete(short[] buffer) {
							// receiveAudio(buffer);
							processAudio(buffer);

							if (isRegistrating == false && isTesting == false) {
								if (cliqMode == MODE_CLIQING) {
									if (checkIsCliqingOccured() == true) {

										count_cliq_occured = count_cliq_occured + 1;

										if (isTrigered == false
												&& CRIT_CLIQ_OCCURED <= count_cliq_occured) {
											time_Pressed = new Date().getTime();

											Log.e("smardi.Cliq", "TRIGERD");
											Intent intent = new Intent();
											intent.setAction(ACTION_CLIQ_CLICK_TRIGERED);
											sendBroadcast(intent);
											isTrigered = true;
										} else if (new Date().getTime()
												- time_Pressed < 200) {
											time_Pressed = new Date().getTime();
										}
									} else {

										count_cliq_occured = 0;

										if (new Date().getTime() - time_Pressed >= 200) {
											Intent intent = new Intent();
											intent.setAction(ACTION_CLIQ_RELEASE_TRIGERED);
											sendBroadcast(intent);
											isTrigered = false;
										}
									}
								} else if (cliqMode == MODE_AMPLITUDE) {
									detectBigSound();
								}
							} else if (isTesting == true) {
								if (checkIsCliqingOccured() == true) {
									
									count_cliq_occured = count_cliq_occured + 1;
									
									if (isTrigered == false && CRIT_CLIQ_OCCURED <= count_cliq_occured) {
										time_Pressed = new Date().getTime();

										Log.e("smardi.Cliq", "TEST TRIGERD");
										Intent intent = new Intent();
										intent.setAction(ACTION_CLIQ_TEST_CLICK_TRIGERED);
										sendBroadcast(intent);
										isTrigered = true;
									} else if (new Date().getTime()
											- time_Pressed < 200) {
										time_Pressed = new Date().getTime();
									}
								} else {
									
									count_cliq_occured = 0;
									
									if (new Date().getTime() - time_Pressed >= 200) {
										Intent intent = new Intent();
										intent.setAction(ACTION_CLIQ_TEST_RELEASE_TRIGERED);
										sendBroadcast(intent);
										isTrigered = false;
									}
								}
							}
						}

						@Override
						public void onReadError(int error) {
							handleError(error);
						}
					});
			isRecordingRunning = true;
		}
	}

	protected boolean detectBigSound() {
		int threadholdPower = mSharedPreference.getThreadholdPower();

		sendCurrentPower(currentPower);

		// Log.e("Test", "threadholdPower:"+threadholdPower + " currentPower:" +
		// currentPower);
		if (threadholdPower < currentPower
				&& new Date().getTime() - time_soundDetect > 2000) {
			if (isTrigered == false) {
				time_soundDetect = new Date().getTime();

				Intent intent = new Intent();
				intent.setAction(ACTION_CLIQ_CLICK_TRIGERED);
				sendBroadcast(intent);
				isTrigered = true;
			} else if (new Date().getTime() - time_soundDetect < 400) {
				time_soundDetect = new Date().getTime();
			}
		} else {
			if (new Date().getTime() - time_soundDetect >= 400) {
				Intent intent = new Intent();
				intent.setAction(ACTION_CLIQ_RELEASE_TRIGERED);
				sendBroadcast(intent);
				isTrigered = false;
			}
		}

		return false;
	}

	private void sendCurrentPower(double power) {
		Intent intent = new Intent();
		intent.setAction(ACTION_CLIQ_SOUNDPOWER);
		intent.putExtra("power", power);

		sendBroadcast(intent);
	}

	private void stopRecording() {
		if (isRecordingRunning == true) {
			isRecordingRunning = false;
			makeLog("stopRecording");
			audioReader.stopReader();
		}
	}

	protected boolean checkIsCliqingOccured() {
		float cliqFrequencyPower = getCliqFrequencyPower();

		int cliqSensitivity = mSharedPreference.getCliqSensitivity();

		if (getDB(cliqFrequencyPower) < 0) {
			return false;
		}

		if ((double) getDB(cliqFrequencyPower) > (double) getDB(getCliqThreadhold())
				* (1.05 + (double) cliqSensitivity / 100. / 5.)) {

			if (D) {
				Log.e("Test", mSharedPreference.getCliqFrequencyIndex()
						+ ") CoolickFreq.:" + getDB(cliqFrequencyPower)
						+ " Threadhold:" + getDB(getCliqThreadhold()));
				Log.e("smardi.Cliq", "sensitivity:"
						+ (1.05 + (double) cliqSensitivity / 100. / 2.));
				Log.e("smardi.Cliq", "sensitivity-value:"
						+ (double) getDB(getCliqThreadhold())
						* (1.05 + (double) cliqSensitivity / 100. / 2.));
			}

			// 클리커에서 신호가 들어왔다고 판단된 당시의 10000Hz 이상의 주파수를 csv 파일 형식으로 저장하기 위한 부분
			String temp = "";
			for (int i = 0; i < spectrumData.length; i++) {
				if (convertIndextToFrequency(i) > 10000) {
					temp += (convertIndextToFrequency(i) + ","
							+ getDB(spectrumData[i]) + "\r\n");
				}
			}

			String filename = new Date().getTime() + ".csv";

			saveFrequency(getFilename(), temp);

			return true;
		} else {
			return false;
		}
	}

	private void saveFrequency(String filename, String content) {
		// String dirPath = getFilesDir().getAbsolutePath();
		File file = new File(getFolder());

		// 일치하는 폴더가 없으면 생성
		if (!file.exists()) {
			file.mkdirs();
		}

		// txt 파일 생성
		String testStr = content;
		File savefile = new File(filename);
		try {
			FileOutputStream fos = new FileOutputStream(savefile);
			fos.write(testStr.getBytes());
			fos.close();
		} catch (IOException e) {

		}
	}

	private String getFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, "FREQUENCY");

		if (!file.exists()) {
			file.mkdirs();
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.KOREA);
		Date currentTime = new Date();
		return (file.getAbsolutePath() + "/" + formatter.format(currentTime) + ".csv");
		// return (file.getAbsolutePath() + "/temp_smardi" +
		// AUDIO_RECORDER_FILE_EXT_WAV);
	}

	private String getFolder() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, "FREQUENCY");

		if (!file.exists()) {
			file.mkdirs();
		}

		return (file.getAbsolutePath());
		// return (file.getAbsolutePath() + "/temp_smardi" +
		// AUDIO_RECORDER_FILE_EXT_WAV);
	}

	private float getDB(float power) {
		return (float) ((float) (Math.log10(power) / 6f + 1f) * 400.);
	}

	private float getCliqFrequencyPower() {
		int LOW_FREQ = mSharedPreference.getCliqFrequency() - 100;
		int HIGH_FREQ = mSharedPreference.getCliqFrequency() + 100;

		if (HIGH_FREQ > 22000) {
			HIGH_FREQ = 22000;
		}

		int LOW_INDEX = convertFrequencyToIndex(LOW_FREQ);
		int HIGH_INDEX = convertFrequencyToIndex(HIGH_FREQ);

		float max = 0;
		int maxINDEX = 0;

		if (LOW_INDEX < 0) {
			LOW_INDEX = 0;
		}

		if (HIGH_INDEX > spectrumData.length - 1) {
			HIGH_INDEX = spectrumData.length - 1;
		} else if (HIGH_INDEX < 0) {
			HIGH_INDEX = 0;
		}

		for (int i = LOW_INDEX; i <= HIGH_INDEX; i++) {
			if (max < spectrumData[i]) {
				max = spectrumData[i];
				maxINDEX = i;
			}
		}

		mSharedPreference.setCliqTempFrequencyIndex(maxINDEX);

		return max;
	}

	private float getCliqThreadhold() {

		int Freq = mSharedPreference.getCliqFrequency();

		// 클리커 주파수에서 +-100 인 주파수는 제외하고 최대값을 구한다.
		int leftFrequencyIndex = convertFrequencyToIndex(Freq - 300);
		int rightFrequencyIndex = convertFrequencyToIndex(Freq + 300);

		float tempMax = 0;
		// int startFrequencyIndex = convertFrequencyToIndex(Freq - 1500);
		// int endFrequencyIndex = convertFrequencyToIndex(Freq + 1500);
		int startFrequencyIndex = convertFrequencyToIndex(14000);
		int endFrequencyIndex = convertFrequencyToIndex(22000);

		int minFrequencyIndex = convertFrequencyToIndex(DETECTING_MIN_FREQ);
		int maxFrequencyIndex = convertFrequencyToIndex(22000);
		if (startFrequencyIndex < minFrequencyIndex) {
			startFrequencyIndex = minFrequencyIndex;
		}
		if (maxFrequencyIndex < endFrequencyIndex) {
			endFrequencyIndex = maxFrequencyIndex;
		}

		// 최대값을 구함.
		for (int i = startFrequencyIndex; i < endFrequencyIndex; i++) {
			if (i < leftFrequencyIndex || rightFrequencyIndex < i) {
				if (tempMax < spectrumData[i]) {
					tempMax = spectrumData[i];
				}
			}
		}

		return tempMax;
	}

	/**
	 * An error has occurred. The reader has been terminated.
	 * 
	 * @param error
	 *            ERR_XXX code describing the error.
	 */
	private void handleError(int error) {
		synchronized (this) {
			readError = error;
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		makeLog("Service startID=" + startId);
		super.onStart(intent, startId);
		mStartID = startId;
	}

	@Override
	public void onDestroy() {
		// onDestroy가 호출되어 서비스가 종료되어도
		// postDelayed는 바로 정지되지 않고 다음번 run 메소드를 호출
		isThreadCheckCliqState_running = false;
		stopRecording();
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case WHAT_CURRENTPOWER:
				double power = (Double) msg.obj;
				break;
			case WHAT_SPECTRUMDATA:
				float[] sData = (float[]) msg.obj;
				mMaxData = getMaxAmplitudeFrequency(sData);

				// makeLog("isRegistrating:"+isRegistrating +
				// "isGreenButtonON:"+isGreenButtonON);
				if (isRegistrating == true && isGreenButtonON == true) {

					if (mMaxData[0] > 0) {
						list_maxIndexGreen.add(mMaxData[0]);
						list_maxValueGreen.add(mMaxData[1]);
						list_spectrumDataGreen.add(sData);
					}
				} else if (isRegistrating == true && isGreenButtonON == false) {
					list_maxIndexGray.add(mMaxData[0]);
					list_maxValueGray.add(mMaxData[1]);
					list_spectrumDataGray.add(sData);
				}

				break;
			}
		}
	};

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (action.equals(ACTION_CLIQ_START)) {
				isCliqRunning = true;
			} else if (action.equals(ACTION_CLIQ_STOP)) {
				isCliqRunning = false;
			}
			if (action.equals(ACTION_CLIQ_REGISTRATION_START)) {
				if (isRegistrating == false) {
					if (isCliqRunning == false) {
						isCliqRunning = true;
					}
					makeLog("Registration start");

					isRegistrating = true;
					list_maxIndexGray = new ArrayList<Integer>();
					list_maxIndexGreen = new ArrayList<Integer>();
					list_maxValueGray = new ArrayList<Integer>();
					list_maxValueGreen = new ArrayList<Integer>();
					list_spectrumDataGray = new ArrayList<float[]>();
					list_spectrumDataGreen = new ArrayList<float[]>();

					// 클리커 주파수를 등록한다는 메시지가 왔을 경우
					Intent popupIntent = new Intent(context,
							AC_SetCliqFrequency.class);

					PendingIntent pie = PendingIntent.getActivity(context, 0,
							popupIntent, PendingIntent.FLAG_ONE_SHOT);
					try {
						pie.send();
					} catch (CanceledException e) {
						Log.e("FFTService", e.getMessage());
					}
				}
			} else if (action.equals(ACTION_CLIQ_REGISTRATION_RESTART)) {
				if (isRegistrating == false) {
					if (isCliqRunning == false) {
						isCliqRunning = true;
					}
					makeLog("Registration re-start");

					isRegistrating = true;
					list_maxIndexGray = new ArrayList<Integer>();
					list_maxIndexGreen = new ArrayList<Integer>();
					list_maxValueGray = new ArrayList<Integer>();
					list_maxValueGreen = new ArrayList<Integer>();
					list_spectrumDataGray = new ArrayList<float[]>();
					list_spectrumDataGreen = new ArrayList<float[]>();

				}
			} else if (action.equals(ACTION_GREEN_BUTTON_IS_ON)) {
				if (isGreenButtonON == false) {
					makeLog("Green button on" + list_maxIndexGreen.size());
					isGreenButtonON = true;
				}

			} else if (action.equals(ACTION_GRAY_BUTTON_IS_ON)) {
				if (isGreenButtonON == true) {
					makeLog("Gray Button on" + list_maxIndexGreen.size());
					isGreenButtonON = false;
				}

			} else if (action.equals(ACTION_CLIQ_REGISTRATION_END)) {

				makeLog("isRegistrating:" + isRegistrating);

				if (isRegistrating == true) {
					if (cliqMode == MODE_NONE) {
						isCliqRunning = false;
					} else {
						isCliqRunning = true;
					}
					makeLog("Registration end" + list_maxIndexGreen.size());

					isRegistrating = false;
					int cliqFreq = calculateRegistrationData();

					// 클리커 주파수가 0보다 작으면 제대로 설정이 안된 것
					if (cliqFreq < 0) {
						makeLog("Cliq.R registration is FAIL");
						makeLog("Cliq.R freq:" + cliqFreq);
						intent = new Intent();
						intent.setAction(ACTION_CLIQ_REGISTRATION_FAIL);
						sendBroadcast(intent);
					} else {
						makeLog("Cliq.R freq:" + cliqFreq);
						// Intent broadIntent = new Intent();
						mSharedPreference.setCliqFrequency(cliqFreq);
						mSharedPreference
								.setCliqFrequencyIndex(sortedFrequencyIndex
										.get(0));
						intent = new Intent();
						intent.setAction(ACTION_SEND_CLIQ_FREQUENCY);
						sendBroadcast(intent);
						// Toast.makeText(context,
						// "Culiq frequency:"+coolickerFreq+"Hz", 1000).show();
						// Toast.makeText(context,
						// "Index:"+sortedFrequencyIndex.get(0), 1000).show();
					}
				}

			} else if (action.equals(ACTION_CLIQ_REGISTRATION_CLOSE)) {
				isRegistrating = false;

				isCliqRunning = false; // 이렇게 하면 Preference 페이지에서 무조건 마이크가 꺼짐

				/*
				 * 이렇게 하면 클리커 모드가 켜져 있을 경우 Preference 페이지에서도 마이크가 활성화 되어있음
				 * if(cliqMode == MODE_NONE) { isCliqRunning = false; }else {
				 * isCliqRunning = true; }
				 */

			} else if (action.equals(ACTION_CLIQ_REGISTRATION_END_ERROR)) {
				isRegistrating = false;
			} else if (action.equals(ACTION_CLIQ_TEST_START)) {
				// 만약에 Frequency가 기본값인 0이면 설정을 하라고 메시지를 출력한다.
				if (mSharedPreference.getCliqFrequency() == 0) {
					Toast.makeText(context, "Please regist your CLIQ.r!", 1000)
							.show();
					intent = new Intent();
					intent.setAction(ACTION_CLIQ_REGISTRATION_START);
					sendBroadcast(intent);
					return;
				}

				isTesting = true;
				isCliqRunning = true;
				// cliqMode = MODE_CLIQING;
				// 클리커 연결을 테스트한다는 메시지가 왔을 경우
				Intent popupIntent = new Intent(context,
						AC_CliqConnectionTest.class);

				PendingIntent pie = PendingIntent.getActivity(context, 0,
						popupIntent, PendingIntent.FLAG_ONE_SHOT);
				try {
					pie.send();
				} catch (CanceledException e) {
					Log.e("Test", e.getMessage());
				}
			} else if (action.equals(ACTION_CLIQ_TEST_END)) {
				isTesting = false;

				isCliqRunning = false; // 테스트가 끝나면 마이크를 끔
			} else if (action.equals(ACTION_SET_THREADHOLD_POWER)) {
				// 볼륨 인식 최소 값을 전달받았을 때
				mSharedPreference.setThreadholdPower(intent.getIntExtra(
						"threadholdPower",
						intent.getIntExtra("ThreadholdPower", 70)));
				// Toast.makeText(context, "Saved", 1000).show();

			} else if (action.equals(ACTION_MODE_NONE_ON)) {
				// Toast.makeText(context, "NONE MODE", 1000).show();
				cliqMode = MODE_NONE;
				isCliqRunning = false;
			} else if (action.equals(ACTION_MODE_CLIQING_ON)) {
				// Toast.makeText(context, "COOLICK MODE", 1000).show();
				cliqMode = MODE_CLIQING;
				isCliqRunning = true;
			} else if (action.equals(ACTION_MODE_AMPLITUDE_ON)) {
				// Toast.makeText(context, "AMPLITUDE MODE", 1000).show();
				cliqMode = MODE_AMPLITUDE;
				isCliqRunning = true;
			}

			else if (action.equals(ACTION_GET_CLIQ_FREQUENCY)) {
				intent = new Intent();
				intent.setAction(ACTION_SEND_CLIQ_FREQUENCY);
				sendBroadcast(intent);
			} else if (action.equals(ACTION_GET_THREADHOLD_POWER)) {
				intent = new Intent();
				intent.setAction(ACTION_SEND_THREADHOLD_POWER);
				intent.putExtra("threadhold",
						mSharedPreference.getThreadholdPower());
				sendBroadcast(intent);
			}
		}

		/**
		 * 
		 * 
		 * @return
		 */
		private int calculateRegistrationData() {

			// 수집된 최대값을 갖는 frequency index들의 빈도를 계산한다.
			ArrayList<Integer> UnsortedFrequencyIndex = new ArrayList<Integer>();
			ArrayList<Integer> UnsortedFrequencyRepeate = new ArrayList<Integer>();

			// makeLog("list_maxIndexGreen.size()="+list_maxIndexGreen.size());

			for (; list_maxIndexGreen.size() > 0;) {
				int tempIndex = list_maxIndexGreen.get(0);
				int countRepeat = 0;

				for (int j = 0; list_maxIndexGreen.size() > 0
						&& j < list_maxIndexGreen.size(); j++) {
					if (tempIndex == list_maxIndexGreen.get(j)) {
						list_maxIndexGreen.remove(j);
						list_maxValueGreen.remove(j);
						j--;
						countRepeat++;
					}
				}

				UnsortedFrequencyIndex.add(tempIndex);
				UnsortedFrequencyRepeate.add(countRepeat);
			}

			// 계산된 인덱스별 빈도값을 빈도에 대한 내림차순으로 정렬한다.
			// 빈도수를 int 배열에 입력한다.
			int[] sortedFrequencyRepeat_intArray = new int[UnsortedFrequencyIndex
					.size()];
			for (int i = 0; i < UnsortedFrequencyIndex.size(); i++) {
				sortedFrequencyRepeat_intArray[i] = UnsortedFrequencyRepeate
						.get(i);
			}
			// 정렬한다.
			Arrays.sort(sortedFrequencyRepeat_intArray);
			// 역정렬한다.(내림차순으로 변환)
			reverseArrayInt(sortedFrequencyRepeat_intArray);

			// 정렬된 빈도수에 맞게 index를 정렬한다.
			sortedFrequencyIndex = new ArrayList<Integer>();
			sortedFrequencyRepeat = new ArrayList<Integer>();
			for (int i = 0; i < UnsortedFrequencyIndex.size(); i++) {
				sortedFrequencyIndex.add(UnsortedFrequencyIndex
						.get(UnsortedFrequencyRepeate
								.indexOf(sortedFrequencyRepeat_intArray[i])));
				sortedFrequencyRepeat.add(sortedFrequencyRepeat_intArray[i]);
			}

			int tempCliqFrequency = 0;

			if (sortedFrequencyIndex.size() > 0) {
				tempCliqFrequency = convertIndextToFrequency(sortedFrequencyIndex
						.get(0) - 1);
			} else {
				tempCliqFrequency = -1;
			}

			// 검증작업은 추후 구현

			return tempCliqFrequency;
		}
	};

	private int convertIndextToFrequency(Integer frequencyIndex) {
		return (int) Math.round((double) (22050. / 512)
				* (double) (frequencyIndex + 1));
	}

	public static void reverseArrayInt(int[] array) {
		int temp;

		for (int i = 0; i < array.length / 2; i++) {
			temp = array[i];
			array[i] = array[(array.length - 1) - i];
			array[(array.length - 1) - i] = temp;
		}
	}

	/**
	 * 수집된 주파수 배열 중에서 가장 큰 값을 갖는 주파수 값과 그 때의 크기를 리턴한다.
	 * 
	 * @param fftResult
	 *            수집된 주파수 배열 0Hz ~ 22100Hz 까지의 주파수를 1024등분하여 각 위치별 크기를 배열로 갖고있다.
	 * @return 수집된 주파수 배열에서 가장 큰 크기를 갖는 index와 크기의 배열\n0: index 1: value
	 */
	private int[] getMaxAmplitudeFrequency(float[] fftResult) {
		int maxIndex = 0;
		float maxValue = 0;

		for (int i = convertFrequencyToIndex(DETECTING_MIN_FREQ); i < fftResult.length; i++) {
			if (maxValue < fftResult[i]) {
				maxValue = fftResult[i];
				maxIndex = i;
			}
		}

		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// 실제로 클리커가 눌려서 발생된 값인지 확인하기 위한 작업 추가-----*********************
		// 주변 주파수들의 평균보다 30%이상 높아야 주파수가 입력된 것으로 판단한다.
		// 아닐 경우 -1을 리턴시킨다.

		int nArround = 100; // 평균 값을 계산 할 주변 index 개수
		float sumArround = 0f; // 주변 값들의 합
		float avgArround = 0f; // 주변 값들의 평균
		int tempCount = 0; // 주변 값들을 더한 개수

		// 주변 값들의 합을 구한다.
		for (int i = 0; i < nArround; i++) {
			int tempIndex = maxIndex - nArround / 2 + i;

			if (tempIndex > 0 && tempIndex < fftResult.length) {
				sumArround += fftResult[tempIndex];
				tempCount++;
			}
		}

		// 주변 값들의 평균을 구한다.
		avgArround = sumArround / (float) tempCount;

		// 최대 값이 주변 값보다 30%이상 높은지 확인한다
		int[] result = new int[2];

		if (maxValue > 0.0001 && maxValue / avgArround > 5) {
			// 최대 값이 일정 신호 값 이상이고 주변값보다 현저히 높은 경우
			result[0] = maxIndex;
			result[1] = (int) maxValue;

			if (D) {
				Log.i(TAG, "Max(" + maxValue + ") / Arround(" + avgArround
						+ ") = " + maxValue / avgArround);
			}
		} else {
			result[0] = -1;
			result[1] = -1;

			if (D) {
				Log.d(TAG, "Max(" + maxValue + ") / Arround(" + avgArround
						+ ") = " + maxValue / avgArround);
			}
		}
		// ---------------------------------------------------*********************
		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

		return result;
	}

	private int convertFrequencyToIndex(int Frequency) {
		return (int) Math.round((512. / 22050. * Frequency - 1));
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void makeLog(String str) {
		if (D) {
			Log.e(TAG, str);
		}
	}

	/**
	 * Handle audio input. This is called on the thread of the parent surface.
	 * 
	 * @param buffer
	 *            Audio data that was just read.
	 */
	private final void processAudio(short[] buffer) {
		// Process the buffer. While reading it, it needs to be locked.
		synchronized (buffer) {
			final int len = buffer.length;
			spectrumAnalyser.setInput(buffer, len - inputBlockSize,
					inputBlockSize);

			currentPower = 100 + SignalPower.calculatePowerDb(buffer, 0, len);

			mMsg = new Message();
			mMsg.what = WHAT_CURRENTPOWER;
			mMsg.obj = currentPower;
			mHandler.sendMessage(mMsg);
			// Tell the reader we're done with the buffer.
			buffer.notify();
		}

		// If we have a spectrum or sonagram analyser, perform the FFT.
		// if (spectrumGauge != null || sonagramGauge != null) {
		// Do the (expensive) transformation.
		// The transformer has its own state, no need to lock here.
		// long specStart = System.currentTimeMillis();
		spectrumAnalyser.transform();
		// long specEnd = System.currentTimeMillis();
		// parentSurface.statsTime(0, (specEnd - specStart) * 1000);

		// Get the FFT output.
		if (historyLen <= 1)
			spectrumAnalyser.getResults(spectrumData);
		else
			spectrumIndex = spectrumAnalyser.getResults(spectrumData,
					spectrumHist, spectrumIndex);

		mMsg = new Message();
		mMsg.what = WHAT_SPECTRUMDATA;
		mMsg.obj = spectrumData;
		mHandler.sendMessage(mMsg);

		if (D) {
			long time_newCalculated = new Date().getTime();
			Log.i(TAG, "time calculating: "
					+ (time_newCalculated - time_lastCalculated) + "ms");

			time_lastCalculated = time_newCalculated;
		}
	}

	boolean isThreadCheckCliqState_running = false;

	Thread mThreadCheckCliqState = new Thread(new Runnable() {
		boolean isCliqON = false;

		public void run() {
			while (isThreadCheckCliqState_running == true) {

				if (isCliqRunning == true && isCliqON == false) {
					startRecording();
					isCliqON = true;
				} else if (isCliqRunning == false && isCliqON == true) {
					stopRecording();
					isCliqON = false;
				}

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	});
}
