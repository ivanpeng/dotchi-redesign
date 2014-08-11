package com.dotchi1.model;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.dotchi1.backend.json.BooleanDeserializer;

public class BaseFeedData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected int gameId;
	protected int activityId;
	protected String eventTitle;
	protected String eventTime;
	protected String headImage;
	protected String userName;
	@JsonDeserialize(using = BooleanDeserializer.class)
	protected boolean isOfficial;
	@JsonDeserialize(using = BooleanDeserializer.class)
	protected boolean isPersonal;
	protected String endTime;
	@JsonDeserialize(using = BooleanDeserializer.class)
	protected boolean isSecret;
	protected String gameTitle;
	protected int votesCount;
	protected int joinCount;
	protected int dotchiType;
	protected int msgCount;
	protected int newMsgCount;
	protected int moodCount;
	protected int isJoin;
	@JsonDeserialize(using = BooleanDeserializer.class)
	protected boolean isPlay;
	@JsonDeserialize(using = BooleanDeserializer.class)
	protected boolean isMood;

	protected int category;
	@JsonDeserialize(using= BooleanDeserializer.class)
	protected boolean isSendJoin;
	@JsonDeserialize(using = BooleanDeserializer.class)
	protected boolean isSendJoinNotice;
	protected List<VoteItem> voteItem;
	@JsonDeserialize(using = BooleanDeserializer.class)
	protected boolean checkIn;
	protected int activityType;
	
	protected String dotchiTime;
	
	public String getDotchiTime() {
		return dotchiTime;
	}
	public void setDotchiTime(String dotchiTime) {
		this.dotchiTime = dotchiTime;
	}
	public int getGameId() {
		return gameId;
	}
	public void setGameId(int gameId) {
		this.gameId = gameId;
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
	public String getEventTime() {
		return eventTime;
	}
	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
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
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public boolean getIsSecret() {
		return isSecret;
	}
	public void setIsSecret(boolean isSecret) {
		this.isSecret = isSecret;
	}
	public String getGameTitle() {
		return gameTitle;
	}
	public void setGameTitle(String gameTitle) {
		this.gameTitle = gameTitle;
	}
	public int getVotesCount() {
		return votesCount;
	}
	public void setVotesCount(int votesCount) {
		this.votesCount = votesCount;
	}
	public int getJoinCount() {
		return joinCount;
	}
	public void setJoinCount(int joinCount) {
		this.joinCount = joinCount;
	}
	public int getDotchiType() {
		return dotchiType;
	}
	public void setDotchiType(int dotchiType) {
		this.dotchiType = dotchiType;
	}
	public int getMsgCount() {
		return msgCount;
	}
	public void setMsgCount(int msgCount) {
		this.msgCount = msgCount;
	}
	public int getNewMsgCount() {
		return newMsgCount;
	}
	public void setNewMsgCount(int newMsgCount) {
		this.newMsgCount = newMsgCount;
	}
	public int getIsJoin() {
		return isJoin;
	}
	public void setIsJoin(int isJoin) {
		this.isJoin = isJoin;
	}
	public boolean getIsPlay() {
		return isPlay;
	}
	public void setIsPlay(boolean isPlay) {
		this.isPlay = isPlay;
	}
	public boolean getIsMood() {
		return isMood;
	}
	public void setIsMood(boolean isMood) {
		this.isMood = isMood;
	}
	public int getCategory() {
		return category;
	}
	public void setCategory(int category) {
		this.category = category;
	}
	public List<VoteItem> getVoteItem() {
		return voteItem;
	}
	public void setVoteItem(List<VoteItem> voteItem) {
		this.voteItem = voteItem;
	}	
	public int getMoodCount() {
		return moodCount;
	}
	public void setMoodCount(int moodCount) {
		this.moodCount = moodCount;
	}
	public boolean getIsSendJoin()	{
		return isSendJoin;
	}
	public void setIsSendJoin(boolean isSendJoin)	{
		this.isSendJoin = isSendJoin;
	}
	public boolean getIsSendJoinNotice()	{
		return isSendJoinNotice;
	}
	public void setIsSendJoinNotice(boolean isSendJoinNotice)	{
		this.isSendJoinNotice = isSendJoinNotice;
	}
	
	public boolean getCheckIn() {
		return checkIn;
	}
	public void setCheckIn(boolean checkIn) {
		this.checkIn = checkIn;
	}
	public int getActivityType() {
		return activityType;
	}
	public void setActivityType(int activityType) {
		this.activityType = activityType;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + activityId;
		result = prime * result + gameId;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseFeedData other = (BaseFeedData) obj;
		if (activityId != other.activityId)
			return false;
		if (gameId != other.gameId)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "BaseFeedData [gameId=" + gameId + ", activityId=" + activityId
				+ ", eventTitle=" + eventTitle + ", eventTime=" + eventTime
				+ ", headImage=" + headImage + ", userName=" + userName
				+ ", isOfficial=" + isOfficial + ", isPersonal=" + isPersonal
				+ ", endTime=" + endTime + ", isSecret=" + isSecret
				+ ", gameTitle=" + gameTitle + ", votesCount=" + votesCount
				+ ", joinCount=" + joinCount + ", dotchiType=" + dotchiType
				+ ", msgCount=" + msgCount + ", newMsgCount=" + newMsgCount
				+ ", moodCount=" + moodCount + ", isJoin=" + isJoin
				+ ", isPlay=" + isPlay + ", isMood=" + isMood + ", category="
				+ category + ", isSendJoin=" + isSendJoin
				+ ", isSendJoinNotice=" + isSendJoinNotice + ", voteItem="
				+ voteItem + ", checkIn=" + checkIn + ", activityType="
				+ activityType + ", dotchiTime=" + dotchiTime + "]";
	}

	
	
}
