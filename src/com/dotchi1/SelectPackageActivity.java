package com.dotchi1;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public class SelectPackageActivity extends ActionBarActivity {

	public static final String TAG = "SelectPackageActivity";
	public static final int REQUEST_HOT_DOTCHI = 10;
	public static final int REQUEST_MY_PACKAGE = 11;
	public static final int REQUEST_CONFIRM_GAME = 12;
	
	private static FragmentManager mFragmentManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_package);
		mFragmentManager = getFragmentManager();
		Intent data = getIntent();
		Bundle bundle = data.getExtras();
		boolean which = data.getBooleanExtra("is_dotchi_package", true);
		if (which)	{
			switchFragment(NewInviteDotchiFragment.class, R.id.fragment_container, bundle);
		} else	
			switchFragment(NewInviteFavorFragment.class, R.id.fragment_container, bundle);
		// 
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_HOT_DOTCHI || requestCode == REQUEST_MY_PACKAGE)	{
			// take intent, get bundle, and switch fragment to Self choice with those arguments
			if (resultCode == RESULT_OK)	{
				Log.d(TAG, "caught onActivityResult from hot dotchi package. rerouting information to fragment now.");
				Log.d(TAG, "Sending value " + data.getStringExtra("data") + " back");
				Intent returnIntent = new Intent();
				returnIntent.putExtra("data", data.getStringExtra("data"));
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		} else
			super.onActivityResult(requestCode, resultCode, data);
	}


	/**
	 * @author William
	 * @param mFragmentClass 
	 * @param continer (res R.id)
	 * @param bundle
	 * @throws Out of memory error
	 * @throws Exception 
	 */
	private static void switchFragment(Class<?> mFragmentClass,int container, Bundle bundle)  {
		Log.d(TAG,"+++ Switch Fragment Page +++");
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		try {
			Fragment fragment = (Fragment) mFragmentClass.newInstance();
			if (null != bundle) {
				fragment.setArguments(bundle);
			}
			ft.replace(container, fragment, null);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
		}catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG,"Something wrong with fragment ...");
		}catch (OutOfMemoryError error) {
			Log.e(TAG,"Out of memory error ...");
        }
		Log.d(TAG,"--- Switch Fragment Page ---");
	}//end of switchFragmentPage

}
