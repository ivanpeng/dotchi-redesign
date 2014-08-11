package com.dotchi1;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.devsmart.android.ui.HorizontalListView;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.VoteItem;
import com.dotchi1.model.VoteItem.MedalType;

public class EventChoicesActivity extends ActionBarActivity {

	private static final int EVENT_CHOICES_ACTIVITY_IMAGE_SIZE = 150;
	
	private ListView detailChoiceListView;
	private ArrayList<VoteItem> voteItems;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_choices);
		
		// Set Views
		detailChoiceListView = (ListView) findViewById(R.id.choice_detail_list);
		voteItems = (ArrayList<VoteItem>) getIntent().getSerializableExtra("vote_items");
		if (voteItems == null)	{
			// do something here, maybe throw an error
		}
		DetailChoiceAdapter adapter = new DetailChoiceAdapter(this, 0, voteItems);
		detailChoiceListView.setAdapter(adapter);
	}

	class DetailChoiceAdapter extends ArrayAdapter<VoteItem> 	{

		private Context context;
		private ArrayList<VoteItem>objects;
		private LiteImageLoader imageLoader;
		
		public DetailChoiceAdapter(Context context, int textViewResourceId,
				List<VoteItem> objects) {
			super(context, textViewResourceId, objects);
			this.context = context;
			this.objects = new ArrayList<VoteItem>(objects);
			this.imageLoader = new LiteImageLoader(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			VoteItem item = objects.get(position);
			Log.d("EventChoicesActivity", item.toString());
			View view = convertView;
			if (view == null)	{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.choice_detail_item, null);
			}
			ImageView votePic = (ImageView) view.findViewById(R.id.choice_detail_image);
			imageLoader.DisplayImage(item.getItemImage(), R.drawable.new_feed_photo_default, votePic, EVENT_CHOICES_ACTIVITY_IMAGE_SIZE);
			
			TextView title = (TextView) view.findViewById(R.id.choice_detail_title);
			title.setText(item.getItemTitle());
			TextView desc = (TextView) view.findViewById(R.id.choice_detail_description);
			desc.setText(item.getItemContent());
			// Medal
			ImageView medal = (ImageView) view.findViewById(R.id.choice_detail_medal);
			if (item.getMedals() == MedalType.NONE)	
				medal.setVisibility(View.GONE);
			else if (item.getMedals() == MedalType.GOLD)
				medal.setImageResource(R.drawable.photo_roll_gold);
			else if (item.getMedals() == MedalType.SILVER)
				medal.setImageResource(R.drawable.photo_roll_silver);
			else if (item.getMedals() == MedalType.COPPER)
				medal.setImageResource(R.drawable.photo_roll_bronze);
			
			// TODO: friends
			HorizontalListView friends = (HorizontalListView) view.findViewById(R.id.choice_detail_friends_list);
			//friends.setAdapter(new ArrayAdapter );
			ProgressBar progress = (ProgressBar) view.findViewById(R.id.choice_detail_progress);
			progress.setProgress((int)item.getPercent());
			TextView progressText = (TextView) view.findViewById(R.id.choice_detail_progress_text);
			progressText.setText(String.valueOf(item.getVotes()));
			
			return view;
		}
		
	}
	
	class FriendAdapter extends ArrayAdapter<VoteItem>	{

		public FriendAdapter(Context context, int textViewResourceId,
				List<VoteItem> objects) {
			super(context, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
		}
	}

}
