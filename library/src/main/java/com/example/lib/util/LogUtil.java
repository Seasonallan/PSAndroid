package com.example.lib.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.text.TextUtils;
import android.util.Log;

import com.example.lib.file.FileUtils;

/**
 * LOG日志工具类；如果需要指定是否需要输入，指定的TAG，知道的输出目录，调用之前需要调用init()方法
 */
public class LogUtil {

	private static String TAG = "Season";
	public static boolean DEBUG = true;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss.ms");
	private static String LOG_FILE = "";

	public static void init(String tag, boolean debug, String logFile) {
		TAG = tag;
		DEBUG = debug;
		LOG_FILE = logFile;
	}

	public static void LOG(String s) {
		Log.e(TAG, s);
	}

	public static void i(String msg) {
		i(TAG, msg);
	}

	public static void i(String tag, String msg) {
		i(tag, msg, null);
	}

	public static void i(String tag, String format, Object... args) {
		String msg;
		if (args != null && args.length > 0) {
			msg = String.format(format, args);
		} else {
			msg = format;
		}
		if (DEBUG) {
			Log.i(tag, msg, null);
		}
		outMessage(tag, msg, null);
	}


	public static void w(String msg) {
		w(TAG, msg);
	}

	public static void w(String tag, String msg) {
		i(tag, msg, null);
	}

	public static void w(String tag, String msg, Throwable tr) {
		if (DEBUG) {
			Log.w(tag, msg, tr);
		}
		outMessage(tag, msg, tr);
	}

	public static void v(String msg) {
		v(TAG, msg);
	}

	public static void v(String tag, String msg) {
		if (DEBUG) {
			Log.v(tag, msg);
		}
		outMessage(tag, msg);
	}

	public static void e(String msg) {
		e(TAG, msg);
	}

	public static void e(String tag, String msg) {
		e(tag, msg, null);
	}
	
	public static void e(String msg, Throwable tr) {
		e(TAG, msg, tr);
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (DEBUG) {
			Log.e(tag, msg, tr);
		}
		outMessage(tag, msg, tr);
	}

	private static void outMessage(String tag, String msg) {
		outMessage(tag, msg, null);
	}

	private static void outMessage(String tag, String msg, Throwable tr) {
		if (TextUtils.isEmpty(LOG_FILE)) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(sdf.format(new Date()));
		sb.append(": ");
		sb.append(tag);
		sb.append(": ");
		sb.append(msg);
		sb.append("\n");
		if (tr != null) {
			sb.append(Log.getStackTraceString(tr));
			sb.append("\n");
		}
		FileUtils.writeStr2File(sb.toString(), LOG_FILE);
	}


	/**
	 * @param object  传入Class类型 可以用getClass()得到
	 * @param out    动态参数，这里是要输出的数据
	 */
	public static void log(Class<?> object, Object... out) {
		boolean next = false;//判断是否含有 lambda 表达式
		String methodName;

		String className = object.getName();
		if (className == null || className.isEmpty()) {
			return;
		} else if (className.contains("$")) { //用于内部类的名字解析
			className = className.substring(className.lastIndexOf(".") + 1, className.indexOf("$"));
		} else {
			className = className.substring(className.lastIndexOf(".") + 1, className.length());
		}
		StackTraceElement[] s = Thread.currentThread().getStackTrace();
		for (StackTraceElement value : s) {
			if (value.getMethodName().startsWith("lambda")) {
				next = true;
			}
		}
		if (!next) {
			methodName = s[3].getMethodName();
		} else {
			methodName = s[5].getMethodName();
		}

		//得到代码所在的行数
		int lines = s[3].getLineNumber();

		//生成指向java的字符串 加入到TAG标签里面
		String TAG = "类class" + "(" + className + ".java:" + lines + ")";

		//生成用户想要输出的数据
		StringBuilder temp = new StringBuilder();
		for (Object anOut : out) {
			temp.append(" ").append(anOut).append(",");
		}
		//删除最后的 ,  号
		if (out.length != 0) {
			temp.deleteCharAt(temp.length() - 1);
		}
		String parameter = "方法method ：" + methodName + "  输出： " + temp;
		Log.d(TAG, parameter);
	}

}
