package com.montycall.android.lebanoncall;

import org.doubango.ngn.sip.NgnAVSession;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.montycall.android.lebanoncall.service.CallService;


public class DtmfDialPad extends Activity
{
    private final String LOG_TAG = "DialPad.java";
	private String mNumberToDial = "";	
	private static DtmfDialPad mSelfContext = null;
	
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	Messenger mChatService;
    boolean mIsBound;
    private NgnAVSession mSession;
	
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
						//Log.v(TAG,"ChatService.STATE_WAITING_FOR_USER_CREDENTIALS");
						break;

					case CallService.STATE_REGISTERING:
						//Log.v(TAG, "ChatService.STATE_REGISTERING");
						break;

					case CallService.STATE_LOGGING_IN:
						break;
						
					case CallService.STATE_LOGGED_IN:
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
    	        	   DtmfDialPad.this.finish();
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
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dtmf_pad);
        doBindService();
        
        mSession = NgnAVSession.getSession(CallService.getActiveSessionId());
        
        mSelfContext = this;
        
        // dialpad button listener
        
        ImageButton imageButtonb1  = (ImageButton)findViewById(R.id.b1);
        imageButtonb1.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"1 Button has been pressed");
				
					mSession.sendDTMF(1);
					mNumberToDial =  mNumberToDial + "1";
					((EditText)findViewById(R.id.dtmf_for_dialing)).setText(mNumberToDial);
				
			}
		});
        
        ImageButton imageButtonb2  = (ImageButton)findViewById(R.id.b2);
        imageButtonb2.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"2 Button has been pressed");
				
					mSession.sendDTMF(2);
					mNumberToDial =  mNumberToDial + "2";
					((TextView)findViewById(R.id.dtmf_for_dialing)).setText(mNumberToDial);
				
			}
		});
        
        ImageButton imageButtonb3  = (ImageButton)findViewById(R.id.b3);
        imageButtonb3.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"3 Button has been pressed");
				
					mSession.sendDTMF(3);
					mNumberToDial =  mNumberToDial + "3";
					((TextView)findViewById(R.id.dtmf_for_dialing)).setText(mNumberToDial);
				

			}
		});
        
        ImageButton imageButtonb4  = (ImageButton)findViewById(R.id.b4);
        imageButtonb4.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"4 Button has been pressed");
				
					mSession.sendDTMF(4);
					mNumberToDial =  mNumberToDial + "4";
					((TextView)findViewById(R.id.dtmf_for_dialing)).setText(mNumberToDial);
				

			}
		});
        
        ImageButton imageButtonb5  = (ImageButton)findViewById(R.id.b5);
        imageButtonb5.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"5 Button has been pressed");
				
					mSession.sendDTMF(5);
					mNumberToDial =  mNumberToDial + "5";
					((TextView)findViewById(R.id.dtmf_for_dialing)).setText(mNumberToDial);
				

			}
		});
        
        ImageButton imageButtonb6  = (ImageButton)findViewById(R.id.b6);
        imageButtonb6.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"6 Button has been pressed");
				
					mSession.sendDTMF(6);
					mNumberToDial =  mNumberToDial + "6";
					((TextView)findViewById(R.id.dtmf_for_dialing)).setText(mNumberToDial);
				

			}
		});
        ImageButton imageButtonb7  = (ImageButton)findViewById(R.id.b7);
        imageButtonb7.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				
					mSession.sendDTMF(7);
					mNumberToDial =  mNumberToDial + "7";
					((TextView)findViewById(R.id.dtmf_for_dialing)).setText(mNumberToDial);
				

			}
		});
        ImageButton imageButtonb8  = (ImageButton)findViewById(R.id.b8);
        imageButtonb8.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"8 Button has been pressed");
				
					mSession.sendDTMF(8);
					mNumberToDial =  mNumberToDial + "8";
					((TextView)findViewById(R.id.dtmf_for_dialing)).setText(mNumberToDial);
				

			}
		});
        ImageButton imageButtonb9  = (ImageButton)findViewById(R.id.b9);
        imageButtonb9.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"9 Button has been pressed");
				
					mSession.sendDTMF(9);
					mNumberToDial =  mNumberToDial + "9";
					((TextView)findViewById(R.id.dtmf_for_dialing)).setText(mNumberToDial);
				

			}
		});
        ImageButton imageButtonb0  = (ImageButton)findViewById(R.id.b0);
        imageButtonb0.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"0 Button has been pressed");
				
					mSession.sendDTMF(0);
					mNumberToDial =  mNumberToDial + "0";
					((TextView)findViewById(R.id.dtmf_for_dialing)).setText(mNumberToDial);
				

			}
		});
        
        ImageButton imageButtonbStar  = (ImageButton)findViewById(R.id.bstar);
        imageButtonbStar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(LOG_TAG, "star Button has been pressed");

				mNumberToDial = mNumberToDial + "*";
				((TextView) findViewById(R.id.dtmf_for_dialing))
						.setText(mNumberToDial);

			}
		});
        ImageButton imageButtonbHash  = (ImageButton)findViewById(R.id.bhash);
        imageButtonbHash.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"Hash Button has been pressed");
				
					mNumberToDial =  mNumberToDial + "#";
					((TextView)findViewById(R.id.dtmf_for_dialing)).setText(mNumberToDial);
				

			}
		});        
     } 
   
	public static void FinishActivity()
	{
		if(null != mSelfContext)
		{
		    mSelfContext.finish();
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		
	}
	
	
	@Override
	protected void onDestroy() {
		doUnbindService();
		super.onDestroy();
	}
}