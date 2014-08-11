package com.dotchi1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dotchi1.backend.ExpandableListAdapter;
import com.dotchi1.model.FriendPageFriendItem;
import com.dotchi1.model.FriendPageGroupItem;
import com.dotchi1.model.FriendPageItem;

public class NewFriendSelectActivity extends Activity {
	public static final String TAG = "NewFriendSelectActivity";
	
	static final int GROUP_KEY = 0;
	static final int FRIEND_KEY = 1;
	static final int UPDATE_COUNT = 10;
	
	private List<Integer> headers;
	private HashMap<Integer, List<? extends FriendPageItem>> childData;
	private ExpandableListView expandableListView;
	private ExpandableListAdapter friendAdapter;
	private ArrayList<FriendPageFriendItem> selectedList;
	
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
	
	
	@Override
	public void onBackPressed() {
		if (selectedCount > 0)	{
			findSelected(childData.get(FRIEND_KEY));
			// Instead of finishing the activity without setting a result, we'll just return the friends
			Intent returnIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("selected_list", selectedList);
			returnIntent.putExtras(bundle);
			setResult(RESULT_OK,  returnIntent);
			finish();
		} else
			super.onBackPressed();
	}
	
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
		Intent historicalIntent = getIntent();
		Bundle bundle = historicalIntent.getExtras();
		headers = new ArrayList<Integer>();
		headers.add(GROUP_KEY);
		headers.add(FRIEND_KEY);
		List<FriendPageGroupItem> groups = bundle.getParcelableArrayList("groups");
		List<FriendPageFriendItem> friends = bundle.getParcelableArrayList("friends");
		childData = new HashMap<Integer, List<? extends FriendPageItem> >();
		childData.put(GROUP_KEY, groups);
		childData.put(FRIEND_KEY, friends);
		// get information about already selected friends;
		setContentView(R.layout.new_friend_list);
		friendAdapter = new ExpandableListAdapter(this, headers, childData);
		
		expandableListView = (ExpandableListView) findViewById(R.id.new_friends_list);
		expandableListView.setAdapter(friendAdapter);
		expandableListView.expandGroup(FRIEND_KEY);
		
		// Set the number selected
		selectedList = bundle.getParcelableArrayList("selected_list");
		numSelectedView = (TextView) findViewById(R.id.friend_selected_count);
		selectedCount = selectedList == null ? 0 : selectedList.size();
		Message m = new Message();
		m.what = UPDATE_COUNT;
		friendsUiHandler.sendMessage(m);
		
		expandableListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// Get the item from data, and then set selected item;
				FriendPageItem item = childData.get(groupPosition).get(childPosition);
				Log.d(TAG, "Group position " + String.valueOf(groupPosition) + " and child position " + String.valueOf(childPosition) + " set to " + String.valueOf(!item.isSelected()));
				// Toggle it
				v.setSelected(!item.isSelected());
				item.setSelected(!item.isSelected());
				friendAdapter.notifyDataSetChanged();
				if (item.isSelected())	{
					// The item has changed to selected now; therefore we increment
					selectedCount ++;
				} else
					selectedCount --;
				Message message = new Message();
				message.what = UPDATE_COUNT;
				friendsUiHandler.sendMessage(message);
				return true;
			}
		});
		
		LinearLayout selectLayout = (LinearLayout) findViewById(R.id.friend_commands_layout);
		selectLayout.setVisibility(View.GONE);
		// Do later
		//adapter.setFilter();

	}

}
