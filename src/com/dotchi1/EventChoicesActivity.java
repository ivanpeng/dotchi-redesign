package com.dotchi1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dotchi1.backend.MainFeedAdapter;
import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.backend.ViewPagerAdapter;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.VoteItem;
import com.dotchi1.model.VoteItem.MedalType;
import com.dotchi1.view.ScrollDisabledHorizontalListView;

public class EventChoicesActivity extends ActionBarActivity {

	public static final String TAG = "EventChoicesActivity";
	
	private static final int EVENT_CHOICES_ACTIVITY_IMAGE_SIZE = 150;
	private static final int EVENT_CHOICES_NONE = 0;
	private static final int EVENT_CHOICES_EVENT = 1;
	private static final int EVENT_CHOICES_DATE = 2;
	private static final int EVENT_CHOICES_BOTH = 3;
	
	private ViewPager viewPager;
	
	//private ListView detailChoiceListView;
	private ArrayList<VoteItem> voteItems;
	private ArrayList<VoteItem> eventItems;
	private ArrayList<VoteItem> dateItems;
	
	private DetailChoiceAdapter eventAdapter;
	private DetailChoiceAdapter dateAdapter;
	private int pagerSize = 0;
	private int pagerState;
	
	// Hashmap to determine the mapping for vote users;
	private HashMap<Integer, ArrayList<String> > headImageMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_choices);
		String rootUrl = getResources().getString(R.string.api_test_root_url);
		ActionBar actionBar = getActionBar();
		getActionBar().setDisplayUseLogoEnabled(false);
		viewPager = (ViewPager) findViewById(R.id.pager);
		
		// Set Views
		voteItems = (ArrayList<VoteItem>) getIntent().getSerializableExtra("vote_items");
		if (voteItems == null)	{
			// do something here, maybe throw an error
			Log.e(TAG, "No vote items; that's a problem!");
		} else	{
			// Split into date items and event items
			splitVoteItems();
			viewPager.setAdapter(new EventTypePagerAdapter(this));
		    headImageMap = new HashMap<Integer, ArrayList<String>>();
		    // Execute data pull for get_vote_users
		    for (VoteItem vi : voteItems)	{	    	
		    	new GetVoteUsers(vi).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rootUrl + "/game/get_vote_users", "game_item_id", String.valueOf(vi.getGameItemId()));
		    }
		}
		
		// Create a tab listener that is called when the user changes tabs.
	    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabReselected(Tab arg0,
					android.app.FragmentTransaction arg1) {
			}

			@Override
			public void onTabSelected(Tab tab,
					android.app.FragmentTransaction ft) {
				viewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(Tab arg0,
					android.app.FragmentTransaction arg1) {
			}

	    };

	    // Add new tabs only if pagerState is set to both;
	    if (pagerState == EVENT_CHOICES_BOTH)	{
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			actionBar.addTab(actionBar.newTab().setText("Event").setTabListener(tabListener));
			actionBar.addTab(actionBar.newTab().setText("Date").setTabListener(tabListener));
			
			viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    // When swiping between pages, select the
                    // corresponding tab.
                    getActionBar().setSelectedNavigationItem(position);
                }
            });
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)	{
		getMenuInflater().inflate(R.menu.event_choices, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())	{
		case R.id.action_play_game:
			//TODO:
			Toast.makeText(this, "Open game", Toast.LENGTH_SHORT).show();
			// Open game activity
			//Intent intent = new Intent(this, GameActivity.class);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void splitVoteItems()	{
		eventItems = new ArrayList<VoteItem>();
		dateItems = new ArrayList<VoteItem>();
		
		for (VoteItem item: voteItems)	{
			if (item.getIsDate())
				dateItems.add(item);
			else
				eventItems.add(item);
		}
		if (dateItems == null && eventItems == null)	{
			pagerSize = 0;
			pagerState = EVENT_CHOICES_NONE;
		} else if (dateItems.size() > 0 && eventItems.size() == 0)	{
			pagerSize = 1;
			pagerState = EVENT_CHOICES_DATE;
		} else if (dateItems.size() == 0 && eventItems.size() > 0)	{
			pagerSize =1 ;
			pagerState = EVENT_CHOICES_EVENT;
		} else	{
			pagerSize = 2;
			pagerState = EVENT_CHOICES_BOTH;
		}
		Log.d(TAG, "Pager state set to " + pagerState);
		dateAdapter = new DetailChoiceAdapter(this, 0, dateItems);
		eventAdapter = new DetailChoiceAdapter(this, 0, eventItems);
	}
	
	class EventTypePagerAdapter extends ViewPagerAdapter	{

		private Context context;
		
		public EventTypePagerAdapter(Context context)	{
			this.context = context;
		}
		
		@Override
		public View getView(int position, ViewPager pager) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.list_container, null);
			ListView list = (ListView)view.findViewById(R.id.list);
			// Now set adapter
			if (position == 0)
				list.setAdapter(eventAdapter);
			else
				list.setAdapter(dateAdapter);
			return view;
		}

		@Override
		public int getCount() {
			return pagerSize;
		}
		
	}

	class DetailChoiceAdapter extends ArrayAdapter<VoteItem> 	{

		private Context context;
		private LiteImageLoader imageLoader;
		
		public DetailChoiceAdapter(Context context, int textViewResourceId,
				List<VoteItem> objects) {
			super(context, textViewResourceId, objects);
			this.context = context;
			this.imageLoader = new LiteImageLoader(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			VoteItem item = getItem(position);
			Log.d("EventChoicesActivity", item.toString());
			View view = convertView;
			if (view == null)	{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.choice_detail_item, null);
			}
			ImageView votePic = (ImageView) view.findViewById(R.id.choice_detail_image);
			if (!item.getIsDate())	{
				votePic.setVisibility(View.VISIBLE);
				imageLoader.DisplayImage(item.getItemImage(), R.drawable.new_feed_photo_default, votePic, EVENT_CHOICES_ACTIVITY_IMAGE_SIZE);
			} else	{
				votePic.setVisibility(View.INVISIBLE);
				View dateView = MainFeedAdapter.makeEventView(context, item);
				LinearLayout dateLayout = (LinearLayout)view.findViewById(R.id.date_layout);
				dateLayout.addView(dateView);
			}
			TextView title = (TextView) view.findViewById(R.id.choice_detail_title);
			title.setText(item.getItemTitle());
			TextView desc = (TextView) view.findViewById(R.id.choice_detail_description);
			desc.setText(item.getItemContent());
			// Medal
			ImageView medal = (ImageView) view.findViewById(R.id.choice_detail_medal);
			if (item.getMedals() == MedalType.NONE)	
				medal.setVisibility(View.GONE);
			else if (item.getMedals() == MedalType.GOLD)
				medal.setImageResource(R.drawable.photo_roll_gold);
			else if (item.getMedals() == MedalType.SILVER)
				medal.setImageResource(R.drawable.photo_roll_silver);
			else if (item.getMedals() == MedalType.COPPER)
				medal.setImageResource(R.drawable.photo_roll_bronze);
			
			// TODO: friends
			ScrollDisabledHorizontalListView friends = (ScrollDisabledHorizontalListView) view.findViewById(R.id.choice_detail_friends_list);
			friends.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_MOVE)
						return true;
					return false;
				}
			});
			if (headImageMap.get(item.getGameItemId()) != null)
				friends.setAdapter(new FriendAdapter(context, 0, headImageMap.get(item.getGameItemId()), imageLoader));
			ProgressBar progress = (ProgressBar) view.findViewById(R.id.choice_detail_progress);
			progress.setProgress((int)item.getPercent());
			TextView progressText = (TextView) view.findViewById(R.id.choice_detail_progress_text);
			progressText.setText(String.valueOf(item.getVotes()));
			
			return view;
		}
		
	}
	
	class FriendAdapter extends ArrayAdapter<String>	{

		Context context;
		LiteImageLoader imageLoader;
		
		public FriendAdapter(Context context, int textViewResourceId,
				List<String> objects, LiteImageLoader imageLoader) {
			super(context, textViewResourceId, objects);
			this.context = context;
			if (imageLoader != null)
				this.imageLoader = imageLoader;
			else
				this.imageLoader = new LiteImageLoader(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String url = getItem(position);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.rounded_head, null);
			ImageView head = (ImageView) view.findViewById(R.id.rounded_image);
			imageLoader.DisplayImage(url, head);
			return view;
		}
		
		
	}
	
	class GetVoteUsers extends PostUrlTask	{
		private VoteItem voteItem;
		
		public GetVoteUsers(VoteItem voteItem)	{
			this.voteItem = voteItem;
		}

		@Override
		protected void onPostExecute(String result) {
			result = processResult(result);
			// Add the integer into the hashmap;
			ArrayList<String> list = new ArrayList<String>();
			try {
				JSONArray ja = new JSONObject(result).getJSONArray("data");
				for (int i = 0; i < ja.length(); i++)	{
					JSONObject jo = ja.getJSONObject(i);
					list.add(jo.getString("head_image"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (list.size() > 0)
				headImageMap.put(voteItem.getGameItemId(), list);
			// Now that we have the data saved, we also put into the respective voteItem
			voteItem.setVoters(list);
			// For now, update both adapters; need to check for null. In future, send parameter that identifies which adapter it's from; date or event
			if (dateAdapter != null)	{
				dateAdapter.notifyDataSetChanged();
			}
			if (eventAdapter != null)	{
				eventAdapter.notifyDataSetChanged();
			}
		}
		
	}

}
