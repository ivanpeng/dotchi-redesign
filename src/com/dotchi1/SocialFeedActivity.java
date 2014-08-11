package com.dotchi1;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.backend.ViewUtils;
import com.dotchi1.model.BaseActivityItem;
import com.dotchi1.model.BaseActivityItem.ActivityType;
import com.google.analytics.tracking.android.EasyTracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SocialFeedActivity extends Activity {

	ImageLoader imageLoader;
	DisplayImageOptions options;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_social_feed);
		imageLoader = ImageLoader.getInstance();
		options = ViewUtils.getLocalImageConfiguration();
		
		int dotchiId = getIntent().getIntExtra("dotchi_id", -1);
		String rootUrl = getResources().getString(R.string.api_root_url);
		final ListView listView = (ListView) findViewById(R.id.activity_social_list);
		new PostUrlTask(){

			@Override
			protected void onPostExecute(String result) {
				result = processResult(result);
				JSONObject jsonObj;
				List<BaseActivityItem> objects = new ArrayList<BaseActivityItem>();
				try {
					jsonObj = new JSONObject(processResult(result));
					JSONArray arr = jsonObj.getJSONArray("data");
					Log.d(TAG, arr.toString());
					ObjectMapper mapper = new ObjectMapper();
					mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
					mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
					mapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
					objects = mapper.readValue(arr.toString(), mapper.getTypeFactory().constructCollectionType(List.class, BaseActivityItem.class));
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				List<BaseActivityItem> feedObj = new ArrayList<BaseActivityItem>();
				for (BaseActivityItem object : objects)
					if (object.getActivityType() == ActivityType.MESSAGE)
						feedObj.add(object);
				if (feedObj.size() == 0){
					// Display a view saying that feed is empty
					TextView emptyView = (TextView) findViewById(R.id.empty_list_view);
					emptyView.setVisibility(View.VISIBLE);
				} else {
					SocialFeedAdapter adapter = new SocialFeedAdapter(SocialFeedActivity.this, R.layout.pending_list_item, feedObj);
					listView.setAdapter(adapter);
				}

			}
		}.execute(rootUrl + "/activity/get_activity", "dotchi_id", String.valueOf(dotchiId), "category", "3");
		
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
		finish();
	}


	class SocialFeedAdapter extends ArrayAdapter<BaseActivityItem> {

		private Context context;
		private ArrayList<BaseActivityItem> objects;
		
		public SocialFeedAdapter(Context context, int resource, List<BaseActivityItem> objects) {
			super(context, resource, objects);
			this.context = context;
			this.objects = new ArrayList<BaseActivityItem>(objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BaseActivityItem item = objects.get(position);
			View view = convertView;
			if (view == null)	{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.pending_list_item, null);			
			}
			// Populate things in list item now
			ImageView imageView = (ImageView) view.findViewById(R.id.list_item_picture);
			imageLoader.displayImage(item.getHeadImage(), imageView, options);
			TextView name = (TextView) view.findViewById(R.id.feed_profile_name);
			name.setText(item.getEventTitle());
			TextView timeLeft = (TextView) view.findViewById(R.id.time_left);
			DateTime dt;
			if (item.getDotchiTime() != null && item.getDotchiTime().length() >0)	{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
				try {
					dt = new DateTime(sdf.parse(item.getDotchiTime()));
					timeLeft.setText(dt.withTimeAtStartOfDay().toString("yyyy-MM-dd"));
				} catch (ParseException e) {
					timeLeft.setText("");
					e.printStackTrace();
				}
			}
			else
				timeLeft.setText("");
			TextView feedTitle = (TextView) view.findViewById(R.id.feed_title);
			feedTitle.setText(item.getGameTitle());
			return view;
		}
	}
}
