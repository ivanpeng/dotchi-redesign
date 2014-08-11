package com.dotchi1.backend;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.inputmethod.InputMethodManager;

import com.dotchi1.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class ViewUtils {
	
	public static void hideSoftKeyboard(Activity activity) {
	    InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
	}
	
	public static void initConfiguration(Context context)	{
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory/8;
		File cacheDir = StorageUtils.getCacheDirectory(context);
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context)
			.memoryCache(new LruMemoryCache(cacheSize))
			.memoryCacheSize(cacheSize)
			.denyCacheImageMultipleSizesInMemory()
			.discCache(new UnlimitedDiscCache(cacheDir)) // default
			.discCacheSize(50 * 1024 * 1024)
			.discCacheFileCount(100)
			.build();
		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.init(configuration);
	}
	
	public static DisplayImageOptions getLocalImageConfiguration()	{
		return new DisplayImageOptions.Builder()
					.showImageOnLoading(R.drawable.default_profile_pic)
					.showImageForEmptyUri(R.drawable.default_profile_pic)
					.showImageOnFail(R.drawable.default_profile_pic)
					.delayBeforeLoading(300)
					.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
					.cacheInMemory(true)
					.cacheOnDisc(true)
					.build();
	}
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}
	
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public static Bitmap decodeSampledBitmapFromFile(File file, int reqWidth, int reqHeight)	{
		// First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(file.getAbsolutePath(), options);
	    
	 // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
	}
	
	public static void processGroupActivity(){
		
	}
}
