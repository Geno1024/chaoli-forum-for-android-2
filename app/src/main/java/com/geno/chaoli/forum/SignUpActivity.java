package com.geno.chaoli.forum;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.geno.chaoli.forum.meta.Constants;
import com.geno.chaoli.forum.meta.CookieUtils;
import com.geno.chaoli.forum.meta.LoginUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

/**
 * Created by jianhao on 16-4-7.
 * SignUpActivity
 */

public class SignUpActivity extends BaseActivity {
    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mContext = this;
        configToolbar(R.string.sign_up);

        final EditText username_edtTxt = (EditText)findViewById(R.id.edtTxt_username);
        final EditText password_edtTxt = (EditText)findViewById(R.id.edtTxt_password);
        final EditText retype_password_edtTxt = (EditText)findViewById(R.id.edtTxt_retype_password);
        final EditText email_edtTxt = (EditText)findViewById(R.id.edtTxt_email);
        final ImageView captcha_iv = (ImageView)findViewById(R.id.iv_captcha);
        final EditText captcha_edtTxt = (EditText)findViewById(R.id.edtTxt_captcha);
        Button refresh_captcha_btn = (Button) findViewById(R.id.btn_refresh_captcha);
        Button sign_up_btn = (Button)findViewById(R.id.btn_sign_up);
        final AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params = new RequestParams();
        CookieUtils.clearCookie(this);
        CookieUtils.saveCookie(client, this);

        Bundle bundle = getIntent().getExtras();
        String inviteCode = bundle == null ? "" :bundle.getString("inviteCode", "");

        if("".equals(inviteCode)){
            Toast.makeText(getApplicationContext(), R.string.you_can_only_sign_up_with_an_invite_code, Toast.LENGTH_SHORT).show();
            finish();
        }else{
            Toast.makeText(mContext, inviteCode, Toast.LENGTH_LONG).show();
        }

        final String signUpUrl = Constants.SIGN_UP_URL + inviteCode;
        client.get(this, signUpUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.i("sign up", response);
                String tokenFormat = "\"token\":\"([\\dabcdef]+)";
                Pattern pattern = Pattern.compile(tokenFormat);
                Matcher matcher = pattern.matcher(response);
                if (matcher.find()) {
                    Log.i("token", matcher.group(1));
                    params.put("token", matcher.group(1));
                    getAndShowCaptchaImage(client, captcha_iv);
                } else {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("sd", "sdf");
            }
        });
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.btn_sign_up:
                        final String USERNAME_HAS_BEEN_USED = "用户名已经被注册了";
                        final String EMAIL_HAS_BEEN_USED = "邮箱已被注册";
                        final String WRONG_CAPTCHA = "你也许需要一个计算器";

                        final String username = username_edtTxt.getText().toString();
                        final String password = password_edtTxt.getText().toString();
                        String confirm = retype_password_edtTxt.getText().toString();
                        String email = email_edtTxt.getText().toString();

                        TextInputLayout passwordTIL = (TextInputLayout) ((Activity) mContext).findViewById(R.id.passwordTIL);
                        TextInputLayout retypePasswordTIL = (TextInputLayout) ((Activity) mContext).findViewById(R.id.retypePasswordTIL);
                        final TextInputLayout usernameTIL = (TextInputLayout) ((Activity) mContext).findViewById(R.id.usernameTIL);
                        final TextInputLayout emailTIL = (TextInputLayout) ((Activity) mContext).findViewById(R.id.emailTIL);
                        final TextInputLayout captchaTIL = (TextInputLayout) ((Activity) mContext).findViewById(R.id.captchaTIL);

                        passwordTIL.setError("");
                        retypePasswordTIL.setError("");
                        usernameTIL.setError("");
                        emailTIL.setError("");
                        captchaTIL.setError("");

                        if (password.length() < 6) {
                            passwordTIL.setError(getString(R.string.at_least_six_character));
                            return;
                        }
                        if (!password.equals(confirm)) {
                            retypePasswordTIL.setError(getString(R.string.should_be_same_with_password));
                            return;
                        }
                        if (!email.contains("@") || !email.contains(".")) {
                            emailTIL.setError(getString(R.string.invaild_email));
                            return;
                        }
                        params.put("username", username);
                        params.put("email", email);
                        params.put("password", password);
                        params.put("confirm", confirm);
                        params.put("mscaptcha", captcha_edtTxt.getText().toString());
                        params.put("submit", "注册");

                        CookieUtils.saveCookie(client, mContext);
                        final ProgressDialog progressDialog = ProgressDialog.show(mContext, "", getResources().getString(R.string.just_a_sec));
                        progressDialog.show();
                        client.post(signUpUrl, params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                progressDialog.dismiss();
                                String response = new String(responseBody);
                                if(response.contains(USERNAME_HAS_BEEN_USED)){
                                    usernameTIL.setError(getString(R.string.username_has_been_used));
                                }else if(response.contains(EMAIL_HAS_BEEN_USED)){
                                    emailTIL.setError(getString(R.string.email_has_been_used));
                                } else if (response.contains(WRONG_CAPTCHA)) {
                                    captchaTIL.setError(getString(R.string.wrong_captcha));
                                } else {
                                    Toast.makeText(mContext, R.string.sign_up_error, Toast.LENGTH_LONG).show();
                                }
                                getAndShowCaptchaImage(client, captcha_iv);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                progressDialog.dismiss();
                                if(statusCode == 302){  //登录成功
                                    LoginUtils.saveUsernameAndPassword(mContext, username, password);
                                    Toast.makeText(getApplicationContext(), R.string.sign_up_successfully, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent();
                                    intent.setClass(SignUpActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);   //清除所在栈所有Activity
                                    startActivity(intent);
                                }
                            }
                        });
                        break;
                    case R.id.btn_refresh_captcha:
                        getAndShowCaptchaImage(client, captcha_iv);
                        break;
                }
            }
        };
        refresh_captcha_btn.setOnClickListener(onClickListener);
        sign_up_btn.setOnClickListener(onClickListener);
    }

    private void getAndShowCaptchaImage(AsyncHttpClient client, final ImageView captcha_iv){
        captcha_iv.setImageDrawable(getResources().getDrawable(R.drawable.refreshing));
        CookieUtils.saveCookie(client, mContext);
        client.get(Constants.GET_CAPTCHA_URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                //设置图片
                captcha_iv.setImageBitmap(bitmap);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(mContext, R.string.error_when_retrieving_captcha, Toast.LENGTH_LONG).show();
            }
        });
    }
}
