package com.dotchi1.backend;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.dotchi1.R;
import com.dotchi1.model.FriendPageFriendItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

public class DialogFriendsAdapter extends ArrayAdapter<FriendPageFriendItem> {

	private Context context;
	private ArrayList<FriendPageFriendItem> objects;
	
	public DialogFriendsAdapter(Context context, int resource) {
		this(context, resource, null);
	}
	public DialogFriendsAdapter(Context context, int resource, List<FriendPageFriendItem> objects)	{
		super(context, resource, objects);
		this.context = context;
		this.objects = new ArrayList<FriendPageFriendItem>(objects);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageLoader imageLoader = ImageLoader.getInstance();
		DisplayImageOptions options = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.displayer(new SimpleBitmapDisplayer())
			.build();
		FriendPageFriendItem friendItem = objects.get(position);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = convertView;
		if (view == null)	{
			view = inflater.inflate(R.layout.dialog_friend_item, null);
		}
		ImageView image = (ImageView) view.findViewById(R.id.dialog_friend_image);
		imageLoader.displayImage(friendItem.getHeadImage(), image, options);
		return view;
	}

	
	
}
