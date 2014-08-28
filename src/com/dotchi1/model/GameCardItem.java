package com.dotchi1.model;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.dotchi1.backend.json.BooleanDeserializer;

public class GameCardItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	protected int gameItemId;
	protected String itemImage;
	protected String itemTitle;
	protected String itemContent;
	@JsonDeserialize(using = BooleanDeserializer.class)
	protected boolean isDate;

	/**
	 * This means whether or not it's a local file uri
	 */

	public GameCardItem()	{}
	
	
	public GameCardItem(String itemImage, String itemTitle, String itemContent)	{
		super();
		this.itemImage = itemImage;
		this.itemTitle = itemTitle;
		this.itemContent = itemContent;
	}
	
	public GameCardItem(int gameItemId,String itemImage, String itemTitle, String itemContent)	{
		super();
		this.gameItemId = gameItemId;
		this.itemImage = itemImage;
		this.itemTitle = itemTitle;
		this.itemContent = itemContent;
	}
	
	public GameCardItem(int gameItemId,String itemImage, String itemTitle, String itemContent, boolean isDate)	{
		super();
		this.gameItemId = gameItemId;
		this.itemImage = itemImage;
		this.itemTitle = itemTitle;
		this.itemContent = itemContent;
		this.isDate = isDate;
	}
	
	public String getItemImage() {
		return itemImage;
	}
	public void setItemImage(String itemImage) {
		this.itemImage = itemImage;
	}
	public String getItemTitle() {
		return itemTitle;
	}
	public void setItemTitle(String itemTitle) {
		this.itemTitle = itemTitle;
	}
	public String getItemContent() {
		return itemContent;
	}
	public void setItemContent(String itemContent) {
		this.itemContent = itemContent;
	}
	public int getGameItemId() {
		return gameItemId;
	}
	public void setGameItemId(int gameItemId) {
		this.gameItemId = gameItemId;
	}
	public boolean getIsDate() {
		return isDate;
	}
	public void setIsDate(boolean isDate) {
		this.isDate = isDate;
	}


	@Override
	public String toString() {
		return "GameCardItem [itemImage=" + itemImage + ", itemTitle="
				+ itemTitle + ", itemContent=" + itemContent + "]";
	}
	
	
}
