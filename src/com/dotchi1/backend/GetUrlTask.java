package com.dotchi1.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

/** 
 * A generic class for accessing url. The url is sent in, with params already wired into the string, and the response is returned
 * If we want to access the URL and return a string all we have to do is implement onPostExecute as blank.
 * @author Ivan
 *
 */
public abstract class GetUrlTask extends AsyncTask<String, Void, String> {

	
	@Override
	protected String doInBackground(String... params) {
		//TODO: Send URL request here; input is parameters, key-value pair; output is json returned object.
		String url = params[0];
		HttpClient httpClient = new DefaultHttpClient();
		BufferedReader br = null;
		try	{
			HttpGet httpget =  new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null)	{
				InputStream instream = entity.getContent();
				StringBuilder sb= new StringBuilder();
				String line;
				br = new BufferedReader(new InputStreamReader(instream, "utf-8"));
				while((line = br.readLine()) != null)	{
					sb.append(line);
				}
				return sb.toString();
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally	{
			if (br != null)
				try	{
					br.close();
				} catch (IOException e)	{
					e.printStackTrace();
				}
		} return null;
	}
	
	@Override
	abstract protected void onPostExecute(String result);

}
