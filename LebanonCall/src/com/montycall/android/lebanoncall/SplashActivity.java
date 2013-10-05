package com.montycall.android.lebanoncall;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.montycall.android.lebanoncall.constants.Constants;
import com.montycall.android.lebanoncall.service.CallService;
import com.montycall.android.lebanoncall.service.UtilityMethods;

public class SplashActivity extends Activity {
	private static final String TAG = SplashActivity.class.getName();

	private static boolean isRunning = true;
	private boolean displayToast_Splash = false;

	/** Start of Service Related Processing and messaging */

	private int mServiceState = CallService.STATE_DISCONNECTED;
	
	private static boolean waitIsOver = false;

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	Messenger mChatService;
	boolean mIsBound;
	static private ImageView splashview = null;
	private static int MSG_DELAY = 1984;
	
	static private enum Action { one, two, three, four, five, six, seven, eight, nine};
	static private Action[] action = Action.values();
	//static public TextView tv = null;
	
	 Handler updater = new Handler() {
		 @Override
		  public void handleMessage(android.os.Message msg) {
			 if(msg.what == MSG_DELAY) {
				 /* If we are logged in, we start the xmpplogin activity. else we wait */
				 synchronized (SplashActivity.class) {
						if(waitIsOver) {
							Intent i = new Intent(getApplicationContext(), MainTabActivity.class);
		        			Bundle b = new Bundle();
		        	        b.putString(MainTabActivity.DEFAULT_TAB, "friends");
		        	        i.putExtras(b);
		                	//i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
		                	SplashActivity.this.startActivity(i);
							finish();
						} else {
							waitIsOver = true;
						}
					}
			 } else {
				 switch (action[msg.what]) {
			      case one:
			    	  splashview.setImageResource(R.drawable.splashscreen1);
			    	  sendMessageDelayed(obtainMessage(Action.two.ordinal(), 0, 0), 800);
			    	  break;
			      }
			 }
	      }
	  };


	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CallService.MSG_STATE_INFO:
				switch (msg.arg1) {
				case CallService.STATE_DISCONNECTED:
					Log.v(TAG, "ChatService.STATE_DISCONNECTED");
					synchronized (SplashActivity.class) {
						if (isRunning) {
							if(UtilityMethods.haveWeRegisteredBefore(getApplicationContext())) {
								if(waitIsOver) {
									Intent i = new Intent(getApplicationContext(), MainTabActivity.class);
				        			Bundle b = new Bundle();
				        	        b.putString(MainTabActivity.DEFAULT_TAB, "friends");
				        	        i.putExtras(b);
				                	SplashActivity.this.startActivity(i);
									finish();
								} else {
									waitIsOver = true;
								}
							}else {
								showNetworkDialog();
							}
						}
					}
					break;

				case CallService.STATE_CONNECTED:
					Log.v(TAG, "ChatService.STATE_CONNECTED");
					break;

				case CallService.STATE_WAITING_FOR_USER_EMAIL:
					Log.v(TAG, "ChatService.STATE_WAITING_FOR_USER_NUMBER");
					//finish();
					break;

				case CallService.STATE_SENDING_EMAIL_ID:
					Log.v(TAG,"ChatService.STATE_WAITING_FOR_PHONE_VERIFICATION");
					//finish();
					break;

				case CallService.STATE_WAITING_FOR_VERIFICATION_CODE:
					Log.v(TAG, "ChatService.STATE_WAITING_FOR_USER_CREDENTIALS");
					if(displayToast_Splash) {
						Toast.makeText(SplashActivity.this, "User Registration Failed! Please Retry " ,Toast.LENGTH_LONG).show();
						displayToast_Splash = false;
					}
					finish();
					break;

				case CallService.STATE_REGISTERING:
					Log.v(TAG, "ChatService.STATE_REGISTERING");
					//finish();
					break;

				case CallService.STATE_LOGGED_IN:
					Log.v(TAG, "ChatService.STATE_LOGGED_IN");
					if(mServiceState!=CallService.STATE_LOGGED_IN) {
						synchronized (SplashActivity.class) {
							if(waitIsOver) {
								Intent i = new Intent(getApplicationContext(), MainTabActivity.class);
			        			Bundle b = new Bundle();
			        	        b.putString(MainTabActivity.DEFAULT_TAB, "dialpad");
			        	        i.putExtras(b);
			                	SplashActivity.this.startActivity(i);
								finish();
							} else {
								waitIsOver = true;
							}
						}
					}
					break;

				}
				mServiceState = msg.arg1;
				
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mChatService = new Messenger(service);
			mIsBound = true;
			sendMessageToService(CallService.MSG_REGISTER_CLIENT, 0, 0, null);
			/* Temp Registration Changes */
			if(isRunning) {
				if(!UtilityMethods.haveWeRegisteredBefore(getApplicationContext())) {
					sendMessageToService(CallService.MSG_REGISTER, 0, 0, null);
					Toast.makeText(SplashActivity.this, "Initiated User Registration ", 
			           		Toast.LENGTH_SHORT).show();
					displayToast_Splash=true;
				}
			}
			sendMessageToService(CallService.MSG_FIRST_ACTIVITY, 0, 0, null);
		}

		public void onServiceDisconnected(ComponentName className) {
			mChatService = null;
		}
	};

	void doBindService() {
		Intent service = new Intent(this, CallService.class);
		startService(service);
		getApplicationContext().bindService(new Intent(this, CallService.class), mConnection,
				Context.BIND_AUTO_CREATE);
	}

	public void showNetworkDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please Check Network Settings")
				.setCancelable(false)
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						finish();
					}
				});
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
		if (mIsBound) {
			Message msg = Message.obtain(null, what);
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//Log.v(TAG, "s1");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		splashview = (ImageView) findViewById(R.id.splashview);

		DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        String str_ScreenSize = "" + dm.widthPixels + "x" + dm.heightPixels;

        SharedPreferences sharedPreferences = getSharedPreferences(
				Constants.PREFS_NAME, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(Constants.DISPLAY_SIZE, str_ScreenSize);
		editor.commit();

		doBindService();
		updater.sendEmptyMessageDelayed(MSG_DELAY, 2000);
		//Log.v(TAG, "s2");
	}

	@Override
	protected void onStart() {
		//Log.v(TAG, "s3");
		super.onStart();
		//Log.v(TAG, "s4");
	}

	@Override
	protected void onResume() {
		//Log.v(TAG, "s5");
		isRunning = true;
		//Log.v(TAG, "on resume called....**...");
		//updater.sendMessage(updater.obtainMessage(Action.one.ordinal(), 0, 0));
		waitIsOver = false;
		super.onResume();
		if (mIsBound && (mServiceState == CallService.STATE_DISCONNECTED)) {
			if(UtilityMethods.haveWeRegisteredBefore(getApplicationContext())) {
				showNetworkDialog();
			}
		}
		//Log.v(TAG, "s6");
	}

	@Override
	protected void onPause() {
		isRunning = false;
		super.onPause();
		updater.removeCallbacksAndMessages(null);
		//Log.v(TAG, "on Pause called....**...");
		finish();
	}

	@Override
	protected void onDestroy() {
		doUnbindService();
		super.onDestroy();
	}

	public static boolean isRunning() {
		return isRunning;
	}

}