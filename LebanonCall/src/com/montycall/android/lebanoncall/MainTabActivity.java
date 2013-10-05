package com.montycall.android.lebanoncall;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

/**
 * An example of tab content that launches an activity via {@link android.widget.TabHost.TabSpec#setContent(android.content.Intent)}
 */

public class MainTabActivity extends TabActivity {
	public static final String DEFAULT_TAB = "com.montychat.android.client.MainTabActivity.DEFAULT_TAB";
	private static TabHost tabHost = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String defaultTab;
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
        	defaultTab = "dialer";
        } else {
        	defaultTab = extras.getString(DEFAULT_TAB);
        } 		

        tabHost = getTabHost();

        LayoutParams param1 = tabHost.getLayoutParams();
        param1.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        tabHost.setLayoutParams(param1);
        
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator("",getResources().getDrawable(R.drawable.tab_favorite_selector))
                .setContent(new Intent(getApplicationContext(), CallLogsList.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                ));
        
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator("",getResources().getDrawable(R.drawable.tab_chat_selector))
                .setContent(new Intent(getApplicationContext(), DialPad.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                ));
        
        tabHost.addTab(tabHost.newTabSpec("tab4")
                .setIndicator("",getResources().getDrawable(R.drawable.tab_contact_selector))
                .setContent(new Intent(getApplicationContext(), PhoneContactList.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                ));
        
        tabHost.addTab(tabHost.newTabSpec("tab5")
                .setIndicator("",getResources().getDrawable(R.drawable.tab_profile_selector))
                .setContent(new Intent(getApplicationContext(), ProfileActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                ));
        
       
        for(int i=0;i<tabHost.getTabWidget().getChildCount();i++)
        {
        	if(Build.VERSION.SDK_INT >= 11) {
        		LayoutParams param = tabHost.getTabWidget().getChildAt(i).getLayoutParams();
        		param.height = 130;
        		tabHost.getTabWidget().getChildAt(i).setLayoutParams(param);
        	}
            //tabHost.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(R.color.actionbar));
            tabHost.getTabWidget().getChildAt(i).setBackgroundDrawable(getResources().getDrawable(R.drawable.titlebar));
        }
        
        if(defaultTab != null && defaultTab.equalsIgnoreCase("chat")) {
        	tabHost.setCurrentTab(0);
        	//tabHost.getTabWidget().getChildAt(1).setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_over));
        	tabHost.getTabWidget().getChildAt(0).setBackgroundResource(R.drawable.tab_background1);
         }else {
        	tabHost.setCurrentTab(1);
        	//tabHost.getTabWidget().getChildAt(2).setBackgroundDrawable(getResources().getDrawable(R.drawable.friends_over));
        	tabHost.getTabWidget().getChildAt(1).setBackgroundResource(R.drawable.tab_background1);
        }
        

        tabHost.setOnTabChangedListener(new OnTabChangeListener(){
        	@Override
        	public void onTabChanged(String tabId) {
        		for(int i=0;i<tabHost.getTabWidget().getChildCount();i++)
                {
        			//tabHost.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(R.color.actionbar));
        			tabHost.getTabWidget().getChildAt(i).setBackgroundDrawable(getResources().getDrawable(R.drawable.titlebar));
                }

        		tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundResource(R.drawable.tab_background1);
        }});
    
    }
    
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
    	finish();
    }
    
}
