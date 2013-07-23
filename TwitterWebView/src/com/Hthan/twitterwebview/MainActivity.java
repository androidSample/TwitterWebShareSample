package com.Hthan.twitterwebview;

import java.io.IOException;
import java.net.MalformedURLException;

import twitter4j.auth.AccessToken;
import com.coevo.twitter.TwitterStatusListener;
import com.coevo.twitter.TwitterUtils;
import com.coevo.twitter.TwitterWebView;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;

public class MainActivity extends Activity {

	private SharedPreferences prefs;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_main);
		final TwitterWebView twitterWebView = (TwitterWebView) findViewById(R.id.twitterWebView1);
		prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		findViewById(R.id.login).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				twitterWebView.start(statusListener);
			}
		});

		findViewById(R.id.logout).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					TwitterUtils.logout(prefs, mContext);
					AlertDialog.Builder msgBox = new AlertDialog.Builder(MainActivity.this);
					msgBox.setCancelable(false);
					msgBox.setMessage("Logout Success").setPositiveButton("Check", null).show();
					twitterWebView.loadUrl("about:blank");
				} catch (Exception e) {
					// TODO: handle exception
					AlertDialog.Builder msgBox = new AlertDialog.Builder(MainActivity.this);
					msgBox.setCancelable(false);
					msgBox.setMessage("Logout Error").setPositiveButton("Check", null).show();
				}
				
			}
		});

	}

	private TwitterStatusListener statusListener = new TwitterStatusListener() {

		public void onSuccess(WebView view, AccessToken accessToken) {
			// TODO Auto-generated method stub
			sendTweet("Share Msg", "http://www.huffingtonpost.com/theblog/archive/Bolton%20Smiley%20JPG.jpg", accessToken);
		}

		public void onFailure(WebView view) {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					new AlertDialog.Builder(mContext).setMessage("Error").setPositiveButton("Check", null).show();
				}
			});

		}
	};

	public void sendTweet(final String text, final String imageUrl, final AccessToken a) {
		Thread t = new Thread() {
			public void run() {
				try {
					TwitterUtils.sendTweet(prefs, TwitterUtils.checkString(text, imageUrl), mContext, a);
				} catch (Exception ex) {
					ex.printStackTrace();
					runOnUiThread(new Runnable() {
						public void run() {
							// TODO Auto-generated method stub
							new AlertDialog.Builder(mContext).setMessage("Share Error").setPositiveButton("Check", null).show();
						}
					});
				}
			}

		};
		t.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
