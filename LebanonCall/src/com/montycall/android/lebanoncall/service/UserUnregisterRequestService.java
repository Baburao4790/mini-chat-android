package com.montycall.android.lebanoncall.service;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.IntentService;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.montycall.android.lebanoncall.constants.Constants;
 
public class UserUnregisterRequestService  extends IntentService{
    private static final String TAG = "UserUnregisterRequestService";  
    public static final String USER_UNREGISTER_REQUEST_STRING_URL = "com.android.chatclient.service.USER_UNREGISTER_REQUEST_STRING_URL";
 
    private String URL = null;
 
    public UserUnregisterRequestService() {
        super("UserUnregisterRequestService");
    }
 
    @Override
    protected void onHandleIntent(Intent intent) {
 
        String requestString = intent.getStringExtra(USER_UNREGISTER_REQUEST_STRING_URL);
        String responseMessage = "";
        //Log.d(TAG,"Sending Read Report for = " + requestString );

        try {
 
            URL = requestString;
            HttpClient httpclient = new DefaultHttpClient();
            
            HttpGet httpGet = new HttpGet(URL);
            String user_pass = Constants.HTTP_USERNAME + ":" + Constants.HTTP_PASSWORD;
            httpGet.setHeader("Authorization", "Basic " + Base64.encodeToString(user_pass.getBytes(), Base64.NO_WRAP));

            HttpResponse response = httpclient.execute(httpGet);
 
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
            	InputStream inputStream = response.getEntity().getContent();
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				StringBuilder stringBuilder = new StringBuilder();
				String bufferedStrChunk = null;
				while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
					stringBuilder.append(bufferedStrChunk);
				}
				responseMessage = stringBuilder.toString();
                //Log.d(TAG,"Read Report Update response = " + responseMessage );
            }else{
                //Closes the connection.
                //Log.w(TAG,statusLine.getReasonPhrase());
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
 
        } catch (ClientProtocolException e) {
            Log.w(TAG,"1" + e );
            responseMessage = e.getMessage();
        } catch (IOException e) {
            Log.w(TAG,"2" + e );
            responseMessage = e.getMessage();
        }catch (Exception e) {
            Log.w(TAG,"3" + e );
            responseMessage = e.getMessage();
        }
 
    }
 
}