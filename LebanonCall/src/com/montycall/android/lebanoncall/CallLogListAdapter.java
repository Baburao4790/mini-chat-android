package com.montycall.android.lebanoncall;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CallLogListAdapter extends ArrayAdapter<CallLogEntry>{

	private static final String LOG_TAG = "CallLogListAdapter";

	private ArrayList<CallLogEntry> mBuddies ;
	private static Context mContext = null;
	private CallLogEntry objItem;
	public ImageLoader imageLoader; 

	public CallLogListAdapter(Context context, ArrayList<CallLogEntry> contacts) {
		super(context, 0, contacts);
		mContext = context;
		mBuddies = contacts;
		imageLoader=new ImageLoader(context.getApplicationContext());
	}

	ViewHolder holder;

	public static class ViewHolder {
		TextView name;
		TextView phone;
		TextView date;
		TextView duration;
		ImageView photo;
		ImageView status;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		objItem = (CallLogEntry) mBuddies.get(position);

			if (convertView == null ) {     
				holder = new ViewHolder();
				convertView = (View) LayoutInflater.from(mContext).inflate(
						R.layout.calllog_list_item_layout, null, false);
				holder.name = (TextView) convertView.findViewById(R.id.friend_name);
				holder.phone = (TextView) convertView.findViewById(R.id.friend_phone);
				holder.date = (TextView) convertView.findViewById(R.id.call_date);
				holder.duration = (TextView) convertView.findViewById(R.id.call_duration);
				holder.photo = (ImageView) convertView
						.findViewById(R.id.friend_pic);
				holder.status = (ImageView) convertView
						.findViewById(R.id.friend_status);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
	
			CallLogEntry entry = (CallLogEntry) objItem; 
			holder.photo.setImageResource(R.drawable.person_icon);
			setPicture(entry.number, holder);
			holder.status.setBackgroundResource(R.drawable.phoneicon);
			holder.phone.setText(entry.number);
			holder.name.setText(entry.name);
			Date changeToDate = new Date(entry.last_call_date);
			CharSequence s  = DateFormat.format("MMM dd, h:mmaa", changeToDate.getTime());
			holder.date.setText(s);
			
			long elapsed = entry.last_call_duration/1000;
			String display = String.format("%02d:%02d:%02d", elapsed / 3600, (elapsed % 3600) / 60, (elapsed % 60));
			holder.duration.setText(display);
			

		return convertView;
	}
	
	void setPicture(String number, ViewHolder holder) {
		String[] projection = new String[] {
		        ContactsContract.PhoneLookup._ID};
		String contactId = "";

		// encode the phone number and build the filter URI
		Uri conUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

		// query time
		Cursor cursor = mContext.getContentResolver().query(conUri, projection, null, null, null);

		if(cursor != null) {
			if (cursor.moveToFirst()) {
				contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
			} else {
			    Log.v(LOG_TAG, "Contact Not Found @ " + number);
			}
			cursor.close();
		}
		
		try {
			Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, Long.parseLong(contactId));
			Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
			Cursor cur = mContext.getContentResolver().query(photoUri, new String[] {Contacts.Photo.DATA15}, null, null, null);
			if (cur == null) {
			    return;
			}
			try {
			    if (cur.moveToFirst()) {
			        byte[] data = cur.getBlob(0);
			        if (data != null) {
			            holder.photo.setImageBitmap(BitmapFactory.decodeStream( new ByteArrayInputStream(data)));
			        }
			    }
			} finally {
			    cursor.close();
			}
		} catch (Exception e) {
			/* Well, don;t display the photo. What else? */
		}
	}
}
