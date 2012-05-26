package org.smardi.Cooliq.R;

import java.io.*;
import java.util.*;

import android.content.*;
import android.hardware.*;
import android.hardware.Camera.*;
import android.os.*;
import android.util.*;
import android.view.*;

public class Surface_Picture_Preview extends SurfaceView implements
		SurfaceHolder.Callback {
	private boolean D = false;

	SurfaceHolder mHolder;
	Camera mCamera = null;
	Context mContext = null;
	public CameraParameters mCameraParameters;
	public boolean isSetCameraParameters = false;
	
	public boolean isLoadCameraparameterSuccese = false;	//surfacechanged에서 제대로 불러왔는지

	// Camera preference
	Manage_Camera_SharedPreference mCameraPref;

	public final static int CAMERA_BACK = 0;
	public final static int CAMERA_FACE = 1;
	private int whichCamera = 0;

	public String ACTION_SURFACE_CHANGED = "org.smardi.Cliq.r.surfacechange";

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
		// Surface가 생성되었다면, 카메라의 인스턴스를 받아온 후 카메라의
		// Preview 를 표시할 위치를 설정합니다.
		/*
		 * mCamera = Camera.open(mCameraPref.getWhichCamera()); //mCamera =
		 * openFrontFacingCameraGingerbread(CAMERA_BACK); try {
		 * mCamera.setPreviewDisplay(holder); } catch (IOException exception) {
		 * mCamera.release(); mCamera = null; // TODO: add more 3exception
		 * handling logic here }
		 */

		openCamera_BackOrFront(mCameraPref.getWhichCamera());
	}

	public void resumePreview() {
		mCamera.startPreview();
		Log.e("smardi.Cliq", "params:" + mCamera.getParameters());
		setCameraParameters(mCamera.getParameters());
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		try {
			// 표면의 크기가 결정될 때 최적의 미리보기 크기를 구해 설정한다.
			Camera.Parameters params = mCamera.getParameters();

			List<Size> arSize = params.getSupportedPreviewSizes();
			if (true) {
				for (Size size : arSize) {
					/*Log.i("smardi.Cliq", "preview:" + size.width + " x "
							+ size.height);*/
				}
			}

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

				// getOptimalPreviewSize 함수를 이용해서 최적 사이즈를 구함
				// opti = getOptimalPreviewSize(arSize, width, height);

				params.setPreviewSize(opti.width, opti.height);

				// -----------------------------------------------------------------
				// params.setPreviewSize(width, height);

				// -----------------------------------------------------------------

				if (D) {
					Log.e("smardi.Cliq", "opti.w:" + opti.width + " opti.h:"
							+ opti.height);
				}
			}
			mCamera.setParameters(params);
			mCamera.startPreview();

			setCameraParameters(params);
			isSetCameraParameters = true;

			isLoadCameraparameterSuccese = true;
			
			// 화면이 갱신된 것을 방송으로 전달
			mContext.sendBroadcast(new Intent()
					.setAction(ACTION_SURFACE_CHANGED));
		} catch (Exception e) {
			Log.e("Cliq", "Error in SurfaceChange");
			isLoadCameraparameterSuccese = false;
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
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		// Log.e("Cliq.R", "cameraCount:"+cameraCount);
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);

			switch (cameraType) {
			case CAMERA_BACK:
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
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
							// TODO: add more exception handling logic here
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
							// TODO: add more exception handling logic here
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
			// mCameraParameters.setAntiBanding(params.getSupportedAntibanding());
			if (Build.MODEL.equals("SHW-M250S") == false) {
				mCameraParameters.setColorEffect(params
						.getSupportedColorEffects());
				mCameraParameters.setSceneMode(params.getSupportedSceneModes());
			}
			// mCameraParameters.setFileFormat(params.getSupportedPictureFormats());
			if (whichCamera == CAMERA_BACK) {
				mCameraParameters.setFlashMode(params.getSupportedFlashModes());
				mCameraParameters.setColorEffect(params
						.getSupportedColorEffects());
			}
			mCameraParameters.setFocusMode(params.getSupportedFocusModes());
			// mCameraParameters.setJpegThumbnailSizes(params.getSupportedJpegThumbnailSizes());
			mCameraParameters
					.setPictureSizes(params.getSupportedPictureSizes());
			mCameraParameters
					.setWhiteBalance(params.getSupportedWhiteBalance());
		} catch (Exception e) {
			Log.e("smardi.Cliq",
					"ERROR in setCameraParameters:" + e.getLocalizedMessage());
		}
	}

}
