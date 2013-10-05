package com.montycall.android.lebanoncall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.montycall.android.lebanoncall.constants.Constants;
import com.montycall.android.lebanoncall.db.DbUtility;
import com.montycall.android.lebanoncall.db.SpactronDataBaseHelper;
import com.montycall.android.lebanoncall.service.CallService;

public class CallerIdSetActivity extends SherlockActivity {
	private static final String TAG = "CallerIdSetActivity";
	
	private static final String GET_VERIFIED_CALLERIDS = "http://54.225.169.80/webapi_spactron/dist/spactron/playstore-0279821/get_callerid.php?user_key=SIP_USER_NAME";
	private static final String SET_DEFAULT_CALLERID = "http://54.225.169.80/webapi_spactron/dist/spactron/playstore-0279821/mobile_callerid.php?user_key=SIP_USER_NAME&user_mobile=MOBILE_PHONE";
	
	public static final int LOCATE_COUNTRY = 0;
	public static final int GET_CALLER_IDS = 1;
	public static final int SET_CALLER_ID = 2;
	public static final int VERIFY_CALLER_ID_WITH_CODE = 3;
	public static final int SET_DEF_CALLER_ID = 4;
	private String phoneNumber = "";
	private SpactronDataBaseHelper dataBaseHelper = null;
	private SQLiteDatabase sqLiteDatabase = null;
	private CallerIdActivityAdapter mAdapter = null;
	private ListView mCallerIdListView;
	public static ArrayList<CallerId> mAllCallerIds = new ArrayList<CallerId>();
	
	static class CallerId {
		public String callerIdNumber = null;
		public int status = 0;
		public String code = null;
		
		public CallerId(String number, int st, String vcode) {
			callerIdNumber = number;
			status = st;
			code = vcode;
		}
	}
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calleridset_activity);
		setTitle("");
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    BitmapDrawable bg = (BitmapDrawable)getResources().getDrawable(R.drawable.titlebar);
        //bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        getSupportActionBar().setBackgroundDrawable(bg);
		mCallerIdListView = (ListView)findViewById(R.id.caller_id_list);
		registerForContextMenu(mCallerIdListView);
		dataBaseHelper  = new SpactronDataBaseHelper(this);
		sqLiteDatabase = dataBaseHelper.getWritableDatabase();
		mAdapter = new CallerIdActivityAdapter(CallerIdSetActivity.this, mAllCallerIds);
		mCallerIdListView.setAdapter(mAdapter);
		populateCallerIds();
		
		mCallerIdListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//if(mAllCallerIds.get(position).status == CallerIdActivityAdapter.UNVERIFIED)
					view.showContextMenu();
			}
		});
	}
	
	void populateCallerIds() {
		mAllCallerIds.clear();
		
		CallerId privateCallerId = new CallerId("PrivateCallerID", CallerIdActivityAdapter.VERIFIED, "");
		mAllCallerIds.add(privateCallerId);
		
		GetCallerIds fci = new GetCallerIds();
		fci.execute(GET_CALLER_IDS);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getSupportMenuInflater();
        menuInflater.inflate(R.menu.activity_main, menu);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        menu.findItem(R.id.menu_roster).setVisible(true);
        menu.findItem(R.id.menu_roster).setEnabled(false);
        menu.findItem(R.id.menu_roster).setIcon(R.drawable.content_new);
        menu.findItem(R.id.menu_roster).setEnabled(true);
        menu.findItem(R.id.menu_settings).setVisible(false);
        menu.findItem(R.id.menu_all_contact).setVisible(false);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            
        case android.R.id.home:
            // app icon in action bar clicked; go home
        	finish();
            overridePendingTransition(0,R.anim.pull_out_to_right);
            return true;
            
        case R.id.menu_roster:
        	AlertDialog.Builder alert = new AlertDialog.Builder(CallerIdSetActivity.this);

			alert.setTitle("Caller ID");
			alert.setMessage("Please input your caller-id phone number or tap on \"Set Private\"");

			// Set an EditText view to get user input
			final EditText input = new EditText(CallerIdSetActivity.this);
			SharedPreferences settings = CallerIdSetActivity.this.getSharedPreferences(Constants.PREFS_NAME, 0);
			String countryCode = settings.getString(Constants.MONTY_CHAT_USER_COUNTRY_CODE, "");
			input.setText(countryCode);
			input.setInputType(InputType.TYPE_CLASS_PHONE);
			input.setSelection(countryCode.length());
			alert.setView(input);

			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							Editable value = input.getText();
							Log.e(TAG, "value " + value);
							Cursor cursor = null;
							try{
								String sql = "SELECT * FROM " + DbUtility.TABLE_NAME_CALLER_ID
										+ " WHERE " + DbUtility.CALLER_NUMBER + "=?";
								cursor = sqLiteDatabase.rawQuery(sql, new String[]{value.toString()});
								if(cursor.getCount() == 0){
									ContentValues contentValues = new ContentValues();
									contentValues.put(DbUtility.CALLER_NUMBER, value.toString());
									contentValues.put(DbUtility.STATUS, CallerIdActivityAdapter.UNVERIFIED);
									contentValues.put(DbUtility.CODE, "");		
									if(sqLiteDatabase.insert(DbUtility.TABLE_NAME_CALLER_ID, "", contentValues) == -1)
										Log.e("Insert to DB", "Failed to insert into DB");
									CallerId cid = new CallerId(value.toString(), CallerIdActivityAdapter.UNVERIFIED, "");
									mAllCallerIds.add(cid);
								}
							} catch(Exception e){							
								e.printStackTrace();
							} finally{
							
								cursor.close();
							}
								
							SetCallerId fci = new SetCallerId();
							fci.execute(value.toString());
							mAdapter.notifyDataSetChanged();
							
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
			break;
	        }
        return super.onOptionsItemSelected(item);
    }
  
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.pull_in_from_left,
				R.anim.pull_out_to_right);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		phoneNumber = null;
		menu.setHeaderTitle("Caller ID Options");
		if(mAllCallerIds.get(info.position).status == CallerIdActivityAdapter.VERIFIED ||
				info.position == 0)
		menu.add(Menu.NONE, 0, 0, "Set as Caller ID");
		if(info.position !=0)
			menu.add(Menu.NONE, 1, 0, "Delete");
		phoneNumber = mAllCallerIds.get(info.position).callerIdNumber;
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		int id = item.getItemId();
		switch (item.getItemId()) {
		case 0:
			if(phoneNumber != null) {
				SetDefaultCallerId sdc = new SetDefaultCallerId();
				sdc.execute(phoneNumber);
			}
			break;
		case 1:
			if(phoneNumber != null) {
				
				try{
					int count = sqLiteDatabase.delete(DbUtility.TABLE_NAME_CALLER_ID, DbUtility.CALLER_NUMBER + "=?",
							new String[] {phoneNumber});
				} catch(Exception e){
					
				} finally{
				}
				populateCallerIds();
			}
			break;
		default:
			break;
		}
		
		return super.onContextItemSelected(item);
	}
			
	class GetCallerIds extends AsyncTask<Integer, Integer, Integer>{
		
		String result = null;
		ArrayList<CallerId> mtempCallerIds = new ArrayList<CallerId>();
		
		@Override
		protected void onPostExecute(Integer result) {
			mAllCallerIds.addAll(mtempCallerIds);
			mAdapter.notifyDataSetChanged();
			if(result == null) {
				Toast.makeText(CallerIdSetActivity.this, "An error occurred, please try again", Toast.LENGTH_LONG).show();
			}
			super.onPostExecute(result);
		}
		
		@Override
		protected Integer doInBackground(Integer... params) {
			if(params.length != 1) {
	        	return -1;
	        }
			mtempCallerIds.clear();
			Cursor cur = null;
			try{
				String sql = "SELECT * FROM " + DbUtility.TABLE_NAME_CALLER_ID;
				cur = sqLiteDatabase.rawQuery(sql, null);
				if(cur.getCount()>0 && cur.moveToFirst()){
					do{
						CallerId cid = new CallerId(cur.getString(cur.getColumnIndex(DbUtility.CALLER_NUMBER)),
								cur.getInt(cur.getColumnIndex(DbUtility.STATUS)), "");
						mtempCallerIds.add(cid);
					} while(cur.moveToNext());
				}
			}catch(Exception e){
				e.printStackTrace();
			} finally{
				if(cur != null && !cur.isClosed())
					cur.close();
			}
    		return params[0];
		}
	}
	
	class SetDefaultCallerId extends AsyncTask<String, String, String>{
		
		String result = null;
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(result != null) {
				SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(Constants.DEFAULT_CALLER_ID, result);
				editor.commit();
			} else {
				Toast.makeText(CallerIdSetActivity.this, "An error occurred, please try again", Toast.LENGTH_LONG).show();
			}
			mAdapter.notifyDataSetChanged();
		}
		
		@Override
		protected String doInBackground(String... params) {
			String url1 = SET_DEFAULT_CALLERID.replace("SIP_USER_NAME", CallService.getSipUserName());
			String url = url1.replace("MOBILE_PHONE", params[0]);
			Log.v(TAG, "URL = " + url);
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			
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
				if((result.equalsIgnoreCase("true")) || (result.equalsIgnoreCase("1"))) {
					return params[0];
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	class SetCallerId extends AsyncTask<String, String, String>{
	
	String result = null;
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if(result == null) {
			Toast.makeText(CallerIdSetActivity.this, "An error occurred, please try again", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected String doInBackground(String... params) {
		String url = "http://54.225.169.80/webapi_spactron/dist/spactron/playstore-0279821/mobile_verify.php?user_key=SIP_USER_NAME&user_mobile=MOBILE_PHONE&app_name=Spactron&app_platform=Android&app_version=1.0.1.1000";
		String url1 = url.replace("SIP_USER_NAME", CallService.getSipUserName());
		String url2 = url1.replace("MOBILE_PHONE", params[0]);
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
				return params[0];
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}	
	
}
