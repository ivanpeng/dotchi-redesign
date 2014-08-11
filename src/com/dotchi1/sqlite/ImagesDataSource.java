package com.dotchi1.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class ImagesDataSource {

	private SQLiteDatabase database;
	private DotchiSQLiteHelper dbHelper;
	private String[] allColumns = {DotchiSQLiteHelper.KEY_ID, DotchiSQLiteHelper.IMAGES_URL, 
			DotchiSQLiteHelper.IMAGES_VALUE, DotchiSQLiteHelper.KEY_CREATED_AT};
	
	public ImagesDataSource(Context context)	{
		dbHelper = new DotchiSQLiteHelper(context);
	}
	
	public void open() throws SQLiteException	{
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() 	{
		dbHelper.close();
	}
	
	public long addEntry(String name, byte[] imageData) throws SQLiteException	{
		ContentValues cv = new ContentValues();
		cv.put(DotchiSQLiteHelper.IMAGES_URL, name);
		cv.put(DotchiSQLiteHelper.IMAGES_VALUE, imageData);
		cv.put(DotchiSQLiteHelper.KEY_CREATED_AT, SqLiteUtils.getCurrentDateTime());
		// Need to figure out id and created at
		return database.insert(DotchiSQLiteHelper.TABLE_IMAGES, null, cv);
	}
	
	public void deleteById(long id)	{
		database.delete(DotchiSQLiteHelper.TABLE_IMAGES, DotchiSQLiteHelper.KEY_ID + "=" + id, null);
	}
	
	public void deleteByUrl(String name)	{
		database.delete(DotchiSQLiteHelper.TABLE_IMAGES, DotchiSQLiteHelper.IMAGES_URL + "= ?", new String[]{name});
	}
	
	public int deleteOldEntries()	{
		// Delete entries older than 24 hours
		String sql = "DELETE FROM " + DotchiSQLiteHelper.TABLE_IMAGES + " WHERE " + DotchiSQLiteHelper.KEY_CREATED_AT + " <= date('now', '-1 day')";
		database.execSQL(sql);
		Cursor cursor = database.rawQuery("SELECT changes() AS ROW_COUNT FROM " + DotchiSQLiteHelper.TABLE_IMAGES, null);
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst())
			return (int)cursor.getLong(cursor.getColumnIndex("ROW_COUNT"));
		else
			return 0;
	}
	
	public byte[] getEntry(String name){
		// Get image byte array by url
		Cursor c = database.query(DotchiSQLiteHelper.TABLE_IMAGES , new String[]{DotchiSQLiteHelper.IMAGES_VALUE}, DotchiSQLiteHelper.IMAGES_URL + "=?", new String[]{name}, null, null, null);
		//Cursor c = database.query(selectQuery, new String[]{name});
		if (c!= null && c.moveToFirst())	{
			byte[] b = c.getBlob(0);
			c.close();
			return b;
		} else	{
			c.close();
			return null;
		}
	}

	public boolean entryExists(String name)	{
		byte[] b = getEntry(name);
		if (b!= null && b.length > 0)	{
			Log.d("imageDao", "grabbed byte array from DB Table.");
			return true;
		}
		else
			return false;
	}
}
