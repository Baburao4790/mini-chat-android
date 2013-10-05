package com.montycall.android.lebanoncall;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.montycall.android.lebanoncall.service.CallService;

public class ReconnectingActivity extends Activity {
	
	private static final String TAG = "ReconnectingActivity";
	private TextView tv1 ;
	private TextView tv2 ;
	private Button button;
	public static final String RECONNECT_TIME = "com.android.chatclient.ReconnectingActivity.reconnectTime";
	
	/** Start of Service Related Processing and messaging */

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	Messenger mChatService;
	boolean mIsBound;

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
					//Log.v(TAG, "ChatService.STATE_WAITING_FOR_USER_CREDENTIALS");
					break;

				case CallService.STATE_REGISTERING:
					//Log.v(TAG, "ChatService.STATE_REGISTERING");
					break;

				case CallService.STATE_LOGGED_IN:
					//Log.v(TAG, "ChatService.STATE_LOGGED_IN");
					finish();
					break;

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
			mChatService = new Messenger(service);
			mIsBound = true;
			sendMessageToService(CallService.MSG_REGISTER_CLIENT, 0, 0, null);
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
	protected void onCreate(Bundle savedInstanceState) {
		doBindService();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reconnecting);
		Bundle extras = getIntent().getExtras();
		int time = extras.getInt(RECONNECT_TIME);
		tv1 = (TextView)findViewById(R.id.time_to_recon);
		tv2 = (TextView)findViewById(R.id.recon_str);
		button = (Button)findViewById(R.id.recon_now);
		//Log.v("TAG", "time " + time);
		tv1.setText(" " + time + " s");
		CountDownTimer cdt = new CountDownTimer(time*1000, 1000) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				tv1.setText(" " + (int) (millisUntilFinished/1000) + " s");
				
			}
			
			@Override
			public void onFinish() {
				tv1.setText("");
				tv2.setText("Reconnecting NOW");
				sendMessageToService(CallService.MSG_LOG_IN, 0, 0, null);
				button.setEnabled(false);
				finish();
			}
		};
		cdt.start();
		
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendMessageToService(CallService.MSG_LOG_IN, 0, 0, null);
				finish();
			}
		});
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		doUnbindService();
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		
	}

}
