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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import at.technikum.mti.fancycoverflow.FancyCoverFlowAdapter;

import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.image.LiteImageLoader;

public class ChoosePhotoActivity extends ActionBarActivity {

	private static final int CHOOSE_PHOTO_ACTIVITY_SIZE = 500;
	
	private FancyCoverFlow fancyCoverFlow;
	private ArrayList<String> images;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_photo);
		fancyCoverFlow = (FancyCoverFlow) findViewById(R.id.pictures_cover_flow);
		
		String query = getIntent().getStringExtra("search_string");
		String rootUrl = getResources().getString(R.string.api_test_root_url);
		// TODO: start activity to render choices;
		new PostUrlTask()	{

			@Override
			protected void onPostExecute(String result) {
				result = processResult(result);
				try {
					JSONArray jo = new JSONObject(result).getJSONArray("data");
					// now we have a ton of photos; how do we display them?
					images = new ArrayList<String>();
					for (int i = 0; i < jo.length(); i++)	{
						images.add(jo.getJSONObject(i).getJSONObject("data").getString("pic"));
					}
					Log.d("cover flow", images.toString());
					fancyCoverFlow.setAdapter(new CoverFlowAdapter(ChoosePhotoActivity.this, images));
					fancyCoverFlow.setUnselectedAlpha(1.0f);
			        fancyCoverFlow.setUnselectedSaturation(0.0f);
			        fancyCoverFlow.setUnselectedScale(0.5f);
			        fancyCoverFlow.setSpacing(-100);
			        fancyCoverFlow.setMaxRotation(0);
			        fancyCoverFlow.setScaleDownGravity(0.2f);
			        fancyCoverFlow.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);
					fancyCoverFlow.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent, View v,
								int position, long id) {
							Log.d(TAG, "Sending string result back for item with position " + position);
							Intent returnIntent = new Intent();
							returnIntent.putExtra("image", images.get(position));
							//put the data sent in from original intent
							returnIntent.putExtra("position", getIntent().getIntExtra("position", 0));
							returnIntent.putExtra("search_string", getIntent().getStringExtra("search_string"));
							setResult(RESULT_OK, returnIntent);
							finish();
						}
					});
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		}.execute(rootUrl+ "/pic/get_google_pic", "search_string", query);

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	class CoverFlowAdapter extends FancyCoverFlowAdapter	{

		private Context context;
		private ArrayList<String> images;
		private LiteImageLoader imageLoader;
		
		public CoverFlowAdapter(Context context, List<String> images) {
			super();
			this.context = context;
			this.images = new ArrayList<String>(images);
			this.imageLoader = new LiteImageLoader(context);
		}
		
		@Override
		public int getCount() {
			return images.size();
		}

		@Override
		public Object getItem(int position) {
			return images.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getCoverFlowItem(int position, View convertView, ViewGroup parent) {
			ImageView imageView = null;
			if (convertView != null)
				imageView = (ImageView) convertView;
			else	{
				imageView = new ImageView(parent.getContext());
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setLayoutParams(new FancyCoverFlow.LayoutParams(600, 450));
			}
			imageLoader.DisplayImage(images.get(position), R.drawable.new_feed_photo_default, imageView, CHOOSE_PHOTO_ACTIVITY_SIZE);
			return imageView;
		}
		
	}
}
