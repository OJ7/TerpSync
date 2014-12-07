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

	private GoogleMap mMap;
	private FloatingActionButton fabButton, mapFAB, normalMapFAB, hybridMapFAB, listFAB, signInFAB,
	adminFAB, locationButton;
	private static final String TAG = "Campus-App";
	String buildingNameQuery;
	private int expandFAB = 0; // 0 = collapsed, 1 = expanded
	private int locToggle = 0; // 0 = will center on current location, 1 = will center on map
	private int mapTypeToggle = 0, adminToggle = 0;
	private final LatLng UMD = new LatLng(38.989822, -76.940637);
	private List<Marker> markers = new ArrayList<Marker>();
	private Context context;
	AlertDialog.Builder builder, list_builder;
	EditText usernameView;
	EditText passwordView;
	EditText newUNView, newPWView;
	View view = null;
	View signInChangesView = null;
	LatLng myLocation = UMD;
	String currentUser = "";
	String currentOrganization = "";
	TextView key1;
	TextView key2;
	TextView key3;



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

		//Check if network is connected -- 
		if (!isNetworkAvailable()) {
			openNetworkDialog();
		} else {

			setupMap();
			createAllFAB(); //creates all FAB objects - better performance
			setupFAB();     //sets up the initial visibility
			queryAndAddEventsFromParse(); //fills map with current events from database

			locationFABListener();
			mainFABlistener();


			//adds the legend to the corner of the map
			View tview = getLayoutInflater().inflate(R.layout.legend_key_item, null);
			getWindow().addContentView(	tview,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			key1 = (TextView)findViewById(R.id.tv1);
			key2 = (TextView)findViewById(R.id.tv2);
			key3 = (TextView)findViewById(R.id.tv3);

			key1.setTextColor(Color.BLACK);
			key2.setTextColor(Color.BLACK);
			key3.setTextColor(Color.BLACK);

		}
	}




	/* 
	 * Creates ALL FAB Views
	 */
	private void createAllFAB() {

		// Main FAB
		fabButton = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_action_star))
		.withButtonColor(Color.RED).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
		.withMargins(0, 0, 16, 16).create();

		//Map Options FAB
		mapFAB = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_map))
		.withButtonColor(Color.parseColor("#EDC951")).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
		.withMargins(0, 0, 16, 86).create();


		//Normal Map FAB
		normalMapFAB = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_action_map))
		.withButtonColor(Color.parseColor("#F8CA00")).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
		.withMargins(0, 0, 86, 86).create();

		//Hybrid Map FAB
		hybridMapFAB = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_satellite))
		.withButtonColor(Color.parseColor("#C7F464")).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
		.withMargins(0, 0, 156, 86).create();

		// location FAB
		locationButton = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_action_locate))
		.withButtonColor(Color.parseColor("#BD1550")).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
		.withMargins(0, 0, 16, 86).create();

		//List FAB
		listFAB = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_action_database))
		.withButtonColor(Color.parseColor("#CBE86B")).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
		.withMargins(0, 0, 16, 156).create();


		//Admin FAB
		adminFAB = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_action_user))
		.withButtonColor(Color.parseColor("#53777A")).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
		.withMargins(0, 0, 16, 226).create();

		//Sign in FAB
		signInFAB = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_gear_50))
		.withButtonColor(Color.parseColor("#FA6900")).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
		.withMargins(0, 0, 16, 226).create();
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
					//Toast.makeText(getApplicationContext(), "count is " + count, Toast.LENGTH_LONG).show();
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
								addMarker(building);
							}
						});
					}
				}
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
	 * Sets up the Floating Action Button the Map Screen
	 */
	private void setupFAB() {

		normalMapFAB.hideFloatingActionButton();
		hybridMapFAB.hideFloatingActionButton();
		adminFAB.hideFloatingActionButton();
		signInFAB.hideFloatingActionButton();
		mapFAB.hideFloatingActionButton();
		listFAB.hideFloatingActionButton();

		fabButton.showFloatingActionButton();
		locationButton.showFloatingActionButton();

	}


	private void locationFABListener() {

		locationButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (locToggle == 0) {
					locToggle = 1;
					locationButton.setFloatingActionButtonColor(Color.parseColor("#00A0B0"));
					centerMapOnMyLocation();
					Toast.makeText(getApplicationContext(),
							"Centering map on current location", Toast.LENGTH_SHORT)
							.show();
				} else {
					locToggle = 0;
					locationButton.setFloatingActionButtonColor(Color.parseColor("#BD1550"));
					centerMapOnCampus();
					Toast.makeText(getApplicationContext(), "Centering map on campus",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void mainFABlistener() {

		fabButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (expandFAB == 0) { //expand menu now

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


		locationButton.hideFloatingActionButton();

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
			.withButtonColor(Color.parseColor("#BD1550")).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
			.withMargins(0, 0, 16, 86).create();
		} else {


			// location FAB
			locationButton = new FloatingActionButton.Builder(this)
			.withDrawable(getResources().getDrawable(R.drawable.ic_action_locate))
			.withButtonColor(Color.parseColor("#00A0B0")).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
			.withMargins(0, 0, 16, 86).create();
		}

		locationButton.showFloatingActionButton();
		locationFABListener();
	}

	private void expandFABMenu() {

		locationButton.hideFloatingActionButton();

		listFAB.showFloatingActionButton();
		listFABListener();

		mapFAB.showFloatingActionButton();
		mapFABListener();


		if (adminToggle == 0) {
			Log.i(TAG, "Inside the listener");
			signInFAB.showFloatingActionButton();
			signInFABListener();
		} else {
			adminFAB.showFloatingActionButton();
			adminFABListener();
		}
	}

	private void listFABListener() {

		listFAB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(MainActivity.this, EventListActivity.class);
				intent.putExtra("ListType","ListFABList");
				startActivity(intent);
			}
		});
	}


	private void mapFABListener() {


		// Show maptype FAB menu
		mapFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Show FAB menu of map types

				if(mapTypeToggle == 0){
					mapFAB.setFloatingActionButtonDrawable(getResources().getDrawable(R.drawable.ic_action_cancel));
					Toast.makeText(getApplicationContext(), "Show Menu", Toast.LENGTH_SHORT).show();
					mapTypeToggle = 1;
					normalMapFAB.showFloatingActionButton();
					hybridMapFAB.showFloatingActionButton();
					mapTypeListeners(); //set up listeners

				}else{

					mapFAB.setFloatingActionButtonDrawable(getResources().getDrawable(R.drawable.ic_map));
					Toast.makeText(getApplicationContext(), "Hide Menu", Toast.LENGTH_SHORT).show();
					mapTypeToggle=0;
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
				Toast.makeText(getApplicationContext(), "Normal Map", Toast.LENGTH_SHORT)
				.show();
			}
		});

		hybridMapFAB.setOnClickListener(new OnClickListener(){


			@Override
			public void onClick(View arg0) {
				mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				key1.setTextColor(Color.RED);
				key2.setTextColor(Color.rgb(255, 102, 0));
				key3.setTextColor(Color.YELLOW);

				Toast.makeText(getApplicationContext(), "Hybrid Map", Toast.LENGTH_LONG).show();
			}
		});

	}


	/*
	 * Dialog that requires a sign in by the Admin
	 * If password and username are valid, it replaces 
	 * the sign in FAB with an admin account FAB 
	 */
	private void signInFABListener() {
		Log.i(TAG, "Made it to the on click");

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
										adminFAB.showFloatingActionButton();
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
									Toast.makeText(getApplicationContext(), "Logged in",
											Toast.LENGTH_LONG).show();
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

	/**
	 * Admin panel FAB
	 * 
	 * This FAB creates a dialog with a list of all options an Admin can perform.
	 */

	private void adminFABListener() {

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
						case 1: //delete
							
							
							startActivityForResult(new Intent(MainActivity.this, EventListActivity.class).putExtra("Delete", currentOrganization), 0);
							
							break;
						case 2: //see all for club
							Intent intent = new Intent(MainActivity.this, EventListActivity.class);
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

							signInFAB.showFloatingActionButton();
							signInFABListener();
							Toast.makeText(getBaseContext(), "Logged out Successfully :]", Toast.LENGTH_LONG).show();

							break;
						default:
							break;
						}
					}
				}).create().show();
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
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {

			if (resultIntent.getStringExtra("eventID") != null) {
				// resultIntent will have a key "eventID" with value an event object to add
				String eventObjectID = resultIntent.getStringExtra("eventID");

				ParseQuery<EventObject> eventsQuery = ParseQuery.getQuery(EventObject.class);

				eventsQuery.getInBackground(eventObjectID, new GetCallback<EventObject>() {
					public void done(EventObject eventObject, ParseException e) {
						if (e == null) {

							buildingNameQuery = new String(eventObject.getBuildingName());
							ParseQuery<UMDBuildings> buildingsQuery = ParseQuery
									.getQuery(UMDBuildings.class);
							buildingsQuery.whereEqualTo(getString(R.string.parse_building_name),
									buildingNameQuery);
							buildingsQuery.findInBackground(new FindCallback<UMDBuildings>() {

								@Override
								public void done(List<UMDBuildings> arg0, ParseException arg1) {
									UMDBuildings building = arg0.get(0);
									addMarker(building);
									Toast.makeText(getApplicationContext(), "Added event to map",
											Toast.LENGTH_SHORT).show();
								}
							});
						}
					}
				});
			} else if(resultIntent.getStringExtra("buildName") != null) {
				
				String deleteBuild = resultIntent.getStringExtra("buildName"); 
					//for (Marker m: markers) {
						//if (m.getTitle().equals(deleteBuild)) {
							
					//	}
				//	}
			}else {
				
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
	 */
	private void addMarker(UMDBuildings building) {
		Double lat = Double.parseDouble(building.getLat());
		Double lon = Double.parseDouble(building.getLng());
		LatLng latLng = new LatLng(lat, lon);
		String name = String.valueOf(building.getName());
		Marker marker = null;
		int numEvent;

		for (Marker m : markers) { // Check if marker already exists
			if (m.getTitle().equals(name)) {
				marker = m;
			}
		}

		if (marker == null) { // Marker not already on map
			numEvent = 1;
			marker = mMap.addMarker(new MarkerOptions().position(latLng).title(name));
			markers.add(marker);

		} else { // Marker already on map
			String temp = marker.getSnippet();
			numEvent = Integer.parseInt(temp.substring(temp.length() - 1)) + 1;
		}

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
	}
}
