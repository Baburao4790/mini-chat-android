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
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.montycall.android.lebanoncall.constants.Constants;
import com.montycall.android.lebanoncall.service.CallService;


public class DialPad extends Activity
{
    private final String LOG_TAG = "DialPad";
	private static final int PICK_CONTACT_FOR_DIAL 	= 3 ;
	private String mNumberToDial = "";	
	private static DialPad mSelfContext = null;
	private TelephonyManager telephonyManager;
	private TextView mBalanceView;
	
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
			
			case CallService.MSG_BALANCE_FETCHED:
				Log.v(LOG_TAG, "Balance Fetched " + msg.obj);
				mBalanceView.setText("Account Balance : " + msg.obj + " USD");
				break;
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
			case CallService.MSG_MAKE_CALL:
				if((msg.obj.toString().equals("false"))){
					Toast.makeText(mSelfContext, "Cannot start the call please check your connectivity!", Toast.LENGTH_LONG).show();
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
    	        	   DialPad.this.finish();
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
        setContentView(R.layout.dialpad_new);
        doBindService();
        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        mBalanceView = (TextView)findViewById(R.id.balance_dialpad);
        mBalanceView.setOnClickListener(getOnBalanceClickListener());
        mSelfContext = this;        
        try
        {
	        Bundle bundle = getIntent().getExtras();
	        if(null != bundle)
	        {
		        String phoneNumber = bundle.getString(Constants.CALL_DEST_NUMBER_KEY);
		        if(0 != phoneNumber.length())
		        {
		     	   	Log.i(LOG_TAG, "Call Destination Name_Number: " + phoneNumber);
		     	   
		            EditText editTextPhoneNumber = (EditText)findViewById(R.id.phonenumber_for_dialing);
		            editTextPhoneNumber.setText(phoneNumber);
		            mNumberToDial = phoneNumber;
		        }
	        }
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
        ImageButton imageButtonContacts  = (ImageButton)findViewById(R.id.contacts_image_button);
        imageButtonContacts.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"Contacts Button has been pressed");
				
			  Intent intentPickContact = new Intent(Intent.ACTION_PICK);  
			  intentPickContact.setType(ContactsContract.Contacts.CONTENT_TYPE); 
			  startActivityForResult(intentPickContact, PICK_CONTACT_FOR_DIAL);
			}
		});
        
        
        
        // dialpad button listener
        
        ImageButton imageButtonb1  = (ImageButton)findViewById(R.id.b1);
        imageButtonb1.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"1 Button has been pressed");
				if(mNumberToDial.length() <32)
				{
					mNumberToDial =  mNumberToDial + "1";
					((EditText)findViewById(R.id.phonenumber_for_dialing)).setText(mNumberToDial);
				}
			}
		});
        
        ImageButton imageButtonb2  = (ImageButton)findViewById(R.id.b2);
        imageButtonb2.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"2 Button has been pressed");
				if(mNumberToDial.length() <32)
				{
					mNumberToDial =  mNumberToDial + "2";
					((TextView)findViewById(R.id.phonenumber_for_dialing)).setText(mNumberToDial);
				}
			}
		});
        
        ImageButton imageButtonb3  = (ImageButton)findViewById(R.id.b3);
        imageButtonb3.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"3 Button has been pressed");
				if(mNumberToDial.length() <32)
				{
					mNumberToDial =  mNumberToDial + "3";
					((TextView)findViewById(R.id.phonenumber_for_dialing)).setText(mNumberToDial);
				}

			}
		});
        
        ImageButton imageButtonb4  = (ImageButton)findViewById(R.id.b4);
        imageButtonb4.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"4 Button has been pressed");
				if(mNumberToDial.length() <32)
				{
					mNumberToDial =  mNumberToDial + "4";
					((TextView)findViewById(R.id.phonenumber_for_dialing)).setText(mNumberToDial);
				}

			}
		});
        
        ImageButton imageButtonb5  = (ImageButton)findViewById(R.id.b5);
        imageButtonb5.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"5 Button has been pressed");
				if(mNumberToDial.length() <32)
				{
					mNumberToDial =  mNumberToDial + "5";
					((TextView)findViewById(R.id.phonenumber_for_dialing)).setText(mNumberToDial);
				}

			}
		});
        
        ImageButton imageButtonb6  = (ImageButton)findViewById(R.id.b6);
        imageButtonb6.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"6 Button has been pressed");
				if(mNumberToDial.length() <32)
				{
					mNumberToDial =  mNumberToDial + "6";
					((TextView)findViewById(R.id.phonenumber_for_dialing)).setText(mNumberToDial);
				}

			}
		});
        ImageButton imageButtonb7  = (ImageButton)findViewById(R.id.b7);
        imageButtonb7.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"7 Button has been pressed");
				{
					mNumberToDial =  mNumberToDial + "7";
					((TextView)findViewById(R.id.phonenumber_for_dialing)).setText(mNumberToDial);
				}

			}
		});
        ImageButton imageButtonb8  = (ImageButton)findViewById(R.id.b8);
        imageButtonb8.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"8 Button has been pressed");
				if(mNumberToDial.length() <32)
				{
					mNumberToDial =  mNumberToDial + "8";
					((TextView)findViewById(R.id.phonenumber_for_dialing)).setText(mNumberToDial);
				}

			}
		});
        ImageButton imageButtonb9  = (ImageButton)findViewById(R.id.b9);
        imageButtonb9.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"9 Button has been pressed");
				if(mNumberToDial.length() <32)
				{
					mNumberToDial =  mNumberToDial + "9";
					((TextView)findViewById(R.id.phonenumber_for_dialing)).setText(mNumberToDial);
				}

			}
		});
        ImageButton imageButtonb0  = (ImageButton)findViewById(R.id.b0);
        imageButtonb0.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"0 Button has been pressed");
				if(mNumberToDial.length() <32)
				{
					mNumberToDial =  mNumberToDial + "0";
					((TextView)findViewById(R.id.phonenumber_for_dialing)).setText(mNumberToDial);
				}

			}
		});
        
        imageButtonb0.setOnLongClickListener(new OnLongClickListener() 
        {        
        	@Override        
        	public boolean onLongClick(View v)
        	{
        		Log.i(LOG_TAG,"0 Button has been long pressed");
				if(mNumberToDial.length() <32)
				{
					mNumberToDial =  mNumberToDial + "+";
					((TextView)findViewById(R.id.phonenumber_for_dialing)).setText(mNumberToDial);
				}
        		return true;         
        	}     
        }); 
        
        ImageButton imageButtonbStar  = (ImageButton)findViewById(R.id.bstar);
        imageButtonbStar.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"star Button has been pressed");
				if(mNumberToDial.length() <32)
				{
					mNumberToDial =  mNumberToDial + "*";
					((TextView)findViewById(R.id.phonenumber_for_dialing)).setText(mNumberToDial);
				}

			}
		});
        ImageButton imageButtonbHash  = (ImageButton)findViewById(R.id.bhash);
        imageButtonbHash.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"Hash Button has been pressed");
				if(mNumberToDial.length() <32)
				{
					mNumberToDial =  mNumberToDial + "#";
					((TextView)findViewById(R.id.phonenumber_for_dialing)).setText(mNumberToDial);
				}

			}
		});
        
        ImageButton imageButtonbbackSpace  = (ImageButton)findViewById(R.id.b_back_space);
        imageButtonbbackSpace.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"backSpace Button has been pressed");
				if(0 != mNumberToDial.length())
				{
					String strTemp =  mNumberToDial.substring(0, mNumberToDial.length()- 1);
					mNumberToDial  = strTemp;
					((TextView)findViewById(R.id.phonenumber_for_dialing)).setText(mNumberToDial);
				}

			}
		});
        ImageButton imageButtonbMakeCall  = (ImageButton)findViewById(R.id.b_make_call);
        imageButtonbMakeCall.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				Log.i(LOG_TAG,"MakeCall Button has been pressed");
				
				if(0 ==  mNumberToDial.length())
				{
					return;
				}
				
				int callState = telephonyManager.getCallState();
				if(callState != TelephonyManager.CALL_STATE_IDLE)
				{
				    Toast.makeText(getApplicationContext(), "Cellular Call is in Active State, CallLebanon is unavailable", Toast.LENGTH_SHORT).show();
				    return;
				} else {
					Bundle bundle = new Bundle();
					String str = mNumberToDial;
					bundle.putString(CallService.k1, str);
					sendMessageToService(CallService.MSG_MAKE_CALL, 0, 0, bundle);
				}
			}
		});
        
     }
	
	private OnClickListener getOnBalanceClickListener() {
		
		return new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendMessageToService(CallService.MSG_CHECK_BALANCE, 0, 0, null);				
			}
		};
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
		String balance = settings.getString(Constants.CURRENT_BALANCE, "");
		mBalanceView.setText("Account Balance : " + balance + " USD");
	}
   
	public static void FinishActivity()
	{
		if(null != mSelfContext)
		{
		    mSelfContext.finish();
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == PICK_CONTACT_FOR_DIAL && null != intent) {
			getContactInfo(intent);
		}
	}
	
	protected void getContactInfo(Intent intent) 
	{   
		Cursor cursor =  getContentResolver().query(intent.getData(), null, null, null, null);
		String phoneNumber ="";
		//String name = "";
		if(cursor != null) {
			while (cursor.moveToNext()) 
			{  
				String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)); 
				//name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
				String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));  
				if ( hasPhone.equalsIgnoreCase("1"))
				{
					hasPhone = "true";
				}
				else
				{
					hasPhone = "false" ;
				}
				
				ArrayList<String> contact_list = new ArrayList<String>();
				if (Boolean.parseBoolean(hasPhone)) 
				{
					Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,null, null);  
					while (phones.moveToNext())
					{ 
						
						phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)); 
						contact_list.add(phoneNumber);
					}
					phones.close(); 
				}  

				if(contact_list.size()>1)
				{
					final CharSequence[] items = contact_list.toArray(new CharSequence[contact_list.size()]); 
			        AlertDialog.Builder builder = new AlertDialog.Builder(this);
			        //builder.setTitle(getString(R.string.select_number));
			        builder.setItems(items, new DialogInterface.OnClickListener()
			        {
			            public void onClick(DialogInterface dialogInterface, int item) 
			            {
			                String phone_number = (String) items[item];
			            	String tempNumber = phone_number;
			    			while(tempNumber.contains("-"))
			    			{
			    				tempNumber =tempNumber.substring(0,tempNumber.indexOf('-')) + tempNumber.substring(tempNumber.indexOf('-')+1,tempNumber.length());
			    			}
			    			phone_number = tempNumber;
			            	EditText 	editTextPhoneNumber 	= (EditText)findViewById(R.id.phonenumber_for_dialing);
							editTextPhoneNumber.setText(phone_number);
							mNumberToDial = phone_number;
							int callState = telephonyManager.getCallState();
							if (callState != TelephonyManager.CALL_STATE_IDLE) {
								Toast.makeText(
										getApplicationContext(),
										"Cellular Call is in Active State, CallLebanon is unavailable",
										Toast.LENGTH_SHORT).show();
								return;
							} else {
								Bundle bundle = new Bundle();
								String str = mNumberToDial;
								bundle.putString(CallService.k1, str);
								sendMessageToService(CallService.MSG_MAKE_CALL, 0, 0, bundle);
							}
			                return;
			            }
			        });
			        builder.create().show();
				}
				else if(1 == contact_list.size())
				{
	            	String tempNumber = phoneNumber;
	    			while(tempNumber.contains("-"))
	    			{
	    				tempNumber = tempNumber.substring(0,tempNumber.indexOf('-')) + tempNumber.substring(tempNumber.indexOf('-')+1,tempNumber.length());
	    			}
	    			phoneNumber = tempNumber;
					EditText 	editTextPhoneNumber 	= (EditText)findViewById(R.id.phonenumber_for_dialing);
					editTextPhoneNumber.setText(phoneNumber);
					mNumberToDial = phoneNumber;
					int callState = telephonyManager.getCallState();
					if (callState != TelephonyManager.CALL_STATE_IDLE) {
						Toast.makeText(
								getApplicationContext(),
								"Cellular Call is in Active State, CallLebanon is unavailable",
								Toast.LENGTH_SHORT).show();
						return;
					} else {
						Bundle bundle = new Bundle();
						String str = mNumberToDial;
						bundle.putString(CallService.k1, str);
						sendMessageToService(CallService.MSG_MAKE_CALL, 0, 0, bundle);
					}
				}
			}
			cursor.close();
		}
		
	}
	
	@Override
	protected void onDestroy() {
		doUnbindService();
		super.onDestroy();
	}

}