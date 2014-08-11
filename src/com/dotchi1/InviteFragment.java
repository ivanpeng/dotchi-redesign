package com.dotchi1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class InviteFragment extends Fragment{
	
	public static final int REQUEST_START_GAME = 10;
	public static final String INVITE_FRAGMENT_FIRST_KEY = "INVITE_FRAGMENT_FIRST";
	public final static String TAG = "InviteFragment";
	final static String[] gameNames = {"活動"};
	final static int[] resIds = {R.drawable.event_logo};
	final Map<String, Integer> gameTypeMap = new HashMap<String, Integer>();
	//final static String[] gameNames = {"時間日期", "景點", "民宿", "電影", "餐廳", "活動", "自訂選項"};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_invite, container,
				false);
		
		ArrayList<GameTypeItem> list = new ArrayList<GameTypeItem>();
		for (int i = 0; i < gameNames.length; i++)	{
			list.add(new GameTypeItem(resIds[i], gameNames[i]));
		}
		gameTypeMap.put(gameNames[0], 4);
		//gameTypeMap.put(gameNames[1], 1);
		GridView gridView = (GridView) rootView.findViewById(R.id.invite_choice_gridlist);
		gridView.setAdapter(new GameItemAdapter(getActivity(), list));
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// Just start activity for friend activity for now
				Intent intent = new Intent(getActivity(), GameSettingActivity.class);
				intent.putExtra("category_id", gameTypeMap.get(gameNames[position]));
				startActivityForResult(intent, REQUEST_START_GAME);
				
			}
		});
		SharedPreferences preferences = getActivity().getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE);
		boolean isFirstTime = preferences.getString(INVITE_FRAGMENT_FIRST_KEY, "0").equals("0") ? true: false;
		if (isFirstTime)	{
			showInviteHelpDialog();
			Editor editor = preferences.edit();
			editor.putString(INVITE_FRAGMENT_FIRST_KEY, "1");
			editor.commit();
		}
		return rootView;
	}
	
	public void showInviteHelpDialog()	{
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = inflater.inflate(R.layout.dialog_help_game_setup, null);
		TextView title = (TextView) dialogView.findViewById(R.id.dialog_help_title);
		title.setText("接下來兜聚吧");
		final AlertDialog dialog = new AlertDialog.Builder(getActivity())
								.setView(dialogView).create();
		ImageView cancel = (ImageView)dialogView.findViewById(R.id.dialog_help_close_button);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_START_GAME)	{
			if (resultCode == Activity.RESULT_OK)
				// if the result from the game was finished, switch to main fragment
				((MainActivity) getActivity()).switchFragment(MainActivity.FRAGMENT_MYDOTCHI_FEED);
		}
	}

	public class GameTypeItem{
		private int imageId;
		private String name;
		
		public GameTypeItem()	{
		}
		
		public GameTypeItem(int imageId, String name)	{
			this.imageId = imageId;
			this.name = name;
		}
		
		public int getImageId() {
			return imageId;
		}
		public void setImageId(int imageId) {
			this.imageId = imageId;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
	}
	
	class GameItemAdapter extends ArrayAdapter<GameTypeItem>	{

		private Context context;
		private ArrayList<GameTypeItem> items;
		
		public GameItemAdapter(Context context) {
			this(context, null);
		}
		
		public GameItemAdapter(Context context,  List<GameTypeItem> items)	{
			super(context, R.layout.game_type_item, items);
			this.context = context;
			this.items = new ArrayList<GameTypeItem>(items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.game_type_item, parent, false);
			// Set name; that's all that's needed for now
			GameTypeItem item = items.get(position);
			ImageView logo = (ImageView) view.findViewById(R.id.game_type_image);
			logo.setImageResource(item.getImageId());
			TextView gameName = (TextView) view.findViewById(R.id.game_name_text);
			gameName.setText(item.getName());
			return view;
		}
		
		
	}
}
