/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.login;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import de.vogler_engineering.smartdevicesapp.app.phone.R;
import de.vogler_engineering.smartdevicesapp.viewelements.di.Injectable;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.NavigationController;

/**
 * A login screen that offers login via email/password.
 * TODO this is currently not in use.
 */
public class LoginFragment extends Fragment implements Injectable {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    NavigationController navigationController;


//    AutoClearedValue<FragmentLoginBinding> binding;

//    AutoClearedValue<RepoListAdapter> adapter;

    private LoginViewModel loginViewModel;
//

//    @Inject
//    ViewModelFactory<LoginViewModel> viewModelFactory;
//
//    // UI references.
//
//    @BindView(R.id.login_progress)
//    ProgressBar mProgressView;
//
////    @BindView(R.id.login_form)
////    View mLoginFormView;
//
////    @BindView(R.id.login_sign_in_button)
////    Button mSignInButton;
//
//    private LoginViewModel viewModel;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
//    private UserLoginTask mAuthTask = null;

//    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    //AutoClearedValue<LoginFragmentBinding> binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        FragmentLoginBinding dataBinding =
//                DataBindingUtil
//                .inflate(inflater, R.layout.fragment_login, container, false/*,
//                        dataBindingComponent*/);
//        binding = new AutoClearedValue<>(this, dataBinding);
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        return view;
//        return dataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loginViewModel = ViewModelProviders.of(this, viewModelFactory).get(LoginViewModel.class);
    }

    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        AndroidInjection.inject(this);
//        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LoginViewModel.class);
//
//        super.onCreate(savedInstanceState);
//
//        if(viewModel.isSignedIn()){
//            //Redirect to MainView or recent View
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//        }
//
//        de.vogler_engineering.smartdevicesapp.app.phone.ui.login.LoginActivityBinding binding =
//                                DataBindingUtil.setContentView(this, R.layout.fragment_login);
//        binding.setViewModel(viewModel);
//        ButterKnife.bind(this);
//    }

//    /**
//     * Attempts to sign in or register the account specified by the login form.
//     * If there are form errors (invalid email, missing fields, etc.), the
//     * errors are presented and no actual login attempt is made.
//     */
//    private void attemptLogin() {
//        if (mAuthTask != null) {
//            return;
//        }
//
//        // Reset errors.
//        mUsernameView.setError(null);
//        mPasswordView.setError(null);
//
//        // Store values at the time of the login attempt.
//        String email = mUsernameView.getInfoText().toString();
//        String password = mPasswordView.getInfoText().toString();
//
//        boolean cancel = false;
//        View focusView = null;
//
//        // Check for a valid password, if the user entered one.
//        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
//            mPasswordView.setError(getString(R.string.error_invalid_password));
//            focusView = mPasswordView;
//            cancel = true;
//        }
//
//        // Check for a valid email address.
//        if (TextUtils.isEmpty(email)) {
//            mUsernameView.setError(getString(R.string.error_field_required));
//            focusView = mUsernameView;
//            cancel = true;
//        } else if (!isEmailValid(email)) {
//            mUsernameView.setError(getString(R.string.error_invalid_email));
//            focusView = mUsernameView;
//            cancel = true;
//        }
//
//        if (cancel) {
//            // There was an error; don't attempt login and focus the first
//            // form field with an error.
//            focusView.requestFocus();
//        } else {
//            // Show a progress spinner, and kick off a background task to
//            // perform the user login attempt.
//            showProgress(true);
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);
//        }
//    }
//
//    private void showProgress(final boolean show) {
//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//            }
//        });
//
//        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//        mProgressView.animate().setDuration(shortAnimTime).alpha(
//                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            }
//        });
//    }
//
//    /**
//     * Represents an asynchronous login/registration task used to authenticate
//     * the user.
//     */
//    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
//
//        private final String mEmail;
//        private final String mPassword;
//
//        UserLoginTask(String email, String password) {
//            mEmail = email;
//            mPassword = password;
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            // TODO: attempt authentication against a network service.
//
//            try {
//                // Simulate network access.
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                return false;
//            }
//
//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }
//
//            // TODO: register the new account here.
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(final Boolean success) {
//            mAuthTask = null;
//            showProgress(false);
//
//            if (success) {
//                finish();
//            } else {
//                mPasswordView.setError(getString(R.string.error_incorrect_password));
//                mPasswordView.requestFocus();
//            }
//        }
//
//        @Override
//        protected void onCancelled() {
//            mAuthTask = null;
//            showProgress(false);
//        }
//    }
}

