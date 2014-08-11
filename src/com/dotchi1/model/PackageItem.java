package com.dotchi1.model;

public class PackageItem {

	private String itemImage;
	private String itemTitle;
	private String itemContent;
	private String itemExtra;
	
	private boolean isSelected = false;
	
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
	public String getItemExtra() {
		return itemExtra;
	}
	public void setItemExtra(String itemExtra) {
		this.itemExtra = itemExtra;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	
	
	
}
