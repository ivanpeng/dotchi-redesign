package com.dotchi1;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.devsmart.android.ui.HorizontalListView;
import com.dotchi1.backend.MainFeedAdapter;
import com.dotchi1.backend.ViewPagerAdapter;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.VoteItem;
import com.dotchi1.model.VoteItem.MedalType;

public class EventChoicesActivity extends FragmentActivity {

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_choices);
		
		ActionBar actionBar = getActionBar();
		
		viewPager = (ViewPager) findViewById(R.id.pager);
		
		// Set Views
		voteItems = (ArrayList<VoteItem>) getIntent().getSerializableExtra("vote_items");
		if (voteItems == null)	{
			// do something here, maybe throw an error
		} else	{
			// Split into date items and event items
			splitVoteItems();
			viewPager.setAdapter(new EventTypePagerAdapter(this));
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
		private ArrayList<VoteItem>objects;
		private LiteImageLoader imageLoader;
		
		public DetailChoiceAdapter(Context context, int textViewResourceId,
				List<VoteItem> objects) {
			super(context, textViewResourceId, objects);
			this.context = context;
			this.objects = new ArrayList<VoteItem>(objects);
			this.imageLoader = new LiteImageLoader(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			VoteItem item = objects.get(position);
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
			HorizontalListView friends = (HorizontalListView) view.findViewById(R.id.choice_detail_friends_list);
			//friends.setAdapter(new ArrayAdapter );
			ProgressBar progress = (ProgressBar) view.findViewById(R.id.choice_detail_progress);
			progress.setProgress((int)item.getPercent());
			TextView progressText = (TextView) view.findViewById(R.id.choice_detail_progress_text);
			progressText.setText(String.valueOf(item.getVotes()));
			
			return view;
		}
		
	}
	
	class FriendAdapter extends ArrayAdapter<VoteItem>	{

		public FriendAdapter(Context context, int textViewResourceId,
				List<VoteItem> objects) {
			super(context, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
		}
	}

}
