package com.montycall.android.lebanoncall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.montycall.android.lebanoncall.CallerIdSetActivity.CallerId;
import com.montycall.android.lebanoncall.constants.Constants;
import com.montycall.android.lebanoncall.db.DbUtility;
import com.montycall.android.lebanoncall.db.SpactronDataBaseHelper;
import com.montycall.android.lebanoncall.service.CallService;

public class CallerIdActivityAdapter extends ArrayAdapter<CallerId> {
	
	public static final int UNVERIFIED = 0; 
	public static final int VERIFICATION_REQUEST_SENT = 1;
	public static final int VERIFICATION_CODE_ENTERED = 2;
	public static final int VERIFIED = 3;
	
	private static final String TAG = "CallerIdActivityAdapter"; 
	
	private Context mContext;
	private ArrayList<CallerId> mList;
	private CallerId callerId;
	ViewHolder holder;

	public CallerIdActivityAdapter(Context context, ArrayList<CallerId> callerId) {
		super(context, 0, callerId);
		mContext = context;
		mList = callerId;
	}
	
	public static class ViewHolder {
		TextView callerId;
		TextView status;
		ImageButton verify;
		ImageButton delete;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
			callerId = mList.get(position);
			SharedPreferences setting = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
			String defaultCallerId = setting.getString(Constants.DEFAULT_CALLER_ID, "");

			if (convertView == null ) {
				holder = new ViewHolder();
				convertView = (View) LayoutInflater.from(mContext).inflate(R.layout.callerid_item_layout, null, false);
				holder.callerId = (TextView) convertView.findViewById(R.id.callerid_number);
				holder.status = (TextView) convertView.findViewById(R.id.callerid_status);
				holder.verify = (ImageButton) convertView.findViewById(R.id.verify_caller_id);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.callerId.setText(callerId.callerIdNumber);
			
			switch (callerId.status) {
			case UNVERIFIED:
				holder.status.setVisibility(View.GONE);
				holder.verify.setVisibility(View.VISIBLE);
				holder.verify.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

						alert.setTitle("Caller ID");
						alert.setMessage("Please input verification code from SMS");

						// Set an EditText view to get user input
						final EditText input = new EditText(mContext);
						input.setInputType(InputType.TYPE_CLASS_NUMBER);
						alert.setView(input);

						alert.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										Editable value = input.getText();
										Log.e(TAG, "value " + value);
										VerifyCallerId fci = new VerifyCallerId();
										fci.execute(value.toString(), holder.callerId.getText().toString());
									}
								});

						alert.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										// Canceled.
									}
								});

						alert.show();
					}
				});
				break;
			case VERIFIED:
				holder.verify.setVisibility(View.GONE);
				if(callerId.callerIdNumber.equalsIgnoreCase("PrivateCallerID")) {
					holder.status.setText("");
					holder.status.setVisibility(View.INVISIBLE);
				} else {
					holder.status.setText("Verified");
				}
				
				if(defaultCallerId.equalsIgnoreCase(callerId.callerIdNumber)) {
					holder.status.setText("Default");
				}
				holder.status.setVisibility(View.VISIBLE);
				break;
			}
		return convertView;
	}
	
	class VerifyCallerId extends AsyncTask<String, String, String>{
		
		String result = null;
		
		@Override
		protected void onPostExecute(String number) {
			super.onPostExecute(number);
			if(number!=null) {
				SharedPreferences settings = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
				Cursor cur = null;
				try{
					SpactronDataBaseHelper baseHelper = new SpactronDataBaseHelper(mContext);
					SQLiteDatabase sqLiteDatabase = baseHelper.getWritableDatabase();
					String sql = "SELECT * FROM " + DbUtility.TABLE_NAME_CALLER_ID;
					cur = sqLiteDatabase.rawQuery(sql, null);
					/*if(cur.getCount() == 1 && cur.moveToFirst()){
						if(cur.getString(cur.getColumnIndex(DbUtility.CALLER_NUMBER)).equalsIgnoreCase(number)){
							SharedPreferences.Editor editor = settings.edit();
							editor.putString(Constants.DEFAULT_CALLER_ID, number);
							editor.commit();							
						}
					}*/
					if(cur.moveToFirst()){
						ContentValues values = new ContentValues();
						values.put(DbUtility.STATUS, VERIFIED);
						sqLiteDatabase.update(DbUtility.TABLE_NAME_CALLER_ID, values, DbUtility.CALLER_NUMBER + "=?",
								new String[]{number});
					}
				} catch(Exception e){
					e.printStackTrace();
				} finally{
					
				}
				
				for(CallerIdSetActivity.CallerId callerId : CallerIdSetActivity.mAllCallerIds) {
					if(callerId.callerIdNumber.equalsIgnoreCase(number)) {
						callerId.status = VERIFIED;
						notifyDataSetChanged();
						break;
					}
				}
				
			} else {
				Toast.makeText(mContext, "An error occurred, please try again", Toast.LENGTH_LONG).show();
			}
		}
		
		@Override
		protected String doInBackground(String... params) {
			String url = "http://54.225.169.80/webapi_spactron/dist/spactron/playstore-0279821/mobile_register.php?user_key=SIP_USER_NAME&rand_code=RAND_CODE&app_name=Spactron&app_platform=Android&app_version=1.0.1.1000";
			String url1 = url.replace("SIP_USER_NAME", CallService.getSipUserName());
			String url2 = url1.replace("RAND_CODE", params[0]);
			Log.v(TAG, "URL = " + url2);
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url2);
			
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
    			Log.d(TAG, "HttpResponse :" + result);
    			
    		} catch (ClientProtocolException cpe) {
    			Log.e(TAG, "Exception generates because of httpResponse :" + cpe);
    			cpe.printStackTrace();
    		} catch (IOException ioe) {
    			Log.e(TAG, "Exception generates because of httpResponse :" + ioe);
    			ioe.printStackTrace();
    		}
			
			if(result != null) {
				if(result.equalsIgnoreCase("true")) {
					return params[1];
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}
}
