package com.coevo.twitter;

import java.util.Timer;
import java.util.TimerTask;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import twitter4j.Twitter;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

//use Lib:
//signpost-commonshttp4
//signpost-core
//twitter4j-core

public class TwitterWebView extends WebView {
	public TwitterWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	private Context mContext;
	private OAuthConsumer consumer;
	private OAuthProvider provider;
	private ProgressDialog loadingDialog;

	public enum Result {
		SUCCESS, CANCELLATION, REQUEST_TOKEN_ERROR, AUTHORIZATION_ERROR, ACCESS_TOKEN_ERROR
	}


	private void init() {
		loadingDialog = new ProgressDialog(mContext);
		loadingDialog.setMessage("Loading...");
		loadingDialog.setCanceledOnTouchOutside(false);
		loadingDialog.setCancelable(false);

		WebSettings settings = getSettings();
		// Not use cache.
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		// Enable JavaScript.
		settings.setJavaScriptEnabled(true);
		// Enable zoom control.
		settings.setBuiltInZoomControls(true);
		// Scroll bar
		setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		this.requestFocusFromTouch();
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		this.requestFocus(View.FOCUS_DOWN);

		this.consumer = new CommonsHttpOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
		this.provider = new CommonsHttpOAuthProvider(Constants.REQUEST_URL, Constants.ACCESS_URL, Constants.AUTHORIZE_URL);
	}

	public void showVirtualKeyboard() {
		Log.d("coevo", "showVirtualKeyboard");
		new Thread(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				InputMethodManager m = (InputMethodManager) ((Activity) mContext).getSystemService(Context.INPUT_METHOD_SERVICE);

				if (m != null) {
					// m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
					m.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
				}
			}
		}).start();
	}

	private TwitterStatusListener StatusListener;

	public void start(TwitterStatusListener StatusListener) {
		init();
		this.StatusListener = StatusListener;
		new TwitterTask().execute();
	}

	private class TwitterTask extends AsyncTask<Void, Void, Result> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			TwitterWebView.this.setWebViewClient(new LocalWebViewClient());
			super.onPreExecute();
		}

		@Override
		protected Result doInBackground(Void... params) {
			// TODO Auto-generated method stub
			try {
				final String url = provider.retrieveRequestToken(consumer, Constants.OAUTH_CALLBACK_URL);
				Log.d("coevo", "load url = " + url);
				((Activity) mContext).runOnUiThread(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						TwitterWebView.this.loadUrl(url);
					}
				});

			} catch (Exception e) {
				// TODO: handle exception
			}

			return null;
		}

	}

	private class LocalWebViewClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
			loadingDialog.show();
		}
		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
			loadingDialog.dismiss();
		}
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO Auto-generated method stub
			Log.d("coevo", "shouldOverrideUrlLoading url = " + url);
			if (url.startsWith(Constants.OAUTH_CALLBACK_URL)) {
				// The URL is not the callback URL.
				new RetrieveAccessTokenTask(mContext, consumer, provider).execute(Uri.parse(url));
				((Activity) mContext).runOnUiThread(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						TwitterWebView.this.loadUrl("about:blank");
					}
				});
			}

			return super.shouldOverrideUrlLoading(view, url);
		}
	}

	public class RetrieveAccessTokenTask extends AsyncTask<Uri, Void, Boolean> {

		private Context context;
		private OAuthProvider provider;
		private OAuthConsumer consumer;
		private SharedPreferences prefs;

		public RetrieveAccessTokenTask(Context context, OAuthConsumer consumer, OAuthProvider provider) {
			this.context = context;
			this.consumer = consumer;
			this.provider = provider;
			this.prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		}

		/**
		 * Retrieve the oauth_verifier, and store the oauth and
		 * oauth_token_secret for future API calls.
		 */
		protected Boolean doInBackground(Uri... params) {
			final Uri uri = params[0];
			// final String oauth_verifier =
			// uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
			final String oauth_verifier = uri.getQueryParameter("oauth_verifier");
			Log.d("coevo", "oauth_verifier = " + oauth_verifier);
			try {
				provider.retrieveAccessToken(consumer, oauth_verifier);
				final Editor edit = prefs.edit();
				Log.d("coevo", "consumer.getToken() = " + consumer.getToken());
				Log.d("coevo", "consumer.getTokenSecret() = " + consumer.getTokenSecret());
				edit.putString(OAuth.OAUTH_TOKEN, consumer.getToken());
				edit.putString(OAuth.OAUTH_TOKEN_SECRET, consumer.getTokenSecret());
				edit.commit();
				consumer.setTokenWithSecret(consumer.getToken(), consumer.getTokenSecret());
				return true;

			} catch (Exception e) {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result) {
				String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
				String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
				Log.d("coevo", "sendTweet.token = " + token);
				Log.d("coevo", "sendTweet.secret = " + secret);
				AccessToken a = new AccessToken(token, secret);
				StatusListener.onSuccess(TwitterWebView.this, a);
			} else {
				StatusListener.onFailure(TwitterWebView.this);
			}
		}

	}

}
