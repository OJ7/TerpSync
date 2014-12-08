package com.example.campuseventsapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.campuseventsapp.FloatingActionButton;
import com.example.campuseventsapp.R;
import com.example.campuseventsapp.card.EventListActivity;
import com.example.campuseventsapp.card.EventObject;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
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

	private static final String TAG = "Campus-App";
	public static final String PREFS_NAME = "MyPrefsFile";
	private Context context;
	String buildingNameQuery;
	String currentUser = "";
	String currentOrganization = "";

	// Global variables for FAB
	private FloatingActionButton fabButton, mapFAB, normalMapFAB, hybridMapFAB, listFAB, signInFAB,
	adminFAB, locationButton;
	private int expandFAB = 0; // 0 = collapsed, 1 = expanded
	private int locToggle = 0; // 0 = will center on current location, 1 = will center on map
	private int mapTypeToggle = 0, adminToggle = 0;

	// Global variables for Map
	private GoogleMap mMap;
	private final LatLng UMD = new LatLng(38.989822, -76.940637);
	private List<Marker> markers = new ArrayList<Marker>();
	LatLng myLocation = UMD;
	TextView key1, key2, key3;

	// Global variables for Dialog
	AlertDialog.Builder builder, list_builder;
	EditText usernameView;
	EditText passwordView;
	EditText newUNView, newPWView;
	View view = null;
	View signInChangesView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		context = this;

		builder = new AlertDialog.Builder(this); // get the context
		list_builder = new AlertDialog.Builder(this);
		view = getLayoutInflater().inflate(R.layout.dialog_signin, null);
		signInChangesView = getLayoutInflater().inflate(R.layout.dialog_changesignin, null);

		// Check if network is connected --
		if (!isNetworkAvailable()) {
			openNetworkDialog();
		} else {

			setupMap();

			queryAndAddEventsFromParse(); // fills map with current events from database

			createMainMenu(); // creates all FAB objects - better performance


			// adds the legend to the corner of the map
			View tview = getLayoutInflater().inflate(R.layout.legend_key_item, null);
			getWindow().addContentView(tview,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT));
			key1 = (TextView) findViewById(R.id.tv1);
			key2 = (TextView) findViewById(R.id.tv2);
			key3 = (TextView) findViewById(R.id.tv3);

			key1.setTextColor(Color.BLACK);
			key2.setTextColor(Color.BLACK);
			key3.setTextColor(Color.BLACK);

			// Restore preferences
			restorePreferences();

		}
	}

	private void restorePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		adminToggle = settings.getInt("adminToggle", adminToggle);
		currentUser = settings.getString("currentUser", currentUser);
		currentOrganization = settings.getString("currentOrganization", currentOrganization);
	}

	private void savePreferences() {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("adminToggle", adminToggle);
		editor.putString("currentUser", currentUser);
		editor.putString("currentOrganization", currentOrganization);

		// Commit the edits!
		editor.commit();
	}

	private void createMainMenu() {


		// Main FAB
		fabButton = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_action_star))
		.withButtonColor(Color.RED).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
		.withMargins(0, 0, 16, 16).create();

		// location FAB
		locationButton = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_action_locate))
		.withButtonColor(Color.parseColor("#00A0B0"))
		.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 86).create();

		// List FAB
		listFAB = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_action_database))
		.withButtonColor(Color.parseColor("#CBE86B"))
		.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 156).create();


		// Normal Map FAB
		normalMapFAB = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_action_map))
		.withButtonColor(Color.parseColor("#00A0B0")).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
		.withMargins(0, 0, 86, 86).create();


		// Hybrid Map FAB
		hybridMapFAB = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_satellite))
		.withButtonColor(Color.parseColor("#C7F464"))
		.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 156, 86).create();


		listFAB.hideFloatingActionButton();
		normalMapFAB.hideFloatingActionButton();
		hybridMapFAB.hideFloatingActionButton();

		mainFABListener();
		locationButtonListener();

	}

	private void mainFABListener() {

		fabButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (expandFAB == 0) { // expand menu now
					expandFAB = 1;
					expandFABMenu();
					fabButton.setFloatingActionButtonDrawable(getResources().getDrawable(
							R.drawable.ic_action_cancel));
				} else {
					expandFAB = 0;
					contractFABMenu();
					fabButton.setFloatingActionButtonDrawable(getResources().getDrawable(
							R.drawable.ic_action_star));
				}
			}
		});
	}


	private void contractFABMenu() {

		listFAB.hideFloatingActionButton();

		if (adminToggle == 1) {
			adminFAB.hideFloatingActionButton();
		} else {
			signInFAB.hideFloatingActionButton();
		}

		if (mapTypeToggle == 1) {
			mapFAB.hideFloatingActionButton();
			hybridMapFAB.hideFloatingActionButton();
			normalMapFAB.hideFloatingActionButton();
			mapFAB.setFloatingActionButtonDrawable(getResources().getDrawable(R.drawable.ic_map));
			mapTypeToggle = 0;
		} else {
			mapFAB.hideFloatingActionButton();

		}

		if (locToggle == 0) {
			// location FAB
			locationButton = new FloatingActionButton.Builder(this)
			.withDrawable(getResources().getDrawable(R.drawable.ic_action_locate))
			.withButtonColor(Color.parseColor("#00A0B0"))
			.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 86).create();

		} else {

			// location FAB
			locationButton = new FloatingActionButton.Builder(this)
			.withDrawable(getResources().getDrawable(R.drawable.ic_action_locate))
			.withButtonColor(Color.parseColor("#BD1550"))
			.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 86).create();

		}
		locationButton.hideFloatingActionButton();
		locationButton.showFloatingActionButton();
		locationButtonListener();

	}

	private void locationButtonListener() {

		locationButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (locToggle == 0) {
					locToggle = 1;
					locationButton.setFloatingActionButtonColor(Color.parseColor("#BD1550"));
					centerMapOnMyLocation();
					Toast.makeText(getApplicationContext(), "Centering map on current location",
							Toast.LENGTH_SHORT).show();
				} else {
					locToggle = 0;
					locationButton.setFloatingActionButtonColor(Color.parseColor("#00A0B0"));
					centerMapOnCampus();
					Toast.makeText(getApplicationContext(), "Centering map on campus",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}


	private void expandFABMenu() {

		locationButton.hideFloatingActionButton();

		listFAB.showFloatingActionButton();
		listFABListener();

		// Map Options FAB
		mapFAB = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_map))
		.withButtonColor(Color.parseColor("#EDC951"))
		.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 86).create();
		mapFAB.hideFloatingActionButton();
		mapFAB.showFloatingActionButton();
		mapFABListener();


		if (adminToggle == 0) {

			signInFABListener();

		} else {

			adminFABListener();
		}
	}


	/**
	 * Admin panel FAB
	 * 
	 * This FAB creates a dialog with a list of all options an Admin can perform.
	 */
	private void adminFABListener() {

		//Admin FAB
		adminFAB = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_action_user))
		.withButtonColor(Color.parseColor("#53777A"))
		.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 226).create();

		adminFAB.hideFloatingActionButton();
		adminFAB.showFloatingActionButton();

		adminFAB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String[] arr = { "Add Event", "Delete An Event", "See All Current Events",
						"Change PW/UN", "Log Out" };

				list_builder.setTitle("Please select an Option")
				.setItems(arr, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						switch (which) { // which is the index of which item in list is
						// clicked
						case 0:
							startActivityForResult(new Intent(MainActivity.this,
									AddEventActivity.class).putExtra(
											context.getString(R.string.parse_admin_org_name),
											currentOrganization), 0);
							break;
						case 1: // delete

							startActivityForResult(new Intent(MainActivity.this,
									EventListActivity.class).putExtra("Delete",
											currentOrganization), 0);

							break;
						case 2: // see all for club
							Intent intent = new Intent(MainActivity.this,
									EventListActivity.class);
							intent.putExtra("SeeAll", currentOrganization);
							startActivity(intent);
							break;
						case 3: // User selected change PW/UN

							newUNView = (EditText) signInChangesView
							.findViewById(R.id.newUsername);
							newPWView = (EditText) signInChangesView
									.findViewById(R.id.newPassword);

							builder.setView(signInChangesView)
							.setTitle("Update Account Info")
							.setCancelable(false)
							.setPositiveButton("Change!",
									new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									final String newPW = newPWView
											.getEditableText().toString();
									final String newUN = newUNView
											.getEditableText().toString();

									ParseQuery<AdminAccounts> query = ParseQuery
											.getQuery(AdminAccounts.class);
									query.whereContains("username",
											currentUser);
									query.findInBackground(new FindCallback<AdminAccounts>() {

										@Override
										public void done(
												List<AdminAccounts> arg0,
												ParseException arg1) {
											arg0.get(0).setUsername(newUN);
											arg0.get(0).setPassword(newPW);
											arg0.get(0).saveInBackground();
										}
									});

									((ViewGroup) signInChangesView
											.getParent())
											.removeView(signInChangesView);
									dialog.cancel();
									dialog.dismiss();
								}
							})

							.setNegativeButton("Cancel",
									new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									((ViewGroup) signInChangesView
											.getParent())
											.removeView(signInChangesView);
									dialog.cancel();
									dialog.dismiss();
								}
							});

							final AlertDialog alertDialog = builder.create();
							alertDialog.show();
							currentUser = newUNView.getEditableText().toString();
							newUNView.setText("");
							newPWView.setText("");
							break;

						case 4: // Log out
							currentUser = "";
							currentOrganization = "";
							adminFAB.hideFloatingActionButton();
							adminToggle = 0;

							signInFABListener();
							Toast.makeText(getBaseContext(), "Logged out Successfully :]",
									Toast.LENGTH_LONG).show();

							break;
						default:
							break;
						}
					}
				}).create().show();
			}
		});
	}

	/*
	 * Dialog that requires a sign in by the Admin If password and username are valid, it replaces
	 * the sign in FAB with an admin account FAB
	 */
	private void signInFABListener() {

		// Sign in FAB
		signInFAB = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_gear_50))
		.withButtonColor(Color.parseColor("#FA6900"))
		.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 16, 226).create();

		signInFAB.hideFloatingActionButton();
		signInFAB.showFloatingActionButton();


		signInFAB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				usernameView = (EditText) view.findViewById(R.id.username);
				passwordView = (EditText) view.findViewById(R.id.password);

				builder.setView(view).setTitle("Enter your Username and Password.")
				.setCancelable(false)
				.setPositiveButton("Sign in", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						final String UN = usernameView.getEditableText().toString()
								.toLowerCase().replaceAll("\\s", "");
						final String PW = passwordView.getEditableText().toString();

						ParseQuery<AdminAccounts> query = ParseQuery
								.getQuery(AdminAccounts.class);
						query.whereExists("username");
						query.setLimit(100);
						query.findInBackground(new FindCallback<AdminAccounts>() {

							@Override
							public void done(List<AdminAccounts> arg0, ParseException arg1) {

								if (arg1 != null) {
									Toast.makeText(getApplicationContext(),
											"Invalid Password or Username",
											Toast.LENGTH_LONG).show();
								} else {
									boolean flag = false;
									for (AdminAccounts x : arg0) {
										if (x.getUsername().equals(UN)
												&& x.getPassword().equals(PW)) {
											currentUser = x.getUsername();
											currentOrganization = x.getOrganizatonName();
											signInFAB.hideFloatingActionButton();
											adminToggle = 1;

											// adds the new settings floating button to the
											// screen where the original button was
											adminFABListener();
											flag = true;
											break;
										}
									}
									if (!flag) {
										Toast.makeText(getApplicationContext(),
												"Invalid Password or Username",
												Toast.LENGTH_LONG).show();
									} else {
										Toast.makeText(getApplicationContext(), "Logged In Successfully :]",
												Toast.LENGTH_LONG).show();
									}
								}


							}
						});
						usernameView.setText("");
						passwordView.setText("");
						((ViewGroup) view.getParent()).removeView(view);
						dialog.cancel();
						dialog.dismiss();

					}
				})

				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						usernameView.setText("");
						passwordView.setText("");
						((ViewGroup) view.getParent()).removeView(view);
						dialog.cancel();
						dialog.dismiss();
					}
				});

				final AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}
		});
	}

	private void mapFABListener() {

		// Show maptype FAB menu
		mapFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (mapTypeToggle == 0) {

					mapFAB.setFloatingActionButtonDrawable(getResources().getDrawable(
							R.drawable.ic_action_cancel));
					Toast.makeText(getApplicationContext(), "Show Menu", Toast.LENGTH_SHORT).show();
					mapTypeToggle = 1;
					normalMapFAB.showFloatingActionButton();
					hybridMapFAB.showFloatingActionButton();
					mapTypeListeners(); // set up listeners

				} else {

					mapFAB.setFloatingActionButtonDrawable(getResources().getDrawable(
							R.drawable.ic_map));
					Toast.makeText(getApplicationContext(), "Hide Menu", Toast.LENGTH_SHORT).show();
					mapTypeToggle = 0;
					normalMapFAB.hideFloatingActionButton();
					hybridMapFAB.hideFloatingActionButton();

				}
			}
		});

	}



	private void mapTypeListeners() {

		normalMapFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Show normal map
				mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

				key1.setTextColor(Color.BLACK);
				key2.setTextColor(Color.BLACK);
				key3.setTextColor(Color.BLACK);
				Toast.makeText(getApplicationContext(), "Normal Map", Toast.LENGTH_SHORT).show();
			}
		});

		hybridMapFAB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				key1.setTextColor(Color.RED);
				key2.setTextColor(Color.rgb(255, 102, 0));
				key3.setTextColor(Color.YELLOW);

				Toast.makeText(getApplicationContext(), "Hybrid Map", Toast.LENGTH_SHORT).show();
			}
		});

	}


	private void listFABListener() {

		listFAB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(MainActivity.this, EventListActivity.class);
				intent.putExtra("ListType", "ListFABList");
				Toast.makeText(getApplicationContext(), "List Of All Current Events!", Toast.LENGTH_LONG).show();
				startActivity(intent);
			}
		});
	}

	/**
	 * Sets up the Map to center location on UMD campus and add markers to all buildings
	 */
	private void setupMap() {
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

		centerMapOnCampus();
		mMap.getUiSettings().setZoomControlsEnabled(false);
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		mMap.setMyLocationEnabled(true);
		mMap.getMyLocation();

		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				// TODO (major) - open up list view with events from building specified in
				// marker
				String buildingName = marker.getTitle();
				Intent intent = new Intent(MainActivity.this, EventListActivity.class);
				intent.putExtra("MarkerList", buildingName);
				startActivity(intent);
			}
		});
	}



	/**
	 * TODO (minor) - Add documentation
	 */
	private void queryAndAddEventsFromParse() {
		ParseObject.registerSubclass(UMDBuildings.class);
		ParseObject.registerSubclass(EventObject.class);
		ParseObject.registerSubclass(AdminAccounts.class);
		Parse.initialize(this, this.getString(R.string.parse_app_id),
				this.getString(R.string.parse_client_key));

		// Adding current events to map
		// Check also if date is past and remove from database and don't add
		ParseQuery<EventObject> eventsQuery = ParseQuery.getQuery(EventObject.class);
		eventsQuery.findInBackground(new FindCallback<EventObject>() {

			@Override
			public void done(List<EventObject> arg0, ParseException arg1) {
				int count = 1;
				for (EventObject x : arg0) {

					Log.i(TAG, "count is " + count);
					// Toast.makeText(getApplicationContext(), "count is " + count,
					// Toast.LENGTH_LONG).show();
					count++;
					boolean oldEvent = false;
					SimpleDateFormat format = new SimpleDateFormat("M/d/y", Locale.US);
					try {
						if (format.parse(x.getEndDate()).before(new Date())) {
							Log.i(TAG, "The event " + x.getEventName() + " has passed");
							oldEvent = true;
						}
					} catch (java.text.ParseException e) {
						e.printStackTrace();
					}

					if (oldEvent) { // dont add to map and delete from database
						Log.i(TAG, "Shouldnt be in here");
						x.deleteInBackground();

					} else {
						ParseQuery<UMDBuildings> buildingsQuery = ParseQuery
								.getQuery(UMDBuildings.class);
						buildingsQuery.whereEqualTo(getString(R.string.parse_building_name),
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
	}

	/*
	 * Centers the view of the map on center of the campus
	 */
	private void centerMapOnCampus() {
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UMD, 14));
	}

	/**
	 * Checks if network is available
	 * 
	 * @return true if available, false otherwise
	 */
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private void openNetworkDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

		alertDialogBuilder.setMessage("Network not available");
		// set positive button: Yes message
		alertDialogBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// reopens network dialog if not available
				if (!isNetworkAvailable()) {
					openNetworkDialog();
				}
			}
		});
		// set neutral button: Exit the app message
		alertDialogBuilder.setNeutralButton("Exit app", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// exit the app and go to the HOME
				MainActivity.this.finish();
			}
		});

		AlertDialog alertDialog = alertDialogBuilder.create();
		// show alert
		alertDialog.show();
	}

	/**
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
				buildingsQuery.whereEqualTo("name", buildingName);
				buildingsQuery.findInBackground(new FindCallback<UMDBuildings>() {

					@Override
					public void done(List<UMDBuildings> arg0, ParseException arg1) {
						UMDBuildings building = arg0.get(0);
						updateMarker(building, true);
						Toast.makeText(getApplicationContext(), "Added event to map", Toast.LENGTH_SHORT).show();
					}
				});
			} else if (resultIntent.getStringExtra("deleteBuildingName") != null) {

				buildingName= resultIntent.getStringExtra("deleteBuildingName"); 

				ParseQuery<UMDBuildings> buildingsQuery2 = ParseQuery.getQuery(UMDBuildings.class);
				buildingsQuery2.whereEqualTo("name", buildingName);
				buildingsQuery2.findInBackground(new FindCallback<UMDBuildings>() {

					@Override
					public void done(List<UMDBuildings> arg0, ParseException arg1) {
						UMDBuildings building = arg0.get(0);
						updateMarker(building, false);
						Toast.makeText(getApplicationContext(), "Remove marker from map",Toast.LENGTH_SHORT).show();
					}
				});
			} else {

			}
		}
	}

	/**
	 * Places a marker on the building specified. The marker pop-up shows the name of the building
	 * and the number of events happening there. If the marker already exists, this method updates
	 * the number of events. The color of the marker is related to the number of events: (1-2 =
	 * YELLOW, 3-5 = ORANGE, 6+ = RED)
	 * 
	 * @param building
	 *            The location to place/update marker at
	 * @param add
	 *            Represents whether an event was added or deleted from the building. True if added,
	 *            False if deleted.
	 */

	private void updateMarker(UMDBuildings building, boolean add) {

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
			}
		}

		// Adding marker to map (or updating event count if already exists)
		if (marker == null) { // Marker not already on map
			numEvent = 1;
			marker = mMap.addMarker(new MarkerOptions().position(latLng).title(name));
			markers.add(marker);

		} else { // Marker already on map

			String temp = marker.getSnippet().replaceAll("\\D+", "");
			if (add) {
				numEvent = Integer.parseInt(temp) + 1;
			} else {
				numEvent = Integer.parseInt(temp) - 1;
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
