package com.montycall.android.lebanoncall;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.sax.StartElementListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.montycall.android.lebanoncall.constants.Constants;
import com.montycall.android.lebanoncall.service.CallService;

public class RegisterEmail extends SherlockActivity {
	
	private static Messenger messenger = new Messenger(new IncomingHandler());
	private static Messenger mService = null;
	protected static boolean mIsBound;
	private Button btn_Continue = null;
	private Button btn_Exit = null;
	private EditText edit_Email = null;
	
	public static class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {				
			switch (msg.what) {
				case CallService.MSG_REGISTER_EMAIL:
					context.setSupportProgressBarIndeterminateVisibility(false);
					if(msg.arg1 == 1){
						Toast.makeText(context, "Your email has been registered", Toast.LENGTH_LONG).show();
						Intent i = new Intent(context, VerifyEmail.class);
						i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(i);
						context.finish();
						context.overridePendingTransition(0, R.anim.pull_out_to_right);
						return;
					} else {
						Toast.makeText(context, "Sorry, Cannot register your email", Toast.LENGTH_LONG).show();
					}
					context.btn_Continue.setEnabled(true);
					context.setSupportProgressBarIndeterminateVisibility(false);
					break;
				default:
					break;
			}
		}
	}
	
	ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			mIsBound = false;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			
			mService = new Messenger(service);
			sendMessageToService(CallService.MSG_REGISTER_CLIENT, 0, 0, null);
			mIsBound = true;
		}
	};
	private static RegisterEmail context;
	
	void doBindService() {
		Intent service = new Intent(this, CallService.class);
		startService(service);
		getApplicationContext().bindService(new Intent(this, CallService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}

	void doUnbindService() {
		if (mIsBound) {
			if (mService != null) {
				sendMessageToService(CallService.MSG_UNREGISTER_CLIENT, 0, 0, null);
			}
			getApplicationContext().unbindService(serviceConnection);
			mIsBound = false;
		}
	}

	private static void sendMessageToService(int what, int arg1, int arg2,
			Object obj) {
		if (mIsBound) {
			Message msg = Message.obtain(null, what);
			msg.arg1 = arg1;
			msg.arg2 = arg2;
			msg.obj = obj;
			msg.replyTo = messenger;
			try {
				mService.send(msg);
			} catch (RemoteException e) {
				/** Don;t do nothing :) */
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_register_email);
		doBindService();
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		BitmapDrawable bg = (BitmapDrawable) getResources().getDrawable(R.drawable.titlebar);
		getSupportActionBar().setBackgroundDrawable(bg);
		btn_Continue = (Button) findViewById(R.id.btnContinue);
		btn_Exit = (Button) findViewById(R.id.btnExit);
		edit_Email = (EditText) findViewById(R.id.editText1);
		context = this;
		addbtnContinueListener();
		addbtnExitListener();
		setSupportProgressBarIndeterminateVisibility(false);
	}
	
	private void addbtnExitListener() {
		btn_Exit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				onBackPressed();
			}
		});
	}

	private void addbtnContinueListener() {
		btn_Continue.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String email = edit_Email.getText().toString();
				if(email.length() > 0){
					btn_Continue.setEnabled(false);
					setSupportProgressBarIndeterminateVisibility(true);
					Bundle bundle = new Bundle();									
					bundle.putString(CallService.k1, email);
					sendMessageToService(CallService.MSG_REGISTER_EMAIL, 0, 0, bundle);
					SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(Constants.SIP_EMAIL, email);
					CallService.SIP_EMAIL = email;
					editor.commit();
				} else {
					Toast.makeText(context, "Please enter your email", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(0, R.anim.pull_out_to_right);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.activity_main, menu);

		// Calling super after populating the menu is necessary here to ensure
		// that the
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
			overridePendingTransition(0, R.anim.pull_out_to_right);
			return true;

		}
		return super.onOptionsItemSelected(item);
	}
	    
}
