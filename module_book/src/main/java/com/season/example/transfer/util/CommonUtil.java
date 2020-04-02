package com.season.example.transfer.util;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.WindowManager;

/**
 * @brief 通用工具
 * @author join
 */
public class CommonUtil {
	private static CommonUtil mCommonUtil;
	private Context mContext;

	public static CommonUtil getSingleton(Context context) {
		if (mCommonUtil == null) {
			mCommonUtil = new CommonUtil(context);
		}
		return mCommonUtil;
	}

	private CommonUtil(Context context) {
		mContext = context;
	}

	/**
	 * 获取文件后缀名，不带`.`
	 * 
	 * @param file
	 *            文件
	 * @return 文件后缀
	 */
	public static String getExtension(File file) {
		String name = file.getName();
		int i = name.lastIndexOf('.');
		int p = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
		return i > p ? name.substring(i + 1) : "";
	}


	public static byte byteOfInt(int value, int which) {
		int shift = which * 8;
		return (byte) (value >> shift);
	}

	public static String intToInet(int value) {
		byte[] bytes = new byte[4];
		for (int i = 0; i < 4; i++) {
			bytes[i] = byteOfInt(value, i);
		}
		try {
			return InetAddress.getByAddress(bytes).getHostAddress();
		} catch (UnknownHostException e) {
			// This only happens if the byte array has a bad length
			return null;
		}
	}

	/**
	 * @brief 获取当前IP地址
	 * @return null if network off
	 */
	public static String getLocalIpAddress(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null && ni.isConnected() == true
				&& ni.getType() == ConnectivityManager.TYPE_WIFI) {
			WifiManager wm = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			int ipAddress = wm.getConnectionInfo().getIpAddress();
			if (ipAddress == 0)
				return null;
			return intToInet(ipAddress);
		}
		try {
			// 遍历网络接口
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();// 遍历IP地址
				// 遍历IP地址
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					// 非回传地址时返回
					if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}


}
