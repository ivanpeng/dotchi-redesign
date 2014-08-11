package com.dotchi1;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.FriendPageFriendItem;
import com.dotchi1.view.RoundedImageView;
/**
 * 
 * @author William
 * edit at 2014-07-21 16:40
 * 
 */
public class NewInviteChoiceFriendFragment extends Fragment implements OnClickListener{
	
	private static final String TAG = "NewInviteChoiceFriendFragment";
	private static final int FRIEND_SELECT_REQUEST_CODE = 1000;
	
	public static final int NO_SELECTED_FRIEND = 0;
	public static final int HAVE_SELECTED_FRIEND = 1;
	public static final int MAX_SELECTED_FRIENDS = 99;//default set 99
	float screenWidthPixels;//real size
	float screenHeightPixels;//real size
	static int totalSelectFriendsNumber;
	private ArrayList<FriendPageFriendItem> selectedFriends;
	
	private OnFriendsSetListener mOnFriendsSetListener;
	// ================ View composer================= //
	private ImageView addIcon;
	private static TextView friendSelectNumber;
	private static GridView selectedFriendView;
	private static LinearLayout emptyView;
	private static SelectedFriendsGridAdapter adapter;
	
	
	// ==============================================
	public interface OnFriendsSetListener	{
		public void onFriendsSet(ArrayList<FriendPageFriendItem> friends);
	}
	
	// ================ UI Handler =================== //
	public static Handler newInviteChoiceFriendUIHandler = new Handler(){		
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
				case NO_SELECTED_FRIEND:
					totalSelectFriendsNumber = 0;
					emptyView.setVisibility(View.VISIBLE);
					selectedFriendView.setVisibility(View.GONE);
					break;
				case HAVE_SELECTED_FRIEND:
					if(msg.arg1>MAX_SELECTED_FRIENDS||msg.arg1<=0)break;//range 1~max selected friends
					totalSelectFriendsNumber = msg.arg1-1;
					friendSelectNumber.setText(""+totalSelectFriendsNumber);
					selectedFriendView.setAdapter(adapter);
					emptyView.setVisibility(View.GONE);
					selectedFriendView.setVisibility(View.VISIBLE);
					// flash select friend item's layout
					// ..
					// ..
					break;
				}
				super.handleMessage(msg);
			}
			
		};
	// =================  Override method  ================= //
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mOnFriendsSetListener = (OnFriendsSetListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View choiceFriendRootView = inflater.inflate(R.layout.fragment_new_invite_choice_friends, container, false);
		// bind view xml res
		addIcon				= (ImageView) choiceFriendRootView.findViewById(R.id.new_invite_friend_choice_icon_add_imageview);
		friendSelectNumber	= (TextView) choiceFriendRootView.findViewById(R.id.new_invite_friend_choice_friend_total_textview);
		emptyView 			= (LinearLayout) choiceFriendRootView.findViewById(R.id.new_invite_friend_choice_empty_view);
		selectedFriendView  = (GridView) choiceFriendRootView.findViewById(R.id.invite_friend_choice_gridview);
		
		emptyView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startFriendActivity();
			}
		});
		// Give illusion that gridview is fixed.
		selectedFriendView.setVerticalScrollBarEnabled(false);
		selectedFriendView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE)	{
					return true;
				}
				return false;
			}
		});
		selectedFriendView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				if (position == adapter.getCount()-1)	{
					startFriendActivity();
				}
			}
		});
		// resize
		// handler bundle data 
		Bundle bundle = getArguments();// load friends list
		handleBundle(bundle);
		// ...


		// ...
		return choiceFriendRootView;
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.new_invite_index_dotchi_button:
			NewInviteActivity.switchDotchiFragment(null);
			break;
		case R.id.new_invite_index_favor_button:
			NewInviteActivity.switchFavouriteFragment(null);
			break;
		case R.id.new_invite_index_self_choice_button:
			NewInviteActivity.switchSelfChoiceFragment(null);
			break;
		}
	}
	
	/**
	 * This method accepts in a bundle from either from fragment or return data
	 * @param bundle
	 */
	protected void handleBundle(Bundle bundle)	{		
		if (bundle != null){
			Log.d(TAG, "Bundle is not null");
			selectedFriends = bundle.getParcelableArrayList("selected_list");
			if (selectedFriends != null)	{
				Log.d(TAG, "Selected Friends: " + selectedFriends.toString());
				adapter = new SelectedFriendsGridAdapter(getActivity(), R.layout.grid_friend_item, selectedFriends);
			}
			mOnFriendsSetListener.onFriendsSet(selectedFriends);
		}
		Message message = new Message();
		if (adapter == null || (adapter != null && adapter.getCount() == 1))	{
			message.what = NO_SELECTED_FRIEND;
			message.arg1 = 0;
		} else {
			message.what = HAVE_SELECTED_FRIEND;
			message.arg1 =  adapter.getCount();
		}
		newInviteChoiceFriendUIHandler.sendMessage(message);
	}
	
	public void startFriendActivity()	{
		Intent intent = new Intent(getActivity(), NewFriendSelectActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList("groups", null);
		bundle.putParcelableArrayList("friends", getArguments().getParcelableArrayList("friends"));
		// TODO: Put selected_list if present?
		intent.putExtras(bundle);
		startActivityForResult(intent, FRIEND_SELECT_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FRIEND_SELECT_REQUEST_CODE)
			if (resultCode == Activity.RESULT_OK)	{
				// Now populate friend list
				Log.d(TAG, "Handling previous bundle data");
				Bundle b = data.getExtras();
				handleBundle(b);
			}
		else
			super.onActivityResult(requestCode, resultCode, data);
	}


	static class SelectedFriendsGridAdapter extends ArrayAdapter<FriendPageFriendItem> {
		private static final int SELECTED_FRIENDS_ADAPTER_IMAGE_SIZE=40;
		
		private Context context;
		private ArrayList<FriendPageFriendItem> objects;
		private ArrayList<FriendPageFriendItem> fullList;
		private LiteImageLoader imageLoader;

		public SelectedFriendsGridAdapter(Context context,
				int textViewResourceId, List<FriendPageFriendItem> objects) {
			super(context, textViewResourceId, objects);
			this.context = context;
			if (objects.size() > 9)	{
				this.objects = new ArrayList<FriendPageFriendItem>(objects.subList(0, 8));
				this.fullList = new ArrayList<FriendPageFriendItem>(objects);
			} else	{
				this.objects = new ArrayList<FriendPageFriendItem>(objects);
				this.fullList = new ArrayList<FriendPageFriendItem>(objects);
			}
			// Add the plus item to the end of the list.
			FriendPageFriendItem holder = new FriendPageFriendItem();
			holder.setHeadImage(String.valueOf(R.drawable.new_invite_icon_add_forcus));

			this.objects.add(holder);
			imageLoader = new LiteImageLoader(context);
			
		}

		@Override
		public int getCount() {
			return objects.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FriendPageFriendItem item = objects.get(position);
			View view = convertView;
			if (view == null)	{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.grid_friend_item, null);
			}
			RoundedImageView pic = (RoundedImageView) view.findViewById(R.id.rounded_image_picture);
			if(position != getCount()-1)
				imageLoader.DisplayImage(item.getHeadImage(), R.drawable.default_profile_pic, pic, SELECTED_FRIENDS_ADAPTER_IMAGE_SIZE);
			else
				pic.setImageResource(Integer.parseInt(item.getHeadImage()));
			
			return view;
		
		}
		
		
	}
}
