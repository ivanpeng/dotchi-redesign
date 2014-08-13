package com.dotchi1.backend;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dotchi1.R;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.VoteItem;
import com.dotchi1.model.VoteItem.MedalType;

import de.passsy.holocircularprogressbar.HoloCircularProgressBar;

/**
 * May need to put in another class.
 * @author Ivan
 *
 */
public class VoteItemAdapter extends ViewPagerAdapter	{

	private static final int VOTE_ITEM_ADAPTER_IMAGE_SIZE = 150;
	
	private Context context;
	private ArrayList<VoteItem> objects;
	private LiteImageLoader imageLoader;
	
	public VoteItemAdapter(Context context, int textViewResourceId,
			List<VoteItem> objects, LiteImageLoader imageLoader) {
		//super(context, textViewResourceId, objects);
		this.context = context;
		this.objects = new ArrayList<VoteItem>(objects);
		this.imageLoader = imageLoader;
	}

	@Override
	public int getCount() {
		return objects.size();
	}

	@Override
	public View getView(int position, ViewPager pager) {
		View view = null;
		if (view == null)	{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.photo_roll_item, null);
		}
		VoteItem vItem = objects.get(position);
		// Populate
		//TODO need to crop imageView to 4:3
		ImageView photo = (ImageView) view.findViewById(R.id.photo_image);
		imageLoader.DisplayImage(vItem.getItemImage(), R.drawable.default_profile_pic, photo, VOTE_ITEM_ADAPTER_IMAGE_SIZE);
		ImageView medal = (ImageView) view.findViewById(R.id.top_choice_symbol);
		if (vItem.getMedals() == MedalType.NONE)	
			medal.setVisibility(View.INVISIBLE);
		else if (vItem.getMedals() == MedalType.GOLD)	
			medal.setImageResource(R.drawable.new_gold);
		else if (vItem.getMedals() == MedalType.SILVER)
			medal.setImageResource(R.drawable.new_silver);
		else // medal is copper
			medal.setImageResource(R.drawable.new_bronze);
		
		TextView photoTitle = (TextView) view.findViewById(R.id.photo_roll_title);
		photoTitle.setText(vItem.getItemTitle());
		
		TextView photoDescription = (TextView) view.findViewById(R.id.photo_description);
		photoDescription.setText(vItem.getItemContent());
		
		HoloCircularProgressBar voteBar = (HoloCircularProgressBar) view.findViewById(R.id.photo_tickets_progress);
		voteBar.setProgress((float)(vItem.getPercent()/100.0f));
		TextView voteCount = (TextView) view.findViewById(R.id.photo_num_tickets);
		voteCount.setText(String.valueOf(vItem.getVotes()) + " 票");
		return view;
	}



}