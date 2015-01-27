package com.terpsync;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.terpsync.FloatingActionButton;
import com.terpsync.R;
import com.terpsync.card.EventListActivity;
import com.terpsync.parse.AdminAccounts;
import com.terpsync.parse.EventObject;
import com.terpsync.parse.ParseConstants;
import com.terpsync.parse.UMDBuildings;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	public static final String PREFS_NAME = "MyPrefsFile";

	private final Object lock = new Object();
	private Context context;

	// Global variable strings used for preferences
	private final String adminTogglePref = "adminToggle", currentUserPref = "currentUser",
			currentOrgPref = "currentOrganization";

	// Global variables for Current User (if signed in)
	String currentUser = "", currentOrganization = "";

	// Global variables for FAB
	private FloatingActionButton menuFAB, locationFAB, mapTypeFAB, listFAB, signInFAB, adminFAB;
	private boolean menuExpanded = false, adminSignedIn = false;
	private int locToggle = 0; // 0 = will center on current location, 1 = will center on map
	private int mapToggle = 0; // 0 = normal map, 1 = hybrid map

	// Global variables for Map
	private GoogleMap mMap;
	private final LatLng UMD = new LatLng(38.989822, -76.940637);
	private List<Marker> markers = new ArrayList<Marker>();
	LatLng myLocation = UMD;
	TextView key1, key2, key3;

	// Global variables for Dialog
	AlertDialog.Builder signInBuilder, adminOptionsListBuilder;
	EditText usernameView, passwordView, newUNView, newPWView, newPWConfirmView;
	View signInView = null, changeSignInView = null;
	String[] adminOptions = { "Create an Event", "Manage My Events", "Change Username/Password",
			"Sign Out" };
	boolean validChange = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "Entering onCreate()");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		setupViewsAndCacheWidgets();
		key1 = ((TextView) findViewById(R.id.tv1));
		key2 = ((TextView) findViewById(R.id.tv2));
		key3 = ((TextView) findViewById(R.id.tv3));

		// Check if network is connected before setting up app
		if (!isNetworkAvailable()) {
			openNetworkDialog();
		} else {

			MapsInitializer.initialize(this);

			setupMap();
			queryAndAddEventsFromParse(); // fills map with current events from database
			createInitialFAB(); // creates all FAB objects - better performance
			restorePreferences();
		}
	}

	/**
	 * Restores information if the user was previously signed in.
	 */
	private void restorePreferences() {
		Log.i(TAG, "Restoring preferences");
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		adminSignedIn = settings.getBoolean(adminTogglePref, adminSignedIn);
		currentUser = settings.getString(currentUserPref, currentUser);
		currentOrganization = settings.getString(currentOrgPref, currentOrganization);
	}

	/**
	 * Saves information about the current user (if signed in) for persistent use.
	 */
	private void savePreferences() {
		Log.i(TAG, "Saving preferences");
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(adminTogglePref, adminSignedIn);
		editor.putString(currentUserPref, currentUser);
		editor.putString(currentOrgPref, currentOrganization);
		if (editor.commit())
			Log.i(TAG, "Preferences saved successfully");
		else
			Log.i(TAG, "Preferences failed to save");
	}

	/**
	 * Creates two Floating Action Buttons (FAB): menu and location.
	 */
	private void createInitialFAB() {
		Log.i(TAG, "Creating initial FAB");
		menuFABListener();
		locationFABListener();
	}

	/**
	 * Creates menuFAB and handles clicks on it: either expanding or collapsing the menu.
	 */
	private void menuFABListener() {
		Log.i(TAG, "Creating Menu FAB...");
		// Setting up FAB
		menuFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_star))
				.withButtonColor(Color.RED).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 16).create();
		// Attaching onClickListener
		Log.i(TAG, "...attaching onClickListener");
		menuFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!menuExpanded) { // Expand Menu
					menuExpanded = true;
					expandFABMenu();
					menuFAB.setFloatingActionButtonDrawable(getResources().getDrawable(
							R.drawable.ic_action_cancel));
				} else { // Collapse Menu
					menuExpanded = false;
					collapseFABMenu();
					menuFAB.setFloatingActionButtonDrawable(getResources().getDrawable(
							R.drawable.ic_action_star));
				}
			}
		});
	}

	/**
	 * Creates locationFAB and handles clicks on it: either centering on campus or current location.
	 */
	private void locationFABListener() {
		Log.i(TAG, "Creating Location FAB...");
		// Setting up the appropriate FAB
		if (locToggle == 0) {
			locationFAB = new FloatingActionButton.Builder(this)
					.withDrawable(getResources().getDrawable(R.drawable.ic_action_locate))
					.withButtonColor(Color.parseColor("#00A0B0"))
					.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 86).create();
		} else {
			locationFAB = new FloatingActionButton.Builder(this)
					.withDrawable(getResources().getDrawable(R.drawable.ic_action_locate))
					.withButtonColor(Color.parseColor("#BD1550"))
					.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 86).create();
		}
		locationFAB.hideFloatingActionButton();
		locationFAB.showFloatingActionButton();

		// Attaching onClickListener
		Log.i(TAG, "...attaching onClickListener");
		locationFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (locToggle == 0) { // Centering map on current location
					locToggle = 1;
					locationFAB.setFloatingActionButtonColor(Color.parseColor("#BD1550"));
					centerMapOnMyLocation();
				} else { // Centering map on campus
					locToggle = 0;
					locationFAB.setFloatingActionButtonColor(Color.parseColor("#00A0B0"));
					centerMapOnCampus();
				}
			}
		});
	}

	/**
	 * Creates mapTypeFAB and handles click on it: changing map type to either normal or hybrid
	 */
	private void mapTypeFABListener() {
		Log.i(TAG, "Creating Map Type FAB...");
		// Setting up FAB
		Log.d(TAG, "mapToggle = " + mapToggle);
		if (mapToggle == 0) { // Normal Map
			mapTypeFAB = new FloatingActionButton.Builder(this)
					.withDrawable(getResources().getDrawable(R.drawable.ic_action_map))
					.withButtonColor(Color.parseColor("#00A0B0"))
					.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 86).create();
		} else { // Hybrid Map
			mapTypeFAB = new FloatingActionButton.Builder(this)
					.withDrawable(getResources().getDrawable(R.drawable.ic_satellite))
					.withButtonColor(Color.parseColor("#C7F464"))
					.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 86).create();
		}
		mapTypeFAB.hideFloatingActionButton();
		mapTypeFAB.showFloatingActionButton();

		// Attaching onClickListener
		Log.i(TAG, "...attaching onClickListener");
		mapTypeFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleMapType();
			}
		});
	}

	/**
	 * Creates listFAB and handles click on it: opens up list of all events
	 */
	private void listFABListener() {
		Log.i(TAG, "Creating List FAB...");
		// Setting up FAB
		listFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_database))
				.withButtonColor(Color.parseColor("#CBE86B"))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 156).create();
		listFAB.hideFloatingActionButton();
		listFAB.showFloatingActionButton();

		// Attaching onClickListener
		Log.i(TAG, "...attaching onClickListener");
		listFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, EventListActivity.class);
				intent.putExtra("FilterType", "All");
				startActivityForResult(intent, 0);
			}
		});
	}

	/**
	 * Creates signInFAB and handles click on it: opens a dialog for signing in.
	 */
	private void signInFABListener() {
		Log.i(TAG, "Creating Sign In FAB...");
		// Setting up FAB
		signInFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_gear_50))
				.withButtonColor(Color.parseColor("#FA6900"))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 226).create();
		signInFAB.hideFloatingActionButton();
		signInFAB.showFloatingActionButton();

		// Attaching onClickListener
		Log.i(TAG, "...attaching onClickListener");
		signInFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showSignInDialog();
			}
		});
	}

	/**
	 * Creates adminFAB and handles click on it: opens a dialog menu with options for Admin Panel.
	 */
	private void adminFABListener() {
		Log.i(TAG, "Creating Admin FAB...");
		// Setting up FAB
		adminFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_user))
				.withButtonColor(Color.parseColor("#53777A"))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 226).create();
		adminFAB.hideFloatingActionButton();
		adminFAB.showFloatingActionButton();

		// Attaching onClickListener
		Log.i(TAG, "...attaching onClickListener");
		adminFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAdminPanelDialog();
			}
		});
	}

	/**
	 * Expands the menu to show the following: mapTypeFAB, listFAB, signInFAB/adminFAB
	 */
	private void expandFABMenu() {
		Log.i(TAG, "Expanding FAB Menu");
		locationFAB.hideFloatingActionButton();
		mapTypeFABListener();
		listFABListener();
		if (!adminSignedIn) {
			signInFABListener();
		} else {
			adminFABListener();
		}
	}

	/**
	 * Collapses the menu to revert back to initial FAB layout
	 */
	private void collapseFABMenu() {
		Log.i(TAG, "Collapsing FAB Menu");
		mapTypeFAB.hideFloatingActionButton();
		listFAB.hideFloatingActionButton();
		if (adminSignedIn) {
			adminFAB.hideFloatingActionButton();
		} else {
			signInFAB.hideFloatingActionButton();
		}
		locationFABListener();
	}

	/**
	 * Toggles the map type between normal and hybrid. Also updates the key legend to display better
	 * with each map type.
	 */
	private void toggleMapType() {
		Log.i(TAG, "Toggling Map Type...");
		if (mapToggle == 0) { // Change Map Type to Hybrid
			Log.i(TAG, "...to hybrid");
			mapToggle = 1;
			// Changing FAB icon and color
			mapTypeFAB.setFloatingActionButtonDrawable(getResources().getDrawable(
					R.drawable.ic_satellite));
			mapTypeFAB.setFloatingActionButtonColor(Color.parseColor("#C7F464"));
			// Changing map type
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			// Updating Key Legend
			key1.setTextColor(Color.RED);
			key2.setTextColor(Color.rgb(255, 102, 0)); // ORANGE
			key3.setTextColor(Color.YELLOW);
			Toast.makeText(getApplicationContext(), "Hybrid Map", Toast.LENGTH_SHORT).show();
		} else { // Change Map Type to Normal
			Log.i(TAG, "...to normal");
			mapToggle = 0;
			// Changing FAB icon and color
			mapTypeFAB.setFloatingActionButtonDrawable(getResources().getDrawable(
					R.drawable.ic_action_map));
			mapTypeFAB.setFloatingActionButtonColor(Color.parseColor("#00A0B0"));
			// Changing map type
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			// Updating Key Legend
			key1.setTextColor(Color.BLACK);
			key2.setTextColor(Color.BLACK);
			key3.setTextColor(Color.BLACK);
			Toast.makeText(getApplicationContext(), "Normal Map", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Creates a dialog box to sign-in to an Admin account.
	 */
	private void showSignInDialog() {
		Log.i(TAG, "Creating Sign In Dialog");
		signInBuilder.setView(signInView).setTitle("Enter Your Username and Password")
				.setCancelable(false)
				.setPositiveButton("Sign in", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						attemptSignIn();
						resetSignInDialog();
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						resetSignInDialog();
						dialog.dismiss();
					}
				}).create().show();
	}

	/**
	 * Creates a dialog menu with options to add events, see current user's events, change
	 * username/password, or sign out.
	 */
	private void showAdminPanelDialog() {
		Log.i(TAG, "Creating Admin Panel Dialog");
		adminOptionsListBuilder.setTitle("Admin Panel")
				.setItems(adminOptions, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
						case 0: // Add Event
							Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
							intent.putExtra(ParseConstants.admin_org_name, currentOrganization);
							intent.putExtra("isNewEvent", true);
							startActivityForResult(intent, 0);
							break;
						case 1: // My Events
							showMyEventsList();
							break;
						case 2: // Change PW/UN
							showChangeSignInCredentialsDialog();
							break;
						case 3: // Sign out
							signOutAdmin();
							break;
						default:
							break;
						}
					}
				}).create().show();
	}

	/**
	 * Starts a new EventListActivity filtered to show events from current user
	 */
	private void showMyEventsList() {
		Log.i(TAG, "Starting EventListActivity to show events from user");
		Intent intent = new Intent(MainActivity.this, EventListActivity.class);
		intent.putExtra("FilterType", ParseConstants.event_org_name);
		intent.putExtra(ParseConstants.event_org_name, currentOrganization);
		startActivityForResult(intent, 0);
	}

	/**
	 * Creates a dialog box to change username and/or password.
	 */
	private void showChangeSignInCredentialsDialog() {
		Log.i(TAG, "Creating Change Sign In Credentials Dialog");
		signInBuilder.setView(changeSignInView).setTitle("Update Account Info")
				.setCancelable(false)
				.setPositiveButton("Change", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						attemptChangeSignInCredentials();
						resetChangeSignInDialog();
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						resetChangeSignInDialog();
						dialog.dismiss();
					}
				}).create().show();
	}

	/**
	 * Attempts to sign in using the username and password specified in the sign in dialog box.
	 * NOTE: Should only be called from the sign in dialog box.
	 * 
	 * @return true if sign in is successful, false otherwise.
	 */
	private boolean attemptSignIn() {
		Log.i(TAG, "Attempting to sign in");
		final String UN = usernameView.getEditableText().toString().toLowerCase(Locale.US)
				.replaceAll("\\s", "");
		final String PW = passwordView.getEditableText().toString();

		ParseQuery<AdminAccounts> query = ParseQuery.getQuery(AdminAccounts.class);
		query.whereExists(ParseConstants.admin_username);
		query.setLimit(100);
		query.findInBackground(new FindCallback<AdminAccounts>() {
			@Override
			public void done(List<AdminAccounts> arg0, ParseException arg1) {
				if (arg1 != null) {
					Log.i(TAG, "No organization accounts found");
					Toast.makeText(getApplicationContext(), "Invalid username or password",
							Toast.LENGTH_SHORT).show();
				} else {
					for (AdminAccounts x : arg0) {
						if (x.getUsername().equals(UN) && x.getPassword().equals(PW)) {
							currentUser = x.getUsername();
							currentOrganization = x.getOrganizatonName();
							adminSignedIn = true;
							// Replacing signInFAB
							signInFAB.hideFloatingActionButton();
							adminFABListener();
							break;
						}
					}
					if (adminSignedIn) {
						Log.i(TAG, "Signed in successfully");
						Toast.makeText(getApplicationContext(), "Signed in successfully :)",
								Toast.LENGTH_SHORT).show();
					} else {
						Log.i(TAG, "Sign in failed...either invalid username or password");
						Toast.makeText(getApplicationContext(), "Invalid username or password",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		return adminSignedIn;
	}

	/**
	 * Attempts to change username and/or password (depending on what is specified). If username
	 * already exists and/or passwords do not match, the changes will not be made.
	 * 
	 * Requirements: Usernames and passwords must be at least three characters in length.
	 * 
	 * @return true if changed successfully, false otherwise.
	 */
	private boolean attemptChangeSignInCredentials() {
		Log.i(TAG, "Attempting to change sign in credentials");
		final String newUN = newUNView.getEditableText().toString().toLowerCase(Locale.US).trim();
		final String newPW = newPWView.getEditableText().toString();
		final String newPWConfirm = newPWConfirmView.getEditableText().toString();

		validChange = true;

		ParseQuery<AdminAccounts> query = ParseQuery.getQuery(AdminAccounts.class);

		// confirm new username does not already exist
		if (!newUN.equals(currentUser)) {
			query.whereContains(ParseConstants.admin_username, newUN);
			query.findInBackground(new FindCallback<AdminAccounts>() {
				@Override
				public void done(List<AdminAccounts> arg0, ParseException arg1) {
					if (arg0 == null) {
						Log.i(TAG, "Username already exists");
						Toast.makeText(getBaseContext(), "Username already exists",
								Toast.LENGTH_SHORT).show();
						synchronized (lock) {
							validChange = false;
						}
					}
				}
			});

		}
		// confirm minimum number of characters in username and password
		if (newUN.length() < 4) {
			Log.i(TAG, "Username too short... must be 4+ chars");
			Toast.makeText(getBaseContext(), "Username must be at least 4 characters",
					Toast.LENGTH_SHORT).show();
			synchronized (lock) {
				validChange = false;
			}
		}
		if (newPW.length() < 4) {
			Log.i(TAG, "Password too short... must be 4+ chars");
			Toast.makeText(getBaseContext(), "Password must be at least 4 characters",
					Toast.LENGTH_SHORT).show();
			synchronized (lock) {
				validChange = false;
			}
		}
		// confirm passwords match
		else if (!newPW.equals(newPWConfirm)) {
			Log.i(TAG, "Passwords do not match");
			Toast.makeText(getBaseContext(),
					"Passwords do not match: [" + newPW + "] [" + newPWConfirm + "]",
					Toast.LENGTH_SHORT).show();
			synchronized (lock) {
				validChange = false;
			}
		}

		if (!validChange) { // either username already exists, username/password is not enough
							// characters, or passwords do not match
			Log.i(TAG, "Changing sign in attempt failed... fix above problem(s)");
			return false;
		}

		ParseQuery<AdminAccounts> query2 = ParseQuery.getQuery(AdminAccounts.class);
		query2.whereContains(ParseConstants.admin_username, currentUser);
		query2.findInBackground(new FindCallback<AdminAccounts>() {
			@Override
			public void done(List<AdminAccounts> arg0, ParseException arg1) {
				if (arg0 == null || arg0.size() == 0) {
					Log.d(TAG, "The current signed in user is probably set to a different user");
					Toast.makeText(
							getBaseContext(),
							"Error changing username/password. Try again after signing out and in.",
							Toast.LENGTH_SHORT).show();
				} else if (arg1 == null) {
					arg0.get(0).setUsername(newUN);
					arg0.get(0).setPassword(newPW);
					arg0.get(0).saveInBackground();
					currentUser = newUN;
					Log.i(TAG, "Change successful!");
					Log.i(TAG, "New username: " + newUN);
					Log.i(TAG, "New password:" + newPW);
					Toast.makeText(
							context,
							"Username/Password changed successfully: [" + newUN + "]" + ":["
									+ newPW + "]", Toast.LENGTH_SHORT).show();
				} else { // object retrieval failed throw exception -- fail fast
					arg1.printStackTrace();
				}
			}
		});
		return true;
	}

	/**
	 * Resets the sign in dialog to clear text and remove from view
	 */
	private void resetSignInDialog() {
		Log.i(TAG, "Resetting fields in sign in dialog");
		usernameView.getText().clear();
		passwordView.getText().clear();
		((ViewGroup) signInView.getParent()).removeView(signInView);
	}

	/**
	 * Resets the change sign in dialog to clear text and remove from view
	 */
	private void resetChangeSignInDialog() {
		Log.i(TAG, "Resetting fields in change sign in credentials dialog");
		newUNView.getText().clear();
		newPWView.getText().clear();
		newPWConfirmView.getText().clear();
		((ViewGroup) changeSignInView.getParent()).removeView(changeSignInView);
	}

	/**
	 * Signs out the current user and resets the FABs appropriately
	 */
	private void signOutAdmin() {
		Log.i(TAG, "Signing out of admin account");
		currentUser = "";
		currentOrganization = "";
		adminSignedIn = false;
		// Replaces adminFAB with signInFAB
		adminFAB.hideFloatingActionButton();
		signInFABListener();
		Toast.makeText(getBaseContext(), "Signed out successfully :)", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Gets context for all dialogs builders, inflates all views, and gets all needed references to
	 * views.
	 */
	@SuppressLint("InflateParams")
	private void setupViewsAndCacheWidgets() {
		Log.i(TAG, "Setting up views and caching widgets");
		context = this;
		signInBuilder = new AlertDialog.Builder(this);
		adminOptionsListBuilder = new AlertDialog.Builder(this);
		signInView = getLayoutInflater().inflate(R.layout.dialog_signin, null);
		changeSignInView = getLayoutInflater().inflate(R.layout.dialog_changesignin, null);

		usernameView = (EditText) signInView.findViewById(R.id.username);
		passwordView = (EditText) signInView.findViewById(R.id.password);
		newUNView = (EditText) changeSignInView.findViewById(R.id.newUsername);
		newPWView = (EditText) changeSignInView.findViewById(R.id.newPassword);
		newPWConfirmView = (EditText) changeSignInView.findViewById(R.id.newPasswordConfirm);

		View tview = getLayoutInflater().inflate(R.layout.legend_key_item, null);
		getWindow().addContentView(
				tview,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
		key1 = ((TextView) findViewById(R.id.tv1));
		key2 = ((TextView) findViewById(R.id.tv2));
		key3 = ((TextView) findViewById(R.id.tv3));
	}

	/**
	 * Sets up the map, centers location on UMD campus and adds markers to all buildings with
	 * events. Also handles clicks on marker windows: opens up list of events filtered by building.
	 */
	private void setupMap() {
		Log.i(TAG, "Initializing and setting up map");

		// MapsInitializer.initialize(this);
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		centerMapOnCampus();
		mMap.getUiSettings().setZoomControlsEnabled(false);
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		mMap.setMyLocationEnabled(true);
		mMap.getMyLocation();
		// attaching onClickListeners for all marker windows
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				String buildingName = marker.getTitle();
				Log.i(TAG, "Clicked on marker: " + buildingName);
				Log.i(TAG, "Starting EventListActivity to show events from building");
				Intent intent = new Intent(MainActivity.this, EventListActivity.class);
				intent.putExtra("FilterType", "BuildingName");
				intent.putExtra("BuildingName", buildingName);
				startActivityForResult(intent, 0);
			}
		});
		// Initially sets the colors used with normal map
		key1.setTextColor(Color.BLACK);
		key2.setTextColor(Color.BLACK);
		key3.setTextColor(Color.BLACK);
	}

	/**
	 * Gets all events from database adds to map.
	 */
	private void queryAndAddEventsFromParse() {
		// Clearing markers (if any exist) prior to adding
		Log.i(TAG, "Clearing all markers on map (if any)");
		mMap.clear();
		markers.clear();

		ParseObject.registerSubclass(UMDBuildings.class);
		ParseObject.registerSubclass(EventObject.class);
		ParseObject.registerSubclass(AdminAccounts.class);
		Parse.initialize(this, this.getString(R.string.parse_app_id),
				this.getString(R.string.parse_client_key));
		// Adding current events to map
		ParseQuery<EventObject> eventsQuery = ParseQuery.getQuery(EventObject.class);
		eventsQuery.findInBackground(new FindCallback<EventObject>() {
			@Override
			public void done(List<EventObject> arg0, ParseException arg1) {
				int count = 1;
				for (EventObject x : arg0) {
					Log.d(TAG, "Event #: " + count++);
					ParseQuery<UMDBuildings> buildingsQuery = ParseQuery
							.getQuery(UMDBuildings.class);
					buildingsQuery.whereEqualTo(ParseConstants.building_name, x.getBuildingName());
					buildingsQuery.findInBackground(new FindCallback<UMDBuildings>() {
						@Override
						public void done(List<UMDBuildings> arg0, ParseException arg1) {
							if (arg1 == null) {
								UMDBuildings building = arg0.get(0);
								updateMarker(building, true);
							} else {
								arg1.printStackTrace();
							}
						}
					});
				}
			}
		});
	}

	/**
	 * Places a marker on the building specified. The marker window shows the name of the building
	 * and the number of events happening there. If the marker already exists, this method updates
	 * the number of events.
	 * 
	 * The color of the marker is related to the number of events:
	 * 
	 * (1-2 = YELLOW, 3-5 = ORANGE, 6+ = RED)
	 * 
	 * @param building
	 *            The location to place/update marker at.
	 * @param added
	 *            Represents whether an event was added or deleted from the building. True if added,
	 *            False if deleted.
	 */
	private void updateMarker(UMDBuildings building, boolean added) {
		Double lat = Double.parseDouble(building.getLat());
		Double lon = Double.parseDouble(building.getLng());
		LatLng latLng = new LatLng(lat, lon);
		String name = String.valueOf(building.getName());
		Log.i(TAG, "Updating marker for " + name);
		Marker marker = null;
		int numEvent;
		// Check if marker already exists
		for (Marker m : markers) {
			if (m.getTitle().equals(name)) {
				marker = m;
				break;
			}
		}
		// Adding marker to map (or updating event count if already exists)
		if (marker == null) { // Marker not already on map
			Log.i(TAG, "Marker does not exist... creating marker");
			numEvent = 1;
			marker = mMap.addMarker(new MarkerOptions().position(latLng).title(name));
			markers.add(marker);
		} else { // Marker already on map
			Log.i(TAG, "Marker already exists... updating count");
			int temp = Integer.parseInt(marker.getSnippet().replaceAll("\\D+", ""));
			if (added) {
				numEvent = ++temp;
			} else {
				numEvent = --temp;
			}
		}
		// Set marker color (or delete if no events)
		if (numEvent > 0) {
			// Getting marker color based on number of events
			Log.i(TAG, "Setting marker color based on number of events: " + numEvent);
			float markerColor;
			if (numEvent < 3) {
				markerColor = BitmapDescriptorFactory.HUE_YELLOW;
			} else if (numEvent < 6) {
				markerColor = BitmapDescriptorFactory.HUE_ORANGE;
			} else {
				markerColor = BitmapDescriptorFactory.HUE_RED;
			}
			marker.setSnippet("Events: " + numEvent);
			marker.setIcon(BitmapDescriptorFactory.defaultMarker(markerColor));
		} else {
			Log.i(TAG, "Removing empty marker for " + name);
			marker.remove();
			markers.remove(marker);
		}
	}

	/**
	 * Centers map on current location. If current location can not be resolved, it defaults to UMD
	 * location.
	 */
	private void centerMapOnMyLocation() {
		Log.i(TAG, "Attempting to center map on current location");
		mMap.setMyLocationEnabled(true);
		Location location = mMap.getMyLocation();
		if (location != null) {
			Log.i(TAG, "Centering on current location");
			myLocation = new LatLng(location.getLatitude(), location.getLongitude());
			Toast.makeText(getApplicationContext(), "Centering map on current location",
					Toast.LENGTH_SHORT).show();
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
		} else {
			Log.i(TAG, "Current location not found");
			Toast.makeText(getApplicationContext(),
					"Can't resolve current location\nTry enabling Location Services",
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Centers map on center of campus.
	 */
	private void centerMapOnCampus() {
		Log.i(TAG, "Centering map on campus");
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UMD, 14));
		Toast.makeText(getApplicationContext(), "Centering map on campus", Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * Checks if network is available.
	 * 
	 * @return true if available, false otherwise
	 */
	private boolean isNetworkAvailable() {
		Log.i(TAG, "Checking if network is available");
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * Creates a dialog box displaying message saying network not available. Clicking retry checks
	 * for network again until available.
	 */
	private void openNetworkDialog() {
		Log.i(TAG, "Opening network dialog");
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this)
				.setMessage("Network not available")
				.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (!isNetworkAvailable()) {
							openNetworkDialog();
						}
					}
				}).setNegativeButton("Exit app", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						MainActivity.this.finish();
					}
				}).setCancelable(false);
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	/**
	 * TODO - update documentation
	 * 
	 * @param requestCode
	 *            If this is 0, then this is a result from returning from add event activity with
	 *            the event object that needs to be added to the markers on the map.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
		Log.i(TAG, "Getting activity result");
		String buildings;
		if (requestCode == 0 && resultCode == Activity.RESULT_OK && resultIntent != null) {
			if (resultIntent.getStringExtra("addedNames") != null
					|| resultIntent.getStringExtra("deletedNames") != null) {
				if (resultIntent.getStringExtra("addedNames") != null) {
					buildings = resultIntent.getStringExtra("addedNames");
					Log.i(TAG, "Got result - building(s) added: " + buildings);
					String[] parsedNames = buildings.split(";");
					// Searching for buildings and updating markers
					for (String name : parsedNames) {
						ParseQuery<UMDBuildings> buildingsQuery = ParseQuery
								.getQuery(UMDBuildings.class);
						buildingsQuery.whereEqualTo(ParseConstants.building_name, name);
						buildingsQuery.findInBackground(new FindCallback<UMDBuildings>() {
							@Override
							public void done(List<UMDBuildings> arg0, ParseException arg1) {
								if (arg1 == null) { // Found building, updating marker
									UMDBuildings building = arg0.get(0);
									updateMarker(building, true);
								} else { // object retrieval failed throw exception -- fail fast
									arg1.printStackTrace();
								}
							}
						});
					}
				}
				if (resultIntent.getStringExtra("deletedNames") != null) {
					Log.i(TAG, "Got result - building(s) deleted");
					buildings = resultIntent.getStringExtra("deletedNames");
					String[] parsedNames = buildings.split(";");
					for (String name : parsedNames) {
						ParseQuery<UMDBuildings> buildingsQuery = ParseQuery
								.getQuery(UMDBuildings.class);
						buildingsQuery.whereEqualTo(ParseConstants.building_name, name);
						buildingsQuery.findInBackground(new FindCallback<UMDBuildings>() {
							@Override
							public void done(List<UMDBuildings> arg0, ParseException arg1) {
								if (arg1 == null) {
									UMDBuildings building = arg0.get(0);
									updateMarker(building, false);
								} else {
									arg1.printStackTrace();
								}
							}
						});
					}
				}
			} else {
				Log.i(TAG, "No events added or deleted from previous activity.");
			}
		} else {
			Log.i(TAG, "No activity result");
		}
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "Resuming Main Activity");
		super.onResume();
		restorePreferences();
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "Pausing Main Activity");
		super.onPause();
		savePreferences();
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "Stopping Main Activity");
		super.onStop();
	}
}
