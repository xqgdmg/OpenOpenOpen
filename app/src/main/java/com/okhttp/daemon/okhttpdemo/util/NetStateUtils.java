package com.okhttp.daemon.okhttpdemo.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetStateUtils {
	public static boolean isNetworkConnected(Context context) { 
		if (context != null) { 
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context 
		.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo(); 
		if (mNetworkInfo != null) { 
		return mNetworkInfo.isAvailable(); 
		} 
		}
		ToastUtil.showToast("网络异常");
		return false; 
		}
	
	public boolean isWifiConnected(Context context) { 
		if (context != null) { 
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context 
		.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo mWiFiNetworkInfo = mConnectivityManager 
		.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
		if (mWiFiNetworkInfo != null) { 
		return mWiFiNetworkInfo.isAvailable(); 
		} 
		} 
		return false; 
		}
	
	
}
