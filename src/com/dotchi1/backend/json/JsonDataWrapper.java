package com.dotchi1.backend.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is meant to wrap whatever incoming data; be it a JSONObject, or JSONArray, with 
 * a data prefix; this is used to follow API protocol.
 * @author Ivan
 *
 */
public class JsonDataWrapper {

	public static String wrapData(JSONArray jsonArray)	{
		if (jsonArray != null && jsonArray.length() != 0)	{
			JSONObject jo = new JSONObject();
			try {
				jo.put("data", jsonArray);
				return jo.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static String wrapData(JSONObject jsonObject)	{
		if (jsonObject != null){
			JSONObject jo = new JSONObject();
			try {
				jo.put("data", jsonObject);
				return jo.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
