package com.dotchi1.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;
import android.util.Log;

/**
 * A generic class for posting url with parameters after the first url
 * We need to make post-processing customizable, because we don't know what we want to do with the post results.
 * @author Ivan
 *
 */
public abstract class PostUrlTask extends AsyncTask<String, Void, String> {

	public static final String TAG = "PostUrlTask";
	@Override
	protected String doInBackground(String... params) {
		String url = params[0];
		Log.d(TAG, "Sending post url with " + url);
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		for (int i = 0; i < params.length-2; i+=2)	{
			parameters.add(new BasicNameValuePair(params[i+1], params[i+2]));
			Log.d(TAG, params[i+1] + "=" +  params[i+2]);
		}
		BufferedReader br = null;
		try	{
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httppost =  new HttpPost(url);
			httppost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
			HttpResponse response = httpClient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null)	{
				InputStream instream = entity.getContent();
				StringBuilder sb= new StringBuilder();
				String line;
				br = new BufferedReader(new InputStreamReader(instream,"utf-8"));
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
	
	protected String processResult(String result)	{
		// This method formats the JSON response. There's an error with PHP server sending an encrypted beginning.
		int idx = result.indexOf("{");
		Log.d(TAG, "Starting at index " + idx + " for result " + result);
		if (idx == -1)	
			return "{}";
		else
			return result.substring(idx);
	}

	@Override
	abstract protected void onPostExecute(String result);	
	
}
