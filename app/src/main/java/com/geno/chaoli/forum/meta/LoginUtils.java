package com.geno.chaoli.forum.meta;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.geno.chaoli.forum.Me;
import com.loopj.android.http.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.cookie.Cookie;

public class LoginUtils {
    private static final String TAG = "LoginUtils";
    public static final String LOGIN_URL = "https://chaoli.club/index.php/user/login?return=%2F";
    public static final String HOMEPAGE_URL = "https://chaoli.club/index.php";
    public static final String LOGOUT_PRE_URL = "https://chaoli.club/index.php/user/logout?token=";
    public static final String COOKIE_UN_AND_PW = "im^#@cookie^$&";
    public static final String LOGIN_SP_NAME = "username_and_password";
    public static final String IS_LOGGED_IN = "is_logged_in";
    public static final String SP_USERNAME_KEY = "username";
    public static final String SP_PASSWORD_KEY = "password";
    public static final int FAILED_AT_OPEN_LOGIN_PAGE = 0;
    public static final int FAILED_AT_GET_TOKEN_ON_LOGIN_PAGE = 1;
    public static final int FAILED_AT_LOGIN = 2;
    public static final int WRONG_USERNAME_OR_PASSWORD = 3;
    public static final int FAILED_AT_OPEN_HOMEPAGE = 4;
    public static final int COOKIE_EXPIRED = 5;
    public static final int EMPTY_UN_OR_PW = 6;
    public static final int ERROR_LOGIN_STATUS = 7;

    private static void setToken(String token) {
        LoginUtils.token = token;
    }

    public static String getToken() {
        return token;
    }

    public static int getUserId() {
        return userId;
    }

    private static void setUserId(int userId) {
        LoginUtils.userId = userId;
    }

    private static String username;
    private static String password;
    private static String token;
    private static int userId;
    private static AsyncHttpClient client = new AsyncHttpClient();

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static void begin_login(final Context context, final String username, final String password, final LoginObserver loginObserver){
        CookieUtils.clearCookie(context); //正常情况下这句应该不会用到，但以防万一
        CookieUtils.saveCookie(client, context);

        sharedPreferences = context.getSharedPreferences(LOGIN_SP_NAME, Context.MODE_PRIVATE);

        if( !sharedPreferences.getBoolean(IS_LOGGED_IN, false)){
            LoginUtils.username = username;
            LoginUtils.password = password;
            pre_login(context, loginObserver);
        }else{
            //如果已经登录，先注销
            logout(context, new LogoutObserver() {
                @Override
                public void onLogoutSuccess() {
                    LoginUtils.username = username;
                    LoginUtils.password = password;
                    pre_login(context, loginObserver);
                }

                @Override
                public void onLogoutFailure(int statusCode) {
                    loginObserver.onLoginFailure(ERROR_LOGIN_STATUS);
                }
            });
        }
    }

    public static void begin_login(final Context context, LoginObserver loginObserver){
        CookieUtils.saveCookie(client, context);

        sharedPreferences = context.getSharedPreferences(LOGIN_SP_NAME, Context.MODE_PRIVATE);
        Boolean is_logged_in = sharedPreferences.getBoolean(IS_LOGGED_IN, false);

        //if(CookieUtils.getCookie(context).size() != 0){

        username = sharedPreferences.getString(SP_USERNAME_KEY, "");
        password = sharedPreferences.getString(SP_PASSWORD_KEY, "");

        if(is_logged_in){
            getNewToken(context, loginObserver);
            //username = password = COOKIE_UN_AND_PW;
            return;
        }

        //Log.i("login_2", "username = " + username + ", password = " + password);

        if("".equals(username) || "".equals(password)){
            loginObserver.onLoginFailure(EMPTY_UN_OR_PW);
            CookieUtils.clearCookie(context);
            return;
        }

        Log.d("login", username + ", " + password);

        begin_login(context, username, password, loginObserver);
    }

    private static void pre_login(final Context context, final LoginObserver loginObserver){//获取登录页面的token
        client.get(context, LOGIN_URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                String tokenFormat = "\"token\":\"([\\dabcdef]+)";
                Pattern pattern = Pattern.compile(tokenFormat);
                Matcher matcher = pattern.matcher(response);
                if (matcher.find()) {
                    setToken(matcher.group(1));
                    login(context, loginObserver);
                } else {
                    //Log.e("regex_error", "regex_error");
                    CookieUtils.clearCookie(context);
                    loginObserver.onLoginFailure(FAILED_AT_GET_TOKEN_ON_LOGIN_PAGE);
                }
                //Log.i("login_page", response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //Log.e("login_error", "");
                CookieUtils.clearCookie(context);
                loginObserver.onLoginFailure(FAILED_AT_OPEN_LOGIN_PAGE);
            }
        });
    }

    private static void login(final Context context, final LoginObserver loginObserver){ //发送请求登录
        RequestParams params = new RequestParams();
        params.put("username", username);
        params.put("password", password);
        params.put("return", "/");
        params.put("login", "登录");
        params.put("token", getToken());
        client.post(context, LOGIN_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //Log.i("after_login", new String(responseBody));
                clear(context);
                loginObserver.onLoginFailure(WRONG_USERNAME_OR_PASSWORD);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                error.printStackTrace(pw);
                if ("Moved Temporarily".equals(error.getMessage())) { //表示登陆成功，若在浏览器中将会跳转到首页
                    getNewToken(context, loginObserver);
                } else {
                    CookieUtils.clearCookie(context);

                    loginObserver.onLoginFailure(FAILED_AT_LOGIN);
                }
            }
        });
    }

    private static void getNewToken(final Context context, final LoginObserver loginObserver){ //得到新的token
        CookieUtils.saveCookie(client, context);
        Log.d("login", "hi");
        client.get(context, HOMEPAGE_URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                //Log.i("homepage", response);
                String tokenFormat = "\"userId\":(\\d+),\"token\":\"([\\dabcdef]+)";
                Pattern pattern = Pattern.compile(tokenFormat);
                Matcher matcher = pattern.matcher(response);
                if (matcher.find()) {
                    int userId = Integer.parseInt(matcher.group(1));
                    setUserId(userId);
                    Me.setUserId(userId);

                    setToken(matcher.group(2));

                    saveUsernameAndPassword(context, username, password);
                    //CookieUtils.setCookies(CookieUtils.getCookie(context));
                    setSPIsLoggedIn(true);
                    Me.setUsername(username);
                    loginObserver.onLoginSuccess(getUserId(), getToken());
                } else {
                    CookieUtils.clearCookie(context);
                    setSPIsLoggedIn(false);
                    //loginObserver.onLoginFailure(COOKIE_EXPIRED);
                    begin_login(context, loginObserver);
                    //Log.e("regex_error", "regex_error");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                CookieUtils.clearCookie(context);
                loginObserver.onLoginFailure(FAILED_AT_OPEN_HOMEPAGE);
            }
        });
    }

    public static void logout(final Context context, final LogoutObserver logoutObserver){
        String logoutURL = LOGOUT_PRE_URL + getToken();
        clear(context);
        Me.clear();
        client.get(context, logoutURL, new AsyncHttpResponseHandler() { //与服务器通信的作用似乎只是告诉服务器我下线了而已
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                logoutObserver.onLogoutSuccess();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, String.valueOf(statusCode));
                logoutObserver.onLogoutFailure(statusCode);
            }
        });
    }

    public static void clear(Context context){
        CookieUtils.clearCookie(context);
        sharedPreferences = context.getSharedPreferences(LOGIN_SP_NAME, Context.MODE_PRIVATE);
        if(sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(IS_LOGGED_IN);
            editor.remove(SP_USERNAME_KEY);
            editor.remove(SP_PASSWORD_KEY);
            editor.apply();
        }
    }

    private static void setSPIsLoggedIn(Boolean isLoggedIn){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public static Boolean isLoggedIn(){
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false);
    }

    public static void saveUsernameAndPassword(Context context, String username, String password){
        sharedPreferences = context.getSharedPreferences(LOGIN_SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SP_USERNAME_KEY, username);
        // TODO: 16-3-11 1915 Encrypt saved password
        editor.putString(SP_PASSWORD_KEY, password);
        editor.apply();
    }

    public interface LoginObserver
    {
        public void onLoginSuccess(int userId, String token);
        public void onLoginFailure(int statusCode);
    }

    public interface LogoutObserver
    {
        public void onLogoutSuccess();
        public void onLogoutFailure(int statusCode);
    }
}
