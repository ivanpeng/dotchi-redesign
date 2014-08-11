package com.dotchi1.backend;

import android.view.View;

public class ToggleClickListener implements View.OnClickListener	{

	// We have current state so that we can pull from this later; when we send the intent, we can keep track of the data
	private int currentState;
	
	public ToggleClickListener(int currentState) {
		this.currentState = currentState;
	}
	
	@Override
	public void onClick(View v) {
		if (currentState == 0)	{
			currentState = 1;
			v.setSelected(true);
		} else	{
			currentState = 0;
			v.setSelected(false);
		}
	}

	public int getCurrentState() {
		return currentState;
	}

	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}
	
}