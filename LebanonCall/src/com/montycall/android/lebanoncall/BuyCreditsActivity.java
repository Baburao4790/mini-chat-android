package com.montycall.android.lebanoncall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.montycall.android.lebanoncall.constants.Constants;
import com.montycall.android.lebanoncall.service.CallService;
import com.montycall.android.lebanoncall.util.IabHelper;
import com.montycall.android.lebanoncall.util.IabResult;
import com.montycall.android.lebanoncall.util.Inventory;
import com.montycall.android.lebanoncall.util.Purchase;
import com.montycall.android.lebanoncall.util.Security;
import com.montycall.android.lebanoncall.util.SkuDetails;

public class BuyCreditsActivity extends SherlockActivity {

	static IabHelper mHelper;
	IabHelper.QueryInventoryFinishedListener mQueryFinishedListener;
	private static final String TAG = "BuyCreditsActivity";
	private BuyCreditsActivityAdapter mAdapter;
	private ArrayList<SkuDetails> mSkuList = new ArrayList<SkuDetails>();
	private static Context mContext;
	private static Context context;
	static Activity mySelf;
	private ListView mListView;
	private static TextView mBalanceView;
	private static ProgressBar mProgress;
	static final int RC_REQUEST = 10001;
	private static Purchase mPurchase;

	private static final String SKU_CREDIT_1_USD = "com.montyholding.lebanoncall_1usd";
	private static final String SKU_CREDIT_2_USD = "com.montyholding.lebanoncall_2usd";
	private static final String SKU_CREDIT_3_USD = "com.montyholding.lebanoncall_3usd";
	private static final String SKU_CREDIT_4_USD = "com.montyholding.lebanoncall_4usd";
	private static final String SKU_CREDIT_5_USD = "com.montyholding.lebanoncall_5usd";

	/** Start of Service Related Processing and messaging */

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	static final Messenger mMessenger = new Messenger(new IncomingHandler());
	static Messenger mChatService;
	static boolean mIsBound;
	static RandomString ra;
	
	/**
	 * Handler of incoming messages from service.
	 */
	static class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CallService.MSG_BALANCE_FETCHED:
				//Log.v(TAG, "ChatService.MSG_BALANCE_FETCHED");
				SharedPreferences settings = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
				String balance = settings.getString(Constants.CURRENT_BALANCE, "");
				mBalanceView.setText("Current Balance : " + balance + " USD");
				break;

			default:
				break;

			}
		}
	}

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mChatService = new Messenger(service);
			mIsBound = true;
			sendMessageToService(CallService.MSG_REGISTER_CLIENT, 0, 0, null);
		}

		public void onServiceDisconnected(ComponentName className) {
			mChatService = null;
		}
	};

	void doBindService() {
		Intent service = new Intent(this, CallService.class);
		startService(service);
		getApplicationContext().bindService(new Intent(this, CallService.class), mConnection, Context.BIND_AUTO_CREATE);
	}

	void doUnbindService() {
		if (mIsBound) {
			if (mChatService != null) {
				sendMessageToService(CallService.MSG_UNREGISTER_CLIENT, 0, 0, null);
			}
			getApplicationContext().unbindService(mConnection);
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
			msg.replyTo = mMessenger;
			try {
				mChatService.send(msg);
			} catch (RemoteException e) {
				/** Don;t do nothing :) */
			}
		}
	}

	/** End of Service Related Processing and messaging */

	static IabHelper.OnConsumeFinishedListener sConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {

		@Override
		public void onConsumeFinished(Purchase purchase, IabResult result) {
			//Log.d(TAG, "Consume Finished");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_buycredits);
		setTitle("");
		doBindService();
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		BitmapDrawable bg = (BitmapDrawable) getResources().getDrawable(R.drawable.titlebar);
		//bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		getSupportActionBar().setBackgroundDrawable(bg);
		mListView = (ListView) findViewById(R.id.buy_credit_list);
		mProgress = (ProgressBar) findViewById(R.id.progressBar1);
		mBalanceView = (TextView) findViewById(R.id.current_bought_balance);
		String base64EncodedPublicKey = MontyNative.getPrivateKey();
		context = this;
		mContext = getApplicationContext();
		ra = new RandomString(20);
		setSupportProgressBarIndeterminateVisibility(true);
		mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
			public void onQueryInventoryFinished(IabResult result,
					Inventory inventory) {
				if (result.isFailure()) {
					// handle error
					return;
				}
				if (inventory == null) {
					//Log.e(TAG, "inventory is NULL");
				} else {
					try {
						List<Purchase> purchaseList = new ArrayList<Purchase>();
						if(inventory.hasPurchase(SKU_CREDIT_1_USD)) {
							purchaseList.add(inventory.getPurchase(SKU_CREDIT_1_USD));
						} 
						if(inventory.hasPurchase(SKU_CREDIT_2_USD)) {
							purchaseList.add(inventory.getPurchase(SKU_CREDIT_2_USD));
						}
						if(inventory.hasPurchase(SKU_CREDIT_3_USD)) {
							purchaseList.add(inventory.getPurchase(SKU_CREDIT_3_USD));
						}
						if(inventory.hasPurchase(SKU_CREDIT_4_USD)) {
							purchaseList.add(inventory.getPurchase(SKU_CREDIT_4_USD));
						}
						if(inventory.hasPurchase(SKU_CREDIT_5_USD)) {
							purchaseList.add(inventory.getPurchase(SKU_CREDIT_5_USD));
						}
						
						mHelper.consumeAsync(purchaseList, new IabHelper.OnConsumeMultiFinishedListener() {
							@Override
							public void onConsumeMultiFinished(List<Purchase> purchases,
									List<IabResult> results) {
								/* Don;t do anything */
							}
						});
					} catch (Exception e) {
						
					}
					
					mSkuList.clear();
					mSkuList.add(inventory.getSkuDetails(SKU_CREDIT_1_USD));
					mSkuList.add(inventory.getSkuDetails(SKU_CREDIT_2_USD));
					mSkuList.add(inventory.getSkuDetails(SKU_CREDIT_3_USD));
					mSkuList.add(inventory.getSkuDetails(SKU_CREDIT_4_USD));
					mSkuList.add(inventory.getSkuDetails(SKU_CREDIT_5_USD));					
					if (mAdapter == null) {
						mAdapter = new BuyCreditsActivityAdapter(mContext, mSkuList);						
						mListView.setAdapter(mAdapter);
						setSupportProgressBarIndeterminateVisibility(false);
					}
				}
			}
		};
		mHelper = new IabHelper(this, base64EncodedPublicKey);
		mHelper.enableDebugLogging(false);
		Log.v("mHelper", "Starting In-App Billin Setup");
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					// Oh no, there was a problem.
					//Log.d(TAG, "Problem setting up In-app Billing: " + result);
				} else {
					// Hooray, IAB is fully set up!
					//Log.d(TAG, "In-app Billing setup successfull: " + result);
					List<String> skuList = new ArrayList<String>();
					skuList.add(SKU_CREDIT_1_USD);
					skuList.add(SKU_CREDIT_2_USD);
					skuList.add(SKU_CREDIT_3_USD);
					skuList.add(SKU_CREDIT_4_USD);
					skuList.add(SKU_CREDIT_5_USD);
					mHelper.queryInventoryAsync(true, skuList, mQueryFinishedListener);
				}
			}
		});
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

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.pull_in_from_left,
				R.anim.pull_out_to_right);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mySelf = this;
		SharedPreferences settings = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
		String balance = settings.getString(Constants.CURRENT_BALANCE, "");
		mBalanceView.setText("Current Balance : " + balance + " USD");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
		if (mHelper != null)
			mHelper.dispose();
		mHelper = null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
			// not handled, so handle it ourselves (here's where you'd
			// perform any handling of activity results not related to in-app
			// billing...
			super.onActivityResult(requestCode, resultCode, data);
		} else {
			//Log.d(TAG, "onActivityResult handled by IABUtil.");
		}
	}

	static class BuyCreditsActivityAdapter extends ArrayAdapter<SkuDetails> {

		private static final String TAG = "BuyCreditsActivityAdapter";

		private Context mContext;
		private ArrayList<SkuDetails> mList;
		private SkuDetails skuDetail;
		ViewHolder holder;

		public BuyCreditsActivityAdapter(Context context,
				ArrayList<SkuDetails> skus) {
			super(context, 0, skus);
			mContext = context;
			mList = skus;
		}

		public static class ViewHolder {
			TextView skuName;
			TextView skuDescription;
			ImageButton buyButton;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			skuDetail = mList.get(position);

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = (View) LayoutInflater.from(mContext).inflate(
						R.layout.buycredits_item_layout, null, false);
				holder.skuName = (TextView) convertView
						.findViewById(R.id.sku_name);
				holder.skuDescription = (TextView) convertView
						.findViewById(R.id.sku_description);
				holder.buyButton = (ImageButton) convertView
						.findViewById(R.id.sku_buy_button);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.skuName.setText(skuDetail.getDescription());
			holder.skuDescription.setText(skuDetail.getPrice());
			holder.buyButton.setTag(skuDetail);
			setButtonOnClickListener(holder);
			return convertView;
		}

		IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
			public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
				mProgress.setVisibility(View.GONE);
				//Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
				
				if(purchase == null) {
					return;
				}
				boolean isSigned = verifyPurchaseSignature(purchase);
				if(!isSigned) {
					Log.v(TAG, "Trying to fool me eh? bad luck!");
					return;
				}
				mPurchase = purchase;				
				if (result.isFailure()) {
					return;
				}
				String sku = purchase.getSku();
				if (sku != null) {
					VerifyPurchase vp = new VerifyPurchase();
					try {
						String productId = new JSONObject(purchase.getOriginalJson()).getString("productId");
						String[] params = new String[]{
								purchase.getPackageName(),
								productId,
								purchase.getToken(),
								CallService.getSipUserName()								
						};
						vp.execute(params);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					
				}
			}
		};

		void setButtonOnClickListener(ViewHolder holder) {
			holder.buyButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//Log.d(TAG, "Launching purchase flow for sku.");
					SkuDetails vh = (SkuDetails) v.getTag();
					//Log.v(TAG, "vh = " + vh);
					mProgress.setVisibility(View.VISIBLE);
					String payload = "";
					payload = CallService.getSipUserName();				
					payload += "#" + ra.nextString();
					//Log.v(TAG, "Sent payload " + payload);
					mHelper.launchPurchaseFlow(mySelf, vh.getSku(), RC_REQUEST, mPurchaseFinishedListener, payload );
				}
			});
		}
	}

	static class GeneratePurchaseTransactionId extends AsyncTask<String, String, String> {

		String result = null;

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (TextUtils.equals(result, "ERROR")) {

			} else {
				VerifyPurchase vp = new VerifyPurchase();
				vp.execute(result);
			}

		}

		@Override
		protected String doInBackground(String... params) {
			String url1 = Constants.PURCHASE_TOKEN_GENERATE_URL;
			String url2 = url1.replace("SIP_USER_NAME",
					CallService.getSipUserName());
			String url = url2.replace("RECHARGE", params[0]);
			//Log.v(TAG, "URL = " + url);
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);

			try {
				HttpResponse httpResponse = httpClient.execute(httpGet);
				InputStream inputStream = httpResponse.getEntity().getContent();

				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);

				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);

				StringBuilder stringBuilder = new StringBuilder();

				String bufferedStrChunk = null;
				while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
					stringBuilder.append(bufferedStrChunk);
				}
				result = stringBuilder.toString();
				//Log.d(TAG, "HttpResponse :" + result);
			} catch (ClientProtocolException cpe) {
				//Log.e(TAG, "Exception generates because of httpResponse :" + cpe);
				cpe.printStackTrace();
			} catch (IOException ioe) {
				//Log.e(TAG, "Exception generates because of httpResponse :" + ioe);
				ioe.printStackTrace();
			}
			return result;
		}
	}

	static class VerifyPurchase extends AsyncTask<String, String, String> {

		String result = null;

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				
				JSONObject json = new JSONObject(result);
				if(json.getBoolean("restResponse")) // purchased has been done successfully
				{
					mHelper.consumeAsync(mPurchase, sConsumeFinishedListener);
					Builder alert = new AlertDialog.Builder(context);
					alert.setTitle("Buy more credits")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							dialog.dismiss();
							
						}
					})
					.setMessage("Your account has been topped up successfully" + 
					 "\nWith " + json.getInt("topupAmount") + " USD");
					alert.create().show();
				} else {
					Builder alert = new AlertDialog.Builder(context);
					alert.setTitle("Buy more credits")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							dialog.dismiss();							
						}
					})
					.setMessage("You purchase has not been completed !");
					alert.create().show();					
				}
				
				sendMessageToService(CallService.MSG_CHECK_BALANCE, 0, 0, null);
			} catch (JSONException e) {
				e.printStackTrace();
			}			
		}

		private String md5(String in) {
			MessageDigest digest;
			try {
				digest = MessageDigest.getInstance("MD5");
				digest.reset();
				digest.update(in.getBytes());
				byte[] a = digest.digest();
				int len = a.length;
				StringBuilder sb = new StringBuilder(len << 1);
				for (int i = 0; i < len; i++) {
					sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
					sb.append(Character.forDigit(a[i] & 0x0f, 16));
				}
				return sb.toString();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected String doInBackground(String... params) {
			String url1 = Constants.PURCHASE_ADD_URL;			
			try {
				String url = String.format(url1, (Object[])params);
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);

				HttpResponse httpResponse = httpClient.execute(httpGet);
				InputStream inputStream = httpResponse.getEntity().getContent();

				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);

				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);

				StringBuilder stringBuilder = new StringBuilder();

				String bufferedStrChunk = null;
				while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
					stringBuilder.append(bufferedStrChunk);
				}
				result = stringBuilder.toString();
				//Log.d(TAG, "HttpResponse :" + result);
			} catch (ClientProtocolException cpe) {
				//Log.e(TAG, "Exception generates because of httpResponse :" + cpe);
				cpe.printStackTrace();
			} catch (IOException ioe) {
				//Log.e(TAG, "Exception generates because of httpResponse :" + ioe);
				ioe.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
				//Log.e(TAG, "Exception" + ex.getMessage());
			}
			return result;
		}
	}

	private static String getRechargeAmountFromSku(String sku) {
		if (sku == null) {
			return "0";
		} else if (sku.equalsIgnoreCase(SKU_CREDIT_1_USD)) {
			return "1";
		} else if (sku.equalsIgnoreCase(SKU_CREDIT_2_USD)) {
			return "2";
		} else if (sku.equalsIgnoreCase(SKU_CREDIT_3_USD)) {
			return "3";
		} else if (sku.equalsIgnoreCase(SKU_CREDIT_4_USD)) {
			return "4";
		} else if (sku.equalsIgnoreCase(SKU_CREDIT_5_USD)) {
			return "5";
		}
		return "0";
	}
	
	static boolean verifyPurchaseSignature(Purchase purchase) {
		String pubkey = MontyNative.getPrivateKey();
		String signedData = purchase.getOriginalJson();
		String signature = purchase.getSignature();
		return Security.verifyPurchase(pubkey, signedData, signature);
	}
	
	public final class RandomString
	{

	  /* Assign a string that contains the set of characters you allow. */
	  private static final String symbols = "ABCDEFGJKLMNPRSTUVWXYZ0123456789"; 

	  private final SecureRandom random = new SecureRandom();

	  private final char[] buf;

	  public RandomString(int length)
	  {
	    if (length < 1)
	      throw new IllegalArgumentException("length < 1: " + length);
	    buf = new char[length];
	  }

	  public String nextString()
	  {
	    for (int idx = 0; idx < buf.length; ++idx) 
	      buf[idx] = symbols.charAt(random.nextInt(symbols.length()));
	    return new String(buf);
	  }

	}
	
	static {
		System.loadLibrary("montynative");
	}
}