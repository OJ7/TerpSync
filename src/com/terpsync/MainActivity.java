package com.terpsync;

import java.util.ArrayList;
import java.util.List;
import com.terpsync.FloatingActionButton;
import com.terpsync.R;
import com.terpsync.card.EventListActivity;
import com.terpsync.parse.AdminAccounts;
import com.terpsync.parse.EventObject;
import com.terpsync.parse.ParseConstants;
import com.terpsync.parse.UMDBuildings;
import com.terpsync.settings.SettingsActivity;
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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

	// Global variable strings used for preferences
	private final String mapTypePref = "pref_key_map_type";

	// Global variables for Current User (if signed in)
	boolean isSignedIn = false;
	String currentUser = "", currentOrganization = "";

	// Global variables for FAB
	private FloatingActionButton locationFAB, listFAB, settingsFAB;
	private int locToggle = 0; // 0 = will center on current location, 1 = will center on map
	private int mapToggle = 0; // 0 = normal map, 1 = hybrid map

	// Global variables for Map
	private GoogleMap mMap;
	private final LatLng UMD = new LatLng(38.989822, -76.940637); // Center of UMD Campus
	private List<Marker> markers = new ArrayList<Marker>();
	LatLng myLocation = UMD; // Initially UMD
	TextView key1, key2, key3;

	// Global variables for Dialog
	AlertDialog.Builder signInBuilder, adminOptionsListBuilder;
	EditText usernameView, passwordView, newUNView, newPWView, newPWConfirmView;
	View signInView = null, changeSignInView = null;
	String[] adminOptions = { "Create an Event", "Manage My Events", "Change Username/Password",
			"Sign Out", "Settings" };
	boolean validChange = false;
	protected ProgressDialog proDialog;

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
		SharedPreferences settingsDefault = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		mapToggle = Integer.parseInt(settingsDefault.getString(mapTypePref, "0"));
	}

	/**
	 * Creates two Floating Action Buttons (FAB): menu and list.
	 */
	private void createInitialFAB() {
		Log.i(TAG, "Creating initial FAB");
		// menuFABListener();
		locationFABListener();
		listFABListener();
		settingsFABListener();
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
					.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 16).create();
		} else {
			locationFAB = new FloatingActionButton.Builder(this)
					.withDrawable(getResources().getDrawable(R.drawable.ic_action_locate))
					.withButtonColor(Color.parseColor("#BD1550"))
					.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 16).create();
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
	 * Creates listFAB and handles click on it: opens up list of all events
	 */
	private void listFABListener() {
		Log.i(TAG, "Creating List FAB...");
		// Setting up FAB
		listFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_database))
				.withButtonColor(Color.parseColor("#CBE86B"))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 86).create();
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
	 * Creates settingsFAB and handles click on it: opens up preference screen.
	 */
	private void settingsFABListener() {
		Log.i(TAG, "Creating Settings FAB...");
		// Setting up FAB
		settingsFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_gear_50))
				.withButtonColor(Color.parseColor("#FA6900"))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 156).create();
		settingsFAB.hideFloatingActionButton();
		settingsFAB.showFloatingActionButton();

		// Attaching onClickListener
		Log.i(TAG, "...attaching onClickListener");
		settingsFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * Sets the map type to normal or hybrid depending on the preference in settings. Also updates
	 * the key legend colors so it's easier to read on each map type.
	 */
	private void setMapType() {
		Log.i(TAG, "Setting map type...");
		if (mapToggle == 0) { // Normal
			Log.i(TAG, "to NORMAL");
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			key1.setTextColor(Color.BLACK);
			key2.setTextColor(Color.BLACK);
			key3.setTextColor(Color.BLACK);
		} else { // Hybrid
			Log.i(TAG, "to HYBRID");
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			key1.setTextColor(Color.RED);
			key2.setTextColor(Color.rgb(255, 102, 0)); // ORANGE
			key3.setTextColor(Color.YELLOW);
		}
	}

	/**
	 * Gets context for all dialogs builders, inflates all views, and gets all needed references to
	 * views.
	 */
	@SuppressLint("InflateParams")
	private void setupViewsAndCacheWidgets() {
		Log.i(TAG, "Setting up views and caching widgets");
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
		MapsInitializer.initialize(this);
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
		setMapType();
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
		startLoading();
		eventsQuery.findInBackground(addMapMarkersCallback);
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
	 * Shows loading dialog. For use when map is loading (i.e. markers adding to map).
	 */
	protected void startLoading() {
		proDialog = new ProgressDialog(this);
		proDialog.setMessage("Loading map...");
		proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		proDialog.setCancelable(false);
		proDialog.show();
	}

	/**
	 * Stops the loading dialog.
	 */
	protected void stopLoading() {
		if (proDialog != null && proDialog.isShowing()) {
			proDialog.dismiss();
		}
		proDialog = null;
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
					buildings = resultIntent.getStringExtra("deletedNames");
					Log.i(TAG, "Got result - building(s) deleted: " + buildings);
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
		setMapType();
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "Pausing Main Activity");
		super.onPause();
		// savePreferences();
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "Stopping Main Activity");
		super.onStop();
	}

	/*
	 * NOTE: Add (Parse) FindCallback functions below to keep the above code dense and organized
	 */

	/**
	 * Two nested FindCallbacks. One gets the list of all events. The other gets the building for
	 * each event and updates the marker for that building.
	 */
	FindCallback<EventObject> addMapMarkersCallback = new FindCallback<EventObject>() {
		@Override
		public void done(List<EventObject> arg0, ParseException arg1) {
			int count = 1;
			for (EventObject x : arg0) {
				Log.d(TAG, "Event #: " + count++);
				ParseQuery<UMDBuildings> buildingsQuery = ParseQuery.getQuery(UMDBuildings.class);
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
						stopLoading();
					}
				});
			}
		}
	};

}
