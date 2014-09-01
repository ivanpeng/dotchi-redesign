package com.dotchi1.backend;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dotchi1.R;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.VoteItem;
import com.dotchi1.model.VoteItem.MedalType;

/**
 * May need to put in another class.
 * @author Ivan
 *
 */
public class VoteItemAdapter extends ViewPagerAdapter	{

	private static final int VOTE_ITEM_ADAPTER_IMAGE_SIZE = 250;
	
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
		ImageView photo = (ImageView) view.findViewById(R.id.photo_image);
		imageLoader.DisplayImage(vItem.getItemImage(), R.drawable.photo_roll_default, photo, VOTE_ITEM_ADAPTER_IMAGE_SIZE);
		
		TextView photoTitle = (TextView) view.findViewById(R.id.photo_roll_title);
		photoTitle.setText(vItem.getItemTitle());
		
		TextView photoDescription = (TextView) view.findViewById(R.id.photo_description);
		photoDescription.setText(vItem.getItemContent());
		
		ProgressBar voteBar = (ProgressBar) view.findViewById(R.id.photo_tickets_progress);
		voteBar.setProgress((int)vItem.getPercent());
		
		TextView voteBarText = (TextView) view.findViewById(R.id.photo_tickets_progress_text);
		voteBarText.setText(String.valueOf((int)vItem.getPercent()) + "%");
		
		TextView fractionText = (TextView) view.findViewById(R.id.photo_fraction);
		String fraction = String.valueOf(position+1) + "/" + String.valueOf(objects.size());
		fractionText.setText(fraction);
		
		return view;
	}



}