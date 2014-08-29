package com.dotchi1;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.devsmart.android.ui.HorizontalListView;
import com.devsmart.android.ui.HorizontalListView.OnCenteredListener;
import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.backend.json.JsonDataWrapper;
import com.dotchi1.image.LiteImageLoader;

public class CreateGameItemsActivity extends ActionBarActivity implements OnClickListener, OnCenteredListener, OnCheckedChangeListener {

	public static final String TAG = "CreateGameItemsActivity";
	public static final int CHOOSE_PHOTO = 100;
	public static final int REQ_GET_PACKAGE_ITEMS = 101;
	
	private ImageButton addItemButton;
	
	private EditText title;
	private ImageView searchButton;
	private ToggleButton addToFavouritesButton;
	private Button dotchiPackageButton;
	private Button favouritePackageButton;
	private Button switchImagesButton;

	private HorizontalListView packageItemView;
	private DotchiPackageItemAdapter adapter;
	private ArrayList<JSONObject> list;
	private ArrayList<JSONObject> dateList;
	private View emptyView;
	
	private Bundle bundle;
	private boolean isSavePackage = false;
	private int currentPosition = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_new_invite_self_choice);
		bundle = getIntent().getExtras();
		// Set actionbar
		View actionbar = getLayoutInflater().inflate(R.layout.menu_dotchi_package, null);
		TextView menuTitle = (TextView) actionbar.findViewById(R.id.package_title);
		menuTitle.setText(getResources().getString(R.string.title_activity_create_game_items));
		ImageButton forwardButton = (ImageButton) actionbar.findViewById(R.id.forward_button);
		// TODO: Add GameCardItems of dates to list
		ArrayList<Date> dates = (ArrayList<Date>) getIntent().getExtras().getSerializable("dates");
		if (dates != null && dates.size() > 0)	{
			dateList = new ArrayList<JSONObject>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
			for (Date d: dates)	{
				JSONObject jo = new JSONObject();
				String titleStr = sdf.format(d);
				try {
					jo.put("item_image", "");
					jo.put("item_title", titleStr);
					jo.put("item_content", "");
					jo.put("is_date", 1);
					dateList.add(jo);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		forwardButton.setVisibility(View.VISIBLE);
		forwardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Start activity for choose friends
				// Before we start, add all of date objects to original list
				if (list != null && dateList != null)
					list.addAll(dateList);
				if (list != null && list.size() > 0){
					Intent intent = new Intent(CreateGameItemsActivity.this, NewFriendSelectActivity.class);
					// put game items into bundle, as string
					String gameItemStr = packageItemsToList(list);
					bundle.putString("game_item", gameItemStr);
					intent.putExtras(bundle);
					startActivity(intent);
				} else	{
					// TODO: make this legal, if dates are viable.
					Toast.makeText(getApplicationContext(), "Can't have 0 game choices", Toast.LENGTH_SHORT).show();
				}
			}
		});
		ImageButton backButton = (ImageButton) actionbar.findViewById(R.id.back_home_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		getSupportActionBar().setCustomView(actionbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		title = (EditText) findViewById(R.id.invite_self_choice_search_text);
		searchButton = (ImageView) findViewById(R.id.invite_self_choice_search_button);
		emptyView = findViewById(R.id.photo_container_empty_view);
		addToFavouritesButton = (ToggleButton) findViewById(R.id.add_to_favourites);
		dotchiPackageButton = (Button) findViewById(R.id.dotchi_package_button);
		favouritePackageButton = (Button) findViewById(R.id.favourite_package_button);
		switchImagesButton = (Button) findViewById(R.id.switch_images_button);
		
		packageItemView = (HorizontalListView) findViewById(R.id.photo_select_container);
		packageItemView.setSnappingToCenter(true);
		packageItemView.setOnCenteredListener(this);
		
		addItemButton = (ImageButton) findViewById(R.id.add_item_button);
		
		// Set onClickListeners for adding photos and whatnot there.
		addItemButton.setOnClickListener(this);
		searchButton.setOnClickListener(this);
		dotchiPackageButton.setOnClickListener(this);
		favouritePackageButton.setOnClickListener(this);
		switchImagesButton.setOnClickListener(this);
		addToFavouritesButton.setOnCheckedChangeListener(this);

		// Now populate the list if there are arguments
		Bundle bundle = getIntent().getExtras();
		
		if (bundle != null){
			String jsonStr = bundle.getString("data");
			Log.d(TAG, "There's data in the bundle! We're parsing that " + jsonStr);
			addPackageItemsToList(jsonStr);
		}
	}

	
	@Override
	public void updateViews(int position) {
		if (list != null && list.size() > 0){
			JSONObject item = adapter.getItem(position);
			// We need to update title and event times here.
			String titleText;
			try {
				titleText = item.has("item_title") ? item.getString("item_title") : "";
				title.setText(titleText);
				currentPosition = position;
				// TODO: set event times and GPS when times are available!
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void addBlankItem()	{
		Log.d(TAG, "adding blank item");
		adapter.addEmptyObject();
		packageItemView.scrollToEnd();
	}
	
	protected void getOnePhoto()	{
		String rootUrl = getResources().getString(R.string.api_test_root_url);
		String query = title.getText().toString();
		new PostUrlTask()	{

			@Override
			protected void onPostExecute(String result) {
				// 
				result = processResult(result);
				try {
					JSONObject jo = new JSONObject(result).getJSONObject("data");
					// This is a slightly complicated process
					// First time: list and adapter will be null. Will go into both loops.
					// Subsequent times: don't need to reinitialize; just need to add through adapter and notify
					jo.put("item_title", title.getText().toString());
					Log.d(TAG, "put " + title.getText().toString() + " as string in item_title.");
					if (list == null || list.size() == 0)	{
						list = new ArrayList<JSONObject>();
						list.add(jo);
						adapter = new DotchiPackageItemAdapter(CreateGameItemsActivity.this, R.layout.dotchi_package_item, list, emptyView);
						packageItemView.setAdapter(adapter);
						addItemButton.setVisibility(View.VISIBLE);
					} else	{
						// This is fucking confusing!
						list.set(adapter.getCount()-1, jo);
						adapter.update(adapter.getCount()-1, jo);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		}.execute(rootUrl +"/pic/get_google_one_pic", "search_string", query);
	}
	
	protected void getMultiplePhotos(int position)	{
		String query = title.getText().toString();
		Log.d(TAG, "Getting multiple photos for string " + query);
		Intent intent = new Intent(this, ChoosePhotoActivity.class);
		intent.putExtra("search_string", query);
		intent.putExtra("position", position);
		startActivityForResult(intent, CHOOSE_PHOTO);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHOOSE_PHOTO)	{
			// We've entered here
			Log.d(TAG, "We've caught a choose photo request code!");
			if (resultCode == RESULT_OK)	{
				// grab the url and display it
				//TODO: need to change this, as the url may also be local files...
				String url = data.getStringExtra("image");
				String searchString = data.getStringExtra("search_string");
				int position = data.getIntExtra("position",0);
				JSONObject jo = new JSONObject();
				try {
					jo.put("pic", url);
					jo.put("item_title", searchString);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				list.set(position, jo);
				adapter.update(position, jo);
			}
		} else if (requestCode == REQ_GET_PACKAGE_ITEMS)	{
			// Add the items to the list;
			if (resultCode == RESULT_OK)
				addPackageItemsToList(data.getStringExtra("data"));
		} else
			super.onActivityResult(requestCode, resultCode, data);
			
	}

	public void addPackageItemsToList(String jsonStr) {
		boolean isNewList = false;
		if (jsonStr != null && jsonStr.length() > 0)	{
			if (list == null || list.size() == 0)	{
				list = new ArrayList<JSONObject>();
				isNewList = true;
			}
			try {
				JSONArray ja = new JSONArray(jsonStr);
				for (int i = 0; i < ja.length(); i++)	{
					list.add(ja.getJSONObject(i));
					if (!isNewList)
						adapter.add(ja.getJSONObject(i));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
				
		if (list != null && list.size() > 0)	{
			if (isNewList)	{
				adapter = new DotchiPackageItemAdapter(this, R.layout.dotchi_package_item, list, emptyView);
				packageItemView.setAdapter(adapter);
				addItemButton.setVisibility(View.VISIBLE);
				// Need this thing when initializing
				updateViews(0);
			}
			adapter.notifyDataSetChanged();
		}
	}

	protected String packageItemsToList(ArrayList<JSONObject> gameItems)	{
		JSONArray arr = new JSONArray();
		for (JSONObject jo : gameItems)	{
			// not sure if this needs a doublecheck for data integrity here;
			JSONObject formattedJO = new JSONObject();
			try {
				if (jo.has("pic"))
					formattedJO.put("item_image", jo.get("pic"));
				else
					formattedJO.put("item_image", "");
				formattedJO.put("item_title", jo.get("item_title"));
				if(jo.has("is_date"))
					formattedJO.put("is_date", jo.get("is_date"));
				else
					formattedJO.put("is_date", "0");
				String itemContent = jo.has("item_content")?jo.getString("item_content"): "";
				formattedJO.put("item_content", itemContent);
				arr.put(formattedJO);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return JsonDataWrapper.wrapData(arr);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, SelectPackageActivity.class);
		switch(v.getId()){
		case R.id.dotchi_package_button:
			// Between this and favourites, show both under same activity, and then load the fragment?
			intent.putExtra("is_dotchi_package", true);
			startActivityForResult(intent, REQ_GET_PACKAGE_ITEMS);
			break;
		case R.id.favourite_package_button:
			intent.putExtra("is_dotchi_package", false);
			startActivityForResult(intent, REQ_GET_PACKAGE_ITEMS);
			break;
		case R.id.invite_self_choice_search_button:
			// call getOnePhoto
			getOnePhoto();
			break;
		case R.id.add_item_button:
			// push list to end, and then add a blank template
			addBlankItem();
			break;
		case R.id.switch_images_button:
			if (list != null && list.size() > 0)
				getMultiplePhotos(currentPosition);
			else
				Toast.makeText(this, "Empty list", Toast.LENGTH_SHORT).show();
			break;
		}
	}



	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		isSavePackage = isChecked;
		Log.d(TAG, "isSavePackage has been toggled to " + isSavePackage);
	}
	
	/**
	 * This class is tasked with creating a dotchi package
	 * @author Ivan
	 *
	 */
	public static class CreateDotchiPackageTask extends PostUrlTask	{

		@Override
		protected void onPostExecute(String result) {
			result = processResult(result);
			try {
				JSONObject jo = new JSONObject(result).getJSONObject("data");
				if (jo != null && "success".equals(jo.getString("status")))
						Log.d(TAG, "Success creating a dotchi Package!");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public class DotchiPackageItemAdapter extends ArrayAdapter<JSONObject> {

		private static final int DOTCHI_PACKAGE_ADAPTER_IMAGE_SIZE = 110;
		
		private Context context;
		private ArrayList<JSONObject> objects;
		private LiteImageLoader imageLoader;
		private View emptyView;
		
		public DotchiPackageItemAdapter(Context context, int textViewResourceId,
				List<JSONObject> objects)	{
			this(context, textViewResourceId, objects, null);
		}
		
		public DotchiPackageItemAdapter(Context context, int textViewResourceId,
				List<JSONObject> objects, View emptyView) {
			super(context, textViewResourceId, objects);
			this.context = context;
			this.objects = new ArrayList<JSONObject>(objects);
			this.imageLoader = new LiteImageLoader(context);
			this.emptyView = emptyView;
			if (emptyView != null) 
				if( objects.size() > 0)	{
					emptyView.setVisibility(View.INVISIBLE);
				} else	{
					emptyView.setVisibility(View.VISIBLE);
				}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final JSONObject item = objects.get(position);
			View view = convertView;
			//if (view == null)	{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.dotchi_package_item, null);
			//}
			ImageView pictureView = (ImageView) view.findViewById(R.id.dotchi_package_picture);
			try {
				String picUrl = item.getString("pic");
				if (picUrl != null && picUrl.length() != 0 && !"".equals(picUrl))
					imageLoader.DisplayImage(picUrl, R.drawable.new_feed_photo_default, pictureView, DOTCHI_PACKAGE_ADAPTER_IMAGE_SIZE);
			} catch (JSONException e) {
				Log.w("dotchi package", "image loading failed. Setting default.");
			}
			ImageButton deleteObject = (ImageButton) view.findViewById(R.id.delete_package_item);
			deleteObject.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					list.remove(item);
					remove(item);
				}
			});
			return view;
		}

		@Override
		public void remove(JSONObject object)	{
			objects.remove(object);
			super.remove(object);
		}

		@Override
		public void add(JSONObject object) {
			super.add(object);
			objects.add(object);
		}
		
		/**
		 * Updates a currently set item in the list
		 * @param position
		 * @param newObject
		 */
		public void update(int position, JSONObject newObject)	{
			Log.d("DotchiPackageAdapter", "Updating index " + position + " with " + newObject.toString());
			objects.set(position, newObject);
			notifyDataSetChanged();
		}
		
		public void addEmptyObject()	{
			JSONObject o = new JSONObject();
			try {
				o.put("pic", "");
				add(o);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public int getCount() {
			return objects.size();
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			if (emptyView != null) 
				if( getCount() > 0)	{
					emptyView.setVisibility(View.INVISIBLE);
				} else	{
					emptyView.setVisibility(View.VISIBLE);
				}
		}
	}

}
