package com.caixiaoqing.dribbbee.dribbble;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.caixiaoqing.dribbbee.model.User;
import com.caixiaoqing.dribbbee.utils.ModelUtils;
import com.caixiaoqing.dribbbee.view.LoginActivity;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by caixiaoqing on 13/12/16.
 */

public class Dribbble {
    private static final String TAG = "Dribbble API";

    private static final String SP_AUTH = "auth";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER = "user";

    private static final String API_URL = "https://api.dribbble.com/v1/";

    private static final String USER_END_POINT = API_URL + "user";

    private static final TypeToken<User> USER_TYPE = new TypeToken<User>(){};

    private static String accessToken;
    private static User user;

    private static OkHttpClient client = new OkHttpClient();

    public static void init(@NonNull Context context) {
        accessToken = loadAccessToken(context);

        if (accessToken != null) {
            Log.i("xqc", accessToken);
            user = loadUser(context);
        }
        else{
            Log.i("xqc", "accessToken is null");
        }
    }

    public static boolean isLoggedIn() {
        return accessToken != null;
    }

    public static String loadAccessToken(@NonNull Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(
                SP_AUTH, Context.MODE_PRIVATE);
        return sp.getString(KEY_ACCESS_TOKEN, null);
    }

    public static User loadUser(@NonNull Context context) {
        return ModelUtils.read(context, KEY_USER, new TypeToken<User>(){});
    }

    public static void login(@NonNull Context context,
                             @NonNull String accessToken) throws IOException, JsonSyntaxException {

        Dribbble.accessToken = accessToken;
        storeAccessToken(context, accessToken);

        Dribbble.user = getUser();
        storeUser(context, user);
    }

    public static void logout(@NonNull Context context) {
        Log.i("xqc", "logout");
        storeAccessToken(context, null);
        storeUser(context, null);

        accessToken = null;
        user = null;
    }

    private static void storeAccessToken(@NonNull Context context, @NonNull String token) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(
                SP_AUTH, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public static User getCurrentUser() {
        return user;
    }

    public static User getUser() throws IOException, JsonSyntaxException {
        return parseResponse(makeGetRequest(USER_END_POINT), USER_TYPE);
    }

    private static void storeUser(Context context, User user) {
        ModelUtils.save(context, KEY_USER, user);
    }

    //Dribbble Request methods
    private static Request.Builder authRequestBuilder(String url) {
        return new Request.Builder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .url(url);
    }

    private static Response makeRequest(Request request) throws IOException {
        Response response = client.newCall(request).execute();
        Log.d(TAG, response.header("X-RateLimit-Remaining"));
        return response;
    }

    private static Response makeGetRequest(String url) throws IOException {
        Request request = authRequestBuilder(url).build();
        return makeRequest(request);
    }

    private static <T> T parseResponse(Response response,
                                       TypeToken<T> typeToken) throws IOException, JsonSyntaxException {
        String responseString = response.body().string();
        Log.d(TAG, responseString);
        return ModelUtils.toObject(responseString, typeToken);
    }
}
