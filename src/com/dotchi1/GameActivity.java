package com.dotchi1;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dotchi1.backend.MainFeedAdapter;
import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.backend.json.JsonDataWrapper;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.GameCardItem;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;

public class GameActivity extends ActionBarActivity {

	public static final String TAG = "GameActivity";
	public static final String GAME_ACTIVITY_KEY = "GAME_ACTIVITY";
	public static final String DOTCHI_ID_KEY="DOTCHI_ID";
	private String rootUrl;
	
	int dotchiId;
	int gameId; 
	LiteImageLoader imageLoader;
	
	
	TextView numYesLeftView;
	SwipeListView gameListView;
	GamePlayAdapter adapter;
	
	ArrayList<GameCardItem> gameCards;
	ArrayList<GameCardItem> eventCards;
	ArrayList<GameItemResult> gameResults;
	boolean isEventLeft = true;
	boolean isGameLeft = true;
	
	int numYesLeft;
	int maxNumYes = numYesLeft;
	String gameTitle;
	String gameDate;
	boolean isSecret;
	boolean isPersonal;
	boolean isOfficial;
	ProgressDialog uploadDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences preferences = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		dotchiId = Integer.parseInt(preferences.getString(DOTCHI_ID_KEY,"0"));
		imageLoader = new LiteImageLoader(this);
		rootUrl = getResources().getString(R.string.api_test_root_url);
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		setContentView(R.layout.activity_game);
		View actionbar = LayoutInflater.from(this).inflate(R.layout.menu_game_play, null);
		boolean isDataInBundle = intent.getBooleanExtra("is_bundle", false);
		if (isDataInBundle){
			Bundle bundle = intent.getExtras();
			numYesLeft = bundle.getInt("vote_limit");
			gameTitle = bundle.getString("game_title");
			gameDate = bundle.getString("dotchi_time");
			isSecret = bundle.getBoolean("is_secret");
			isPersonal = bundle.getBoolean("is_personal");
			isOfficial = bundle.getBoolean("is_official");
		} else	{
			numYesLeft = intent.getIntExtra("vote_limit", -1);
			gameTitle = intent.getStringExtra("game_title");
			gameDate = intent.getStringExtra("dotchi_time");
			isSecret = intent.getBooleanExtra("is_secret", false);
			isPersonal = intent.getBooleanExtra("is_personal", false);
			isOfficial = intent.getBooleanExtra("is_official", false);
		}
		TextView title = (TextView) actionbar.findViewById(R.id.game_play_title);
		title.setText(gameTitle);
		TextView dateView = (TextView) actionbar.findViewById(R.id.game_menu_date);
		if (gameDate == null || gameDate.equals("") || gameDate.equals("0000-00-00 00:00:00"))
			dateView.setText("");
		else
			dateView.setText(gameDate);
		numYesLeftView = (TextView) actionbar.findViewById(R.id.num_yes_remaining);
		if (numYesLeft == -1 || numYesLeft == 0)	
			numYesLeftView.setVisibility(View.INVISIBLE);
		else
			numYesLeftView.setText("還剩" + String.valueOf(numYesLeft)+ "票Yes可以選擇");
		
		ImageView isPersonalImage = (ImageView) actionbar.findViewById(R.id.game_menu_is_personal);
		if (isPersonal)	
			isPersonalImage.setImageResource(R.drawable.is_personal_positive_icon);
		else
			isPersonalImage.setImageResource(R.drawable.is_personal_negative_icon);
		ImageView isPrivateImage = (ImageView) actionbar.findViewById(R.id.game_menu_is_secret);
		if (isSecret)
			isPrivateImage.setImageResource(R.drawable.is_secret_negative_icon);
		else
			isPrivateImage.setImageResource(R.drawable.is_secret_positive_icon);

		getSupportActionBar().setCustomView(actionbar);
	    getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		
		boolean isFirstTime = preferences.getString(GAME_ACTIVITY_KEY, "0").equals("0") ? true: false;
		if (isFirstTime)	{
			//showHelpDialog();
			Editor editor = preferences.edit();
			editor.putString(GAME_ACTIVITY_KEY, "1");
			editor.commit();
		}
		// Set list adapter for this list; how do we get the data?
		// Either host directly takes the set, or we have to call the API
		gameListView = (SwipeListView) findViewById(R.id.game_choices_list);
		gameListView.setSwipeListViewListener(new BaseSwipeListViewListener(){
		    @Override
		    public void onOpened(int position, boolean toRight) {
		    	Log.d("swipe", "OnOpened called for position " + position + " with toRight " + toRight);
		    	// Assemble data for pushing out here
		    	GameCardItem gameCard = adapter.getItem(position);
		    	// Subtract from numYesLeft, and determine if that's it.
		    	String resultStr = toRight? "yes":"no";
		    	gameResults.add(new GameItemResult(gameCard.getGameItemId(), resultStr));
		    	
		    	gameListView.dismiss(position);
		    	if (toRight)	{
			    	if (numYesLeft != -1)	{
						numYesLeft--;
						// Change textview
						numYesLeftView.setText("還剩" + String.valueOf(numYesLeft)+ "票Yes可以選擇");
					}
			    	if (numYesLeft == 0)	{
			    		if (isEventLeft) {
			    			adapter = new GamePlayAdapter(GameActivity.this, eventCards);
			    			gameListView.setAdapter(adapter);
			    			isEventLeft = false;
			    		} else	{
				    		Log.d(TAG, "Vote limit reached! Setting rest of results to 0, and sending results.");
				    		// We're finished! Add all the rest of the game results
				    		for (int i = 0; i < adapter.getCount(); i++){
				    			GameCardItem item = adapter.getItem(i);
				    			gameResults.add(new GameItemResult(item.getGameItemId(), "no"));
				    		}
				    		Log.d(TAG, "GameResults length:" + gameResults.size());
				    		sendResults();
			    		}
			    	}
		    	}
		    }
		    
			@Override
            public void onStartOpen(int position, int action, boolean right) {
				Log.d("swipe", String.format("onStartOpen %d - action %d", position, action));
			}
			@Override
			public void onDismiss(int[] reverseSortedPositions)	{
				for (int position: reverseSortedPositions)	{
					adapter.remove(adapter.getItem(position));
					// Here, include the direction as well
					if (adapter.getCount() == 0)	{
						// List is empty! Send data
						if (isEventLeft){
							isEventLeft = false;
							adapter = new GamePlayAdapter(GameActivity.this, eventCards);
							gameListView.setAdapter(adapter);
						} else	{
							Log.d(TAG, gameResults.toString());
							sendResults();
						}
					}
				}
				adapter.notifyDataSetChanged();
				gameListView.closeOpenedItems();
				
			}
			
			@Override
			public int onChangeSwipeMode(int position){
				return SwipeListView.SWIPE_MODE_BOTH;
			}
		});
		// This part is different from game creator and user; we won't get this if we're someone invited to this game.
		//gameCards = (ArrayList<GameCardItem>)getIntent().getSerializableExtra("game_cards");
		// We need gameItemId, so we are going to skip this every time.
		// If these are null, we'll grab the game id, and then call API
		gameId = getIntent().getIntExtra("game_id", -1);
		String baseUrl = rootUrl+ "/game/get_game_item";
		final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading","");
		progressDialog.setCancelable(true);
		// After all PostURL Tasks are completed, set the adapter
		new PostUrlTask(){

			@Override
			protected void onPostExecute(String result) {
				result = processResult(result);
				Log.d(TAG, "Completed a call of get_game_item: " + result);
				JSONObject jo;
				try {
					jo = new JSONObject(result);
					JSONArray ja = jo.getJSONArray("data");
					ObjectMapper mapper = new ObjectMapper();
					mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
					List<GameCardItem> items = mapper.readValue(ja.toString(), mapper.getTypeFactory().constructCollectionType(List.class, GameCardItem.class));
					// Now separate items by isDate and not
					gameCards = new ArrayList<GameCardItem>();
					eventCards = new ArrayList<GameCardItem>();
					gameResults = new ArrayList<GameItemResult>(items.size());
					for (GameCardItem item: items)	{
						if (item.getIsDate())
							eventCards.add(item);
						else
							gameCards.add(item);
					}
					//numYesLeftView.setText(String.valueOf(gameCards.size()));
					if (gameCards.size() == 0)
						isGameLeft = false;
					if (eventCards.size() == 0)	
						isEventLeft = false;
					if (isGameLeft)	{
						adapter = new GamePlayAdapter(GameActivity.this, gameCards);
						gameListView.setAdapter(adapter);
					} else if (isEventLeft)	{
						adapter = new GamePlayAdapter(GameActivity.this, eventCards);
						gameListView.setAdapter(adapter);
					}
					progressDialog.dismiss();
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
		}.execute(baseUrl, "game_id", String.valueOf(gameId));
	}

	@Override
	public void onBackPressed() {
		// We're going to return to MainActivity, but sometimes we might have activity stacks created already. return with clearing top stack
		Intent intent = new Intent(this, NewMainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		//super.onBackPressed();
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
	

	protected void sendResults()	{
		Log.d(TAG, "List is empty. Sending data out now");
		uploadDialog = ProgressDialog.show(this, "Loading","");
		uploadDialog.setCancelable(true);
		String insertUrl = rootUrl + "/game/insert_user_game_item";
		JSONArray arr = new JSONArray();
		for (GameItemResult item : gameResults)	{
			JSONObject jo = new JSONObject();
			try {
				jo.put("game_item_id", item.getGameItemId());
				jo.put("result", item.getResult());
				arr.put(jo);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		String itemResultStr = JsonDataWrapper.wrapData(arr);
		new InsertGameResultTask().execute(insertUrl, "dotchi_id", String.valueOf(dotchiId), "game_id", String.valueOf(gameId),
				"item_result", itemResultStr);
	}
	
	class GamePlayAdapter extends ArrayAdapter<GameCardItem>	{
		
		private Context context;
		private ArrayList<GameCardItem> objects;
		private ArrayList<byte[]> imageData;
		
		public GamePlayAdapter(Context context, List<GameCardItem> objects)	{
			super(context, android.R.layout.simple_list_item_1, objects);
			this.context = context;
			this.objects = new ArrayList<GameCardItem>(objects);
			Log.d(TAG, "Size of data adapter" + String.valueOf(objects.size()));
			//this.imageData = imageData;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			GameCardItem item = objects.get(position);
			Log.d(TAG, item.toString());
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = null; // If we want to recycle views, we use convertView here;
			if (!item.getIsDate())	{
				view = inflater.inflate(R.layout.dotchi_package_view_item, null);

				ImageView itemImage = (ImageView) view.findViewById(R.id.photo_image);
				TextView itemTitle = (TextView) view.findViewById(R.id.dotchi_package_title);
				TextView itemContent = (TextView) view.findViewById(R.id.dotchi_package_description);
				//LinearLayout backContainer = (LinearLayout) view.findViewById(R.id.swipe_right_container);
				
				imageLoader.DisplayImage(item.getItemImage(), R.drawable.default_profile_pic, itemImage, 250);
				itemTitle.setText(item.getItemTitle());
				itemContent.setText(item.getItemContent());
				ImageView checkmark = (ImageView) view.findViewById(R.id.checkmark_if_selected);
				checkmark.setVisibility(View.INVISIBLE);
			} else	{
				view = MainFeedAdapter.makeEventView(GameActivity.this, item);
				
			}
			((SwipeListView)parent).recycle(view, position);

			
			return view;
		}

		@Override
		public void add(GameCardItem object) {
			super.add(object);
			objects.add(object);
		}

		@Override
		public void remove(GameCardItem object) {
			super.remove(object);
			int index = objects.indexOf(object);
			objects.remove(index);
			//imageData.remove(index);
			Log.d(TAG, "Overridden remove card item completed. " + object.toString() + " removed");
			Log.d(TAG, "Size of list: " + objects.size());
		}
		
		@Override
		public int getCount() {
			return objects.size();
		}

	}
	
	class GameResultAdapter	extends ArrayAdapter<JSONObject> {

		private Context context;
		private ArrayList<JSONObject> objects;
		
		public GameResultAdapter(Context context, List<JSONObject> objects)	{
			super(context, R.layout.game_choice_item, objects);
			this.context = context;
			this.objects = new ArrayList<JSONObject>(objects);
		}
		@Override
		public View getView(int position, View view, ViewGroup parent){
			JSONObject item = objects.get(position);
			if (view == null)	{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.game_choice_item, null);
			}
			TextView title = (TextView) view.findViewById(R.id.game_play_choice_title);
			TextView description = (TextView) view.findViewById(R.id.game_play_choice_description);
			ImageView image = (ImageView) view.findViewById(R.id.game_play_picture);
			TextView count = (TextView) view.findViewById(R.id.num_said_yes);
			try	{
				title.setText(item.getString("item_title"));
				description.setText(item.getString("item_content"));
				imageLoader.DisplayImage(item.getString("item_image"), R.drawable.default_profile_pic, image, 250);
				//Figure out count of user_list
				JSONArray userList = item.getJSONArray("user_list");
				int length = userList.length();
				if (length > 0)
					count.setText(String.valueOf(length));
				else
					count.setVisibility(View.INVISIBLE);
			} catch (JSONException jex)	{
				jex.printStackTrace();
			}
			return view;
		}
	}
	
	class InsertGameResultTask extends PostUrlTask	{

		@Override
		protected void onPostExecute(String result) {
			result = processResult(result);
			uploadDialog.dismiss();
			try {
				JSONObject jsonObj = new JSONObject(result).getJSONObject("data");
				String status = jsonObj.getString("status");
				if (status.equals("success"))	{
					Log.d(TAG, "Inserted game items successfully");
					Intent intent = new Intent(GameActivity.this, NewMainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					//finish();
				}
				else
					Log.d(TAG, "Something went wrong!");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	static class ViewHolder	{
		ImageView itemImage;
		TextView itemTitle;
		TextView itemContent;
		LinearLayout backContainer;
	}
	
	/**
	 * An efficient wrapper to store data for GameResult
	 * @author Ivan
	 *
	 */
	class GameItemResult {
		private int gameItemId;
		private String result;
		
		public GameItemResult()	{
		}
		
		public GameItemResult(int gameItemId, String result) {
			super();
			this.gameItemId = gameItemId;
			this.result = result;
		}
		public int getGameItemId() {
			return gameItemId;
		}
		public void setGameItemId(int gameItemId) {
			this.gameItemId = gameItemId;
		}
		public String getResult() {
			return result;
		}
		public void setResult(String result) {
			this.result = result;
		}

		@Override
		public String toString() {
			return "GameItemResult [gameItemId=" + gameItemId + ", result="
					+ result + "]";
		}
		
	}

}
