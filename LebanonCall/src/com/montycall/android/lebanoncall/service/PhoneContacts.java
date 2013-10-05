package com.montycall.android.lebanoncall.service;

import java.util.ArrayList;
import java.util.HashSet;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.montycall.android.lebanoncall.XmppPhoneContact;
import com.montycall.android.lebanoncall.constants.Constants;

/**
 * This class basically is a storehouse of the phone contacts which we store in
 * our cache so that we can display them quickly.
 * 
 */
public class PhoneContacts {

	private static final String TAG = "PhoneContacts";
	
	private static PhoneContacts mySelf;

	private ArrayList<XmppPhoneContact> mContactList;

	private Context mContext;
	
	private boolean mFetchingContacts = false;
	
	Messenger mChatService = null;
	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mChatService = new Messenger(service);
		}

		public void onServiceDisconnected(ComponentName className) {
			mChatService = null;
		}
	};

	private PhoneContacts(Context context) {
		mContext = context;
		mContactList = new ArrayList<XmppPhoneContact>();
		FetchPhoneContactList fetchPhoneContactList = new FetchPhoneContactList();
		fetchPhoneContactList.start();
	}

	public void fetchContacts() {
		if(!mFetchingContacts) {
			FetchPhoneContactList fetchPhoneContactList = new FetchPhoneContactList();
			fetchPhoneContactList.start();
		}
	}
	public synchronized static PhoneContacts getInstance(Context context) {
		if (mySelf == null) {
			mySelf = new PhoneContacts(context);
		}
		return mySelf;
	}
	
	public String getAllPhoneNumberString() {
		ArrayList<String> list = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		ArrayList<XmppPhoneContact> contactList = getContactList();
		for(XmppPhoneContact contact : contactList) {
			for(String number : contact.phoneNumber) {
				list.add(number);
			}
		}
		HashSet<String> h = new HashSet<String>(list);
		list.clear();
		list.addAll(h);
		for(String number : list) {
			sb.append(number);
			sb.append(";");
		}
		if(sb.length() > 0) {
			return sb.toString().substring(0, sb.length()-1);
		} else {
			return "";	// return an empty string
		}		
	}

	public synchronized ArrayList<XmppPhoneContact> getContactList() {
		return (new ArrayList<XmppPhoneContact>(mContactList));
	}
	
	public synchronized void setContactList(ArrayList<XmppPhoneContact> contactList) {
		mContactList.clear();
		mContactList = null;
		mContactList = contactList;
	}

	class FetchPhoneContactList extends Thread {

		@Override
		public void run() {
			mFetchingContacts = true;
			mContext.bindService(new Intent(mContext, CallService.class), mConnection,
					Context.BIND_AUTO_CREATE);
			ArrayList<XmppPhoneContact> contactList = new ArrayList<XmppPhoneContact>();
			ContentResolver cr = mContext.getContentResolver();
			Cursor nameCursor = null;
			if(Build.VERSION.SDK_INT < 11) {
				nameCursor = cr.query(Phone.CONTENT_URI, new String[] {Phone._ID,
						Phone.DISPLAY_NAME, Phone.CONTACT_ID }, null, null, null);
			} else {
				nameCursor = cr.query(Phone.CONTENT_URI, new String[] {Phone._ID,
						Phone.DISPLAY_NAME, Phone.PHOTO_THUMBNAIL_URI }, null, null, null);
			}

			if (nameCursor != null) {
				while (nameCursor.moveToNext()) {
					XmppPhoneContact contact = new XmppPhoneContact();

					String name = nameCursor.getString(nameCursor
							.getColumnIndex(Phone.DISPLAY_NAME));
					String photo_id;
					if(Build.VERSION.SDK_INT < 11) {
						photo_id = nameCursor.getString(nameCursor
								.getColumnIndex(Phone.CONTACT_ID));
					} else {
						photo_id = nameCursor.getString(nameCursor
								.getColumnIndex(Phone.PHOTO_THUMBNAIL_URI));
					}
					String id = nameCursor.getString(nameCursor
							.getColumnIndex(Phone._ID));
					
					contact.name = name;
					contact.photo_id = photo_id;
					Cursor cCursor = cr.query(Phone.CONTENT_URI, new String[] {
							Phone.NUMBER, Phone.TYPE }, "_id='" + id
							+ "'", null, null);
					
					if (cCursor != null) {
						ArrayList<String> numberList = new ArrayList<String>();
						while (cCursor.moveToNext()) {
							String phoneNumber = cCursor.getString(cCursor
									.getColumnIndex(Phone.NUMBER));
							int length = phoneNumber.length();
							StringBuffer phone = new StringBuffer(length);
							for (int i = 0; i < length; i++) {
								char ch = phoneNumber.charAt(i);
								if (Character.isDigit(ch)) {
									phone.append(ch);
								}
							}
							String phoneNum = phone.toString();
							numberList.add(phoneNum);
							
						}
						contact.phoneNumber = numberList;
						contactList.add(contact);
					}
					cCursor.close();
				}
				nameCursor.close();
			}
			setContactList(contactList);
			Intent intent = new Intent(Constants.PHONE_CONTACTS_CHANGED);
			mContext.sendBroadcast(intent);
			Message msg = Message.obtain(null, CallService.MSG_PHONE_CONTACTS_FETCHED);
			try {
				if(mChatService != null) {
					mChatService.send(msg);
				}
			} catch (RemoteException e) {
				//Log.e(TAG, "Error while sending mesage to service. Msg Type: MSG_PHONE_CONTACTS_FETCHED");
			}
			mContext.unbindService(mConnection);
			mFetchingContacts = false;
		}
	}
}
