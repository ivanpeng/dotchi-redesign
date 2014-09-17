package com.dotchi1.backend;

import java.util.ArrayList;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.dotchi1.R;

/**
 * Implementation of {@link PagerAdapter} that represents each page as a {@link View}.
 *
 * @author Sebastian Kaspari <sebastian@androidzeitgeist.com>
 */
public abstract class ViewPagerAdapter extends PagerAdapter
{
	protected ArrayList<View> views = new ArrayList<View>();

	/**
	 * Used by ViewPager 
	 * @param object represents the page; tell the ViewPager where the page should be displayed, from left-to-right. If the page no longer
	 * exists, return POSITION_NONE
	 */
	@Override
	public int getItemPosition (Object object)
	{
		return POSITION_NONE;
//		int index = views.indexOf (object);
//		if (index == -1)
//			return POSITION_NONE;
//		else
//			return index;
	}
	/**
	 * Get a View that displays the data at the specified position in the data set.
	 *
	 * @param position The position of the item within the adapter's data set of the item whose view we want.
	 * @param pager    The ViewPager that this view will eventually be attached to.
	 *
	 * @return A View corresponding to the data at the specified position.
	 */
	public abstract View getView(int position, ViewPager pager);

	/**
	 * Determines whether a page View is associated with a specific key object as
	 * returned by instantiateItem(ViewGroup, int).
	 *
	 * @param view   Page View to check for association with object
	 * @param object Object to check for association with view
	 *
	 * @return true if view is associated with the key object object.
	 */
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	/**
	 * Create the page for the given position.
	 *
	 * @param container The containing View in which the page will be shown.
	 * @param position  The page position to be instantiated.
	 *
	 * @return Returns an Object representing the new page. This does not need
	 *         to be a View, but can be some other container of the page.
	 */
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		ViewPager pager = (ViewPager) container;
		View view = getView(position, pager);
		view.setBackgroundResource(R.drawable.gs_background);
		container.addView(view);
		addView(pager, view);

		return view;
	}

	/**
	 * Remove a page for the given position.
	 *
	 * @param container The containing View from which the page will be removed.
	 * @param position  The page position to be removed.
	 * @param view      The same object that was returned by instantiateItem(View, int).
	 */
	@Override
	public void destroyItem(ViewGroup container, int position, Object view) {
		container.removeView(views.get(position));
	}
	
	public int addView(ViewPager pager, View v)	{
		return addView(pager, v, views.size());
	}
	
	public int addView(ViewPager pager, View v, int position)	{
		//pager.setAdapter(null);
		views.add(position, v);
		//pager.setAdapter(this);
		return position;
	}
	
	public int removeView (ViewPager pager, View v) {
		return removeView (pager, views.indexOf (v));
	}
	
	public int removeView (ViewPager pager, int position)
	{
		// ViewPager doesn't have a delete method; the closest is to set the adapter
		// again.  When doing so, it deletes all its views.  Then we can delete the view
		// from from the adapter and finally set the adapter to the pager again.  Note
		// that we set the adapter to null before removing the view from "views" - that's
		// because while ViewPager deletes all its views, it will call destroyItem which
		// will in turn cause a null pointer ref.
		pager.setAdapter (null);
		views.remove (position);
		pager.setAdapter (this);
		return position;
	}
}
