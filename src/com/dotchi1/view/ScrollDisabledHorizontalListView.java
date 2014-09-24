package com.dotchi1.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.devsmart.android.ui.HorizontalListView;

public class ScrollDisabledHorizontalListView extends HorizontalListView {

	public ScrollDisabledHorizontalListView(Context context) {
		super(context);
	}

	public ScrollDisabledHorizontalListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		final int actionMasked = ev.getActionMasked() & MotionEvent.ACTION_MASK;

		if (actionMasked == MotionEvent.ACTION_DOWN) {
			return super.dispatchTouchEvent(ev);
		}

		if (actionMasked == MotionEvent.ACTION_MOVE) {
			// Ignore move events
			return true;
		}

		if (actionMasked == MotionEvent.ACTION_UP) {
			// This does not check if we are still in the view. Maybe should include MotionEvent.ACTION_CANCEL
			setPressed(false);
			invalidate();
			return true;
		}
		// MotionEvent.ACTION_CANCEL
		return super.dispatchTouchEvent(ev);
	}
}
