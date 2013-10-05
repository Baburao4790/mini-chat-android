package com.montycall.android.lebanoncall.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnUriUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract.Contacts;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.montycall.android.lebanoncall.CallScreen;
import com.montycall.android.lebanoncall.EmailEntryActivity;
import com.montycall.android.lebanoncall.UserCredentialsActivity;
import com.montycall.android.lebanoncall.constants.Constants;

public class CallService extends Service  {
	/** Logging Tag */
    private static final String TAG = "CallService";
    /** Some key values which could be used by anyone for entering data in Bundles. */
    public static final String k1 = "k1";
	public static final String k2 = "k2";
	public static final String k3 = "k3";
	public static final String k4 = "k4";
	public static final String k5 = "k5";
	public static final String k6 = "k6";
	public static final String k7 = "k7";
	public static String CURRENT_BALANCE = "0";
	public static String SIP_USER = "";
	public static String SIP_EMAIL = "";
	
	public static String SIP_DOMAIN;
	
	private static boolean isCallInProgress = false;
	
	public final static String EXTRAT_SIP_SESSION_ID = "SipSession";
	public final static String CALLED_NUMBER = "Called_Number";
	public final static String CALLED_NAME = "Called_Name";
	private static long mActiveAudioSessionId = -1;
	
	
	/** The possible states in which this ChatService could wander */
	/** Typical states once the user is registered - 
	 * UNKNOWN->DISCONNECTED->CONNECTED->LOGGING_IN->LOGGED_IN
	 * For the very first time, this is what should happen
	 * UNKNOWN->DISCONNECTED->WAITING_FOR_EMAIL->SENDING_EMAIL_ID->WAITING_FOR_CODE->REGISTERIING
	 * ->LOGGING_IN->LOGGED_IN
	 */
	public static final int STATE_UNKNOWN = 200;
    public static final int STATE_DISCONNECTED = 201;
    public static final int STATE_CONNECTED = 202;
    public static final int STATE_WAITING_FOR_USER_EMAIL = 203;
    public static final int STATE_SENDING_EMAIL_ID = 204;
    public static final int STATE_WAITING_FOR_VERIFICATION_CODE= 205;
    public static final int STATE_REGISTERING = 206;
    public static final int STATE_LOGGING_IN = 207;
    public static final int STATE_LOGGED_IN = 208;
    public static final int STATE_LOGGED_OUT = 209;
    
    /** The broadcast receiver for receiving Connectivity Broadcasts */
    private MyReceiver mReceiver;
    
    /** Have we fetched phone contacts before */
    private boolean doWeHavePhoneContacts = false;
    
    /** Is WiFi network available for use */
    private boolean mIsWifiAvailable;
    
    /** Is Cellular Data Network available for use */
    private boolean mIsMobileDataAvailable;
    
    /** The instance of singleton {@link com.montycall.android.callLebanon.XmppClient}*/
    private static NgnEngine mEngine;
	private static INgnConfigurationService mConfigurationService;
	private static INgnSipService mSipService;
    
    /** The list of all the phone contacts that are there on this phone */
    private static PhoneContacts sPhoneContacts;
    
    /** Content Observer to be used if the data changes at anytime */
    private PhoneContactsObserver mPhoneContactsObserver;
    
    /** Target we publish for clients to send messages to Incoming Handler. */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /** The current state */
    private  AtomicInteger mState = new AtomicInteger(STATE_DISCONNECTED);
    
    /** Keeps track of all current registered clients. */
    private ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    
    /** The notification type in the Status Bar */
    public static final int MSG_NOTIFICATION = 9901;
    public static final int SUB_NOTIFICATION = 9902;
    
    /** Message type to register some client to receive callbacks from this service */
    public static final int MSG_REGISTER_CLIENT = 1;

    /** Message type to unregister from getting callbacks from this service */
    public static final int MSG_UNREGISTER_CLIENT = 2;

    /** This could either be sent to the service to ask for state information 
     * or could be broadcasted by the service to everyone if the state changes.
     */
    public static final int MSG_STATE_INFO = 3;
    
    /** You can change the state of the service from outside using this message type */
    public static final int MSG_SET_STATE = 4;
    
    /** Start the phone verification request */
    public static final int MSG_START_EMAIL_REGISTRATION = 5; 
    
    /** Register for Monty Chat  */
    public static final int MSG_REGISTER = 6;
    
    /** Login into Monty Chat */
    public static final int MSG_LOG_IN = 7;
    
    /** The first activity has come up. This means that some one has tried to open
     * the application from launcher.
     */
    public static final int MSG_FIRST_ACTIVITY = 12;
    
    /** Log out from Monty Chat Server */
    public static final int MSG_LOG_OUT = 13;
    
    /** Delete this account from this phone. Make this phone unverified */
    public static final int MSG_DELETE_ACCOUNT = 14;
    
    public static final int MSG_PHONE_CONTACTS_FETCHED = 22;
    
    public static final int MSG_LOGIN_TIMEOUT = 25;
    
    public static final int MSG_MAKE_CALL = 26;
    
    public static final int MSG_CHECK_BALANCE = 27;
    
    public static final int MSG_BALANCE_FETCHED = 28;
    
    public static final int MSG_CALL_ENDED = 29;
    
	public static final int MSG_REGISTER_EMAIL = 30;
	
	public static final int MSG_VERIFY_EMAIL = 31;
    
    public static AtomicInteger activityCount = new AtomicInteger(0);
    
    /** Handler of incoming messages from clients. */
    @SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UNREGISTER_CLIENT:
                    Log.v(TAG, "MSG_UNREGISTER_CLIENT");
                    mClients.remove(msg.replyTo);
                    break;

                case MSG_REGISTER_CLIENT:
                    Log.v(TAG, "MSG_REGISTER_CLIENT");
                    mClients.add(msg.replyTo);
                    /* Intentionally avoided break statement
                     * so that whenever someone registers, they get the state
                     */
                case MSG_STATE_INFO:
                    Log.v(TAG, "MSG_STATE_INFO");
                    sendMessageToClients(MSG_STATE_INFO, getState(), 0, null);
                    break;

                case MSG_SET_STATE:
                	Log.v(TAG, "MSG_SET_STATE : " + msg.arg1);
                	if(getState() != msg.arg1) {
                		Log.v(TAG, "Setting State to " + msg.arg1);
                		setState(msg.arg1);
                	}
                	break;

                case MSG_START_EMAIL_REGISTRATION: {
                	Log.v(TAG, "MSG_START_EMAIL_REGISTRATION");
                	Bundle bundle = (Bundle)msg.obj;
                	String email = bundle.getString(k1);
                	EmailSender emailSender = new EmailSender(getApplicationContext(), email);
                	emailSender.start();
                }
                	break;
                
                case MSG_REGISTER :
                	Log.v(TAG, "MSG_REGISTER");
                	setState(STATE_REGISTERING);
					UserRegister userRegister = new UserRegister();
					userRegister.start();
                	break;
                	
                case MSG_LOG_IN:
                	Log.v(TAG, "MSG_LOG_IN");
                	setState(STATE_LOGGING_IN);
                	break;
                	
                case MSG_LOG_OUT:
                	Log.v(TAG, "MSG_LOG_OUT");
                	setState(STATE_LOGGED_OUT);
                	break;
                	
                case MSG_FIRST_ACTIVITY:
                	Log.v(TAG, "MSG_FIRST_ACTIVITY");
                	if(isCallInProgress) {
                		Intent i = new Intent(getApplicationContext(), CallScreen.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getApplicationContext().startActivity(i);
            			break;
                	} else {
                		setConnectionState();
                    	switch (getState()) {
                    		case STATE_LOGGED_OUT:
                    			break;
                    		case STATE_LOGGING_IN:
                    			break;
                    		case STATE_LOGGED_IN:
                    			break;
                    		case STATE_DISCONNECTED:
                    			break;
                    		case STATE_CONNECTED:
                    			/* Why the hell are we stuck at connected state?? */
                    			if(UtilityMethods.haveWeRegisteredBefore(getApplicationContext())) {
                    				setState(STATE_LOGGING_IN);
                                	/** Now we need to login */
                                	//Log.v(TAG, "Starting Login");
                    				//mSipService.register(getApplicationContext());
                    				RegisterToSipServer rgs = new RegisterToSipServer(getApplicationContext());
                    				rgs.start();
                                } else {
                                	setState(STATE_WAITING_FOR_USER_EMAIL);
                                	Intent i3 = new Intent(getApplicationContext(), EmailEntryActivity.class);
                                    i3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    getApplicationContext().startActivity(i3);
                                }
                    			break;
                    		case STATE_WAITING_FOR_USER_EMAIL:
                    			Intent i3 = new Intent(getApplicationContext(), EmailEntryActivity.class);
                                i3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                getApplicationContext().startActivity(i3);
                    			break;
                    		case STATE_SENDING_EMAIL_ID:
                    		case STATE_WAITING_FOR_VERIFICATION_CODE:
                    			Intent i4 = new Intent(getApplicationContext(), UserCredentialsActivity.class);
                                i4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                getApplicationContext().startActivity(i4);
                    			break;
                    		case STATE_REGISTERING:
                    			break;
                    		
                    		default:
                    				break;
                    			
                    	}
                	}
                	
                	
                	break;
                	
                case MSG_PHONE_CONTACTS_FETCHED:
                	Log.v(TAG, "MSG_PHONE_CONTACTS_FETCHED");
                	doWeHavePhoneContacts = true;
                	break;

                case MSG_MAKE_CALL:
                	Log.v(TAG, "MSG_MAKE_CALL");
                	Bundle bundle = (Bundle)msg.obj;
                	String phoneNumber = bundle.getString(k1);
                	String name = bundle.getString(k2);
                	boolean flag = makeVoiceCall(phoneNumber, name);
                	sendMessageToClients(MSG_MAKE_CALL, 0, 0, flag);
                	break;
                	
                case MSG_CHECK_BALANCE:
                	Log.v(TAG, "MSG_CHECK_BALANCE");
                	CheckBalance cb = new CheckBalance();
                	cb.start();
                	break;
                	
                case MSG_DELETE_ACCOUNT:
                	if(mSipService.isRegistered()) {
                		mSipService.unRegister();
                	}
                	mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, null);
                	mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, null);
                	mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, null);
                	mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST, null);
                	mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, null);
                	mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, null);
                	mConfigurationService.commit();
                	setState(STATE_WAITING_FOR_USER_EMAIL);
                	break;
                	
                case MSG_CALL_ENDED:
                	isCallInProgress = false;
                	break;
                
                case MSG_REGISTER_EMAIL:
                	Bundle verifybundle = (Bundle)msg.obj;
                	String email = verifybundle.getString(k1);
                	mClients.add(msg.replyTo);
                	RegisterEmail registeremail = new RegisterEmail(email);
                	registeremail.start();
                	break;
                	
                case MSG_VERIFY_EMAIL:
                	bundle = (Bundle)msg.obj;
                	String regCode = bundle.getString(k1);
                	mClients.add(msg.replyTo);
                	VerifyEmail verifyEmail = new VerifyEmail(regCode);
                	verifyEmail.start();
                	break;
                	
                default:
                    super.handleMessage(msg);
            }
        }				
    }

    /** This is called when the service starts. The application has just started 
     * so we do not know anything yet. We register for appropriate events and wait for
     * things to settle down.
     */
    @Override
	public void onCreate() {
		Log.v(TAG, "onCreate ");
		this.startForeground(0, null);
		mEngine = NgnEngine.getInstance();
		mConfigurationService = mEngine.getConfigurationService();
		mSipService = mEngine.getSipService();

		if (!mEngine.isStarted()) {
			mEngine.start();
		}

		/** We are in STATE_DISCONNECTED when we start. Let it be like that for now */
        /** Register for the broadcast receiver which will get connectivity intents */
		mReceiver = new MyReceiver();
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		iFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
		registerReceiver(mReceiver, iFilter);
		/**
		 * Start fetching the phone contacts. By the time we need them, this
		 * thread would be done
		 */
		sPhoneContacts = PhoneContacts.getInstance(getApplicationContext());
		mPhoneContactsObserver = new PhoneContactsObserver(new Handler());
		getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true,
				mPhoneContactsObserver);
	}

    /** Just unregister the broadcast receiver registered earlier to prevent leaks */
    @Override
    public void onDestroy() {
    	unregisterReceiver(mReceiver);
    	if(mEngine.isStarted()) {
			mEngine.stop();
		}
    	super.onDestroy();
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
    	Log.v(TAG, "---------------onBind----------------");
        return mMessenger.getBinder();
    }
    
    /** User within this service to send a message to all register clients. */
	private void sendMessageToClients(int what, int arg1, int arg2, Object obj) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			Log.d(TAG, "sendMessageToClients  " + what);
			try {
				if (obj == null) {
					mClients.get(i)
							.send(Message.obtain(null, what, arg1, arg2));
				} else {
					mClients.get(i).send(Message.obtain(null, what, obj));
				}
			} catch (RemoteException e) {
				// The client is dead. Remove it from the list;
				// we are going through the list from back to front
				// so this is safe to do inside the loop.
				mClients.remove(i);
			}
		}
	}
    
	/** Set the current state. Let everyone know about it by broadcasting to clients */
	public synchronized void setState(int state) {
		if (mState.get() != state) {
			mState.set(state);
			sendMessageToClients(MSG_STATE_INFO, state, 0, null);
			if (getState() == STATE_DISCONNECTED) {
				mSipService.unRegister();
			} else if (getState() == STATE_LOGGED_IN) {
				CheckBalance cb = new CheckBalance();
				cb.start();
			}
		}
	}
    /** Get the current state */
    public synchronized int getState() {
    	return mState.get();
    }
    
    /** Set the current state based on available connectivity */
    synchronized void setConnectionState() {
    	ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		boolean wasMobileDataAvailable = mIsMobileDataAvailable;
		
		if(wifiNetworkInfo.isConnected()) {
			mIsWifiAvailable = true;
			Log.v(TAG, "Wi-Fi is now available");
		} else {
			mIsWifiAvailable = false;
			Log.v(TAG, "Wi-Fi is now UNavailable");
		}
		
		if((mobileNetworkInfo != null) && (mobileNetworkInfo.isConnected())) {
			mIsMobileDataAvailable = true;
			Log.v(TAG, "Mobile Data is now available");
		} else {
			mIsMobileDataAvailable = false;
			Log.v(TAG, "Mobile Data is now UNavailable");
		}
		
		if (mIsMobileDataAvailable || mIsWifiAvailable) {
			if (getState() == STATE_DISCONNECTED) {
				setState(STATE_CONNECTED);
				if (UtilityMethods.haveWeRegisteredBefore(getApplicationContext())) {
					setState(STATE_LOGGING_IN);
					/** Now we need to login */
					Log.v(TAG, "Starting Login");
					RegisterToSipServer rgs = new RegisterToSipServer(getApplicationContext());
					rgs.start();
					//mSipService.register(getApplicationContext());
				}
			} else {
				/** Earlier we we were not disconnected and we have a network change */
				if(wasMobileDataAvailable) {
					if(mIsWifiAvailable) {
						setState(STATE_CONNECTED);
						if (UtilityMethods.haveWeRegisteredBefore(getApplicationContext())) {
							setState(STATE_LOGGING_IN);
							RegisterToSipServer rgs = new RegisterToSipServer(getApplicationContext());
							rgs.start();
							//mSipService.register(getApplicationContext());
						}
					}
				}
			}
		} else {
			setState(STATE_DISCONNECTED);
		}
    }
    
    /** The Broadcast receiver for receiving connectivity events from Android */
	class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					ConnectivityManager.CONNECTIVITY_ACTION)) {
				setConnectionState();
			} else if (NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT
					.equals(intent.getAction())) {

				NgnRegistrationEventArgs args = intent
						.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
				if (args == null) {
					Log.e(TAG, "Invalid event args");
					return;
				}
				switch (args.getEventType()) {
				case REGISTRATION_NOK:
					Log.v(TAG, "REGISTRATION_NOK");
					// mTvInfo.setText("Failed to register :(");
					break;
				case UNREGISTRATION_OK:
					Log.v(TAG, "UNREGISTRATION_OK");
					// mTvInfo.setText("You are now unregistered :)");
					break;
				case REGISTRATION_OK:
					Log.v(TAG, "REGISTRATION_OK");
					setState(STATE_LOGGED_IN);
					CheckBalance cb = new CheckBalance();
					cb.start();
					// mTvInfo.setText("You are now registered :)");
					break;
				case REGISTRATION_INPROGRESS:
					Log.v(TAG, "REGISTRATION_INPROGRESS");
					// mTvInfo.setText("Trying to register...");
					break;
				case UNREGISTRATION_INPROGRESS:
					Log.v(TAG, "UNREGISTRATION_INPROGRESS");
					// mTvInfo.setText("Trying to unregister...");
					break;
				case UNREGISTRATION_NOK:
					Log.v(TAG, "UNREGISTRATION_NOK");
					// mTvInfo.setText("Failed to unregister :(");
					break;
				}

			}
		}
	}    
    
    /** The phone verification request sender */
    class EmailSender extends Thread {
    	private static final String TAG = "EmailSender";
    	
    	private Context mContext;
    	private String mEmail;
    	private static final String emailSenderURL = "http://54.225.169.80/webapi_spactron/dist/spactron/playstore-0279821/email_verify.php?user_email=EMAIL_ADDRESS&app_name=Spactron&app_platform=Android&app_version=1.0.1.1000";
        private String result = "";
        
        public EmailSender(Context context, String email) {
    		mContext = context;
    		mEmail = email;
    		SavePreferences(Constants.MONTYCALL_USER_EMAIL, mEmail);
    	}
        
        private void SavePreferences(String key, String value) {
			SharedPreferences sharedPreferences = getSharedPreferences(
					Constants.PREFS_NAME, 0);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(key, value);
			editor.commit();
		}
        
        @Override
    	public void run() {
        	setState(STATE_SENDING_EMAIL_ID);
    		HttpClient httpClient = new DefaultHttpClient();
    		String url = emailSenderURL.replace("EMAIL_ADDRESS", mEmail);
    		Log.v(TAG, "email url " + url);
     		HttpGet httpGet = new HttpGet(url);
    		//String user_pass = Constants.HTTP_USERNAME + ":" + Constants.HTTP_PASSWORD;
    		//httpGet.setHeader("Authorization", "Basic " + Base64.encodeToString(user_pass.getBytes(), Base64.NO_WRAP));
    		
    		try {
    			Log.d(TAG, "Sending Email Address");
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
    	
    		} catch (ClientProtocolException cpe) {
    			//Log.e(TAG, "Exception generates because of httpResponse :" + cpe);
    			cpe.printStackTrace();
    		} catch (IOException ioe) {
    			//Log.e(TAG, "Exception generates because of httpResponse :" + ioe);
    			ioe.printStackTrace();
    		}
    		
    		if(result.equalsIgnoreCase("true")){
    			Log.v(TAG, "Opening User credential");
    			setState(STATE_WAITING_FOR_VERIFICATION_CODE);
    			//SavePreferences(Constants.PHONE_VERIFIED, true);
    			Intent intent = new Intent(mContext, UserCredentialsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
    		}else {
    			setState(STATE_CONNECTED);
    		}
    	}
    }

    /** This thread registers the user to Monty Chat Server */
	class UserRegister extends Thread {
		private static final String userCredentialsUrl_1 = "http://54.243.233.222/smartApp/rest/script_dev.php?" +
				"restScript=spactronRegDevice&" +
				"appKey=ORIG_APP_KEY&" +
				"deviceName=DEVICE_NAME&" +
				"deviceIdentifier=DEVICE_ID&" +
				"deviceSystemVersion=SYSTEM_VERSION&" +
				"deviceModel=DEVICE_MODEL&" +
				"deviceLang=DEVICE_LANG&" +
				"deviceDisplay=DEVICE_DISPLAY";
	
		private static final String userCredentialsUrl_2 = "http://54.243.233.222/smartApp/rest/script_dev.php?" +
				"restScript=spactronRegUser&" +
				"appKey=NEW_APP_KEY";

		SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);

		private String appKey = "b6d767d2f8ed5d21a44b0e5886680cb9";
		private String secretKey = "8deb7b20d7832fa006de5f7fa1eccac1";
		private String installationKey;
		private String newAppKey;
		private String deviceName;
		private String deviceIdentifier;
		private String deviceSystemVersion;
		private String deviceModel;
		private String deviceLang;
		private String deviceDisplay;
		private String sipDomain;
		private String userID;
		private String password;
		private String countryCode;

		private String result_1 = null;
		private String result_2 = null;

		public UserRegister() {
			deviceIdentifier = (String) getDeviceID();
			deviceName = getDeviceName();
			deviceModel = Build.MODEL;
			deviceDisplay = (String) getDisplay();
			deviceLang = Locale.getDefault().getDisplayLanguage();
			deviceSystemVersion = Build.VERSION.RELEASE;
		}
		
		@Override
		public void run() {
			setState(STATE_REGISTERING);
			Log.d(TAG, "UserRegister : Started");
			
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used. 
			int timeoutConnection = 5000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 15000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters); 
			String url_1 = userCredentialsUrl_1.replace("ORIG_APP_KEY", appKey).replace("DEVICE_NAME", deviceName).replace("DEVICE_ID", deviceIdentifier)
					.replace("SYSTEM_VERSION", deviceSystemVersion).replace("DEVICE_MODEL", deviceModel).replace("DEVICE_LANG", deviceLang).replace("DEVICE_DISPLAY", deviceDisplay);
			Log.v(TAG, "url_1 = " + url_1);
			HttpGet httpGet_1 = new HttpGet(url_1);
			
			try {
				HttpResponse httpResponse = httpClient.execute(httpGet_1);
				InputStream inputStream = httpResponse.getEntity().getContent();
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				StringBuilder stringBuilder = new StringBuilder();
				String bufferedStrChunk = null;
				while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
					stringBuilder.append(bufferedStrChunk);
				}
				result_1 = stringBuilder.toString();
			} catch (ClientProtocolException cpe) {
				Log.e(TAG, "Exception generated because of httpResponse :"+ cpe);
    			setState(STATE_WAITING_FOR_VERIFICATION_CODE);
			} catch (IOException e) {
				Log.e(TAG, "IO Exception while HTTP REquest:");
				e.printStackTrace();
    			setState(STATE_WAITING_FOR_VERIFICATION_CODE);
			}try {
				if(result_1 != null) {
					Log.v(TAG, "result 1: " + result_1);				
					JSONObject jsonObject = new JSONObject(result_1);
					if ((jsonObject.getBoolean("restResponse"))) {
					
						String device_key = jsonObject.getString("restMessage");						
						installationKey = device_key;
						
						Log.v(TAG, "Registration Complete, now going for login");
						newAppKey = getStringMD5(secretKey + installationKey + appKey);
						SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putString(Constants.APP_KEY_Hashed, newAppKey);
						editor.commit();
						
						
						String url_2 = userCredentialsUrl_2.replace("NEW_APP_KEY", newAppKey);
						Log.v(TAG, "url_2 = " + url_2);
						HttpGet httpGet_2 = new HttpGet(url_2);
					
						HttpResponse httpResponse = httpClient.execute(httpGet_2);
						InputStream inputStream = httpResponse.getEntity().getContent();
						InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
						BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
						StringBuilder stringBuilder = new StringBuilder();
						String bufferedStrChunk = null;
						while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
							stringBuilder.append(bufferedStrChunk);
						}
						result_2 = stringBuilder.toString();
					
						if(result_2 != null) {
							Log.v(TAG, "result 2: " + result_2);
							UserLogin();
						} else {
							Log.v(TAG, "Registration NOT Complete");
			    			setState(STATE_WAITING_FOR_VERIFICATION_CODE);
						}
					} else {
						Log.v(TAG, "Registration NOT Complete");
		    			setState(STATE_WAITING_FOR_VERIFICATION_CODE);	    			
					} 
				
				
				}else {
					
				}
			} catch (ClientProtocolException cpe) {
				Log.e(TAG, "Exception generated because of httpResponse :"+ cpe);
    			setState(STATE_WAITING_FOR_VERIFICATION_CODE);
			} catch (IOException e) {
				Log.e(TAG, "IO Exception while HTTP REquest:");
				e.printStackTrace();
    			setState(STATE_WAITING_FOR_VERIFICATION_CODE);
			} catch (JSONException e) {
				setState(STATE_WAITING_FOR_VERIFICATION_CODE);
				e.printStackTrace();
			} finally {
				//response to user
			}
			
		}

		private void UserLogin() throws JSONException {
			JSONObject jsonObject = new JSONObject(result_2);
			if ((jsonObject.getBoolean("restResponse"))) {
				
				String[] res = jsonObject.getString("restMessage").split("#");
				String userID = res[0];
				String password = res[1];
				String domain = res[2];
				sipDomain = domain;
				
				Log.v(TAG, "Logging In");
				/** Lets get the default country and save it */
				GetDefaultCountry gdc = new GetDefaultCountry(getApplicationContext());
				gdc.start();
				
				setState(STATE_LOGGING_IN);
				SavePreferences(Constants.PHONE_VERIFIED, true);

				SIP_DOMAIN = sipDomain;
				mConfigurationService.putString(NgnConfigurationEntry.NETWORK_TRANSPORT, "tcp");
				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, userID);
				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, password);
				mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_WIFI, true);
				mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_3G, true);
				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, String.format("sip:%s@%s", userID, sipDomain));
				mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST, sipDomain);
				mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_INTERCEPT_OUTGOING_CALLS, false);
				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, userID);
				mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, sipDomain);
				mConfigurationService.putInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT, 5060);
				mConfigurationService.commit();
				RegisterToSipServer rgs = new RegisterToSipServer(getApplicationContext());
				rgs.start();
			} else {
				Log.v(TAG, "Registration NOT Complete");
				setState(STATE_WAITING_FOR_VERIFICATION_CODE);
			}
		}
		
		private CharSequence getDisplay() {
			
			try{
				WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				String formatter = "%dx%dx%d";
				String display_dimension = String.format(formatter, display.getWidth(), display.getHeight(), display.getOrientation());
				return display_dimension;
			}catch(Exception e){
				return "100x100x2";
			}
			
		}
		
		public String getDeviceName() {
			try{
			  String manufacturer = Build.MANUFACTURER;
			  String model = Build.MODEL;
			  if (model.startsWith(manufacturer)) {
			    return model.replace(" ", "_");
			  } else {
			    return (manufacturer + "-" + model).replace(" ", "_");
			  }
		}catch(Exception e){
			return "Device_name";
		}
    }
		
		private CharSequence getDeviceID() {			
				try{
					TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
					String deviceId = telephonyManager.getDeviceId();
					if (deviceId != null)
					{
						deviceId = getStringMD5(deviceId);
						return deviceId;
					} else {
						return "na";
					}
					
				}catch(Exception e){
					return "na";
				}
			}		
		
		private String getStringMD5(String in) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.reset();
			digest.update(in.getBytes());
			byte[] a = digest.digest();
			int len = a.length;
			StringBuilder sb = new StringBuilder(len << 1);
			for (int i = 0; i < len; i++) {
				sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
				sb.append(Character.forDigit(a[i] & 0x0f, 16));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "na";
	}
		
		private void SavePreferences(String key, Boolean value) {
			SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean(key, value);
			editor.commit();
		}
		
	}
	
	private class PhoneContactsObserver extends ContentObserver {
		public PhoneContactsObserver(Handler handler) {
			super(handler);
		}
		public void onChange(boolean selfChange) {
			if((sPhoneContacts != null) && (doWeHavePhoneContacts)) {
				doWeHavePhoneContacts = false;
				sPhoneContacts.fetchContacts();
			}
		}
	}
	
	boolean makeVoiceCall(String pNumber, String name){
		if(!isCallInProgress) {
			if(mSipService.isRegistered()) {
				String phoneNumber = pNumber;
				if(pNumber.contains("+")) {
					phoneNumber = pNumber.replace("+", "00");
				} else if(!pNumber.startsWith("0")&& pNumber.length()>10) {
					phoneNumber = "00" + pNumber;
				}	else if((pNumber.startsWith("0")) &&  (!pNumber.startsWith("00"))) {
					SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
					String countryCode = settings.getString(Constants.MONTY_CHAT_USER_COUNTRY_CODE, "");
					phoneNumber = "00" + countryCode + pNumber.substring(1);
				} else if(pNumber.length()<=10) {
					SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
					String countryCode = settings.getString(Constants.MONTY_CHAT_USER_COUNTRY_CODE, "");
					phoneNumber = "00" + countryCode + pNumber;
				}
				
				final String validUri = NgnUriUtils.makeValidSipUri(String.format("sip:%s@%s", phoneNumber, SIP_DOMAIN));
				if(validUri == null){
					Log.e(TAG, "failed to normalize sip uri '" + phoneNumber + "'");
					return false;
				}
				isCallInProgress = true;
				Log.v(TAG, "calling " + validUri);
				NgnAVSession avSession = NgnAVSession.createOutgoingSession(mSipService.getSipStack(), NgnMediaType.Audio);
				mActiveAudioSessionId = avSession.getId();
				Intent i = new Intent();
				i.setClass(this, CallScreen.class);
				i.putExtra(EXTRAT_SIP_SESSION_ID, mActiveAudioSessionId);
				i.putExtra(CALLED_NUMBER, phoneNumber);
				i.putExtra(CALLED_NAME, name);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
				return avSession.makeCall(validUri);
			} else {
				RegisterToSipServer rgs = new RegisterToSipServer(getApplicationContext());
				rgs.start();
				//mSipService.register(getApplicationContext());
				return false;
			}
		} else {
			return false;
		}
	}
	
	class CheckBalance extends Thread {
		
		private String balanceUrl = "http://54.225.169.80/webapi_spactron/dist/spactron/playstore-0279821/mobile_balance.php?user_mobile=%s&user_pass=%s";
		private String result = null;
		@Override
		public void run() {
			String userName = mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, "");

			String password = mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_PASSWORD, "");
			balanceUrl = String.format(balanceUrl, userName, password);
			HttpClient httpClient = new DefaultHttpClient();
    		HttpGet httpGet = new HttpGet(balanceUrl);
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
    			//Log.d(TAG, "HttpResponse :" + stringBuilder.toString());
    	
    			if (!result.equalsIgnoreCase("ERROR")) {
    				CURRENT_BALANCE = result;
    				
    				SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
    				SIP_USER = sharedPreferences.getString(Constants.USER_FIRST_NAME, "");
    				SIP_EMAIL = sharedPreferences.getString(Constants.SIP_EMAIL, ""); 
    				SharedPreferences.Editor editor = sharedPreferences.edit();
    				editor.putString(Constants.CURRENT_BALANCE, result);
    				editor.commit();
    				sendMessageToClients(MSG_BALANCE_FETCHED, 0, 0, result);
    			}
    			
    		} catch (ClientProtocolException cpe) {
    			//Log.e(TAG, "Exception generates because of httpResponse :" + cpe);
    			cpe.printStackTrace();
    		} catch (IOException ioe) {
    			//Log.e(TAG, "Exception generates because of httpResponse :" + ioe);
    			ioe.printStackTrace();
    		}	
		}
	}
	
	public static String getSipUserName() {
		return mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, "");
	}
	
	public static long getActiveSessionId() {
		return mActiveAudioSessionId;
	}
	
	static class RegisterToSipServer extends Thread {
		
		private Context mContext;
		
		public RegisterToSipServer(Context ctx) {
			mContext = ctx;
		}
		
		@Override
		public void run() {
			boolean result = mSipService.register(mContext);
			if(!result){			
				Log.e(TAG, "Cannot initialize the sipservice");
				System.exit(1);
			} else {
				Log.v(TAG, "SipService initialized correctly");
			}
		}
	}
	
	private class RegisterEmail extends Thread{
		private final String RegisterEmailURL = "http://54.243.233.222/smartapp/rest/script_dev.php?restScript=spactronRegEmail&regEmail=%s&appKey=%s";
		
		private String email = null;
		private String result;
		private String appKey;
		URI url = null;
		private String regCode;
		private boolean emailVerified = false;
		private boolean emailRegistered = false;
		
		public RegisterEmail(String email) {
			this.email = email;
			//prepare register email url;
			SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, 0);
			appKey = prefs.getString(Constants.APP_KEY_Hashed, "");
			url = URI.create(String.format(RegisterEmailURL, email, appKey));
		}
		
		@Override
		public void run() {
			if(this.email != null){
				HttpParams httpParameters = new BasicHttpParams();
				int timeoutConnection = 5000;
				HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				int timeoutSocket = 15000;
				HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
				DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
				HttpGet httpGet = new HttpGet(url);
				HttpResponse httpResponse;
				try {
					httpResponse = httpClient.execute(httpGet);
					InputStream inputStream = httpResponse.getEntity().getContent();
					InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
					StringBuilder stringBuilder = new StringBuilder();
					String bufferedStrChunk = null;
					while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
						stringBuilder.append(bufferedStrChunk);
					}
					result = stringBuilder.toString();
					try {
						JSONObject json = new JSONObject(result);
						if(json.getBoolean("restResponse")){
							emailRegistered = true;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					sendMessageToClients(MSG_REGISTER_EMAIL, emailRegistered? 1:0, 0, null);
				}
			}
		}
	}

	private class VerifyEmail extends Thread{
	
		private final String VerifyEmailURL = "http://54.243.233.222/smartapp/rest/script_dev.php?restScript=spactronVerifyEmail&regCode=%s&appKey=%s";
		private String regCode;
		private String appKey;
		private String result;
		private boolean emailVerified;
		
		public VerifyEmail(String regcode) {
			this.regCode = regcode;
			SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, 0);
			this.appKey = prefs.getString(Constants.APP_KEY_Hashed, "");
			 
		}
		
		
		@Override
		public void run() {
			doVerifyEmail();
		}
		
		private void doVerifyEmail() {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			String uri = String.format(VerifyEmailURL, regCode, appKey);
			HttpGet httpGet = new HttpGet(uri);
			HttpResponse response;
			try {
				response = httpClient.execute(httpGet);
				InputStream inputStream = response.getEntity().getContent();
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				StringBuilder builder = new StringBuilder();
				String bufferedStrChunk = null;
				while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
					builder.append(bufferedStrChunk);
				}
				result = builder.toString();
				try {
					JSONObject json = new JSONObject(result);
					if(json.getBoolean("restResponse")){
						emailVerified = true;						
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				sendMessageToClients(MSG_REGISTER_EMAIL, emailVerified? 1:0, 0, null);
			}
		}
	}
	
}
