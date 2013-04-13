package com.example.twitterViewer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.example.twitterViewer.R;

public class LoginActivity extends Activity {
	 
	private WebView webView;
 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
 
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		
		String url;
		url = getIntent().getExtras().getString("Url");
		webView = new WebView(LoginActivity.this);
		
		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(url);
			
	}

	@Override
	public void onBackPressed()
	{
		
		Intent startIntent = new Intent(this, MainActivity.class);
		startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startIntent);
        
	}
 
}