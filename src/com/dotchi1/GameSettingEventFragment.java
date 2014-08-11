package com.dotchi1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.dotchi1.GameSettingActivity.PassDownData;
import com.dotchi1.backend.PassUpData;
import com.dotchi1.backend.PostUrlTask;
import com.dotchi1.backend.SwipeDismissListViewTouchListener;
import com.dotchi1.backend.SwipeDismissListViewTouchListener.Direction;
import com.dotchi1.backend.ViewUtils;
import com.dotchi1.model.GameCardItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class GameSettingEventFragment extends Fragment implements PassDownData {

	public static final String TAG = "GameSettingFragment";

	
	PassUpData dataPasser;

	private DotchiDateObject dotchiDateObject;
	int categoryId;
	
	ImageLoader imageLoader;
	DisplayImageOptions options;
	private GameCardAdapter adapter;
	private ListView gameCardList;
	private ArrayList<GameCardItem> gameCards;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		dataPasser = (PassUpData) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_game_setting_event, container, false);
		dotchiDateObject = new DotchiDateObject();
		imageLoader = ImageLoader.getInstance();
		options = ViewUtils.getLocalImageConfiguration();
		setEventFragmentViews(view);
		return view;
	}

	
	protected void setEventFragmentViews(View view){

		// This should all go in a different procedure....
		LocalDateTime now = new LocalDateTime();
		final int year = now.getYear();
		final int monthOfYear = now.getMonthOfYear();
		final int dayOfMonth = now.getDayOfMonth();

		final int hourOfDay = now.getHourOfDay();
		final int minute = now.getMinuteOfHour();
		
		dotchiDateObject.year = 0;
		dotchiDateObject.month = 0;
		dotchiDateObject.day = 1;
		dotchiDateObject.hour = 0;
		dotchiDateObject.minute = 0;

		final TextView datePicker = (TextView) view.findViewById(R.id.gs_date_picker_button);
		//datePicker.setText(dateSdf.format(now.toDate()));
		//datePicker.addTextChangedListener(new EnableInviteFriendsWatcher(R.id.gs_date_picker_button));
		datePicker.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Start actual datePicker Dialog
				DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear,
							int dayOfMonth) {
						// Save data to some class variables so we can grab later.
						// Make sure to display at imageView as well!
						// This is messed; month of year is 0-indexed, day is 1-indexed, hour is 0-indexed
						monthOfYear = monthOfYear + 1;
						StringBuilder sb = new StringBuilder();
						sb.append(year).append("/");
						if (monthOfYear < 10)
							sb.append("0");
						sb.append(monthOfYear).append("/");
						if (dayOfMonth < 10)
							sb.append("0");
						sb.append(dayOfMonth);
						Log.d(TAG, "Logged date: " + sb.toString());
						datePicker.setText(sb.toString());
						dotchiDateObject.year = year;
						dotchiDateObject.month = monthOfYear;
						dotchiDateObject.day = dayOfMonth;
					}
				}, year, monthOfYear-1, dayOfMonth);
				datePickerDialog.show();
			}
		});
		
		final TextView timePicker = (TextView) view.findViewById(R.id.gs_time_picker_button);
		//timePicker.setText(timeSdf.format(now.toDate()));
		//timePicker.addTextChangedListener(new EnableInviteFriendsWatcher(R.id.gs_time_picker_button));
		timePicker.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean is24HourView = true;
				TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new OnTimeSetListener() {
					
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						StringBuilder sb = new StringBuilder();
						if(hourOfDay < 10)	
							sb.append("0");
						sb.append(hourOfDay).append(":");
						if(minute < 10)
							sb.append("0");
						sb.append(minute);
						Log.d(TAG, "Logged time: " + sb.toString());
						timePicker.setText(sb.toString());

						dotchiDateObject.hour = hourOfDay;
						dotchiDateObject.minute = minute;
					}
				}, hourOfDay, minute, is24HourView);
				timePickerDialog.show();
				
			}
		});

		categoryId = getActivity().getIntent().getIntExtra("category_id", -1);
		if (categoryId != -1)
			new GetDefaultItemTask().execute(getResources().getString(R.string.api_root_url) + "/game/get_game_default_item", "category_id", String.valueOf(categoryId));
		gameCardList = (ListView) view.findViewById(R.id.game_card_list);
		SwipeDismissListViewTouchListener touchListener = 
                new SwipeDismissListViewTouchListener(
                		gameCardList,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {

							@Override
							public boolean canDismiss(int position) {
								return true;
							}

							@Override
							public void onDismiss(ListView view, int[] reverseSortedPositions, Direction swipeDirection) {
								for (int position : reverseSortedPositions) {
									adapter.remove(adapter.getItem(position));
								}
								adapter.notifyDataSetChanged();
							}
                		});
		gameCardList.setOnTouchListener(touchListener);
	}
	
	@Override
	public void onNewCardCreated(GameCardItem item) {
		// Parse data!
		if (gameCards == null)	{
			gameCards = new ArrayList<GameCardItem>();
			adapter = new GameCardAdapter(getActivity(), gameCards);
			gameCardList.setAdapter(adapter);
		}
		adapter.add(item);
		adapter.notifyDataSetChanged();
		
	}
	
	@Override
	public String getDotchiDate() {
		DateTime dt;
		if (!(dotchiDateObject.year == 0 && dotchiDateObject.month == 0 && dotchiDateObject.day == 1 && dotchiDateObject.hour == 0 && dotchiDateObject.minute == 0))	{
			dt = new DateTime(dotchiDateObject.year, dotchiDateObject.month, dotchiDateObject.day, dotchiDateObject.hour, dotchiDateObject.minute);
			DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			Log.d(TAG, "Passing up dotchi Date of " + dtf.print(dt));
			return dtf.print(dt);
		}
		return "0000-00-00 00:00:00";
	}
		
	class GetDefaultItemTask extends PostUrlTask	{

		@Override
		protected void onPostExecute(String result) {
			result = processResult(result);
			// Parse the result into GameCardItems, set adapter and listview
			JSONObject jsonObj;
			try {
				jsonObj = new JSONObject(result);
				JSONArray arr = jsonObj.getJSONArray("data");
				Log.d(TAG, arr.toString());
				ObjectMapper mapper = new ObjectMapper();
				mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
				mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
				mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				gameCards = mapper.readValue(arr.toString(), mapper.getTypeFactory().constructCollectionType(List.class, GameCardItem.class));
				adapter = new GameCardAdapter(getActivity(), gameCards);
				gameCardList.setAdapter(adapter);
				dataPasser.onDataPass(gameCards);
				//setNumberPickerViews();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	class GameCardAdapter extends ArrayAdapter<GameCardItem>	{

		private Context context;
		private ArrayList<GameCardItem> values;
		
		public GameCardAdapter(Context context, List<GameCardItem> values) {
			super(context, R.layout.game_card_item, values);
			this.context = context;
			this.values = new ArrayList<GameCardItem>(values);
		}

		@Override
		public void remove(GameCardItem object) {
			int index = values.indexOf(object);
			if (index != -1)
				values.remove(index);
			super.remove(object);
			//Log.d(TAG, "Values: " + values.toString());
			Log.d(TAG, "Removed " + object.toString() + " from list");

		}

		@Override
		public int getCount() {
			return values.size();
		}

		@Override
		public void add(GameCardItem object) {
			values.add(object);
			super.add(object);
			Log.d(TAG, "Added " + object.toString() + " to list");
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			GameCardItem item = values.get(position);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.game_card_item, parent, false);
			ImageView gameCardImage = (ImageView) view.findViewById(R.id.game_card_profile_image);
			imageLoader.displayImage(item.getItemImage(), gameCardImage, options);
			TextView gameCardTitle = (TextView) view.findViewById(R.id.game_card_title);
			gameCardTitle.setText(item.getItemTitle());
			TextView gameCardContent = (TextView) view.findViewById(R.id.game_card_content);
			gameCardContent.setText(item.getItemContent());
			return view;
		}


		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			dataPasser.onDataPass(values);
			dataPasser.notifyListSizeChanged(adapter.getCount());
		}
		
	}
	
	public class DotchiDateObject	{
		int year;
		int month;
		int day;
		int hour;
		int minute;
	}



}
