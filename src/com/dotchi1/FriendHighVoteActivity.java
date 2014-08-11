package com.dotchi1;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dotchi1.backend.ViewUtils;
import com.dotchi1.model.FriendPageFriendItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FriendHighVoteActivity extends Activity {

	ImageLoader imageLoader;
	DisplayImageOptions options;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_friend_high_vote);
		imageLoader = ImageLoader.getInstance();
		options = ViewUtils.getLocalImageConfiguration();
		Intent data = getIntent();
		
		ListView listView = (ListView) findViewById(R.id.friend_high_vote_list);
		ArrayList<FriendPageFriendItem> friends = (ArrayList<FriendPageFriendItem>) data.getSerializableExtra("friends");
		FriendListAdapter adapter = new FriendListAdapter(this, R.layout.game_choice_item, friends);
		listView.setAdapter(adapter);
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
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.friend_high_vote, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class FriendListAdapter	extends ArrayAdapter<FriendPageFriendItem>	{
		
		Context context;
		ArrayList<FriendPageFriendItem> values;
		
		public FriendListAdapter(Context context, int resId, List<FriendPageFriendItem> values)	{
			super(context, resId, values);
			this.context = context;
			this.values = new ArrayList<FriendPageFriendItem>(values);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			FriendPageFriendItem item = values.get(position);
			if (view == null)	{
				LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.game_choice_item, null);
			}
			ImageView headImage = (ImageView) view.findViewById(R.id.game_play_picture);
			imageLoader.displayImage(item.getHeadImage(), headImage, options);
			TextView name = (TextView) view.findViewById(R.id.game_play_choice_title);
			name.setText(item.getUserName());
			TextView desc = (TextView) view.findViewById(R.id.game_play_choice_description);
			desc.setVisibility(View.INVISIBLE);
			return view;
		}
		
		
	}

}
