package com.dotchi1;


import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.backend.ViewUtils;
import com.facebook.Session;

public class MainActivity extends ActionBarActivity {

	public static final String TAG = "MainActivity";	
	public static final String DOTCHI_ID_KEY = "DOTCHI_ID";
	public static final String MAIN_ACTIVITY_FIRST_KEY = "MAIN_ACTIVITY_FIRST";
	
	public static final int FRAGMENT_MYDOTCHI_FEED = 0;
	public static final int FRAGMENT_MYDOTCHI_PENDING = 1;
	public static final int FRAGMENT_MYDOTCHI_EVENT = 2;
	public static final int FRAGMENT_SOCIAL = 3;
	public static final int FRAGMENT_INVITE = 4;
	private int stateToSet = 0;
	String dotchiId;
	
	private Fragment[] fragments = new Fragment[FRAGMENT_INVITE+1];
	// Have 2 ids as 0 to account for the fragments; In this situation, we won't need to worry about ArrayIndexOutOfBounds
	private final int[] arrowIdList = new int[]{R.id.feeds_pointer,R.id.pending_pointer, R.id.events_pointer, 0, 0};
	private final int[] buttonIdList = new int[] {R.id.feeds_tab, R.id.pending_image_tab, R.id.events_tab, R.id.social_button, R.id.invite_button};
	private final int [] fragmentUpButtons = new int[] {R.drawable.feeds_image, R.drawable.pending_image, R.drawable.events_image, R.drawable.social_image, R.drawable.invite_image};	
	
	private int currentFragmentId;
	
	private ProfileBox profileBox;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ViewUtils.initConfiguration(getApplicationContext());
		// Populate profile box
		profileBox = (ProfileBox) findViewById(R.id.profilebox);
		View actionbar = LayoutInflater.from(this).inflate(R.layout.main_activity_menu, null);
		ImageView navigationDrawer = (ImageView) actionbar.findViewById(R.id.navigation_button);
		navigationDrawer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("Logout")
				.setMessage("Are you sure you want to logout?")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// dismiss dialog and close Session activity
						Session session = Session.openActiveSession(MainActivity.this, false, null);
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
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.create();
				dialog.show();
				
			}
		});
		Button helpButton = (Button) actionbar.findViewById(R.id.main_menu_help);
		helpButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				showMainActivityHelpDialog();
			}
		});
		getSupportActionBar().setCustomView(actionbar);
	    getSupportActionBar().setDisplayShowTitleEnabled(false);
	    getSupportActionBar().setDisplayShowCustomEnabled(true);
	   	getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.fragment_container, MyDotchiFragment.newInstance(FRAGMENT_MYDOTCHI_FEED)).commit();
			addSubtabs(FRAGMENT_MYDOTCHI_FEED);
			ImageView myDotchiView = (ImageView) findViewById(R.id.mydotchi_button);
			myDotchiView.setImageResource(R.drawable.mydotchi_image_down);
			myDotchiView.setBackgroundResource(R.drawable.down_pressed_background);
			ImageView myDotchiFeedView = (ImageView)findViewById(R.id.feeds_tab);
			myDotchiFeedView.setBackgroundResource(R.drawable.tab_pressed_background);
			ImageView arrowView = (ImageView) findViewById(R.id.feeds_pointer);
			arrowView.setVisibility(View.VISIBLE);
			currentFragmentId = FRAGMENT_MYDOTCHI_FEED;
		}
		TableRow tableRow = (TableRow) profileBox.findViewById(R.id.table_row_subtab_fragments);
		
		
		// There should be nothing for that; but we post the user info task
		SharedPreferences preferences = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		dotchiId = preferences.getString(DOTCHI_ID_KEY,"0");
		//TODO: check if dotchiId is here! if not, then we have to wait. 
		String url = getResources().getString(R.string.api_root_url) + "/users/user_info";
		new PostUrlTask()	{

			@Override
			protected void onPostExecute(String result) {
				Log.d(TAG, result);
				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(result.substring(2));
					JSONObject dataObj = jsonObject.getJSONObject("data");
					String name = dataObj.getString("user_name");
					//URI uri = new URI(dataObj.getString("head_image"));
					profileBox.setBox(dataObj.getString("head_image"), name);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		}.execute(url, "dotchi_id", dotchiId);
		
		// Show help dialog if first time;
		boolean isFirstTime = preferences.getString(MAIN_ACTIVITY_FIRST_KEY, "0").equals("0") ? true: false;
		if (isFirstTime)	{
			showMainActivityHelpDialog();
			Editor editor = preferences.edit();
			editor.putString(MAIN_ACTIVITY_FIRST_KEY, "1");
			editor.commit();
		}
	}
	
	public void showMainActivityHelpDialog()	{
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = inflater.inflate(R.layout.dialog_help_main_activity, null);
		TextView title = (TextView) dialogView.findViewById(R.id.dialog_help_title);
		title.setText("我的活動頁");
		final AlertDialog dialog = new AlertDialog.Builder(this)
								.setView(dialogView).create();
		ImageView cancel = (ImageView)dialogView.findViewById(R.id.dialog_help_close_button);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	private void showFragment(int fragmentIndex, boolean addToBackStack) {
	    FragmentManager fm = getFragmentManager();
	    FragmentTransaction transaction = fm.beginTransaction();
	    for (int i = 0; i < fragments.length; i++) {
	        if (i == fragmentIndex) {
	            transaction.show(fragments[i]);
	        } else {
	            transaction.hide(fragments[i]);
	        }
	    }
	    if (addToBackStack) {
	        transaction.addToBackStack(null);
	    }
	    transaction.commit();
	}

	public void switchFragment(View v)	{
		switchFragment(Integer.parseInt(v.getTag().toString()));
	}
	
	public void switchFragment(int key)	{
		//TODO: we have the view; switch the image source with the downpressed button
		Log.d(TAG, "onClick clicked with key " + key);
		// Check if key is same as current fragment; if so, then just do nothing.
		if (key == currentFragmentId)
			return;
		setImageButtonUp();
		addSubtabs(key);
		ImageView myDotchiView = (ImageView) findViewById(R.id.mydotchi_button);
		ImageView pressedView = (ImageView) findViewById(buttonIdList[key]);
		ImageView arrowView = (ImageView) findViewById(arrowIdList[key]);
		FragmentTransaction transactionManager = getFragmentManager().beginTransaction();
		switch (key)	{
			case FRAGMENT_MYDOTCHI_FEED:
				Log.d(TAG, "Switching to MyDotchi Fragment, Feed");
				transactionManager.replace(R.id.fragment_container, MyDotchiFragment.newInstance(FRAGMENT_MYDOTCHI_FEED));
				// Before switching to this, make sure MyDotchi is down, and tab is down
				myDotchiView.setImageResource(R.drawable.mydotchi_image_down);
				myDotchiView.setBackgroundResource(R.drawable.down_pressed_background);
				pressedView.setBackgroundResource(R.drawable.tab_pressed_background);
				arrowView.setVisibility(View.VISIBLE);
				currentFragmentId = FRAGMENT_MYDOTCHI_FEED;
				break;
			case FRAGMENT_MYDOTCHI_PENDING:
				Log.d(TAG, "Switching to MyDotchi Fragment, Pending");
				transactionManager.replace(R.id.fragment_container, MyDotchiFragment.newInstance(FRAGMENT_MYDOTCHI_PENDING));
				myDotchiView.setImageResource(R.drawable.mydotchi_image_down);
				myDotchiView.setBackgroundResource(R.drawable.down_pressed_background);
				pressedView.setBackgroundResource(R.drawable.tab_pressed_background);
				arrowView.setVisibility(View.VISIBLE);
				currentFragmentId = FRAGMENT_MYDOTCHI_PENDING;
				//TODO: Start an activity instead of changing fragments.
				break;
			case FRAGMENT_MYDOTCHI_EVENT:
				Log.d(TAG, "Switching to MyDotchi Fragment, Event");
				transactionManager.replace(R.id.fragment_container, MyDotchiFragment.newInstance(FRAGMENT_MYDOTCHI_EVENT));
				myDotchiView.setImageResource(R.drawable.mydotchi_image_down);
				myDotchiView.setBackgroundResource(R.drawable.down_pressed_background);
				pressedView.setBackgroundResource(R.drawable.tab_pressed_background);
				arrowView.setVisibility(View.VISIBLE);
				currentFragmentId = FRAGMENT_MYDOTCHI_EVENT;
				break;
			case FRAGMENT_SOCIAL:
				Log.d(TAG, "Switching to Social Fragment");
				Intent socialIntent = new Intent(MainActivity.this, SocialFeedActivity.class);
				socialIntent.putExtra("dotchi_id", Integer.parseInt(dotchiId));
				startActivityForResult(socialIntent, 100);
//				transactionManager.replace(R.id.fragment_container, new SocialFragment());
//				myDotchiView.setImageResource(R.drawable.mydotchi_image);
//				myDotchiView.setBackgroundColor(Color.TRANSPARENT);
//				pressedView.setImageResource(R.drawable.social_image_down);
//				pressedView.setBackgroundResource(R.drawable.down_pressed_background);
				currentFragmentId = FRAGMENT_SOCIAL;
				break;
			case FRAGMENT_INVITE:
				Log.d(TAG, "Switching to Invite Fragment");
				transactionManager.replace(R.id.fragment_container, new InviteFragment());
				myDotchiView.setImageResource(R.drawable.mydotchi_image);
				myDotchiView.setBackgroundColor(Color.TRANSPARENT);
				pressedView.setImageResource(R.drawable.invite_image_down);
				pressedView.setBackgroundResource(R.drawable.down_pressed_background);
				currentFragmentId = FRAGMENT_INVITE;
				break;
		}
		transactionManager.commit();
		
	}
	
	@Override
	protected void onResume()	{
		super.onResume();
		if (stateToSet == 1)	{
			switchFragment(FRAGMENT_MYDOTCHI_FEED);
			stateToSet = 0;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 100)	{
			// Doesn't matter; we had 100 executed; just switch fragment back to Main Feed
			stateToSet = 1;
		}
	}

	protected void addSubtabs(int currentFragmentId) {
		TableRow row = (TableRow) profileBox.findViewById(R.id.table_row_subtab_fragments);
		row.removeAllViews();
		Log.d(TAG, row == null? "Row is null": "Row is not null");
		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (currentFragmentId == FRAGMENT_MYDOTCHI_FEED || currentFragmentId == FRAGMENT_MYDOTCHI_PENDING || currentFragmentId == FRAGMENT_MYDOTCHI_EVENT)	{
			View v = inflater.inflate(R.layout.feeds_subtab, row, false);
			// add yellow subtab
			row.addView(v);
			ImageView feedTab = (ImageView) row.findViewById(R.id.feeds_tab);
			ImageView pendingTab = (ImageView) row.findViewById(R.id.pending_image_tab);
			ImageView eventsTab = (ImageView) row.findViewById(R.id.events_tab);
			// Set onClickListener, and wire fragments up to it.
			feedTab.setOnClickListener(new TopTabOnClickListener());
			pendingTab.setOnClickListener(new TopTabOnClickListener());
			eventsTab.setOnClickListener(new TopTabOnClickListener());
		} else	{
			View v = inflater.inflate(R.layout.invite_subtab, row, false);
			ImageView quickCreateView = (ImageView) v.findViewById(R.id.dotchi_quick_setup_tab);
			quickCreateView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getApplicationContext(), GameSettingActivity.class);
					intent.putExtra("category_id", -1);
					startActivity(intent);
				}
			});
			row.addView(v);
		}
	}
	private void setImageButtonUp()	{
		for (int i = 0; i < 3; i++)	{
			View v = findViewById(arrowIdList[i]);
			if (v != null)
				v.setVisibility(View.INVISIBLE);
		}
		ImageView currentDownView = (ImageView) findViewById(buttonIdList[currentFragmentId]);
		if (currentFragmentId == FRAGMENT_SOCIAL || currentFragmentId == FRAGMENT_INVITE)	{
			currentDownView.setImageResource(fragmentUpButtons[currentFragmentId]);
			currentDownView.setBackgroundColor(Color.TRANSPARENT);
		} else	{
			currentDownView.setBackgroundColor(Color.TRANSPARENT);
		}
	}
	
	class TopTabOnClickListener implements View.OnClickListener	{

		@Override
		public void onClick(View v) {
			Integer key = Integer.parseInt(v.getTag().toString());
			Log.d(TAG, "Top tab has been clicked. The key is " + key);
			if (key != FRAGMENT_MYDOTCHI_FEED && key != FRAGMENT_MYDOTCHI_PENDING && key != FRAGMENT_MYDOTCHI_EVENT)
				throw new RuntimeException("TopTabListener has been implemented at the wrong location");
			switchFragment(key);
		}
		
	}



}
