package com.dotchi1.backend;

import java.util.List;

import com.dotchi1.model.GameCardItem;


public interface PassUpData{
	public void onDataPass(List<GameCardItem> data);
	
	public void notifyListSizeChanged(int newSize);
	
}