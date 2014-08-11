package com.dotchi1.model;

import java.util.Date;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.dotchi1.backend.json.BooleanDeserializer;
import com.dotchi1.backend.json.DateDeserializer;
import com.dotchi1.backend.json.DotchiTypeDeserializer;

public class BaseActivityItem {

	protected int activityId;
	protected String eventTitle;
	@JsonDeserialize(using = DateDeserializer.class)
	protected Date eventTime;
	protected String headImage;
	protected String userName;

	@JsonDeserialize(using = BooleanDeserializer.class)
	protected boolean isOfficial;
	@JsonDeserialize(using = BooleanDeserializer.class)
	protected boolean isPersonal;
	@JsonDeserialize(using = DateDeserializer.class)
	protected Date endTime;
	@JsonDeserialize(using = BooleanDeserializer.class)
	protected boolean isSecret;
	@JsonDeserialize(using = DotchiTypeDeserializer.class)
	protected ActivityType activityType;
	
	protected String dotchiType;
	protected String gameTitle;
	protected String joinRatio;
	protected String joinPercent;
	//@JsonDeserialize(using = DateDeserializer.class)
	protected String dotchiTime;
	protected String dotchiRatio;
	protected float dotchiPercent;
	
	protected int gameId;
	protected int joinStage;
	@JsonDeserialize(using = BooleanDeserializer.class)
	protected boolean isRead;
	
	protected List<GameCardItem> highVote;
	
	public enum ActivityType	{
		FRIEND(0),
		OFFICIAL(1),
		FRIEND_INVITE(2),
		INVITE(3),
		MESSAGE(4),
		FINAL_INVITE(5);
		
		
		private final int id;
	    
	    ActivityType (int id) { 
	    	this.id = id; 
	    }
	    
	    public int getValue() { 
	    	return id; 
	    }
	}


	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public String getEventTitle() {
		return eventTitle;
	}

	public void setEventTitle(String eventTitle) {
		this.eventTitle = eventTitle;
	}

	public Date getEventTime() {
		return eventTime;
	}

	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}

	public String getHeadImage() {
		return headImage;
	}

	public void setHeadImage(String headImage) {
		this.headImage = headImage;
	}

	public boolean getIsOfficial() {
		return isOfficial;
	}

	public void setIsOfficial(boolean isOfficial) {
		this.isOfficial = isOfficial;
	}

	public boolean getIsPersonal() {
		return isPersonal;
	}

	public void setIsPersonal(boolean isPersonal) {
		this.isPersonal = isPersonal;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public boolean getIsSecret() {
		return isSecret;
	}

	public void setIsSecret(boolean isSecret) {
		this.isSecret = isSecret;
	}

	public ActivityType getActivityType() {
		return activityType;
	}

	public void setActivityType(ActivityType activityType) {
		this.activityType = activityType;
	}

	/*
	public DotchiType getDotchiType() {
		return dotchiType;
	}

	public void setDotchiType(DotchiType dotchiType) {
		this.dotchiType = dotchiType;
	}
	*/

	public String getDotchiTime() {
		return dotchiTime;
	}

	public String getDotchiType() {
		return dotchiType;
	}

	public void setDotchiType(String dotchiType) {
		this.dotchiType = dotchiType;
	}

	public void setDotchiTime(String dotchiTime) {
		this.dotchiTime = dotchiTime;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDotchiRatio() {
		return dotchiRatio;
	}

	public void setDotchiRatio(String dotchiRatio) {
		this.dotchiRatio = dotchiRatio;
	}

	public float getDotchiPercent() {
		return dotchiPercent;
	}

	public void setDotchiPercent(float dotchiPercent) {
		this.dotchiPercent = dotchiPercent;
	}

	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public int getJoinStage() {
		return joinStage;
	}

	public void setJoinStage(int joinStage) {
		this.joinStage = joinStage;
	}

	public boolean getIsRead() {
		return isRead;
	}

	public void setIsRead(boolean isRead) {
		this.isRead = isRead;
	}

	public String getGameTitle() {
		return gameTitle;
	}

	public void setGameTitle(String gameTitle) {
		this.gameTitle = gameTitle;
	}

	public String getJoinRatio() {
		return joinRatio;
	}

	public void setJoinRatio(String joinRatio) {
		this.joinRatio = joinRatio;
	}

	public String getJoinPercent() {
		return joinPercent;
	}

	public void setJoinPercent(String joinPercent) {
		this.joinPercent = joinPercent;
	}

	public List<GameCardItem> getHighVote() {
		return highVote;
	}

	public void setHighVote(List<GameCardItem> highVote) {
		this.highVote = highVote;
	}

	@Override
	public String toString() {
		return "BaseActivity [activityId=" + activityId + ", eventTitle="
				+ eventTitle + ", eventTime=" + eventTime + ", headImage="
				+ headImage + ", userName=" + userName + ", isOfficial="
				+ isOfficial + ", isPersonal=" + isPersonal + ", endTime="
				+ endTime + ", isSecret=" + isSecret + ", activityType="
				+ activityType + ", dotchiType=" + dotchiType + ", gameTitle="
				+ gameTitle + ", joinRatio=" + joinRatio + ", joinPercent="
				+ joinPercent + ", dotchiTime=" + dotchiTime + ", dotchiRatio="
				+ dotchiRatio + ", dotchiPercent=" + dotchiPercent
				+ ", isRead=" + isRead + ", gameId=" + gameId + ", joinStage=" 
				+ joinStage + "]";
	}
	
}
