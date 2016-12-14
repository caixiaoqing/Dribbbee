package com.caixiaoqing.dribbbee.dribbble.auth;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.caixiaoqing.dribbbee.dribbble.auth.AuthActivity.KEY_CODE;

/**
 * Created by caixiaoqing on 14/12/16.
 */

public class Auth {

    public static final int REQ_CODE = 100;

    public static final String URI_AUTHORIZE = "https://dribbble.com/oauth/authorize";
    public static final String URI_REDIRECT = "http://www.dribbbee.com";

    private static final String URI_TOKEN = "https://dribbble.com/oauth/token";
    public static final String REDIRECT_URI = "http://www.dribbbee.com";

    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_CLIENT_SECRET = "client_secret";
    private static final String KEY_REDIRECT_URI = "redirect_uri";
    private static final String KEY_SCOPE = "scope";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    // use yours
    private static final String CLIENT_ID = "8e79e1d923f0640e28f7e93b90e7795d093bf72b6e77f9dac4d0c096c9f6ce5e";

    // use yours
    private static final String CLIENT_SECRET = "9e69b2f848fda3c1beb1325690e2c6a604133971647a3cdb5dcccbc4dfffc29f";

    // see http://developer.dribbble.com/v1/oauth/#scopes
    private static final String SCOPE = "public+write";

    public static void openAuthActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, AuthActivity.class);
        intent.putExtra(AuthActivity.KEY_URL, getAuthorizeUrl());

        activity.startActivityForResult(intent, REQ_CODE);
    }


    private static String getAuthorizeUrl() {
        String url = Uri.parse(URI_AUTHORIZE)
                .buildUpon()
                .appendQueryParameter(KEY_CLIENT_ID, CLIENT_ID)
                .build()
                .toString();

        // fix encode issue
        url += "&" + KEY_REDIRECT_URI + "=" + REDIRECT_URI;
        url += "&" + KEY_SCOPE + "=" + SCOPE;

        return url;
    }

    public static String fetchAccessToken(String authCode) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody postBody = new FormBody.Builder()
                .add(KEY_CLIENT_ID, CLIENT_ID)
                .add(KEY_CLIENT_SECRET, CLIENT_SECRET)
                .add(KEY_CODE, authCode)
                .add(KEY_REDIRECT_URI, REDIRECT_URI)
                .build();
        Request request = new Request.Builder()
                .url(URI_TOKEN)
                .post(postBody)
                .build();
        Response response = client.newCall(request).execute();

        String responseString = response.body().string();

        Log.i("Auth-xqc", responseString);
        try {
            JSONObject obj = new JSONObject(responseString);
            return obj.getString(KEY_ACCESS_TOKEN);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}
