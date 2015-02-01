package com.terpsync;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.terpsync.parse.AdminAccounts;
import com.terpsync.parse.ParseConstants;

/**
 * A login screen that offers login via user/password.
 */
public class SignInActivity extends PreferenceActivity {

	private static final String TAG = "SignInActivity";
	public static final String PREFS_NAME = "MyPrefsFile";
	private final Object lock = new Object();

	// Global variable strings used for preferences
	private final String signedInPref = "isSignedIn", currentUserPref = "currentUser",
			currentOrgPref = "currentOrganization";

	private boolean signedIn = false;
	private String mUser, mPassword;

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserSignInTask mAuthTask = null;

	// UI references.
	private AutoCompleteTextView mUserView;
	private EditText mPasswordView;
	private View mProgressView;
	private View mLoginFormView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);

		// Set up the login form.
		mUserView = (AutoCompleteTextView) findViewById(R.id.user);
		// populateAutoComplete();

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptSignIn();
					return true;
				}
				return false;
			}
		});

		Button mSignInButton = (Button) findViewById(R.id.user_sign_in_button);
		mSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptSignIn();
			}
		});

		mLoginFormView = findViewById(R.id.sign_in_form);
		mProgressView = findViewById(R.id.login_progress);
	}

	/**
	 * Attempts to sign in or register the account specified by the login form. If there are form
	 * errors (invalid email, missing fields, etc.), the errors are presented and no actual login
	 * attempt is made.
	 */
	public void attemptSignIn() {
		Log.i(TAG, "Attempting to sign in");

		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUserView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String username = mUserView.getText().toString().toLowerCase(Locale.US)
				.replaceAll("\\s", "");
		String password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username.
		if (TextUtils.isEmpty(username)) {
			mUserView.setError(getString(R.string.error_field_required));
			focusView = mUserView;
			cancel = true;
		} else if (!isUserValid(username)) {
			mUserView.setError(getString(R.string.error_invalid_username));
			focusView = mUserView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Kick off a background task to perform the user login attempt.
			/*
			 * mAuthTask = new UserSignInTask(username, password); mAuthTask.execute((Void) null);
			 */
			mUser = username;
			mPassword = password;
			ParseQuery<AdminAccounts> query = ParseQuery.getQuery(AdminAccounts.class);
			query.whereContains(ParseConstants.admin_username, mUser);
			query.setLimit(2);
			query.findInBackground(new FindCallback<AdminAccounts>() {
				@Override
				public void done(List<AdminAccounts> arg0, ParseException arg1) {
					if (arg1 != null && arg0.size() < 1) {
						Log.i(TAG, "No organization accounts found");
						/*
						 * Toast.makeText(getApplicationContext(), "Invalid username or password",
						 * Toast.LENGTH_SHORT).show();
						 */
					} else {
						AdminAccounts x = arg0.get(0);
						// Log.i(TAG, "user: " + mUser + " -- " + x.getUsername());
						// Log.i(TAG, "pass" + mPassword + " -- " + x.getPassword());
						if (x.getUsername().equals(mUser) && x.getPassword().equals(mPassword)) {
							SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME,
									MODE_PRIVATE).edit();
							signedIn = true;
							editor.putBoolean(signedInPref, true);
							editor.putString(currentUserPref, x.getUsername());
							editor.putString(currentOrgPref, x.getOrganizatonName());
							Log.i(TAG, "Signed in successfully");
							/*
							 * Toast.makeText(getApplicationContext(), "Signed in successfully :)",
							 * Toast.LENGTH_SHORT).show();
							 */
							if (editor.commit())
								Log.i(TAG, "Preferences saved successfully");
							else
								Log.i(TAG, "Preferences failed to save");
						} else {
							Log.i(TAG, "Sign in failed...either invalid username or password");
							/*
							 * Toast.makeText(getApplicationContext(),
							 * "Invalid username or password", Toast.LENGTH_SHORT).show();
							 */
						}
					}

					mAuthTask = null;

					if (signedIn) {
						finish();
					} else {
						mPasswordView.setError(getString(R.string.error_incorrect_password));
						mPasswordView.requestFocus();
					}
				}
			});

		}
	}

	private boolean isUserValid(String user) {
		// TODO: Replace this with your own logic
		return user.length() > 3;
	}

	private boolean isPasswordValid(String password) {
		// TODO: Replace this with your own logic
		return password.length() > 3;
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate the user.
	 */
	public class UserSignInTask extends AsyncTask<Void, Void, Boolean> {

		private final String mUser;
		private final String mPassword;

		UserSignInTask(String username, String password) {
			mUser = username;
			mPassword = password;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ParseQuery<AdminAccounts> query = ParseQuery.getQuery(AdminAccounts.class);
			query.whereContains(ParseConstants.admin_username, mUser);
			query.setLimit(2);
			query.findInBackground(new FindCallback<AdminAccounts>() {
				@Override
				public void done(List<AdminAccounts> arg0, ParseException arg1) {
					if (arg1 != null && arg0.size() < 1) {
						Log.i(TAG, "No organization accounts found");
						mUserView.setError(getString(R.string.error_invalid_username));
						mUserView.requestFocus();
						/*
						 * Toast.makeText(getApplicationContext(), "Invalid username or password",
						 * Toast.LENGTH_SHORT).show();
						 */
					} else {
						AdminAccounts x = arg0.get(0);
						// Log.i(TAG, "user: " + mUser + " -- " + x.getUsername());
						// Log.i(TAG, "pass" + mPassword + " -- " + x.getPassword());
						if (x.getUsername().equals(mUser) && x.getPassword().equals(mPassword)) {
							SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME,
									MODE_PRIVATE).edit();
							signedIn = true;
							editor.putBoolean(signedInPref, true);
							editor.putString(currentUserPref, x.getUsername());
							editor.putString(currentOrgPref, x.getOrganizatonName());
							Log.i(TAG, "Signed in successfully");
							/*
							 * Toast.makeText(getApplicationContext(), "Signed in successfully :)",
							 * Toast.LENGTH_SHORT).show();
							 */
							if (editor.commit())
								Log.i(TAG, "Preferences saved successfully");
							else
								Log.i(TAG, "Preferences failed to save");
						} else {
							Log.i(TAG, "Sign in failed... invalid password");
							mPasswordView.setError(getString(R.string.error_incorrect_password));
							mPasswordView.requestFocus();
							/*
							 * Toast.makeText(getApplicationContext(),
							 * "Invalid username or password", Toast.LENGTH_SHORT).show();
							 */
						}
					}

					mAuthTask = null;

					if (signedIn) {
						finish();
					} else {
						mPasswordView.setError(getString(R.string.error_incorrect_password));
						mPasswordView.requestFocus();
					}
				}
			});

			return signedIn;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;

			if (success) {
				finish();
			} else {
				mPasswordView.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
		}
	}
}
