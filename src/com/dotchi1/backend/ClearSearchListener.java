package com.dotchi1.backend;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;

public class ClearSearchListener implements View.OnClickListener	{

	private Activity activity;
	// The resId describes the resource id of the textbox it's associated with, NOT its own view.
	private int resId;
	
	public ClearSearchListener(Activity activity, int resId)	{
		this.activity = activity;
		this.resId = resId;
	}
	
	@Override
	public void onClick(View v) {
		// Do it at root level
		EditText editText = (EditText) activity.findViewById(resId);
		editText.setText("");
	}
}
