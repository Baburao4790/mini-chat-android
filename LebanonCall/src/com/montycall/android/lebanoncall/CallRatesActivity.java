package com.montycall.android.lebanoncall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.montycall.android.lebanoncall.service.CallService;

public class CallRatesActivity extends SherlockActivity implements TextWatcher {
	
	private static final String TAG = "CallRates";
	private CallRatesListAdapter mAdapter = null;
	private ListView mCallRatesListView;
	private EditText mCallRatesSearchText;
	private String mSearchString;
	private static boolean isRunning = false;
	private static ArrayList<CallRates> mAllCallRates = new ArrayList<CallRatesActivity.CallRates>();
	List<CallRates> filterCallRatesArray = new ArrayList<CallRates>();
	ArrayList<CallRates> itemsCallRatesSection = new ArrayList<CallRates>();

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
	Messenger mChatService;
    boolean mIsBound;
    public int mServiceState = CallService.STATE_UNKNOWN;
    
    
/** Start of Service Related Processing and messaging */
    
    /**
     * Handler of incoming messages from service.
     */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case CallService.MSG_STATE_INFO:
				if (mServiceState != msg.arg1) {
					mServiceState = msg.arg1;
					switch (msg.arg1) {
					case CallService.STATE_DISCONNECTED:
						//Log.v(TAG, "ChatService.STATE_DISCONNECTED");
						break;

					case CallService.STATE_CONNECTED:
						//Log.v(TAG, "ChatService.STATE_CONNECTED");
						break;

					case CallService.STATE_WAITING_FOR_USER_EMAIL:
						//Log.v(TAG, "ChatService.STATE_WAITING_FOR_USER_NUMBER");
						break;

					case CallService.STATE_SENDING_EMAIL_ID:
						//Log.v(TAG, "ChatService.STATE_WAITING_FOR_PHONE_VERIFICATION");
						break;

					case CallService.STATE_WAITING_FOR_VERIFICATION_CODE:
						//Log.v(TAG, "ChatService.STATE_WAITING_FOR_USER_CREDENTIALS");
						break;

					case CallService.STATE_REGISTERING:
						//Log.v(TAG, "ChatService.STATE_REGISTERING");
						break;

					case CallService.STATE_LOGGED_IN:
						//Log.v(TAG, "ChatService.STATE_LOGGED_IN");
						break;
					}
				}
				break;

			default:
				super.handleMessage(msg);
				//Log.d(TAG, "###################  default  1");
			}
			
		}
	}

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mChatService = new Messenger(service);
            mIsBound = true;
            sendMessageToService(CallService.MSG_REGISTER_CLIENT, 0, 0, null);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mChatService = null;
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        Intent service = new Intent(this, CallService.class);
        startService(service);
        getApplicationContext().bindService(new Intent(this, CallService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

	void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mChatService != null) {
                sendMessageToService(CallService.MSG_UNREGISTER_CLIENT, 0, 0, null);
            }
            // Detach our existing connection.
            getApplicationContext().unbindService(mConnection);
            mIsBound = false;
        }
    }

	private void sendMessageToService(int what, int arg1, int arg2, Object obj) {
		if(mIsBound) {
            Message msg = Message.obtain(null, what);
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            msg.obj = obj;
            msg.replyTo = mMessenger;
            try {                
                mChatService.send(msg);
            } catch (RemoteException e) {
                //Log.e(TAG, "Error while sending mesage to service. Msg Type " + what);
            }
        }		
	}

    /** End of Service Related Processing and messaging */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setTitle("");
		setContentView(R.layout.callrates_list_display);
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    BitmapDrawable bg = (BitmapDrawable)getResources().getDrawable(R.drawable.titlebar);
        //bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        getSupportActionBar().setBackgroundDrawable(bg);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		doBindService();
		mCallRatesListView = (ListView) findViewById(R.id.call_rate_list);
		mCallRatesSearchText = (EditText) findViewById(R.id.callrate_search_box);
		mCallRatesSearchText.addTextChangedListener(this);
		
		setAdapterToListview(mAllCallRates);
		setSupportProgressBarIndeterminateVisibility(true);
		FetchCallRates fcr = new FetchCallRates();
		fcr.execute(null, null);
	}
	
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        MenuInflater menuInflater = getSupportMenuInflater();
	        menuInflater.inflate(R.menu.activity_main, menu);

	        // Calling super after populating the menu is necessary here to ensure that the
	        // action bar helpers have a chance to handle this event.
	        menu.findItem(R.id.menu_roster).setVisible(false);
	        menu.findItem(R.id.menu_roster).setEnabled(false);
	        menu.findItem(R.id.menu_roster).setIcon(R.drawable.content_picture);
	        menu.findItem(R.id.menu_roster).setEnabled(false);
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
	    
		        }
	        return super.onOptionsItemSelected(item);
	    }
	    
			 
	 @Override
	public void afterTextChanged(Editable s) {
		filterCallRatesArray.clear();
		mSearchString = mCallRatesSearchText.getText().toString().trim()
				.replaceAll("\\s", "");

		if (mAllCallRates.size() > 0 && mSearchString.length() > 0) {
			for (CallRates callrate : mAllCallRates) {
				if (callrate.countryName.toLowerCase()
						.startsWith(mSearchString.toLowerCase())) {

					filterCallRatesArray.add(callrate);
				}
			}
			setAdapterToListview(filterCallRatesArray);
		} else {
			filterCallRatesArray.clear();
			setAdapterToListview(mAllCallRates);
		}

	}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		// Here Data is Filtered!!!
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
		
		public void setAdapterToListview(List<CallRates> listForAdapter) {

			itemsCallRatesSection.clear();

			if (null != listForAdapter && listForAdapter.size() != 0) {

				for (int index = 0; index < listForAdapter.size(); index++) {
					CallRates objItem = (CallRates) listForAdapter.get(index);
					itemsCallRatesSection.add(objItem);
				}
			} 
			
			if (null == mAdapter) {
				mAdapter = new CallRatesListAdapter(CallRatesActivity.this, itemsCallRatesSection);
				mCallRatesListView.setAdapter(mAdapter);
			} else {
				Log.v(TAG, "Data Set Changed");
				mAdapter.notifyDataSetInvalidated();
				mAdapter = new CallRatesListAdapter(CallRatesActivity.this, itemsCallRatesSection);
				mCallRatesListView.setAdapter(mAdapter);
			}
		}
	
	@Override
	protected void onStart() {
		super.onStart();
		CallService.activityCount.incrementAndGet();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		CallService.activityCount.decrementAndGet();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//doBindService();
		isRunning = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		//doUnbindService();
		isRunning = false;
		//Log.v(TAG, "on Pause called");
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.pull_in_from_left,
				R.anim.pull_out_to_right);
	}
	
	@Override
	protected void onDestroy() {
		doUnbindService();
		super.onDestroy();
	}

	public static boolean isRunning() {
		return isRunning;
	}

	class CallRates {
		public String countryName = "";
		public String prices = "";
		public Integer index;
	}
	
	class FetchCallRates extends AsyncTask<Void, Integer, Void>{
		
		private String URL = "http://54.225.169.80/webapi_spactron/dist/spactron/playstore-0279821/call_rate_cc.php?country_code=&country_ios=";
		String result = null;
		
		@Override
		protected void onPostExecute(Void result) {
			setAdapterToListview(mAllCallRates);
			super.onPostExecute(result);
			setSupportProgressBarIndeterminateVisibility(false);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			Integer i = 0;
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
    			//Log.d(TAG, "HttpResponse :" + result);
    			Pattern pattern = Pattern.compile("\\{(.*?)\\}");
    			Matcher matchPattern = pattern.matcher(result);

    			mAllCallRates.clear();
    	        while(matchPattern.find()) {
    	        	String a = matchPattern.group(1);
    	        	String[] b = a.split(",");
    	        	CallRates cr = new CallRates();
    	        	cr.countryName = b[0].substring(15, b[0].length()-1);
    	        	cr.prices = b[1].substring(14, b[1].length() -1);
    	        	cr.index = i;
    	        	i++;
    	        	//Log.v(TAG, i + " " + cr.countryName);
    	        	//Log.v(TAG, i + " " + cr.prices);
    	        	mAllCallRates.add(cr);
    	        }
    		} catch (ClientProtocolException cpe) {
    			Log.e(TAG, "Exception generates because of httpResponse :" + cpe);
    			cpe.printStackTrace();
    		} catch (IOException ioe) {
    			Log.e(TAG, "Exception generates because of httpResponse :" + ioe);
    			ioe.printStackTrace();
    		}
    		return null;
		}
	}
}
