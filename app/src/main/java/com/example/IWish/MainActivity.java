package com.example.IWish;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.IWish.api.AuthenticationApi;
import com.example.IWish.api.LoginResponse;
import com.example.IWish.api.UserApi;
import com.example.IWish.api.UserResponse;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        View loginShow = findViewById(R.id.loginShow);
        loginShow.setVisibility(View.INVISIBLE);

        View createShow = findViewById(R.id.createShow);
        createShow.setVisibility(View.INVISIBLE);
    }

    public void returnMainMenu(View view){

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
            UserResponse res = userApi.createUser(email, password, firstName, lastName);
            Intent intent = new Intent(this, DashboardActivity.class);
            if(res.user != null){
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }else{
                ((TextView)findViewById(R.id.errorText)).setText(R.string.default_error_creation);
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
            if(res.message.equals("Login Succesful")){
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
