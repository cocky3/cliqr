package org.smardi.Cooliq.R;

import java.util.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

public class AC_Help_tutorial extends Activity {

	private ViewFlipper m_viewFlipper;
	private Animation appear_right;
	private Animation appear_left;
	private Animation disappear_right;
	private Animation disappear_left;

	private int m_nPreTouchPosX = 0;
	private int pageCount = 0;

	private ArrayList<Integer> listTuto;
	private ArrayList<ImageView> listView;

	private Manage_Camera_SharedPreference mPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_help_tutorial);

		appear_right = AnimationUtils.loadAnimation(this,
				R.anim.appear_from_right);
		appear_left = AnimationUtils.loadAnimation(this,
				R.anim.appear_from_left);
		disappear_right = AnimationUtils.loadAnimation(this,
				R.anim.disappear_to_right);
		disappear_left = AnimationUtils.loadAnimation(this,
				R.anim.disappear_to_left);

		m_viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		m_viewFlipper.setOnTouchListener(onTouchListener);

		mPref = new Manage_Camera_SharedPreference(AC_Help_tutorial.this);

		setListTutorial();
		setListView();
	}

	private void setListTutorial() {
		listTuto = new ArrayList<Integer>();

		if (getString(R.string.language).equals("ko") == true) {
			listTuto.add(R.drawable.step1);
			listTuto.add(R.drawable.step2);
			listTuto.add(R.drawable.step3);
			listTuto.add(R.drawable.step4);
		} else {
			listTuto.add(R.drawable.step1_en);
			listTuto.add(R.drawable.step2_en);
			listTuto.add(R.drawable.step3_en);
			listTuto.add(R.drawable.step4_en);
		}
		((ImageView) (m_viewFlipper.getChildAt(0)))
				.setImageDrawable(getResources().getDrawable(listTuto.get(0)));
	}

	private void setListView() {

		listView = new ArrayList<ImageView>();

		for (int i = 0; i < m_viewFlipper.getChildCount(); i++) {
			listView.add((ImageView) m_viewFlipper.getChildAt(i));
		}
		/*
		 * listView.add((ImageView) m_viewFlipper.findViewById(R.id.view1));
		 * listView.add((ImageView) m_viewFlipper.findViewById(R.id.view2));
		 * listView.add((ImageView) m_viewFlipper.findViewById(R.id.view3));
		 * listView.add((ImageView) m_viewFlipper.findViewById(R.id.view4));
		 * listView.add((ImageView) m_viewFlipper.findViewById(R.id.view5));
		 */
	}

	private void loadImage(int idx) {
		listView.get(idx).setImageDrawable(
				getResources().getDrawable(listTuto.get(idx)));
	}

	private void removeImage(int idx) {
		listView.get(idx).setImageDrawable(null);
	}

	private void MoveNextView() {
		loadImage(pageCount + 1);
		if (pageCount > 0) {
			removeImage(pageCount - 1);
		}

		m_viewFlipper.setInAnimation(appear_right);
		m_viewFlipper.setOutAnimation(disappear_left);
		m_viewFlipper.showNext();
		pageCount++;
	}

	private void MovewPreviousView() {
		loadImage(pageCount - 1);
		if (pageCount < m_viewFlipper.getChildCount() - 1) {
			removeImage(pageCount + 1);
		}

		m_viewFlipper.setInAnimation(appear_left);
		m_viewFlipper.setOutAnimation(disappear_right);
		m_viewFlipper.showPrevious();
		pageCount--;
	}

	View.OnTouchListener onTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				m_nPreTouchPosX = (int) event.getX();
			}

			if (event.getAction() == MotionEvent.ACTION_UP) {
				int nTouchPosX = (int) event.getX();

				if (nTouchPosX < m_nPreTouchPosX) {
					if (pageCount < m_viewFlipper.getChildCount() - 1) {
						MoveNextView();
					} else {
						// 마지막 페이지에서 왼쪽으로 이동을 했음
						// 끝낼건지 물어봄
						AlertDialog.Builder alert = new AlertDialog.Builder(
								AC_Help_tutorial.this);
						alert.setTitle(getString(R.string.dialog_tutorial_title));
						alert.setMessage(getString(R.string.dialog_tutorial_message));
						alert.setPositiveButton(getString(R.string.dialog_OK),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										finish();

										if (mPref.getCheckedTutorial() == false) {
											Intent intent = new Intent(
													AC_Help_tutorial.this,
													AC_Main.class);
											intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
											startActivity(intent);
											mPref.setCheckedTutorial(true);
										}

									}
								});
						alert.setNegativeButton(
								getString(R.string.dialog_Cancel),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});
						alert.show();
					}
				} else if (nTouchPosX > m_nPreTouchPosX) {
					if (0 < pageCount) {
						MovewPreviousView();
					}
				}

				m_nPreTouchPosX = nTouchPosX;
			}

			return true;
		}
	};

}
