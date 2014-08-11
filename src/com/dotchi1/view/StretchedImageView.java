package com.dotchi1.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * This is a subclassed imageview to encapsulate maintaining aspect ratio of 3/2, set in beginning.
 * If I'm feeling up for the challenge, I'll set up
 * @author Ivan
 *
 */
public class StretchedImageView extends ImageView {

	public StretchedImageView(Context context)	{
		super(context);
	}
	public StretchedImageView(Context context, AttributeSet attrs)	{
		super(context, attrs);
	}
	public StretchedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
		setMeasuredDimension(width, height);
	}

	
}
