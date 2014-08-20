package com.dotchi1;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.devsmart.android.ui.HorizontalListView;
import com.devsmart.android.ui.HorizontalListView.OnCenteredListener;
import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.image.LiteImageLoader;
/**
 * 
 * This serves as both a fill-in and a template for adding values from favourite + dotchi packages
 * @author Ivan
 *
 */
public class NewInviteSelfChoiceFragment extends Fragment implements OnClickListener, OnCenteredListener, OnCheckedChangeListener{

	public static final String TAG = "NewInviteSelfChoiceFragment";
	public static final int CHOOSE_PHOTO = 100;
	
	private ImageButton addItemButton;
	private OnGameItemSetListener mOnGameSetListener;
	
	private EditText title;
	private ImageView searchButton;
	private ToggleButton addToFavouritesButton;
	private ImageView dateButton;
	
	private HorizontalListView packageItemView;
	private DotchiPackageItemAdapter adapter;
	private ArrayList<JSONObject> list;
	private View emptyView;
	
	private boolean isSavePackage = false;
	private PostUrlTask dotchiPackageTask = null;
	
	public interface OnGameItemSetListener	{
		public void onGameItemSet(ArrayList<JSONObject> gameItems, boolean isSavePackage);
	}
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mOnGameSetListener = (OnGameItemSetListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View selfChoiceRootView = inflater.inflate(R.layout.fragment_new_invite_self_choice, container, false);
		title = (EditText) selfChoiceRootView.findViewById(R.id.invite_self_choice_search_text);
		searchButton = (ImageView) selfChoiceRootView.findViewById(R.id.invite_self_choice_search_button);
		emptyView = selfChoiceRootView.findViewById(R.id.photo_container_empty_view);
		addToFavouritesButton = (ToggleButton) selfChoiceRootView.findViewById(R.id.add_to_favourites);
		
		
		packageItemView = (HorizontalListView) selfChoiceRootView.findViewById(R.id.photo_select_container);
		packageItemView.setSnappingToCenter(true);
		packageItemView.setOnCenteredListener(this);
		
		addItemButton = (ImageButton) selfChoiceRootView.findViewById(R.id.add_item_button);
		
		// Set onClickListeners for adding photos and whatnot there.
		addItemButton.setOnClickListener(this);
		searchButton.setOnClickListener(this);
		dateButton.setOnClickListener(this);
		addToFavouritesButton.setOnCheckedChangeListener(this);
		
		packageItemView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				getMultiplePhotos(position);
			}
		});
		// Now populate the list if there are arguments
		Bundle bundle = getArguments();
		
		if (bundle != null){
			String jsonStr = bundle.getString("data");
			Log.d(TAG, "There's data in the bundle! We're parsing that " + jsonStr);
			if (jsonStr != null && jsonStr.length() > 0)	{
				list = new ArrayList<JSONObject>();
				try {
					JSONArray ja = new JSONArray(jsonStr);
					for (int i = 0; i < ja.length(); i++)	{
						list.add(ja.getJSONObject(i));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
					
			if (list != null && list.size() > 0)	{
				adapter = new DotchiPackageItemAdapter(getActivity(), R.layout.dotchi_package_item, list, emptyView);
				packageItemView.setAdapter(adapter);
				addItemButton.setVisibility(View.VISIBLE);
				// Need this thing when initializing
				updateViews(0);
				mOnGameSetListener.onGameItemSet(list, isSavePackage);
			}
		}
		
		// Declare the PostUrlTask, but don't execute it yet;
		dotchiPackageTask = new CreateDotchiPackageTask();
		
		return selfChoiceRootView;
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
					if (list == null)	{
						list = new ArrayList<JSONObject>();
						list.add(jo);
						adapter = new DotchiPackageItemAdapter(getActivity(), R.layout.dotchi_package_item, list, emptyView);
						packageItemView.setAdapter(adapter);
						addItemButton.setVisibility(View.VISIBLE);
					} else	{
						// This is fucking confusing!
						list.set(adapter.getCount()-1, jo);
						adapter.update(adapter.getCount()-1, jo);
					}
					mOnGameSetListener.onGameItemSet(list, isSavePackage);

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		}.execute(rootUrl +"/pic/get_google_one_pic", "search_string", query);
	}
	
	protected void getMultiplePhotos(int position)	{
		String query = title.getText().toString();
		Log.d(TAG, "Getting multiple photos for string " + query);
		Intent intent = new Intent(getActivity(), ChoosePhotoActivity.class);
		intent.putExtra("search_string", query);
		intent.putExtra("position", position);
		startActivityForResult(intent, CHOOSE_PHOTO);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHOOSE_PHOTO)	{
			// We've entered here
			Log.d("NewInviteSelfChoiceFragment", "We've caught a choose photo request code!");
			if (resultCode == Activity.RESULT_OK)	{
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
		} else
			super.onActivityResult(requestCode, resultCode, data);
			
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
		switch(v.getId()){
		case R.id.invite_self_choice_search_button:
			// call getOnePhoto
			getOnePhoto();
			break;
		case R.id.add_item_button:
			// push list to end, and then add a blank template
			addBlankItem();
			break;
		}
	}

	private void selectDates() {
		Intent intent = new Intent(getActivity(), ChooseDateActivity.class);
		startActivity(intent);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		isSavePackage = isChecked;
		mOnGameSetListener.onGameItemSet(list, isSavePackage);
		Log.d(TAG, "isSavePackage has been toggled to " + isSavePackage);
	}
	
	/**
	 * This class is tasked with creating a dotchi package
	 * TODO: have a onPreExecute check to see if this package is indeed new.
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
			JSONObject item = objects.get(position);
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
			return view;
		}

		@Override
		public void remove(JSONObject object)	{
			super.remove(object);
			objects.remove(object);
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
