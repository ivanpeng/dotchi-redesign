package com.dotchi1;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SelectPackageActivity extends Activity {

	public static final String TAG = "SelectPackageActivity";
	
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
			switchFragment(NewInviteDotchiFragment.class, R.id.container, bundle);
		} else	
			switchFragment(NewInviteFavorFragment.class, R.id.container, bundle);
		// 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_package, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
