package com.dotchi1.backend;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import com.dotchi1.CommentActivity;
import com.dotchi1.EventChoicesActivity;
import com.dotchi1.GameActivity;
import com.dotchi1.R;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.BaseFeedData;
import com.dotchi1.model.VoteItem;

public class MainFeedAdapter extends ArrayAdapter<BaseFeedData>{

	private LiteImageLoader imageLoader;
	
	public MainFeedAdapter(Context context, int textViewResourceId,
			List<BaseFeedData> objects, LiteImageLoader imageLoader) {
		super(context, 0, objects);
		this.imageLoader = imageLoader;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int stubloader = R.drawable.default_profile_pic;
		final BaseFeedData item = getItem(position);
		View view = convertView;
		if (view == null)	{
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.main_feed_item, null);
		}
		// Find Views first
		// Layouts
		final RelativeLayout imageLayout= (RelativeLayout) view.findViewById(R.id.image_layout);
		final RelativeLayout onClickLayout = (RelativeLayout) view.findViewById(R.id.on_click_layout);
		final LinearLayout imageDetailsLayout = (LinearLayout) view.findViewById(R.id.image_details_layout);
		onClickLayout.setVisibility(View.GONE);
		imageDetailsLayout.setVisibility(View.VISIBLE);

		ImageView headImage = (ImageView) view.findViewById(R.id.main_feed_head);
		ImageView topPicture = (ImageView) view.findViewById(R.id.top_picture);
		TextView feedTitle = (TextView) view.findViewById(R.id.feed_title);
		TextView numberFriends = (TextView) view.findViewById(R.id.number_of_friends);
		TextView datePosted = (TextView) view.findViewById(R.id.date_posted);
		
		Button commentButton = (Button) view.findViewById(R.id.comment_button);
		Button playButton = (Button) view.findViewById(R.id.play_button);
		Button detailsButton = (Button) view.findViewById(R.id.details_button);
		
//		int height = screenWidth*2/3;
//		LayoutParams lp = (LayoutParams) imageLayout.getLayoutParams();
//		lp.height = height;
//		imageLayout.setLayoutParams(lp);
//		RelativeLayout relLayout = (RelativeLayout) view.findViewById(R.id.relativeLayout1);
//		LayoutParams relParams = (LayoutParams) relLayout.getLayoutParams();
//		relParams.setMargins(-4, height-105, 0, 0);
		
		// Populate views
		//TODO: determine what type of scale the image needs
		imageLoader.DisplayImage(item.getHeadImage(), stubloader, headImage, 150);
		List<VoteItem> voteItems = item.getVoteItem();
		if (voteItems != null && voteItems.size() > 0)
			imageLoader.DisplayImage(voteItems.get(0).getItemImage(), stubloader, topPicture, 250);
		else
			topPicture.setImageResource(stubloader);
		// Set titles 
		feedTitle.setText(item.getGameTitle());
		//numberFriends.setText();
		datePosted.setText(item.getEventTime());
		
		// buttons
		final Context context = getContext();
		commentButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, CommentActivity.class);
				// put extras
				intent.putExtra("item", item);
				context.startActivity(intent);
			}
		});
		playButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, GameActivity.class);
				Toast.makeText(context, "Start Game", Toast.LENGTH_LONG).show();
			}
		});
		detailsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, EventChoicesActivity.class);
				intent.putExtra("vote_items", new ArrayList<VoteItem>(item.getVoteItem()));
				//Toast.makeText(context, "Show Details", Toast.LENGTH_LONG).show();
				context.startActivity(intent);
			}
		});
		return view;
	}
	

}
