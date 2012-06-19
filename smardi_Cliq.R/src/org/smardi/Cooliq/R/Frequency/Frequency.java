package org.smardi.Cooliq.R.Frequency;

import java.util.*;

import org.hermit.audalyzer.*;
import org.smardi.CliqService.*;

import android.content.*;
import android.os.*;
import android.util.*;

public class Frequency {

	private final boolean D = false;

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
	// private float[] biasRange = null;

	// Sequence number of the last block we processed.
	// private long audioProcessed = 0;

	// Buffered audio data, and sequence number of the latest block.
	// private short[] audioData;
	// private long audioSequence = 0;

	// If we got a read error, the error code.
	// private int readError = AudioReader.Listener.ERR_OK;

	// Current signal power level, in dB relative to max. input power.
	private double currentPower = 0f;

	// Message
	Message mMsg = new Message();

	// ==============================================================
	public static final int WHAT_SPECTRUMDATA = 0;
	public static final int WHAT_CURRENTPOWER = 1;
	public static final int WHAT_CLIQ_DETECTED = 2;
	public static final int WHAT_CLIQ_NOT_DETECTED = 3;
	// ==============================================================
	public static final String ACTION_CLIQ_REGIST_HIGHEST_FREQUENCY = "org.smardi.CLIQ.r.Frequency.regist_highest_frequency";
	public static final String ACTION_CLIQ_DETECTED = "org.smardi.CLIQ.r.Frequency.Cliq_Detected";
	public static final String ACTION_CLIQ_NOT_DETECTED = "org.smardi.CLIQ.r.Frequency.Cliq_Not_Detected";
	public static final String ACTION_MIC_DO_NOT_WORK = "org.smardi.CLIQ.r.MIC_do_not_work";	//마이크가 작동하지 않을 때
	// ==============================================================
	String TAG = "getPowerfulFrequency";

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

	private boolean isRecordingRunning = false; // 녹음 중인지

	// ------------------------------------------

	private long time_lastCalculated = 0;

	private final int DETECTING_MIN_FREQ = 16000;

	Context mContext = null;

	Manage_CLIQ_SharedPreference mPref = null;

	// Averaging을 위한 변수 선언
	private ArrayList<float[]> list_soundData = new ArrayList<float[]>();
	private final int MAX_AVERAGING = 10; // 최대로 중첩시키는 개수

	
	//주파수 등록 중인지 확인하기 위한 변수
	public boolean isCliqrRegistation = false;
	//테스트 중인지 확인하기 위한 변수
	public boolean isCliqrTesting = false;
	
	//마지막으로 클리커 신호가 끊긴 시간 저장
	private long timeCliqrStoped = 0;
	
	
	//Singleton 패턴 적용
	private static Frequency instance = null;
	
	public static Frequency getInstance(Context context) {
		if(instance == null) {
			instance = new Frequency(context);
		}
		return instance;
	}
	
	public Frequency(Context context) {
		mContext = context;
		mPref = new Manage_CLIQ_SharedPreference(mContext);

		initAudio();
	}

	public void setStartFreq(int freq) {
		// TODO
	}

	private void initAudio() {
		audioReader = new AudioReader();

		spectrumAnalyser = new FFTTransformer(inputBlockSize, windowFunction);

		// Allocate the spectrum data.
		spectrumData = new float[inputBlockSize / 2];
		spectrumHist = new float[inputBlockSize / 2][historyLen];
		spectrumIndex = 0;

		// biasRange = new float[2];

		/**
		 * We are starting the main run; start measurements.
		 */
		// audioProcessed = audioSequence = 0;
	}

	public void startRecording() {
		if (isRecordingRunning == false) {

			audioReader.startReader(sampleRate,
					inputBlockSize * sampleDecimate,
					new AudioReader.Listener() {
						@Override
						public final void onReadComplete(short[] buffer) {
							// receiveAudio(buffer);
							processAudio(buffer);
						}

						@Override
						public void onReadError(int error) {
							// handleError(error);
						}
					});
			isRecordingRunning = true;
		}
	}

	public void stopRecording() {
		if (isRecordingRunning == true) {
			isRecordingRunning = false;
			audioReader.stopReader();
		}
	}

	/*
	 * Handler mHandler = new Handler() {
	 * 
	 * @Override public void handleMessage(Message msg) {
	 * super.handleMessage(msg);
	 * 
	 * switch (msg.what) { case WHAT_CURRENTPOWER: double power = (Double)
	 * msg.obj; break; case WHAT_SPECTRUMDATA: float[] sData = (float[])
	 * msg.obj; mMaxData = getMaxAmplitudeFrequency(sData);
	 * 
	 * //makeLog("isRegistrating:"+isRegistrating +
	 * "isGreenButtonON:"+isGreenButtonON); if(isRegistrating == true &&
	 * isGreenButtonON == true) {
	 * 
	 * if(mMaxData[0] > 0) { list_maxIndexGreen.add(mMaxData[0]);
	 * list_maxValueGreen.add(mMaxData[1]); list_spectrumDataGreen.add(sData); }
	 * } else if(isRegistrating == true && isGreenButtonON == false) {
	 * list_maxIndexGray.add(mMaxData[0]); list_maxValueGray.add(mMaxData[1]);
	 * list_spectrumDataGray.add(sData); }
	 * 
	 * break; } } };
	 */

	/**
	 * 수집된 주파수 배열 중에서 가장 큰 값을 갖는 주파수 값과 그 때의 크기를 리턴한다.
	 * 
	 * @param fftResult
	 *            수집된 주파수 배열 0Hz ~ 22100Hz 까지의 주파수를 1024등분하여 각 위치별 크기를 배열로 갖고있다.
	 * @return 수집된 주파수 배열에서 가장 큰 크기를 갖는 index와 크기의 배열\n0: index 1: value
	 */
	public int[] getMaxAmplitudeFrequency(float[] fftResult) {
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

		if (maxValue > 0.0001 && maxValue / avgArround > 1.2) {
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

	/**
	 * 수집된 주파수 배열 중에서 가장 큰 값을 갖는 주파수 값과 그 때의 크기를 리턴한다.
	 * 
	 * @param fftResult
	 *            수집된 주파수 배열 0Hz ~ 22100Hz 까지의 주파수를 1024등분하여 각 위치별 크기를 배열로 갖고있다.
	 * @return 수집된 주파수 배열에서 가장 큰 크기를 갖는 index와 크기의 배열\n0: index 1: value
	 */
	public int[] getMaxAmplitudeFrequency(float[] fftResult, int minFreq,
			int maxFreq) {
		int maxIndex = 0;
		float maxValue = 0;

		for (int i = convertFrequencyToIndex(minFreq); i < fftResult.length
				&& i < convertFrequencyToIndex(maxFreq); i++) {
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
				// 최대값을 낸 주파수에 인접한 부분은 제외한다
				if (tempIndex < maxIndex - 10 || maxIndex + 10 < tempIndex) {
					sumArround += fftResult[tempIndex];
					tempCount++;
				}
			}
		}

		// 주변 값들의 평균을 구한다.
		avgArround = sumArround / (float) tempCount;

		// 최대 값이 주변 값보다 30%이상 높은지 확인한다
		int[] result = new int[2];

		if (maxValue > 0.0001 && maxValue / avgArround > 15) {
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

	private float getCliqFrequencyPower() {
		int LOW_FREQ = mPref.getCliqFrequency() - 150;
		int HIGH_FREQ = mPref.getCliqFrequency() + 150;

		if (HIGH_FREQ > 22050) {
			HIGH_FREQ = 22050;
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
			if (max < getAveragingValue(i)) {
				max = getAveragingValue(i);
				maxINDEX = i;
			}
		}

		mPref.setCliqFrequencyIndex(maxINDEX);

		return max;
	}

	private float getAveragingValue(int i) {

		float sum = 0;

		for (int j = 0; j < list_soundData.size(); j++) {
			sum += list_soundData.get(j)[i];
		}

		return sum / list_soundData.size();
	}

	private float getCliqThreadhold() {

		int Freq = mPref.getCliqFrequency();

		// 클리커 주파수에서 +-100 인 주파수는 제외하고 최대값을 구한다.
		int leftFrequencyIndex = convertFrequencyToIndex(Freq - 150);
		int rightFrequencyIndex = convertFrequencyToIndex(Freq + 150);

		float tempMax = 0;
		// int startFrequencyIndex = convertFrequencyToIndex(Freq - 1500);
		// int endFrequencyIndex = convertFrequencyToIndex(Freq + 1500);
		int startFrequencyIndex = convertFrequencyToIndex(Freq - 600);
		int endFrequencyIndex = convertFrequencyToIndex(Freq + 600);

		int minFrequencyIndex = convertFrequencyToIndex(DETECTING_MIN_FREQ);
		int maxFrequencyIndex = convertFrequencyToIndex(22050);
		if (startFrequencyIndex < minFrequencyIndex) {
			startFrequencyIndex = minFrequencyIndex;
		}
		if (maxFrequencyIndex < endFrequencyIndex) {
			endFrequencyIndex = maxFrequencyIndex;
		}

		// 최대값을 구함.
		for (int i = startFrequencyIndex; i < endFrequencyIndex; i++) {
			if (i < leftFrequencyIndex || rightFrequencyIndex < i) {
				if (tempMax < getAveragingValue(i)) {
					tempMax = getAveragingValue(i);
				}
			}
		}

		return tempMax;
	}

	private int convertFrequencyToIndex(int Frequency) {
		return (int) Math.round((inputBlockSize / 2 / 22050. * Frequency - 1));
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

			if(currentPower == 0) {
				mContext.sendBroadcast(new Intent(ACTION_MIC_DO_NOT_WORK));
			}
			
			mMsg = new Message();
			mMsg.what = WHAT_CURRENTPOWER;
			mMsg.obj = currentPower;
			//mHandler.sendMessage(mMsg);
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

		
		//Averaging을 위해서 변수를 추가
		if(list_soundData.size() < MAX_AVERAGING) {
			list_soundData.add(spectrumData);
		} else {
			list_soundData.remove(0);
			list_soundData.add(spectrumData);
		}
		
/*		mMsg = new Message();
		mMsg.what = WHAT_SPECTRUMDATA;
		mMsg.obj = spectrumData;
		mHandler.sendMessage(mMsg);
*/
		
		
		//클리커 주파수 대역에서 가장 높은 주파수 값을 내보낸다
		if(isCliqrRegistation == true) {
			int[] result = getMaxAmplitudeFrequency(spectrumData, 16000, 21050);
			Intent intentHightestFreq = new Intent().setAction(ACTION_CLIQ_REGIST_HIGHEST_FREQUENCY);
			intentHightestFreq.putExtra("freq", convertIndextToFrequency(result[0]));
			mContext.sendBroadcast(intentHightestFreq);
		} else {

			// CLIQ.r 신호가 발생했는지 확인한다
			if (0 < mPref.getCliqFrequency()) {
				if (check_CLIQ_Pressed() == true) {
					mContext.sendBroadcast(new Intent()
							.setAction(ACTION_CLIQ_DETECTED));
					timeCliqrStoped = 0;
				} else {
					
					long now = new Date().getTime();
					if(timeCliqrStoped == 0) {
						timeCliqrStoped = now;
					}
					
					if(now - timeCliqrStoped < 200) {
						
					} else {
						mContext.sendBroadcast(new Intent()
								.setAction(ACTION_CLIQ_NOT_DETECTED));
					}
				}
			}
		}
		
		
		long time_newCalculated = new Date().getTime();
		if (D) {
			Log.i(TAG, "time calculating: "
					+ (time_newCalculated - time_lastCalculated) + "ms");
		}
		time_lastCalculated = time_newCalculated;
		
	}

	
	int countPressed = 0;
	final int MAX_COUNT_PRESS = 3;
	
	private boolean check_CLIQ_Pressed() {
		float powerCliqr = (float) (getCliqFrequencyPower() - 3E-5);
		float powerThred = (float) (getCliqThreadhold() - (3E-5));
		
		float multiple = 0;
		if(0 < powerThred) {
			multiple = powerCliqr / powerThred;
			
			if(D) {
				Log.e(TAG, "C:"+powerCliqr + "\t\t  T:"+ powerThred + "\t\t   multiple:"+multiple);
			}
			
			if(multiple > 10) {
				countPressed += 1;
			} else {
				countPressed = 0;
				return false;
			}
		} else {
			
			if(D) {
				Log.e(TAG, "C:"+powerCliqr + "\t\t  T:"+ powerThred+ "  D:"+(powerCliqr - powerThred));
			}
			
			if (3E-5 < powerCliqr) {
				countPressed += 1;
			} else {
				countPressed = 0;
				return false;
			}
		}
		
		
		if(MAX_COUNT_PRESS <= countPressed) {
			return true;
		} else {
			return false;
		}
	}

	public int convertIndextToFrequency(Integer frequencyIndex) {
		int result = (int) Math.round((double) (22050. / (inputBlockSize / 2))
				* (double) (frequencyIndex + 1)) - 20;

		if (result < 0) {
			return 0;
		} else {
			return result;
		}
	}
	
}
