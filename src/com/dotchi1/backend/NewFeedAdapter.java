package com.dotchi1.backend;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import com.devsmart.android.ui.HorizontalListView;
import com.dotchi1.FinalChoiceActivity;
import com.dotchi1.NewMainActivity;
import com.dotchi1.R;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.BaseFeedData;
import com.dotchi1.model.VoteItem;

import de.passsy.holocircularprogressbar.HoloCircularProgressBar;

public class NewFeedAdapter extends ArrayAdapter<BaseFeedData> {

	private static final int FEED_ADAPTER_HEAD_IMAGE_SIZE=60;
	
	public static final String TAG = "NewFeedAdapter";
	public static final int MOOD_COUNT_CHANGED = 1;
	public static final int COMMENT_CHANGED = 2;
	
	private static final String[] settingOptions = {"","Quit Game"};
	
	public static final int TOTAL_DAYS = 15;
	private Context context;
	private ArrayList<BaseFeedData> objects;
	static LiteImageLoader imageLoader;
	private static int screenWidth;
	
	static Handler newsFeedHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what)	{
			case MOOD_COUNT_CHANGED:
				ViewHolder holder = (ViewHolder) msg.obj;
				break;
			case COMMENT_CHANGED:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	public NewFeedAdapter(Context context, int textViewResourceId, ArrayList<BaseFeedData> objects, Integer s) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.objects = objects;
		imageLoader = new LiteImageLoader(context);
		if (s != null)
			screenWidth = s;
	}

	@Override
	public int getCount() {
		return objects.size();
	}

	/**
	 * This is called from list, so we need isInComment to be true.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;

		final BaseFeedData item = objects.get(position);
		if (view == null)	{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.new_feed_item, parent, false);
			holder = initHolder(view);
			view.setTag(holder);
		} else	{
			holder = (ViewHolder) view.getTag();
		}
		boolean isMoodClicked = item.getIsMood();
		populateView(view, context, holder, item, false);

		// One thing that we don't do in populate view: deleting the object
		// Set the settings drawer
		final String rootUrl = context.getResources().getString(R.string.api_test_root_url);
		final String dotchiId = ((Activity)context).getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE).getString("DOTCHI_ID", "0");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, settingOptions);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		holder.settingsLayout.setSelection(0);
		holder.settingsLayout.setAdapter(adapter);
		holder.settingsLayout.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position,
					long id) {
				// Position is always 0, there's only remove/quit game
				if (position == 1)	{
					new PostUrlTask()	{

						@Override
						protected void onPostExecute(String result) {
							result = processResult(result);
							try {
								JSONObject jo = new JSONObject(result).getJSONObject("data");
								if ("success".equals(jo.getString("status")))	{
									Log.d(TAG, "successfully deleted game");
									remove(item);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}

					}.execute(rootUrl + "/game/quit_game", "dotchi_id", dotchiId, "game_id", String.valueOf(item.getGameId()), "activity_id", 
							String.valueOf(item.getActivityId()));

				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		return view;
	}
	
	@Override
	public void add(BaseFeedData object) {
		super.add(object);
		objects.add(object);
	}

	@Override
	public void remove(BaseFeedData object) {
		super.remove(object);
		objects.remove(object);
	}

	public static ViewHolder initHolder(View view)	{
		if (imageLoader == null)	{
			imageLoader = new LiteImageLoader(view.getContext());
		}
		if (screenWidth == 0)	{
			Display d = ((Activity)view.getContext()).getWindowManager().getDefaultDisplay();
			Point p = new Point();
			d.getSize(p);
			screenWidth = p.x;
		}
		ViewHolder holder = new ViewHolder();
		holder.profilePicture = (ImageView) view.findViewById(R.id.new_feed_image);
		holder.title = (TextView) view.findViewById(R.id.new_feed_event_title);
		holder.timeRemainingView = (TextView) view.findViewById(R.id.new_feed_end_time);
		holder.descriptionView = (TextView) view.findViewById(R.id.new_feed_description);
		holder.keyView = view.findViewById(R.id.new_feed_is_secret_image);
		holder.photoRoll = (ViewPager) view.findViewById(R.id.photo_roll_list);
		holder.notifyImage = (ImageView) view.findViewById(R.id.mail_notification);
		holder.endTimeProgressBar = (HoloCircularProgressBar) view.findViewById(R.id.new_feed_progress);

		holder.settingsLayout = (Spinner) view.findViewById(R.id.new_feed_settings_drawer);
		return holder;
	}
	
	public static void populateView(final View view, final Context context, final ViewHolder holder, final BaseFeedData item, final boolean isInComment)	{
		
		int stubLoader = R.drawable.default_profile_pic;

		//Log.d(TAG, item.toString());
		// url is sometimes null!? 
		imageLoader.DisplayImage(item.getHeadImage(), stubLoader, holder.profilePicture, FEED_ADAPTER_HEAD_IMAGE_SIZE);

		holder.title.setText(item.getEventTitle());
		
		holder.timeRemainingView.setText(item.getEventTime());
		holder.descriptionView.setText(item.getGameTitle());
		// Key
		if (item.getIsSecret())
			holder.keyView.setVisibility(View.VISIBLE);
		else
			holder.keyView.setVisibility(View.INVISIBLE);		
		ImageView emptyPhotoRoll = (ImageView) view.findViewById(R.id.empty_photo_roll_holder);
		if (item.getVoteItem() != null && item.getVoteItem().size() > 0){
			VoteItemAdapter viAdapter = new VoteItemAdapter(context, R.layout.photo_roll_item, item.getVoteItem(), imageLoader);
			holder.photoRoll.setVisibility(View.VISIBLE);
			emptyPhotoRoll.setVisibility(View.INVISIBLE);
			//holder.photoRoll.setSnappingToCenter(true);
			holder.photoRoll.setAdapter(viAdapter);

		} else	{
			holder.photoRoll.setVisibility(View.INVISIBLE);
			emptyPhotoRoll.setVisibility(View.VISIBLE);
		}
		
		// Before we proceed, set the height based on height of device
		int height = screenWidth*2/3;
		// get RelativeLayout params and then set that, not horizontal listview
		RelativeLayout layoutContainer = (RelativeLayout) view.findViewById(R.id.photo_roll_container);
		LayoutParams lp = (LayoutParams) layoutContainer.getLayoutParams();
		lp.height = height;
		layoutContainer.setLayoutParams(lp);
		RelativeLayout relLayout = (RelativeLayout) view.findViewById(R.id.relativeLayout1);
		LayoutParams relParams = (LayoutParams) relLayout.getLayoutParams();
		relParams.setMargins(-4, height-105, 0, 0);
		
		if (item.getCategory() == 0)
			holder.endTimeProgressBar.setProgressBackgroundColor(Color.RED);
		else if (item.getCategory() == 1)
			holder.endTimeProgressBar.setProgressBackgroundColor(Color.YELLOW);
		else
			holder.endTimeProgressBar.setProgressBackgroundColor(Color.WHITE);
		// set progress
		int endTime = "".equals(item.getEndTime())? 0 : Integer.parseInt(item.getEndTime());
		holder.endTimeProgressBar.setProgress( (float)((TOTAL_DAYS-endTime)/(float)TOTAL_DAYS));
		

		if (item.getCategory() == 2 && item.getDotchiType() == 0 && item.getIsSendJoin() == false && item.getIsSendJoinNotice() == true)	{
			holder.notifyImage.setVisibility(View.VISIBLE);
			// set onclick listener for this item;
			holder.notifyImage.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// Start activity to choose data;
					// Remember to send all data for the grid!
					Intent intent = new Intent(context, FinalChoiceActivity.class);
					if (item.getVoteItem() != null && item.getVoteItem().size() > 0)
						intent.putExtra("choices", new ArrayList<VoteItem>(item.getVoteItem()));
					((Activity)context).startActivityForResult(intent, NewMainActivity.FINAL_DECISION_REQ_CODE);
				}
			});
		}
		else
			holder.notifyImage.setVisibility(View.GONE);
		
		
	}
	
	class ParticipatingStatusAdapter extends ArrayAdapter<Integer>	{

		private Context context;
		private List<Integer> resIdList;
		
		public ParticipatingStatusAdapter(Context context,
				int textViewResourceId, List<Integer> objects) {
			super(context, textViewResourceId, objects);
			this.context = context;
			this.resIdList = objects;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			return getCustomView(position, convertView, parent);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getCustomView(position, convertView, parent);
		}
		
		public View getCustomView(int position, View convertView, ViewGroup parent)	{
			// 
			View view = convertView;
			if (view == null)	{	
				// Do stuff here
				view = new View(context);
				view.setBackgroundResource(resIdList.get(position));
			}
			return view;	
		}
		
	}
	
	
	public static class ViewHolder	{
		ImageView profilePicture;
		TextView title;
		TextView timeRemainingView;
		TextView descriptionView;
		View keyView;
		ViewPager photoRoll;
		HorizontalListView moodListView;
		ImageView notifyImage;
		HoloCircularProgressBar endTimeProgressBar;

		Spinner settingsLayout;
		
	}

}
