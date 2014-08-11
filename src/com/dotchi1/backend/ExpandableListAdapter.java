package com.dotchi1.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dotchi1.MakeGroupActivity;
import com.dotchi1.NewMainActivity;
import com.dotchi1.R;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.FriendPageFriendItem;
import com.dotchi1.model.FriendPageGroupItem;
import com.dotchi1.model.FriendPageItem;
import com.dotchi1.model.FriendPageSectionItem;
import com.dotchi1.view.RoundedImageView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private static final int EXPANDABLE_LIST_ADAPTER_IMAGE_SIZE = 40;
	
	public static final String TAG = "FriendSelectActivity";
	static final int GROUP_KEY = 0;
	static final int FRIEND_KEY = 1;
	private LiteImageLoader imageLoader;
	private Context context;
	private List<Integer> headers;
	private HashMap<Integer, FriendPageSectionItem> headerData;
	private HashMap<Integer, List<? extends FriendPageItem>> childData;
	private Filter filter;
	
	public ExpandableListAdapter(Context context, List<Integer> headers, HashMap<Integer, List<? extends FriendPageItem>> childData)	{
		this.context = context;
		this.headers = headers;
		this.childData = childData;
		this.imageLoader = new LiteImageLoader(context);
		headerData = new HashMap<Integer, FriendPageSectionItem>();
		headerData.put(GROUP_KEY, new FriendPageSectionItem(GROUP_KEY, "群組", 0, 0));
		headerData.put(FRIEND_KEY, new FriendPageSectionItem(FRIEND_KEY, "朋友", 0,0));
		
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		List<? extends FriendPageItem> childList = childData.get(groupPosition);
		if (childList == null)
			return null;
		return childData.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = null;
		final FriendPageItem i = (FriendPageItem) getChild(groupPosition, childPosition);
		if (i == null)
			Log.d(TAG, "Friend page item is null");
		if (view == null)	{
			view = inflater.inflate(R.layout.list_friend_item, null);
		}
		int stubLoader = R.drawable.friend_default_profile_picture;
		ToggleButton highlightedButton = (ToggleButton) view.findViewById(R.id.friend_invite_button);
		highlightedButton.setChecked(i.isSelected());
		if (i instanceof FriendPageGroupItem)	{
			// Group
			FriendPageGroupItem gi = (FriendPageGroupItem) i;
			RoundedImageView headImage = (RoundedImageView) view.findViewById(R.id.friend_head_image);
			imageLoader.DisplayImage(gi.getHeadImage(), stubLoader, headImage, EXPANDABLE_LIST_ADAPTER_IMAGE_SIZE);
			StringBuilder textToDisplay = new StringBuilder();
			// Build the string to display in the username place. It's a combination of group name followed by group count
			textToDisplay.append(gi.getGroupName() + " [" + gi.getCount() +"]");
			TextView groupText = (TextView) view.findViewById(R.id.item_name);
			groupText.setText(textToDisplay.toString());
			// Set onclickListener for group item
		} else	{
			// Friend layout
			FriendPageFriendItem fi = (FriendPageFriendItem) i;
			ImageView headImage = (ImageView) view.findViewById(R.id.friend_head_image);
			imageLoader.DisplayImage(fi.getHeadImage(), stubLoader, headImage, EXPANDABLE_LIST_ADAPTER_IMAGE_SIZE);
			TextView userName = (TextView) view.findViewById(R.id.item_name);
			userName.setText(fi.getUserName());
			//view.setOnClickListener(new ToggleClickListener(0));
		}
		return view;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		List<? extends FriendPageItem> childList = childData.get(groupPosition);
		if (childList == null)
			return 0;
		else
			return childList.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return headers.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return headers.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
			ViewGroup parent) {
		Integer key = (Integer) getGroup(groupPosition);
		FriendPageSectionItem si = headerData.get(key);
		if (key  == null)
			return convertView;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = convertView;
		view = inflater.inflate(R.layout.list_friend_section, null);
		
		TextView groupName = (TextView) view.findViewById(R.id.list_text);
		groupName.setText(si.getSectionName());
		
		ImageView addGroupButton = (ImageView) view.findViewById(R.id.add_group_button);
		if (groupPosition == GROUP_KEY)	{
			addGroupButton.setVisibility(View.VISIBLE);
			addGroupButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO: add selected friends to a group
					// Create new activity? Or do it in this page?
					Toast.makeText(context, "Make group button here", Toast.LENGTH_LONG).show();
					Intent intent = new Intent(context, MakeGroupActivity.class);
					Bundle bundle = new Bundle();
					ArrayList<FriendPageItem> friends = new ArrayList<FriendPageItem>(childData.get(FRIEND_KEY)); 
					bundle.putParcelableArrayList("friends", friends);
					intent.putExtras(bundle);
					((Activity)context).startActivityForResult(intent, NewMainActivity.CREATE_GROUP_REQ_CODE);
				}
			});
		} else	{
			addGroupButton.setVisibility(View.GONE);
		}
		return view;
		
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		if (groupPosition == 1)
			return true;
		else
			return false;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	
	public static class FriendsListCheckedListener implements OnCheckedChangeListener	{
		
		FriendPageItem item;
		
		public FriendsListCheckedListener(FriendPageItem item)	{
			this.item = item;
		}
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			item.setSelected(isChecked);
		}
	}
	
	public Filter getFilter()	{
		return filter;
	}
}