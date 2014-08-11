package com.dotchi1;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.dotchi1.NewInviteChoiceFriendFragment.OnFriendsSetListener;
import com.dotchi1.NewInviteSelfChoiceFragment.CreateDotchiPackageTask;
import com.dotchi1.NewInviteSelfChoiceFragment.OnGameItemSetListener;
import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.backend.ViewUtils;
import com.dotchi1.backend.json.JsonDataWrapper;
import com.dotchi1.model.FriendPageFriendItem;
import com.google.analytics.tracking.android.EasyTracker;
/**
 * 
 * @author William
 * @see <br> There are two button , one textView ,one editText and two fragment layout
 * 
 */
public class NewInviteActivity extends Activity implements OnClickListener,OnEditorActionListener, OnFriendsSetListener, OnGameItemSetListener {
	
	public static final int INVITE_INDEX_FRAGMENT_ID = 1;
	public static final int INVITE_DOTCHI_FRAGMENT_ID = 2;
	public static final int INVITE_FAVOURITE_FRAGMENT_ID = 3;
	public static final int INVITE_CHOICE_FRAGMENT_ID = 4;
	
	public static final int REQUEST_HOT_DOTCHI = 10;
	public static final int REQUEST_MY_PACKAGE = 11;
	public static final int REQUEST_CONFIRM_GAME = 12;
	/** {@link #layoutResize()} */
	private static Resources res;
	/** {@link #onBackPressed()} */
	private static boolean inIndexPage;
	/** {@link #switchFragment()} */
	private static FragmentManager mFragmentManager;
	private static String TAG = "NewInviteActivity";
	private static Class<?> indexFragment = NewInviteIndexFragment.class;
	private static Class<?> dotchiFragment = NewInviteDotchiFragment.class;
	private static Class<?> favourFragment = NewInviteFavorFragment.class;
	private static Class<?> selfChoiceFragment = NewInviteSelfChoiceFragment.class;
	private static Class<?> choiceFriendFragment = NewInviteChoiceFriendFragment.class;
	// ================ View composer================= //
	private RelativeLayout titleLayout,inputLayout,pagesLayout,choiceFriendLayout;
	private static int pagesfragmentContainer;// address reference
	private static int choiceFriendsfragmentContainer;// address reference
	/** {@link #onClick(View)} */
	private ImageButton sendInviteButton,backButton;
	private static TextView titleView;
	/** {@link #onEditorAction()} */
	private EditText input;
	// =============== PassUp Data variables ================ //
	private ArrayList<FriendPageFriendItem> friends;
	private ArrayList<JSONObject> gameItems;
	private boolean isSavePackage;
	private String packageName = "";
	
	private boolean isPersonal;
	private boolean isSecret;
	private int replyDay;
	private int voteLimit;
	
	
	// =================  Override method  ================= //
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// no title bar
		setContentView(R.layout.activity_new_invite);
		Bundle bundle = getIntent().getExtras();
		
		res = getResources();
		mFragmentManager = getFragmentManager();
		// bind xml view
		titleLayout			=	(RelativeLayout) findViewById(R.id.new_invite_title_layout);
		inputLayout			=	(RelativeLayout) findViewById(R.id.new_invite_input_layout);
		pagesLayout			=	(RelativeLayout) findViewById(R.id.new_invite_pages_fragment_container);
		choiceFriendLayout	=	(RelativeLayout) findViewById(R.id.new_invite_choice_fragment_container);
		sendInviteButton	=	(ImageButton)	 findViewById(R.id.new_invite_send_invite_button);
		backButton			=	(ImageButton)	 findViewById(R.id.new_invite_back_home_button);
		input				=	(EditText)		 findViewById(R.id.new_invite_input);
		titleView			=	(TextView)		 findViewById(R.id.new_invite_title_textView);
		pagesfragmentContainer = R.id.new_invite_pages_fragment_container;
		choiceFriendsfragmentContainer = R.id.new_invite_choice_fragment_container;
		// set event
		backButton.setOnClickListener(this);
		sendInviteButton.setOnClickListener(this);
		
		switchIndexFragment(null);//can bundle something if need 
		switchChoiceFriendsFragment(bundle);//can bundle selected friend list

	}//end of onCreate
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
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
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
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }// end of onStop 
	@Override
	public void onBackPressed() {
		//ViewUtils.hideSoftKeyboard(this);
		if(inIndexPage){
			finish();
		}else{ // back to index page
			switchIndexFragment(null);
		}
	}
	/* set all onClick event */
	@Override
	public void onClick(View v) {
		//ViewUtils.hideSoftKeyboard(this);
		switch(v.getId()){
		case R.id.new_invite_send_invite_button:
			// Send data!
			boolean canCreate = validateFields();
			if (canCreate)	{
				// Before we confirm and create game, check the isSavePackage first.
				if (isSavePackage)	{
					Log.d(TAG, "isSavePackage here is " + isSavePackage);
					// Create dialog, get title, and call package
					final EditText input = new EditText(NewInviteActivity.this);
					AlertDialog dialog = new AlertDialog.Builder(NewInviteActivity.this)
											.setTitle("Save to Favourites")
											.setMessage("Enter Dotchi Package name to be saved as:")
											.setView(input)
											.setPositiveButton("OK", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													packageName = input.getText().toString();
													createPackage();
													// Go confirm other fields first, and then create game
													Intent i = new Intent(NewInviteActivity.this, ConfirmGameSettingActivity.class);
													startActivityForResult(i, REQUEST_CONFIRM_GAME);
												}
											})
											.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													// Cancelled
													dialog.dismiss();
													// Go confirm other fields first, and then create game
												}
											})
											.show();
				} else	{
					Intent i = new Intent(NewInviteActivity.this, ConfirmGameSettingActivity.class);
					startActivityForResult(i, REQUEST_CONFIRM_GAME); 
				}
			} else
				Toast.makeText(this, "All parameters need to be set before we create a game!", Toast.LENGTH_SHORT).show();
			break;
		case R.id.new_invite_back_home_button:
			finish();
			break;
		}
	}
	
	@Override
	public void onGameItemSet(ArrayList<JSONObject> gameItems, boolean isSavePackage) {
		this.gameItems = gameItems; 
		this.isSavePackage = isSavePackage;
		Log.d(TAG, "Package saving has been set to " + isSavePackage);
		
	}
	
	@Override
	public void onFriendsSet(ArrayList<FriendPageFriendItem> friends) {
		this.friends = friends;
	}
	
	
	/**
	 * This function generates the url to send to the API for creating a game
	 */
	protected void createGame()	{
		Toast.makeText(this, "Starting game", Toast.LENGTH_SHORT).show();
		String rootUrl = getResources().getString(R.string.api_test_root_url);
		SharedPreferences preferences = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		final String gameTitle = input.getText() == null? "" : input.getText().toString();
		final String dotchiType = "0"; // 約聚會
		final String isOfficial = "0";
		final String dotchiId = preferences.getString("DOTCHI_ID", "35");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		final String dotchiTime = sdf.format(new Date());
		JSONArray arr = new JSONArray();
		for (JSONObject jo : gameItems)	{
			// not sure if this needs a doublecheck for data integrity here;
			JSONObject formattedJO = new JSONObject();
			try {
				formattedJO.put("item_image", jo.get("pic"));
				formattedJO.put("item_title", jo.get("item_title"));
				formattedJO.put("is_date", "0");
				String itemContent = jo.has("item_content")?jo.getString("item_content"): "";
				formattedJO.put("item_content", itemContent);
				arr.put(formattedJO);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		String gameItemStr = JsonDataWrapper.wrapData(arr);
		JSONArray arr2 = new JSONArray();
		for (FriendPageFriendItem friend : friends)	{
			JSONObject jo = new JSONObject();
			try {
				jo.put("dotchi_id", friend.getDotchiId());
				arr2.put(jo);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		String friendStr = JsonDataWrapper.wrapData(arr2);
		
		new PostUrlTask()	{

			@Override
			protected void onPostExecute(String result) {
				result = processResult(result);
				try {
					JSONObject jo = new JSONObject(result).getJSONObject("data");
					if (jo.has("game_id"))	{
						// Successful! Game created!
						// Start game from here
						Intent intent = new Intent(NewInviteActivity.this, GameActivity.class);
						intent.putExtra("game_id", jo.getInt("game_id"));
						intent.putExtra("game_title", gameTitle);
						intent.putExtra("dotchi_time", dotchiTime);
						intent.putExtra("is_personal", isPersonal);
						intent.putExtra("is_secret", isSecret);
						intent.putExtra("is_official", false);
						startActivity(intent);
						finish();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		}.execute(rootUrl + "/game/create_game", "game_title", gameTitle, "dotchi_type", dotchiType, "is_official", isOfficial,
						"is_personal", isPersonal?"1":"0", "is_secret", isSecret?"1":"0", "reply_day", String.valueOf(replyDay),
						"vote_limit", String.valueOf(voteLimit), "dotchi_id", String.valueOf(dotchiId), "game_item", gameItemStr,
						"invite_friends", friendStr, "dotchi_time", dotchiTime);
		
	}
	
	protected void createPackage()	{
		// If isSavePackage, call create Dotchi package. There's a postURL task for that in SelfChoice Fragment
		String rootUrl = getResources().getString(R.string.api_test_root_url);
		SharedPreferences preferences = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		String dotchiId = preferences.getString("DOTCHI_ID", "35");
		// Process itemStr from gameItems;
		JSONArray arr = new JSONArray();
		for (JSONObject jo : gameItems)	{
			JSONObject formattedJO = new JSONObject();
			try	{
				formattedJO.put("item_image", jo.get("pic"));
				formattedJO.put("item_title", jo.get("item_title"));
				String itemContent = jo.has("item_content")?jo.getString("item_content"): "";
				formattedJO.put("item_content", itemContent == null || itemContent.length() == 0? "":itemContent);				
				arr.put(formattedJO);
			} catch(JSONException e)	{
				e.printStackTrace();
			}
		}
		String itemStr = JsonDataWrapper.wrapData(arr);
		String[] params = {rootUrl + "/game/create_dotchi_package", "dotchi_id", dotchiId, "dotchi_package_title", packageName, "items", itemStr};
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
			new CreateDotchiPackageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else
			new CreateDotchiPackageTask().execute(params);
		
	}
	
	/**
	 * This function checks if all fields are filled. If so, then enable createGame click.
	 * @return
	 */
	protected boolean validateFields(){
		Log.d(TAG, friends==null? "Friends is null": "friends is not null");
		Log.d(TAG, gameItems == null? "Game Items are null": "game Items are not null");
		return friends != null && gameItems != null && friends.size() > 0 && gameItems.size() > 0;
	}
	
	@Override
	public boolean onEditorAction(TextView arg0, int actionId, KeyEvent event) {
		ViewUtils.hideSoftKeyboard(this);
        return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_HOT_DOTCHI || requestCode == REQUEST_MY_PACKAGE)	{
			// take intent, get bundle, and switch fragment to Self choice with those arguments
			Log.d(TAG, "caught onActivityResult from hot dotchi package. rerouting information to fragment now.");
			if (resultCode == RESULT_OK)	{
				// We bundled at intent, not inside args. Might as well do it here.
				Bundle args = new Bundle();
				args.putString("data", data.getStringExtra("data"));
				switchSelfChoiceFragment(args);
			}
		} else if (requestCode == REQUEST_CONFIRM_GAME)	{
			if (resultCode == RESULT_OK)	{
				// Add it to game fields
				isPersonal = data.getBooleanExtra("is_personal", true);
				isSecret = data.getBooleanExtra("is_secret", true);
				replyDay = data.getIntExtra("reply_day", 7);
				voteLimit = data.getIntExtra("vote_limit", 0);
				// Create game and then start game activity!
				createGame();
			}
		} else	
			super.onActivityResult(requestCode, resultCode, data);
	}
	/**
	 * @author William
	 * @param mFragmentClass 
	 * @param continer (res R.id)
	 * @param bundle
	 * @throws Out of memory error
	 * @throws Exception 
	 */
	private static void switchFragment(Class<?> mFragmentClass,int container, Bundle bundle)throws OutOfMemoryError,Exception  {
		Log.d(TAG,"+++ Switch Fragment Page +++");
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		try {
			Fragment fragment = (Fragment) mFragmentClass.newInstance();
			if (null != bundle) {
				fragment.setArguments(bundle);
			}
			ft.replace(container, fragment, null);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.addToBackStack(null);
			ft.commit();
		}catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG,"Something wrong with fragment ...");
			throw e;
		}catch (OutOfMemoryError error) {
			Log.e(TAG,"Out of memory error ...");
			throw error;
        }
		Log.d(TAG,"--- Switch Fragment Page ---");
	}//end of switchFragmentPage
	//
	private static boolean switchChoiceFriendsFragment(Bundle bundle){
			try{switchFragment(choiceFriendFragment,choiceFriendsfragmentContainer,bundle);
			}catch(OutOfMemoryError e){
				System.gc();return false;
			}catch(Exception e){
				return false;
			}
			return true;
	}
	// call back for other ChildFragment 
	public static boolean switchIndexFragment(Bundle bundle){
		if (bundle == null)	{
			bundle = new Bundle();
		}
		bundle.putInt("ID", INVITE_INDEX_FRAGMENT_ID);
		try{switchFragment(indexFragment,pagesfragmentContainer,bundle);
		}catch(OutOfMemoryError e){
			System.gc();return false;
		}catch(Exception e){
			return false;
		}
		titleView.setText(""+res.getString(R.string.new_invite_title_index_page));
		inIndexPage = true;
		return true;
	}
	// call back for other ChildFragment
	public static boolean switchDotchiFragment(Bundle bundle){
		if (bundle == null)	{
			bundle = new Bundle();
		}
		bundle.putInt("ID", INVITE_DOTCHI_FRAGMENT_ID);
		try{switchFragment(dotchiFragment,pagesfragmentContainer,bundle);
		}catch(OutOfMemoryError e){
			System.gc();return false;
		}catch(Exception e){
			return false;
		}
		titleView.setText(""+res.getString(R.string.new_invite_title_edit_page));
		inIndexPage = false;
		return true;
	}
	// call back for other ChildFragment
	public static boolean switchFavouriteFragment(Bundle bundle){
		if (bundle == null)	{
			bundle = new Bundle();
		}
		bundle.putInt("ID", INVITE_FAVOURITE_FRAGMENT_ID);
		try{switchFragment(favourFragment,pagesfragmentContainer,bundle);
		}catch(OutOfMemoryError e){
			System.gc();return false;
		}catch(Exception e){
			return false;
		}
		titleView.setText(""+res.getString(R.string.new_invite_title_edit_page));
		inIndexPage = false;
		return true;
	}
	// call back for other ChildFragment
	public static boolean switchSelfChoiceFragment(Bundle bundle){
		if (bundle == null)	{
			bundle = new Bundle();
		}
		bundle.putInt("ID", INVITE_CHOICE_FRAGMENT_ID);
		try{switchFragment(selfChoiceFragment,pagesfragmentContainer,bundle);
		}catch(OutOfMemoryError e){
			System.gc();return false;
		}catch(Exception e){
			return false;
		}
		titleView.setText(""+res.getString(R.string.new_invite_title_edit_page));
		inIndexPage = false;
		return true;
	}

	

}
