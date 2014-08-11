package com.dotchi1.backend;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.dotchi1.model.GameCardItem;

public abstract class SaveImageTask extends AsyncTask<String, Void, String> {

	public static final String TAG = "SaveImageTask";
	
	protected Context context;
	protected GameCardItem item;
	
	public SaveImageTask(Context context, GameCardItem item)	{
		this.context = context;
		this.item = item;
	}
	
	@Override
	protected String doInBackground(String... params) {
		String urlStr = params[0];
		Log.d(TAG, "Sending post url with " + urlStr);
		Log.d(TAG, "Params " + params[1] + " and " + params[2]);

		if (item.getItemImage() != null && !item.getItemImage().equals(""))	{
			BufferedReader br = null;
			try	{
				// Parse first part of code first
				URL url = new URL(params[2]);
				URLConnection connection = url.openConnection();
				connection.connect();
				InputStream input = new BufferedInputStream(url.openStream(), 8192);
				File file = File.createTempFile("temp", ".png", context.getCacheDir());
				OutputStream os = new FileOutputStream(file);
				byte data[] = new byte[1024];
				int count;
				while((count=input.read(data)) != -1)	{
					os.write(data, 0, count);
				}
				os.flush();
				os.close();
				input.close();
				Log.d(TAG, "Image file written at " + file.getAbsolutePath());
				// Compress bitmap from file here before sending out, because we are going to use thumbnail at most
				// TODO: have parameters for input width and height, but for now just default to 300x200 px
				Bitmap bmp = ViewUtils.decodeSampledBitmapFromFile(file, 210, 140);
				File compressedFile = File.createTempFile("compressed_tmp", ".png", context.getCacheDir());
				FileOutputStream compressedOutStream = new FileOutputStream(compressedFile);
				bmp.compress(Bitmap.CompressFormat.PNG, 100, compressedOutStream);
				Log.d(TAG, "Original file size: " + file.length() + " bytes");
				Log.d(TAG, "Compressed file size: " + compressedFile.length() + " bytes");
				// Now send result
				String result = uploadFile(urlStr, params[1], compressedFile.getAbsolutePath()); 
				return result;
				
			} catch(IOException iex)	{
				iex.printStackTrace();
			} finally	{
				if (br != null)
					try	{
						br.close();
					} catch (IOException e)	{
						e.printStackTrace();
					}
			}
		} else{
			// Have to still write the JSON Object so we can convert back 
			ObjectMapper mapper = new ObjectMapper();
			try {
				item.setItemImage("");
				return mapper.writeValueAsString(item); 
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
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
	
	private String uploadFile(String uploadUrl, String srcKey, String srcPath)
	{
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "******";
		try
		{
			URL url = new URL(uploadUrl);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection();
			httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Charset", "UTF-8");
			httpURLConnection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			DataOutputStream dos = new DataOutputStream(
					httpURLConnection.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + end);
			dos.writeBytes("Content-Disposition: form-data; name=\"" + srcKey +"\"; filename=\""
					+ srcPath.substring(srcPath.lastIndexOf("/") + 1)
					+ "\""
					+ end);
			dos.writeBytes(end);

			FileInputStream fis = new FileInputStream(srcPath);
			byte[] buffer = new byte[8192]; // 8k
			int count = 0;
			while ((count = fis.read(buffer)) != -1)
			{
				dos.write(buffer, 0, count);
			}
			fis.close();

			dos.writeBytes(end);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
			dos.flush();

			InputStream is = httpURLConnection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "utf-8");
			BufferedReader br = new BufferedReader(isr);
			String result = br.readLine();

			Log.d("ImageTest", result);
			dos.close();
			is.close();

			return result;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	abstract protected void onPostExecute(String result);

}
