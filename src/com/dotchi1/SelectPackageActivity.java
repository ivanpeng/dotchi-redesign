package com.dotchi1;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

public class SelectPackageActivity extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{

	public static final String TAG = "SelectPackageActivity";
	public static final int REQUEST_HOT_DOTCHI = 10;
	public static final int REQUEST_MY_PACKAGE = 11;
	public static final int REQUEST_CONFIRM_GAME = 12;
	
	private LocationClient mLocationClient;
	private Location mLocation;
	
	private static FragmentManager mFragmentManager;
	
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	// Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		private Dialog mDialog;
		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}
		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}
		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_package);
		mFragmentManager = getFragmentManager();
		mLocationClient = new LocationClient(this, this, this);
		
	}
	
	@Override
	protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
        // Connect the client.
        mLocationClient.connect();
        
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

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Toast.makeText(this, "Error code met with " + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
        }
	}

	@Override
	public void onConnected(Bundle dataBundle) {
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		mLocation = mLocationClient.getLastLocation();
        // Moved here from onCreate to guarantee mLocation data sent over
		Intent data = getIntent();
		Bundle bundle = data.getExtras();
		boolean which = data.getBooleanExtra("is_dotchi_package", true);
        if (which)	{
        	bundle.putString("coordinates", locationStringFromLocation(mLocation));
			switchFragment(NewInviteDotchiFragment.class, R.id.fragment_container, bundle);
		} else	
			switchFragment(NewInviteFavorFragment.class, R.id.fragment_container, bundle);
		// 
	}

	@Override
	public void onDisconnected() {	
		Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
	}
	
	public static String locationStringFromLocation(final Location location) {
	    return Location.convert(location.getLatitude(), Location.FORMAT_DEGREES) + "," + Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
	}

}
