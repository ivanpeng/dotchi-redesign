package com.dotchi1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.Toast;

import com.dotchi1.backend.ClearSearchListener;
import com.dotchi1.model.GameCardItem;
import com.google.analytics.tracking.android.EasyTracker;

public class CreateGameCardActivity extends Activity {

	public static final String TAG = "CreateGameCardActivity";
	
	public static final int CAMERA_PIC_REQUEST = 100;
	public static final int MY_PHOTOS_REQUEST = 101;
	
	Uri imageUri;
	EditText gameCardTitle;
	EditText gameCardDetails;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_create_game_card);
		gameCardTitle = (EditText) findViewById(R.id.gs_card_title_text);
		gameCardDetails = (EditText) findViewById(R.id.gs_card_details_text);
		// set button properties for game card
		ImageView cancelGameCard = (ImageView) findViewById(R.id.gs_card_cancel_button);
		cancelGameCard.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finishWithResultCancel();
			}
		});
		ImageView gameCardTextCancel = (ImageView)findViewById(R.id.gs_card_text_clear_button);
		gameCardTextCancel.setOnClickListener(new ClearSearchListener(this, R.id.gs_card_title_text));
		
		// Set clickable layouts for photos/details
		RelativeLayout addPhotoLayout = (RelativeLayout) findViewById(R.id.add_photo_box);
		addPhotoLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// pop up on select from photos or open camera intent
				Log.d(TAG, "Add Photo Layout has been clicked");
				openPhotoAlert();
				
			}
		});
		ImageView completeGameCard = (ImageView) findViewById(R.id.gs_card_create_button);
		completeGameCard.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Need to check for properties filled
				GameCardItem item = checkGameCardFields();
				if (item != null)
					addGameCard(item);
				else
					Toast.makeText(getApplicationContext(), "Not all properties are filled: ", Toast.LENGTH_SHORT).show();

			}
		});
	}

	protected void finishWithResultCancel()	{
		// Call activity on back pressed, or x
		Intent returnIntent = new Intent();
		setResult(RESULT_CANCELED, returnIntent);
		finish();
		overridePendingTransition(0, R.animator.pull_down_from_top);
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
	@Override
	public void onBackPressed() {
		finishWithResultCancel();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_game_card, menu);
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

	protected GameCardItem checkGameCardFields()	{
		// validates all properties of gameCard to see if they're filled, and creates a game
		// How do we deal with itemImage URL first?
		Log.d(TAG, "Image URI: " + (imageUri == null ? "" : imageUri.toString()));
		Log.d(TAG, "Game Card Title" + gameCardTitle.getText().toString().trim());
		Log.d(TAG, "Game card details: " + gameCardDetails.getText().toString().trim());
		if (imageUri != null && gameCardTitle.getText().toString().trim().length() > 0 && gameCardDetails.getText().toString().trim().length() > 0)
			return new GameCardItem(imageUri.toString(), gameCardTitle.getText().toString().trim(), gameCardDetails.getText().toString().trim());
		return null;
	}
	
	protected void addGameCard(GameCardItem gameCardItem)	{
		// Returns data data item back
		Intent returnIntent = new Intent();
		returnIntent.putExtra("game_card", gameCardItem);
		setResult(RESULT_OK, returnIntent);
		finish();
		overridePendingTransition(0, R.animator.pull_down_from_top);
	}
	
	
	protected void openPhotoAlert() {
		String[] choices = {"選擇相片", "相機拍照"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setItems(choices, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (which == 0)	{
					// Start intent to open photos
					// Remember to use startActivity for Result
					Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://media/internal/images/media")); 
					startActivityForResult(intent, MY_PHOTOS_REQUEST); 
				} else	{
					// Start intent to open camera
					Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(intent, CAMERA_PIC_REQUEST);
				}
			}
		});
		builder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Bitmap thumbnail = null;
		if (requestCode == CAMERA_PIC_REQUEST)	{
			if (resultCode == RESULT_OK) {
				// Photo was taken
				thumbnail = (Bitmap) data.getExtras().get("data");
				// Write the data to a temp cache, and return that as the URI;
				try {
					File file = File.createTempFile("data", ".png", getCacheDir());
					FileOutputStream fos = new FileOutputStream(file);
					thumbnail.compress(Bitmap.CompressFormat.PNG, 80, fos);
					fos.close();
					Log.d(TAG, file.toString());
					imageUri = Uri.fromFile(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (resultCode == RESULT_CANCELED)	{
				Log.d(TAG, "Take photo request has been cancelled.");
			}
			
		} else if (requestCode == MY_PHOTOS_REQUEST)	{
			if (resultCode == RESULT_OK)	{
				// Photo was chosen from my Photos
				Uri uri = data.getData();
				String filePath = getPath(uri);
				imageUri = Uri.parse("file://" + filePath);
				InputStream imageStream;
				try {
					imageStream = getContentResolver().openInputStream(imageUri);
					thumbnail = BitmapFactory.decodeStream(imageStream);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} else if (resultCode == RESULT_CANCELED)	{
				Log.d(TAG, "Get photo from files request has been cancelled.");
			}
		}
		if (thumbnail != null)	{
			RelativeLayout layout =(RelativeLayout) findViewById(R.id.add_photo_box);
			layout.removeAllViews();
			ImageView image = new ImageView(this);		    
			image.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			image.setScaleType(ScaleType.FIT_CENTER);
			image.setImageBitmap(thumbnail);
			layout.addView(image);
		}
	}
	
	public String getPath(Uri uri) {
		String[]  data = { MediaStore.Images.Media.DATA };
	    CursorLoader loader = new CursorLoader(getApplicationContext(), uri, data, null, null, null);
	    Cursor cursor = loader.loadInBackground();
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	}
	
	
}
