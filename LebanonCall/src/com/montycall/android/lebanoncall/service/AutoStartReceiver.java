package com.montycall.android.lebanoncall.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStartReceiver extends BroadcastReceiver {

	/** The notification type in the Status Bar */
	public static final int MSG_NOTIFICATION = 9901;
	public static final int SUB_NOTIFICATION = 9902;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			//Log.v("AutoStartReceiver", "BOOT_COMPLETED. Now Starting ChatService");
			Intent service = new Intent(context, CallService.class);
			context.startService(service);
		}
	}
}
