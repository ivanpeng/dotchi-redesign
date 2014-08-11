package com.dotchi1;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.view.RoundedImageView;

public class ShowListActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_list);
		Intent intent = getIntent();
		// Get intent is called from multiple places; but the result is the same; photo, and name 
		//we don't bother with the post url task, and get the json string from intent
		String jsonStr = intent.getStringExtra("json");
		ListView lv = (ListView) findViewById(R.id.people_list);
		ArrayList<JSONObject> objects = new ArrayList<JSONObject>();
		try {
			JSONArray arr = new JSONObject(jsonStr).getJSONArray("data");
			for (int i = 0; i< arr.length(); i++)	{
				objects.add(arr.getJSONObject(i));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (objects != null && objects.size() > 0)	{
			SimplePersonAdapter adapter = new SimplePersonAdapter(this, R.layout.list_friend_item, objects);
			lv.setAdapter(adapter);
		}
		
		
	}


	class SimplePersonAdapter extends ArrayAdapter<JSONObject>	{

		private Context context;
		private ArrayList<JSONObject> objects;
		private LiteImageLoader imageLoader;
		
		public SimplePersonAdapter(Context context, int textViewResourceId,
				List<JSONObject> objects) {
			super(context, textViewResourceId, objects);
			this.context = context;
			this.objects = new ArrayList<JSONObject>(objects);
			this.imageLoader = new LiteImageLoader(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			JSONObject item = objects.get(position);
			if (view == null)	{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.list_friend_item, null);
			}
			ToggleButton check = (ToggleButton) view.findViewById(R.id.friend_invite_button);
			check.setVisibility(View.GONE);
			RoundedImageView image = (RoundedImageView) view.findViewById(R.id.friend_head_image);
			TextView name = (TextView) view.findViewById(R.id.item_name);
			name.setTextColor(getResources().getColor(R.color.black));
			try {
				imageLoader.DisplayImage(item.getString("head_image"), R.drawable.default_profile_pic, image, 120);
				name.setText(item.getString("user_name"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return view;
		}
		
		
	}

}
