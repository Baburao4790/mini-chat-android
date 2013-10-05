package com.montycall.android.lebanoncall;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.montycall.android.lebanoncall.CallerIdActivityAdapter.VerifyCallerId;
import com.montycall.android.lebanoncall.constants.Constants;
import com.montycall.android.lebanoncall.db.CallLogProvider;
import com.montycall.android.lebanoncall.service.CallService;


public class ProfileActivity extends Activity{
	private ListView mListView;
	private ProfileListAdapter mAdapter;
	private Button btnUnregister;
	private Context mContext;
	public int mServiceState = CallService.STATE_UNKNOWN;
	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	/** Start of Service Related Processing and messaging */
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	Messenger mChatService;
	boolean mIsBound;
	

	private static final String TAG = "ProfileActivity";
	

	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(android.os.Message msg) {
			//Log.v(TAG, "some mesaage " + msg.what);
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
						//Log.v(TAG,"ChatService.STATE_WAITING_FOR_PHONE_VERIFICATION");
						break;

					case CallService.STATE_WAITING_FOR_VERIFICATION_CODE:
						//Log.v(TAG,"ChatService.STATE_WAITING_FOR_USER_CREDENTIALS");
						break;

					case CallService.STATE_REGISTERING:
						//Log.v(TAG, "ChatService.STATE_REGISTERING");
						break;

					case CallService.STATE_LOGGING_IN:
						//Log.v(TAG, "ChatService.STATE_LOGGING_IN");
						//sendMessageToService(ChatService.MSG_LOG_IN, 0, 0, null);
						break;
					case CallService.STATE_LOGGED_IN:
						//Log.v(TAG, "ChatService.STATE_LOGGED_IN");
						
						break;
						
					case CallService.STATE_LOGGED_OUT:
						//Log.v(TAG, "ChatService.STATE_LOGGED_OUT");
						break;
					}					
				}
				break;
			case CallService.MSG_BALANCE_FETCHED:
				runOnUiThread(new Runnable() {
					public void run() {
						mAdapter.notifyDataSetChanged();
					}
				});
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
			sendMessageToService(CallService.MSG_REGISTER_CLIENT, 0, 0, null);
		}

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

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.pull_in_from_left,
				R.anim.pull_out_to_right);
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTitle("");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		doBindService();
		mContext = this;
		mListView = (ListView) findViewById(R.id.profile_list);
		mAdapter = new ProfileListAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new ProfileItemClickListener());
		/*addListenerOnButtonUnregister();*/
	}
	
	private void reInitSharedPreferences() {
		SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(Constants.MONTYCALL_USER_EMAIL, null);
		editor.putBoolean(Constants.PHONE_VERIFIED, false);
		editor.putString(Constants.MONTY_CHAT_USER_COUNTRY_CODE, null);
		editor.putString(Constants.USER_UNVERIFIED_CALLER_IDS, null);
		editor.commit();
	}
	
	/*public void addListenerOnButtonUnregister() {
		btnUnregister = (Button) findViewById(R.id.btnUnregister);
		btnUnregister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
				builder.setMessage("Are you sure you want to unregister?")
				       .setCancelable(false)
				       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   
							  sendMessageToService(CallService.MSG_DELETE_ACCOUNT, 0, 0, null);
							  getContentResolver().delete(CallLogProvider.CONTENT_URI, null, null);
							  reInitSharedPreferences();
				              Intent i = new Intent(ProfileActivity.this, SplashActivity.class);
				              i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				              ProfileActivity.this.startActivity(i);
				              finish();
				           }
				       })
				       .setNegativeButton("No", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
	}
	*/
	@Override
	protected void onDestroy() {
		doUnbindService();
		super.onDestroy();
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
		setupUserInfo();
	}
	
	private void setupUserInfo() {
     
		ArrayList<String> userInfo = new ArrayList<String>();
		userInfo.add(0, "");
		userInfo.add(1, "");
		userInfo.add(2, "");
		userInfo.add(3, "");
		userInfo.add(4, "");
		userInfo.add(5, "");
		userInfo.add(6, "");
		mAdapter.initializeContent(userInfo);
		mAdapter.notifyDataSetChanged();
	}
	
	 private class ProfileItemClickListener implements OnItemClickListener {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				switch(arg2) {

					
					case 1:
						Intent i0 = new Intent(ProfileActivity.this, RegisterEmail.class);
						i0.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
						ProfileActivity.this.startActivity(i0);
                        overridePendingTransition(R.anim.pull_in_from_right, R.anim.pull_out_to_left);
						break;
					case 2:
						sendMessageToService(CallService.MSG_CHECK_BALANCE, 0, 0, null);
						break;
					case 3:
						Intent i1 = new Intent(ProfileActivity.this, CallRatesActivity.class);
						i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						ProfileActivity.this.startActivity(i1);
                        overridePendingTransition(R.anim.pull_in_from_right, R.anim.pull_out_to_left);
						break;
					case 4:
						Intent i2 = new Intent(ProfileActivity.this, CallerIdSetActivity.class);
						i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						ProfileActivity.this.startActivity(i2);
						break;
					case 5:
						Intent i3 = new Intent(ProfileActivity.this, BuyCreditsActivity.class);
						i3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						ProfileActivity.this.startActivity(i3);
						break;
						
					case 6:
						try{
						Intent i4 = new Intent(Intent.ACTION_SEND);
						String[] recipients={"apps@spactron.com"};
						String[] cc = {"support@montycall.com"};
						i4.putExtra(Intent.EXTRA_EMAIL, recipients);
						i4.putExtra(Intent.EXTRA_CC, cc);
						i4.putExtra(Intent.EXTRA_SUBJECT, "Sent from: " + CallService.getSipUserName());
						i4.setType("text/html");
						startActivity(Intent.createChooser(i4, "Send mail"));
		        		startActivity(i4);
						} catch (Exception e) {
							Toast.makeText(ProfileActivity.this, "No application found to send email", Toast.LENGTH_LONG).show();
						}
						break;
				}
			}
		}

	public void setUserName() {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Profile");
		alert.setMessage("Please enter your name");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		alert.setView(input);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Editable value = input.getText();
				Log.e(TAG, "value " + value);
				SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(Constants.USER_FIRST_NAME, value.toString());
				CallService.SIP_USER = value.toString();
				editor.commit();
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}
	   
}
