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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.model.FriendPageFriendItem;
import com.dotchi1.model.FriendPageGroupItem;
import com.dotchi1.model.FriendPageItem;
import com.dotchi1.model.FriendPageSectionItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FriendSelectActivity extends ActionBarActivity {

	public static final String TAG = "FriendSelectActivity";
	static final int GROUP_KEY = 0;
	static final int FRIEND_KEY = 1;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	//private ExpandableListView expandableListView;
	private ListView listView;
	private ArrayList<FriendPageItem> list;
	private BasicFriendPageAdapter adapter;
	private ArrayList<FriendPageSectionItem> sections;
	private HashMap<Integer, List<? extends FriendPageItem>> childData;
	private ArrayList<FriendPageGroupItem> groups;
	private ArrayList<FriendPageFriendItem> friends;
	// We have current state so that we can pull from this later; when we send the intent, we can keep track of the data
	private boolean currentState[];
	boolean[] isFinished = new boolean[] {false, false};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		SharedPreferences preferences = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		String dotchiId = preferences.getString("DOTCHI_ID", "");
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
					.cacheInMemory(true)
					.cacheOnDisc(true)
					.build();
		
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_friend_select);
		View actionbar = LayoutInflater.from(this).inflate(R.layout.friend_select_menu, null);
		getSupportActionBar().setCustomView(actionbar);
	    getSupportActionBar().setDisplayShowHomeEnabled(false);
	    getSupportActionBar().setDisplayShowTitleEnabled(false);
	    getSupportActionBar().setDisplayShowCustomEnabled(true);
	    
	    // Set actionbar now
	    ImageView back = (ImageView) actionbar.findViewById(R.id.back_button);
	    back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED, returnIntent);
				finish();
			}
		});
	    ImageView next = (ImageView) actionbar.findViewById(R.id.forward_button);
	    next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Finish this activity with the friends selected
				// calculate the friends which you have selected, and get their dotchiIds
				int[] selectedFriendIds = getSelectedFriends();
				if (selectedFriendIds != null &&selectedFriendIds.length > 0)	{					
					Intent returnIntent = new Intent();
					returnIntent.putExtra("dotchi_ids", selectedFriendIds);
					returnIntent.putExtra("friend_items", friends);
					setResult(RESULT_OK, returnIntent);
				}
				finish();
			}
		});
	    
	    listView = (ListView) findViewById(R.id.main_friends_list);
		//expandableListView = (ExpandableListView) findViewById(R.id.main_friends_list);

		LinearLayout searchContainerLayout = (LinearLayout) findViewById(R.id.search_container);
		View v = LayoutInflater.from(this).inflate(R.layout.search_bar_layout, null);
		searchContainerLayout.addView(v);
		// Populate data here.
		new BaseGetFriendsUrlTask(FriendPageFriendItem.class, FRIEND_KEY).execute(getResources().getString(R.string.api_root_url) + "/users/get_user_friends", "dotchi_id", dotchiId);
		new BaseGetFriendsUrlTask(FriendPageGroupItem.class, GROUP_KEY).execute(getResources().getString(R.string.api_root_url) + "/users/get_user_groups", "dotchi_id", dotchiId);
		new PopulateViewTask().execute();
		isFinished[GROUP_KEY] = true;
		
		// Set search bar here
		final EditText searchbar = (EditText) findViewById(R.id.search_friends_text);
		searchbar.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
				Log.d(TAG, "Text changed: " + s.toString());
				adapter.getFilter().filter(s.toString());
			}
			@Override
			public void beforeTextChanged(CharSequence cs, int start,
					int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence cs, int start, int before,
					int count) {
			}
		});
		ImageView searchCancelButton = (ImageView) findViewById(R.id.search_bar_cancel_button);
		searchCancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// clear editText searchBar
				searchbar.setText("");
				
			}
		});
		
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
	    //EasyTracker.getInstance(this).activityStart(this);  // Add this method.
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
	    //EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }// end of onStop
	// This only works on the full list; that's all we need for now.
	protected void setIfSelected(ListView listView, ArrayList<FriendPageFriendItem> data)	{
		boolean [] localCurrentState = new boolean[friends.size()];
		for (int i = 0; i < localCurrentState.length; i++)
			localCurrentState[i] = false;
		if (data.size() != friends.size())	{
			// Rearrange to full list size
			for (int i = 0; i < data.size(); i++)	{
				FriendPageFriendItem item = data.get(i);
				int index = friends.indexOf(item);
				localCurrentState[index] = true;
			}
			
		} else	{
			localCurrentState = currentState;
		}
		for (int i = 0; i < listView.getCount(); i++)	{
			View child = listView.getChildAt(i);
			if (localCurrentState[i])
				// current state at this location is true; set the child view to be selected
				child.setSelected(true);
			else
				child.setSelected(false);
		}

		
	}
	
	protected int[] getSelectedFriends()	{
		List<Integer> indexes = new ArrayList<Integer>();
		for (int i = 0; i < currentState.length; i++)	{
			if (currentState[i])
				indexes.add(i);
		}
		// Convert to int[]
		Log.d(TAG, "Items checked: " + indexes.toString());
		
		int[] indexConverted = new int[indexes.size()];
		for (int i = 0; i < indexes.size(); i++)	{
			indexConverted[i] = (int) friends.get(indexes.get(i)).getDotchiId();
			Log.d(TAG, indexConverted[i] + " dotchi id added to list.");
		}
		return indexConverted;
	}

	class BasicFriendPageAdapter extends ArrayAdapter<FriendPageFriendItem>	{
	
		private Context context;
		private ArrayList<FriendPageFriendItem> friends;
		
		public BasicFriendPageAdapter(Context context, int resId, List<FriendPageFriendItem> friends)	{
			super(context, resId, friends);
			this.context = context;
			this.friends = new ArrayList<FriendPageFriendItem>(friends);
		}

		@Override
		public Filter getFilter() {
			return new FriendsOnlyFilter();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FriendPageFriendItem item = friends.get(position);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.list_friend_item, null);
			ImageView headImage = (ImageView) view.findViewById(R.id.friend_head_image);
			imageLoader.displayImage(item.getHeadImage(), headImage, options);
			TextView userName = (TextView) view.findViewById(R.id.item_name);
			userName.setText(item.getUserName());
			return view;
		}
		
	}
	
	class FriendPageAdapter extends BaseExpandableListAdapter	{

		private Context context;
		private List<FriendPageSectionItem> sections;
		private HashMap<Integer, List<? extends FriendPageItem>> childDataMap;
		
		public FriendPageAdapter(Context context, List<FriendPageSectionItem> sections, HashMap<Integer, List<? extends FriendPageItem>> childDataMap) {
			this.context = context;
			this.sections = sections;
			this.childDataMap = childDataMap;
		}


		public Filter getFilter() {
			return new FriendPageFilter();
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			List<? extends FriendPageItem> childList = childDataMap.get(groupPosition);
			if (childList == null)
				return null;
			return childDataMap.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = convertView;
			FriendPageItem i = (FriendPageItem) getChild(groupPosition, childPosition);
			if (i == null)
				Log.d(TAG, "Friend page item is null");
			view = inflater.inflate(R.layout.list_friend_item, null);
			if (i instanceof FriendPageGroupItem)	{
				// Group
				FriendPageGroupItem gi = (FriendPageGroupItem) i;
				ImageView headImage = (ImageView) view.findViewById(R.id.friend_head_image);
				imageLoader.displayImage(gi.getHeadImage(), headImage, options);
				StringBuilder textToDisplay = new StringBuilder();
				// Build the string to display in the username place. It's a combination of group name followed by group count
				textToDisplay.append(gi.getGroupName() + " [" + gi.getCount() +"]");
				TextView groupText = (TextView) view.findViewById(R.id.item_name);
				groupText.setText(textToDisplay.toString());
				// Set onclickListener for group item
			} else	{
				// Friend layout
				FriendPageFriendItem fi = (FriendPageFriendItem) i;
				ImageView headImage = (ImageView) view.findViewById(R.id.friend_head_image);
				imageLoader.displayImage(fi.getHeadImage(), headImage, options);
				TextView userName = (TextView) view.findViewById(R.id.item_name);
				userName.setText(fi.getUserName());
				//view.setOnClickListener(new ToggleClickListener(0));
			}
			return view;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			List<? extends FriendPageItem> childList = childDataMap.get(groupPosition);
			if (childList == null)
				return 0;
			else
				return childList.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return sections.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return sections.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
				ViewGroup parent) {
			FriendPageSectionItem si = (FriendPageSectionItem) getGroup(groupPosition);
			if (si == null)
				return convertView;
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = convertView;
			view = inflater.inflate(R.layout.list_friend_section, null);
			
			// set section part now; image resid and count
			ImageView sectionImage = (ImageView) view.findViewById(R.id.list_header);
			sectionImage.setImageResource(si.getImageResId());
			TextView groupName = (TextView) view.findViewById(R.id.list_text);
			groupName.setText(si.getSectionName() + " ("+ String.valueOf(si.getCount()) + ")");
			return view;
			
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			if (groupPosition == 1)
				return true;
			else
				return false;
		}
		
	}
	
	protected ArrayList<FriendPageItem> populateAdapterList(List<FriendPageGroupItem> groups, List<FriendPageFriendItem> friends)	{
		ArrayList<FriendPageItem> objects = new ArrayList<FriendPageItem>();
		objects = new ArrayList<FriendPageItem>();
		if (groups != null && groups.size() != 0)	{
			objects.add(new FriendPageSectionItem(GROUP_KEY, "群組", R.drawable.groups_header, groups.size()));
			objects.addAll(groups);
		}
		if (friends != null && friends.size() != 0)	{
			objects.add(new FriendPageSectionItem(FRIEND_KEY, "好友", R.drawable.friends_header, friends.size()));
			objects.addAll(friends);
		}
		return objects;
	}
	
	class BaseGetFriendsUrlTask extends PostUrlTask	{

		private Class className;
		private int type;
		
		public BaseGetFriendsUrlTask(Class className, int type)	{
			this.className = className;
			this.type = type;
		}
		
		@Override
		protected void onPostExecute(String result) {
			result = processResult(result);
			JSONObject jsonObj;
			try {
				jsonObj = new JSONObject(result);
				JSONArray arr = jsonObj.getJSONArray("data");
				Log.d(TAG, arr.toString());
				ObjectMapper mapper = new ObjectMapper();
				mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
				mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
				if (type == GROUP_KEY)	{
					groups = mapper.readValue(arr.toString(), mapper.getTypeFactory().constructCollectionType(List.class, className));
					Log.d(TAG, groups.toString());
				} else {
					// type == FRIEND_KEY
					friends = mapper.readValue(arr.toString(), mapper.getTypeFactory().constructCollectionType(List.class, className));
					Log.d(TAG, friends.toString());
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Set the task to be finished
			isFinished[type] = true;
		}
	}
	
	class PopulateViewTask extends AsyncTask<Void, Void, List<FriendPageItem>>{

		static final long TIMEOUT_LENGTH = 30000;
		static final long REFRESH_INTERVAL = 100;
		
		@Override
		protected List<FriendPageItem> doInBackground(Void... params) {
			// Check if both lists are populated; if they are, assemble list and populate listview!
			// Can't check if these are null; if there's no data returned then this loop will never exit. Add a boolean instead.
			long timeWaiting = 0;
			while ((!isFinished[0] || !isFinished[1]) && timeWaiting < TIMEOUT_LENGTH)	{
				try {
					Thread.sleep(REFRESH_INTERVAL);
					timeWaiting += REFRESH_INTERVAL;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// If we're here, that means that either both the lists are complete, or we're timed out.
			if (timeWaiting < TIMEOUT_LENGTH)	{
				sections = new ArrayList<FriendPageSectionItem>();
				if (groups != null && groups.size() != 0)
					sections.add(new FriendPageSectionItem(GROUP_KEY, "群組", R.drawable.groups_header, groups.size()));
				if (friends != null && friends.size() != 0)
					sections.add(new FriendPageSectionItem(FRIEND_KEY, "好友", R.drawable.friends_header, friends.size()));
				list = populateAdapterList(groups, friends);
				childData = new HashMap<Integer, List<? extends FriendPageItem>>();
				childData.put(GROUP_KEY, groups);
				childData.put(FRIEND_KEY, friends);
				return list;
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<FriendPageItem> result) {
			adapter = new BasicFriendPageAdapter(getApplicationContext(), R.layout.list_friend_item, friends);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new ToggleClickListener(friends.size()));
			//adapter = new FriendPageAdapter(getApplicationContext(), sections, childData);
			//expandableListView.setAdapter(adapter);
			//expandableListView.expandGroup(FRIEND_KEY);
			//expandableListView.setOnChildClickListener(new ToggleChildClickListener(childData.get(FRIEND_KEY).size()));
		}
	}
	
	private class FriendsOnlyFilter extends Filter	{

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			Log.d(TAG, "Perform filtering entered with constraint " + constraint.toString());
			Log.d(TAG, friends.toString());
			if (constraint == null || constraint.length() == 0)	{
				results.values = friends;
				results.count = friends.size();
			} else	{
				ArrayList<FriendPageFriendItem> filteredFriends = new ArrayList<FriendPageFriendItem>();
				for (FriendPageFriendItem friendItem: friends)	{
					String[] nameSplit = friendItem.getUserName().split(" ");
					for (int i = 0; i < nameSplit.length; i++)
						if (nameSplit[i].toUpperCase().startsWith(constraint.toString().toUpperCase()))	{
							filteredFriends.add(friendItem);
							break;
						}
					Log.d(TAG, "Filtered Friends: " + filteredFriends.toString());
					results.values = filteredFriends;
					results.count = filteredFriends.size();
				}
			}
			
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			if (results.count == 0)
				adapter.notifyDataSetInvalidated();
			else	{
				ArrayList<FriendPageFriendItem> data = (ArrayList<FriendPageFriendItem>) results.values;
				adapter = new BasicFriendPageAdapter(getApplicationContext(),android.R.layout.simple_list_item_1, data);
				listView.setAdapter(adapter);
				//setIfSelected(listView, data);

			}
		}
		
	}
	
	private class FriendPageFilter extends Filter	{

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			// implement filter logic
			Log.d(TAG, "Perform filtering entered with constraint " + constraint.toString());
			Log.d(TAG, childData.toString());
			if (constraint == null || constraint.length() == 0)	{
				results.values = childData;
				results.count = 2;
			} else	{
				// perform filtering; filter through 2 lists, and then join lists again with populate
				ArrayList<FriendPageGroupItem> filteredGroups = new ArrayList<FriendPageGroupItem>();
				for (FriendPageGroupItem groupItem : groups)	{
					if (groupItem.getGroupName().toUpperCase().startsWith(constraint.toString().toUpperCase()))
						filteredGroups.add(groupItem);
				}
				ArrayList<FriendPageFriendItem> filteredFriends = new ArrayList<FriendPageFriendItem>();
				for (FriendPageFriendItem friendItem: friends)	{
					if (friendItem.getUserName().toUpperCase().startsWith(constraint.toString().toUpperCase()))
						filteredFriends.add(friendItem);
				}
				Log.d(TAG, "Filtered Friends: " + filteredFriends.toString());
				HashMap<Integer, List<? extends FriendPageItem>> filteredResults = new HashMap<Integer, List<? extends FriendPageItem>>(); 
				filteredResults.put(GROUP_KEY, filteredGroups);
				filteredResults.put(FRIEND_KEY, filteredFriends);
				results.values = filteredResults;
				results.count = 2;
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			if (results.count == 0)	{
				adapter.notifyDataSetInvalidated();
			} else	{
				
				HashMap<Integer, List<? extends FriendPageItem>> data = (HashMap<Integer, List<? extends FriendPageItem>>) results.values;
				//adapter.notifyDataSetChanged();
				/*
				adapter = new FriendPageAdapter(getApplicationContext(), sections, data);
				expandableListView.setAdapter(adapter);
				expandableListView.expandGroup(FRIEND_KEY);
				expandableListView.setOnGroupClickListener(new OnGroupClickListener() {
					
					@Override
					public boolean onGroupClick(ExpandableListView parent, View v,
							int groupPosition, long id) {
						Log.d(TAG, "Making groups key unclickable");
						if (groupPosition == GROUP_KEY)
							return true;
						else
							return false;
					}
				});
				*/
			}
		}
	}
	
	class ToggleClickListener implements OnItemClickListener	{
		public ToggleClickListener(int size)	{
			currentState = new boolean[size];
			for (int i = 0; i < size; i++)	{
				currentState[i] = false;
			}	
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.d(TAG, "Current state is " + currentState[position]);
			if (currentState[position] == false)	{
				currentState[position] = true;
				view.setSelected(true);
			} else	{
				currentState[position] = false;
				view.setSelected(false);
			}
		}
	}
	
	class ToggleChildClickListener implements ExpandableListView.OnChildClickListener	{

		public ToggleChildClickListener(int size) {
			currentState = new boolean[size];
			for (int i = 0; i < size; i++)	{
				currentState[i] = false;
			}	
		}
		
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			if (groupPosition == FRIEND_KEY)	{
				Log.d(TAG, "Current state is " + currentState[childPosition]);
				if (currentState[childPosition] == false)	{
					currentState[childPosition] = true;
					//int index = groupPosition*groups.size() + childPosition;
					int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
				    parent.setItemChecked(index, true);
				} else	{
					currentState[childPosition] = false;
					v.setSelected(false);
					//int index = groupPosition*childData.get("0").size() + childPosition;
					int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
				    parent.setItemChecked(index, false);
				}
				Log.d(TAG, "Child view group " + groupPosition + " and child " + childPosition + " set to " + currentState[childPosition]);
			}
			return true;
		}

		public boolean[] getCurrentState() {
			return currentState;
		}		
		
		
	}
}
