package com.montycall.android.lebanoncall;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.montycall.android.lebanoncall.db.CallLogProvider;
import com.montycall.android.lebanoncall.db.CallLogProvider.CallLogConstants;
import com.montycall.android.lebanoncall.service.CallService;



@SuppressLint("NewApi")
public class CallLogsList extends Activity {

	private static final String TAG = "CallLogsList";
	private CallLogListAdapter mAdapter = null;
	private ListView mPhoneContactListView;
	private EditText mPhoneContactSearchText;
	private TelephonyManager telephonyManager;
	private static boolean isRunning = false;
	private static ArrayList<CallLogEntry> mCallLogEntryList = new ArrayList<CallLogEntry>();
	private ContentObserver mCallLogObserver = new CallLogObserver();
	private static final int NOTIFY_DATA_SET_CHANED = 1236;
	
	private CallLogEntry entryToDelete = null;

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
	IncomingHandler mIncomingHandler = new IncomingHandler();
    final Messenger mMessenger = new Messenger(mIncomingHandler);
	Messenger mChatService;
    boolean mIsBound;
    public int mServiceState = CallService.STATE_UNKNOWN;
    
    
/** Start of Service Related Processing and messaging */
    
    /**
     * Handler of incoming messages from service.
     */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(android.os.Message msg) {
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
						//Log.v(TAG, "ChatService.STATE_WAITING_FOR_PHONE_VERIFICATION");
						break;

					case CallService.STATE_WAITING_FOR_VERIFICATION_CODE:
						//Log.v(TAG, "ChatService.STATE_WAITING_FOR_USER_CREDENTIALS");
						break;

					case CallService.STATE_REGISTERING:
						//Log.v(TAG, "ChatService.STATE_REGISTERING");
						break;

					case CallService.STATE_LOGGED_IN:
						//Log.v(TAG, "ChatService.STATE_LOGGED_IN");
						break;
					}
				}
				break;
				
			case NOTIFY_DATA_SET_CHANED:
				mAdapter.notifyDataSetChanged();
				break;

			default:
				super.handleMessage(msg);
				//Log.d(TAG, "###################  default  1");
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

    /** End of Service Related Processing and messaging */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phone_contact_list_display);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		doBindService();
		mPhoneContactListView = (ListView) findViewById(R.id.contact_list);
		mPhoneContactSearchText = (EditText) findViewById(R.id.search_box);
		mPhoneContactSearchText.setVisibility(View.GONE);
		telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		getContentResolver().registerContentObserver(CallLogProvider.CONTENT_URI, true, mCallLogObserver);
		mPhoneContactListView.setOnItemClickListener(new CallLogItemClickListener());
		registerForContextMenu(mPhoneContactListView);
	}
	
	public void setUpCallLogs() {
		mCallLogEntryList.clear();
		getCallLogContactList(mCallLogEntryList);
		setAdapterToListview(mCallLogEntryList);
	}
		
	public void setAdapterToListview(ArrayList<CallLogEntry> listForAdapter) {
		if (null == mAdapter) {
			mAdapter = new CallLogListAdapter(CallLogsList.this, listForAdapter);
			mPhoneContactListView.setAdapter(mAdapter);
		} else {
			Message msg = mIncomingHandler.obtainMessage(NOTIFY_DATA_SET_CHANED, null);
			mIncomingHandler.sendMessage(msg);
			//mAdapter.notifyDataSetChanged();
		}
	}

	
	public void getCallLogContactList(ArrayList<CallLogEntry> list) {
		list.clear();
		Cursor cursor = this.getContentResolver().query(CallLogProvider.CONTENT_URI, null,
					null, null, CallLogConstants.DATE + " DESC");
		if(cursor != null) {
			int numberIdx = cursor.getColumnIndex(CallLogConstants.NUMBER);
			int nameIdx = cursor.getColumnIndex(CallLogConstants.NAME);
			int typeIdx = cursor.getColumnIndex(CallLogConstants.DIRECTION);
			int dateIdx = cursor.getColumnIndex(CallLogConstants.DATE);
			int durIdx = cursor.getColumnIndex(CallLogConstants.DURATION);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				CallLogEntry rosterContact = new CallLogEntry();
				rosterContact.number = cursor.getString(numberIdx);
				rosterContact.name = cursor.getString(nameIdx);
				rosterContact.type = cursor.getInt(typeIdx);
				rosterContact.last_call_date = cursor.getLong(dateIdx);
				rosterContact.last_call_duration = cursor.getLong(durIdx);
				list.add(rosterContact);
				cursor.moveToNext();
			}
			cursor.close();
		}
				
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		entryToDelete = null;
		menu.setHeaderTitle("Call Log Options");
		menu.add(Menu.NONE, 0, 0, "Delete Call Log");
		entryToDelete = mCallLogEntryList.get(info.position);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if(entryToDelete != null) {
			this.getContentResolver().delete(CallLogProvider.CONTENT_URI, CallLogConstants.DATE + "=" + entryToDelete.last_call_date, null);
			entryToDelete = null;
		}
		return super.onContextItemSelected(item);
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
		//doBindService();
		setUpCallLogs();
		isRunning = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		//doUnbindService();
		isRunning = false;
	}
	
	@Override
	protected void onDestroy() {
		doUnbindService();
		super.onDestroy();
	}

	public static boolean isRunning() {
		return isRunning;
	}
	
	private class CallLogItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
			if (arg1.getTag().getClass().getSimpleName().equals("ViewHolder")) {
				CallLogEntry entry = (CallLogEntry) arg0.getItemAtPosition(arg2);
				//open callScreen to make call to selected contact
				int callState = telephonyManager.getCallState();
				if(callState != TelephonyManager.CALL_STATE_IDLE)
				{
				    Toast.makeText(getApplicationContext(), "Cellular Call is in Active State, CallLebanon is unavailable", Toast.LENGTH_SHORT).show();
				    return;
				} else {
					Bundle bundle = new Bundle();
					bundle.putString(CallService.k1, entry.number);
					bundle.putString(CallService.k2, entry.name);
					sendMessageToService(CallService.MSG_MAKE_CALL, 0, 0, bundle);
				}
			}
		}
	}
	private class CallLogObserver extends ContentObserver {
		public CallLogObserver() {
			super(null);
		}
		public void onChange(boolean selfChange) {
			setUpCallLogs();
		}
	}	
}
