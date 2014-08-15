
package com.dotchi1;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class CreateGameFirstActivity extends Activity {

	public static final int GET_DATES_REQ_CODE = 10;
	
	private TextView singleDateSelectView;
	private ListView multipleDateSelectView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_game_first);
		
		List<String> objects = new ArrayList<String>();
		objects.add("Select Single Date");
		objects.add("Have user select");
		// hint
		objects.add(getResources().getString(R.string.date_selection_prompt));
		
		singleDateSelectView = (TextView) findViewById(R.id.set_date_view);
		multipleDateSelectView = (ListView) findViewById(R.id.dates_list);
		
		// Set up adapter 
		HintAdapter adapter = new HintAdapter(this, objects, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spinner = (Spinner) findViewById(R.id.single_or_multiple_dates);
		spinner.setAdapter(adapter);
		spinner.setSelection(adapter.getCount());
		
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// Start respective activity
				if (position == 0)	{
					// Select single date
					multipleDateSelectView.setVisibility(View.GONE);
					// Show alert dialog 
				} else	{
					singleDateSelectView.setText("Selected Dates: ");
					multipleDateSelectView.setVisibility(View.VISIBLE);
					// Open multiple activities
					Intent intent = new Intent(CreateGameFirstActivity.this, ChooseDateActivity.class);
					startActivityForResult(intent, GET_DATES_REQ_CODE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {	
			}
		});
		
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GET_DATES_REQ_CODE)	{
			if (resultCode == RESULT_OK)	{
				// set list adapter
				ArrayList<Date> dates = (ArrayList<Date>) data.getSerializableExtra("dates");
				multipleDateSelectView.setAdapter(new DateAdapter(this, dates));
			}
		} else
			super.onActivityResult(requestCode, resultCode, data);
	}


	class HintAdapter extends ArrayAdapter<String>	{

	    public HintAdapter(Context context, List<String> objects) {
	        super(context, 0, 0, objects);
	    }
	 
	    public HintAdapter(Context context, List<String> objects, int resId) {
	        super(context, resId, 0, objects);
	    }
	 
	    @Override
	    public int getCount() {
	        // don't display last item. It is used as hint.
	        int count = super.getCount();
	        return count > 0 ? count - 1 : count;
	    }
	}
	
	class DateAdapter extends ArrayAdapter<Date>	{
		private Context context;
		private SimpleDateFormat sdf;
		
		public DateAdapter(Context context, List<Date>objects)	{
			super(context, 0, objects);
			this.context = context;
			sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			Date date = getItem(position);
			if (view == null)	{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(android.R.layout.simple_list_item_1, null);
			}
			TextView text = (TextView) view.findViewById(android.R.id.text1);
			text.setText(sdf.format(date));
			return view;
		}
		
		
	}


}
