package com.montycall.android.lebanoncall.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.montycall.android.lebanoncall.constants.Constants;

public class GetDefaultCountry extends Thread {
	private static final String URL = "http://54.225.169.80/webapi_spactron/dist/spactron/playstore-0279821/country_locator.php";
	private static final String TAG = "GetDefaultCountry";
	private Context mContext;
	private String result = null;
	
	public GetDefaultCountry(Context context) {
		mContext = context;
	}
	
	@Override
	public void run() {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(URL);
		//String user_pass = Constants.HTTP_USERNAME + ":" + Constants.HTTP_PASSWORD;
		//httpGet.setHeader("Authorization", "Basic " + Base64.encodeToString(user_pass.getBytes(), Base64.NO_WRAP));
		
		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			InputStream inputStream = httpResponse.getEntity().getContent();

			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			StringBuilder stringBuilder = new StringBuilder();

			String bufferedStrChunk = null;
			while((bufferedStrChunk = bufferedReader.readLine()) != null){
				stringBuilder.append(bufferedStrChunk);
			}
			result = stringBuilder.toString();
			Log.d(TAG, "HttpResponse :" + stringBuilder.toString());
			
			Pattern pattern = Pattern.compile("\\{(.*?)\\}");
			Matcher matchPattern = pattern.matcher(result);

			String countryCode = null;
	        while(matchPattern.find()) {
	        	String a = matchPattern.group(1);
	        	String[] b = a.split(",");
	        	countryCode = b[1].substring(8, b[1].length()-1);
	        	Log.v(TAG, "Country Name "+ countryCode);
	        	if((countryCode != null) && (!countryCode.isEmpty())) {
	        		SavePreferences(Constants.MONTY_CHAT_USER_COUNTRY_CODE, countryCode);
	        	}
	        }
	
		} catch (ClientProtocolException cpe) {
			//Log.e(TAG, "Exception generates because of httpResponse :" + cpe);
			cpe.printStackTrace();
		} catch (IOException ioe) {
			//Log.e(TAG, "Exception generates because of httpResponse :" + ioe);
			ioe.printStackTrace();
		}
		
	}
	
	private void SavePreferences(String key, String value) {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

}
