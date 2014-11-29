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
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	private GoogleMap mMap;
	private FloatingActionButton fabButton, item1, item2, item3, item4,locationButton;
	private static final String TAG = "MainActivity";
	String buildingNameQuery;
	private int toggle = 0; // 0 = hidden, 1 = shown
	private int locToggle = 0; // 0 = will center on current location, 1 = will center on map
	private int mapTypeToggle = 0;
	private final LatLng UMD = new LatLng(38.989822, -76.940637);
	private List<Marker> markers = new ArrayList<Marker>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

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
				// String buildingName = marker.getTitle();

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
				if (toggle == 0) {
					toggle = 1;
					showFABMenu();
					fabButton.setFloatingActionButtonDrawable(getResources().getDrawable(R.drawable.ic_action_cancel));
				} else {
					toggle = 0;
					hideFABMenu();
					fabButton.setFloatingActionButtonDrawable(getResources().getDrawable(R.drawable.ic_action_star));
				}

			}
		});
	}

	private void hideFABMenu() {
		showLocationButton();
		item1.hideFloatingActionButton();
		item2.hideFloatingActionButton();
		item3.hideFloatingActionButton();
		item4.hideFloatingActionButton();
	}

	private void showFABMenu() {
		locationButton.hideFloatingActionButton();
		showItem1();
		showItem2();
		showItem3();
		showItem4();
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
	}
	/*
	 * This FAB changes map types:
	 * variable: mapTypeToggle
	 * 0 = Normal
	 * 1 = Hybrid
	 * 2 = Satellite
	 * 3 = Terrain
	 * 
	 */
	private void showItem1() {
		item1 = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_map))
				.withButtonColor(Color.BLUE).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 86).create();
		mapTypeToggle++; // Increase one so that when user clicks it first time, it changes map type
		item1.setOnClickListener(new OnClickListener() {
	
			@Override
			public void onClick(View v) {

				
				if(mapTypeToggle == 0){
					//Show normal map
					mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
					mapTypeToggle++;
					Toast.makeText(getApplicationContext(), "Normal Map", Toast.LENGTH_SHORT)
					.show();
					
				}else if(mapTypeToggle == 1){
					//Show Hybrid map
					mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
					mapTypeToggle++;
					Toast.makeText(getApplicationContext(), "Hybrid Map", Toast.LENGTH_SHORT)
					.show();
					
				}else if (mapTypeToggle == 2){
					//Show Satellite map
					mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
					mapTypeToggle++;
					Toast.makeText(getApplicationContext(), "Satellite Map", Toast.LENGTH_SHORT)
					.show();
					
				}else if (mapTypeToggle == 3){
					//Show Terrain map
					mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
					mapTypeToggle = 0;
					Toast.makeText(getApplicationContext(), "Terrain Map", Toast.LENGTH_SHORT)
					.show();
				}
				

			}

		});
	}

	/*
	 * List View short cut FAB
	 */
	private void showItem2() {
		item2 = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_launcher))
				.withButtonColor(Color.GREEN).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 156).create();
		item2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "clicked item 2", Toast.LENGTH_SHORT)
						.show();
				Intent intent = new Intent(MainActivity.this, ListActivity.class);
				startActivity(intent);

			}

		});
	}

	/*
	 * Setting short cut FAB
	 */
	private void showItem3() {
		item3 = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_launcher))
				.withButtonColor(Color.GRAY).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 226).create();
		item3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "clicked item 3", Toast.LENGTH_SHORT)
						.show();
			}

		});
	}
	
	/*
	 * Admin panel short cut FAB
	 */
	private void showItem4(){
		item4 = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_launcher))
		.withButtonColor(Color.WHITE).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
		.withMargins(0, 0, 16, 296).create();
		item4.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "clicked item 3", Toast.LENGTH_SHORT)
						.show();
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

			// Retrieve the object by id
			// runs on main thread I can change this
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
	}

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
		} else if (numEvent < 5) {
			markerColor = BitmapDescriptorFactory.HUE_ORANGE;
		} else {
			markerColor = BitmapDescriptorFactory.HUE_RED;
		}

		marker.setSnippet("Events: " + numEvent);
		marker.setIcon(BitmapDescriptorFactory.defaultMarker(markerColor));

	}

}
