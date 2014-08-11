package com.dotchi1;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.FriendPageFriendItem;
import com.dotchi1.model.FriendPageItem;

/**
 * Simple class which calls make group activity. There should be no reason this calls a URL task except for the last part.
 * This is a simple FriendAdapter, wrapped with an action bar updating the status.
 * @author Ivan
 *
 */
public class MakeGroupActivity extends ActionBarActivity {

	private static final int FRIEND_COUNT_KEY = 1;
	private static TextView friendCountView;
	private ListView listView;
	private static int count;
	private ArrayList<FriendPageItem> friendList;
	
	private static Handler uiCountHandler = new Handler()	{

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what)	{
			case FRIEND_COUNT_KEY:
				friendCountView.setText(""+count);
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_make_group);
		final String rootUrl = getResources().getString(R.string.api_test_root_url);
		final String dotchiId = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE).getString("DOTCHI_ID", "0");
		count = 0;
		// Set actionbar
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View actionbar = inflater.inflate(R.layout.menu_make_group, null);
		ImageView backButton = (ImageView) actionbar.findViewById(R.id.menu_back);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();			
			}
		});
		final EditText titleView = (EditText) actionbar.findViewById(R.id.menu_title);
		Button submitButton = (Button) actionbar.findViewById(R.id.submit_group);
		submitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Finish activity by setting results.
				// Want to put selected friends, and create group.
				final ArrayList<FriendPageItem> selectedFriends = getSelectedFriends();
				String friendsId = getFriendIds(selectedFriends);
				final String groupName = titleView.getText() != null && titleView.getText().length() > 0? titleView.getText().toString() : "";
				// Call group API to create group
				new PostUrlTask()	{

					@Override
					protected void onPostExecute(String result) {
						Intent returnIntent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putParcelableArrayList("selected_friends", selectedFriends);
						returnIntent.putExtras(bundle);
						setResult(RESULT_OK, returnIntent);
						finish();
					}
					
				}.execute(rootUrl + "/users/create_user_group", "dotchi_id", dotchiId, "group_name", groupName, "friends_id", friendsId);
			}
		});
		getSupportActionBar().setCustomView(actionbar);
	    getSupportActionBar().setDisplayShowTitleEnabled(false);
	    getSupportActionBar().setDisplayShowCustomEnabled(true);
	   	getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		
		listView = (ListView) findViewById(R.id.friend_list);
		// A generic list holder
		Bundle bundle = getIntent().getExtras();
		friendCountView = (TextView) findViewById(R.id.friend_selected_count);
		friendList = bundle.getParcelableArrayList("friends");
		int c = 0;
		for (FriendPageItem fpi : friendList)	{
			if (fpi.isSelected())
				c++;
		}
		count = c;
		Message m = new Message();
		m.what = FRIEND_COUNT_KEY;
		uiCountHandler.handleMessage(m);
		
		SimpleFriendAdapter adapter = new SimpleFriendAdapter(this, R.layout.list_friend_item, friendList);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, final View view, int position,
					long id) {
				// Set click
				final FriendPageItem item = friendList.get(position);
				ToggleButton highlightedButton = (ToggleButton) view.findViewById(R.id.friend_invite_button);
				highlightedButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// Relying on selected is erroneous. Have to use this convoluted way to toggle a view
						view.setSelected(isChecked);
						item.setSelected(isChecked);
					}
					
				});
				highlightedButton.setChecked(!item.isSelected());
				if (item.isSelected())	{
					// The item has changed to selected now; therefore we increment
					count ++;
				} else
					count --;
				Message message = new Message();
				message.what = FRIEND_COUNT_KEY;
				uiCountHandler.sendMessage(message);
			}
		});
		
	}
	
	protected ArrayList<FriendPageItem> getSelectedFriends()	{
		ArrayList<FriendPageItem> list = new ArrayList<FriendPageItem>();
		for (FriendPageItem item: friendList)	{
			if (item.isSelected())
				list.add(item);
		}
		return list;
	}
	
	protected String getFriendIds(ArrayList<FriendPageItem> list)	{
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (FriendPageItem item: list)	{
			if (!isFirst)	{
				sb.append(",");
				isFirst = false;
			}
			FriendPageFriendItem fi = (FriendPageFriendItem) item;
			sb.append(fi.getDotchiId());
		}
		return sb.toString();
	}
	/**
	 * Similar to ExpandableListAdapter, except we put it in the context of a list, not expandable list.
	 * @author Ivan
	 *
	 */
	class SimpleFriendAdapter extends ArrayAdapter<FriendPageItem>	{

		private Context context;
		private ArrayList<FriendPageItem> objects;
		private LiteImageLoader imageLoader;
		
		public SimpleFriendAdapter(Context context, int textViewResourceId,
				List<FriendPageItem> objects) {
			super(context, textViewResourceId, objects);
			this.context = context;
			this.objects = new ArrayList<FriendPageItem>(objects);
			imageLoader = new LiteImageLoader(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = null;
			FriendPageFriendItem fi = (FriendPageFriendItem) objects.get(position);
			if (view == null)	{
				view = inflater.inflate(R.layout.list_friend_item, null);
			}
			ImageView headImage = (ImageView) view.findViewById(R.id.friend_head_image);
			imageLoader.DisplayImage(fi.getHeadImage(), R.drawable.friend_default_profile_picture, headImage, 140);
			TextView userName = (TextView) view.findViewById(R.id.item_name);
			userName.setText(fi.getUserName());
			ToggleButton highlightedButton = (ToggleButton) view.findViewById(R.id.friend_invite_button);
			
			highlightedButton.setChecked(fi.isSelected());
			return view;
		}
		
	}


}
