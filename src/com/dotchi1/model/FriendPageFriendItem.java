package com.dotchi1.model;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import android.os.Parcel;
import android.os.Parcelable;

import com.dotchi1.backend.json.BooleanDeserializer;

public class FriendPageFriendItem implements FriendPageItem {

	
	private long dotchiId;
	private String headImage;
	private String userName;
	@JsonDeserialize(using= BooleanDeserializer.class)
	private boolean isSetupDotchi;
	
	private boolean isSelected = false;
	
	public FriendPageFriendItem()	{
	}
	
	public FriendPageFriendItem(long dotchiId, String headImage, String userName)	{
		this.dotchiId = dotchiId;
		this.headImage = headImage;
		this.userName = userName;
	}
	
	public FriendPageFriendItem(long dotchiId, String headImage, String userName, boolean isSetupDotchi)	{
		this.dotchiId = dotchiId;
		this.headImage = headImage;
		this.userName = userName;
		this.isSetupDotchi = isSetupDotchi;
	}
	
	public FriendPageFriendItem(Parcel in)	{
		this.dotchiId = in.readLong();
		this.headImage = in.readString();
		this.userName = in.readString();
		this.isSetupDotchi = (in.readInt() ==1);
		this.isSelected = (in.readInt() == 1);
		
	}
	
	@Override
	public boolean isSelected() {
		return isSelected;
	}
	
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public long getDotchiId() {
		return dotchiId;
	}

	public void setDotchiId(long dotchiId) {
		this.dotchiId = dotchiId;
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

	public boolean getIsSetupDotchi()	{
		return isSetupDotchi;
	}
	
	public void setIsSetupDotchi(boolean isSetupDotchi)	{
		this.isSetupDotchi = isSetupDotchi;
	}
	
	@Override
	public String toString() {
		return "FriendPageFriendItem [dotchiId=" + dotchiId + ", headImage="
				+ headImage + ", userName=" + userName + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.dotchiId);
		dest.writeString(this.headImage);
		dest.writeString(this.userName);
		dest.writeInt(this.isSetupDotchi? 1:0);
		dest.writeInt(this.isSelected?1:0);
		
	}
	public static final Parcelable.Creator<FriendPageFriendItem> CREATOR = new Parcelable.Creator<FriendPageFriendItem>()	{

		@Override
		public FriendPageFriendItem createFromParcel(Parcel source) {
			return new FriendPageFriendItem(source);
		}

		@Override
		public FriendPageFriendItem[] newArray(int size) {
			return new FriendPageFriendItem[size];
		}
		
	};

}
