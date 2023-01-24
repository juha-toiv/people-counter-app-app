package com.example.juha.peoplecounterapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity{

    private AutoCompleteTextView mEmailView;
    private TextInputEditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mLoginErrorView;
    private Button mEmailSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_email);
        mLoginErrorView = (TextView) findViewById(R.id.textView_login_error);
        mLoginErrorView.setVisibility(View.GONE);
        mPasswordView = (TextInputEditText) findViewById(R.id.editText_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mEmailSignInButton = (Button) findViewById(R.id.button_email_sign_in);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mLoginFormView = findViewById(R.id.scrollView_login_form);
        mProgressView = findViewById(R.id.progressBar_login_progress);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isUserSignedIn()) {
            startLoginActivity();
        }
    }

    private boolean isUserSignedIn() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            return true;
        }
        return false;
    }

    private void startLoginActivity() {
        Intent intent = new Intent(LoginActivity.this, DeviceListActivity.class);
        startActivity(intent);
        finish();
    }

    private void attemptLogin() {
        if (isUserSignedIn()) {
            return;
        }
        mEmailSignInButton.setEnabled(false);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;
        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.login_form_error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        if (email.isEmpty()) {
            mEmailView.setError(getString(R.string.login_form_error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.login_form_error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
            mEmailSignInButton.setEnabled(true);
        } else {
            showProgress(true);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    showProgress(false);
                    mEmailSignInButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        startLoginActivity();
                    } else {
                        String exceptionMessage = task.getException().getMessage();
                        if (exceptionMessage.contains("password is invalid")) {
                            mPasswordView.setError(getString(R.string.login_form_error_incorrect_password));
                        }
                        if (exceptionMessage.contains("no user record")) {
                            mEmailView.setError(getString(R.string.login_form_error_user_does_not_exist));
                        }
                        if (exceptionMessage.contains("email address")) {
                            mEmailView.setError(getString(R.string.login_form_error_invalid_email));
                        }
                        mLoginErrorView.setVisibility(View.VISIBLE);
                        if (Utilities.isInternetConnection(getApplicationContext()) == false) {
                            mLoginErrorView.setText(getString(R.string.all_no_internet_connection));
                        } else {
                            mLoginErrorView.setText(getString(R.string.login_form_sign_in_failed));
                        }
                    }
                }
            });
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    // TODO: password format
    private boolean isPasswordValid(String password) {
        return password.length() > 0;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}

