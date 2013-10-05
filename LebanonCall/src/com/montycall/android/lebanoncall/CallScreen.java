package com.montycall.android.lebanoncall;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.montycall.android.lebanoncall.db.CallLogProvider;
import com.montycall.android.lebanoncall.db.CallLogProvider.CallLogConstants;
import com.montycall.android.lebanoncall.service.CallService;


public class CallScreen extends SherlockActivity {
	private static final String TAG = "CallScreen";
	
	private static final byte IDLE = 100;
	private static final byte IN_CALL = 101;
	
	private final NgnEngine mEngine;
	private static byte sCallState = IDLE;
	private TextView mTvInfo;
	private TextView mTvName;
	private ImageView mContactPicture;
	private Button mBtHangUp;
	private TextView mCallRateView;
	private Button speakerBtn;
	private Button muteBtn;
	private Button bluetoothBtn;
	private Button dialpadBtn;
	private String mCalledNumber;
	private String mCalledName;
	private long mCallStartTime;
	private NgnAVSession mSession;
	private BroadcastReceiver mSipBroadCastRecv;
	private ContentResolver mContentResolver;
	private Chronometer mChronometer;
	private static final int NOTIFICATION_ID = 10000;
	private static AudioManager m_amAudioManager = null;

	final Messenger mMessenger = new Messenger(new IncomingHandler());
	Messenger mChatService;
	boolean mIsBound;
	String mPhotoID = null;
	
	// Bad hack 
	private boolean didCallgetActive = false; 

	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(android.os.Message msg) {
			
		}
	}

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mChatService = new Messenger(service);
			sendMessageToService(CallService.MSG_REGISTER_CLIENT, 0, 0, null);
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mChatService = null;
		}
	};

	void doBindService() {
		Intent service = new Intent(getApplicationContext(), CallService.class);
		startService(service);
		mIsBound = true;
		getApplicationContext().bindService(new Intent(getApplicationContext(), CallService.class), mConnection,
				Context.BIND_AUTO_CREATE);
	}

	public void showNetworkDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Check Network Settings")
				.setCancelable(false)
				.setNeutralButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
				;
		AlertDialog alert = builder.create();
		alert.show();

	}

	void doUnbindService() {
		if (mIsBound) {
			if (mChatService != null) {
				sendMessageToService(CallService.MSG_UNREGISTER_CLIENT, 0, 0,
						null);
			}
			getApplicationContext().unbindService(mConnection);
			mIsBound = false;
		}
	}

	private void sendMessageToService(int what, int arg1, int arg2, Object obj) {
		if (mIsBound && mChatService!=null) {
			android.os.Message msg = android.os.Message.obtain(null, what);
			msg.arg1 = arg1;
			msg.arg2 = arg2;
			msg.obj = obj;
			msg.replyTo = mMessenger;
			try {
				mChatService.send(msg);
			} catch (RemoteException e) {
				//Log.e(TAG, "Error while sending mesage to service. Msg Type "	+ what);
			}
		}
	}

	/** End of Service Related Processing and messaging */
	
	
	
	public CallScreen(){
		super();
		mEngine = NgnEngine.getInstance();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        sCallState = IN_CALL;
        setContentView(R.layout.callscreen);
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(false);
	    BitmapDrawable bg = (BitmapDrawable)getResources().getDrawable(R.drawable.titlebar);
        //bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        getSupportActionBar().setBackgroundDrawable(bg);
        doBindService();
        mCallRateView = (TextView)findViewById(R.id.callscreen_price);
        speakerBtn = (Button)findViewById(R.id.speaker_button);
        muteBtn = (Button)findViewById(R.id.mute_button);
        bluetoothBtn = (Button)findViewById(R.id.bluetooth_button);
        dialpadBtn = (Button)findViewById(R.id.dialpad_button);
        mTvName = (TextView) findViewById(R.id.call_screen_textView_name);
        mContactPicture = (ImageView) findViewById(R.id.contact_pic);
        mContentResolver = this.getContentResolver();
        mChronometer = (Chronometer)findViewById(R.id.chronometer1);
        Bundle extras = getIntent().getExtras();
        m_amAudioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        if(extras != null){
        	didCallgetActive = false;
        	mSession = NgnAVSession.getSession(extras.getLong(CallService.EXTRAT_SIP_SESSION_ID));
        	mCalledNumber = extras.getString(CallService.CALLED_NUMBER);
        	mCalledName = extras.getString(CallService.CALLED_NAME);
        	mCallStartTime = System.currentTimeMillis();
        	Log.v(TAG, "Called name " + mCalledName);
        	if(mCalledName == null) {
        		mCalledName = getContactName(getApplicationContext(), mCalledNumber);
        	}
        	if(mCalledName == null) {
         		mCalledName = mCalledNumber;
        	}
        	mTvName.setText(mCalledName);
        }
        
        if(mSession == null){
        	Log.e(TAG, "Null session");
        	finish();
        	return;
        }
        
        FetchCurrentCallRate fcr = new FetchCurrentCallRate(mCalledNumber);
		fcr.execute(null, null);
        
        mSession.incRef();
        mSession.setContext(this);
        
        // listen for audio/video session state
        mSipBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				handleSipEvent(intent);
			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT);
	    registerReceiver(mSipBroadCastRecv, intentFilter);
        
        mTvInfo = (TextView)findViewById(R.id.call_screen_textView_info);
        //mTvRemote = (TextView)findViewById(R.id.callscreen_textView_remote);
        mBtHangUp = (Button)findViewById(R.id.callscreen_button_hangup);
        
        mBtHangUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSession != null){
					mSession.hangUpCall();
					speakerBtn.setBackgroundResource(R.drawable.speaker);
					muteBtn.setBackgroundResource(R.drawable.mute);
				}
			}
		});
        
        //mTvRemote.setText(mSession.getRemotePartyDisplayName());
        mTvInfo.setText(getStateDesc(mSession.getState()));
        
        speakerBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mSession.isSpeakerOn()) {
					mSession.setSpeakerphoneOn(false);
					//m_amAudioManager.setSpeakerphoneOn(false);
					Log.d(TAG, "Set    Speaker Phone Off ");
					speakerBtn.setBackgroundResource(R.drawable.speaker);
				} else {
					mSession.setSpeakerphoneOn(true);
					//m_amAudioManager.setSpeakerphoneOn(true);
					Log.d(TAG, "Set    Speaker Phone On ");
					speakerBtn.setBackgroundResource(R.drawable.speaker_over);
				}
			}
		});
        
        muteBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mSession.isMicrophoneMute()) {
					mSession.setMicrophoneMute(false);
					muteBtn.setBackgroundResource(R.drawable.mute);
				} else {
					mSession.setMicrophoneMute(true);
					muteBtn.setBackgroundResource(R.drawable.mute_over);
				}
			}
		});

        bluetoothBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mSession.isLocalHeld() && mSession.getState() == InviteState.INCALL) {
					
					mSession.resumeCall();
					bluetoothBtn.setBackgroundResource(R.drawable.bluetooth);
				} else {
					
					mSession.holdCall();
					bluetoothBtn.setBackgroundResource(R.drawable.bluetooth_over);
				}
			}
		});
        
        dialpadBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), DtmfDialPad.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
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
        	//finish();
            overridePendingTransition(0,R.anim.pull_out_to_right);
            return true;
    
	        }
        return super.onOptionsItemSelected(item);
    }
    
	@Override
	public void onBackPressed() {
		/* We are not going to do anything here for now. During 
		 * call, a user is allowed to go out of this screen only by pressing
		 * the home button.
		 */
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		showNotification();
		Log.d(TAG,"onResume()");
		if(mSession != null){
			final InviteState callState = mSession.getState();
			mTvInfo.setText(getStateDesc(callState));
			if(callState == InviteState.TERMINATING || callState == InviteState.TERMINATED){
				finish();
			}
		}
		if(mPhotoID != null) {
			mContactPicture.setImageBitmap(getByteContactPhoto(mPhotoID));
		}
		
	}
	
	public Bitmap getByteContactPhoto(String contactId) {
		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, Long.parseLong(contactId));
		Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
		Cursor cursor = getContentResolver().query(photoUri,
		                new String[] {Contacts.Photo.DATA15}, null, null, null);
		if (cursor == null) {
		    return null;
		}
		try {
		    if (cursor.moveToFirst()) {
		        byte[] data = cursor.getBlob(0);
		        if (data != null) {
		            return BitmapFactory.decodeStream( new ByteArrayInputStream(data));
		        }
		    }
		} finally {
		    cursor.close();
		}

		return null;
	}
	
	private void showNotification() {
		Intent intent = new Intent(this, CallScreen.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);

		NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		
		Notification callInProgress = new Notification();
		callInProgress.icon = R.drawable.ic_launcher;
		callInProgress.tickerText = "";

		CharSequence contentTitle = "Spactron";
		CharSequence contentText = "Return to Call";
		callInProgress.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, pIntent);
		
		notificationManager.notify(NOTIFICATION_ID, callInProgress);
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy()");
		doUnbindService();
		if (mSipBroadCastRecv != null) {
			unregisterReceiver(mSipBroadCastRecv);
			mSipBroadCastRecv = null;
		}

		if (mSession != null) {
			mSession.setContext(null);
			mSession.decRef();
		}

		Intent i = new Intent(getApplicationContext(), MainTabActivity.class);
		Bundle b = new Bundle();
		b.putString(MainTabActivity.DEFAULT_TAB, "dialpad");
		i.putExtras(b);
		startActivity(i);
		super.onDestroy();
	}
	
	private String getStateDesc(InviteState state){
		switch(state){
			case NONE:
			default:
				return "Unknown";
			case INCOMING:
				return "Incoming";
			case INPROGRESS:
				return "Dialing";
			case REMOTE_RINGING:
				return "Alerting";
			case EARLY_MEDIA:
				return "Alerting";
				//return "Early media";
			case INCALL:
				return "In Call";
			case TERMINATING:
				return "Terminating";
			case TERMINATED:
				return "terminated";
		}
	}
	
	private void handleSipEvent(Intent intent){
		if(mSession == null){
			Log.e(TAG, "Invalid session object");
			return;
		}
		final String action = intent.getAction();
		if(NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)){
			NgnInviteEventArgs args = intent.getParcelableExtra(NgnInviteEventArgs.EXTRA_EMBEDDED);
			if(args == null){
				Log.e(TAG, "Invalid event args");
				return;
			}
			if(args.getSessionId() != mSession.getId()){
				return;
			}
			final InviteState callState = mSession.getState();
			mTvInfo.setText(getStateDesc(callState));
			switch(callState){
				case REMOTE_RINGING:
					Log.v(TAG, "REMOTE_RINGING");
					sCallState = IN_CALL;
					mEngine.getSoundService().startRingBackTone();
					break;
				case INCOMING:
					Log.v(TAG, "INCOMING");
					sCallState = IN_CALL;
					mEngine.getSoundService().startRingTone();
					break;
				
				case INCALL:
					Log.v(TAG, "INCALL");
					sCallState = IN_CALL;
					didCallgetActive = true;
					mEngine.getSoundService().stopRingTone();
					mEngine.getSoundService().stopRingBackTone();
					//mSession.setSpeakerphoneOn(false);
					//m_amAudioManager.setSpeakerphoneOn(false);
					mChronometer.setBase(SystemClock.elapsedRealtime());
					mChronometer.start();
					break;
				case EARLY_MEDIA:
					sCallState = IN_CALL;
					mEngine.getSoundService().stopRingTone();
					mEngine.getSoundService().stopRingBackTone();
					//mSession.setSpeakerphoneOn(false);
					//m_amAudioManager.setSpeakerphoneOn(false);
					Log.v(TAG, "EARLY_MEDIA");
					break;
				case TERMINATING:
					Log.v(TAG, "TERMINATING");
					if(sCallState != IDLE) {
						sCallState = IDLE;
						NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
						notificationManager.cancel(NOTIFICATION_ID);
						mPhotoID = null;
						sendMessageToService(CallService.MSG_CHECK_BALANCE, 0, 0, null);
						mChronometer.stop();
						finish();
						long elapsedMillis = 0;
						if(didCallgetActive) {
							elapsedMillis = SystemClock.elapsedRealtime() - mChronometer.getBase();
						}
						didCallgetActive = false;
						//make entry in call logs
						addCallLogToDB(CallLogConstants.OUTGOING, mCalledNumber, mCalledName, mCallStartTime, elapsedMillis);
						mEngine.getSoundService().stopRingTone();
						mEngine.getSoundService().stopRingBackTone();
						sendMessageToService(CallService.MSG_CALL_ENDED, 0, 0, null);
					}
					break;
				case TERMINATED:
					Log.v(TAG, "TERMINATED");
					if(sCallState != IDLE) {
						sCallState = IDLE;
						NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
						notificationManager.cancel(NOTIFICATION_ID);
						mPhotoID = null;
						sendMessageToService(CallService.MSG_CHECK_BALANCE, 0, 0, null);
						mChronometer.stop();
						finish();
						long elapsedMillis = 0;
						if(didCallgetActive) {
							elapsedMillis = SystemClock.elapsedRealtime() - mChronometer.getBase();
						}
						didCallgetActive = false;
						//make entry in call logs
						addCallLogToDB(CallLogConstants.OUTGOING, mCalledNumber, mCalledName, mCallStartTime, elapsedMillis);
						mEngine.getSoundService().stopRingTone();
						mEngine.getSoundService().stopRingBackTone();
						sendMessageToService(CallService.MSG_CALL_ENDED, 0, 0, null);
					}
					break;
				default:
						break;
			}
		}
	}
	
	private void addCallLogToDB(int direction, String number, String name, long ts, long duration) {
		ContentValues values = new ContentValues();
		values.put(CallLogConstants.DIRECTION, direction);
		values.put(CallLogConstants.NUMBER, number);
		values.put(CallLogConstants.NAME, name);
		values.put(CallLogConstants.DATE, ts);
		values.put(CallLogConstants.DURATION, duration);
		
		//if (mContentResolver.update(CallLogProvider.CONTENT_URI, values,
		//		CallLogConstants.NUMBER + "='" + number + "'", null) == 0) {
			Log.v(TAG, "New Call Log Entry Added " + name);
			mContentResolver.insert(CallLogProvider.CONTENT_URI, values);
		//}else {
		//	Log.v(TAG, "Call Log Entry Updated " + name);
		//}
	}
	
	class FetchCurrentCallRate extends AsyncTask<Void, Integer, Void>{
		
		private String costURL = "http://54.225.169.80/webapi_spactron/dist/spactron/playstore-0279821/call_rate.php?called_number=PHONE_NUMBER&app_name=Spactron";
		String mResult = null;
		private String mPhoneNumber;
		
		public FetchCurrentCallRate(String number) {
			mPhoneNumber = number;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			mCallRateView.setText(mResult);
			super.onPostExecute(result);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			HttpClient httpClient = new DefaultHttpClient();
			String URL = costURL.replace("PHONE_NUMBER", mPhoneNumber);
			Log.v(TAG, "URL " + URL);
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
    			String result = stringBuilder.toString();
    			Log.d(TAG, "HttpResponse :" + result);
    			if(result.equals("ERROR")) {
    				Log.e(TAG, "Some kind of error fetching current call rate");
    			} else {
    				String[] res = result.split(",");
    				String countryName = res[3].split(":")[1].substring(1, res[3].split(":")[1].length()-1);
    				String countryCost = res[4].split(":")[1].substring(1, res[4].split(":")[1].length()-2);
    				Log.v(TAG, "countryName " + countryName);
    				Log.v(TAG, "countryCost " + countryCost);
    				mResult = countryName + " : " + countryCost;
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
	
	private String getContactName(Context context, String number) {

		String name = null;

		// define the columns I want the query to return
		String[] projection = new String[] {
		        ContactsContract.PhoneLookup.DISPLAY_NAME,
		        ContactsContract.PhoneLookup._ID};

		// encode the phone number and build the filter URI
		Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

		// query time
		Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

		if(cursor != null) {
			if (cursor.moveToFirst()) {
			    name =  cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			    mPhotoID = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
			    Log.v(TAG, "getContactName: Contact Found @ " + number);            
			    Log.v(TAG, "getContactName: Contact name  = " + name);
			    Log.v(TAG, "getContactName: Contact id  = " + mPhotoID);
			} else {
			    Log.v(TAG, "Contact Not Found @ " + number);
			}
			cursor.close();
		}
		return name;
	}
	
}
