package com.dotchi1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.dotchi1.backend.PostUrlTask;
/**
 * 
 * @author William
 * final edit at 2014-07-21 
 *
 */
public class NewInviteIndexFragment extends Fragment implements OnClickListener{
	
	/** {@link #layoutResize()} */
	private static final int DEFAULT_WIDTH = 720;// layout.xml model size
	private static final int DEFAULT_HEIGHT = 1280;// layout.xml model size
	public static final int HIDE_REDPOINT = 0;
	public static final int SHOW_REDPOINT = 1;
	float screenWidthPixels;//real size
	float screenHeightPixels;//real size
	private static int redPointCounter = 0;
	// ================ View composer================= //
	/** {@link #onClick(View)} */
	Button switchDotchi,switchFavor,switchSelfChoice;
	private static Button redPoint;
	private String redpointData = "";
	// ================ UI Handler =================== //
	public static Handler newInviteIndexUIHandler = new Handler(){		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case HIDE_REDPOINT:
				redPointCounter = 0;
				redPoint.setVisibility(View.GONE);
				break;
			case SHOW_REDPOINT:
				if(msg.arg1>99||msg.arg1<=0)break;//range 1~99
				redPointCounter = msg.arg1;
				redPoint.setText(""+redPointCounter);
				redPoint.setVisibility(View.VISIBLE);
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	// =================  Override method  ================= //
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View indexRootView = inflater.inflate(R.layout.fragment_new_invite_index, container, false);
		// bind view xml res
		switchDotchi		= (Button) indexRootView.findViewById(R.id.new_invite_index_dotchi_button);
		switchFavor			= (Button) indexRootView.findViewById(R.id.new_invite_index_favor_button);
		switchSelfChoice	= (Button) indexRootView.findViewById(R.id.new_invite_index_self_choice_button);
		redPoint			= (Button) indexRootView.findViewById(R.id.new_invite_index_red_point_button);
		// resize
		layoutResize();
		// set event
		switchDotchi.setOnClickListener(this);
		switchFavor.setOnClickListener(this);
		switchSelfChoice.setOnClickListener(this);
		return indexRootView;
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
		String rootUrl = getResources().getString(R.string.api_test_root_url);
		new PostUrlTask()	{

			@Override
			protected void onPostExecute(String result) {
				// Set red dot, and then save string to send to fragments
				result = processResult(result);
				try {
					final JSONArray arr = new JSONObject(result).getJSONArray("data");
					if (arr.length() == 0)	{
						redpointData = "";
						Message m = new Message();
						m.what = HIDE_REDPOINT;
						newInviteIndexUIHandler.sendMessage(m);
					} else	{
						// There's content! 
						redpointData = result;
						Message m = new Message();
						m.what = SHOW_REDPOINT;
						m.arg1 = arr.length();
						newInviteIndexUIHandler.sendMessage(m);
					}
				} catch (JSONException jex)	{
					jex.printStackTrace();
				}
			}
			
		}.execute(rootUrl + "/game/get_hot_dotchi_package", "dotchi_id", "35");
		super.onStart();
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.new_invite_index_dotchi_button:
			Bundle bundle = new Bundle();
			bundle.putString("json", redpointData);
			NewInviteActivity.switchDotchiFragment(bundle);
			break;
		case R.id.new_invite_index_favor_button:
			NewInviteActivity.switchFavouriteFragment(null);
			break;
		case R.id.new_invite_index_self_choice_button:
			NewInviteActivity.switchSelfChoiceFragment(null);
			break;
		}
	}
	// =================  method  ================= //
		/**
		 * @see Reset all view composer position/size
		 * 
		 */
		private void layoutResize(){
			// get devices display info 
			DisplayMetrics displayMetrics = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			screenWidthPixels = displayMetrics.widthPixels;
			screenHeightPixels = displayMetrics.heightPixels;
			float xwScale = ((float)screenWidthPixels/(float)DEFAULT_WIDTH);
			float yhScale = ((float)screenHeightPixels/(float)DEFAULT_HEIGHT);
			// set frames
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int)(xwScale*330),(int)(yhScale*100));
			lp.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
			lp.addRule(RelativeLayout.CENTER_VERTICAL,RelativeLayout.TRUE);
			lp.topMargin = (int)(yhScale*10);
			lp.bottomMargin = (int)(yhScale*10);
			switchDotchi.setLayoutParams(lp);
			switchFavor.setLayoutParams(lp);
			switchSelfChoice.setLayoutParams(lp);
			
			lp = new RelativeLayout.LayoutParams((int)(xwScale*40),(int)(xwScale*40));
			lp.addRule(RelativeLayout.ALIGN_TOP,switchDotchi.getId());
			lp.addRule(RelativeLayout.ALIGN_RIGHT,switchDotchi.getId());
			lp.topMargin = (int)(yhScale*20);
			lp.rightMargin = (int)(xwScale*30);
			redPoint.setLayoutParams(lp);
			
		}
}
