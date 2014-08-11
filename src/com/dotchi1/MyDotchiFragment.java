package com.dotchi1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dotchi1.backend.DialogFriendsAdapter;
import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.backend.ViewUtils;
import com.dotchi1.model.BaseActivityItem;
import com.dotchi1.model.BaseActivityItem.ActivityType;
import com.dotchi1.model.EventItem;
import com.dotchi1.model.FeedItem;
import com.dotchi1.model.FriendPageFriendItem;
import com.dotchi1.model.GameCardItem;
import com.dotchi1.model.PendingItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MyDotchiFragment extends Fragment {
	
	public static final String TAG = "MyDotchiFragment";
	public static final String MYDOTCHI_FRAGMENT_ID_KEY = "MYDOTCHI_FRAGMENT_ID";
	
	private int fragmentId;
	ListView listView;
	ImageLoader imageLoader;
	DisplayImageOptions displayOptions;
	
	String dotchi_id;
	int voteLimit = -1;
	ArrayList<GameCardItem> gameCards;
	
	public static final MyDotchiFragment newInstance(int fragmentId)	{
		MyDotchiFragment f = new MyDotchiFragment();
		Bundle b = new Bundle(1);
		b.putInt(MYDOTCHI_FRAGMENT_ID_KEY, fragmentId);
		f.setArguments(b);
		return f;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragmentId = getArguments().getInt(MYDOTCHI_FRAGMENT_ID_KEY);
		imageLoader = ImageLoader.getInstance();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_mydotchi, container,
				false);
		listView = (ListView) rootView.findViewById(R.id.mydotchi_list_items);
		displayOptions = ViewUtils.getLocalImageConfiguration();
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences preferences = getActivity().getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		dotchi_id = preferences.getString("DOTCHI_ID", "");
		// Create the news feed
		String url = getResources().getString(R.string.api_root_url) + "/activity/get_activity";
		
		new MyDotchiPostTask(fragmentId).execute(url,"dotchi_id", dotchi_id, "category", String.valueOf(fragmentId));
	}
	
	public String calculateTimeLeft(Date expiryDate)	{
		// Calculate the time between set date and now
		Date now = new Date();
		DateTime start = new DateTime(now);
		DateTime end = new DateTime(expiryDate);
		StringBuilder sb = new StringBuilder("回覆倒數期限：");
		int days = Days.daysBetween(start, end).getDays();
		int hours = Hours.hoursBetween(start, end).getHours() % 24;
		int minutes = Minutes.minutesBetween(start, end).getMinutes() % 60;
		if (days < 0 || hours < 0 || minutes < 0)	
			return "0 分鐘";
		else {
			if (days != 0)
				sb.append(days).append("天,");
			if (hours != 0)
				sb.append(hours).append("小時,");
			sb.append(minutes).append("分");
		}
		return sb.toString();
	}
	
	class MyDotchiPostTask extends PostUrlTask	{
		
		private int category;
		
		public MyDotchiPostTask(int category) {
			this.category = category;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				//TODO: we have to make sure that we are still in this view; if not then we jump out of it.
				// We are going to do a two step processs; Jackson doesn't do well with nested values, so we use normal JSON to get the nested json value
				Log.d(TAG, result);
				JSONObject jsonObj = new JSONObject(processResult(result));
				JSONArray arr = jsonObj.getJSONArray("data");
				Log.d(TAG, arr.toString());
				ObjectMapper mapper = new ObjectMapper();
				mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
				mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
				mapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
				if (category == 0)	{
					final List<FeedItem> objects = mapper.readValue(arr.toString(), mapper.getTypeFactory().constructCollectionType(List.class, FeedItem.class));
					// Might want to save earlier feed data in android and then display it.
					try	{
						final FeedListAdapter adapter = new FeedListAdapter(getActivity(), objects);
						listView.setOnItemClickListener(new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
								FeedItem feedItem = objects.get(position);
								showPreGameDialog(adapter, feedItem);
								
							}
						});
						listView.setAdapter(adapter);
					} catch (NullPointerException nex)	{
						Log.w(TAG, "Null pointer exception thrown because clicking too fast. That's fine; we just don't need to display it.");
					}
				} else if (category == 1)	{
					mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
					mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
					mapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
					final List<PendingItem> objects = mapper.readValue(arr.toString(), mapper.getTypeFactory().constructCollectionType(List.class, PendingItem.class));
					// Might want to save earlier feed data in android and then display it.
					try	{
						final PendingListAdapter adapter = new PendingListAdapter(getActivity(), objects);
						listView.setOnItemClickListener(new OnItemClickListener() {
	
							@Override
							public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
								PendingItem pendingItem = objects.get(position);
								showPendingGameDialog(adapter, pendingItem);
							}
						});
						listView.setAdapter(adapter);
					} catch (NullPointerException nex)	{
						Log.w(TAG, "Null pointer exception thrown because clicking too fast. That's fine; we just don't need to display it.");
					}
				} else if (category == 2)	{
					List<EventItem> objects = mapper.readValue(arr.toString(), mapper.getTypeFactory().constructCollectionType(List.class, EventItem.class));
					// Might want to save earlier feed data in android and then display it.
					try	{
						EventListAdapter adapter = new EventListAdapter(getActivity(), objects);
						listView.setAdapter(adapter);
					} catch (NullPointerException nex)	{
						Log.w(TAG, "Null pointer exception thrown because clicking too fast. That's fine; we just don't need to display it.");
					}
				} else	{
					throw new RuntimeException("Invalid category id " + String.valueOf(category));
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	protected View showDialogTemplate(BaseActivityItem item, int layoutId){
		Log.d(TAG, "Showing dialog template");
		// Now render Dialog
		final View dialogView = View.inflate(getActivity(), layoutId, null);
		// Before we show dialog, we send a request to get vote limit
		new PostUrlTask() {
			@Override
			protected void onPostExecute(String result) {
				// Just use jsonobject to grab vote limit
				result = processResult(result);
				try {
					JSONArray ja = new JSONObject(result).getJSONArray("data");
					JSONObject jo = ja.getJSONObject(0);
					String vl = jo.getString("vote_limit");
					voteLimit = Integer.parseInt(vl);
					Log.d("Vote limit", "Vote limit is " + voteLimit);
					if (voteLimit == 0)
						voteLimit = -1;
					if (voteLimit > 0)	{
						ImageView numYesView = (ImageView) dialogView.findViewById(R.id.dotchi_num_yes_image);
						// Only need to do stuff for maxNumYes > 0
						numYesView.setImageResource(R.drawable.num_yes);
						TextView number = (TextView) dialogView.findViewById(R.id.num_yes_number);
						number.setVisibility(View.VISIBLE);
						number.setText(String.valueOf(voteLimit));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.execute(getResources().getString(R.string.api_root_url) + "/game/get_game_info", "game_id", String.valueOf(item.getGameId()));

		TextView dotchiGameDateView = (TextView) dialogView.findViewById(R.id.dotchi_game_set_datetime_text);
		String dateStr = item.getDotchiTime().equals("") || item.getDotchiTime().equals("0000-00-00 00:00:00") ? "" : item.getDotchiTime(); 
		dotchiGameDateView.setText(dateStr);
		TextView dotchiGameTitleView = (TextView) dialogView.findViewById(R.id.dotchi_game_set_title);
		dotchiGameTitleView.setText(item.getGameTitle());
		ImageView isPersonalImage = (ImageView) dialogView.findViewById(R.id.dotchi_is_personal_image);
		if (item.getIsPersonal())	
			isPersonalImage.setImageResource(R.drawable.dialog_personal);
		else
			isPersonalImage.setImageResource(R.drawable.dialog_not_personal);
		ImageView isPrivateImage = (ImageView) dialogView.findViewById(R.id.dotchi_is_private_image);
		Log.d(TAG, "is_secret" + item.getIsSecret());
		if (item.getIsSecret())
			isPrivateImage.setImageResource(R.drawable.dialog_not_public);
		else
			isPrivateImage.setImageResource(R.drawable.dialog_public);

		final GridView friendsView = (GridView) dialogView.findViewById(R.id.dialog_friends_confirm);
		// How do we get friends here?
		new PostUrlTask() {
			
			@Override
			protected void onPostExecute(String result) {
				result = processResult(result);
				try {
					JSONArray jsonArr = new JSONObject(result).getJSONArray("data");
					ObjectMapper mapper = new ObjectMapper();
					mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
					mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					List<FriendPageFriendItem> selectedFriendData = mapper.readValue(jsonArr.toString(), mapper.getTypeFactory().constructCollectionType(List.class, FriendPageFriendItem.class));
					DialogFriendsAdapter dfAdapter = new DialogFriendsAdapter(getActivity(), R.layout.dialog_friend_item, selectedFriendData);
					friendsView.setAdapter(dfAdapter);
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.execute(getResources().getString(R.string.api_root_url) + "/game/get_dotchi_join_status", "game_id", String.valueOf(item.getGameId()));
		return dialogView;
	}
	
	protected void showPreGameDialog(final FeedListAdapter adapter, BaseActivityItem item)	{
		final FeedItem feedItem = (FeedItem) item;
		// Reuse code!
		View dialogView = showDialogTemplate(item, R.layout.dialog_enter_game); 
		final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(dialogView)
				.show();
		ImageView cancel = (ImageView) dialogView.findViewById(R.id.dialog_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		ImageView confirm = (ImageView) dialogView.findViewById(R.id.participate_yes_button);
		confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int gameId = feedItem.getGameId();
				Intent intent = new Intent(getActivity(), GameActivity.class);
				intent.putExtra("game_id", gameId);
				intent.putExtra("game_title", feedItem.getGameTitle());
				String dateStr = feedItem.getDotchiTime().equals("") || feedItem.getDotchiTime().equals("0000-00-00 00:00:00") ? "" : feedItem.getDotchiTime(); 
				intent.putExtra("dotchi_time", dateStr);
				intent.putExtra("is_personal", feedItem.getIsPersonal());
				intent.putExtra("is_secret", feedItem.getIsSecret());
				// TODO: HARDCODED
				intent.putExtra("is_official", false);
				intent.putExtra("vote_limit", voteLimit);
				// We should probably have startActivityForResult...
				startActivity(intent);
				Log.d(TAG, "FeedItem event time: " + feedItem.getEventTime().toString());
				Log.d(TAG, "FeedItem event endtime: " + feedItem.getEndTime().toString());
				Log.d(TAG, "We have reached after the activity");
				dialog.dismiss();
			}
		});
		ImageView decline = (ImageView)dialogView.findViewById(R.id.participate_no_button);
		decline.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new PostUrlTask() {
					@Override
					protected void onPostExecute(String result) {
						result = processResult(result);
						try {
							JSONObject jsonObj = new JSONObject(result).getJSONObject("data");
							if (jsonObj.getString("status").equals("success"))	{
								Log.d(TAG,"We are successful in quitting game.");
								adapter.remove(feedItem);
							}
							else
								throw new RuntimeException("Error trying to quit game.");
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}.execute(getResources().getString(R.string.api_root_url) + "/game/quit_game", "dotchi_id", dotchi_id, "game_id", String.valueOf(feedItem.getGameId()),
							"activity_id", String.valueOf(feedItem.getActivityId()));
				dialog.dismiss();
			}
		});
	}
	
	public void showPendingGameDialog(PendingListAdapter adapter, BaseActivityItem item)	{
		final PendingItem pendingItem = (PendingItem) item;
		View dialogView = showDialogTemplate(item, R.layout.dialog_confirm_game_results);
		final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(dialogView)
				.show();
		ImageView cancel = (ImageView) dialogView.findViewById(R.id.dialog_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		gameCards = new ArrayList<GameCardItem>();
		final ListView gameCardView = (ListView) dialogView.findViewById(R.id.user_results);		
		final ArrayAdapter<GameCardItem> gameCardAdapter = new ArrayAdapter<GameCardItem>(getActivity(), R.layout.game_choice_item, gameCards){
	
			@Override
			public int getCount() {
				return gameCards.size();
			}

			@Override
			public GameCardItem getItem(int position) {
				return gameCards.get(position);
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				GameCardItem item = gameCards.get(position);
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = inflater.inflate(R.layout.game_choice_item, parent, false);
				ImageView gameCardImage = (ImageView) view.findViewById(R.id.game_play_picture);
				imageLoader.displayImage(item.getItemImage(), gameCardImage, displayOptions);
				TextView gameCardTitle = (TextView) view.findViewById(R.id.game_play_choice_title);
				gameCardTitle.setText(item.getItemTitle());
				TextView gameCardContent = (TextView) view.findViewById(R.id.game_play_choice_description);
				gameCardContent.setText(item.getItemContent());
				return view;
			}
		};
		new PostUrlTask() {
			
			@Override
			protected void onPostExecute(String result) {
				result = processResult(result);
				JSONArray jsonArr;
				try {
					jsonArr = new JSONObject(result).getJSONArray("data");
					for (int i = 0; i < jsonArr.length(); i++)	{
						JSONObject jo = jsonArr.getJSONObject(i);
						GameCardItem item = new GameCardItem(jo.getString("item_image"), jo.getString("item_title"), jo.getString("item_content"));
						gameCards.add(item);
					}
					gameCardView.setAdapter(gameCardAdapter);

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.execute(getResources().getString(R.string.api_root_url) + "/game/get_game_item_high_vote_result", "game_id", String.valueOf(pendingItem.getGameId()));
		
		ImageView confirm = (ImageView)dialogView.findViewById(R.id.dialog_game_complete_confirm_button);
		confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();				
			}
		});
	}
	
	class FeedListAdapter extends ArrayAdapter<FeedItem> {
		private final Context context;
		private final ArrayList<FeedItem> values;
		private ImageLoader imageLoader;
		
		public FeedListAdapter(Context context, List<FeedItem> values) {
			super(context, R.layout.feed_list_item, values);
			this.context = context;
			this.values = new ArrayList<FeedItem>(values);
			imageLoader = ImageLoader.getInstance();
			
		}
		

		@Override
		public void remove(FeedItem object) {
			super.remove(object);
			values.remove(values.indexOf(object));
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.feed_list_item, parent, false);
			FeedItem item = values.get(position);
			Log.d(TAG, item.toString());
			ImageView iv = (ImageView) view.findViewById(R.id.list_item_picture);
			imageLoader.displayImage(item.getHeadImage(), iv, displayOptions);
			TextView hostName = (TextView) view.findViewById(R.id.feed_profile_name);
			hostName.setText(item.getUserName());

			TextView content = (TextView) view.findViewById(R.id.feed_title);
			
			if (item.getActivityType() != ActivityType.FRIEND_INVITE && item.getActivityType() != ActivityType.MESSAGE)	{
				content.setText(item.getGameTitle());
				TextView timeRemaining = (TextView) view.findViewById(R.id.time_left);
				timeRemaining.setVisibility(View.VISIBLE);
				timeRemaining.setText(calculateTimeLeft(item.getEndTime()));
				
				LinearLayout icons = (LinearLayout) view.findViewById(R.id.icon_list);
				ImageView secretView = new ImageView(getContext());
				if (item.getIsSecret())	{
					secretView.setImageResource(R.drawable.is_secret_negative_icon);
				} else
					secretView.setImageResource(R.drawable.is_secret_positive_icon);
				icons.addView(secretView);
				ImageView personalView = new ImageView(getContext());
				if (item.getIsPersonal())	{
					personalView.setImageResource(R.drawable.is_personal_positive_icon);
				} else
					personalView.setImageResource(R.drawable.is_personal_negative_icon);
				icons.addView(personalView);
				if (item.getIsOfficial()==true)	{
					ImageView officialView = new ImageView(getContext());
					officialView.setImageResource(R.drawable.is_official_icon);
					icons.addView(officialView);
				}
			} else {
				// FRIEND or MESSAGE; don't display time, but display the box if it's 2
				content.setText(item.getEventTitle());
				if (item.getActivityType() == ActivityType.FRIEND_INVITE)	{
					// Display friend
					ImageView yesButton = (ImageView) view.findViewById(R.id.yes_response_button);
					yesButton.setVisibility(View.VISIBLE);
					ImageView noButton = (ImageView) view.findViewById(R.id.no_response_button);
					noButton.setVisibility(View.VISIBLE);
				}
			}
			
			return view;
		}
		
	}
	
	class PendingListAdapter extends ArrayAdapter<PendingItem>	{
		private final Context context;
		private final ArrayList<PendingItem> values;
		private ImageLoader imageLoader;
		
		public PendingListAdapter(Context context, List<PendingItem> values)	{
			super(context, R.layout.pending_list_item, values);
			this.context = context;
			this.values = new ArrayList<PendingItem>(values);
			imageLoader = ImageLoader.getInstance();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.pending_list_item, parent, false);
			PendingItem item = values.get(position);
			Log.d(TAG, item.toString());
			ImageView iv = (ImageView) view.findViewById(R.id.list_item_picture);
			imageLoader.displayImage(item.getHeadImage(), iv, displayOptions);
			TextView hostName = (TextView) view.findViewById(R.id.feed_profile_name);
			hostName.setText(item.getUserName());
			TextView timeRemaining = (TextView) view.findViewById(R.id.time_left);
			timeRemaining.setText(calculateTimeLeft(item.getEndTime()));
			TextView content = (TextView) view.findViewById(R.id.feed_title);
			content.setText(item.getGameTitle());
			
			// Set the icons now
			Log.d(TAG, "For Activity Id " + item.getActivityId());
			Log.d(TAG, "Is Secret: " + item.getIsSecret());
			Log.d(TAG, "Is Personal: " + item.getIsPersonal());
			Log.d(TAG, "End Time: " + item.getEndTime());
			Log.d(TAG, "Is Official: " + item.getIsOfficial());
			LinearLayout icons = (LinearLayout) view.findViewById(R.id.icon_list);
			ImageView secretView = new ImageView(getContext());
			if (item.getIsSecret())	{
				secretView.setImageResource(R.drawable.is_secret_negative_icon);
			} else
				secretView.setImageResource(R.drawable.is_secret_positive_icon);
			icons.addView(secretView);
			ImageView personalView = new ImageView(getContext());
			if (item.getIsPersonal())	{
				personalView.setImageResource(R.drawable.is_personal_positive_icon);
			} else
				personalView.setImageResource(R.drawable.is_personal_negative_icon);
			icons.addView(personalView);
			if (item.getIsOfficial())	{
				ImageView officialView = new ImageView(getContext());
				officialView.setImageResource(R.drawable.is_official_icon);
				icons.addView(officialView);
			}
			
			return view;
		}
		
	}
	
	class EventListAdapter extends ArrayAdapter<EventItem>	{
		private final Context context;
		private final ArrayList<EventItem> values;
		private ImageLoader imageLoader;
		
		public EventListAdapter(Context context, List<EventItem> values)	{
			super(context, R.layout.event_list_item, values);
			this.context = context;
			this.values = new ArrayList<EventItem>(values);
			imageLoader = ImageLoader.getInstance();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// There are two views that you want to render in this situation; first is the incomplete response, and the second is the complete

			final EventItem item = values.get(position);
			Log.d(TAG, item.toString());
			
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.event_list_item, parent, false);

			ImageView iv = (ImageView) view.findViewById(R.id.list_item_picture);
			imageLoader.displayImage(item.getHeadImage(), iv, displayOptions);
			TextView hostName = (TextView) view.findViewById(R.id.feed_profile_name);
			hostName.setText(item.getUserName());
			TextView matchedDateText = (TextView) view.findViewById(R.id.matched_date);
			Log.d(TAG, item.getDotchiTime());
			matchedDateText.setText(item.getDotchiTime());
			TextView eventTitle = (TextView) view.findViewById(R.id.event_description);
			eventTitle.setText(item.getGameTitle());				

			LinearLayout icons = (LinearLayout) view.findViewById(R.id.icon_list);
			ImageView secretView = new ImageView(getContext());
			if (item.getIsSecret())	{
				secretView.setImageResource(R.drawable.is_secret_negative_icon);
			} else
				secretView.setImageResource(R.drawable.is_secret_positive_icon);
			icons.addView(secretView);
			ImageView personalView = new ImageView(getContext());
			if (item.getIsPersonal())	{
				personalView.setImageResource(R.drawable.is_personal_positive_icon);
			} else
				personalView.setImageResource(R.drawable.is_personal_negative_icon);
			icons.addView(personalView);
			if (item.getIsOfficial())	{
				ImageView officialView = new ImageView(getContext());
				officialView.setImageResource(R.drawable.is_official_icon);
				icons.addView(officialView);
			}
			final TextView topChoicesView = (TextView)view.findViewById(R.id.event_top_choice_name);
			if (item.getHighVote() != null && item.getHighVote().size() > 0)
				topChoicesView.setText(item.getHighVote().get(0).getItemTitle());
			else
				topChoicesView.setText("");
			final Button seeAllButton = (Button) view.findViewById(R.id.event_show_all_List);
			seeAllButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), EventListActivity.class);
					if (item.getHighVote() != null && item.getHighVote().size() > 0){
						ArrayList<GameCardItem> items = new ArrayList<GameCardItem>(item.getHighVote());
						intent.putExtra("high_vote", items);
					}
					intent.putExtra("game_id", item.getGameId());
					intent.putExtra("is_personal", item.getIsPersonal());
					startActivity(intent);
				}
			});
			
			return view;

		}
		
	}

}
