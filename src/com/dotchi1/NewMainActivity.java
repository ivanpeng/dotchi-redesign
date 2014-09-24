package com.dotchi1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.facebook.Session;
import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

@SuppressWarnings("unused")
public class NewMainActivity extends ActionBarActivity {
	
	public static final String TAG = "NewMainActivity";
	
	private static final int[] bottomIds = {R.id.new_home_feed_button, R.id.new_invite_friends_button, 
			R.id.new_messages_button, R.id.new_dotchi_package_button};
	private static final Class<?>[] fragmentClasses = {NewFeedFragment.class, NewInviteFriendsFragment.class,
			NewNotificationsFragment.class, NewDotchiPackageSelectFragment.class};
	private int selectedBottomIndex = 0;
	
	public static final int MAIN_ACTIVITY_REQ_CODE = 100;
	public static final int CREATE_GROUP_REQ_CODE = 10000;
	public static final int FINAL_DECISION_REQ_CODE = 10001;
	private static final int UPDATE_COUNT = 10;
	
	private static FragmentManager mFragmentManager;
	
	private String dotchiId;
	
	private SlidingMenu slidingMenu;

	
	public void setupUI(View view) {
	    //Set up touch listener for non-text box views to hide keyboard.
	    if(!(view instanceof EditText)) {
	        view.setOnTouchListener(new OnTouchListener() {
	            public boolean onTouch(View v, MotionEvent event) {
	                //ViewUtils.hideSoftKeyboard(NewMainActivity.this);
	                return false;
	            }
	        });
	    }
	    //If a layout container, iterate over children and seed recursion.
	    if (view instanceof ViewGroup) {
	        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
	            View innerView = ((ViewGroup) view).getChildAt(i);
	            setupUI(innerView);
	        }
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_home_feed);
		LayoutInflater inflater = getLayoutInflater();
		
		mFragmentManager = getSupportFragmentManager();

	   	slidingMenu = new SlidingMenu(this);
	   	slidingMenu.setMode(SlidingMenu.LEFT);
	   	slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
	   	slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
	   	//slidingMenu.setMenu(R.layout.activity_test);
        slidingMenu.setMenu(makeMenuLayout());
	   	slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		View actionbar = inflater.inflate(R.layout.menu_new_feed, null);

		ImageView navigationDrawer = (ImageView) actionbar.findViewById(R.id.menu_settings_drawer);
		navigationDrawer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				slidingMenu.showMenu(true);
			}
		});
		
		getSupportActionBar().setCustomView(actionbar);
	    getSupportActionBar().setDisplayShowTitleEnabled(false);
	    getSupportActionBar().setDisplayShowCustomEnabled(true);
	   	getSupportActionBar().setDisplayShowHomeEnabled(false);
		
	   	RelativeLayout arrangeMeeting = (RelativeLayout) findViewById(R.id.arrange_meeting);
		arrangeMeeting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Start CreateGameFirstActivity
				String dotchiType = "0";
				Intent intent = new Intent(NewMainActivity.this, CreateGameFirstActivity.class);
				intent.putExtra("dotchiType", dotchiType);
				startActivity(intent);
			}
		});
	   	
	   	// Set footer
	   	for (int i = 0; i < bottomIds.length; i++)	{
	   		View v = findViewById(bottomIds[i]);
	   		v.setOnClickListener(new BottomFeedOnClickListener());
	   	}
	   	// Set bottom view
	   	View bottomSelectedView = findViewById(bottomIds[selectedBottomIndex]);
	   	bottomSelectedView.setSelected(true);
	   	bottomSelectedView.setOnClickListener(new BottomFeedOnClickListener());
	   	try {
	   		switchFragment(NewFeedFragment.class, R.id.fragment_container,null);
	   	} catch (OutOfMemoryError e) {
	   		e.printStackTrace();
	   	} catch (Exception e) {
	   		e.printStackTrace();
	   	}
	   	
	}//end of onCreate()
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}

	/** 
	 * @see William add google analytics to tracker user error/exception edit in 2014-07-21 
	 * <br> and see alse:
	 * <br> libs/libGoogleAnalyticsServices.jar  
	 * <br> value/analytics.xml  
	 * */
	@Override
	  public void onStart() {
	    super.onStart();

	    // The rest of your onStart() code.
	    // ..
	    // ..
	    //You can enable this code when you ready online
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
		}// end of onStart
	/** 
	 * @see William add google analytics to tracker user error/exception edit in 2014-07-21 
	 * <br> and see alse:
	 * <br> libs/libGoogleAnalyticsServices.jar  
	 * <br> value/analytics.xml 
	 * */
	@Override
	public void onStop() {		

	    super.onStop();
	    // The rest of your onStop() code.
	    // ..
	    // ..
	    //You can enable this code when you ready online
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }// end of onStop

	protected View makeMenuLayout(){
		View view = getLayoutInflater().inflate(android.R.layout.list_content, null);
		final String[] menuSelection = {"Logout"};
		ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, menuSelection);
		ListView l = (ListView)view.findViewById(android.R.id.list);
		l.setAdapter(menuAdapter);
		l.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				setLogoutButton();
				
			}
		});
		return view;
	}
	protected void setLogoutButton()	{
		Session session = Session.openActiveSession(NewMainActivity.this, false, null);
		if (session == null)	{
			Log.d(TAG, "Making active session");
			session = new Session(getApplicationContext());
			Session.setActiveSession(session);
		}
		if (session.isOpened())	{
			Log.d(TAG, "Logging out...");
			session.closeAndClearTokenInformation();
			// Finish Activity, return to main
			SharedPreferences preferences = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
			Editor editor = preferences.edit();
			editor.putString(LoginActivity.IS_LOGGED_IN_KEY, "0");
			
			editor.commit();
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
	}
	
	/**
	 * @author William
	 * @param mFragmentClass 
	 * @param continer (res R.id)
	 * @param bundle
	 * @throws Out of memory error
	 * @throws Exception 
	 */
	private static void switchFragment(Class<?> mFragmentClass,int container, Bundle bundle)throws OutOfMemoryError,Exception  {
		Log.d(TAG,"+++ Switch Fragment Page +++");
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		try {
			Fragment fragment = (Fragment) mFragmentClass.newInstance();
			if (null != bundle) {
				fragment.setArguments(bundle);
			}
			ft.replace(container, fragment);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			//ft.addToBackStack(null);
			ft.commit();
		}catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG,"Something wrong with fragment ...");
			throw e;
		}catch (OutOfMemoryError error) {
			Log.e(TAG,"Out of memory error ...");
			throw error;
        }
		Log.d(TAG,"--- Switch Fragment Page ---");
	}//end of switchFragmentPage

	
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent data) {
		if (requestCode == CommentActivity.COMMENT_ACTIVITY_REQ_CODE)	{
			super.onActivityResult(requestCode, responseCode, data);
		} else if (requestCode == CREATE_GROUP_REQ_CODE)	{
			// Add group; either pull again, or just notify data set changed
			super.onActivityResult(requestCode, responseCode, data);
		} else
			super.onActivityResult(requestCode, responseCode, data);
	}

	class BottomFeedOnClickListener implements OnClickListener	{

		@Override
		public void onClick(View v) {
			// Find which index was selected
			for (int i = 0; i < bottomIds.length; i++)	{
				if (v.getId() == bottomIds[i])	{
					selectedBottomIndex = i;
					break;
				}
			}
			for (int i = 0; i < bottomIds.length; i++)	{
				View view = findViewById(bottomIds[i]);
				view.setSelected(false);
			}
			v.setSelected(true);
			// TODO: Switch fragment
			try {
				switchFragment(fragmentClasses[selectedBottomIndex], R.id.fragment_container, null);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
}
