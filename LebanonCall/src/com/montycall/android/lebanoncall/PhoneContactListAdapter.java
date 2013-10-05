package com.montycall.android.lebanoncall;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PhoneContactListAdapter extends ArrayAdapter<IXmppPhoneContact> {

	private static final String LOG_TAG = "PhoneContactListAdapter";

	private ArrayList<IXmppPhoneContact> mBuddies ;
	private static Context mContext = null;
	private IXmppPhoneContact objItem;
	public ImageLoader imageLoader; 

	public PhoneContactListAdapter(Context context, ArrayList<IXmppPhoneContact> contacts) {
		super(context, 0, contacts);
		mContext = context;
		mBuddies = contacts;
		imageLoader=new ImageLoader(context.getApplicationContext());
		
	}

	ViewHolder holder;

	public static class ViewHolder {
		TextView name;
		TextView phone;
		ImageView photo;
		ImageView status;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		objItem = (IXmppPhoneContact) mBuddies.get(position);

			if (convertView == null ) {     
				holder = new ViewHolder();
				convertView = (View) LayoutInflater.from(mContext).inflate(
						R.layout.favorite_list_item_layout, null, false);
				holder.name = (TextView) convertView.findViewById(R.id.friend_name);
				holder.phone = (TextView) convertView
						.findViewById(R.id.friend_phone);
				holder.photo = (ImageView) convertView
						.findViewById(R.id.friend_pic);
				holder.status = (ImageView) convertView
						.findViewById(R.id.friend_status);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
	
			XmppPhoneContact entry = (XmppPhoneContact) objItem; 
			if((entry.avatar != null) || ((entry.photo_id != null)&&(!entry.photo_id.equalsIgnoreCase("")))){
				imageLoader.DisplayImage(entry.avatar, holder.photo, entry.phoneNumber.get(0), entry.photo_id, 0);
			}else {
				holder.photo.setImageResource(R.drawable.person_icon);
			}
			
			holder.status.setBackgroundResource(R.drawable.phoneicon);
			holder.phone.setText(entry.phoneNumber.get(0));
			holder.name.setText(entry.name);

		return convertView;
	}

}
