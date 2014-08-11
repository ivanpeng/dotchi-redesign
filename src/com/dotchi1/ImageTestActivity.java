package com.dotchi1;

import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import de.passsy.holocircularprogressbar.HoloCircularProgressBar;

public class ImageTestActivity extends Activity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		
		HoloCircularProgressBar circle = (HoloCircularProgressBar) findViewById(R.id.bar);
		circle.setProgress(0.8f);
		circle.setProgressColor(Color.BLACK);
	}
	/** 
	 * @see William add google analytics to tracker user error/exception edit in 2014-07-21 
	 * <br> and see alse:
	 * <br> libs/libGoogleAnalyticsServices.jar  
	 * <br> value/analytics.xml  
	 * */
	@Override
	  public void onStart() {
	    super.onStart();
	    // The rest of your onStart() code.
	    // ..
	    // ..
	    //You can enable this code when you ready online
	    //EasyTracker.getInstance(this).activityStart(this);  // Add this method.
		}// end of onStart
	/** 
	 * @see William add google analytics to tracker user error/exception edit in 2014-07-21 
	 * <br> and see alse:
	 * <br> libs/libGoogleAnalyticsServices.jar  
	 * <br> value/analytics.xml 
	 * */
	@Override
	  public void onStop() {
	    super.onStop();
	    // The rest of your onStop() code.
	    // ..
	    // ..
	    //You can enable this code when you ready online
	    //EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }// end of onStop
	

}
