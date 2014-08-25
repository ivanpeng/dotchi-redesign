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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dotchi1.backend.ExpandableListAdapter;
import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.backend.json.JsonDataWrapper;
import com.dotchi1.model.FriendPageFriendItem;
import com.dotchi1.model.FriendPageGroupItem;
import com.dotchi1.model.FriendPageItem;

public class NewFriendSelectActivity extends ActionBarActivity {
	public static final String TAG = "NewFriendSelectActivity";
	
	static final int GROUP_KEY = 0;
	static final int FRIEND_KEY = 1;
	static final int UPDATE_COUNT = 10;
	
	private List<Integer> headers;
	private HashMap<Integer, List<? extends FriendPageItem>> childData;
	private ExpandableListView expandableListView;
	private ExpandableListAdapter friendAdapter;
	private ArrayList<FriendPageFriendItem> selectedList;

	private boolean[] isFinished = {false,false};
	private static TextView numSelectedView;
	private static int selectedCount = 0;
	private static Handler friendsUiHandler = new Handler()	{

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
				case (UPDATE_COUNT):
					numSelectedView.setText(""+ selectedCount);
					break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	
	/**
	 * This method sets the selectedList
	 */
	protected void findSelected(List<? extends FriendPageItem> list)	{
		if (selectedList == null)	{
			selectedList = new ArrayList<FriendPageFriendItem>();
		}
		for (FriendPageItem friend: list)	{
			if (friend.isSelected())
				selectedList.add((FriendPageFriendItem)friend);
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_friend_list);
		View actionbar = getLayoutInflater().inflate(R.layout.menu_dotchi_package, null);
		TextView menuTitle = (TextView) actionbar.findViewById(R.id.package_title);
		menuTitle.setText("Select Friends");
		ImageButton backButton = (ImageButton) actionbar.findViewById(R.id.back_home_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		ImageButton forwardButton = (ImageButton) actionbar.findViewById(R.id.forward_button);
		forwardButton.setVisibility(View.VISIBLE);
		forwardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selectedCount == 0)	{
					Toast.makeText(NewFriendSelectActivity.this, "Can't select 0 friends", Toast.LENGTH_LONG).show();
				} else	{
					Bundle bundle = packageFriendsToBundle();
					// Send off
					// Create game
					createGame();

				}
			}
		});
		getSupportActionBar().setCustomView(actionbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		
		String rootUrl = getResources().getString(R.string.api_test_root_url);
		String dotchiId = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE).getString("DOTCHI_ID", "0");
		Intent historicalIntent = getIntent();
		Bundle bundle = historicalIntent.getExtras();
		headers = new ArrayList<Integer>();
		headers.add(GROUP_KEY);
		headers.add(FRIEND_KEY);
		List<FriendPageGroupItem> groups = bundle.getParcelableArrayList("groups");
		List<FriendPageFriendItem> friends = bundle.getParcelableArrayList("friends");
		expandableListView = (ExpandableListView) findViewById(R.id.new_friends_list);
		numSelectedView = (TextView) findViewById(R.id.friend_selected_count);
		LinearLayout selectLayout = (LinearLayout) findViewById(R.id.friend_commands_layout);
		selectLayout.setVisibility(View.GONE);

		if (groups == null || groups.size() == 0)	{
			// Do a call
			new BaseGetFriendsUrlTask(FriendPageGroupItem.class, GROUP_KEY).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rootUrl + "/users/get_user_groups", "dotchi_id", dotchiId);
		} else 
			isFinished[GROUP_KEY] = true;
		if (friends == null || friends.size() == 0)	{
		    new BaseGetFriendsUrlTask(FriendPageFriendItem.class, FRIEND_KEY).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rootUrl + "/users/get_user_friends", "dotchi_id", dotchiId);
		} else
			isFinished[FRIEND_KEY] = true;
		new PopulateViewTask().execute();
	}
	
	protected Bundle packageFriendsToBundle() {
		findSelected(childData.get(FRIEND_KEY));
		Bundle bundle = getIntent().getExtras();
		bundle.putParcelableArrayList("selected_list", selectedList);
		return bundle;
	}
	
	protected String makeFriendString()	{
		JSONArray arr2 = new JSONArray();
		for (FriendPageFriendItem friend : selectedList)	{
			JSONObject jo = new JSONObject();
			try {
				jo.put("dotchi_id", friend.getDotchiId());
				arr2.put(jo);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		String friendStr = JsonDataWrapper.wrapData(arr2);
		return friendStr;
	}
	
	protected void createGame()	{
		final String rootUrl = getResources().getString(R.string.api_test_root_url);
		Bundle bundle = getIntent().getExtras();
		final String dotchiType = bundle.getString("dotchi_type");
		final String gameTitle = bundle.getString("game_title", "");
		final String isOfficial = bundle.getBoolean("is_official")? "1":"0";
		final String isPersonal = bundle.getBoolean("is_personal") ? "1":"0";
		final String isSecret = bundle.getBoolean("is_secret") ? "1":"0";
		final String dotchiTime = bundle.getString("dotchi_time");
		final String replyDay = bundle.getString("reply_day");
		final String voteLimit = bundle.getString("vote_limit");
		final String dotchiId = bundle.getString("dotchi_id");
		final String gameItemStr = bundle.getString("game_item");
		final String friendStr = makeFriendString();
		
		new PostUrlTask()	{

			@Override
			protected void onPostExecute(String result) {
				result = processResult(result);
				try {
					JSONObject jo = new JSONObject(result).getJSONObject("data");
					if (jo.has("game_id"))	{
						// Successful! Game created!
						// Start game from here
						Intent intent = new Intent(NewFriendSelectActivity.this, GameActivity.class);
						intent.putExtra("game_id", jo.getInt("game_id"));
						intent.putExtra("game_title", gameTitle);
						intent.putExtra("dotchi_time", dotchiTime);
						intent.putExtra("is_personal", isPersonal);
						intent.putExtra("is_secret", isSecret);
						intent.putExtra("is_official", isOfficial);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		}.execute(rootUrl + "/game/create_game", "game_title", gameTitle, "dotchi_type", dotchiType, "is_official", isOfficial,
						"is_personal", isPersonal, "is_secret", isSecret, "reply_day", String.valueOf(replyDay),
						"vote_limit", String.valueOf(voteLimit), "dotchi_id", String.valueOf(dotchiId), "game_item", gameItemStr,
						"invite_friends", friendStr, "dotchi_time", dotchiTime);
	}

	@SuppressWarnings("rawtypes")
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
				if (childData == null)
					childData = new HashMap<Integer, List<? extends FriendPageItem>>();
				ObjectMapper mapper = new ObjectMapper();
				mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
				mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
				if (type == GROUP_KEY)	{
					List<FriendPageGroupItem> groups = mapper.readValue(arr.toString(), mapper.getTypeFactory().constructCollectionType(List.class, className));
					childData.put(GROUP_KEY, groups);
					Log.d(TAG, groups.toString());
				} else {
					// type == FRIEND_KEY
					List<FriendPageFriendItem> friends = mapper.readValue(arr.toString(), mapper.getTypeFactory().constructCollectionType(List.class, className));
					childData.put(FRIEND_KEY, friends.subList(0, 50));
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
	
	
	
	class PopulateViewTask extends AsyncTask<Void, Void, Void>{

		static final long TIMEOUT_LENGTH = 30000;
		static final long REFRESH_INTERVAL = 100;
		
		@Override
		protected Void doInBackground(Void... params) {
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
				// We also want to 
				return null;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			headers = new ArrayList<Integer>();
			headers.add(GROUP_KEY);
			headers.add(FRIEND_KEY);
			friendAdapter = new ExpandableListAdapter(NewFriendSelectActivity.this, headers, childData);
			expandableListView.setAdapter(friendAdapter);
			expandableListView.expandGroup(GROUP_KEY);
			expandableListView.expandGroup(FRIEND_KEY);
			friendAdapter.setFilter(new FriendPageFilter(childData));
			final List<FriendPageFriendItem> l = (List<FriendPageFriendItem>) childData.get(FRIEND_KEY);
			
			//slidingMenu.setSecondaryMenu(friendLayout);
			expandableListView.setOnChildClickListener(new OnChildClickListener() {
				@Override
				public boolean onChildClick(ExpandableListView parent, final View v,
						int groupPosition, int childPosition, long id) {
					// Get the item from data, and then set selected item;
					final FriendPageItem item = childData.get(groupPosition).get(childPosition);
					Log.d(TAG, "Group position " + String.valueOf(groupPosition) + " and child position " + String.valueOf(childPosition) + " set to " + String.valueOf(!item.isSelected()));
					// Toggle it
					// Before we jump into group or friend, we determine whether or not this item is selected
					ToggleButton highlightedButton = (ToggleButton) v.findViewById(R.id.friend_invite_button);
					highlightedButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							item.setSelected(isChecked);
							v.setSelected(isChecked);
						}
						
					});
					highlightedButton.setChecked(!item.isSelected());
					if (item.isSelected())	{
						selectedCount++;
					} else
						selectedCount--;
					Message m = new Message();
					m.what = UPDATE_COUNT;
					friendsUiHandler.sendMessage(m);
					return false;
				}
			});
			final EditText searchbar = (EditText) findViewById(R.id.friend_search_text);
			searchbar.addTextChangedListener(new TextWatcher(){
				@Override
				public void afterTextChanged(Editable s) {
					Log.d(TAG, "Text changed: " + s.toString());
					friendAdapter.getFilter().filter(s.toString());
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
		}

	}
	
	/**
	 * This function is much more nested with this class than the adapter, so we keep here. We have the set filter layer done here as opposed
	 * to being done in ExpandableListAdapter; to keep it generic. The only thing is, we need to set FriendPageFilter for NewFriendSelectActivity as well
	 * @author Ivan
	 *
	 */
	public class FriendPageFilter extends Filter	{
		
		HashMap<Integer, List<? extends FriendPageItem>> filteredData;
		
		public FriendPageFilter(HashMap<Integer, List<? extends FriendPageItem>> data)	{
			super();
			this.filteredData = data;
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			// implement filter logic
			Log.d(TAG, "Perform filtering entered with constraint " + constraint.toString());
			Log.d(TAG, childData.toString());
			List<FriendPageGroupItem> groups = (List<FriendPageGroupItem>) filteredData.get(GROUP_KEY);
			List<FriendPageFriendItem> friends = (List<FriendPageFriendItem>) filteredData.get(FRIEND_KEY);
			
			if (constraint == null || constraint.length() == 0)	{
				results.values = childData;
				results.count = 2;
			} else	{
				// perform filtering; filter through 2 lists, and then join lists again with populate
				ArrayList<FriendPageGroupItem> filteredGroups = new ArrayList<FriendPageGroupItem>();
//				for (FriendPageItem g : groups)	{
//					FriendPageGroupItem groupItem = (FriendPageGroupItem)g;
//					if (groupItem.getGroupName().toUpperCase().startsWith(constraint.toString().toUpperCase()))
//						filteredGroups.add(groupItem);
//				}
				ArrayList<FriendPageFriendItem> filteredFriends = new ArrayList<FriendPageFriendItem>();
				for (FriendPageItem f: friends)	{
					FriendPageFriendItem friendItem = (FriendPageFriendItem) f;
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
				friendAdapter.notifyDataSetInvalidated();
			} else	{
				HashMap<Integer, List<? extends FriendPageItem>> data = (HashMap<Integer, List<? extends FriendPageItem>>) results.values;
				if (data.get(FRIEND_KEY).size() != friendAdapter.getChildrenCount(FRIEND_KEY))	{
					friendAdapter = new ExpandableListAdapter(NewFriendSelectActivity.this, headers, data);
					friendAdapter.setFilter(new FriendPageFilter(childData));
					expandableListView.setAdapter(friendAdapter);
					expandableListView.expandGroup(FRIEND_KEY);
				}
				
			}
		}
	}
	

}
