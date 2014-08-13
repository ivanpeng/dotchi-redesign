package com.dotchi1.backend;

import java.util.ArrayList;
import java.util.List;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import net.londatiga.android.QuickAction.OnActionItemClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import com.devsmart.android.ui.HorizontalListView;
import com.dotchi1.CommentActivity;
import com.dotchi1.CommentActivity.MoodAdapter;
import com.dotchi1.EventChoicesActivity;
import com.dotchi1.FinalChoiceActivity;
import com.dotchi1.GameActivity;
import com.dotchi1.NewMainActivity;
import com.dotchi1.R;
import com.dotchi1.ShowListActivity;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.BaseFeedData;
import com.dotchi1.model.MoodItem;
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
				holder.moodCount.setText(String.valueOf(msg.arg1));
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
		if (isMoodClicked || item.getIsMood())	{
			holder.moodLayout.setClickable(false);
		}
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
		ViewHolder holder = new ViewHolder();
		holder.profilePicture = (ImageView) view.findViewById(R.id.new_feed_image);
		holder.title = (TextView) view.findViewById(R.id.new_feed_event_title);
		holder.timeRemainingView = (TextView) view.findViewById(R.id.new_feed_end_time);
		holder.descriptionView = (TextView) view.findViewById(R.id.new_feed_description);
		holder.keyView = view.findViewById(R.id.new_feed_is_secret_image);
		holder.photoRoll = (ViewPager) view.findViewById(R.id.photo_roll_list);
		holder.notifyImage = (ImageView) view.findViewById(R.id.mail_notification);
		holder.endTimeProgressBar = (HoloCircularProgressBar) view.findViewById(R.id.new_feed_progress);
		holder.moodLayout = (RelativeLayout) view.findViewById(R.id.new_feed_like_button);
		holder.moodCount = (TextView) holder.moodLayout.findViewById(R.id.num_hearts);
		holder.moodContainer = (LinearLayout) view.findViewById(R.id.mood_container);
		holder.moodListView = (HorizontalListView) view.findViewById(R.id.mood_list);
		holder.commentLayout = (RelativeLayout) view.findViewById(R.id.new_feed_comment_button);
		holder.commentCount = (TextView) holder.commentLayout.findViewById(R.id.num_comments);
		holder.voteLayout = (RelativeLayout) view.findViewById(R.id.new_feed_vote_button);
		holder.voteCount = (TextView) view.findViewById(R.id.num_vote);
		holder.participateLayout = (RelativeLayout) view.findViewById(R.id.new_feed_participate_button);
		holder.participateCount = (TextView) view.findViewById(R.id.num_participate);
		holder.settingsLayout = (Spinner) view.findViewById(R.id.new_feed_settings_drawer);
		return holder;
	}
	
	public static void populateView(final View view, final Context context, final ViewHolder holder, final BaseFeedData item, final boolean isInComment)	{
		
		int stubLoader = R.drawable.default_profile_pic;
		if (isInComment)	{
			if (item.getMoodCount() > 0)
				holder.moodContainer.setVisibility(View.VISIBLE);
			else 
				holder.moodContainer.setVisibility(View.GONE); 
		} else	{
			holder.moodContainer.setVisibility(View.GONE);
		}
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
			// onItemClick needs to be moved to the vote item adapter, as it shouldn't have this
//			holder.photoRoll.setOnItemClickListener(new OnItemClickListener() {
//				
//				@Override
//				public void onItemClick(AdapterView<?> parent, View v,
//						int position, long id) {
//					Intent intent = new Intent(context, EventChoicesActivity.class);
//					intent.putExtra("vote_items", new ArrayList<VoteItem>(item.getVoteItem()));
//					context.startActivity(intent);
//				}
//			});
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
		
		// Participate
		if (item.getCategory() == 2 && item.getDotchiType() == 0 && item.getIsSendJoin())	{
			if (item.getIsJoin() == 0)	{
				holder.participateLayout.setBackgroundResource(R.drawable.join_button_default);
				holder.participateCount.setVisibility(View.INVISIBLE);
			} else if (item.getIsJoin() == 1)	{
				holder.participateLayout.setBackgroundResource(R.drawable.join_button_1);
				holder.participateCount.setVisibility(View.VISIBLE);
			} else	{
				holder.participateLayout.setBackgroundResource(R.drawable.join_button_2);
				holder.participateCount.setVisibility(View.VISIBLE);
			}
			manageParticipateLayout(context, item, holder);
		} else	{
			// Needs to be gone so we can push the view over.
			holder.participateLayout.setVisibility(View.GONE);
		}
		// Set colour of hearts and number of mood comments.
		if (item.getMoodCount() == 0)	{
			holder.moodCount.setVisibility(View.INVISIBLE);
			holder.moodLayout.setBackgroundResource(R.drawable.heart_default);
			holder.moodLayout.setClickable(true);
			addMoodClick(context, isInComment, item, holder);
		} else	{
			holder.moodCount.setVisibility(View.VISIBLE);
			if (item.getIsMood())	{
				holder.moodLayout.setBackgroundResource(R.drawable.heart_count_liked);
				holder.moodLayout.setClickable(false);
			}
			else	{
				holder.moodLayout.setBackgroundResource(R.drawable.heart_count);
				holder.moodLayout.setClickable(true);
				addMoodClick(context, isInComment, item, holder);
			}
			holder.moodCount.setText(String.valueOf(item.getMoodCount()));
		}
		
		if (item.getMsgCount() == 0)	{
			holder.commentCount.setVisibility(View.INVISIBLE);
			holder.commentLayout.setBackgroundResource(R.drawable.comment_background);
		} else	{
			holder.commentCount.setVisibility(View.VISIBLE);
			holder.commentLayout.setBackgroundResource(R.drawable.comment_populated);
			holder.commentCount.setText(String.valueOf(item.getMsgCount()));
		}

		if (!isInComment)	{
			holder.commentLayout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, CommentActivity.class);
					intent.putExtra("item", item);
					SharedPreferences preferences = context.getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
					String dotchiId = preferences.getString("DOTCHI_ID", "0");
					intent.putExtra("dotchi_id", dotchiId);
					// TODO: work on activity animation
					Activity a = (Activity) context;
					a.startActivityForResult(intent, CommentActivity.COMMENT_ACTIVITY_REQ_CODE);
				}
			});
		}
		manageVoteLayout(context, item, holder);
		
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
	
	/**
	 * This function is called when a mood button is pressed, and the success status is returned.
	 * This should be a static handler, but a little more specific
	 * @param context
	 * @param isInComment
	 * @param item
	 * @param holder
	 */
	public static void manageMoodLayout(boolean isInComment, BaseFeedData item, ViewHolder holder)	{
		// Three things we need to do; update visibility of container, update count, update background, and update clickability if needed
		item.setMoodCount(item.getMoodCount() +1);
		item.setIsMood(true);
		if (isInComment)	{
			holder.moodContainer.setVisibility(View.VISIBLE);
		} else	{
			holder.moodContainer.setVisibility(View.GONE);
		}
		holder.moodCount.setText(String.valueOf(item.getMoodCount()));
		// Everything below is inclusive of both Comment and MainActivity
		// set Mood layout background; get all possibilities
		if (item.getMoodCount() == 1)	{
			// Being in here means we just went from 0 to 1; change the moodLayout background and visibility of the textView
			holder.moodCount.setVisibility(View.VISIBLE);
		} 
		holder.moodCount.setClickable(false);
		holder.moodLayout.setBackgroundResource(R.drawable.heart_count_liked);
	}
	
	public static void manageCommentLayout(boolean isInComment, BaseFeedData item, ViewHolder holder)	{
		item.setMsgCount(item.getMsgCount()+1);
		holder.commentCount.setText(String.valueOf(item.getMsgCount()));
		if (item.getMsgCount() == 1)	{
			holder.commentLayout.setBackgroundResource(R.drawable.comment_populated);
		}
	}
	
	/**
	 * TODO: set voteCount
	 * @param context
	 * @param item
	 * @param holder
	 */
	public static void manageVoteLayout(final Context context, final BaseFeedData item, ViewHolder holder)	{
		// Check item isPlay, a
		final String rootUrl = context.getResources().getString(R.string.api_test_root_url);
		final QuickAction voteQuickAction = new QuickAction(context, QuickAction.VERTICAL);
		ActionItem vaItem1 = new ActionItem(1,context.getResources().getDrawable(R.drawable.voteitem1));
		ActionItem vaItem2 = new ActionItem(2,context.getResources().getDrawable(R.drawable.voteitem2));
		voteQuickAction.addActionItem(vaItem1);
		if (!item.getIsPlay())	{
			// Default settings
			holder.voteLayout.setBackgroundResource(R.drawable.vote_button);
			holder.voteCount.setVisibility(View.VISIBLE);
			voteQuickAction.addActionItem(vaItem2);
		} else	{
			holder.voteLayout.setBackgroundResource(R.drawable.vote_button_1);
		}
		voteQuickAction.setOnActionItemClickListener(new OnActionItemClickListener() {

			@Override
			public void onItemClick(QuickAction source, int pos, int actionId) {
				if (pos == 0)	{
					new PostUrlTask()	{
						@Override
						protected void onPostExecute(String result) {
							result = processResult(result);
							Intent intent = new Intent(context, ShowListActivity.class);
							intent.putExtra("json", result);
							context.startActivity(intent);
						}
					}.execute(rootUrl + "/game/get_game_play_users", "game_id", String.valueOf(item.getGameId()));
	
				}
				else if (pos == 1)	{
					Intent intent = new Intent(context, GameActivity.class);
					// put extras
					intent.putExtra("game_id", item.getGameId());
					intent.putExtra("game_title", item.getGameTitle());
					intent.putExtra("dotchi_time", item.getDotchiTime());
					intent.putExtra("is_personal", item.getIsPersonal());
					intent.putExtra("is_secret", item.getIsSecret());
					intent.putExtra("is_official", false);
					// TODO: need to figure out how to update item with isPlay, only if the game has been successfully played. 
					((Activity)context).startActivity(intent);
				}
			}
		});
		holder.voteLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				voteQuickAction.show(v);
			}
		});
	}
	
	public static void manageParticipateLayout(final Context context, final BaseFeedData item, final ViewHolder holder)	{

		final String rootUrl = context.getResources().getString(R.string.api_test_root_url);
		final String dotchiId = ((Activity)context).getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE).getString("DOTCHI_ID", "0");
		final PostUrlTask participateTask = new PostUrlTask()	{
			@Override
			protected void onPostExecute(String result) {
			}
		};
		// Set actions for participate
		final QuickAction participateQuickAction = new QuickAction(context, QuickAction.VERTICAL);
		ActionItem paItem1 = new ActionItem(1, context.getResources().getDrawable(R.drawable.paitem1));
		ActionItem paItem2 = new ActionItem(1, context.getResources().getDrawable(R.drawable.paitem2));
		ActionItem paItem3 = new ActionItem(1, context.getResources().getDrawable(R.drawable.paitem3));
		participateQuickAction.addActionItem(paItem1);
		participateQuickAction.addActionItem(paItem2);
		// TODO: determine if we need to add this
		participateQuickAction.addActionItem(paItem3);
		participateQuickAction.setOnActionItemClickListener(new OnActionItemClickListener() {
			
			@Override
			public void onItemClick(QuickAction source, int pos, int actionId) {
				if (pos == 0)	{
					// Send PostURL, and then when it returns start the other activity
					new PostUrlTask()	{
						@Override
						protected void onPostExecute(String result) {
							result = processResult(result);
							Intent intent = new Intent(context, ShowListActivity.class);
							intent.putExtra("json", result);
							context.startActivity(intent);
						}
					}.execute(rootUrl + "/game/get_game_join_users", "game_id", String.valueOf(item.getGameId()));
				}
				else if (pos == 1)	{
					// Participate;
					holder.participateLayout.setBackgroundResource(R.drawable.join_button_2);
					// send post url task to say that you're participating
					participateTask.execute(rootUrl + "/game/decide_game_user_join", "dotchi_id", dotchiId, "game_id", String.valueOf(item.getGameId()),
							"is_join", "2");
				} else	{
					// Don't participate
					holder.participateLayout.setBackgroundResource(R.drawable.join_button_1);
					participateTask.execute(rootUrl + "/game/decide_game_user_join", "dotchi_id", dotchiId, "game_id", String.valueOf(item.getGameId()),
							"is_join", "1");
				}
			}
		});
		holder.participateLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				participateQuickAction.show(v);
			}
		});
	}
	
	static void addMoodClick(final Context context, final boolean isInComment, final BaseFeedData item, final ViewHolder holder )	{
		final String rootUrl = context.getResources().getString(R.string.api_test_root_url);
		final String dotchiId = context.getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE).getString("DOTCHI_ID", "");
		final QuickAction moodQuickAction = new QuickAction(context, QuickAction.HORIZONTAL);
		ActionItem item1 = new ActionItem(1, context.getResources().getDrawable(R.drawable.mood_1));
		ActionItem item2 = new ActionItem(2, context.getResources().getDrawable(R.drawable.mood_2));
		ActionItem item3 = new ActionItem(3, context.getResources().getDrawable(R.drawable.mood_3));
		ActionItem item4 = new ActionItem(4, context.getResources().getDrawable(R.drawable.mood_4));
		
		moodQuickAction.addActionItem(item1);
		moodQuickAction.addActionItem(item2);
		moodQuickAction.addActionItem(item3);
		moodQuickAction.addActionItem(item4);
		moodQuickAction.setOnActionItemClickListener(new OnActionItemClickListener() {
			
			@Override
			public void onItemClick(QuickAction source, int pos, int actionId) {
				// Send a post request to the API saying that we have moods inserted.
				final ActionItem aItem = source.getActionItem(pos);
				new PostUrlTask(){
					@Override
					protected void onPostExecute(String result) {
						result = processResult(result);
						// Detect if we're in a comment. If we are, then proceed. Otherwise, we can just leave it.
						if (!isInComment)	{
							// Display it
							MoodAdapter moodAdapter = (MoodAdapter)holder.moodListView.getAdapter();
							MoodItem moodItem = new MoodItem(item.getHeadImage(), item.getUserName(), aItem.getActionId());
							if (moodAdapter == null){
								List<MoodItem> data = new ArrayList<MoodItem>();
								data.add(moodItem);
								item.setIsMood(true);
								moodAdapter = new MoodAdapter(context, R.layout.mood_layout, data);
								holder.moodListView.setAdapter(moodAdapter);
							} else	{
								// Add own profile picture, own user name, and mood type id
								moodAdapter.add(moodItem);
								item.setIsMood(true);
								moodAdapter.notifyDataSetChanged();
							}
						}
						manageMoodLayout(isInComment, item, holder);
					}
				}.execute(rootUrl + "/mood/insert_user_mood", "game_id", String.valueOf(item.getGameId()), 
						"dotchi_id", dotchiId, "mood_type_id", String.valueOf(aItem.getActionId()));
			}
		});
		
		holder.moodLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				moodQuickAction.show(v);
			}
		});
		
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
		RelativeLayout moodLayout;
		TextView moodCount;
		LinearLayout moodContainer;
		RelativeLayout commentLayout;
		TextView commentCount;
		RelativeLayout voteLayout;
		TextView voteCount;
		RelativeLayout participateLayout;
		TextView participateCount;
		Spinner settingsLayout;
		
	}

}
