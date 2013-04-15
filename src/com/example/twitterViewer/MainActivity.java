package com.example.twitterViewer;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ListView msgList;	;
	private int page = 1;

	private String CALLBACKURL = "app://twitter";
	private String consumerKey = "x46JM8cJFabpdUlftDRHJg";
	private String consumerSecret = "8PMnaWNeX6pqhsgDBQAxOA8kpWQdjr8XmngiwDQEZE0";

	private OAuthProvider httpOauthprovider = new DefaultOAuthProvider(
			"https://api.twitter.com/oauth/request_token",
			"https://api.twitter.com/oauth/access_token",
			"https://api.twitter.com/oauth/authorize");
	private CommonsHttpOAuthConsumer httpOauthConsumer = new CommonsHttpOAuthConsumer(
			consumerKey, consumerSecret);
	
	private Dialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		msgList = (ListView) findViewById(R.id.listView1);
		
		if (!isOnline()) {
			Toast.makeText(getApplicationContext(), "No internet connection",
					Toast.LENGTH_LONG).show();
		} else {

			SharedPreferences settings = getBaseContext().getSharedPreferences(
					"your_app_prefs", 0);
			String userKey = settings.getString("user_key", "");

			if (userKey.equals("")) {
				new OpenOauth(this,httpOauthConsumer,httpOauthprovider,progressDialog).execute();
			} else {
				new GetTimeLine(this,httpOauthConsumer,progressDialog,page,msgList).execute();

			}
		}

	}

	public void logintBtn_Click(View v) {
		if (!isOnline()) {
			Toast.makeText(getApplicationContext(), "No internet connection",
					Toast.LENGTH_LONG).show();

		} else {
			new OpenOauth(this,httpOauthConsumer,httpOauthprovider,progressDialog).execute();
		}
	}

	public void refreshBtn_Click(View v) {
		if (!isOnline()) {
			Toast.makeText(getApplicationContext(), "No internet connection",
					Toast.LENGTH_LONG).show();

		} else {
			new GetTimeLine(this,httpOauthConsumer,progressDialog,page,msgList).execute();

		}

	}

	public void nextPageBtn_Click(View v) {
		if (!isOnline()) {
			Toast.makeText(getApplicationContext(), "No internet connection",
					Toast.LENGTH_LONG).show();

		} else {

			btnPreviousVis(true);
			new GetTimeLine(this,httpOauthConsumer,progressDialog,page,msgList).execute();


		}

	}

	public void previousPageBtn_Click(View v) {
		if (!isOnline()) {
			Toast.makeText(getApplicationContext(), "No internet connection",
					Toast.LENGTH_LONG).show();

		} else {
			btnPreviousVis(false);
			new GetTimeLine(this,httpOauthConsumer,progressDialog,page,msgList).execute();

		}

	}

	public void btnPreviousVis(boolean b) {
		Button btnPrevious = (Button) findViewById(R.id.btnPrevious);
		btnPrevious.setVisibility(View.VISIBLE);
		if (b) {
			++page;
			btnPrevious.setVisibility(View.VISIBLE);
		} else {
			if (page == 2) {
				--page;
				btnPrevious.setVisibility(View.INVISIBLE);
			} else
				--page;
		}
	}


	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Log.w("redirect-to-app", "going to save the key and secret");

		Uri uri = intent.getData();
		if (uri != null && uri.toString().startsWith(CALLBACKURL)) {
			if(uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER)!=null){
				String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
				new GetUserKeys(this,httpOauthConsumer,httpOauthprovider,verifier, msgList,progressDialog).execute();		
			}
			
		} else {
			// callback comes from elsewhere
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

	public void onBackPressed() {
		finish();
	}

}
