package com.dotchi1.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.dotchi1.R;

public class DateImageView extends LinearLayout {

	private View view;
	
	public DateImageView(Context context)	{
		this(context, null, 0);
	}
	
	public DateImageView(Context context, AttributeSet attrs)	{
		this(context, attrs, 0);
	}
	
	public DateImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DateImageView, 0, 0);
		String year = a.getString(R.styleable.DateImageView_year);
		String month = a.getString(R.styleable.DateImageView_month);
		String day = a.getString(R.styleable.DateImageView_day);
		String hour = a.getString(R.styleable.DateImageView_hour);
		String minute = a.getString(R.styleable.DateImageView_min);
		AmPm ampm = AmPm.fromId(a.getInt(R.styleable.DateImageView_ampm, 0));
		a.recycle();
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.date_image_layout, this, true);
		
		//getChild
	}
	
	private enum AmPm	{
		AM(0),
		PM(1);
		
		int id;
		
		AmPm(int id)	{
			this.id = id;
		}
		static AmPm fromId(int id)	{
			for (AmPm i : values())	{
				if (i.id == id)
					return i;
			} 
			return AM;
		}
	}

}
