package com.dotchi1.sqlite;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SqLiteUtils {

	public static String getCurrentDateTime()	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		return sdf.format(date);
	}
}
