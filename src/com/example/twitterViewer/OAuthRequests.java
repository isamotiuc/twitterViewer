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
	protected void onPostExecute(Void result) {
		progressDialog.dismiss();
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

}

class GetUserKeys extends AsyncTask<Void, Void, Boolean> {

	private Context	context;
	private OAuthProvider httpOauthprovider;
	private CommonsHttpOAuthConsumer httpOauthConsumer;
	private String verifier;
	
	public GetUserKeys(Context context,CommonsHttpOAuthConsumer httpOauthConsumer,OAuthProvider httpOauthprovider,String verifier) {
		this.context = context;
		this.httpOauthConsumer = httpOauthConsumer;
		this.httpOauthprovider = httpOauthprovider;
		this.verifier = verifier;
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

//			Log.w("oauth fail", "key  " + userKey);
//			Log.w("oauth fail", "secret  " + userSecret);

		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		
	//	new GetTimeLine(this,httpOauthConsumer,progressDialog,builderUri(),msgList).execute();
		

	}
}
class GetTimeLine extends AsyncTask<Void, Void, Void> {

	private Context	context;
	private CommonsHttpOAuthConsumer httpOauthConsumer;
	private String userKey;
	private String userSecret;
	private Dialog progressDialog;
	private Uri.Builder uriBuilder;
	private int[] colors = new int[2];
	private MessageDetails Detail;
	private ArrayList<MessageDetails> details = new ArrayList<MessageDetails>();
	private ListView msgList;
	
	public GetTimeLine(Context context,CommonsHttpOAuthConsumer httpOauthConsumer,Dialog progressDialog, Uri.Builder uriBuilder, ListView msgList) {
		this.context = context;
		this.httpOauthConsumer = httpOauthConsumer;
		this.progressDialog = progressDialog;
		this.uriBuilder = uriBuilder;
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

//		Log.w("oauth fail", "key  " + userKey);
//		Log.w("oauth fail", "secret  " + userSecret);

		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... p) {

		try {

			DefaultHttpClient mClient = new DefaultHttpClient();
			httpOauthConsumer.setTokenWithSecret(userKey, userSecret);
		
			HttpGet get = new HttpGet(uriBuilder.build().toString());
			httpOauthConsumer.sign(get);
			String response = mClient.execute(get,new BasicResponseHandler());		
			JSONArray array = new JSONArray(response);
			
			SimpleDateFormat curFormater = new SimpleDateFormat(
					"EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
			SimpleDateFormat postFormater = new SimpleDateFormat(
					" dd MMMM, HH:mm", Locale.ENGLISH);

			details = new ArrayList<MessageDetails>();
			
			colors[0] = Color.parseColor("#DDEEF6");
			colors[1] = Color.parseColor("#C0DEED");
			
			for (int i = 0; i < array.length(); i++) {					
			
				String iconPath = array.getJSONObject(i).getJSONObject("user").getString("profile_image_url");
				String name = array.getJSONObject(i).getJSONObject("user").getString("name");
				Date dateObj = curFormater.parse(array.getJSONObject(i).getString("created_at"));
				String newDateStr = postFormater.format(dateObj);
				String desc = array.getJSONObject(i).getString("text");

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

//				Log.w("oauth fail", "get  " + name);
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

