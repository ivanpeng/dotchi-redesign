package com.dotchi1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dotchi1.backend.ExpandableListAdapter;
import com.dotchi1.backend.MainFeedAdapter;
import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.BaseFeedData;
import com.dotchi1.model.FriendPageFriendItem;
import com.dotchi1.model.FriendPageGroupItem;
import com.dotchi1.model.FriendPageItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.larswerkman.quickreturnlistview.QuickReturnListView;

@SuppressWarnings("unused")
public class NewMainActivity extends ActionBarActivity implements OnRefreshListener{
	
	public static final String TAG = "NewMainActivity";
	public static final String HOME_FEED_JSON_KEY = "HOME_FEED_JSON";
	public static final String HOME_FEED_JSON_VALUE = "HOME_FEED_JSON_VALUE";

	public static final int MAIN_ACTIVITY_REQ_CODE = 100;
	public static final int CREATE_GROUP_REQ_CODE = 10000;
	public static final int FINAL_DECISION_REQ_CODE = 10001;
	private static final int UPDATE_COUNT = 10;
	
	private int screenWidth;
	private String dotchiId;
	//private SwipeRefreshLayout swipeLayout;
	
	private View mHeader;
	private ListView listView;
	private LinearLayout mQuickReturnView;
	private View mPlaceHolder;
	private int mCachedVerticalScrollRange;
	private int mQuickReturnHeight;
	
	private SlidingMenu slidingMenu;
	
	private static final int STATE_ONSCREEN = 0;
	private static final int STATE_OFFSCREEN = 1;
	private static final int STATE_RETURNING = 2;
	private int mState = STATE_ONSCREEN;
	private int mScrollY;
	private int mMinRawY = 0;
	private TranslateAnimation anim;
	
	private boolean isFriendState = false;
	private MainFeedAdapter adapter;
	private ArrayList<BaseFeedData> feedData;
	
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
		
		SharedPreferences preferences = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		dotchiId = preferences.getString("DOTCHI_ID", "35");
		setContentView(R.layout.activity_home_feed);
		setupUI(findViewById(R.id.home_feed_container));
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final String rootUrl = getResources().getString(R.string.api_test_root_url);
		
		Display d = getWindowManager().getDefaultDisplay();
		Point p = new Point();
		d.getSize(p);
		screenWidth = p.x;
	   	slidingMenu = new SlidingMenu(this);
	   	slidingMenu.setMode(SlidingMenu.LEFT);
	   	slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
//	   	slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
//	   	slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	   	slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
	   	slidingMenu.setMenu(R.layout.activity_test);
	   	slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
//	   	slidingMenu.setSecondaryMenu(R.layout.new_friend_list);

//	   	swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
//	   	swipeLayout.setOnRefreshListener(this);
//        swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
//                android.R.color.holo_green_light, 
//                android.R.color.holo_orange_light, 
//                android.R.color.holo_red_light);
	   	
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
			new GetHomeFeedTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rootUrl + "/activity/get_activity_msg", "dotchi_id", dotchiId, "activity_type", "0");
		} else {
			new GetHomeFeedTask().execute(rootUrl + "/activity/get_activity_msg", "dotchi_id", dotchiId, "activity_type", "0");
		}

		View actionbar = inflater.inflate(R.layout.menu_new_feed, null);
		final TextView title = (TextView)actionbar.findViewById(R.id.menu_new_feed_title);
		title.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				isFriendState = !isFriendState;
				String code;
				if (isFriendState)	{
					code = "1";
					title.setText("朋友動態");
				}
				else	{
					code = "0";
					title.setText("個人動態");
				}
				new GetHomeFeedTask().execute(rootUrl +  "/activity/get_activity_msg", "dotchi_id", dotchiId, "activity_type", code);
			}
			
		});
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
		
	   	// Set footer
	   	View homeFeedView = findViewById(R.id.new_home_feed_button);
	   	homeFeedView.setSelected(true);
	   	
		mHeader = inflater.inflate(R.layout.header, null);
		mQuickReturnView = (LinearLayout) findViewById(R.id.header);
		mPlaceHolder = mHeader.findViewById(R.id.placeholder);
		listView = (ListView) findViewById(R.id.feed_list);
		
	    //String rootUrl = getResources().getString(R.string.api_test_root_url);
		//SharedPreferences preferences = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);

		
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
		// We want to delete the JSON saved in sharedPreferences so we can grab it every time on start up, but nothing else.
		SharedPreferences preferences = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(HOME_FEED_JSON_KEY, false);
		editor.remove(HOME_FEED_JSON_VALUE);
		editor.commit();
	    super.onStop();
	    // The rest of your onStop() code.
	    // ..
	    // ..
	    //You can enable this code when you ready online
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }// end of onStop

	
	@Override
	public void onDestroy()	{

		super.onDestroy();
	}
	
	@Override public void onRefresh() {
		/*swipeLayout.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                swipeLayout.setRefreshing(false);
            }
        }, 2000);*/
    }
	
	public ArrayList<BaseFeedData> processJson(String json)	{
		ArrayList<BaseFeedData> objects = new ArrayList<BaseFeedData>();
		try {
			JSONArray ja = new JSONObject(json).getJSONArray("data");
			ObjectMapper mapper = new ObjectMapper();
			mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
			mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
			mapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
			objects = mapper.readValue(ja.toString(), mapper.getTypeFactory().constructCollectionType(ArrayList.class, BaseFeedData.class));
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return objects;
	}
	/**
	 * This function compares the new incoming JSON, and sees if it's any different. If it is, we update the array list, do it in such a manner 
	 * that we don't reset the adapter.
	 * TODO: we just keep adding to new data. we don't remove if the old list has something new list doesn't have. While that's not very important
	 * because we reset this process after every destroy, we should still do it eventually.
	 * @param oldData
	 * @param newData
	 * @return
	 */
	public ArrayList<BaseFeedData> compareResults(ArrayList<BaseFeedData> oldData, ArrayList<BaseFeedData> newData)	{
		List<BaseFeedData> newItems = new ArrayList<BaseFeedData>();
		for (int i = 0; i < newData.size(); i++)	{
			BaseFeedData item = newData.get(i);
			if (oldData.contains(item))	{
				// Contains is based off the equals() function. That's set in BaseFeedData to be very loose.
				// TODO: Update the item with a strong equals, if necessary
				// For now, we can just find the item and call a set
				int oldIndex = oldData.indexOf(item);
				Log.d(TAG, "new index: " + i + ", old index: " + oldIndex);
				oldData.set(oldIndex, item);
			} else {
				// New data is not in old data; NOT other way
				// Instead of adding one by one, we add to a secondary list, and then addAll at beginning. This maintains order.
				newItems.add(item);
				Log.d(TAG,"adding new item.");
			}
			if (newItems.size() > 0)	{
				oldData.addAll(0, newItems);
			}
		}
		return oldData;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent data) {
		if (requestCode == CommentActivity.COMMENT_ACTIVITY_REQ_CODE)	{
			//String moodCount = data.getStringExtra("mood_count");
			// How do we access the adapter to update the data;
			adapter.notifyDataSetChanged();
			super.onActivityResult(requestCode, responseCode, data);
		} else if (requestCode == CREATE_GROUP_REQ_CODE)	{
			// Add group; either pull again, or just notify data set changed
			super.onActivityResult(requestCode, responseCode, data);
		} else
			super.onActivityResult(requestCode, responseCode, data);
	}


	
	class GetHomeFeedTask extends PostUrlTask	{

		@Override
		protected void onPostExecute(String result) {
			result = processResult(result);
			// Check preferences; if not there, save!
			SharedPreferences preferences = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
			boolean isJsonSaved = preferences.getBoolean(HOME_FEED_JSON_KEY, false);
			if (!isJsonSaved)	{
				Editor editor = preferences.edit();
				editor.putBoolean(HOME_FEED_JSON_KEY, true);
				editor.putString(HOME_FEED_JSON_VALUE, result);
				editor.commit();
				// Now call process and set adapter
				feedData = processJson(result);
				if (feedData != null && feedData.size() > 0)	{
					//adapter = new NewFeedAdapter(NewMainActivity.this, R.layout.new_feed_item, feedData, screenWidth);
					adapter = new MainFeedAdapter(NewMainActivity.this, 0, feedData, new LiteImageLoader(getApplicationContext()));
					listView.setAdapter(adapter);
					//Set OnItemClickListener
					listView.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
							RelativeLayout imageLayout = (RelativeLayout) view.findViewById(R.id.image_layout);
							final RelativeLayout onClickLayout = (RelativeLayout) view.findViewById(R.id.on_click_layout);
							final LinearLayout imageDetailsLayout = (LinearLayout) view.findViewById(R.id.image_details_layout);
							if (onClickLayout != null)	{
								view.setSelected(!view.isSelected());
								if (view.isSelected())	{
									// add mask, disappear textView Layout
									onClickLayout.setVisibility(View.VISIBLE);
									imageDetailsLayout.setVisibility(View.GONE);
									Log.d(TAG, "Item View Selected");
									onClickLayout.setOnClickListener(new OnClickListener() {
										
										@Override
										public void onClick(View v) {
											v.setSelected(false);
											onClickLayout.setVisibility(View.GONE);
											imageDetailsLayout.setVisibility(View.VISIBLE);
										}
									});
								} else	{
									onClickLayout.setVisibility(View.GONE);
									imageDetailsLayout.setVisibility(View.VISIBLE);
									Log.d(TAG, "Item View unselected");
								}
							}
						}
					});
				} else	{
					listView.setAdapter(null);
					TextView empty = (TextView) findViewById(R.id.empty_view);
					empty.setVisibility(View.VISIBLE);
				}
				
			} else 	{
				Log.d(TAG, "JSON already saved, we compare and process new results.");
				// Here, we don't reset the adapter. Just compare and see what differences 
				ArrayList<BaseFeedData> newData = processJson(result);
				if (feedData != null){
					feedData = compareResults(feedData, newData);
					adapter.notifyDataSetChanged();
					Log.d(TAG, "List comparison from previous is different. Notifying dataset updated instead of pushing for update");
				}
			}
			
		}
	}
}
