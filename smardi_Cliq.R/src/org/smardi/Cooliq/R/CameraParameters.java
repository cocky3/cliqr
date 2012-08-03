package org.smardi.Cooliq.R;

import java.util.*;

import android.content.*;
import android.hardware.Camera.Size;
import android.util.*;

public class CameraParameters {
	private List<String> colorEffect = null;
	private List<String> flashMode = null;
	private List<String> focusMode = null;
	private List<String> sceneMode = null;
	private List<String> whiteBalance = null;
	private List<Size> jpegThumbnailSizes = null;
	private List<Size> pictureSizes_back = null;
	private List<Size> pictureSizes_front= null;
	private List<String> antiBanding = null;
	private List<Integer> fileFormat = null;
	private int timerTime = 0;
	private int whichCamera = 0;
	
	Manage_Camera_SharedPreference mCameraPref;

	private final boolean D = false;
	private final String TAG = "CLIQ.r::CameraParameters";
	
	private static CameraParameters instance = null;

	public static CameraParameters getInstance(Context context) {
		if(instance == null) {
			instance = new CameraParameters(context);
		} 
		
		return instance;
	}
	
	private CameraParameters(Context context) {
		colorEffect = null;
		flashMode = null;
		focusMode = null;
		sceneMode = null;
		whiteBalance = null;
		jpegThumbnailSizes = null;
		pictureSizes_back = null;
		pictureSizes_front = null;
		antiBanding = null;
		fileFormat = null;
		
		mCameraPref = new Manage_Camera_SharedPreference(context);
	}

	public void setFlashMode(List<String> flashMode) {
		this.flashMode = flashMode;

		if (D) {
			for (String str : flashMode) {
				Log.i(TAG, "flashMode:" + str);
			}
		}
	}

	public List<String> getFlashMode() {
		return flashMode;
	}
	
	

	public void setFocusMode(List<String> focusMode) {
		this.focusMode = focusMode;
		if (D)
			for (String str : focusMode) {
				Log.i(TAG, "focusMode:" + str);
			}
	}

	public List<String> getFocusMode() {
		return focusMode;
	}

	public void setSceneMode(List<String> sceneMode) {
		this.sceneMode = sceneMode;
		mCameraPref.setSceneModeList(sceneMode);
		if (D)
			for (String str : sceneMode) {
				Log.i(TAG, "sceneMode:" + str);
			}
	}

	public List<String> getSceneMode() {
		return sceneMode;
	}

	public void setWhiteBalance(List<String> whiteBalance) {
		this.whiteBalance = whiteBalance;
		mCameraPref.setWhiteBalanceList(whiteBalance);
		if (D)
			for (String str : whiteBalance) {
				Log.i(TAG, "whiteBalance:" + str);
			}
	}

	public List<String> getWhiteBalance() {
		return whiteBalance;
	}

	public void setJpegThumbnailSizes(List<Size> jpegThumbnailSizes) {
		this.jpegThumbnailSizes = jpegThumbnailSizes;
		if (D)
			for (Size str : jpegThumbnailSizes) {
				Log.i(TAG, "jpegThumbnailWidth:" + str.width
						+ "jpegThumbnailHeight" + str.height);
			}
	}

	public List<Size> getJpegThumbnailSizes() {
		return jpegThumbnailSizes;
	}

	public void setPictureBackSizes(List<Size> pictureSizes) {
		
		mCameraPref.setPictureSizeBackList(pictureSizes);
		this.pictureSizes_back = pictureSizes;
		
		if (D)
			for (Size str : pictureSizes) {
				Log.i(TAG, whichCamera + " pictureSizes Width:" + str.width
						+ "pictureSizesHeight" + str.height);
			}
	}
	
	public void setPictureFrontSizes(List<Size> pictureSizes) {
		
		mCameraPref.setPictureSizeFrontList(pictureSizes);
		this.pictureSizes_front = pictureSizes;
		
		if (true)
			for (Size str : pictureSizes) {
				Log.i(TAG, whichCamera + " pictureSizes Width:" + str.width
						+ "pictureSizesHeight" + str.height);
			}
	}
	
	/*public void setPictureSizes(List<Size> pictureSizes) {
		
		if(whichCamera == Surface_Picture_Preview.CAMERA_BACK) {
			mCameraPref.setPictureSizeBackList(pictureSizes);
			this.pictureSizes_back = pictureSizes;
		} else {
			mCameraPref.setPictureSizeFrontList(pictureSizes);
			this.pictureSizes_front = pictureSizes;
		}
		
		if (D)
			for (Size str : pictureSizes) {
				Log.i(TAG, whichCamera + " pictureSizes Width:" + str.width
						+ "pictureSizesHeight" + str.height);
			}
	}*/

	public List<Size> getPictureBackSizes() {
		return pictureSizes_back;
	}
	
	public List<Size> getPictureFrontSizes() {
		return pictureSizes_front;
	}
	/*
	public List<Size> getPictureSizes() {
		if(whichCamera == Surface_Picture_Preview.CAMERA_BACK) {
			return pictureSizes_back;
		} else {
			return pictureSizes_front;
		}
		
	}*/

	public void setFileFormat(List<Integer> fileFormat) {
		this.fileFormat = fileFormat;
		if (D)
			for (int str : fileFormat) {
				Log.i(TAG, "fileFormat:" + str);
			}
	}

	public List<Integer> getFileFormat() {
		return fileFormat;
	}

	public void setAntiBanding(List<String> antiBanding) {
		this.antiBanding = antiBanding;
		if (D)
			for (String str : antiBanding) {
				Log.i(TAG, "antiBanding:" + str);
			}
	}

	public List<String> getAntiBanding() {
		return antiBanding;
	}

	public void setColorEffect(List<String> colorEffect) {
		this.colorEffect = colorEffect;
		mCameraPref.setColorEffectList(colorEffect);
		if (D)
			for (String str : colorEffect) {
				Log.i(TAG, "colorEffect:" + str);
			}
	}

	public List<String> getColorEffect() {
		return colorEffect;
	}

	public void setTimerTime(int timerTime) {
		this.timerTime = timerTime;
	}

	public int getTimerTime() {
		return timerTime;
	}

	public void setCameraType(int whichCamera) {
		this.whichCamera = whichCamera;
	}
	
	public int getCameraType () {
		return whichCamera;
	}
}
