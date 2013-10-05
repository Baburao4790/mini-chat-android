package com.montycall.android.lebanoncall;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.montycall.android.lebanoncall.service.CallService;

public class EmailEntryActivity extends SherlockActivity {
	private static final String TAG = "EmailEntryActivity";

	private EditText mTextEmailBox;
	private Button btnCodeReceived;
	private Button btnContinue;
	private Button btnExit;
	private String mEmail;

	public int mServiceState = CallService.STATE_WAITING_FOR_USER_EMAIL;
	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	Messenger mChatService;
	boolean mIsBound;

	/** Start of Service Related Processing and messaging */

	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CallService.MSG_STATE_INFO:
				if (mServiceState != msg.arg1) {
					switch (msg.arg1) {
					case CallService.STATE_DISCONNECTED:
						Log.v(TAG, "ChatService.STATE_DISCONNECTED");
						break;

					case CallService.STATE_CONNECTED:
						Log.v(TAG, "ChatService.STATE_CONNECTED");
						break;

					case CallService.STATE_WAITING_FOR_USER_EMAIL:
						Log.v(TAG, "ChatService.STATE_WAITING_FOR_USER_NUMBER");
						break;

					case CallService.STATE_SENDING_EMAIL_ID:
						Log.v(TAG, "ChatService.STATE_WAITING_FOR_PHONE_VERIFICATION");
						break;

					case CallService.STATE_WAITING_FOR_VERIFICATION_CODE:
						Log.v(TAG, "ChatService.STATE_WAITING_FOR_USER_CREDENTIALS");
						finish();
						overridePendingTransition(R.anim.pull_in_from_right,
								R.anim.pull_out_to_left);
						break;

					case CallService.STATE_REGISTERING:
						Log.v(TAG, "ChatService.STATE_REGISTERING");
						break;

					case CallService.STATE_LOGGED_IN:
						Log.v(TAG, "ChatService.STATE_LOGGED_IN");
						break;

					}
					mServiceState = msg.arg1;
				}
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
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. We are communicating with our
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
		// Establish a connection with the service. We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		Intent service = new Intent(this, CallService.class);
		startService(service);
		getApplicationContext().bindService(
				new Intent(this, CallService.class), mConnection,
				Context.BIND_AUTO_CREATE);
	}

	public void showNetworkDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"There is no network. Are you sure you want to continue?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						EmailEntryActivity.this.finish();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();

	}

	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mChatService != null) {
				sendMessageToService(CallService.MSG_UNREGISTER_CLIENT, 0, 0,
						null);
			}
			// Detach our existing connection.
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
				// Log.e(TAG, "Error while sending mesage to service. Msg Type "
				// + what);
			}
		}
	}

	/** End of Service Related Processing and messaging */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_phone_verification);
		doBindService();
		BitmapDrawable bg = (BitmapDrawable) getResources().getDrawable(
				R.drawable.titlebar);
		//bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		getSupportActionBar().setBackgroundDrawable(bg);

		mTextEmailBox = (EditText) findViewById(R.id.editText1);
		final TextView text = (TextView)findViewById(R.id.emailValidText);

		mTextEmailBox.addTextChangedListener(new TextWatcher() { 
	        public void afterTextChanged(Editable s) { 
	            if ((mTextEmailBox.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+") || mTextEmailBox.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+\\.+[a-z]+"))  && s.length() > 0)
	            {
	                text.setText("valid email");
	            }
	            else
	            {
	                text.setText("invalid email");
	            }
	        } 
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {} 
	        public void onTextChanged(CharSequence s, int start, int before, int count) {} 
	    });

	    
		addListenerOnButtonContinue();
		addListenerOnButtonExit();
		addListenerOnButtonCodeReceived();
		setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	protected void onPause() {
		finish();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		doUnbindService();
		super.onDestroy();
	}

	// get the selected dropdown list value
	public void addListenerOnButtonContinue() {

		btnContinue = (Button) findViewById(R.id.btnContinue);
		btnContinue.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mEmail = mTextEmailBox.getText().toString();

				if (mEmail.equals("")) {
					Toast.makeText(EmailEntryActivity.this,
							"Email Address Cannot Be Empty", Toast.LENGTH_SHORT)
							.show();
				} else if(mEmail.contains(" ")) {
					Toast.makeText(EmailEntryActivity.this, "Invalid Email address", Toast.LENGTH_SHORT).show();
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							EmailEntryActivity.this);
					builder.setMessage(
							"Your verification code will be delivered to "
									+ mEmail)
							.setCancelable(false)
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											btnContinue.setEnabled(false);
											setSupportProgressBarIndeterminateVisibility(true);
											Bundle bundle = new Bundle();
											bundle.putString(CallService.k1,
													mEmail);
											sendMessageToService(
													CallService.MSG_START_EMAIL_REGISTRATION,
													0, 0, bundle);
										}
									})
							.setNegativeButton("No",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					AlertDialog alert = builder.create();
					alert.show();
				}
			}
		});
	}

	// get the selected dropdown list value
	public void addListenerOnButtonExit() {
		btnExit = (Button) findViewById(R.id.btnExit);
		btnExit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent service = new Intent(getApplicationContext(),
						CallService.class);
				stopService(service);
				finish();
			}
		});
	}

	private void addListenerOnButtonCodeReceived() {
		btnCodeReceived = (Button) findViewById(R.id.btnCodeReceived);
		btnCodeReceived.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendMessageToService(CallService.MSG_SET_STATE, CallService.STATE_WAITING_FOR_VERIFICATION_CODE, 0, null); 
				Intent act = new Intent(getApplicationContext(), UserCredentialsActivity.class);
				act.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(act);
			}
		});
	}

}