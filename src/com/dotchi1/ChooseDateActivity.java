package com.dotchi1;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.CalendarPickerView.OnDateSelectedListener;
import com.squareup.timessquare.CalendarPickerView.SelectionMode;

public class ChooseDateActivity extends ActionBarActivity implements OnClickListener {
	private CalendarPickerView calendar;
	private ListView dateListView;
	private ArrayList<Date> dates = new ArrayList<Date>();
	private ArrayAdapter<Date> adapter;
	
	private int h = 0, m = 0;
	private Date d;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_date);
		LayoutInflater inflater = getLayoutInflater();
		View actionbar = inflater.inflate(R.layout.menu_choose_date, null);
		TextView menuTitle = (TextView)actionbar.findViewById(R.id.menu_title);
		menuTitle.setText("Choose Dates");
		Button nextStep = (Button) actionbar.findViewById(R.id.complete_button);
		nextStep.setVisibility(View.VISIBLE);
		nextStep.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Package dates and send back to original activity
				Intent returnIntent = new Intent();
				// Before we set dates, we process for extra times.
				ArrayList<Date> processedDates = removeDuplicateDates(dates);
				returnIntent.putExtra("dates", processedDates);
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		});
		
		ImageView backButton = (ImageView) actionbar.findViewById(R.id.menu_back);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		getSupportActionBar().setCustomView(actionbar);
	    getSupportActionBar().setDisplayShowTitleEnabled(false);
	    getSupportActionBar().setDisplayShowCustomEnabled(true);
	   	getSupportActionBar().setDisplayShowHomeEnabled(false);
		
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
				Log.d("Calendar", "Before removing dates, list is "  + dates.toString());
				Iterator<Date> itr = dates.iterator();
				while (itr.hasNext())	{
					Date d = itr.next();
					if (isSameDay(date, d))
						itr.remove();
				}
				adapter.notifyDataSetChanged();
				Log.d("Calendar", "Dates after: " + dates.toString());
			}
			
			@Override
			public void onDateSelected(Date date) {
				dates.add(date);
				if (dates.size() == 1)	{
					adapter = new CalendarAdapter(ChooseDateActivity.this, dates);
					dateListView.setAdapter(adapter);
				} else	{
					adapter.notifyDataSetChanged();
				}
			}
		});
		
		// Button now to add all
		Button removeAll = (Button) findViewById(R.id.remove_all_times);
		removeAll.setOnClickListener(this);
		Button addAll = (Button) findViewById(R.id.add_times_to_all);
		addAll.setOnClickListener(this);
		
		
	}
	
	// This checks if the dates are the same (not necessarily the time)
	protected boolean isSameDay(Date d1, Date d2)	{
		Calendar c1 = Calendar.getInstance(), c2 = Calendar.getInstance();
		c1.setTime(d1);
		c2.setTime(d2);
		boolean v;
		if (c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) && c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
			 v = true;
		else
			v = false;
		return v;
	}
	
	protected void chooseTimeDialog(final Date date)	{
		d = null;
		final Calendar cal = Calendar.getInstance();
		
		final TimePicker timePickerView = (TimePicker) getLayoutInflater().inflate(R.layout.time_picker_layout, null);
		timePickerView.setIs24HourView(false);
		timePickerView.setCurrentHour(cal.get(Calendar.HOUR));
		timePickerView.setCurrentMinute(cal.get(Calendar.MINUTE));
		
		// This function opens up a dialog and creates a date
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					h = timePickerView.getCurrentHour();
					m = timePickerView.getCurrentMinute();
					cal.setTime(date);
					cal.set(Calendar.HOUR, h);
					cal.set(Calendar.MINUTE, m);
					d = cal.getTime();
					dates.add(d);
					adapter.notifyDataSetChanged();
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.setView(timePickerView);
		builder.show();

	}
	
	protected void addAllDialog(){
		// Similar to chooseTimeDialog, except different onClickListener
		final Calendar cal = Calendar.getInstance();
		
		final TimePicker timePickerView = (TimePicker) getLayoutInflater().inflate(R.layout.time_picker_layout, null);
		timePickerView.setIs24HourView(false);
		timePickerView.setCurrentHour(cal.get(Calendar.HOUR));
		timePickerView.setCurrentMinute(cal.get(Calendar.MINUTE));
		
		// This function opens up a dialog and creates a date
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ArrayList<Date> newDates = new ArrayList<Date>();
					for (Date date: dates)	{
						Calendar cal = Calendar.getInstance();
						cal.setTime(date);
						// Only if the time is set to be 00:00 will we add
						if (cal.get(Calendar.HOUR) == 0 && cal.get(Calendar.MINUTE) == 0)	{
							cal.set(Calendar.HOUR, timePickerView.getCurrentHour());
							cal.set(Calendar.MINUTE, timePickerView.getCurrentMinute());
							newDates.add(cal.getTime());
						}
					}
					dates.addAll(newDates);
					Log.d("Calendar", "Added " + newDates.toString());
					adapter.notifyDataSetChanged();
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.setView(timePickerView);
		builder.show();
	}
	
	protected ArrayList<Date> removeDuplicateDates(ArrayList<Date> unprocessedDates)	{
		Log.d("Process dates", "Input is " + unprocessedDates.toString());
		ArrayList<Date> processedDates = new ArrayList<Date>();
		int i = 0;
		while (i < unprocessedDates.size()-1){
			int j = i+1;
			if (!isSameDay(unprocessedDates.get(i), unprocessedDates.get(j)))	{
				// First iteration; if it is the first iteration, we just add the ith date
				processedDates.add(unprocessedDates.get(i));
				// Need to do a special check for last items. If we're comparing last items, add the last item as well.
				
			}
			else	{
				// Multiple dates; we're adding everything except the first one
				while (j < unprocessedDates.size()-1 && isSameDay(unprocessedDates.get(i), unprocessedDates.get(j))) {
					// Add to new list
					processedDates.add(unprocessedDates.get(j));
					j++;
				} 
			}
			if (j == unprocessedDates.size()-1)
				processedDates.add(unprocessedDates.get(j));
			i = j;
		}
		return processedDates;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId())	{
		case R.id.remove_all_times:
			// Nothing yet
			break;
		case R.id.add_times_to_all:
			Log.d("ChooseDateActivity", "Picked up add all button press");
			addAllDialog();
			break;
		}
	}
	
	class CalendarAdapter extends ArrayAdapter<Date>	{

		private Context context;
		private ArrayList<Date> objects;
		private SimpleDateFormat dateFormat, dayOfWeekFormat, timeFormat;
		
		public CalendarAdapter(Context context, ArrayList<Date> objects) {
			super(context, 0, objects);
			this.context = context;
			this.objects = objects;
			dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			dayOfWeekFormat = new SimpleDateFormat("E");
			timeFormat = new SimpleDateFormat("hh:mm a");
		}

		@Override
		public void add(Date object) {
			Log.d("Calendar", "Added date " +object.toString());
			super.add(object);
		}

		@Override
		public void remove(Date object) {
			super.remove(object);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Date item = objects.get(position);
			Calendar cal = Calendar.getInstance();
			cal.setTime(item);
			View view = null;
			// Don't recycle views
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (cal.get(Calendar.HOUR) == 0 && cal.get(Calendar.MINUTE) == 0)	{
				view = inflater.inflate(R.layout.date_text_item, null);
			
				TextView dateView = (TextView) view.findViewById(R.id.date_headline);
				dateView.setText(dateFormat.format(item));
				TextView dayOfWeekView = (TextView) view.findViewById(R.id.day_of_week);
				dayOfWeekView.setText(dayOfWeekFormat.format(item));
				// Add adapter for clicking add date
				Button addDateButton = (Button) view.findViewById(R.id.add_date_button);
				addDateButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// Pop up alert dialog and set date.
						chooseTimeDialog(item);
					}
	
	
				});
			} else	{
				view = inflater.inflate(R.layout.date_text_time_item, null);
				TextView timeView = (TextView) view.findViewById(R.id.time_header);
				timeView.setText(timeFormat.format(item));
				// Set delete button
				ImageView deleteButton = (ImageView) view.findViewById(R.id.delete_time);
				deleteButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						remove(item);
					}
				});
			}
			return view;
		}

		@Override
		public void notifyDataSetChanged() {
			// Before we notify dataset changed, we reorder the list
			Collections.sort(objects);
			super.notifyDataSetChanged();
		}
		
		
	}


}
