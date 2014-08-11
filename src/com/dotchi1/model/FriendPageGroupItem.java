package com.dotchi1.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FriendPageGroupItem implements FriendPageItem {


	private long groupId;
	private String groupName;
	private String headImage;
	private int count;
	
	private boolean isSelected = false;
	
	public FriendPageGroupItem()	{
	}
	
	public FriendPageGroupItem(long groupId, String groupName, String headImage, int count)	{
		this.groupId = groupId;
		this.groupName = groupName;
		this.headImage = headImage;
		this.count = count;
	}
	
	public FriendPageGroupItem(Parcel in)	{
		groupId = in.readLong();
		groupName = in.readString();
		headImage = in.readString();
		count = in.readInt();
		isSelected = (in.readInt() ==1);
	}
	
	@Override
	public boolean isSelected() {
		return isSelected;
	}

	@Override
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public long getgroupId() {
		return groupId;
	}

	public void setgroupId(long groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getHeadImage() {
		return headImage;
	}

	public void setHeadImage(String headImage) {
		this.headImage = headImage;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "FriendPageGroupItem [groupId=" + groupId + ", groupName="
				+ groupName + ", headImage=" + headImage + ", count=" + count
				+ "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.groupId);
		dest.writeString(groupName);
		dest.writeString(headImage);
		dest.writeInt(count);
		dest.writeInt(isSelected?1:0);
	}
	public static final Parcelable.Creator<FriendPageGroupItem> CREATOR = new Parcelable.Creator<FriendPageGroupItem>()	{

		@Override
		public FriendPageGroupItem createFromParcel(Parcel source) {
			return new FriendPageGroupItem(source);
		}

		@Override
		public FriendPageGroupItem[] newArray(int size) {
			return new FriendPageGroupItem[size];
		}
		
	};

}
