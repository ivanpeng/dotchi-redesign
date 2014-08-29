
package com.dotchi1;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class CreateGameFirstActivity extends ActionBarActivity implements OnCheckedChangeListener{

	public static final int GET_DATES_REQ_CODE = 10;
	
	private TextView gameTitleView;
	
	// Arrange Meeting views
	private TextView singleDateSelectView;
	private ListView multipleDateSelectView;
	
	// Vote Views
	private TextView voteLimitView;
	private TextView replyDayView;
	
	// Variables
	private boolean isSecret;
	private boolean isPersonal;
	private boolean isOfficial = false;
	private String gameTitle = "";
	private String voteLimit = "0";
	private String replyDay = "15";
	private String dotchiType = "0";
	private String dotchiTime;
	private ArrayList<Date> dates;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_game_first);
		// Set up actionbar layout
		View actionbar = setupActionBar();
		getSupportActionBar().setCustomView(actionbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		// Before we continue, set which view is visible, and which view is gone\
		gameTitleView = (TextView) findViewById(R.id.game_title_text);
		Intent lastIntent = getIntent();
		dotchiType = lastIntent.getStringExtra("dotchiType");
		View arrangeMeetingLayout = findViewById(R.id.arrange_meeting_layout);
		View voteLayout = findViewById(R.id.vote_layout);
		if ("1".equals(dotchiType))	{
			// find arrange meeting layout, set that to gone, then set vote layout up.
			arrangeMeetingLayout.setVisibility(View.GONE);
			setupVoteLayout();
		} else	{
			// vote layout gone, arrange meeting set
			voteLayout.setVisibility(View.GONE);
			setupArrangeMeetingLayout();
		}
	}

	protected View setupActionBar()	{

		LayoutInflater inflater = getLayoutInflater();
		View actionbar = inflater.inflate(R.layout.menu_dotchi_package, null);
		ImageButton back = (ImageButton)actionbar.findViewById(R.id.back_home_button);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		TextView packageTitleView = (TextView) actionbar.findViewById(R.id.package_title);
		if (dotchiType.equals("0"))
			packageTitleView.setText("Arrange Meeting");
		else
			packageTitleView.setText("Vote");
		ImageButton forwardButton = (ImageButton) actionbar.findViewById(R.id.forward_button);
		forwardButton.setVisibility(View.VISIBLE);
		forwardButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO
				// If it's vote type, then before sending data forward, we grab the text values first;
				if (dotchiType.equals("1"))	{
					replyDay = (replyDayView.getText() != null && replyDayView.getText().length() >  0) ? "15": replyDayView.getText().toString();
					voteLimit = (voteLimitView.getText() != null && voteLimitView.getText().length() > 0) ? "0": voteLimitView.getText().toString();
				}
				// Bundle up, and then send to next step
				gameTitle = gameTitleView.getText().toString();
				Bundle bundle = bundleGameArgs();
				Intent intent = new Intent(CreateGameFirstActivity.this, CreateGameItemsActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});	    
		return actionbar;
	}
	
	protected void setupArrangeMeetingLayout()	{
		// First, preset values because we're in arrange meeting
		isPersonal = true;
		isSecret = false;
		voteLimit = "0";
		replyDay = "15";
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
					final DatePicker datePickerView = (DatePicker) getLayoutInflater().inflate(R.layout.date_picker_layout, null);
					datePickerView.setCalendarViewShown(false);
					//datePickerView.setMinDate(Calendar.getInstance().getTimeInMillis());
					AlertDialog dialog = new AlertDialog.Builder(CreateGameFirstActivity.this)
							// Set title?
							.setView(datePickerView)
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// clicked ok; set date
									int year = datePickerView.getYear();
									int month = datePickerView.getMonth();
									int day = datePickerView.getDayOfMonth();
									Calendar cal = Calendar.getInstance();
									cal.set(Calendar.YEAR, year);
									cal.set(Calendar.MONTH, month);
									cal.set(Calendar.DAY_OF_MONTH, day);
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
									dotchiTime = sdf.format(cal.getTime());
									singleDateSelectView.setText(dotchiTime);
								}
							})
							.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							})
							.create();
					dialog.show();
				} else if (position == 1){
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
		
		// 
	}
	
	protected void setupVoteLayout()	{
		CheckBox isSecretBox = (CheckBox) findViewById(R.id.is_secret_box);
		isSecretBox.setOnCheckedChangeListener(this);
		CheckBox isPersonalBox = (CheckBox) findViewById(R.id.is_personal_box);
		isPersonalBox.setOnCheckedChangeListener(this);
		
		voteLimitView = (TextView) findViewById(R.id.vote_limit_text_box);
		replyDayView = (TextView) findViewById(R.id.day_limit_text_box);
		
	}
	
	protected Bundle bundleGameArgs()	{
		String dotchiId = getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE).getString("DOTCHI_ID", "0");
		
		Bundle bundle = new Bundle();
		bundle.putBoolean("is_secret", isSecret);
		bundle.putBoolean("is_personal", isPersonal);
		bundle.putBoolean("is_official", isOfficial);
		bundle.putString("vote_limit", voteLimit);
		bundle.putString("reply_day", replyDay);
		bundle.putString("dotchi_type", dotchiType);
		bundle.putString("game_title", gameTitle);
		bundle.putString("dotchi_id", dotchiId);
		// do a little formatting for dotchi time
		if (dates != null)
			bundle.putSerializable("dates", dates);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if (dotchiTime == null)	{
			bundle.putString("dotchi_time", sdf.format(new Date()));
		} else
			bundle.putString("dotchi_time", dotchiTime);
		return bundle;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId())	{
		case R.id.is_personal_box:
			isPersonal = isChecked;
			break;
		case R.id.is_secret_box:
			isSecret = isChecked;
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GET_DATES_REQ_CODE)	{
			if (resultCode == RESULT_OK)	{
				// set list adapter
				dates = (ArrayList<Date>) data.getSerializableExtra("dates");
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
