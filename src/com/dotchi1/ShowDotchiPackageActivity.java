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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.PackageItem;

public class ShowDotchiPackageActivity extends ActionBarActivity {
	
	private static final int INCREMENT_COUNT = 1;
	private static final int CHANGE_TITLE = 2;
	// Menu view 
	private static ImageButton forwardButton;
	// Main view
	private static TextView selectedCountView;
	private static int selectedCount = 0;
	private TextView titleView;
	private GridView photoChoices;
	private List<PackageItem> packageItems;
	private PackageChoiceAdapter adapter;
	
	private static Handler uiHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what)	{
			case INCREMENT_COUNT:
				selectedCountView.setText(""+selectedCount);
				if (selectedCount > 0 )	{
					forwardButton.setVisibility(View.VISIBLE);
				} else
					forwardButton.setVisibility(View.INVISIBLE);
				break;
			case CHANGE_TITLE:
				break;
			}
			super.handleMessage(msg);
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_dotchi_package);
		String rootUrl = getResources().getString(R.string.api_test_root_url);
		int packageId = getIntent().getIntExtra("package_id", 2);
		String packageTitle = getIntent().getStringExtra("package_title");
		
		selectedCountView = (TextView) findViewById(R.id.dotchi_package_count);
		photoChoices = (GridView) findViewById(R.id.dotchi_package_grid);
		
		//TODO set action bar
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View actionbar = inflater.inflate(R.layout.menu_dotchi_package, null);
		ImageButton back = (ImageButton)actionbar.findViewById(R.id.back_home_button);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		TextView packageTitleView = (TextView) actionbar.findViewById(R.id.package_title);
		packageTitleView.setText(packageTitle);
		forwardButton = (ImageButton) actionbar.findViewById(R.id.forward_button);
		forwardButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// We create an arraylist of json objects, push forward the information
				ArrayList<JSONObject> objects = getSelectedItems();
				Intent returnIntent = new Intent();
				returnIntent.putExtra("data", objects.toString());
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		});	    
		
		getSupportActionBar().setCustomView(actionbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		// Grab dotchi package and display items
		//photoChoices.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);

		new PostUrlTask()	{
			@Override
			protected void onPostExecute(String result) {
				result = processResult(result);
				try {
					JSONArray arr = new JSONObject(result).getJSONArray("data");
					ObjectMapper mapper = new ObjectMapper();
					mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
					mapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
					packageItems = mapper.readValue(arr.toString(), mapper.getTypeFactory().constructCollectionType(List.class, PackageItem.class));
					
					adapter = new PackageChoiceAdapter(ShowDotchiPackageActivity.this, R.layout.dotchi_package_view_item, packageItems);
					photoChoices.setAdapter(adapter);
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
		}.execute(rootUrl + "/game/get_dotchi_package_item", "package_id", String.valueOf(packageId));
		photoChoices.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				PackageItem item = packageItems.get(position);
				Log.d("item_click", " view has been selected: " + !item.isSelected());
				ImageView checkmark = (ImageView) view.findViewById(R.id.checkmark_if_selected);
				view.setSelected(!item.isSelected());
				item.setSelected(!item.isSelected());
				if (item.isSelected())	{
					selectedCount++;
					checkmark.setVisibility(View.VISIBLE);
				}
				else	{
					selectedCount--;
					checkmark.setVisibility(View.INVISIBLE);
				}
				Message msg = new Message();
				msg.what = INCREMENT_COUNT;
				uiHandler.sendMessage(msg);
			}
		});
		
		
	}

	private ArrayList<JSONObject> getSelectedItems()	{
		ArrayList<JSONObject> objects = new ArrayList<JSONObject>();
		// Look through list, if selected, create new JSONObject and add to list
		// Need to keep in mind, 2 properties that are important are pic and item_title
		for (PackageItem item : packageItems)	{
			if (item.isSelected())	{
				JSONObject jo = new JSONObject();
				try {
					jo.put("pic", item.getItemImage());
					jo.put("item_title", item.getItemTitle());
					jo.put("item_content", item.getItemContent());
					objects.add(jo);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return objects;
	}
	
	public static class PackageChoiceAdapter extends ArrayAdapter<PackageItem>	{
		
		private static final int PACKAGE_CHOICE_ADAPTER_IMAGE_SIZE= 85;
		
		private Context context;
		private ArrayList<PackageItem> objects;
		private LiteImageLoader imageLoader;

		public PackageChoiceAdapter(Context context, int resource, List<PackageItem> objects) {
			super(context, resource, objects);
			this.context = context;
			this.objects = new ArrayList<PackageItem>(objects);
			imageLoader = new LiteImageLoader(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			PackageItem item = objects.get(position);
			if (view == null)	{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.dotchi_package_view_item, null);
			}
			// Set Title, description, and photo
			ImageView photo = (ImageView) view.findViewById(R.id.photo_image);
			imageLoader.DisplayImage(item.getItemImage(), R.drawable.new_feed_photo_default, photo, PACKAGE_CHOICE_ADAPTER_IMAGE_SIZE);
			TextView title = (TextView) view.findViewById(R.id.dotchi_package_title);
			title.setText(item.getItemTitle());
			TextView desc = (TextView) view.findViewById(R.id.dotchi_package_description);
			desc.setText(item.getItemContent());
			view.setSelected(item.isSelected());
			ImageView checkmark = (ImageView) view.findViewById(R.id.checkmark_if_selected);
			if (item.isSelected())	
				checkmark.setVisibility(View.VISIBLE);
			else
				checkmark.setVisibility(View.INVISIBLE);
			return view;
		}
		
	}
}
