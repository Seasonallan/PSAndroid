package com.season.lib.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * 输入法管理器
 *
 * @author Linmd
 */
public class InputMethodUtil {

	/**
	 * 如果输入法在窗口上已经显示，则隐藏，反之则显示
	 *
	 * @param context
	 */
	public static void toggleInput(Context context) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, 0);
	}

	/**
	 * view为接受软键盘输入的视图，SHOW_FORCED表示强制显示
	 *
	 * @param context
	 * @param view
	 */
	public static void showInputForced(Context context, View view) {
		if (view != null) {
			view.requestFocus();
		}
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
	}

	public static void showInputAndRequestFocus(Context context, View view) {
		InputMethodUtil.toggleInput(context);
		requestFocus(view);
	}

	public static void requestFocus(View view) {
		view.setFocusableInTouchMode(true);
		view.setFocusable(true);
		view.requestFocus();
	}

	//强制显示软键盘
	public static void show(Context context, View view) {
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.requestFocusFromTouch();

		InputMethodManager imm = (InputMethodManager) context.getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
	}

	public static void showSoftInput(Context context, View view) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
	}

	public static void hideSoftInput(Context context, View view) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(
				Context.INPUT_METHOD_SERVICE);
		try {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		} catch (Exception e) {

		}
	}

	/**
	 * view为接受软键盘输入的视图，强制隐藏键盘
	 *
	 * @param context
	 * @param view
	 */
	public static void hideInputForced(Context context, View view) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	/**
	 * 调用隐藏系统默认的输入法
	 *
	 * @param activity
	 */
	public static void hideInput(Activity activity) {

		try {
			((InputMethodManager) activity
					.getSystemService(Context.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(activity.getCurrentFocus()
									.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取输入法打开的状态
	 *
	 * @param context
	 * @return
	 */
	public static boolean getInputStatus(Context context) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isOpen = imm.isActive();// isOpen若返回true，则表示输入法打开
		return isOpen;
	}

}
