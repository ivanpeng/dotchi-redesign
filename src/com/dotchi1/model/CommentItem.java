package com.dotchi1.model;

public class CommentItem {

	private String headImage;
	private String userName;
	private String comment;
	private String createTime;
	
	public CommentItem() {}
	
	public CommentItem(String headImage, String userName, String createTime,
			String comment) {
		this.headImage = headImage;
		this.userName = userName;
		this.createTime = createTime;
		this.comment = comment;
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
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}
