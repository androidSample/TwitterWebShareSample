package com.coevo.twitter;

import java.text.SimpleDateFormat;
import java.util.Date;

import oauth.signpost.OAuth;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class TwitterUtils {
	private static String url = "";

	public static AccessToken isAuthenticated(SharedPreferences prefs) {
		if (prefs.contains(OAuth.OAUTH_TOKEN) && prefs.contains(OAuth.OAUTH_TOKEN_SECRET)) {
			String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
			String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
			AccessToken a = new AccessToken(token, secret);
			return a;
		} else {
			return null;
		}
	}
	
	public static void logout(SharedPreferences prefs,Context mContext){
		prefs.edit().remove(OAuth.OAUTH_TOKEN).commit();
		prefs.edit().remove(OAuth.OAUTH_TOKEN_SECRET).commit();
		CookieSyncManager.createInstance(mContext);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeSessionCookie();

	}
	
	public static String getNowTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String currentDateandTime = sdf.format(new Date());
		return currentDateandTime;
	}

	public static String checkString(String text, String imageUrl) {
		String nowTime = getNowTime();
		if (text != null) {
			char[] c = text.toCharArray();
			String returnString = "";
			if (c.length > 135 - imageUrl.length()-nowTime.length()) {
				for (int i = 0; i < 135 - imageUrl.length()-nowTime.length(); i++) {
					returnString = returnString + c[i];
				}
				return returnString + "..."+nowTime+"\n" + imageUrl;

			} else {
				return text + nowTime+"\n" + imageUrl;
			}
		}
		return "";
	}

	public static void sendTweet(SharedPreferences prefs, String msg, final Context context, AccessToken a) throws Exception {
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
		twitter.setOAuthAccessToken(a);
		StatusUpdate status = new StatusUpdate(msg);
		twitter.updateStatus(status);
		((Activity) context).runOnUiThread(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(context).setMessage("Success").setPositiveButton("Check", null).show();
			}
		});

	}

}
