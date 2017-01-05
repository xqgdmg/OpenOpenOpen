package com.okhttp.daemon.okhttpdemo.util;

import android.widget.Toast;

import com.okhttp.daemon.okhttpdemo.MyApplication;

public class ToastUtil {

	private static Toast mToast;
	
    public static void showToast(String text) {
        if(mToast == null) {  
            mToast = Toast.makeText(MyApplication.getContext(), text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);    
            mToast.setDuration(Toast.LENGTH_SHORT);  
        }  
        mToast.show();  
    }  
      
    public static void cancelToast() {  
    	
            if (mToast != null) {  
                mToast.cancel();  
            }  
        }  
      
}
