package com.dotchi1.image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.widget.ImageView;

import com.dotchi1.R;
import com.dotchi1.sqlite.ImagesDataSource;

public class LiteImageLoader {
    static MemoryCache memoryCache;
    FileCache fileCache;
    //static private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ImagesDataSource imageDao;
    ExecutorService executorService; 
  
    public LiteImageLoader(Context context){
    	if (memoryCache == null)
    		memoryCache = new MemoryCache();
        fileCache=new FileCache(context);
        imageDao = new ImagesDataSource(context);
        imageDao.open();
        executorService=Executors.newFixedThreadPool(5);
    }
  
    int stub_id = R.drawable.default_profile_pic;
    
    public void DisplayImage(String url, ImageView imageView)	{
    	DisplayImage(url, stub_id, imageView, 250);
    }
    
    public void DisplayImage(String url, int loader, ImageView imageView)	{
    	DisplayImage(url, loader, imageView, 250);
    }
    
    public void DisplayImage(String url, int loader, ImageView imageView, int size)
    {
        stub_id = loader;
        if (url == null || url.length() == 0)	{
        	imageView.setImageResource(stub_id);
        	return;
        }
        //imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        if(bitmap!=null)
            imageView.setImageBitmap(bitmap);
        else
        {
        	byte[] blob = imageDao.getEntry(url);
        	if (blob != null && blob.length > 0)	{
        		imageView.setImageBitmap(BitmapFactory.decodeByteArray(blob, 0, blob.length));
        		Log.d("lite image loader", "Loaded from DB!");
        	}
            queuePhoto(url, imageView, size);
            imageView.setImageResource(loader);
        }
    }
  
    private void queuePhoto(String url, ImageView imageView, int size)
    {
        PhotoToLoad p=new PhotoToLoad(url, imageView, size);
        executorService.submit(new PhotosLoader(p));
    }
  
    private Bitmap getBitmap(String url, int size)
    {
        File f=fileCache.getFile(url);
  
        //from SD cache
        Bitmap b = decodeFile(f, size);
        if(b!=null)
            return b;
  
        //from web
        Bitmap bitmap=null;
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.copyStream(is, os);
            os.close();
            bitmap = decodeFile(f, size);
        } catch (Exception ex){
           ex.printStackTrace();
        }finally{
        }
        return bitmap;
    }
  
    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f, int size){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
  
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE=size;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
  
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }
    //TODO: use instead of scale down option
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
  
    //Task for the queue
    // size is assumed scaled by user activity; between 0-100, 0 for largest shrinking
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public int size;
        public PhotoToLoad(String u, ImageView i, int s){
            url=u;
            imageView=i;
            size = s;
        }
    }
  
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
  
        @Override
        public void run() {
            if(imageViewReused(photoToLoad))
                return;
            Bitmap bmp=getBitmap(photoToLoad.url, photoToLoad.size);
            memoryCache.put(photoToLoad.url, bmp);
            // Put to DB if not there; this is called every time the view is refreshed
            if (!imageDao.entryExists(photoToLoad.url))	{
	            ByteArrayOutputStream blob = new ByteArrayOutputStream();
	            
	            bmp.compress(CompressFormat.JPEG, 100, blob);
	            imageDao.addEntry(photoToLoad.url, blob.toByteArray());
	            Log.d("lite image loader", "image added to db!");
            }
            if(imageViewReused(photoToLoad))
                return;
            BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
            Activity a=(Activity)photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }
  
    boolean imageViewReused(PhotoToLoad photoToLoad){
//        String tag=imageViews.get(photoToLoad.imageView);
//        if(tag==null || !tag.equals(photoToLoad.url))
//            return true;
        return false;
    }
  
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
        public void run()
        {
            if(imageViewReused(photoToLoad))
                return;
            if(bitmap!=null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else
                photoToLoad.imageView.setImageResource(stub_id);
        }
    }
  
    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }
  
}
