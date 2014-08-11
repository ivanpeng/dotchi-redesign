package com.dotchi1;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.devsmart.android.ui.HorizontalListView;
import com.dotchi1.backend.NewFeedAdapter;
import com.dotchi1.backend.NewFeedAdapter.ViewHolder;
import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.backend.ViewUtils;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.BaseFeedData;
import com.dotchi1.model.CommentItem;
import com.dotchi1.model.MoodItem;
import com.dotchi1.view.RoundedImageView;

public class CommentActivity extends Activity {

	public static final String TAG = "CommentActivity";
	public static final int COMMENT_ACTIVITY_REQ_CODE = 101;
	private static final int COMMENT_ACTIVITY_IMAGE_SIZE = 50;

	LiteImageLoader imageLoader;
	ListView commentList;
	CommentAdapter adapter;
	HorizontalListView moodListView;
	
	String userName;
	String headImage;
	boolean isCommentAdded = false;
	
	public void setupUI(View view) {
	    //Set up touch listener for non-text box views to hide keyboard.
	    if(!(view instanceof EditText)) {
	        view.setOnTouchListener(new OnTouchListener() {
	            public boolean onTouch(View v, MotionEvent event) {
	                ViewUtils.hideSoftKeyboard(CommentActivity.this);
	                return false;
	            }
	        });
	    }
	    //If a layout container, iterate over children and seed recursion.
	    if (view instanceof ViewGroup) {
	        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
	            View innerView = ((ViewGroup) view).getChildAt(i);
	            setupUI(innerView);
	        }
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.activity_comment);
		//setupUI(findViewById(R.id.container));
		
		imageLoader = new LiteImageLoader(this);
		final String rootUrl = getResources().getString(R.string.api_test_root_url);
		Intent intent = getIntent();
		final BaseFeedData item = (BaseFeedData)intent.getSerializableExtra("item");
		final String dotchiId = intent.getStringExtra("dotchi_id");
		// Before we proceed with inflating stuff, we need to get a task;
		//TODO: Migrate this to Login/MainActivity, and then store in DB
		new PostUrlTask(){
			protected void onPostExecute(String result) {
				result = processResult(result);
				try {
					JSONObject jo = new JSONObject(result).getJSONObject("data");
					userName = jo.getString("user_name");
					headImage = jo.getString("head_image");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.execute(rootUrl + "/users/user_info", "dotchi_id", dotchiId);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.comment_header, null, false);
		final ViewHolder holder = NewFeedAdapter.initHolder(view);
		NewFeedAdapter.populateView(view, this, holder, item, true);
		moodListView = (HorizontalListView)view.findViewById(R.id.mood_list);
		
		// Now call getMoodTask for each feedData
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
			// Execute on Executor
			new GetMoodTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rootUrl + "/mood/get_mood", "game_id", String.valueOf(item.getGameId()));
		} else	{
			new GetMoodTask().execute(rootUrl + "/mood/get_mood", "game_id", String.valueOf(item.getGameId()));
		}
		// Populate ListView
		commentList = (ListView) findViewById(R.id.comment_list);
		

		new PostUrlTask(){

			@Override
			protected void onPostExecute(String result) {
				result = processResult(result);
				ArrayList<CommentItem> objects = processJson(result); 
				if (objects != null && objects.size() > 0)	{
					adapter = new CommentAdapter(CommentActivity.this, R.layout.comment_item, objects, false);
				} else	{
					List<CommentItem> pholder = new ArrayList<CommentItem>();
					pholder.add(new CommentItem());
					adapter = new CommentAdapter(CommentActivity.this, R.layout.comment_item, pholder, true);
				}					
				commentList.setAdapter(adapter);
				commentList.addHeaderView(view);
			}
			
		}.execute(rootUrl + "/comment/get_activity_comment", "game_id", String.valueOf(item.getGameId()));
		// wire up edittext now
		final EditText commentText = (EditText) findViewById(R.id.comment_text_box);
		commentText.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus)	{
					// If it is focused; if so, then scroll to the bottom of the list. Otherwise just leave it
					if (adapter != null)
						commentList.smoothScrollToPosition(adapter.getCount());
				}
			}
		});
		// Search button to execute get text from editText and then to call API
		ImageView submitButton = (ImageView) findViewById(R.id.submit_comment);
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String comment = commentText.getText().toString();
				if (comment != null && comment.length() > 0)	{
					Date date = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm");
					final CommentItem commentItem = new CommentItem(item.getHeadImage(), item.getUserName(), sdf.format(date), comment);
					// Not null! Call submit comment API
					new PostUrlTask(){

						@Override
						protected void onPostExecute(String result) {
							result = processResult(result);
							// All we have to check if it is successful. If it is, add it to the listview view.
							// We don't need to worry about synchronicity; dynamic insertions will be a problem but lets' not worry about that yet
							JSONObject obj;
							try {
								obj = new JSONObject(result);
								String status = obj.getJSONObject("data").getString("status");
								if (status.equals("success"))	{
									// update list
									isCommentAdded = true;
									NewFeedAdapter.manageCommentLayout(true, item, holder);
									if (adapter.isPlaceholder){
										// reinitialize adapter with comment as first, and don't make it
										List<CommentItem> ll = new ArrayList<CommentItem>();
										ll.add(commentItem);
										adapter = new CommentAdapter(CommentActivity.this, R.layout.comment_item, ll, false);
										commentList.setAdapter(adapter);
									} else	{
										// just add to the list with adapter
										//TODO: confer with API the order of how they are presented; newest comments on top or bottom?
										adapter.add(commentItem);
										// Change textView!
										TextView commentCount = (TextView) view.findViewById(R.id.num_comments);
										commentCount.setText(String.valueOf(adapter.getCount()));
										adapter.notifyDataSetChanged();
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}.execute(rootUrl + "/comment/insert_comment", "game_id", String.valueOf(item.getGameId()),
								"dotchi_id", dotchiId, "comment", comment);
					commentText.setText("");
				}
				
			}
		}); // That's some nested shit....
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
	public void onBackPressed() {
		Intent returnIntent = new Intent();
		returnIntent.putExtra("comment_count", adapter.getCount());
		returnIntent.putExtra("mood_count", 0);
		setResult(RESULT_OK, returnIntent);
		super.onBackPressed();
	}

	protected ArrayList<CommentItem> processJson(String jsonString)	{
		ArrayList<CommentItem> items = new ArrayList<CommentItem>();
		JSONArray ja;
		try {
			ja = new JSONObject(jsonString).getJSONArray("data");
			ObjectMapper mapper = new ObjectMapper();
			mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
			List<CommentItem> objects = mapper.readValue(ja.toString(), mapper.getTypeFactory().constructCollectionType(List.class, CommentItem.class));
			items = new ArrayList<CommentItem>(objects);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return items;
	}
	
	class CommentAdapter extends ArrayAdapter<CommentItem>	{

		private Context context;
		private ArrayList<CommentItem> objects;
		private boolean isPlaceholder;
		
		public CommentAdapter(Context context, int textViewResourceId,
				List<CommentItem> objects, boolean isPlaceholder) {
			super(context, textViewResourceId, objects);
			this.context = context;
			this.isPlaceholder = isPlaceholder;
			this.objects = new ArrayList<CommentItem>(objects);
			
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// The structure of listview might not allow this, so we might just have to send a boolean in the constructor.
			if (!isPlaceholder)	{
				CommentItem item = objects.get(position);
				View view = convertView;
				if (view == null)	{
					LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					view = inflater.inflate(R.layout.comment_item, parent, false);
				}
				RoundedImageView profileHead = (RoundedImageView) view.findViewById(R.id.comment_head_view);
				imageLoader.DisplayImage(item.getHeadImage(), R.drawable.default_profile_pic, profileHead, COMMENT_ACTIVITY_IMAGE_SIZE);
				
				TextView username = (TextView) view.findViewById(R.id.username_text);
				username.setText(item.getUserName());
				TextView timeLeft = (TextView) view.findViewById(R.id.time_text);
				timeLeft.setText(item.getCreateTime());
				TextView comment = (TextView) view.findViewById(R.id.comment_text);
				comment.setText(item.getComment());
				return view;
			}
			else	{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = inflater.inflate(R.layout.blank_placeholder, parent, false);
				return view;
			}
		}

		@Override
		public int getCount() {
			return objects.size();
		}

		@Override
		public void add(CommentItem object) {
			objects.add(object);
			super.add(object);
		}

		@Override
		public void remove(CommentItem object) {
			int index = objects.indexOf(object);
			if (index != -1)
				objects.remove(index);
			super.remove(object);
			//Log.d(TAG, "Values: " + values.toString());
			Log.d(TAG, "Removed " + object.toString() + " from list");
		}

		public boolean isPlaceholder() {
			return isPlaceholder;
		}

		public void setPlaceholder(boolean isPlaceholder) {
			this.isPlaceholder = isPlaceholder;
		}
		
	}
	
	class GetMoodTask extends PostUrlTask	{


		@Override
		protected void onPostExecute(String result) {
			result = processResult(result);
			try {
				JSONArray ja = new JSONObject(result).getJSONArray("data");
				List<MoodItem> data = new ArrayList<MoodItem>();
				for (int i = 0; i < ja.length(); i++){
					JSONObject jo = ja.getJSONObject(i);
					// Add to 
					MoodItem mItem = new MoodItem(jo.getString("head_image"), jo.getString("user_name"), jo.getInt("mood_type_id"));
					data.add(mItem);
				}
				if (data.size() > 0)	{
					MoodAdapter moodAdapter = new MoodAdapter(CommentActivity.this, R.layout.mood_layout, data);
					moodListView.setAdapter(moodAdapter);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	
	
	public static class MoodAdapter extends ArrayAdapter<MoodItem>	{

		private Context context;
		private ArrayList<MoodItem> objects;
		private LiteImageLoader imageLoader;
		private static final int[] moodArr = {R.drawable.mood_1, R.drawable.mood_2, R.drawable.mood_3, R.drawable.mood_4};

		
		public MoodAdapter(Context context, int textViewResourceId,	List<MoodItem> objects) {
			super(context, textViewResourceId, objects);
			this.context = context;
			this.objects = new ArrayList<MoodItem>(objects);
			this.imageLoader = new LiteImageLoader(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			MoodItem mItem = objects.get(position);
			if (view == null)	{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.mood_layout, null);
			}
			RoundedImageView head = (RoundedImageView) view.findViewById(R.id.head_picture);
			imageLoader.DisplayImage(mItem.getHeadImage(), R.drawable.default_profile_pic, head, COMMENT_ACTIVITY_IMAGE_SIZE);
			ImageView mood = (ImageView) view.findViewById(R.id.mood_picture);
			mood.setImageResource(moodArr[mItem.getMoodTypeId()-1]);
			return view;
		}

		@Override
		public void add(MoodItem object) {
			objects.add(object);
			super.add(object);
		}

		@Override
		public int getCount() {
			return objects.size();
		}
		
	}

}
