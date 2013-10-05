package com.montycall.android.lebanoncall;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.montycall.android.lebanoncall.constants.Constants;
import com.montycall.android.lebanoncall.service.CallService;

public final class ProfileListAdapter extends BaseAdapter {
	
	private static final String LOG_TAG = "ProfileListAdapter";
	
	private ArrayList<String> mUserInfo = new ArrayList<String>();
	private Context mContext = null;

	public ProfileListAdapter(Context context) {
		mContext = context;
	}
	
	public void initializeContent(ArrayList<String> contacts) {
		mUserInfo = contacts;
		
	}
	
	public void clear() {
		mUserInfo = null;
		
	}
	
	@Override
	public int getCount() {
		return mUserInfo.size();
	}

	@Override
	public Object getItem(int position) {
		return mUserInfo.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	static class ViewHolder {
		TextView name;
		ImageView photo;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.v(LOG_TAG, "position  = " + position);
		ViewHolder holder = new ViewHolder();
		if (convertView == null) {
			convertView = (View) LayoutInflater.from(mContext).inflate(
					R.layout.profile_list_item_layout, null, false);
			holder.name = (TextView) convertView
					.findViewById(R.id.profile_name);
			holder.photo = (ImageView) convertView
					.findViewById(R.id.profile_status);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		switch(position) {
		case 0:
			holder.name.setText("  Account ID: " + CallService.getSipUserName());
			holder.photo.setVisibility(4);
			break;
		case 1:			
			holder.name.setText("  Account Email: " + CallService.SIP_EMAIL);
			holder.photo.setImageResource(R.drawable.content_edit);
			holder.photo.setVisibility(1);
			break;
		case 2:
			SharedPreferences settings = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
			String balance = settings.getString(Constants.CURRENT_BALANCE, "");
			holder.name.setText("  Current Balance: " + "\"" + balance + " USD\"");
			holder.photo.setVisibility(4);
			break;
		case 3:
			holder.name.setText("  Check Call Rates  ");
			holder.photo.setImageResource(R.drawable.content_edit);
			holder.photo.setVisibility(1);
			break;
		case 4:
			SharedPreferences setting = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
			String defaultCallerId = setting.getString(Constants.DEFAULT_CALLER_ID, "");
			
			holder.name.setText("  Set Caller Id: " + defaultCallerId);
			holder.photo.setImageResource(R.drawable.content_edit);
			holder.photo.setVisibility(1);
			break;
		case 5:
			holder.name.setText("  Buy More Credits ");
			holder.photo.setImageResource(R.drawable.content_edit);
			holder.photo.setVisibility(1);
			break;
		case 6:
			holder.name.setText("  Contact Us ");
			holder.photo.setVisibility(4);
			break;
		default:
			holder.name.setVisibility(4);
			holder.photo.setVisibility(4);
			break;
		}
		return convertView;
	}
}
