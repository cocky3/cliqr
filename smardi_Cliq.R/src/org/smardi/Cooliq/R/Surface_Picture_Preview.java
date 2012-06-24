package org.smardi.Cooliq.R;

import java.io.*;
import java.util.*;

import android.content.*;
import android.hardware.*;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.*;
import android.view.*;

public class Surface_Picture_Preview extends SurfaceView implements
		SurfaceHolder.Callback {
	private boolean D = false;

	public SurfaceHolder mHolder;
	Camera mCamera = null;
	Context mContext = null;
	public CameraParameters mCameraParameters;
	public boolean isSetCameraParameters = false;

	public boolean isLoadCameraparameterSuccese = false; // surfacechanged에서 제대로
															// 불러왔는지

	// Camera preference
	Manage_Camera_SharedPreference mCameraPref;

	public final static int CAMERA_BACK = 0;
	public final static int CAMERA_FACE = 1;
	private int whichCamera = 0;

	public String ACTION_SURFACE_CHANGED = "org.smardi.Cliq.r.surfacechange";

	Camera.Parameters params = null;
	Camera.Parameters temp_params = null;
	
	public int size_Picture_width = 0;
	public int size_Picture_height = 0;
	public Camera.Parameters size_Params = null;
	
	static int lastCamera = -1;	//이전에 설정되있던 카메라 종류를 알아내기 위함
	

	public Surface_Picture_Preview(Context context, AttributeSet attr) {
		super(context, attr);
		mContext = context;
		init();
	}

	private void init() {
		// SurfaceHolder.Callback을 설정함으로써 Surface가 생성/소멸되었음을
		// 알 수 있습니다.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mCameraParameters = CameraParameters.getInstance(mContext);

		// Camera preference
		mCameraPref = new Manage_Camera_SharedPreference(mContext);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		openCamera_BackOrFront(mCameraPref.getWhichCamera());
	}

	public void resumePreview() {
		mCamera.startPreview();
		setCameraParameters(mCamera.getParameters());
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		try {
		// 표면의 크기가 결정될 때 최적의 미리보기 크기를 구해 설정한다.
		if (params == null && temp_params == null) {
			params = mCamera.getParameters();
			temp_params = mCamera.getParameters();
		} else if (params == null) {
			params = temp_params;
		}

		List<Size> arSize = null;
		if(size_Params != null) {
			arSize = size_Params.getSupportedPreviewSizes();
		} else {
			arSize = params.getSupportedPreviewSizes();
		}
		/*if (false) {
			for (Size size : arSize) {
				Log.i("smardi.Cliq", "preview:" + size.width + " x "
						+ size.height);
			}
		}*/

		if (arSize == null) {
			params.setPreviewSize(width, height);
		} else {
			
			
			int diff = 10000;
			Size opti = null;
			for (Size s : arSize) {
				if (Math.abs(s.height - height) < diff) {
					diff = Math.abs(s.height - height);
					opti = s;
				}
			}
			
			params.setPictureSize(size_Picture_width, size_Picture_height);
			
			double ratioDiff = Double.MAX_VALUE;
			double ratioPicture = (double)params.getPictureSize().width / (double)params.getPictureSize().height;
			
			if(D) {
				Log.e("CLIQ", "PictureSize:"+params.getPictureSize().width+" x "+params.getPictureSize().height);
				Log.e("CLIQ", "ratioPicture:"+ratioPicture);
			}
			
			for (Size s : arSize) {
				double ratioPreview = (double)s.width / (double)s.height;
				
				if (Math.abs(ratioPicture - ratioPreview) < ratioDiff) {
					ratioDiff = Math.abs(ratioPicture - ratioPreview);
					opti = s;
				}
			}

			// getOptimalPreviewSize 함수를 이용해서 최적 사이즈를 구함
			// opti = getOptimalPreviewSize(arSize, width, height);

			params.setPreviewSize(opti.width, opti.height);

			// -----------------------------------------------------------------
			// params.setPreviewSize(width, height);

			// -----------------------------------------------------------------

			if (D) {
				Log.e("CLIQ", "in Surface opti.w:" + opti.width + " opti.h:"
						+ opti.height);
			}
		}
		mCamera.setParameters(params);
		mCamera.startPreview();

		setCameraParameters(params);
		isSetCameraParameters = true;

		isLoadCameraparameterSuccese = true;

		// 화면이 갱신된 것을 방송으로 전달
		mContext.sendBroadcast(new Intent().setAction(ACTION_SURFACE_CHANGED));
		} catch (Exception e) {
			isLoadCameraparameterSuccese = false;
			
			Log.e("Cliq.r", "Error in surfaceChanged:"+e.getLocalizedMessage());
			Log.v("Cliq.r", "Error in surfaceChanged:"+e.getLocalizedMessage());
			Log.i("Cliq.r", "Error in surfaceChanged:"+e.getLocalizedMessage());
		}
	}

	@SuppressWarnings("unused")
	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;
		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		int targetHeight = h;
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public Camera openCamera_BackOrFront(int cameraType) {
		whichCamera = cameraType;
		int cameraCount = 0;
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		// Log.e("Cliq.R", "cameraCount:"+cameraCount);
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);

			switch (cameraType) {
			case CAMERA_BACK:
				if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
					lastCamera = cameraInfo.facing;
					try {
						if (mCamera != null) {
							mCamera.stopPreview();
							mCamera.release();
							mCamera = null;
						}

						mCamera = Camera.open(camIdx);
						try {
							mCamera.setPreviewDisplay(mHolder);
						} catch (IOException exception) {
							mCamera.release();
							mCamera = null;
						}
						resumePreview();
					} catch (RuntimeException e) {
						Log.e("Cliq.R",
								"Camera failed to open back: "
										+ e.getLocalizedMessage());
					}
				}
				break;
			case CAMERA_FACE:
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					lastCamera = cameraInfo.facing;
					try {
						if (mCamera != null) {
							mCamera.stopPreview();
							mCamera.release();
							mCamera = null;
						}

						mCamera = Camera.open(camIdx);
						try {
							mCamera.setPreviewDisplay(mHolder);
						} catch (IOException exception) {
							mCamera.release();
							mCamera = null;
						}
						resumePreview();
					} catch (RuntimeException e) {
						Log.e("Cliq.R",
								"Camera failed to open front: "
										+ e.getLocalizedMessage());
					}
				}
				break;
			}

		}

		return mCamera;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// 다른 화면으로 돌아가면, Surface가 소멸됩니다. 따라서 카메라의 Preview도
		// 중지해야 합니다. 카메라는 공유할 수 있는 자원이 아니기에, 사용하지 않을
		// 경우 -액티비티가 일시정지 상태가 된 경우 등 - 자원을 반환해야합니다.
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			mCameraPref.setWhichCamera(whichCamera);
		}
	}

	// 카메라 설정들을 불러온다.
	private void setCameraParameters(Parameters params) {
		try {
			mCameraParameters.setCameraType(whichCamera);
		}catch (Exception e) {
			Log.e("smardi.Cliq",
					"ERROR in setCameraParameters (CameraType):" + e.getLocalizedMessage());
		}
		
		try {
			mCameraParameters.setColorEffect(params.getSupportedColorEffects());
		}catch (Exception e) {
			Log.e("smardi.Cliq",
					"ERROR in setCameraParameters (ColorEffects):" + e.getLocalizedMessage());
		}
			
		try {
			mCameraParameters.setSceneMode(params.getSupportedSceneModes());
		}catch (Exception e) {
			Log.e("smardi.Cliq",
					"ERROR in setCameraParameters (SceneModes):" + e.getLocalizedMessage());
		}
		
		try {
			mCameraParameters
					.setWhiteBalance(params.getSupportedWhiteBalance());
		} catch (Exception e) {
			Log.e("smardi.Cliq",
					"ERROR in setCameraParameters (WhiteBalance):" + e.getLocalizedMessage());
		}
			
		try {
			mCameraParameters.setFlashMode(params.getSupportedFlashModes());
		}catch (Exception e) {
			Log.e("smardi.Cliq",
					"ERROR in setCameraParameters (FlashModes):" + e.getLocalizedMessage());
		}
		
		try {
			mCameraParameters.setFocusMode(params.getSupportedFocusModes());
		}catch (Exception e) {
			Log.e("smardi.Cliq",
					"ERROR in setCameraParameters (FocusModes):" + e.getLocalizedMessage());
		}
			
		try {
			mCameraParameters
					.setPictureSizes(params.getSupportedPictureSizes());
		}catch (Exception e) {
			Log.e("smardi.Cliq",
					"ERROR in setCameraParameters (PictureSizes):" + e.getLocalizedMessage());
		}
		// mCameraParameters.setJpegThumbnailSizes(params.getSupportedJpegThumbnailSizes());
		// mCameraParameters.setFileFormat(params.getSupportedPictureFormats());
		// mCameraParameters.setAntiBanding(params.getSupportedAntibanding());
	}

}
