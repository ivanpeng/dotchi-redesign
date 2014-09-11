package com.dotchi1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dotchi1.backend.MainFeedAdapter;
import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.BaseFeedData;

public class NewFeedFragment extends Fragment implements OnRefreshListener{
	public static final String TAG = "NewFeedFragment";
	
	public static final String HOME_FEED_JSON_KEY = "HOME_FEED_JSON";
	public static final String HOME_FEED_JSON_VALUE = "HOME_FEED_JSON_VALUE";

	private SwipeRefreshLayout swipeLayout;
	private String dotchiId;
	private ListView listView;
	
	private MainFeedAdapter adapter;
	private ArrayList<BaseFeedData> feedData;
	private TextView empty;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// TODO: use here if we want communication interface with activity
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);



	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.new_feed_fragment, null);
		SharedPreferences preferences = getActivity().getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		dotchiId = preferences.getString("DOTCHI_ID", "35");

		final String rootUrl = getResources().getString(R.string.api_test_root_url);
		listView = (ListView) view.findViewById(R.id.feed_list);
		swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiping_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
                android.R.color.holo_green_light, 
                android.R.color.holo_orange_light, 
                android.R.color.holo_red_light);
        empty = (TextView) view.findViewById(R.id.empty_view);
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
			new GetHomeFeedTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rootUrl + "/activity/get_activity_msg", "dotchi_id", dotchiId, "activity_type", "0");
		} else {
			new GetHomeFeedTask().execute(rootUrl + "/activity/get_activity_msg", "dotchi_id", dotchiId, "activity_type", "0");
		}
		return view;
	}

	
	@Override
	public void onStop() {
		// We want to delete the JSON saved in sharedPreferences so we can grab it every time on start up, but nothing else.
		SharedPreferences preferences = getActivity().getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(HOME_FEED_JSON_KEY, false);
		editor.remove(HOME_FEED_JSON_VALUE);
		editor.commit();		
		super.onStop();
	}

	
	@Override 
	public void onRefresh() {
		swipeLayout.setRefreshing(true);
		final String rootUrl = getResources().getString(R.string.api_test_root_url);
		new GetHomeFeedTask().execute(rootUrl +  "/activity/get_activity_msg", "dotchi_id", dotchiId, "activity_type", "0");

        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                swipeLayout.setRefreshing(false);
            }
        }, 2000);
    }
	
	class GetHomeFeedTask extends PostUrlTask	{

		@Override
		protected void onPostExecute(String result) {
			swipeLayout.setRefreshing(false);
			result = processResult(result);
			// Check preferences; if not there, save!
			SharedPreferences preferences = getActivity().getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
			boolean isJsonSaved = preferences.getBoolean(HOME_FEED_JSON_KEY, false);
			if (!isJsonSaved)	{
				Editor editor = preferences.edit();
				editor.putBoolean(HOME_FEED_JSON_KEY, true);
				editor.putString(HOME_FEED_JSON_VALUE, result);
				editor.commit();
				// Now call process and set adapter
				feedData = processJson(result);
				if (feedData != null && feedData.size() > 0)	{
					//Log.d(TAG, "Populating the list with feed data: "+ feedData.toString());
					adapter = new MainFeedAdapter(getActivity(), 0, feedData, new LiteImageLoader(getActivity()));
					listView.setAdapter(adapter);
					//Set OnItemClickListener
					listView.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
							RelativeLayout imageLayout = (RelativeLayout) view.findViewById(R.id.image_layout);
							final RelativeLayout onClickLayout = (RelativeLayout) view.findViewById(R.id.on_click_layout);
							final LinearLayout imageDetailsLayout = (LinearLayout) view.findViewById(R.id.image_details_layout);
							if (onClickLayout != null)	{
								view.setSelected(!view.isSelected());
								if (view.isSelected())	{
									// add mask, disappear textView Layout
									onClickLayout.setVisibility(View.VISIBLE);
									imageDetailsLayout.setVisibility(View.GONE);
									Log.d(TAG, "Item View Selected");
									onClickLayout.setOnClickListener(new OnClickListener() {
										
										@Override
										public void onClick(View v) {
											v.setSelected(false);
											onClickLayout.setVisibility(View.GONE);
											imageDetailsLayout.setVisibility(View.VISIBLE);
										}
									});
								} else	{
									onClickLayout.setVisibility(View.GONE);
									imageDetailsLayout.setVisibility(View.VISIBLE);
									Log.d(TAG, "Item View unselected");
								}
							}
						}
					});
					listView.setOnScrollListener(new OnScrollListener() {
						
						@Override
						public void onScrollStateChanged(AbsListView view, int scrollState) {
						}
						
						@Override
						public void onScroll(AbsListView view, int firstVisibleItem,
								int visibleItemCount, int totalItemCount) {
							boolean enable = false;
							if (listView != null && listView.getChildCount() > 0)	{
								// check if the first item of the list is visible
					            boolean firstItemVisible = listView.getFirstVisiblePosition() == 0;
					            // check if the top of the first item is visible
					            boolean topOfFirstItemVisible = listView.getChildAt(0).getTop() == 0;
					            // enabling or disabling the refresh layout
					            enable = firstItemVisible && topOfFirstItemVisible;		
							}
							swipeLayout.setEnabled(enable);
						}
					});
				} else	{
					listView.setAdapter(null);
					empty.setVisibility(View.VISIBLE);
				}
				
			} else 	{
				Log.d(TAG, "JSON already saved, we compare and process new results.");
				// Here, we don't reset the adapter. Just compare and see what differences 
				ArrayList<BaseFeedData> newData = processJson(result);
				if (feedData != null){
					feedData = compareResults(feedData, newData);
					adapter.notifyDataSetChanged();
					Log.d(TAG, "List comparison from previous is different. Notifying dataset updated instead of pushing for update");
				}
			}
			
		}
	}
	
	/**
	 * This function compares the new incoming JSON, and sees if it's any different. If it is, we update the array list, do it in such a manner 
	 * that we don't reset the adapter.
	 * TODO: we just keep adding to new data. we don't remove if the old list has something new list doesn't have. While that's not very important
	 * because we reset this process after every destroy, we should still do it eventually.
	 * @param oldData
	 * @param newData
	 * @return
	 */
	public ArrayList<BaseFeedData> compareResults(ArrayList<BaseFeedData> oldData, ArrayList<BaseFeedData> newData)	{
		List<BaseFeedData> newItems = new ArrayList<BaseFeedData>();
		for (int i = 0; i < newData.size(); i++)	{
			BaseFeedData item = newData.get(i);
			if (oldData.contains(item))	{
				// Contains is based off the equals() function. That's set in BaseFeedData to be very loose.
				// TODO: Update the item with a strong equals, if necessary
				// For now, we can just find the item and call a set
				int oldIndex = oldData.indexOf(item);
				Log.d(TAG, "new index: " + i + ", old index: " + oldIndex);
				oldData.set(oldIndex, item);
			} else {
				// New data is not in old data; NOT other way
				// Instead of adding one by one, we add to a secondary list, and then addAll at beginning. This maintains order.
				newItems.add(item);
				Log.d(TAG,"adding new item.");
			}
		}
		if (newItems.size() > 0)	{
			oldData.addAll(0, newItems);
		}
		return oldData;
	}
	
	public ArrayList<BaseFeedData> processJson(String json)	{
		ArrayList<BaseFeedData> objects = new ArrayList<BaseFeedData>();
		try {
			JSONArray ja = new JSONObject(json).getJSONArray("data");
			ObjectMapper mapper = new ObjectMapper();
			mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
			mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
			mapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
			objects = mapper.readValue(ja.toString(), mapper.getTypeFactory().constructCollectionType(ArrayList.class, BaseFeedData.class));
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return objects;
	}
}
