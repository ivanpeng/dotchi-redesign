package com.dotchi1;

import java.io.File;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class ProfileBox extends RelativeLayout {

	private View view;
	//private URI profileHead;
	//private String profileName;
	
	public ProfileBox(Context context)	{
		this(context, null, 0);
	}
	
	public ProfileBox(Context context, AttributeSet attrs)	{
		this(context, attrs, 0);
	}
	
	public ProfileBox(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs, defStyle, null, null);
		
	}
	
	public ProfileBox(Context context, AttributeSet attrs, int defStyle, String profileUrl, String name)	{
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.profile_head, this, true);
		if (profileUrl != null && name != null)
			setBox(profileUrl, name);
	}
	
	public void setBox(String profileHead, String name)	{
		if (profileHead != null && name != null)	{
			// Populate these
			ImageLoader imageLoader = ImageLoader.getInstance();
			ImageView pp = (ImageView) view.findViewById(R.id.profile_picture);
			imageLoader.displayImage(profileHead, pp);
			TextView n = (TextView) view.findViewById(R.id.profile_name);
			n.setText(name);
		}
	}

}
