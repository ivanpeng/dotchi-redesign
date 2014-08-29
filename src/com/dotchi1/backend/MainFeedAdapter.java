package com.dotchi1.backend;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

import com.dotchi1.CommentActivity;
import com.dotchi1.EventChoicesActivity;
import com.dotchi1.GameActivity;
import com.dotchi1.R;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.BaseFeedData;
import com.dotchi1.model.GameCardItem;
import com.dotchi1.model.VoteItem;

public class MainFeedAdapter extends ArrayAdapter<BaseFeedData>{

	private static LiteImageLoader imageLoader;
	
	public MainFeedAdapter(Context context, int textViewResourceId,
			List<BaseFeedData> objects, LiteImageLoader imageLoader) {
		super(context, 0, objects);
		this.imageLoader = imageLoader;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final BaseFeedData item = getItem(position);
		//View view = convertView;
		View view = makeCommentView(getContext(), item);
		// Find Views first
		// Layouts
		final RelativeLayout imageLayout= (RelativeLayout) view.findViewById(R.id.image_layout);
		final RelativeLayout onClickLayout = (RelativeLayout) view.findViewById(R.id.on_click_layout);
		final LinearLayout imageDetailsLayout = (LinearLayout) view.findViewById(R.id.image_details_layout);
		onClickLayout.setVisibility(View.GONE);
		imageDetailsLayout.setVisibility(View.VISIBLE);

		Button commentButton = (Button) view.findViewById(R.id.comment_button);
		Button playButton = (Button) view.findViewById(R.id.play_button);
		Button detailsButton = (Button) view.findViewById(R.id.details_button);
		
		commentButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), CommentActivity.class);
				// put extras
				intent.putExtra("item", item);
				getContext().startActivity(intent);
			}
		});
		if (item.getIsPlay())	
			playButton.setVisibility(View.GONE);
		else	{
			playButton.setVisibility(View.VISIBLE);
			playButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getContext(), GameActivity.class);
					// put extras
					intent.putExtra("game_id", item.getGameId());
					intent.putExtra("game_title", item.getGameTitle());
					intent.putExtra("dotchi_time", item.getDotchiTime());
					intent.putExtra("is_personal", item.getIsPersonal());
					intent.putExtra("is_secret", item.getIsSecret());
					intent.putExtra("is_official", false);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					((Activity)getContext()).startActivity(intent);
				}
			});
		}
		detailsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), EventChoicesActivity.class);
				intent.putExtra("vote_items", new ArrayList<VoteItem>(item.getVoteItem()));
				//Toast.makeText(context, "Show Details", Toast.LENGTH_LONG).show();
				getContext().startActivity(intent);
			}
		});
		return view;
	}
	
	public static View makeCommentView(Context context, BaseFeedData item)	{
		int stubloader = R.drawable.default_profile_pic;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.main_feed_item, null);
		ImageView headImage = (ImageView) view.findViewById(R.id.main_feed_head);
		ImageView topPicture = (ImageView) view.findViewById(R.id.top_picture);
		TextView feedTitle = (TextView) view.findViewById(R.id.feed_title);
		TextView numberFriends = (TextView) view.findViewById(R.id.number_of_friends);
		TextView datePosted = (TextView) view.findViewById(R.id.date_posted);
		// Populate views
		//TODO: determine what type of scale the image needs
		imageLoader.DisplayImage(item.getHeadImage(), headImage);
		List<VoteItem> voteItems = item.getVoteItem();
		if (voteItems != null && voteItems.size() > 0)
			imageLoader.DisplayImage(voteItems.get(0).getItemImage(), topPicture);
		else
			topPicture.setImageResource(stubloader);
		// Set titles 
		feedTitle.setText(item.getGameTitle());
		//numberFriends.setText();
		datePosted.setText(item.getEventTime());
		
		
		return view;
	}
	
	public static View makeEventView(Context context, GameCardItem item)	{
		// Need to compartmentalize this into a static function;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.date_image_layout, null);
		TextView yearView = (TextView) view.findViewById(R.id.date_image_year);
		TextView dateView = (TextView) view.findViewById(R.id.date_image_date);
		TextView dayOfWeekView = (TextView) view.findViewById(R.id.date_image_day_of_week);
		LinearLayout timeBox = (LinearLayout) view.findViewById(R.id.date_image_time_box);
		TextView hourView = (TextView) view.findViewById(R.id.date_image_hour);
		TextView minuteView = (TextView) view.findViewById(R.id.date_image_minute);
		TextView ampmView = (TextView) view.findViewById(R.id.date_image_ampm);
		
		dayOfWeekView.setText(item.getItemContent());
		Date d = tryParse(item.getItemTitle());
		if (d != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			// TODO: Convert this all to R.string afterwards
			yearView.setText(String.valueOf(cal.get(Calendar.YEAR))+ "年");
			dateView.setText(String.valueOf(cal.get(Calendar.MONTH)) + "月" + cal.get(Calendar.DAY_OF_MONTH) + "號");
			// TODO: check that this is necessary
			hourView.setText(String.valueOf(cal.get(Calendar.HOUR)));
			minuteView.setText(String.valueOf(cal.get(Calendar.MINUTE)));
			ampmView.setText(cal.get(Calendar.AM_PM) == Calendar.AM? "AM":"PM");
		}	else
			Log.w("DATE VIEW", "We've come across an erroneous date. Please check logs for date format");
		return view;
	}
	
	public static Date tryParse(String dateString)	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy'年'MM'月'dd'日' aa HH:mm", Locale.US);
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
		ArrayList<SimpleDateFormat> formatStrings = new ArrayList<SimpleDateFormat>();
		formatStrings.add(sdf);
		formatStrings.add(sdf2);
		for (SimpleDateFormat formatString : formatStrings)
	    {
	        try
	        {
	            return formatString.parse(dateString);
	        }
	        catch (ParseException e) {}
	    }

	    return null;
	}
	

}
