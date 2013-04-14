package com.example.twitterViewer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends Activity {

	String userKey;
	String userSecret;

	ArrayList<MessageDetails> details = new ArrayList<MessageDetails>();
	ListView msgList;
	MessageDetails Detail;
	int page = 1;
	
	private String CALLBACKURL = "app://twitter";
	private String consumerKey = "x46JM8cJFabpdUlftDRHJg";
	private String consumerSecret = "8PMnaWNeX6pqhsgDBQAxOA8kpWQdjr8XmngiwDQEZE0";

	private OAuthProvider httpOauthprovider = new DefaultOAuthProvider(
			"https://api.twitter.com/oauth/request_token",
			"https://api.twitter.com/oauth/access_token",
			"https://api.twitter.com/oauth/authorize");
	private CommonsHttpOAuthConsumer httpOauthConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
	public Dialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (!isOnline()) {
			Toast.makeText(getApplicationContext(), "No internet connection",
					Toast.LENGTH_LONG).show();
		}
		else{
			
			SharedPreferences settings = getBaseContext().getSharedPreferences(
					"your_app_prefs", 0);
			userKey = settings.getString("user_key", "");
			userSecret = settings.getString("user_secret", "");
			
			if(userKey.equals("")){
				OpenOauth mt;
				mt = new OpenOauth();
				mt.execute();
			}
			else{
				GetTimeLine mt;
				mt = new GetTimeLine();
				mt.execute();
			}
		}

	}

	public void logintBtn_Click(View v) {
		if (!isOnline()) {
			Toast.makeText(getApplicationContext(), "No internet connection",
					Toast.LENGTH_LONG).show();

		} else {
			OpenOauth mt;
			mt = new OpenOauth();
			mt.execute();
		}
	}

	public void refreshBtn_Click(View v) {
		if (!isOnline()) {
			Toast.makeText(getApplicationContext(), "No internet connection",
					Toast.LENGTH_LONG).show();

		} else {

			GetTimeLine mt;
			mt = new GetTimeLine();
			mt.execute();
		}

	}
	
	public void nextPageBtn_Click(View v) {
		if (!isOnline()) {
			Toast.makeText(getApplicationContext(), "No internet connection",
					Toast.LENGTH_LONG).show();

		} else {
			
			btnPreviousVis(true);
			GetTimeLine mt;
			mt = new GetTimeLine();
			mt.execute();
			
		}

	}

	public void previousPageBtn_Click(View v) {
		if (!isOnline()) {
			Toast.makeText(getApplicationContext(), "No internet connection",
					Toast.LENGTH_LONG).show();

		} else {
			btnPreviousVis(false);
			GetTimeLine mt;
			mt = new GetTimeLine();
			mt.execute();
		}

	}
	
	public void btnPreviousVis(boolean b){
		Button btnPrevious = (Button)findViewById(R.id.btnPrevious);
		btnPrevious.setVisibility(View.VISIBLE);
		if(b){
			++page;
			btnPrevious.setVisibility(View.VISIBLE);
		} else {
			if(page==2){
				--page;
				btnPrevious.setVisibility(View.INVISIBLE);
			}
			else --page;
		}
	}
	
	
	class OpenOauth extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			MainActivity.this.progressDialog = ProgressDialog.show(
					MainActivity.this, "", "Loading...", true);
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			MainActivity.this.progressDialog.dismiss();
		}

		@Override
		protected Void doInBackground(Void... v) {
			try {
				StrictMode.enableDefaults();
				String authUrl = httpOauthprovider.retrieveRequestToken(httpOauthConsumer, CALLBACKURL);

				 Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
				 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				 intent.putExtra("Url", authUrl);
				 startActivity(intent);

			} catch (Exception e) {
				Log.w("oauth fail", e);
			}
			return null;
		}

	}

	class GetUserKeys extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... verifier) {
			try {
				// this will populate token and token_secret in consumer

				httpOauthprovider.retrieveAccessToken(httpOauthConsumer,
						verifier[0]);
				String userKey = httpOauthConsumer.getToken();
				String userSecret = httpOauthConsumer.getTokenSecret();

				SharedPreferences settings = getBaseContext()
						.getSharedPreferences("your_app_prefs", 0);

				SharedPreferences.Editor editor = settings.edit();
				editor.putString("user_key", userKey);
				editor.putString("user_secret", userSecret);
				editor.commit();

				Log.w("oauth fail", "key  " + userKey);
				Log.w("oauth fail", "secret  " + userSecret);

			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			
			GetTimeLine mt;
			mt = new GetTimeLine();
			mt.execute();
			
		}
	}

	
	
	class GetTimeLine extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			MainActivity.this.progressDialog = ProgressDialog.show(
					MainActivity.this, "", "Refreshing...", true);
			SharedPreferences settings = getBaseContext().getSharedPreferences(
					"your_app_prefs", 0);
			userKey = settings.getString("user_key", "");
			userSecret = settings.getString("user_secret", "");
			
			Log.w("oauth fail", "key  " + userKey);
			Log.w("oauth fail", "secret  " + userSecret);
			
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... p) {
			
			try {

				DefaultHttpClient mClient = new DefaultHttpClient();
				httpOauthConsumer.setTokenWithSecret(userKey, userSecret);
				
                Uri sUri = Uri.parse("http://api.twitter.com/1/statuses/home_timeline.json");
                Uri.Builder builder = sUri.buildUpon();
                
//              builder.appendQueryParameter("since_id", null); 
//              builder.appendQueryParameter("max_id", null);
		        builder.appendQueryParameter("count", "20");
                builder.appendQueryParameter("page", String.valueOf(page));
                HttpGet get = new HttpGet(builder.build().toString());

                httpOauthConsumer.sign(get);
                String response = mClient.execute(get, new BasicResponseHandler());
	            
                JSONArray array = new JSONArray(response);

				SimpleDateFormat curFormater = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH); 
				SimpleDateFormat postFormater = new SimpleDateFormat(" dd MMMM, HH:mm", Locale.ENGLISH);	
				
				details = new ArrayList<MessageDetails>();
				for (int i = 0; i < array.length(); i++) {

					
					String name = array.getJSONObject(i).getJSONObject("user").getString("name");
					Date dateObj = curFormater.parse(array.getJSONObject(i).getString("created_at")); 		 
					String newDateStr = postFormater.format(dateObj); 
					String desc = array.getJSONObject(i).getString("text");
					
					
					Detail = new MessageDetails();
					
					Detail.setIcon(R.drawable.ic_launcher);
					Detail.setName(name);
					Detail.setSub("Yeah MF");
					Detail.setTime(newDateStr);
					Detail.setDesc(desc);
					
					details.add(Detail);
					
					Log.w("oauth fail", "get  " + name);
				}

				
			} catch (Exception e) {

			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			MainActivity.this.progressDialog.dismiss();

			msgList = (ListView) findViewById(R.id.listView1);			
			msgList.setAdapter(new CustomAdapter(details, getBaseContext() ));
		}

	}

	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Log.w("redirect-to-app", "going to save the key and secret");

		Uri uri = intent.getData();
		if (uri != null && uri.toString().startsWith(CALLBACKURL)) {

			String verifier = uri
					.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);

			GetUserKeys mt;
			mt = new GetUserKeys();
			mt.execute(verifier);
		} else {
			// Do something if the callback comes from elsewhere
		}
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}
	
	public void onBackPressed()
	{	
		finish();     
	}

}


