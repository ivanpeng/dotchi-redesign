package com.dotchi1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * This class is responsible for confirming extra details, and then returning to NewInviteActivity,
 * and then starting game activity. All we have to do is pass all the information back.
 * @author Ivan
 *
 */
public class ConfirmGameSettingActivity extends Activity implements OnClickListener, OnCheckedChangeListener {

	public static final String TAG = "ConfirmGameSettingActivity";
	
	// View Holders
	private ImageView closeConfirmButton;
	private ToggleButton isPrivateButton;
	private ToggleButton isPersonalButton;
	private EditText voteLimitText;
	private EditText dayLimitText;
	private Button confirmButton;
	
	// Variables holders;
	private boolean isPersonal = true;
	private boolean isSecret = true;
	private int replyDay = 7;
	private int voteLimit = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm_game_setting);
		
		closeConfirmButton = (ImageView) findViewById(R.id.close_confirm_page_button);
		isPrivateButton = (ToggleButton) findViewById(R.id.is_private_switch);
		isPersonalButton = (ToggleButton) findViewById(R.id.is_personal_switch);
		voteLimitText = (EditText) findViewById(R.id.vote_limit_text_box);
		dayLimitText = (EditText) findViewById(R.id.day_limit_text_box);
		confirmButton = (Button) findViewById(R.id.confirm_game_setting_button);
		
		// Set onClickListeners; 
		closeConfirmButton.setOnClickListener(this);
		isPrivateButton.setOnCheckedChangeListener(this);
		isPersonalButton.setOnCheckedChangeListener(this);
		confirmButton.setOnClickListener(this);
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId())	{
		case R.id.is_private_switch:
			if (isChecked)
				isSecret = true;
			else
				isSecret = false;
			Log.d(TAG, "Is Secret setting to" + isSecret);
			break;
		case R.id.is_personal_switch:
			if (isChecked)
				isPersonal = true;
			else
				isPersonal = false;
			Log.d(TAG, "Is personal setting to " + isPersonal);
			break;
		}
		
	}


	@Override
	public void onBackPressed() {
		finishActivity(RESULT_CANCELED);
	}


	@Override
	public void onClick(View v) {
		switch(v.getId())	{
		case R.id.close_confirm_page_button:
			// wrap finish activity with failure
			finishActivity(RESULT_CANCELED);
			break;
		case R.id.confirm_game_setting_button:
			try	{
				voteLimit = Integer.parseInt(voteLimitText.getText().toString());
				replyDay = Integer.parseInt(dayLimitText.getText().toString());
				finishActivity(RESULT_OK);	
			} catch(NumberFormatException n)	{
				// Entered erroneous text; we display toast saying you dun goofed
				Toast.makeText(getApplicationContext(), "Have to enter a valid number!", Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

	public void finishActivity(int resultCode )	{
		Intent returnIntent = new Intent();
		if (resultCode == RESULT_OK){
			returnIntent.putExtra("is_personal", isPersonal);
			returnIntent.putExtra("is_secret", isSecret);
			returnIntent.putExtra("vote_limit", voteLimit);
			returnIntent.putExtra("reply_day", replyDay);
		}
		setResult(resultCode, returnIntent);
		finish();
	}

}
