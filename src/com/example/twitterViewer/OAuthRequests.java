package com.example.twitterViewer;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.Toast;

class OpenOauth extends AsyncTask<Void, Void, Void> {

	private Context	context;
	private OAuthProvider httpOauthprovider;
	private CommonsHttpOAuthConsumer httpOauthConsumer;
	private String CALLBACKURL = "app://twitter";
	private Dialog progressDialog;
	
	
	public OpenOauth(Context context,CommonsHttpOAuthConsumer httpOauthConsumer,OAuthProvider httpOauthprovider,Dialog progressDialog) {
		this.context = context;
		this.httpOauthConsumer = httpOauthConsumer;
		this.httpOauthprovider = httpOauthprovider;
		this.progressDialog = progressDialog;
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(
				context, "", "Loading...", true);
		super.onPreExecute();
	}

	

	@Override
	protected Void doInBackground(Void... v) {
		try {
			String authUrl = httpOauthprovider.retrieveRequestToken(
					httpOauthConsumer, CALLBACKURL);

			Intent intent = new Intent(context,
					LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("Url", authUrl);
			context.startActivity(intent);

		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		progressDialog.dismiss();
	}

}

class GetUserKeys extends AsyncTask<Void, Void, Boolean> {

	private Context	context;
	private OAuthProvider httpOauthprovider;
	private CommonsHttpOAuthConsumer httpOauthConsumer;
	private String verifier;
	private ListView msgList;
	private Dialog progressDialog;

	
	public GetUserKeys(Context context,CommonsHttpOAuthConsumer httpOauthConsumer,OAuthProvider httpOauthprovider,String verifier,ListView msgList,Dialog progressDialog) {
		this.context = context;
		this.httpOauthConsumer = httpOauthConsumer;
		this.httpOauthprovider = httpOauthprovider;
		this.verifier = verifier;
		this.msgList = msgList;
		this.progressDialog = progressDialog;
	}
	
	@Override
	protected Boolean doInBackground(Void... v) {
		try {

			httpOauthprovider.retrieveAccessToken(httpOauthConsumer,
					verifier);
			String userKey = httpOauthConsumer.getToken();
			String userSecret = httpOauthConsumer.getTokenSecret();

			SharedPreferences settings = context
					.getSharedPreferences("your_app_prefs", 0);

			SharedPreferences.Editor editor = settings.edit();
			editor.putString("user_key", userKey);
			editor.putString("user_secret", userSecret);
			editor.commit();

		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		
		new GetTimeLine(context,httpOauthConsumer,progressDialog,1,msgList).execute();
		

	}
}
class GetTimeLine extends AsyncTask<Void, Void, Void> {

	private Context	context;
	private CommonsHttpOAuthConsumer httpOauthConsumer;
	private String userKey;
	private String userSecret;
	private Dialog progressDialog;
	private int[] colors = new int[2];
	private MessageDetails Detail;
	private ArrayList<MessageDetails> details = new ArrayList<MessageDetails>();
	private ListView msgList;
	private int page;
	
	public GetTimeLine(Context context,CommonsHttpOAuthConsumer httpOauthConsumer,Dialog progressDialog, int page, ListView msgList) {
		this.context = context;
		this.httpOauthConsumer = httpOauthConsumer;
		this.progressDialog = progressDialog;
		this.page = page;
		this.msgList = msgList;
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(
				context, "", "Refreshing...", true);
		SharedPreferences settings = context.getSharedPreferences(
				"your_app_prefs", 0);
		userKey = settings.getString("user_key", "");
		userSecret = settings.getString("user_secret", "");

		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... p) {

		try {

			DefaultHttpClient mClient = new DefaultHttpClient();
			httpOauthConsumer.setTokenWithSecret(userKey, userSecret);
		
			//Builder Parameters
			Uri sUri = Uri.parse("http://api.twitter.com/1/statuses/home_timeline.json");
			Uri.Builder builder = sUri.buildUpon();
			builder.appendQueryParameter("count", "20");
			builder.appendQueryParameter("page", String.valueOf(page));
			
			HttpGet get = new HttpGet(builder.build().toString());
			httpOauthConsumer.sign(get);
			String response = mClient.execute(get,new BasicResponseHandler());		
			JSONArray array = new JSONArray(response);
			
			//Format Date
			SimpleDateFormat curFormater = new SimpleDateFormat(
					"EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
			SimpleDateFormat postFormater = new SimpleDateFormat(
					" dd MMMM, HH:mm", Locale.ENGLISH);
			
			colors[0] = Color.parseColor("#DDEEF6");
			colors[1] = Color.parseColor("#C0DEED");
			
			for (int i = 0; i < array.length(); i++) {					
			
				String iconPath = array.getJSONObject(i).getJSONObject("user").getString("profile_image_url");
				String name = array.getJSONObject(i).getJSONObject("user").getString("name");
				Date dateObj = curFormater.parse(array.getJSONObject(i).getString("created_at"));
				String newDateStr = postFormater.format(dateObj);
				String desc = array.getJSONObject(i).getString("text");

				
				//get Icon
				URL url = new URL(iconPath);
		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        connection.setDoInput(true);
		        connection.connect();
		        InputStream input = connection.getInputStream();
		        Bitmap myBitmap = BitmapFactory.decodeStream(input);
		        
				Detail = new MessageDetails();
				Detail.setIcon(myBitmap);
				Detail.setName(name);
				Detail.setTime(newDateStr);
				Detail.setDesc(desc);
				Detail.setColor(colors[i % 2]);

				details.add(Detail);

			}

		} catch (Exception e) {

			Toast.makeText(context, e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		progressDialog.dismiss();
		msgList.setAdapter(new CustomAdapter(details, context));
	}

}

