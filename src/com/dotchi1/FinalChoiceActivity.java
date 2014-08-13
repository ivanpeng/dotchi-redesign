package com.dotchi1;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.dotchi1.backend.VoteItemAdapter;
import com.dotchi1.image.LiteImageLoader;
import com.dotchi1.model.VoteItem;

public class FinalChoiceActivity extends ActionBarActivity {

	GridView gridView;
	ArrayList<VoteItem> choices;
	VoteItemAdapter adapter;
	boolean isOneSelected = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_final_choice);
		gridView = (GridView) findViewById(R.id.grid_choices);
		
		// Actionbar
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View actionbar = inflater.inflate(R.layout.menu_dotchi_package, null);
		ImageView forwardButton = (ImageView) actionbar.findViewById(R.id.forward_button);
		
		// Get data from intent
		Intent data = getIntent();
		LiteImageLoader imageLoader = new LiteImageLoader(this);
		choices =(ArrayList<VoteItem>) data.getSerializableExtra("choices"); 
		adapter = new VoteItemAdapter(this, R.layout.final_choice_item, choices, imageLoader);
		//gridView.setAdapter(adapter);
		gridView.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long id) {
				// Set checkmark
				view.setSelected(!view.isSelected());
				ImageView checkmark = (ImageView) view.findViewById(R.id.checkmark_if_selected);
				if (view.isSelected())	{
					checkmark.setVisibility(View.VISIBLE);
				} else	{
					checkmark.setVisibility(View.INVISIBLE);
				}
				
			}
		});
		
	}

}
