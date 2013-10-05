package com.montycall.android.lebanoncall;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.montycall.android.lebanoncall.constants.Constants;
import com.montycall.android.lebanoncall.service.CallService;




public class UserCredentialsActivity  extends SherlockActivity  {
    private static final String TAG = "UserCredentialsActivity";

    private Button btnContinue = null;
	private Button btnExit;
	private Button btnSupport;
    private EditText textUserFirstName;
    //private EditText textUserLastName;
    //private EditText textUserEmail;
    private EditText verificationCode;
    private boolean displayToast = false;
    //private ProgressBar progressBar;

	public int mServiceState = CallService.STATE_WAITING_FOR_VERIFICATION_CODE;
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
						if(btnContinue != null) {
							if(displayToast) {
								Toast.makeText(UserCredentialsActivity.this, "User Registration Failed! Please Retry " ,Toast.LENGTH_LONG).show();
								displayToast = false;
							}
							setSupportProgressBarIndeterminateVisibility(false);
							btnContinue.setEnabled(true);
						}
						//Log.v(TAG,"ChatService.STATE_WAITING_FOR_USER_CREDENTIALS");
						break;

					case CallService.STATE_REGISTERING:
						//Log.v(TAG, "ChatService.STATE_REGISTERING");
						break;

					case CallService.STATE_LOGGING_IN:
						//Log.v(TAG, "ChatService.STATE_LOGGING_IN");
						/*finish();
						overridePendingTransition(R.anim.pull_in_from_right,
								R.anim.pull_out_to_left);*/
						break;
						
					case CallService.STATE_LOGGED_IN:
						Intent i = new Intent(getApplicationContext(), MainTabActivity.class);
	        			Bundle b = new Bundle();
	        	        b.putString(MainTabActivity.DEFAULT_TAB, "dialpad");
	        	        i.putExtras(b);
	                	UserCredentialsActivity.this.startActivity(i);
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
            //sendMessageToService(ChatService.MSG_STATE_INFO, 0, 0, null);            
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

    public void showNetworkDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("There is no network. Are you sure you want to continue?")
    	       .setCancelable(false)
    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   dialog.cancel();
    	           }
    	       })
    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   UserCredentialsActivity.this.finish();
    	           }
    	       });    	AlertDialog alert = builder.create();
    	alert.show();
		
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

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_user_credentials);
        BitmapDrawable bg = (BitmapDrawable)getResources().getDrawable(R.drawable.titlebar);
        //bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        getSupportActionBar().setBackgroundDrawable(bg);
        verificationCode = (EditText) findViewById (R.id.editTextVerificationCode);
        textUserFirstName = (EditText) findViewById (R.id.editTextFirstName);
        
        addListenerOnButtonContinue();
        addListenerOnButtonExit();
        
        setSupportProgressBarIndeterminateVisibility(false);
        /*btnSupport = (Button) findViewById(R.id.btnSupport);
        
        btnSupport.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View arg0) {
        		Intent i = new Intent(Intent.ACTION_VIEW);
        		i.setData(Uri.parse("http://93.89.95.10:8080/newapps/support_montychat.php"));
        		startActivity(i);
          	}
        });*/
    }
	
	@Override
	protected void onResume() {
		doBindService();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		doUnbindService();
		super.onPause();
		finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	//get the selected dropdown list value
	public void addListenerOnButtonContinue() {

		btnContinue = (Button) findViewById(R.id.btnContinue);
		//progressBar = (ProgressBar)findViewById(R.id.progressBar1);
		btnContinue.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
				
			String firstname = textUserFirstName.getText().toString();
			//String lasttname = textUserLastName.getText().toString();
			//String email = textUserEmail.getText().toString();
			String vericode = verificationCode.getText().toString();
			if(vericode.equals(""))
			{
				Toast.makeText(UserCredentialsActivity.this, "Verification Code Cannot Be Empty", 
		           		Toast.LENGTH_SHORT).show();
				return;
			}
			if(firstname.equals(""))
			{
				Toast.makeText(UserCredentialsActivity.this, "First Name Cannot Be Empty", 
		           		Toast.LENGTH_SHORT).show();
			}
			/*else if(email.equals(""))
			{
				Toast.makeText(UserCredentialsActivity.this, "Email ID Cannot Be Empty", 
		           		Toast.LENGTH_SHORT).show();
			}*/
			else
			{
				btnContinue.setEnabled(false);
				setSupportProgressBarIndeterminateVisibility(true);
				Bundle bundle = new Bundle();
				bundle.putString(CallService.k1, vericode);
				bundle.putString(CallService.k2, firstname);
				sendMessageToService(CallService.MSG_REGISTER, 0, 0, bundle);
				Toast.makeText(UserCredentialsActivity.this, "Initiated User Registration ", 
		           		Toast.LENGTH_SHORT).show();
				displayToast = true;
			}
		}

	});

	}
	
	//get the selected dropdown list value
	public void addListenerOnButtonExit() {

		btnExit = (Button) findViewById(R.id.btnExit);

		btnExit.setOnClickListener(new OnClickListener() {
			
			private void SavePreferences(String key, Boolean value) {
				SharedPreferences sharedPreferences = getSharedPreferences(
						Constants.PREFS_NAME, 0);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putBoolean(key, value);
				editor.commit();
			}

			@Override
			public void onClick(View v) {
				//SavePreferences(Constants.PHONE_VERIFIED, false);
				Intent service = new Intent(getApplicationContext(), CallService.class);
            	stopService(service);
				finish();
			}
		});
	}
}