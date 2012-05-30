package org.smardi.Cooliq.R;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.widget.*;

public class AC_Loading extends Activity {
    
	private final int TIME_LOADING = 3000;
	private final int TIME_ICON_CHANGE_INTERVAL = 500;
	
	private ImageView loadingIconLayout = null;
	
	Manage_Camera_SharedPreference mPref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_loading);
		
		getWindow().setFormat(PixelFormat.RGBA_8888);
		
		mPref = new Manage_Camera_SharedPreference(AC_Loading.this);
		
		loadingIconLayout = (ImageView)findViewById(R.id.loadingIcon);
		
		cTimer.start();
	}
	
	CountDownTimer cTimer = new CountDownTimer(TIME_ICON_CHANGE_INTERVAL * 8, TIME_ICON_CHANGE_INTERVAL) {
		
		int iconCount = 0;
		Drawable drawable = null;
		@Override
		public void onTick(long millisUntilFinished) {
			switch(iconCount) {
			case 0:
				drawable = getResources().getDrawable(R.drawable.loading);
				break;
			case 1:
				drawable = getResources().getDrawable(R.drawable.loading_1);
				break;
			case 2:
				drawable = getResources().getDrawable(R.drawable.loading_2);
				break;
			case 3:
				drawable = getResources().getDrawable(R.drawable.loading_3);
				break;
			case 4:
				drawable = getResources().getDrawable(R.drawable.loading_4);
				break;
			case 5:
				drawable = getResources().getDrawable(R.drawable.loading_5);
				break;
			case 6:
				drawable = getResources().getDrawable(R.drawable.loading_6);
				break;	
			}
			iconCount++;
			
			loadingIconLayout.setImageDrawable(drawable);
		}
		
		@Override
		public void onFinish() {
			Intent intent;
			
			if(mPref.getCheckedTutorial() == true) {
				//튜토리얼을 했을 경우
				intent = new Intent(AC_Loading.this, AC_Main.class);
			} else {
				//튜토리얼을 안했을 경우
				intent = new Intent(AC_Loading.this, AC_Help_tutorial.class);
			}
			intent.putExtra("tutorial", true);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			overridePendingTransition(R.anim.fadein, R.anim.fadeout);
			cTimer.cancel();
			finish();
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		//종료시 타이머 멈춤
		cTimer.cancel();
	}
	
	
}