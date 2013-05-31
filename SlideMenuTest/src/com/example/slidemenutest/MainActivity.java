package com.example.slidemenutest;

import com.example.slidemenutest.MyLinearLayout.OnScrollListener;

import android.os.Bundle;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements OnTouchListener,
		GestureDetector.OnGestureListener, OnItemClickListener {
	private boolean hasMeasured = false;// 是否Measured.
	private LinearLayout layout_left;// 左边布局
	private LinearLayout layout_right;// 右边布局
	private ImageView iv_set;// 图片
	private ListView lv_set;// 设置菜单

	/** 每次自动展开/收缩的范围 */
	private int MAX_WIDTH = 0;
	/** 每次自动展开/收缩的速度 */
	private final static int SPEED = 30;

	private final static int sleep_time = 5;

	private GestureDetector mGestureDetector;// 手势
	private boolean isScrolling = false;
	private float mScrollX = 0; // 滑块滑动距离
	private int window_width;// 屏幕的宽度

	private String TAG = "CJL";

	private View view = null;// 点击的view

	private String title[] = { "用户", "同步", "标准", "帮助", "关于"};

	private MyLinearLayout mylayout;

	/***
	 * 初始化view
	 */
	void InitView() {
		layout_left = (LinearLayout) findViewById(R.id.layout_left);
		layout_right = (LinearLayout) findViewById(R.id.layout_right);
		iv_set = (ImageView) findViewById(R.id.iv_set);
		lv_set = (ListView) findViewById(R.id.lv_set);
		mylayout = (MyLinearLayout) findViewById(R.id.mylaout);
		lv_set.setAdapter(new ArrayAdapter<String>(this, R.layout.item,
				R.id.tv_item, title));
		/***
		 * 实现该接口
		 */
		mylayout.setOnScrollListener(new OnScrollListener() {
			@Override
			public void doScroll(float distanceX) {
				doScrolling(distanceX);
			}

			@Override
			public void doLoosen() {
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_right
						.getLayoutParams();
				Log.e(TAG, "layoutParams.rightMargin="
						+ layoutParams.rightMargin);
				// 缩回去
				if (layoutParams.rightMargin < -window_width / 2) {
					new AsynMove().execute(-SPEED);
				} else {
					new AsynMove().execute(SPEED);
				}
			}
		});

		// 点击监听
		lv_set.setOnItemClickListener(this);

		layout_right.setOnTouchListener(this);
		layout_left.setOnTouchListener(this);
		iv_set.setOnTouchListener(this);
		mGestureDetector = new GestureDetector(this);
		// 禁用长按监听
		mGestureDetector.setIsLongpressEnabled(false);
		getMAX_WIDTH();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		InitView();

	}

	/***
	 * listview 正在滑动时执行.
	 */
	void doScrolling(float distanceX) {
		isScrolling = true;

		mScrollX += distanceX;// distanceX:向左为正，右为负

		RelativeLayout.LayoutParams layoutParams_left = (RelativeLayout.LayoutParams) layout_left
				.getLayoutParams();
		RelativeLayout.LayoutParams layoutParams_right = (RelativeLayout.LayoutParams) layout_right
				.getLayoutParams();

		layoutParams_right.rightMargin += mScrollX;
		layoutParams_left.rightMargin = window_width
				+ layoutParams_right.rightMargin;

		if (layoutParams_right.rightMargin >= 0) {

			isScrolling = false;// 拖过头了不需要再执行AsynMove了
			layoutParams_right.rightMargin = 0;
			layoutParams_left.rightMargin = window_width;

		} else if (layoutParams_right.rightMargin <= -MAX_WIDTH) {
			// 拖过头了不需要再执行AsynMove了
			isScrolling = false;
			layoutParams_left.rightMargin = window_width - MAX_WIDTH;
			layoutParams_right.rightMargin = -MAX_WIDTH;
		}

		layout_left.setLayoutParams(layoutParams_left);
		layout_right.setLayoutParams(layoutParams_right);
	}

	/***
	 * 获取移动距离
	 */
	void getMAX_WIDTH() {
		ViewTreeObserver viewTreeObserver = layout_right.getViewTreeObserver();
		// 获取控件宽度
		viewTreeObserver.addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (!hasMeasured) {
					window_width = getWindowManager().getDefaultDisplay()
							.getWidth();
					MAX_WIDTH = layout_left.getWidth();
					RelativeLayout.LayoutParams layoutParams_left = (RelativeLayout.LayoutParams) layout_left
							.getLayoutParams();
					RelativeLayout.LayoutParams layoutParams_right = (RelativeLayout.LayoutParams) layout_right
							.getLayoutParams();
					ViewGroup.LayoutParams layoutParams_mylayout = mylayout
							.getLayoutParams();

					// 设置layout_left的初始位置.
					layoutParams_left.rightMargin = window_width;
					layout_left.setLayoutParams(layoutParams_left);
					// 注意：设置lv_set的宽度防止被在移动的时候控件被挤压
					layoutParams_mylayout.width = MAX_WIDTH;
					mylayout.setLayoutParams(layoutParams_mylayout);

					// 注意： 设置layout_right的宽度。防止被在移动的时候控件被挤压
					layoutParams_right.width = window_width;
					layout_right.setLayoutParams(layoutParams_right);
					Log.v(TAG, "MAX_WIDTH=" + MAX_WIDTH + "width="
							+ window_width);
					hasMeasured = true;
				}
				return true;
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			RelativeLayout.LayoutParams layoutParams_right = (RelativeLayout.LayoutParams) layout_right
					.getLayoutParams();
			if (layoutParams_right.rightMargin < 0) {
				new AsynMove().execute(-SPEED);
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		view = v;// 记录点击的控件

		// 松开的时候要判断，如果不到半屏幕位子则缩回去，
		if (MotionEvent.ACTION_UP == event.getAction() && isScrolling == true) {
			RelativeLayout.LayoutParams layoutParams_left = (RelativeLayout.LayoutParams) layout_left
					.getLayoutParams();
			// 缩回去
			if (layoutParams_left.rightMargin > window_width - MAX_WIDTH / 2) {
				new AsynMove().execute(-SPEED);
			} else {
				new AsynMove().execute(SPEED);
			}
		}

		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {

		int position = lv_set.pointToPosition((int) e.getX(), (int) e.getY());
		if (position != ListView.INVALID_POSITION) {
			View child = lv_set.getChildAt(position
					- lv_set.getFirstVisiblePosition());
			if (child != null)
				child.setPressed(true);
		}

		mScrollX = 0;
		isScrolling = false;
		// 将之改为true，才会传递给onSingleTapUp,不然事件不会向下传递.
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	/***
	 * 点击松开执行
	 */
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// 点击的不是layout_left

		if (view != null && view == iv_set) {
			RelativeLayout.LayoutParams layoutParams_right = (RelativeLayout.LayoutParams) layout_right
					.getLayoutParams();
			// 右移动
			if (layoutParams_right.rightMargin >= 0) {
				new AsynMove().execute(SPEED);
				lv_set.setSelection(0);// 设置为首位.
			} else {
				// 左移动
				new AsynMove().execute(-SPEED);
			}
		} else if (view != null && view == layout_right) {
			RelativeLayout.LayoutParams layoutParams_right = (android.widget.RelativeLayout.LayoutParams) layout_right
					.getLayoutParams();
			if (layoutParams_right.rightMargin < 0) {
				// 说明layout_left处于移动最左端状态，这个时候如果点击layout_left应该直接所以原有状态.(更人性化)
				// 左移动
				new AsynMove().execute(-SPEED);
			}
		}

		return true;
	}

	/***
	 * 滑动监听 就是一个点移动到另外一个点. distanceX=后面点x-前面点x，如果大于0，说明后面点在前面点的右边及向右滑动
	 */
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// 执行滑动.
		doScrolling(distanceX);

		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	class AsynMove extends AsyncTask<Integer, Integer, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			int times = 0;
			if (MAX_WIDTH % Math.abs(params[0]) == 0)// 整除
				times = MAX_WIDTH / Math.abs(params[0]);
			else
				times = MAX_WIDTH / Math.abs(params[0]) + 1;// 有余数

			for (int i = 0; i < times; i++) {
				publishProgress(params[0]);
				try {
					Thread.sleep(sleep_time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return null;
		}

		/**
		 * update UI
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			RelativeLayout.LayoutParams layoutParams_left = (RelativeLayout.LayoutParams) layout_left
					.getLayoutParams();
			RelativeLayout.LayoutParams layoutParams_right = (RelativeLayout.LayoutParams) layout_right
					.getLayoutParams();

			if (values[0] < 0) {
				// 左移动
				layoutParams_left.rightMargin = Math
						.min(layoutParams_left.rightMargin - values[0],
								window_width);
				layoutParams_right.rightMargin = Math.min(
						layoutParams_right.rightMargin - values[0], 0);

			} else {
				// 右移动
				layoutParams_left.rightMargin = Math.max(
						layoutParams_left.rightMargin - values[0], window_width
								- MAX_WIDTH);
				layoutParams_right.rightMargin = Math.max(
						layoutParams_right.rightMargin - values[0], -MAX_WIDTH);
				System.out.println("left=" + layoutParams_left.rightMargin
						+ "，right=" + layoutParams_right.rightMargin);
			}

			layout_left.setLayoutParams(layoutParams_left);
			layout_right.setLayoutParams(layoutParams_right);

		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		RelativeLayout.LayoutParams layoutParams_right = (RelativeLayout.LayoutParams) layout_right
				.getLayoutParams();
		// 只要没有滑动则都属于点击
		if (layoutParams_right.rightMargin == -MAX_WIDTH)
			Toast.makeText(MainActivity.this, title[position], 1).show();
	}
}
