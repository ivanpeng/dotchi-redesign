package com.dotchi1;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;

import com.dotchi1.GameSettingActivity.PassDownData;
import com.dotchi1.backend.PassUpData;
import com.dotchi1.model.GameCardItem;

public class GameSettingDateTimeFragment extends Fragment implements PassDownData{

	PassUpData passUpData;
	
	DatePicker datePicker;
	TimePicker timePicker;
	ListView gameCards;
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		passUpData = (PassUpData) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_game_setting_datetime, container,false);
		
		return view;
	}

	@Override
	public void onNewCardCreated(GameCardItem item) {
		// Wire to adapter for list here.
	}

	@Override
	public String getDotchiDate() {
		// This is not necessary; alternatively, we could just pass up 0000-00-00 00:00
		return null;
	}

}
