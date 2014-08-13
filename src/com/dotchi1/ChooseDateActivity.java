package com.dotchi1;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.AlertDialog.Builder;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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

public class ChooseDateActivity extends Activity {
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
	
	protected void chooseTimeDialog(final Date date)	{
		d = null;
		final Calendar cal = Calendar.getInstance();
		
		View timePickerView = getLayoutInflater().inflate(R.layout.time_picker_view, null);

		// This function opens up a dialog and creates a date
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d("Calendar", "Entered Here!");
					h = ;
					m = ;
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
			//objects.add(object);
			Log.d("Calendar", "Added date " +object.toString());
			super.add(object);
		}

		@Override
		public void remove(Date object) {
			//objects.remove(object);
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
