package com.dotchi1.model;

public class MoodItem {

	String headImage;
	String userName;
	int moodTypeId;
	
	public MoodItem()	{}
	
	public MoodItem(String headImage, String userName, int moodTypeId) {
		this.headImage = headImage;
		this.userName = userName;
		this.moodTypeId = moodTypeId;
	}
	public String getHeadImage() {
		return headImage;
	}
	public void setHeadImage(String headImage) {
		this.headImage = headImage;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getMoodTypeId() {
		return moodTypeId;
	}
	public void setMoodTypeId(int moodTypeId) {
		this.moodTypeId = moodTypeId;
	}
}
