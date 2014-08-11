package com.dotchi1;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.dotchi1.backend.ClearSearchListener;
import com.dotchi1.backend.DialogFriendsAdapter;
import com.dotchi1.backend.PassUpData;
import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.backend.SaveImageTask;
import com.dotchi1.backend.ToggleClickListener;
import com.dotchi1.backend.ViewUtils;
import com.dotchi1.backend.json.JsonDataWrapper;
import com.dotchi1.model.FriendPageFriendItem;
import com.dotchi1.model.GameCardItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class GameSettingActivity extends ActionBarActivity implements PassUpData {
	
	public interface PassDownData	{
		public void onNewCardCreated(GameCardItem item);
		public String getDotchiDate();
	}
	
	private PassDownData passDownData;
	
	public static final String DOTCHI_ID_KEY = "DOTCHI_ID";
	public static final String TAG = "GameSettingActivity";
	public static final String GAME_SETTING_ACTIVITY_KEY = "GAME_SETTING_ACTIVITY";

	public static final int REQUEST_CREATE_CARD = 100;
	public static final int REQUEST_FRIENDS = 1000;
	public static final int REQUEST_GAME = 2000;
	// This variable is for updating profile urls.
	private List<GameCardItem> synchronizedGameCards;
	private ArrayList<GameCardItem> gameCards;

	int dotchiId;
	String dotchiGameTitle;
	int categoryId;
	
	String dotchiGameDate;
	boolean isPrivate;
	boolean isPersonal;
	int daysToExpire;
	int maxNumYes;
	EditText titleView;
	ImageView isPrivateView;
	ImageView isPersonalView;
	TextView setExpiryView;
	TextView setMaxNumDaysView;
	NumberPicker maxNumYesPicker; 
	
	int[] selectedFriends;
	ArrayList<FriendPageFriendItem> friendData;
	

	final int[] textViewResIds = {R.id.gs_title_text,
			R.id.set_days_expiry_text, R.id.set_max_num_yes_text};
	private boolean[] areFieldsNull = new boolean[textViewResIds.length];

	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	
	public void setupUI(View view) {
	    //Set up touch listener for non-text box views to hide keyboard.
	    if(!(view instanceof EditText)) {
	        view.setOnTouchListener(new OnTouchListener() {
	            public boolean onTouch(View v, MotionEvent event) {
	                ViewUtils.hideSoftKeyboard(GameSettingActivity.this);
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
		
		SharedPreferences preferences = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		dotchiId = Integer.parseInt(preferences.getString(DOTCHI_ID_KEY,"0"));
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
					.cacheInMemory(true)
					.cacheOnDisc(true)
					//.displayer(new RoundedBitmapDisplayer(5))
					.build();
		for (int i = 0; i < areFieldsNull.length; i++)	
			areFieldsNull[i] = true;
		// Title, date, time 
		areFieldsNull[0] = false;
		
		synchronizedGameCards = Collections.synchronizedList(new ArrayList<GameCardItem>());
		setContentView(R.layout.activity_game_setting);
		setupUI(findViewById(R.id.game_setting_container));
		View actionbar = LayoutInflater.from(this).inflate(R.layout.menu_game_setting, null);
		getSupportActionBar().setCustomView(actionbar);
	    getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		
		ImageView menuBack = (ImageView) actionbar.findViewById(R.id.game_select_back);
		menuBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		ImageView createGame = (ImageView) actionbar.findViewById(R.id.game_create);
		createGame.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), CreateGameCardActivity.class);
				startActivityForResult(intent, REQUEST_CREATE_CARD);
				overridePendingTransition(R.animator.pull_up_from_bottom, 0);
			}
		});
		
		titleView = (EditText) findViewById(R.id.gs_title_text);
		titleView.addTextChangedListener(new EnableInviteFriendsWatcher(R.id.gs_title_text));
		ImageView titleTextCancel = (ImageView) findViewById(R.id.gs_cancel_button);
		titleTextCancel.setOnClickListener(new ClearSearchListener(this, R.id.gs_title_text));
		setBottomListener();
		
		// Show help dialog if first time;
		boolean isFirstTime = preferences.getString(GAME_SETTING_ACTIVITY_KEY, "0").equals("0") ? true: false;
		if (isFirstTime)	{
			showHelpDialog();
			Editor editor = preferences.edit();
			editor.putString(GAME_SETTING_ACTIVITY_KEY, "1");
			editor.commit();
		}
		
		TextView daysResponse = (TextView) findViewById(R.id.set_days_expiry_text);
		daysResponse.addTextChangedListener(new EnableInviteFriendsWatcher(R.id.set_days_expiry_text));
		
		TextView numYes = (TextView) findViewById(R.id.set_max_num_yes_text);
		numYes.addTextChangedListener(new EnableInviteFriendsWatcher(R.id.set_max_num_yes_text));
		
		categoryId = getIntent().getIntExtra("category_id", -1);
		// Add fragment
		if (categoryId != 1)	{
			// Event
			GameSettingEventFragment fragment = new GameSettingEventFragment();
			passDownData = (PassDownData) fragment;
			getSupportFragmentManager().beginTransaction()
					.add(R.id.game_setting_fragment_container, fragment).commit();
			// This should be called afterwards
			//setNumberPickerViews();
		}
		
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
	private void showHelpDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = inflater.inflate(R.layout.dialog_help_game_play, null);
		TextView title = (TextView) dialogView.findViewById(R.id.dialog_help_title);
		//TODO
		title.setText("活動三步驟");
		final AlertDialog dialog = new AlertDialog.Builder(this)
								.setView(dialogView).create();
		ImageView cancel = (ImageView)dialogView.findViewById(R.id.dialog_help_close_button);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
		
	}

	private void setBottomListener() {
		// Set the listeners for the bottom.
		isPrivateView = (ImageView) findViewById(R.id.is_private_image);
		isPrivateView.setOnClickListener(new ToggleClickListener(0));
		isPersonalView = (ImageView) findViewById(R.id.is_personal_image);
		isPersonalView.setOnClickListener(new ToggleClickListener(0));
		setExpiryView = (TextView) findViewById(R.id.set_days_expiry_text);
		setMaxNumDaysView = (TextView) findViewById(R.id.set_max_num_yes_text);
		
		ImageView inviteFriendsView = (ImageView) findViewById(R.id.invite_friends_image);
		inviteFriendsView.setEnabled(false);
		inviteFriendsView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Before we send off, set all variables
				// First is title
				dotchiGameTitle = titleView.getText().toString();
				// Set the bottom listeners
				isPrivate = !isPrivateView.isSelected();
				isPersonal = isPersonalView.isSelected();
				daysToExpire = Integer.parseInt(setExpiryView.getText().toString());
				maxNumYes = Integer.parseInt(setMaxNumDaysView.getText().toString());
				Intent i = new Intent(getApplicationContext(), FriendSelectActivity.class);
				startActivityForResult(i, REQUEST_FRIENDS);
			}
		});
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)	{
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == GameSettingActivity.REQUEST_CREATE_CARD)	{
			if (resultCode == Activity.RESULT_OK)	{
				Log.d(TAG, "Getting result from create card!");
				GameCardItem result = (GameCardItem) data.getSerializableExtra("game_card");
				Log.d(TAG, result.toString());
				passDownData.onNewCardCreated(result);
				ImageView inviteFriends = (ImageView) findViewById(R.id.invite_friends_image);
				if (checkFields())	{
					inviteFriends.setEnabled(true);
				} else
					inviteFriends.setEnabled(false);
			} else	{
				// No data; do we need to do anything?
				Log.d(TAG, "No data received from create game card activity");
			}
		}
		else if (requestCode == REQUEST_FRIENDS)	{
			//Build Dialog
			if (resultCode == RESULT_OK)	{
				// Get return data from intent
				selectedFriends = data.getIntArrayExtra("dotchi_ids");
				friendData = (ArrayList<FriendPageFriendItem>)data.getSerializableExtra("friend_items");
				StringBuilder s = new StringBuilder();
				for (int i = 0; i < selectedFriends.length; i++)	{
					if (i != 0)
						s.append(",");
					s.append(selectedFriends[i]);
				}
				Log.d(TAG, s.toString());
				Log.d(TAG, "friendData: " + friendData.toString());
				showConfirmDialog();
			} else {
				Log.d(TAG, "No data received from Request Friends");
			}
		} else if (requestCode == REQUEST_GAME)	{
			if (resultCode == RESULT_OK)	{
				// Okay! Finish from here
				Intent returnIntent = new Intent();
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		}
	}
	
	protected void showConfirmDialog()	{
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = inflater.inflate(R.layout.dialog_confirm_game_settings, null);
		dialogView.setLayoutParams(new LinearLayout.LayoutParams(313, 268));
		TextView dotchiGameDateView = (TextView) dialogView.findViewById(R.id.dotchi_game_set_datetime_text);
		dotchiGameDate = passDownData.getDotchiDate();
		Log.d(TAG, "Got dotchiDate from fragment: " +  dotchiGameDate);
		String dateStr;
		if (dotchiGameDate != null)	{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			dateStr = dotchiGameDate;
		} else
			dateStr = "";
		dotchiGameDateView.setText(dateStr);
		TextView dotchiGameTitleView = (TextView) dialogView.findViewById(R.id.dotchi_game_set_title);
		dotchiGameTitleView.setText(dotchiGameTitle);
		ImageView isPersonalImage = (ImageView) dialogView.findViewById(R.id.dotchi_is_personal_image);
		if (isPersonal)	
			isPersonalImage.setImageResource(R.drawable.dialog_personal);
		else
			isPersonalImage.setImageResource(R.drawable.dialog_not_personal);
		ImageView isPrivateImage = (ImageView) dialogView.findViewById(R.id.dotchi_is_private_image);
		Log.d(TAG, "is_secret" + isPrivate);
		if (isPrivate)
			isPrivateImage.setImageResource(R.drawable.dialog_not_public);
		else
			isPrivateImage.setImageResource(R.drawable.dialog_public);
		ImageView numYesView = (ImageView) dialogView.findViewById(R.id.dotchi_num_yes_image);
		if (maxNumYes > 0)	{
			// Only need to do stuff for maxNumYes > 0
			numYesView.setImageResource(R.drawable.num_yes);
			TextView number = (TextView) dialogView.findViewById(R.id.num_yes_number);
			number.setVisibility(View.VISIBLE);
			number.setText(String.valueOf(maxNumYes));
		}
		
		GridView friendsView = (GridView) dialogView.findViewById(R.id.dialog_friends_confirm);
		ArrayList<FriendPageFriendItem> selectedFriendData = new ArrayList<FriendPageFriendItem>();
		for (int i = 0; i < selectedFriends.length; i++)	
			for (int j = 0; j < friendData.size(); j++)
				if (selectedFriends[i] == (int)friendData.get(j).getDotchiId())	{
					selectedFriendData.add(friendData.get(j));
					break;
				}
		DialogFriendsAdapter dfAdapter = new DialogFriendsAdapter(GameSettingActivity.this, R.layout.dialog_friend_item, selectedFriendData);
		friendsView.setAdapter(dfAdapter);
		
		final AlertDialog dialog = new AlertDialog.Builder(GameSettingActivity.this).setView(dialogView).create();
		dialog.show();
		ImageButton confirm = (ImageButton) dialogView.findViewById(R.id.dialog_confirm_button);
		confirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				uploadPhotos();
				dialog.dismiss();
			}
		});
		ImageView cancel = (ImageView) dialogView.findViewById(R.id.dialog_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				onBackPressed();
			}
		});
	}
	
	protected void uploadPhotos()	{
		// Gets all list items, and converts the items to not a local url. 
		String rootUrl = getResources().getString(R.string.api_root_url) + "/pic/upload_pic";
		// First send the image
		Log.d(TAG, "Uploading files: " + gameCards.toString());
		for (int i = 0; i < gameCards.size(); i++)	{
			GameCardItem gameCard = gameCards.get(i);
			if (!gameCard.getItemImage().startsWith("http://www.plany.com.tw/"))	{
				Log.d(TAG, "Sending url " + rootUrl + " and uploading image " + gameCard.getItemImage());
				new UploadPhotoTask(getApplicationContext(), gameCard).execute(rootUrl, "user_file", gameCard.getItemImage());
			} else	{
				synchronizedGameCards.add(gameCard);
			}
		}
		// Have second async thread to listen to if that thread is done;
		final ProgressDialog progressDialog = ProgressDialog.show(GameSettingActivity.this, "Loading", "");
		progressDialog.setCancelable(true);
		new AsyncTask<Void, Void, Void>()	{
			
			static final long TIMEOUT_LENGTH = 20000;
			static final long REFRESH_INTERVAL = 100;
			
			@Override
			protected Void doInBackground(Void... params) {
				long timeWaiting = 0;
				
				while (gameCards.size() > synchronizedGameCards.size() && timeWaiting < TIMEOUT_LENGTH)	{
					try {
						Thread.sleep(REFRESH_INTERVAL);
						timeWaiting += REFRESH_INTERVAL;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// If we're here, that means either we've processed it or there's been a timeout.
				if (timeWaiting < TIMEOUT_LENGTH)	{
					Log.d(TAG, "Fully completed uploaded pictures.");
					Log.d(TAG, synchronizedGameCards.toString());
					// SEND JSON HERE WHEN COMPLETE
					sendJson();
				} else {
					Log.d(TAG, "Request timed out.");
				}
				progressDialog.dismiss();
				return null;
			}
		}.execute();
	}

	protected void sendJson() {
		String rootUrl = getResources().getString(R.string.api_root_url) + "/game/create_dotchi_game";
		String isPersonalStr = isPersonal ? "1" : "0";
		String isPrivateStr = isPrivate ? "1" : "0";
		final String dateStr;
		if (dotchiGameDate != null)
			dateStr = dotchiGameDate;
		else
			dateStr = "0000-00-00 00:00:00";
		Log.d(TAG, "Sending out a date string of " + dateStr);
		String gameCardItems;
		try {
			JSONArray arr = new JSONArray();
			for (GameCardItem item : synchronizedGameCards)	{
				//gameCardItems = JsonDataWrapper.wrapData(mapper.writeValueAsString(synchronizedGameCards));
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("item_image", item.getItemImage());
				jsonObject.put("item_title", item.getItemTitle());
				jsonObject.put("item_content", item.getItemContent());
				arr.put(jsonObject);
			}
			gameCardItems = JsonDataWrapper.wrapData(arr);
			Log.d(TAG, gameCardItems);
			// Selected friends; do a little bit of parsing for friends as well
			JSONArray ja = new JSONArray();
			for (int i = 0; i < selectedFriends.length; i++)	{
				JSONObject jo = new JSONObject();
				jo.put("dotchi_id", selectedFriends[i]);
				ja.put(jo);
			}
			Log.d(TAG, "Selected friends: " + ja.toString());
			String inviteFriendsItems = JsonDataWrapper.wrapData(ja);
			// Group now
			JSONArray groupArr = new JSONArray();
			JSONObject groupObj = new JSONObject();
			groupObj.put("data", groupArr);
			Log.d(TAG, "Group ID: " + groupObj.toString());
			new PostUrlTask(){
	
				@Override
				protected void onPostExecute(String result) {
					result = processResult(result);
					// The results are here for display in log only; make something of it!
					JSONObject jsonObject;
					try {
						jsonObject = new JSONObject(result).getJSONObject("data");
						String gameId = jsonObject.getString("game_id");
						
						Intent intent = new Intent(GameSettingActivity.this, GameActivity.class);
						intent.putExtra("game_title", dotchiGameTitle);
						intent.putExtra("dotchi_time", dateStr);
						intent.putExtra("is_personal", isPersonal);
						intent.putExtra("is_secret", isPrivate);
						intent.putExtra("is_official", false);
						intent.putExtra("game_id", Integer.parseInt(gameId));
						if (maxNumYes == 0)
							intent.putExtra("vote_limit", -1);
						else
							intent.putExtra("vote_limit", maxNumYes);
						startActivityForResult(intent, REQUEST_GAME);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
			}.execute(rootUrl, "game_title", dotchiGameTitle, "category_id", String.valueOf(categoryId), "is_official", "0","is_secret", isPrivateStr, "is_personal", isPersonalStr,
					"reply_day", String.valueOf(daysToExpire), "dotchi_time", dateStr, "dotchi_id", String.valueOf(dotchiId), "vote_limit", String.valueOf(maxNumYes), "game_item", gameCardItems,
					"invite_friends", inviteFriendsItems, "group_id", "");
			

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	protected void setNumberPickerViews()	{
		// Set the last two items on the bottom.
		ImageView replyDateView = (ImageView) findViewById(R.id.set_days_expiry_image);
		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View npView = inflater.inflate(R.layout.number_picker_layout, null);
		final NumberPicker np = (NumberPicker) npView.findViewById(R.id.numberPicker);
		np.setMinValue(1);
		np.setMaxValue(10);
		np.setWrapSelectorWheel(true);
		AlertDialog.Builder builder = new AlertDialog.Builder(GameSettingActivity.this, R.style.npColor);
		final AlertDialog d1 = builder.setView(npView)
			.setPositiveButton("Set", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// set textView
					setExpiryView.setText(String.valueOf(np.getValue()));
					dialog.dismiss();
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create();
		replyDateView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				d1.show();
			}
		});
		
		View npView2 = inflater.inflate(R.layout.number_picker_layout, null);
		maxNumYesPicker = (NumberPicker) npView2.findViewById(R.id.numberPicker);
		maxNumYesPicker.setMinValue(0);
		maxNumYesPicker.setMaxValue(gameCards.size());
		maxNumYesPicker.setWrapSelectorWheel(true);
		final AlertDialog d2 = builder.setView(npView2)
			.setPositiveButton("Set", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setMaxNumDaysView.setText(String.valueOf(maxNumYesPicker.getValue()));
					dialog.dismiss();
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();								
				}
			})
			.create();
		ImageView maxNumYesView = (ImageView) findViewById(R.id.set_max_num_yes_image);
		maxNumYesView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				d2.show();
			}
		});
	}
	
	@Override
	public void onDataPass(List<GameCardItem> data) {
		// This is used to pass information for gameCards, when information has changed
		Log.d(TAG, "Passing data up!");
		gameCards = new ArrayList<GameCardItem>(data);
		notifyListSizeChanged(gameCards.size());
		
	}

	@Override
	public void notifyListSizeChanged(int newSize) {
		if (maxNumYesPicker == null)
			setNumberPickerViews();
		maxNumYesPicker.setMaxValue(newSize);
	}

	class EnableInviteFriendsWatcher implements TextWatcher	{

		private int index = -1;

		public EnableInviteFriendsWatcher(int resId)	{
			for (int i = 0 ; i < textViewResIds.length; i++)	
				if (resId == textViewResIds[i])
					this.index = i;

		}
		
		@Override
		public void afterTextChanged(Editable s) {
			// We are going to register this to multiple texts
			ImageView inviteFriends = (ImageView) findViewById(R.id.invite_friends_image);
			if (s != null && s.toString().trim().length() > 0)	{
				areFieldsNull[index] = true;
				// Check if all areFieldsNull are true; if so, then set invite friends to enabled
				if (checkFields())	{
					inviteFriends.setEnabled(true);
				} else
					inviteFriends.setEnabled(false);
			} else	{
				areFieldsNull[index] = false;
				inviteFriends.setEnabled(false);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}
		
	}
	
	protected boolean checkFields()	{
		boolean b = true;
		for (int i = 0; i < areFieldsNull.length; i++)
			b = b && areFieldsNull[i];
		return b & (gameCards == null || gameCards.size() == 0) ? false:true;
	}
	
	class UploadPhotoTask extends SaveImageTask	{

		
		public UploadPhotoTask(Context context, GameCardItem item) {
			super(context, item);
		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null)
				result = "Empty";
			Log.d(TAG, result);
			result = processResult(result);
			// Process data for this now;
			try {
				// There are two objects coming in; if the first object is not data, then we jump into other version
				GameCardItem updatedItem;
				JSONObject json = new JSONObject(result);
				if (json.has("data"))	{
					JSONObject data = json.getJSONObject("data");
					String url = data.getString("pic_url");
					// Set item's url as complete.
					updatedItem = new GameCardItem();
					updatedItem.setItemTitle(item.getItemTitle());
					updatedItem.setItemContent(item.getItemContent());
					updatedItem.setItemImage(url);
				} else	{
					updatedItem = item;
				}
				// Now that we have the updated item, update that to the synchronized list.
				synchronizedGameCards.add(updatedItem);
				Log.d(TAG, "Synchronized item added: " + updatedItem.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}	
	}

}
