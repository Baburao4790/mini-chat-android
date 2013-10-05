package com.montycall.android.lebanoncall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.montycall.android.lebanoncall.constants.Constants;
import com.montycall.android.lebanoncall.service.CallService;
import com.montycall.android.lebanoncall.service.PhoneContacts;


@SuppressLint("NewApi")
public class PhoneContactList extends Activity implements TextWatcher{
	//,  ActionBar.OnNavigationListener

	private static final String TAG = "PhoneContactList";
	private PhoneContactListAdapter mAdapter = null;
	private PhoneContacts phoneContacts;
	private ListView mPhoneContactListView;
	private EditText mPhoneContactSearchText;
	private Button mSyncButton;
	private Button mClearSearchButton;
	private TelephonyManager telephonyManager;
	SharedPreferences pref;
	String searchString;
	private static boolean isRunning = false;
	private static ArrayList<XmppPhoneContact> mPhoneAllContacts;
	List<XmppPhoneContact> filterPhoneContactArray = new ArrayList<XmppPhoneContact>();
	public Handler mHandler = new Handler();
	private ContactsChangedReceiver mContactChangeReceiver;
	private static ArrayList<XmppPhoneContact> mPhoneChatContacts;

	ArrayList<IXmppPhoneContact> itemsPhoneContactSection = new ArrayList<IXmppPhoneContact>();
	//private String[] mListType;
	private boolean isMontyChatListSelected = false;
	
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
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
		mSyncButton = (Button) findViewById(R.id.contact_sync_button);
		mClearSearchButton = (Button) findViewById(R.id.clear_search_text);
		mPhoneContactSearchText.addTextChangedListener(this);
		telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

		mPhoneContactListView.setOnItemClickListener(new PhoneContactItemClickListener());
		//mPhoneContactListView.setOnItemLongClickListener(new PhoneContactItemLongClickListener());
		//setAdapterToListview(mPhoneAllContacts);
		mContactChangeReceiver = new ContactsChangedReceiver();		
		setupPhoneContactList();
		setAdapterToListview(mPhoneAllContacts);
		
		mSyncButton.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPhoneAllContacts = getUpdatedContactList();
				setupPhoneContactList();
				setAdapterToListview(mPhoneAllContacts);
			}
		});
		
		mClearSearchButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPhoneContactSearchText.setText("");
				mPhoneContactSearchText.clearFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mPhoneContactSearchText.getWindowToken(), 0);
			}
		});
		
		
	}
	
	public void setupPhoneContactList() {
		//Log.v(TAG, "setupphonecontactlist " + result);
		mPhoneAllContacts = getUpdatedContactList();
	}
	 
	 @Override
		public void afterTextChanged(Editable s) {
			filterPhoneContactArray.clear();
			searchString = mPhoneContactSearchText.getText().toString().trim()
					.replaceAll("\\s", "");

			if(isMontyChatListSelected) {
				if (mPhoneChatContacts.size() > 0 && searchString.length() > 0) {
					for (XmppPhoneContact name : mPhoneChatContacts) {
						if (name.getName().toLowerCase()
								.startsWith(searchString.toLowerCase())) {
	
							filterPhoneContactArray.add(name);
						}
					}
					setAdapterToListview(filterPhoneContactArray);
				} else {
					filterPhoneContactArray.clear();
					setAdapterToListview(mPhoneChatContacts);
				}
			}else {
				if (mPhoneAllContacts.size() > 0 && searchString.length() > 0) {
					for (XmppPhoneContact name : mPhoneAllContacts) {
						if (name.getName().toLowerCase()
								.startsWith(searchString.toLowerCase())) {
	
							filterPhoneContactArray.add(name);
						}
					}
					setAdapterToListview(filterPhoneContactArray);
				} else {
					filterPhoneContactArray.clear();
					setAdapterToListview(mPhoneAllContacts);
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		// Here Data is Filtered!!!
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
		
		public void setAdapterToListview(List<XmppPhoneContact> listForAdapter) {

			itemsPhoneContactSection.clear();

			if (null != listForAdapter && listForAdapter.size() != 0) {

				Collections.sort(listForAdapter);
				char checkChar = ' ';

				for (int index = 0; index < listForAdapter.size(); index++) {

					XmppPhoneContact objItem = (XmppPhoneContact) listForAdapter.get(index);
					/*char firstChar = objItem.getName().charAt(0);
					
					if (' ' != checkChar) {
						if (checkChar != firstChar) {
							XmppPhoneContactSections objSectionItem = new XmppPhoneContactSections();
							objSectionItem.setSectionLetter(firstChar);
							itemsPhoneContactSection.add(objSectionItem);
						}
					} else {
						XmppPhoneContactSections objSectionItem = new XmppPhoneContactSections();
						objSectionItem.setSectionLetter(firstChar);
						itemsPhoneContactSection.add(objSectionItem);
					}

					checkChar = firstChar;*/
					
					itemsPhoneContactSection.add(objItem);
				}
			} 
			
			if (null == mAdapter) {
				mAdapter = new PhoneContactListAdapter(PhoneContactList.this, itemsPhoneContactSection);
				mPhoneContactListView.setAdapter(mAdapter);
			} else {
				mAdapter.notifyDataSetChanged();
			}
		}

	 
	public void updateListMontyChatStatus(ArrayList<XmppPhoneContact> contactList,String result) {
		String phoneList = result;
		String phoneWithCode = "";
		while(phoneList.length() > 0) {
			//Log.v(TAG, "phoneList " + result);
			if(phoneList.contains(";")) {
				phoneWithCode = phoneList.substring(0, phoneList.indexOf(";"));
				phoneList = phoneList.substring(phoneList.indexOf(";")+1, phoneList.length());
				//Log.v(TAG, "phoneWithCode " + phoneWithCode);
				//Log.v(TAG, "phoneList " + phoneList);
			} else {
				break;
			}
			String number = getNumber(phoneWithCode);
			//Log.v(TAG, "number " + number);
			for (ListIterator<XmppPhoneContact> itr1 = contactList.listIterator(); itr1.hasNext();) {
				XmppPhoneContact contact = itr1.next();
				if(contact.phoneNumber != null) {
					for (ListIterator<String> itr2 = contact.phoneNumber.listIterator(); itr2.hasNext();) {
						String conNumber = itr2.next();
						if(number.contains(conNumber) && (contact.montyChatUserNumber == null)) {
							//Log.d(TAG, "Phone Number :" + number + "of length" + number.length());
							contact.montyChatUserNumber = number.trim();
						}
					}
				}
			}
		
		}
	}
	
	//private ArrayList<XmppPhoneContact> getContactList() {
	private ArrayList<XmppPhoneContact> getUpdatedContactList() {
		phoneContacts = PhoneContacts.getInstance(getApplicationContext());
		ArrayList<XmppPhoneContact>contactList = phoneContacts.getContactList();
		ArrayList<XmppPhoneContact> cloneList = new ArrayList<XmppPhoneContact>(contactList.size());
		for (ListIterator<XmppPhoneContact> itr1 = contactList.listIterator(); itr1.hasNext();) {
			XmppPhoneContact contact = itr1.next();
			//don't add duplicate entries
			boolean duplicateEntryFound = false;
			for (ListIterator<XmppPhoneContact> itr2 = cloneList.listIterator(); itr2.hasNext();) {
				XmppPhoneContact entry = itr2.next();
				if((entry.phoneNumber.size() > 0) && (contact.phoneNumber.size() > 0)) {
					if(entry.phoneNumber.get(0).contentEquals(contact.phoneNumber.get(0))) {
						duplicateEntryFound = true;
						break;
					}else {
						if(entry.phoneNumber.get(0).length() > contact.phoneNumber.get(0).length()){
							if(entry.phoneNumber.get(0).contains(contact.phoneNumber.get(0))) {
								duplicateEntryFound = true;
								break;
							}
						}else {
							if(contact.phoneNumber.get(0).contains(entry.phoneNumber.get(0))) {
								duplicateEntryFound = true;
								break;
							}
						}
					}
				}
			}
			if(!duplicateEntryFound) {
			cloneList.add(new XmppPhoneContact(contact));
			}
		}
		return cloneList;
	}
	
	public String getNumber(String phoneList){
		String phone = "";
		if(phoneList.contains("#")) {
			int index = phoneList.indexOf("#");
			phone = phoneList.substring(0, index) + phoneList.substring(index+1, phoneList.length());
		} else {
			phone = phoneList;
		}
		return phone;
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
		isRunning = true;
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(Constants.PHONE_CONTACTS_CHANGED);
		registerReceiver(mContactChangeReceiver, iFilter);
	}

	@Override
	protected void onPause() {
		//doUnbindService();
		isRunning = false;
		//Log.v(TAG, "on Pause called");
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mPhoneContactSearchText.getWindowToken(), 0);
		unregisterReceiver(mContactChangeReceiver);
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.pull_in_from_left,
				R.anim.pull_out_to_right);
	}
	
	@Override
	protected void onDestroy() {
		doUnbindService();
		super.onDestroy();
	}

	public static boolean isRunning() {
		return isRunning;
	}

	
	private class PhoneContactItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
			if (arg1.getTag().getClass().getSimpleName().equals("ViewHolder")) {
				XmppPhoneContact entry = (XmppPhoneContact) arg0.getItemAtPosition(arg2);
				//open callScreen to make call to selected contact
				int callState = telephonyManager.getCallState();
				if(callState != TelephonyManager.CALL_STATE_IDLE)
				{
				    Toast.makeText(getApplicationContext(), "Cellular Call is in Active State, CallLebanon is unavailable", Toast.LENGTH_SHORT).show();
				    return;
				} else {
					Bundle bundle = new Bundle();
					bundle.putString(CallService.k1, entry.phoneNumber.get(0));
					bundle.putString(CallService.k2, entry.name);
					sendMessageToService(CallService.MSG_MAKE_CALL, 0, 0, bundle);
				}
			}
		}
	}
	
	private class ContactsChangedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Constants.PHONE_CONTACTS_CHANGED)) {
				//String result = PresenceSubscribeStore.getInstance(getApplicationContext()).getMontyUsersContacts();
				mPhoneAllContacts = getUpdatedContactList();
				setupPhoneContactList();
				setAdapterToListview(mPhoneAllContacts);
			}			
		}		
	}
}
