package com.dotchi1;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class NewNotificationsFragment extends Fragment{

	public static final int NOTIFICATION_ID = -1;
	
	private TextView blankView;
	private ListView notificationsList;
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.new_notification_fragment, null);
		
		blankView = (TextView) view.findViewById(R.id.no_notifications);
		notificationsList = (ListView) view.findViewById(R.id.notifications_list);
		// Programming flow goes: create fragment view, call notification manager to clear notifications (if present);
		// 
		NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
		
		// Grab data from system preferences
		SharedPreferences preferences = getActivity().getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		String json = preferences.getString("NOTIFICATIONS_JSON", "");
		// Map to JSON. If not there, we hide the list view
		if (json.length() == 0)
			notificationsList.setVisibility(View.INVISIBLE);
		// TODO: Need a background service to write and update the JSON String
		return view;
	}

}
