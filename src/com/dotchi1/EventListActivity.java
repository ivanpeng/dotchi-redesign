package com.dotchi1;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.backend.ViewUtils;
import com.dotchi1.model.FriendPageFriendItem;
import com.dotchi1.model.GameCardItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class EventListActivity extends Activity {

	
	ImageLoader imageLoader;
	DisplayImageOptions options;
	
	ArrayList<Integer> userCount;
	ArrayList<ArrayList<FriendPageFriendItem>> friendList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_event_list);
		imageLoader = ImageLoader.getInstance();
		options = ViewUtils.getLocalImageConfiguration();
		
		Intent data = getIntent();
		final boolean isPersonal = data.getBooleanExtra("is_personal", false);
		//ArrayList<GameCardItem> items = (ArrayList<GameCardItem>)data.getSerializableExtra("high_vote");
		final ArrayList<GameCardItem> items = new ArrayList<GameCardItem>();
		userCount = new ArrayList<Integer>();
		friendList = new ArrayList<ArrayList<FriendPageFriendItem>>(); 
		new PostUrlTask(){

			@Override
			protected void onPostExecute(String result) {
				result = processResult(result);
				try {
					JSONArray ja = new JSONObject(result).getJSONArray("data");
					for (int i = 0; i < ja.length(); i++)	{
						JSONObject jo = ja.getJSONObject(i);
						GameCardItem item = new GameCardItem(jo.getString("item_image"), jo.getString("item_title"), jo.getString("item_content"));
						items.add(item);
						ArrayList<FriendPageFriendItem> friends = new ArrayList<FriendPageFriendItem>();
						JSONArray joFriends = jo.getJSONArray("user_list");
						for (int j = 0; j < joFriends.length(); j++)	{
							JSONObject joFriend = joFriends.getJSONObject(j);
							friends.add(new FriendPageFriendItem(Long.parseLong(joFriend.getString("dotchi_id")), joFriend.getString("head_image"), joFriend.getString("user_name")));
						}
						friendList.add(friends);
						userCount.add(friends.size());
						
					}
					ListView listView = (ListView) findViewById(R.id.show_all_choices);
					GameCardEventAdapter adapter = new GameCardEventAdapter(EventListActivity.this, R.layout.game_choice_item, items);
					listView.setAdapter(adapter);
					listView.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
							// Send the intent of displaying friends.
							if (isPersonal)	{
								Intent intent = new Intent(EventListActivity.this, FriendHighVoteActivity.class);
								intent.putExtra("friends", friendList.get(position));
								startActivity(intent);
							}
						}
					});
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
			
			
		}.execute(getResources().getString(R.string.api_root_url) + "/game/get_game_item_vote_users", "game_id", String.valueOf(data.getIntExtra("game_id", 0)));
		
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
	class GameCardEventAdapter extends ArrayAdapter<GameCardItem>	{

		Context context;
		ArrayList<GameCardItem> objects;
		
		public GameCardEventAdapter(Context context, int resId, List<GameCardItem> objects)	{
			super(context, resId, objects);
			this.context = context;
			this.objects = new ArrayList<GameCardItem>(objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			// Using class view doesn't usually matter for this case, but we should be careful accessing full class vars here
			GameCardItem item = objects.get(position);
			Integer numSaidYes = userCount.get(position);
			if (view == null)	{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.game_choice_item, null);
			}
			ImageView pp = (ImageView) view.findViewById(R.id.game_play_picture);
			imageLoader.displayImage(item.getItemImage(), pp, options);
			TextView title = (TextView) view.findViewById(R.id.game_play_choice_title);
			title.setText(item.getItemTitle());
			TextView desc = (TextView)view.findViewById(R.id.game_play_choice_description);
			desc.setText(item.getItemContent());
			TextView numYesView = (TextView) view.findViewById(R.id.num_said_yes);
			numYesView.setVisibility(View.VISIBLE);
			numYesView.setText(String.valueOf(numSaidYes) + "票");
			return view;
		}
		
		
		
	}
}
