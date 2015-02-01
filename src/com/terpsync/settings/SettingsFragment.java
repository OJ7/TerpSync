package com.terpsync.settings;

import com.terpsync.R;
import com.terpsync.SignInActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment {

	private static final String TAG = "SettingsFragment";
	public static final String PREFS_NAME = "MyPrefsFile";

	// Global variable strings used for preferences
	private final String signedInPref = "isSignedIn", currentUserPref = "currentUser",
			currentOrgPref = "currentOrganization";

	Preference signInPreference;

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
		signInPreference.setTitle(R.string.pref_title_admin_sign_out);
		signInPreference.setSummary(this.getActivity().getString(
				R.string.pref_summary_admin_signed_in_as)
				+ " " + currentOrganization);
		// Sign out dialog when clicked
		signInPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO - add dialog
				signOutAdmin();
				signInPreference.setOnPreferenceClickListener(null);
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