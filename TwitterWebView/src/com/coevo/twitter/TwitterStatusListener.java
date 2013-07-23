package com.coevo.twitter;

import twitter4j.auth.AccessToken;
import android.webkit.WebView;

public interface TwitterStatusListener
{
    void onSuccess(WebView view, AccessToken accessToken);
    void onFailure(WebView view);
}
