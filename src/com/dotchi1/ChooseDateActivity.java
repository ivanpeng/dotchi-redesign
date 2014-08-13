package com.dotchi1;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.CalendarPickerView.OnDateSelectedListener;
import com.squareup.timessquare.CalendarPickerView.SelectionMode;

public class ChooseDateActivity extends Activity {
	private CalendarPickerView calendar;
	private ListView dateListView;
	private ArrayList<Date> dates = new ArrayList<Date>();
	private ArrayAdapter<Date> adapter;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_date);
		
		dateListView = (ListView) findViewById(R.id.selected_dates_list);
		
		final Calendar nextYear = Calendar.getInstance();
		nextYear.add(Calendar.YEAR, 1);

		final Calendar lastYear = Calendar.getInstance();
		lastYear.add(Calendar.YEAR, -1);

		calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
		calendar.init(new Date(), nextYear.getTime()) //
			.inMode(SelectionMode.MULTIPLE); //
		
		calendar.setOnDateSelectedListener(new OnDateSelectedListener() {
			
			@Override
			public void onDateUnselected(Date date) {
				dates.remove(date);
				if (dates.size() == 0)
					adapter.notifyDataSetInvalidated();
				else
					adapter.notifyDataSetChanged();
			}
			
			@Override
			public void onDateSelected(Date date) {
				dates.add(date);
				if (dates.size() == 1)	{
					adapter = new CalendarAdapter(ChooseDateActivity.this, dates);
					dateListView.setAdapter(adapter);
				} else	{
					Log.d("choose date activity", "entered here!");
					adapter.notifyDataSetChanged();
				}
			}
		});
	}
	
	class CalendarAdapter extends ArrayAdapter<Date>	{

		private Context context;
		private ArrayList<Date> objects;
		
		public CalendarAdapter(Context context, ArrayList<Date> objects) {
			super(context, 0, objects);
			this.context = context;
			this.objects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null)	{
				LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				//view = inflater.inflate(R.layout.);
			}
			return super.getView(position, convertView, parent);
		}

		@Override
		public void notifyDataSetChanged() {
			// Before we notify dataset changed, we reorder the list
			Collections.sort(objects);
			super.notifyDataSetChanged();
		}
		
		
	}

}
