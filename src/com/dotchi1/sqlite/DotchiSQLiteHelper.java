package com.dotchi1.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DotchiSQLiteHelper extends SQLiteOpenHelper {

	// DB
	private static final String DATABASE_NAME = "dotchi.db";
	private static final int DATABASE_VERSION = 1;
	//Table
	public static final String TABLE_INFO = "info";
	public static final String TABLE_IMAGES = "images";
	//Column
	public static final String KEY_ID = "id";
	public static final String KEY_CREATED_AT ="created_at";

	public static final String IMAGES_URL = "url";
	public static final String IMAGES_VALUE = "value";

	// Create statements;
	private static final String CREATE_TABLE_IMAGES = "CREATE TABLE "
			+ TABLE_IMAGES + "(" 
			+ KEY_ID + " INTEGER PRIMARY KEY,"
			+ IMAGES_URL + " TEXT, "
			+ IMAGES_VALUE + " BLOB, "
			+ KEY_CREATED_AT + " DATETIME" + ")";
	
	public DotchiSQLiteHelper(Context context)	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_IMAGES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
		onCreate(db);
	}

}
