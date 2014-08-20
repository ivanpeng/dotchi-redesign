package com.dotchi1;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dotchi1.backend.PostUrlTask;
/**
 * 
 * @author William
 * edit at 2014-07-21-12:00
 *
 */
public class NewInviteDotchiFragment extends Fragment {
	
	// ================ View composer================= //
	/** {@link #onClick(View)} */
	TextView giftNumber;
	ImageView gift;
	ListView dotchiPackageListView;
	ArrayList<String> packageList;
	
	// =================  Override method  ================= //
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View dotchiRootView = inflater.inflate(R.layout.fragment_new_invite_dotchi, container, false);
		// bind view xml res
		giftNumber	= (TextView)	dotchiRootView.findViewById(R.id.new_invite_dotchi_gift_textview);
		gift 		= (ImageView)	dotchiRootView.findViewById(R.id.new_invite_dotchi_gift_imageview);
		dotchiPackageListView = (ListView) dotchiRootView.findViewById(R.id.hot_dotchi_package_list);
		
		// call dotchi hot package
		packageList = new ArrayList<String>();
		Bundle args = getArguments();
		String data = args.getString("json");
		if (data == null || data.length() == 0)	{
			String rootUrl = getResources().getString(R.string.api_test_root_url);
			new PostUrlTask(){
	
				@Override
				protected void onPostExecute(String result) {
					result = processResult(result);
					setListValues(result);
				}
				
			}.execute(rootUrl + "/game/get_hot_dotchi_package", "dotchi_id", "35");
		} else	{
			setListValues(data);
		}
		return dotchiRootView;
	}
	
	protected void setListValues(String result)	{
		try {
			final JSONArray arr = new JSONObject(result).getJSONArray("data");
			for (int i = 0; i < arr.length(); i++)	{
				packageList.add(arr.getJSONObject(i).getString("package_title"));
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.hot_dotchi_package_item, R.id.dotchi_package_name, packageList);
			dotchiPackageListView.setAdapter(adapter);
			dotchiPackageListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					String packageId;
					try {
						packageId = arr.getJSONObject(position).getString("package_id");
						//Toast.makeText(getActivity(), "Package ID " + packageId + " selected", Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(getActivity(), ShowDotchiPackageActivity.class);
						intent.putExtra("package_id", Integer.parseInt(packageId));
						intent.putExtra("package_title", packageList.get(position));
						// TODO: start activity for result
						// Need to have the root activity start the next activity, because we are intending on catching it there and rerouting
						getActivity().startActivityForResult(intent, NewInviteActivity.REQUEST_HOT_DOTCHI);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
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

}
