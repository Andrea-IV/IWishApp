package com.example.IWish;

import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.IWish.Model.User;
import com.example.IWish.api.AuthenticationApi;
import com.example.IWish.api.LoginResponse;
import com.example.IWish.api.UserApi;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    CallbackManager callbackManager;
    ProgressDialog mDialog;
    ImageView imgAvatar;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String tokenSaved = sharedPref.getString(getString(R.string.saved_facebook_token), "");

        FacebookSdk.sdkInitialize(this.getApplicationContext());

        if(AccessToken.getCurrentAccessToken() != null){
            Log.i("FACEBOOKLOGIN", AccessToken.getCurrentAccessToken().toString());
            GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    isAlreadyActive(object);
                }
            });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,email,first_name,last_name");
            request.setParameters(parameters);
            request.executeAsync();
        }else{
            Log.i("FACEBOOKLOGIN", tokenSaved);
        }

        View loginAction = findViewById(R.id.loginButton);
        loginAction.setVisibility(View.INVISIBLE);

        View emailInput = findViewById(R.id.emailInput);
        emailInput.setVisibility(View.INVISIBLE);

        View passwordInput = findViewById(R.id.passwordInput);
        passwordInput.setVisibility(View.INVISIBLE);

        View emailInputCreate = findViewById(R.id.emailInputCreate);
        emailInputCreate.setVisibility(View.INVISIBLE);

        View passwordInputCreate = findViewById(R.id.passwordInputCreate);
        passwordInputCreate.setVisibility(View.INVISIBLE);

        View passwordConfirmInputCreate = findViewById(R.id.passwordConfirmInputCreate);
        passwordConfirmInputCreate.setVisibility(View.INVISIBLE);

        View FirstName = findViewById(R.id.FirstName);
        FirstName.setVisibility(View.INVISIBLE);

        View LastName = findViewById(R.id.LastName);
        LastName.setVisibility(View.INVISIBLE);

        View createButton = findViewById(R.id.createButton);
        createButton.setVisibility(View.INVISIBLE);

        View returnButton = findViewById(R.id.returnButton);
        returnButton.setVisibility(View.INVISIBLE);

        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.saved_facebook_token), loginResult.getAccessToken().toString());
                editor.apply();
                mDialog = new ProgressDialog(MainActivity.this);
                mDialog.setMessage("Retrieving data...");
                mDialog.show();

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        mDialog.dismiss();
                        isAlreadyActive(object);
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,email,first_name,last_name");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void isAlreadyActive(JSONObject object) {
        UserApi userApi = new UserApi();
        try {
            JSONArray response = userApi.findsUser("", object.getString("email"), "", "");
            Intent intent = new Intent(this, DashboardActivity.class);

            if(response.toString().equals("[]")){
                User user = userApi.createUser(object.getString("email"), randomPassword(),  object.getString("first_name"), object.getString("last_name"));
                Bundle bundle = new Bundle();
                bundle.putString("USER", user.toString());
                intent.putExtras(bundle);

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }else{
                User user = new User((JSONObject)response.get(0));
                Log.i("FACEBOOKLOGIN", user.toString());
                Bundle bundle = new Bundle();
                bundle.putString("USER", user.toString());
                intent.putExtras(bundle);

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*try{
            imgAvatar = findViewById(R.id.logo);
            URL profile_picture = new URL("https://graph.facebook.com/" + object.getString("id") + "/picture?width=250&height=250");

            Picasso.with(this).load(profile_picture.toString()).into(imgAvatar);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    public static String randomPassword() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(20);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    /*private void printKeyHash() {
        try{
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for(Signature signature: info.signatures){
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }*/

    public void showLoginInput(View view) {
        View emailInput = findViewById(R.id.emailInput);
        emailInput.setVisibility(View.VISIBLE);

        View passwordInput = findViewById(R.id.passwordInput);
        passwordInput.setVisibility(View.VISIBLE);

        View loginAction = findViewById(R.id.loginButton);
        loginAction.setVisibility(View.VISIBLE);

        View returnButton = findViewById(R.id.returnButton);
        returnButton.setVisibility(View.VISIBLE);

        ValueAnimator fadeOut = ValueAnimator.ofFloat(1f, 0f);
        fadeOut.setDuration(500);
        fadeOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                findViewById(R.id.loginShow).setAlpha(alpha);
                findViewById(R.id.login_button).setAlpha(alpha);
                findViewById(R.id.createShow).setAlpha(alpha);
            }
        });
        ValueAnimator fadeIn = ValueAnimator.ofFloat(0f, 1f);
        fadeIn.setDuration(500);
        fadeIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                findViewById(R.id.emailInput).setAlpha(alpha);
                findViewById(R.id.passwordInput).setAlpha(alpha);
                findViewById(R.id.loginButton).setAlpha(alpha);
                findViewById(R.id.returnButton).setAlpha(alpha);
            }
        });
        fadeOut.start();
        fadeIn.start();

        View facebookShow = findViewById(R.id.login_button);
        facebookShow.setVisibility(View.INVISIBLE);

        View loginShow = findViewById(R.id.loginShow);
        loginShow.setVisibility(View.INVISIBLE);

        View createShow = findViewById(R.id.createShow);
        createShow.setVisibility(View.INVISIBLE);
    }

    public void showCreateAccount(View view){
        view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));

        View emailInputCreate = findViewById(R.id.emailInputCreate);
        emailInputCreate.setVisibility(View.VISIBLE);

        View passwordInputCreate = findViewById(R.id.passwordInputCreate);
        passwordInputCreate.setVisibility(View.VISIBLE);

        View passwordConfirmInputCreate = findViewById(R.id.passwordConfirmInputCreate);
        passwordConfirmInputCreate.setVisibility(View.VISIBLE);

        View FirstName = findViewById(R.id.FirstName);
        FirstName.setVisibility(View.VISIBLE);

        View LastName = findViewById(R.id.LastName);
        LastName.setVisibility(View.VISIBLE);

        View createButton = findViewById(R.id.createButton);
        createButton.setVisibility(View.VISIBLE);

        View returnButton = findViewById(R.id.returnButton);
        returnButton.setVisibility(View.VISIBLE);

        ValueAnimator fadeOut = ValueAnimator.ofFloat(1f, 0f);
        fadeOut.setDuration(500);
        fadeOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                findViewById(R.id.titleApp).setAlpha(alpha);
                findViewById(R.id.loginShow).setAlpha(alpha);
                findViewById(R.id.createShow).setAlpha(alpha);
                findViewById(R.id.login_button).setAlpha(alpha);
            }
        });
        ValueAnimator fadeIn = ValueAnimator.ofFloat(0f, 1f);
        fadeIn.setDuration(500);
        fadeIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                findViewById(R.id.emailInputCreate).setAlpha(alpha);
                findViewById(R.id.passwordInputCreate).setAlpha(alpha);
                findViewById(R.id.passwordConfirmInputCreate).setAlpha(alpha);
                findViewById(R.id.FirstName).setAlpha(alpha);
                findViewById(R.id.LastName).setAlpha(alpha);
                findViewById(R.id.createButton).setAlpha(alpha);
                findViewById(R.id.returnButton).setAlpha(alpha);
            }
        });
        fadeOut.start();
        fadeIn.start();

        View facebookShow = findViewById(R.id.login_button);
        facebookShow.setVisibility(View.INVISIBLE);

        View loginShow = findViewById(R.id.loginShow);
        loginShow.setVisibility(View.INVISIBLE);

        View createShow = findViewById(R.id.createShow);
        createShow.setVisibility(View.INVISIBLE);
    }

    public void returnMainMenu(View view){
        View facebookShow = findViewById(R.id.login_button);
        facebookShow.setVisibility(View.VISIBLE);

        View loginShow = findViewById(R.id.loginShow);
        loginShow.setVisibility(View.VISIBLE);

        View createShow = findViewById(R.id.createShow);
        createShow.setVisibility(View.VISIBLE);

        ValueAnimator fadeOut = ValueAnimator.ofFloat(1f, 0f);
        fadeOut.setDuration(500);
        fadeOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                findViewById(R.id.loginButton).setAlpha(alpha);
                findViewById(R.id.emailInput).setAlpha(alpha);
                findViewById(R.id.passwordInput).setAlpha(alpha);
                findViewById(R.id.emailInputCreate).setAlpha(alpha);
                findViewById(R.id.passwordInputCreate).setAlpha(alpha);
                findViewById(R.id.passwordConfirmInputCreate).setAlpha(alpha);
                findViewById(R.id.FirstName).setAlpha(alpha);
                findViewById(R.id.LastName).setAlpha(alpha);
                findViewById(R.id.createButton).setAlpha(alpha);
                findViewById(R.id.returnButton).setAlpha(alpha);
            }
        });
        ValueAnimator fadeIn = ValueAnimator.ofFloat(0f, 1f);
        fadeIn.setDuration(500);
        fadeIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                findViewById(R.id.loginShow).setAlpha(alpha);
                findViewById(R.id.login_button).setAlpha(alpha);
                findViewById(R.id.createShow).setAlpha(alpha);
            }
        });
        fadeOut.start();
        fadeIn.start();

        View loginAction = findViewById(R.id.loginButton);
        loginAction.setVisibility(View.INVISIBLE);

        View emailInput = findViewById(R.id.emailInput);
        emailInput.setVisibility(View.INVISIBLE);

        View passwordInput = findViewById(R.id.passwordInput);
        passwordInput.setVisibility(View.INVISIBLE);

        View emailInputCreate = findViewById(R.id.emailInputCreate);
        emailInputCreate.setVisibility(View.INVISIBLE);

        View passwordInputCreate = findViewById(R.id.passwordInputCreate);
        passwordInputCreate.setVisibility(View.INVISIBLE);

        View passwordConfirmInputCreate = findViewById(R.id.passwordConfirmInputCreate);
        passwordConfirmInputCreate.setVisibility(View.INVISIBLE);

        View FirstName = findViewById(R.id.FirstName);
        FirstName.setVisibility(View.INVISIBLE);

        View LastName = findViewById(R.id.LastName);
        LastName.setVisibility(View.INVISIBLE);

        View createButton = findViewById(R.id.createButton);
        createButton.setVisibility(View.INVISIBLE);

        View returnButton = findViewById(R.id.returnButton);
        returnButton.setVisibility(View.INVISIBLE);
    }

    public void createAccount(View view){
        view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
        View loadingPanel = findViewById(R.id.loadingPanel);
        loadingPanel.setVisibility(View.VISIBLE);

        String email = ((TextView)findViewById(R.id.emailInputCreate)).getText().toString();
        String password = ((TextView)findViewById(R.id.passwordInputCreate)).getText().toString();
        String passwordConfirm = ((TextView)findViewById(R.id.passwordConfirmInputCreate)).getText().toString();
        String firstName = ((TextView)findViewById(R.id.FirstName)).getText().toString();
        String lastName = ((TextView)findViewById(R.id.LastName)).getText().toString();

        if(!password.equals(passwordConfirm)){
            ((TextView)findViewById(R.id.errorText)).setText(R.string.password_not_equal);
        }else{
            tryCreateAccount(email, password, firstName, lastName);
        }
    }

    public void tryCreateAccount(String email, String password, String firstName, String lastName){
        UserApi userApi = new UserApi();
        try {

            User user = userApi.createUser(email, password, firstName, lastName);
            Intent intent = new Intent(this, DashboardActivity.class);
            if(user != null){
                Bundle bundle = new Bundle();
                bundle.putString("USER", user.toString());
                intent.putExtras(bundle);

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }else{
                ((TextView)findViewById(R.id.errorText)).setText(R.string.default_error_creation);
                View loadingPanel = findViewById(R.id.loadingPanel);
                loadingPanel.setVisibility(View.INVISIBLE);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void toDashboard(View view) {
        view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
        View loadingPanel = findViewById(R.id.loadingPanel);
        loadingPanel.setVisibility(View.VISIBLE);

        String login = ((TextView)findViewById(R.id.emailInput)).getText().toString();
        String password = ((TextView)findViewById(R.id.passwordInput)).getText().toString();
        tryLogin(login, password);
    }

    public void tryLogin(String login, String password){
        AuthenticationApi authApi = new AuthenticationApi();
        try {
            LoginResponse res = authApi.login(login, password);
            Intent intent = new Intent(this, DashboardActivity.class);
            if(res.status == 200){
                Bundle bundle = new Bundle();
                bundle.putString("USER", res.user.toString());
                intent.putExtras(bundle);

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }else{
                ((TextView)findViewById(R.id.errorText)).setText(res.message);
                View loadingPanel = findViewById(R.id.loadingPanel);
                loadingPanel.setVisibility(View.INVISIBLE);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
