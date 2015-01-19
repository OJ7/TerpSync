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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseException;
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
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		adminSignedIn = settings.getBoolean(adminTogglePref, adminSignedIn);
		currentUser = settings.getString(currentUserPref, currentUser);
		currentOrganization = settings.getString(currentOrgPref, currentOrganization);
	}

	/**
	 * Saves information about the current user (if signed in) for persistent use.
	 */
	private void savePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(adminTogglePref, adminSignedIn);
		editor.putString(currentUserPref, currentUser);
		editor.putString(currentOrgPref, currentOrganization);
		editor.commit();
	}

	/**
	 * Creates two Floating Action Buttons (FAB): menu and location.
	 */
	private void createInitialFAB() {
		menuFABListener();
		locationFABListener();
	}

	/**
	 * Creates menuFAB and handles clicks on it: either expanding or collapsing the menu.
	 */
	private void menuFABListener() {
		// Setting up FAB
		menuFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_star))
				.withButtonColor(Color.RED).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 16).create();
		// Attaching onClickListener
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
		// Setting up FAB
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
		// Setting up FAB
		listFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_database))
				.withButtonColor(Color.parseColor("#CBE86B"))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 156).create();
		listFAB.hideFloatingActionButton();
		listFAB.showFloatingActionButton();

		// Attaching onClickListener
		listFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, EventListActivity.class);
				intent.putExtra("FilterType", "All");
				startActivity(intent);
			}
		});
	}

	/**
	 * Creates signInFAB and handles click on it: opens a dialog for signing in.
	 */
	private void signInFABListener() {
		// Setting up FAB
		signInFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_gear_50))
				.withButtonColor(Color.parseColor("#FA6900"))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 226).create();
		signInFAB.hideFloatingActionButton();
		signInFAB.showFloatingActionButton();

		// Attaching onClickListener
		signInFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showSignInView();
			}
		});
	}

	/**
	 * Creates adminFAB and handles click on it: opens a dialog menu with options for Admin Panel.
	 */
	private void adminFABListener() {
		// Setting up FAB
		adminFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_user))
				.withButtonColor(Color.parseColor("#53777A"))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 226).create();
		adminFAB.hideFloatingActionButton();
		adminFAB.showFloatingActionButton();

		// Attaching onClickListener
		adminFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAdminPanel();
			}
		});
	}

	/**
	 * Expands the menu to show the following: mapTypeFAB, listFAB, signInFAB/adminFAB
	 */
	private void expandFABMenu() {
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
		if (mapToggle == 0) { // Change Map Type to Hybrid
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
	private void showSignInView() {
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
	private void showAdminPanel() {
		adminOptionsListBuilder.setTitle("Admin Panel")
				.setItems(adminOptions, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
						case 0: // Add Event
							startActivityForResult(new Intent(MainActivity.this,
									AddEventActivity.class).putExtra(ParseConstants.admin_org_name,
									currentOrganization), 0);
							break;
						case 1: // My Events
							showMyEventsList();
							break;
						case 2: // Change PW/UN
							showChangeSignInView();
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
		Intent intent = new Intent(MainActivity.this, EventListActivity.class);
		intent.putExtra("FilterType", ParseConstants.event_org_name);
		intent.putExtra(ParseConstants.event_org_name, currentOrganization);
		startActivity(intent);
	}

	/**
	 * Creates a dialog box to change username and/or password.
	 */
	private void showChangeSignInView() {
		signInBuilder.setView(changeSignInView).setTitle("Update Account Info")
				.setCancelable(false)
				.setPositiveButton("Change", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						attemptChangeSignIn();
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
		final String UN = usernameView.getEditableText().toString().toLowerCase()
				.replaceAll("\\s", "");
		final String PW = passwordView.getEditableText().toString();

		ParseQuery<AdminAccounts> query = ParseQuery.getQuery(AdminAccounts.class);
		query.whereExists(ParseConstants.admin_username);
		query.setLimit(100);
		query.findInBackground(new FindCallback<AdminAccounts>() {
			@Override
			public void done(List<AdminAccounts> arg0, ParseException arg1) {
				if (arg1 != null) {
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
						Toast.makeText(getApplicationContext(), "Signed in successfully :)",
								Toast.LENGTH_SHORT).show();
					} else {
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
	private boolean attemptChangeSignIn() {
		final String newUN = newUNView.getEditableText().toString().toLowerCase().trim();
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
			Toast.makeText(getBaseContext(), "Username must be at least 4 characters",
					Toast.LENGTH_SHORT).show();
			synchronized (lock) {
				validChange = false;
			}
		}
		if (newPW.length() < 4) {
			Toast.makeText(getBaseContext(), "Password must be at least 4 characters",
					Toast.LENGTH_SHORT).show();
			synchronized (lock) {
				validChange = false;
			}
		}
		// confirm passwords match
		else if (!newPW.equals(newPWConfirm)) {
			Toast.makeText(getBaseContext(),
					"Passwords do not match: [" + newPW + "] [" + newPWConfirm + "]",
					Toast.LENGTH_SHORT).show();
			synchronized (lock) {
				validChange = false;
			}
		}

		if (!validChange) // either username already exists, username/password is not enough
							// characters, or passwords do not match
			return false;

		ParseQuery<AdminAccounts> query2 = ParseQuery.getQuery(AdminAccounts.class);
		query2.whereContains(ParseConstants.admin_username, currentUser);
		query2.findInBackground(new FindCallback<AdminAccounts>() {
			@Override
			public void done(List<AdminAccounts> arg0, ParseException arg1) {
				if (arg0 == null || arg0.size() == 0) {
					Toast.makeText(
							getBaseContext(),
							"Error changing username/password. Try again after signing out and in.",
							Toast.LENGTH_SHORT).show();
				} else if (arg1 == null) {
					arg0.get(0).setUsername(newUN);
					arg0.get(0).setPassword(newPW);
					arg0.get(0).saveInBackground();
					currentUser = newUN;
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
		usernameView.getText().clear();
		passwordView.getText().clear();
		((ViewGroup) signInView.getParent()).removeView(signInView);
	}

	/**
	 * Resets the change sign in dialog to clear text and remove from view
	 */
	private void resetChangeSignInDialog() {
		newUNView.getText().clear();
		newPWView.getText().clear();
		newPWConfirmView.getText().clear();
		((ViewGroup) changeSignInView.getParent()).removeView(changeSignInView);
	}

	/**
	 * Signs out the current user and resets the FABs appropriately
	 */
	private void signOutAdmin() {
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
	private void setupViewsAndCacheWidgets() {
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
				Intent intent = new Intent(MainActivity.this, EventListActivity.class);
				intent.putExtra("FilterType", "BuildingName");
				intent.putExtra("BuildingName", buildingName);
				startActivity(intent);
			}
		});
		// Initially sets the colors used with normal map
		key1.setTextColor(Color.BLACK);
		key2.setTextColor(Color.BLACK);
		key3.setTextColor(Color.BLACK);
	}

	/**
	 * Gets all events from database and either adds event to map or deletes from database if the
	 * event has passed.
	 */
	private void queryAndAddEventsFromParse() {
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
					Log.i(TAG, "Event number  is " + count++);
					// Checking if date has passed
					boolean oldEvent = false;
					SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
					try {
						if (format.parse(x.getEndDate()).before(new Date())) {
							Log.i(TAG, "The event " + x.getEventName() + " has passed");
							oldEvent = true;
						}
					} catch (java.text.ParseException e) {
						e.printStackTrace();
					}
					if (oldEvent) { // deleting from database
						x.deleteInBackground();

					} else { // adding to map
						ParseQuery<UMDBuildings> buildingsQuery = ParseQuery
								.getQuery(UMDBuildings.class);
						buildingsQuery.whereEqualTo(ParseConstants.building_name,
								x.getBuildingName());
						buildingsQuery.findInBackground(new FindCallback<UMDBuildings>() {
							@Override
							public void done(List<UMDBuildings> arg0, ParseException arg1) {
								UMDBuildings building = arg0.get(0);
								updateMarker(building, true);
							}
						});
					}
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
			numEvent = 1;
			marker = mMap.addMarker(new MarkerOptions().position(latLng).title(name));
			markers.add(marker);
		} else { // Marker already on map
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
			marker.remove();
		}
	}

	/**
	 * Centers map on current location. If current location can not be resolved, it defaults to UMD
	 * location.
	 */
	private void centerMapOnMyLocation() {
		mMap.setMyLocationEnabled(true);
		Location location = mMap.getMyLocation();
		if (location != null) {
			myLocation = new LatLng(location.getLatitude(), location.getLongitude());
		}
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));

		Toast.makeText(getApplicationContext(), "Centering map on current location",
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * Centers map on center of campus.
	 */
	private void centerMapOnCampus() {
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
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * Creates a dialog box displaying message saying network not available. Clicking retry checks
	 * for network again until available.
	 */
	private void openNetworkDialog() {
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
		String buildingName;
		if (requestCode == 0 && resultCode == Activity.RESULT_OK && resultIntent != null) {
			if (resultIntent.getStringExtra("addBuildingName") != null) {
				buildingName = resultIntent.getStringExtra("addBuildingName");
				ParseQuery<UMDBuildings> buildingsQuery = ParseQuery.getQuery(UMDBuildings.class);
				buildingsQuery.whereEqualTo(ParseConstants.building_name, buildingName);
				buildingsQuery.findInBackground(new FindCallback<UMDBuildings>() {
					@Override
					public void done(List<UMDBuildings> arg0, ParseException arg1) {
						UMDBuildings building = arg0.get(0);
						updateMarker(building, true);
						Toast.makeText(getApplicationContext(), "Added event to map",
								Toast.LENGTH_SHORT).show();
					}
				});
			} else if (resultIntent.getBooleanExtra("deletedEvent", false)) {
				Log.i(TAG, "Event(s) deleted, rebuilding map markers");
				queryAndAddEventsFromParse();
				// Note: Instead of getting events to delete from map, just remaking the map.
				// A lazy implementation, but gets around the bug for now.

				/*
				 * buildingName = resultIntent.getStringExtra("deleteBuildingName");
				 * ParseQuery<UMDBuildings> buildingsQuery2 =
				 * ParseQuery.getQuery(UMDBuildings.class);
				 * buildingsQuery2.whereEqualTo(ParseConstants.building_name, buildingName);
				 * buildingsQuery2.findInBackground(new FindCallback<UMDBuildings>() {
				 * 
				 * @Override public void done(List<UMDBuildings> arg0, ParseException arg1) {
				 * UMDBuildings building = arg0.get(0); updateMarker(building, false);
				 * Toast.makeText(getApplicationContext(), "Remove marker from map",
				 * Toast.LENGTH_SHORT).show(); } });
				 */
			} else {

			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		restorePreferences();
	}

	@Override
	protected void onPause() {
		super.onPause();
		savePreferences();
	}

	@Override
	protected void onStop() {
		super.onStop();
		savePreferences();
	}
}
