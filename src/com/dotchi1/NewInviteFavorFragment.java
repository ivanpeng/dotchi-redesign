package com.dotchi1;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dotchi1.backend.PostUrlTask;
/**
 * 
 * @author William
 * edit at 2014-07-21-17:30
 * This fragment is going to be the manager of two pages: create dotchi package, and view preset dotchi packages
 * We will send in 
 *
 */
public class NewInviteFavorFragment extends Fragment{
	
	static int totalItem;// from api
	static int totalPages;// 1~5 item one page 
	static int pageIndex;//  
	static boolean animateBlock;
	protected float downX;
	protected float upX;
	// ================ View composer================= //
	private TextView numFavouritesCount;
	private ListView myDotchiFavouritesList;
	private ArrayList<String> packageList;
	
		
	// =================  Override method  ================= //
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

		// Get the tag of which object
		// Do we need bundle?
		Bundle bundle = getArguments();
		String dotchiId = getActivity().getSharedPreferences("com.dotchi1", Context.MODE_PRIVATE).getString("DOTCHI_ID", "0");
		View favorRootView = inflater.inflate(R.layout.fragment_new_invite_favor, container, false);
		numFavouritesCount = (TextView) favorRootView.findViewById(R.id.new_invite_favor_total_textview);
		myDotchiFavouritesList = (ListView) favorRootView.findViewById(R.id.my_dotchi_package_list);
		packageList = new ArrayList<String>();
		String rootUrl = getResources().getString(R.string.api_test_root_url);
		new PostUrlTask(){

			@Override
			protected void onPostExecute(String result) {
				result = processResult(result);
				try {
					final JSONArray arr = new JSONObject(result).getJSONArray("data");
					for (int i = 0; i < arr.length(); i++)	{
						packageList.add(arr.getJSONObject(i).getString("package_title"));
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.hot_dotchi_package_item, R.id.dotchi_package_name, packageList);
					myDotchiFavouritesList.setAdapter(adapter);
					numFavouritesCount.setText(String.valueOf(arr.length()));
					myDotchiFavouritesList.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
							String packageId, packageTitle;
							try {
								packageId = arr.getJSONObject(position).getString("package_id");
								packageTitle = arr.getJSONObject(position).getString("package_title");
								// Call startActivityForResult
								Intent intent = new Intent(getActivity(), ShowDotchiPackageActivity.class);
								intent.putExtra("package_id", Integer.parseInt(packageId));
								intent.putExtra("package_title", packageTitle);
								intent.putExtra("request_code", NewInviteActivity.REQUEST_MY_PACKAGE);
								getActivity().startActivityForResult(intent, SelectPackageActivity.REQUEST_MY_PACKAGE);
								
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (JSONException e) {
					// No dotchi packages; display empty list.
					e.printStackTrace();
				}
			}
			
		}.execute(rootUrl + "/game/get_my_dotchi_package", "dotchi_id", dotchiId);

		return favorRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		
	}



}
