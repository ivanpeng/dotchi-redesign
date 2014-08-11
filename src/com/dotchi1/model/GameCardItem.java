package com.dotchi1.model;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;

public class GameCardItem implements Serializable{

	private int gameItemId;
	private String itemImage;
	private String itemTitle;
	private String itemContent;
	private String isDate;

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
	
	public GameCardItem(int gameItemId,String itemImage, String itemTitle, String itemContent, String isDate)	{
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
	public String getIsDate() {
		return isDate;
	}
	public void setIsDate(String isDate) {
		this.isDate = isDate;
	}


	@Override
	public String toString() {
		return "GameCardItem [itemImage=" + itemImage + ", itemTitle="
				+ itemTitle + ", itemContent=" + itemContent + "]";
	}
	
	
}
