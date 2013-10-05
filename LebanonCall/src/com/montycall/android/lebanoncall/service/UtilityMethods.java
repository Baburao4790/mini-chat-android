package com.montycall.android.lebanoncall.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.montycall.android.lebanoncall.constants.Constants;

public class UtilityMethods {

	/**
	 * This method just checks if we have ever registered before. Will return
	 * true if we have a saved user name and password and will return false
	 * otherwise
	 * 
	 * @param context
	 *            - Any context within the application so as to get
	 *            SharedPreferences
	 */
	public static boolean haveWeRegisteredBefore(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constants.PREFS_NAME, Context.MODE_PRIVATE);

		if (sharedPreferences.contains(Constants.PHONE_VERIFIED)) {
			return true;
		}
		return false;
	}

	}
