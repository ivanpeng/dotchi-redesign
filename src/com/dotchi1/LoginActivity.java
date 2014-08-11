package com.dotchi1;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.backend.ViewUtils;
import com.dotchi1.sqlite.ImagesDataSource;
import com.facebook.AccessToken;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.google.analytics.tracking.android.EasyTracker;

public class LoginActivity extends FragmentActivity implements OnClickListener{

	public static final String TAG = "LoginActivity";
	private String url;
	public static final String UID_KEY = "UID";
	public static final String DOTCHI_ID_KEY = "DOTCHI_ID";
	public static final String IS_LOGGED_IN_KEY = "IS_LOGGED_IN";
	
	private static final List<String> PUBLISH_PERMISSIONS = Arrays.asList("publish_actions", "publish_stream");
	private boolean pendingPublishReauthorization = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		url = getResources().getString(R.string.api_test_root_url) + "/users/login";
		// before we begin
		int cleared = clearCache();
		Log.e(TAG, "Cleared " + cleared + " images in cache");
		SharedPreferences preferences = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		boolean isLoggedIn = preferences.getString(IS_LOGGED_IN_KEY, "0").equals("1") ? true : false;
		if (isLoggedIn)	{
			// Start async delay thread, and then finish activity
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					try {
						// TODO: get session and reauthenticate with necessary permissions here instead of sleeping...
						Thread.sleep(1000);
						Session session = Session.getActiveSession();
						if (session != null){
							List<String> permissions = session.getPermissions();
							if (!isSubsetOf(PUBLISH_PERMISSIONS, permissions)) {
								pendingPublishReauthorization = true;
								Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(LoginActivity.this, PUBLISH_PERMISSIONS);
								newPermissionsRequest = newPermissionsRequest.setDefaultAudience(SessionDefaultAudience.ONLY_ME);
								session.requestNewPublishPermissions(newPermissionsRequest);
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					Intent intent = new Intent(getApplicationContext(), NewMainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);

					finish();
				}
			}.execute();
		} else {
			// Make the button appear and set onClickListener
			ImageView fbLogin = (ImageView) findViewById(R.id.login_facebook);
			fbLogin.setVisibility(View.VISIBLE);
			final Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.animator.fade_in);
			fbLogin.startAnimation(animationFadeIn);
			fbLogin.setOnClickListener(this);
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
	@Override
	public void onClick(View v) {
		Session.openActiveSession(this, true, new Session.StatusCallback(){
			
			@Override
			public void call(Session session, SessionState state,
					Exception exception) {
				Log.d(TAG,session.getPermissions().toString());
				if (session.isOpened())	{
					
					Request.newMeRequest(session, new Request.GraphUserCallback() {
						
						@Override
						public void onCompleted(GraphUser user, Response response) {
							if (user != null)	{
								// This is where we proceed with authentication
								authenticateDotchi(Long.parseLong(user.getId()), user.getName());
								// Display and save to sharedPreferences
								savePreferences(UID_KEY, user.getId());
								ImageView fbLogin = (ImageView) findViewById(R.id.login_facebook);
								fbLogin.setVisibility(View.INVISIBLE);
								Intent intent = new Intent(getApplicationContext(), NewMainActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
								finish();
							}
						}
					}).executeAsync();
				} 
			}
		}, Arrays.asList("email", "user_friends", "user_birthday"));					
	}

	protected void authenticateDotchi(long uid, String name)	{ 
		Session session = Session.getActiveSession();
		if (session.isOpened())	{
			AccessToken accessToken = AccessToken.createFromExistingAccessToken(session.getAccessToken(), null, null, null, null);
			try	{
				// There is no authentication layer server-side; if there is, it would be added here
				// Instead, just call url here to add to dotchi User.
				new PostUrlTask() {

					@Override
					protected void onPostExecute(String result) {
						// Do some simple Json mapping;
						try {
							result = processResult(result);
							Log.d(TAG, result);
							JSONObject jsonObject = new JSONObject(result);
							JSONObject dataObj = jsonObject.getJSONObject("data");
							String dotchiId = dataObj.getString("dotchi_id");
							savePreferences(DOTCHI_ID_KEY, dotchiId, IS_LOGGED_IN_KEY, "1");
							Log.d(TAG, "Dotchi ID saved with " + dotchiId);
						} catch (JSONException e) {
							e.printStackTrace();
							savePreferences(DOTCHI_ID_KEY, "0", IS_LOGGED_IN_KEY, "0");
						}
					}
					
				}.execute(url, "facebook_token", accessToken.getToken(), "facebook_uid", String.valueOf(uid), "user_name", name).get();
				// Need to wait until this is complete, or else an invalid error will occur.
				
			} catch (Exception e)	{
				e.printStackTrace();
			}
		}
	}
	
	protected void savePreferences(String...data)	{
		SharedPreferences preferences = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		for (int i = 0; i < data.length; i+=2)	{
			editor.putString(data[i], data[i+1]);
		}
		editor.commit();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//SharedPreferences prefs = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		//Toast.makeText(getApplicationContext(), "Dotchi User ID: " + prefs.getString(DOTCHI_ID_KEY, "0"), Toast.LENGTH_LONG).show();
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
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
	
	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}
	
	public int clearCache()	{
		// Delete items that are older than 24 hours, upon starting this.
		ImagesDataSource imageDao = new ImagesDataSource(this);
		imageDao.open();
		int deleted = imageDao.deleteOldEntries();
		imageDao.close();
		return deleted;
	}
	

}
