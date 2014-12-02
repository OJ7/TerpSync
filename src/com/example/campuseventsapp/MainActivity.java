package com.example.campuseventsapp;

import java.util.ArrayList;
import java.util.List;

import com.example.campuseventsapp.FloatingActionButton;
import com.example.campuseventsapp.R;
import com.google.android.gms.analytics.n;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private GoogleMap mMap;
	private FloatingActionButton fabButton, mapFAB, listFAB, signInFAB, adminFAB, locationButton;
	private static final String TAG = "MainActivity";
	String buildingNameQuery;
	private int expandFAB = 0; // 0 = collapsed, 1 = expanded
	private int locToggle = 0; // 0 = will center on current location, 1 = will center on map
	private int mapTypeToggle = 1, adminToggle = 0;
	private final LatLng UMD = new LatLng(38.989822, -76.940637);
	private List<Marker> markers = new ArrayList<Marker>();
	AlertDialog.Builder builder, list_builder;
	EditText usernameView;
	EditText passwordView;
	View view = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		builder = new AlertDialog.Builder(this); // get the context
		list_builder = new AlertDialog.Builder(this);
		view = getLayoutInflater().inflate(R.layout.dialog_signin, null);

		setupMap();
		setupFAB();
		queryAndAddEventsFromParse();

	} // end of onCreate

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
		eventsQuery.whereExists("BuildingName");
		eventsQuery.setLimit(1000);
		eventsQuery.findInBackground(new FindCallback<EventObject>() {

			@Override
			public void done(List<EventObject> arg0, ParseException arg1) {
				for (EventObject x : arg0) {

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
		});
	}

	/**
	 * Sets up the Map to center location on UMD campus and add markers to all buildings
	 */
	private void setupMap() {
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

		centerMapOnCampus();
		mMap.getUiSettings().setZoomControlsEnabled(false);

		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				// TODO (major) - open up list view with events from building specified in
				// marker
				String buildingName = marker.getTitle();

			}
		});
	}

	/**
	 * Sets up the Floating Action Button the Map Screen
	 */
	private void setupFAB() {
		fabButton = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_star))
				.withButtonColor(Color.RED).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 16).create();
		showLocationButton();
		fabButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO (minor) - implement material design animations
				if (expandFAB == 0) {
					expandFAB = 1;
					showFABMenu();
					fabButton.setFloatingActionButtonDrawable(getResources().getDrawable(
							R.drawable.ic_action_cancel));
				} else {
					expandFAB = 0;
					hideFABMenu();
					fabButton.setFloatingActionButtonDrawable(getResources().getDrawable(
							R.drawable.ic_action_star));
				}

			}
		});
	}

	private void hideFABMenu() {
		showLocationButton();
		mapFAB.hideFloatingActionButton();
		listFAB.hideFloatingActionButton();
		if (adminToggle == 0) {
			signInFAB.hideFloatingActionButton();
		} else {
			adminFAB.hideFloatingActionButton();
		}

	}

	private void showFABMenu() {
		locationButton.hideFloatingActionButton();
		showMapFAB();
		showListFAB();
		if (adminToggle == 0) {
			showSignInFAB();
		} else {
			showAdminFAB();
		}

	}

	private void showLocationButton() {
		locationButton = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_locate))
				.withButtonColor(Color.CYAN).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 86).create();

		locationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (locToggle == 0) {
					locToggle = 1;
					locationButton.setFloatingActionButtonColor(Color.MAGENTA);
					centerMapOnMyLocation();
					Toast.makeText(getApplicationContext(),
							"Attempting to center map on current location", Toast.LENGTH_SHORT)
							.show();
				} else {
					locToggle = 0;
					locationButton.setFloatingActionButtonColor(Color.CYAN);
					centerMapOnCampus();
					Toast.makeText(getApplicationContext(), "Centering map on campus",
							Toast.LENGTH_SHORT).show();
				}
			}

		});
	} // end of showLocationButton

	/*
	 * This FAB changes map types for the variable mapTypeToggle
	 * 
	 * 0 = Normal, 1 = Hybrid, 2 = Satellite, 3 = Terrain
	 */
	private void showMapFAB() {
		mapFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_map))
				.withButtonColor(Color.BLUE).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 86).create();

		mapFAB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				switch (mapTypeToggle) {
				case 0:
					// Show normal map
					mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
					mapTypeToggle++;
					Toast.makeText(getApplicationContext(), "Normal Map", Toast.LENGTH_SHORT)
							.show();
					break;
				case 1:
					// Show Hybrid map
					mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
					mapTypeToggle++;
					Toast.makeText(getApplicationContext(), "Hybrid Map", Toast.LENGTH_SHORT)
							.show();
					break;
				case 2:
					// Show Satellite map
					mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
					mapTypeToggle++;
					Toast.makeText(getApplicationContext(), "Satellite Map", Toast.LENGTH_SHORT)
							.show();
					break;
				case 3:
					// Show Terrain map
					mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
					mapTypeToggle = 0;
					Toast.makeText(getApplicationContext(), "Terrain Map", Toast.LENGTH_SHORT)
							.show();
				default:
					break;
				}
			}

		});
	} // end of MapFAB

	/*
	 * List View shortcut FAB
	 */
	private void showListFAB() {
		listFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_view_as_list))
				.withButtonColor(Color.GREEN).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 156).create();
		listFAB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Implement List of ALL current events",
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(MainActivity.this, ListActivity.class);
				startActivity(intent);

			}

		});
	} // end of ListFAB

	/*
	 * Admin -> pop dialogs for password and username
	 * 
	 * If new -> put in our default to open a screen for entering club info
	 * 
	 * If already registered -> sign in
	 */
	private void showSignInFAB() {
		signInFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_gear_50))
				.withButtonColor(Color.GRAY).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 226).create();
		signInFAB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				usernameView = (EditText) view.findViewById(R.id.username);
				passwordView = (EditText) view.findViewById(R.id.password);

				builder.setView(view)

						.setTitle(
								"Please enter your Username and Password. Use default if first time user.")
						.setCancelable(false)

						.setPositiveButton("Sign in", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

								final String UN = usernameView.getEditableText().toString();
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
													&& x.getPassword().equals(PW)
													&& x.getObjectId().equals("6q5lDmyI1R")) {

												// default
												startActivity(new Intent(MainActivity.this,
														SetupLoginInfo.class));
												flag = true;
												break;

											} else if (x.getUsername().equals(UN)
													&& x.getPassword().equals(PW)) {
												signInFAB.hideFloatingActionButton();
												adminToggle = 1;
												// adds the new settings floating button to the
												// screen where the original button was
												showAdminFAB();
												flag = true;
												break;
											} else {

											}
										}
										if (!flag) {
											Toast.makeText(getApplicationContext(),
													"Invalid Password or Username",
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

						.setNegativeButton("cancel", new DialogInterface.OnClickListener() {

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
	} // end of SignInFab

	/*
	 * Admin panel shortcut FAB
	 */
	private void showAdminFAB() {
		adminFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_user))
				.withButtonColor(Color.LTGRAY).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 226).create();
		adminFAB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String[] arr = { "Add Event", "Delete An Event", "See All Current Events",
						"Update A Current Event", "Log Out" };

				list_builder.setTitle("Please select an Option")
						.setItems(arr, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub

								// which is the index of which item in list is clicked
								switch (which) {
								case 0:
									startActivityForResult(new Intent(MainActivity.this,
											AddEventActivity.class), 0);
									break;
								case 1:
									Toast.makeText(getApplicationContext(), "clicked Delete",
											Toast.LENGTH_SHORT).show();
									break;
								case 2:
									Toast.makeText(getApplicationContext(), "clicked See All",
											Toast.LENGTH_SHORT).show();
									break;
								case 3:
									Toast.makeText(getApplicationContext(), "clicked Update",
											Toast.LENGTH_SHORT).show();
									break;
								case 4:
									Toast.makeText(getApplicationContext(), "clicked Log out",
											Toast.LENGTH_SHORT).show();
									break;
								default:
									break;
								}
							}
						}).create().show();
			}
		});
	} // end of AdminFAB

	/**
	 * Centers map on current location. If current location can not be resolved, it defaults to UMD
	 * location.
	 */
	private void centerMapOnMyLocation() {

		mMap.setMyLocationEnabled(true);

		Location location = mMap.getMyLocation();
		LatLng myLocation = UMD;

		if (location != null) {
			myLocation = new LatLng(location.getLatitude(), location.getLongitude());
		}
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
	}

	private void centerMapOnCampus() {
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UMD, 14));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
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

		}
	} // end of onActivityResult

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
