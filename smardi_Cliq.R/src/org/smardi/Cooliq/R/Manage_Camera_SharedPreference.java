package org.smardi.Cooliq.R;

import java.util.*;

import android.content.*;
import android.hardware.Camera.Size;

public class Manage_Camera_SharedPreference {
	private Context mContext;
	private SharedPreferences mPref;
	private SharedPreferences.Editor mEditor;
	final String preferenceName = "CLIQ.r";
	
		
	public Manage_Camera_SharedPreference(Context context) {
		mContext = context;
		mPref = mContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
		mEditor = mPref.edit();
	}


	public void setColorEffect(String colorEffect) {
		mEditor.putString("colorEffect", colorEffect);
		mEditor.commit();
	}


	public String getColorEffect() {
		return mPref.getString("colorEffect", "none");
	}
	
	/**
	 * 카메라 정보로부터 가져온 설정들을 쉼표(,)로 구분된 String으로 저장한다.
	 * @param colorEffect 카메라에서 가져온 설정 정보
	 */
	public void setColorEffectList(List<String> colorEffect) {
		String value = "";
		
		for (int i=0; i<colorEffect.size(); i++) {
			if(i > 0) {
				value += ("," + colorEffect.get(i));
			} else {
				value += colorEffect.get(i);
			}
		}
		
		mEditor.putString("colorEffectList", value);
		mEditor.commit();
	}
	
	public String[] getColorEffectList() {
		String[] value = null;
		String tempValue = mPref.getString("colorEffectList", "");
		
		value = tempValue.split(",");
				
		return value;
	}
	
	public void setFlashMode(String flashMode) {
		mEditor.putString("flashMode", flashMode);
		mEditor.commit();
	}

	public String getFlashMode() {
		return mPref.getString("flashMode", "auto");
	}


	public void setFocusMode(String focusMode) {
		mEditor.putString("focusMode", focusMode);
		mEditor.commit();
	}


	public String getFocusMode() {
		return mPref.getString("focusMode", "auto");
	}


	public void setSceneMode(String sceneMode) {
		mEditor.putString("sceneMode", sceneMode);
		mEditor.commit();
	}


	public String getSceneMode() {
		return mPref.getString("sceneMode", "auto");
	}
	
	/**
	 * 카메라 정보로부터 가져온 설정들을 쉼표(,)로 구분된 String으로 저장한다.
	 * @param sceneMode 카메라에서 가져온 설정 정보
	 */
	public void setSceneModeList (List<String> sceneMode) {
		String value = "";
		
		for (int i=0; i<sceneMode.size(); i++) {
			if(i > 0) {
				value += ("," + sceneMode.get(i));
			} else {
				value += sceneMode.get(i);
			}
			
		}
		
		mEditor.putString("sceneModeList", value);
		mEditor.commit();
	}
	
	/**
	 * 저장된 카메라의 장면모드를 String[] 형태로 가져온다.
	 * @return String[] 형태의 장면모드
	 */
	public String[] getSceneModeList () {
		String[] value = null;
		String tempValue = mPref.getString("sceneModeList", "");
		
		value = tempValue.split(",");
		
		return value;
	}


	public void setWhiteBalance(String whiteBalance) {
		mEditor.putString("whiteBalance", whiteBalance);
		mEditor.commit();
	}


	public String getWhiteBalance() {
		return mPref.getString("whiteBalance", "auto");
	}
	
	/**
	 * 카메라 정보로부터 가져온 설정들을 쉼표(,)로 구분된 String으로 저장한다.
	 * 
	 * @param whiteBalance 카메라에서 가져온 White Balance 설정 정보
	 */
	public void setWhiteBalanceList(List<String> whiteBalance) {
		String value = "";
		
		for (int i=0; i<whiteBalance.size(); i++) {
			if(i > 0) {
				value += ("," + whiteBalance.get(i));
			} else {
				value += whiteBalance.get(i);
			}
		}
		
		mEditor.putString("whiteBalanceList", value);
		mEditor.commit();
	}
	
	
	public String[] getWhiteBalanceList() {
		String[] value = null;
		String tempValue = mPref.getString("whiteBalanceList", "");
		
		value = tempValue.split(",");
		
		return value;
	}


	public void setJpegThumbnailSizes(Size jpegThumbnailSizes) {
		mEditor.putInt("jpegThumbnailSizes_x", jpegThumbnailSizes.width);
		mEditor.putInt("jpegThumbnailSizes_y", jpegThumbnailSizes.height);
		mEditor.commit();
	}


	public int[] getJpegThumbnailSizes() {
		int width = mPref.getInt("jpegThumbnailSizes_x", 0);
		int height = mPref.getInt("jpegThumbnailSizes_y", 0);
		
		return new int[] {width, height};
	}


	public void setPictureSizes(Size pictureSizes) {
		mEditor.putInt("pictureSizes_x", pictureSizes.width);
		mEditor.putInt("pictureSizes_y", pictureSizes.height);
		mEditor.commit();
	}
	
	public void setPictureSizes(int width, int height) {
		mEditor.putInt("pictureSizes_x", width);
		mEditor.putInt("pictureSizes_y", height);
		mEditor.commit();
	}


	public int[] getPictureSizes() {
		int width = mPref.getInt("pictureSizes_x", 640);
		int height = mPref.getInt("pictureSizes_y", 480);
		
		return new int[]{width, height};
	}
	
	public void setPictureSizes_BACK(Size pictureSizes) {
		mEditor.putInt("pictureSizes_BACK_x", pictureSizes.width);
		mEditor.putInt("pictureSizes_BACK_y", pictureSizes.height);
		mEditor.commit();
	}
	
	public void setPictureSizes_BACK(int width, int height) {
		mEditor.putInt("pictureSizes_BACK_x", width);
		mEditor.putInt("pictureSizes_BACK_y", height);
		mEditor.commit();
	}


	public int[] getPictureSizes_BACK() {
		int width = mPref.getInt("pictureSizes_BACK_x", 640);
		int height = mPref.getInt("pictureSizes_BACK_y", 480);
		
		return new int[]{width, height};
	}
	
	public void setPictureSizes_FRONT(Size pictureSizes) {
		mEditor.putInt("pictureSizes_FRONT_x", pictureSizes.width);
		mEditor.putInt("pictureSizes_FRONT_y", pictureSizes.height);
		mEditor.commit();
	}
	
	public void setPictureSizes_FRONT(int width, int height) {
		mEditor.putInt("pictureSizes_FRONT_x", width);
		mEditor.putInt("pictureSizes_FRONT_y", height);
		mEditor.commit();
	}


	public int[] getPictureSizes_FRONT() {
		int width = mPref.getInt("pictureSizes_FRONT_x", 640);
		int height = mPref.getInt("pictureSizes_FRONT_y", 480);
		
		return new int[]{width, height};
	}
	
	
	/**
	 * 카메라 정보로부터 가져온 설정들을 쉼표(,)로 구분된 String으로 저장한다.
	 * 
	 * @param pictureSize 카메라에서 가져온 White Balance 설정 정보
	 */
	public void setPictureSizeBackList(List<Size> pictureSize) {
		String value = "";
		
		for (int i=0; i<pictureSize.size(); i++) {
			int width = pictureSize.get(i).width;
			int height = pictureSize.get(i).height;
			String pixel = String.format("%.1f", Math.round(width*height/100000f)/10f);
			String currentSize = width+ " x " + height + " ("+pixel+"M)";
			
			String tempSize = currentSize;
			if(i > 0) {
				value += ("," + tempSize);
			} else {
				value += tempSize;
			}
		}
		
		mEditor.putString("pictureSizeBackList", value);
		mEditor.commit();
	}
	
	
	public String[] getPictureSizeBackList() {
		String[] value = null;
		String tempValue = mPref.getString("pictureSizeBackList", "");
		
		value = tempValue.split(",");
		//Log.e("smardi.Cliq", tempValue);
		return value;
	}
	
	/**
	 * 카메라 정보로부터 가져온 설정들을 쉼표(,)로 구분된 String으로 저장한다.
	 * 
	 * @param pictureSize 카메라에서 가져온 White Balance 설정 정보
	 */
	public void setPictureSizeFrontList(List<Size> pictureSize) {
		String value = "";
		
		for (int i=0; i<pictureSize.size(); i++) {
			int width = pictureSize.get(i).width;
			int height = pictureSize.get(i).height;
			String pixel = String.format("%.1f", Math.round(width*height/100000f)/10f);
			String currentSize = width+ " x " + height + " ("+pixel+"M)";
			
			String tempSize = currentSize;
			if(i > 0) {
				value += ("," + tempSize);
			} else {
				value += tempSize;
			}
		}
		
		mEditor.putString("pictureFrontSizeList", value);
		mEditor.commit();
	}
	
	
	public String[] getPictureSizeFrontList() {
		String[] value = null;
		String tempValue = mPref.getString("pictureFrontSizeList", "");
		
		value = tempValue.split(",");
		//Log.e("smardi.Cliq", tempValue);
		return value;
	}


	public void setAntiBanding(String antiBanding) {
		mEditor.putString("antiBanding", antiBanding);
		mEditor.commit();
	}


	public String getAntiBanding() {
		return mPref.getString("antiBanding", "auto");
	}


	public void setFileFormat(int fileFormat) {
		mEditor.putInt("fileFormat", fileFormat);
		mEditor.commit();
	}


	public int getFileFormat() {
		return mPref.getInt("fileFormat", 256);
	}

	public void setWhichCamera(int camera) {
		mEditor.putInt("whichCamera", camera);
		mEditor.commit();
	}
	
	public int getWhichCamera() {
		return mPref.getInt("whichCamera", 0);
	}
	
	/**
	 * 사용자가 적용한 CLIQ.r 모드를 저장한다.
	 * 
	 * @param mode CLIQ.r의 모드
	 * 0:NONE
	 * 1:CLIQ.r
	 * 2:Volume
	 */
	public void setModeCLIQ(int mode) {
		mEditor.putInt("CLIQ.r mode", mode);
	}
	
	/**
	 * 적용되어있는 CLIQ.r 모드를 반환한다.
	 * 
	 * @return 현재 적용된 모드 
	 * 0:NONE
	 * 1:CLIQ.r
	 * 2:Volume
	 */
	public int getModeCLIQ() {
		return mPref.getInt("CLIQ.r mode", 0);
	}
	
	//튜토리얼을 확인했는지 저장
	public void setCheckedTutorial(boolean checked) {
		mEditor.putBoolean("checkedTutorial", checked);
		mEditor.commit();
	}
	
	//튜토리얼을 확인했는지 가져온다.
	public boolean getCheckedTutorial() {
		return mPref.getBoolean("checkedTutorial", false);
	}
}
