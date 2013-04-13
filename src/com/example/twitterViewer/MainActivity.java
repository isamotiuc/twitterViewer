package com.example.twitterViewer;

import java.util.ArrayList;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends Activity {

	String userKey;
	String userSecret;
	ArrayList<String> stringarray = new ArrayList<String>();

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
			GetTimeLine mt;
			mt = new GetTimeLine();
			mt.execute();
		}

	}

	public void tweetBtn_Click(View v) {
		if (!isOnline()) {
			Toast.makeText(getApplicationContext(), "No internet connection",
					Toast.LENGTH_LONG).show();

		} else {
			OpenOauth mt;
			mt = new OpenOauth();
			mt.execute(v);
		}
	}

	

	public void showBtn_Click(View v) {
		if (!isOnline()) {
			Toast.makeText(getApplicationContext(), "No internet connection",
					Toast.LENGTH_LONG).show();

		} else {

			GetTimeLine mt;
			mt = new GetTimeLine();
			mt.execute();
		}

	}

	class OpenOauth extends AsyncTask<View, Void, Void> {

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
		protected Void doInBackground(View... v) {
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
		protected void onPreExecute() {
//			MainActivity.this.progressDialog = ProgressDialog.show(
//					MainActivity.this, "", "Loading...", true);
			super.onPreExecute();
		}

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
			
//			MainActivity.this.progressDialog.dismiss();
		}
	}

	class GetTimeLine extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			MainActivity.this.progressDialog = ProgressDialog.show(
					MainActivity.this, "", "Loading...", true);
			SharedPreferences settings = getBaseContext().getSharedPreferences(
					"your_app_prefs", 0);
			userKey = settings.getString("user_key", "");
			userSecret = settings.getString("user_secret", "");
			Log.w("oauth fail", "key111  " + userKey);
			Log.w("oauth fail", "secret111  " + userSecret);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... vd) {
			try {

				HttpGet get = new HttpGet("http://api.twitter.com/1/statuses/home_timeline.json");
				HttpParams params = new BasicHttpParams();
				HttpProtocolParams.setUseExpectContinue(params, false);
				get.setParams(params);
				
				httpOauthConsumer.setTokenWithSecret(userKey, userSecret);
				httpOauthConsumer.sign(get);

				DefaultHttpClient client = new DefaultHttpClient();
				String response = client.execute(get,
						new BasicResponseHandler());
				Log.w("oauth fail", "response" + response);
				JSONArray array = new JSONArray(response);
				stringarray.clear();
				for (int i = 0; i < array.length(); i++) {
					// int id=array.getJSONObject(i).getInt("id");
					String name = array.getJSONObject(i).getString("text");

					stringarray.add(name);
				}

			} catch (Exception e) {

			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			MainActivity.this.progressDialog.dismiss();

			ListView list = (ListView) findViewById(R.id.listView1);

			ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(
					getBaseContext(),
					android.R.layout.simple_expandable_list_item_1, stringarray);
			list.setAdapter(mArrayAdapter);
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


