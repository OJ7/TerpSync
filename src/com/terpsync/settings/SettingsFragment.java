package com.terpsync.settings;

import java.util.List;
import java.util.Locale;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.terpsync.R;
import com.terpsync.SignInActivity;
import com.terpsync.parse.AdminAccounts;
import com.terpsync.parse.ParseConstants;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment {

	private static final String TAG = "SettingsFragment";
	public static final String PREFS_NAME = "MyPrefsFile";
	private Object lock = new Object();

	// Global variable strings used for preferences
	private final String signedInPref = "isSignedIn", currentUserPref = "currentUser",
			currentOrgPref = "currentOrganization";

	Preference signInPreference;
	Builder manageAccountDialog, changeSignInBuilder;
	String[] manageAccountOptions = { "Change Username/Ppassword", "Sign Out" };
	AlertDialog myDialog;
	EditText mUserView, mPasswordView, mConfirmView;
	Button mChangeButton;
	View changeSignInView;
	AlertDialog changeSignInDialog;
	boolean cancel;
	View focusView;

	// Global variables for Current User (if signed in)
	boolean isSignedIn = false;
	String currentUser = "", currentOrganization = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		signInPreference = (Preference) findPreference("pref_admin_sign_in");
		restorePreferences();
		checkSignInState();
	}

	/**
	 * Saves information about the current user (if signed in) for persistent use.
	 */
	private void savePreferences() {
		Log.i(TAG, "Saving preferences");
		SharedPreferences.Editor editor = this.getActivity().getSharedPreferences(PREFS_NAME, 0)
				.edit();
		editor.putBoolean(signedInPref, isSignedIn);
		editor.putString(currentUserPref, currentUser);
		editor.putString(currentOrgPref, currentOrganization);
		if (editor.commit())
			Log.i(TAG, "Preferences saved successfully");
		else
			Log.i(TAG, "Preferences failed to save");
	}

	/**
	 * Restores information if the user was previously signed in.
	 */
	private void restorePreferences() {
		Log.i(TAG, "Restoring preferences");
		SharedPreferences settings = this.getActivity().getSharedPreferences(PREFS_NAME, 0);
		isSignedIn = settings.getBoolean(signedInPref, false);
		currentUser = settings.getString(currentUserPref, "");
		currentOrganization = settings.getString(currentOrgPref, "");
	}

	/**
	 * Checks if user is signed in and calls the method to update the preference for sign in/out
	 */
	private void checkSignInState() {
		if (isSignedIn) {
			changeToSignedIn();
		} else {
			changeToSignedOut();
		}
	}

	/**
	 * Modify preference if user is signed in
	 */
	private void changeToSignedIn() {
		signInPreference.setTitle(R.string.pref_title_admin_manage);
		signInPreference.setSummary(this.getActivity().getString(
				R.string.pref_summary_admin_signed_in_as)
				+ " " + currentOrganization);
		// Manage account dialog when clicked
		signInPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				showManageAccountDialog();
				return true;
			}
		});
	}

	/**
	 * Modify preference if user is signed out
	 */
	private void changeToSignedOut() {
		signInPreference.setTitle(R.string.pref_title_admin_sign_in);
		signInPreference.setSummary(R.string.pref_summary_admin_sign_in);
		signInPreference.setOnPreferenceClickListener(null);
		Intent intent = new Intent(this.getActivity(), SignInActivity.class);
		signInPreference.setIntent(intent);
	}

	/**
	 * Signs out the current user and resets the FABs appropriately
	 */
	private void signOutAdmin() {
		Log.i(TAG, "Signing out of admin account");
		Toast.makeText(this.getActivity().getApplicationContext(), "Signed in successfully :)",
				Toast.LENGTH_SHORT).show();
		currentUser = "";
		currentOrganization = "";
		isSignedIn = false;
		changeToSignedOut();
		savePreferences();
	}

	/**
	 * Creates a dialog menu with options to change username/password or sign out.
	 */
	private void showManageAccountDialog() {
		Log.i(TAG, "Creating Manage Account Dialog");
		manageAccountDialog = new AlertDialog.Builder(this.getActivity());
		manageAccountDialog.setTitle("Manage Account")
				.setItems(manageAccountOptions, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
							case 0: // Change UN/PW
								showChangeSignInDialog();
								break;
							case 1: // Sign out
								signOutAdmin();
								break;
							default:
								break;
						}
					}
				}).create().show();
	}

	/**
	 * Creates a dialog box to change username and/or password.
	 */
	private void showChangeSignInDialog() {
		Log.i(TAG, "Creating Change Account Info Dialog");

		// Inflating View
		changeSignInView = this.getActivity().getLayoutInflater()
				.inflate(R.layout.dialog_changesignin, null);
		// Caching fields
		mUserView = (EditText) changeSignInView.findViewById(R.id.newUsername);
		mPasswordView = (EditText) changeSignInView.findViewById(R.id.newPassword);
		mConfirmView = (EditText) changeSignInView.findViewById(R.id.newPasswordConfirm);
		mChangeButton = (Button) changeSignInView.findViewById(R.id.changeButton);

		// Creating dialog box
		changeSignInBuilder = new AlertDialog.Builder(this.getActivity());
		changeSignInDialog = changeSignInBuilder.setView(changeSignInView)
				.setTitle("Update Account Info").create();
		changeSignInDialog.show();

		mChangeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				attemptChangeAccountInfo();
			}
		});

	}

	/**
	 * Attempts to change username and/or password (depending on what is specified). If username
	 * already exists and/or passwords do not match, the changes will not be made.
	 * 
	 * Requirements: Usernames and passwords must be at least three characters in length.
	 * 
	 * @return true if changed successfully, false otherwise.
	 */
	private void attemptChangeAccountInfo() {
		Log.i(TAG, "Attempting to change sign in credentials");

		// Reset errors.
		mUserView.setError(null);
		mPasswordView.setError(null);
		mConfirmView.setError(null);

		// Store values at the time of the login attempt.
		final String username = mUserView.getEditableText().toString().toLowerCase(Locale.US)
				.trim();
		final String password = mPasswordView.getEditableText().toString();
		final String confirm = mConfirmView.getEditableText().toString();

		cancel = false;
		focusView = null;

		// Check for a valid password, if the user entered one.
		if (!isPasswordValid(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}
		// Checks if passwords are equal
		else if (!password.equals(confirm)) {
			// TODO - change string for below
			mConfirmView.setError(getString(R.string.error_not_matching_password));
			focusView = mConfirmView;
			cancel = true;
		}

		// Check for a valid username.
		if (!isUserValid(username)) {
			mUserView.setError(getString(R.string.error_invalid_username));
			focusView = mUserView;
			cancel = true;
		} else {
			// confirm username does not already exist
			ParseQuery<AdminAccounts> query = ParseQuery.getQuery(AdminAccounts.class);
			if (!username.equals(currentUser)) { // allow keeping same username
				query.whereContains(ParseConstants.admin_username, username);
				query.findInBackground(new FindCallback<AdminAccounts>() {
					@Override
					public void done(List<AdminAccounts> arg0, ParseException arg1) {
						if (arg0 == null) {
							Log.i(TAG, "Username already exists");
							synchronized (lock) {
								mUserView.setError(getString(R.string.error_existing_username));
								focusView = mUserView;
								cancel = true;
							}
						}
					}
				});
			}
		}

		if (cancel) {
			// There was an error; don't attempt changes and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Perform changes to account
			ParseQuery<AdminAccounts> query = ParseQuery.getQuery(AdminAccounts.class);
			query.whereContains(ParseConstants.admin_username, currentUser);
			query.findInBackground(new FindCallback<AdminAccounts>() {
				@Override
				public void done(List<AdminAccounts> arg0, ParseException arg1) {
					if (arg0 == null || arg0.size() == 0) {
						Log.d(TAG, "The current signed in user is probably set to a different user");
						Toast.makeText(
								getActivity().getBaseContext(),
								"Error changing username/password.\nTry again after signing out and in.",
								Toast.LENGTH_SHORT).show();
					} else if (arg1 == null) {
						AdminAccounts x = arg0.get(0);
						x.setUsername(username);
						x.setPassword(password);
						x.saveInBackground();
						currentUser = username;
						Log.i(TAG, "Change successful!");
						Log.i(TAG, "New username: " + username);
						Log.i(TAG, "New password:" + password);
						Toast.makeText(
								getActivity().getBaseContext(),
								"Username/Password changed successfully:\n[" + username + "]" + ":["
										+ password + "]", Toast.LENGTH_SHORT).show();
					} else { // object retrieval failed throw exception -- fail fast
						arg1.printStackTrace();
					}
					resetChangeSignInDialog();
				}
			});
		}
	}

	private boolean isUserValid(String user) {
		return user.length() > 2;
	}

	private boolean isPasswordValid(String password) {
		return password.length() > 2;
	}

	/**
	 * Resets the change sign in dialog to clear text and remove from view
	 */
	private void resetChangeSignInDialog() {
		Log.i(TAG, "Resetting fields in change sign in credentials dialog");
		mUserView.getText().clear();
		mPasswordView.getText().clear();
		mConfirmView.getText().clear();
		((ViewGroup) changeSignInView.getParent()).removeView(changeSignInView);
		changeSignInDialog.dismiss();
	}

	@Override
	public void onResume() {
		Log.i(TAG, "Resuming Settings Fragment");
		super.onResume();
		restorePreferences();
		checkSignInState();
	}

	@Override
	public void onPause() {
		Log.i(TAG, "Pausing Settings Fragment");
		super.onPause();
		savePreferences();
	}

}