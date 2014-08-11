package com.dotchi1.model;

import android.os.Parcel;

public class FriendPageSectionItem implements FriendPageItem{

	private int sectionPosition;
	private String sectionName;
	private int imageResId;
	private int count;
	
	public FriendPageSectionItem()	{
	}
	
	public FriendPageSectionItem(int sectionPosition, String sectionName, int imageResId, int count)	{
		this.sectionPosition = sectionPosition;
		this.sectionName = sectionName;
		this.imageResId = imageResId;
		this.count = count;
	}
	
	@Override
	public boolean isSelected() {
		return false;
	}

	@Override
	public void setSelected(boolean isSelected) {
		// Do nothing cause not necessary
	}

	public int getSectionPosition() {
		return sectionPosition;
	}

	public void setSectionPosition(int sectionPosition) {
		this.sectionPosition = sectionPosition;
	}

	public int getImageResId() {
		return imageResId;
	}

	public void setImageResId(int imageResId) {
		this.imageResId = imageResId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	@Override
	public String toString() {
		return "FriendPageSectionItem [sectionPosition=" + sectionPosition
				+ ", sectionName=" + sectionName + ", imageResId=" + imageResId
				+ ", count=" + count + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// NULL because we don't use this.
	}

}
