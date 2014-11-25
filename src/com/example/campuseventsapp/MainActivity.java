package com.example.campuseventsapp;

import java.util.List;

import com.example.campuseventsapp.FloatingActionButton;
import com.example.campuseventsapp.R;
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
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {

	private GoogleMap mMap;
	private FloatingActionButton fabButton;
	private static final String TAG = "Campus-App";
	String buildingNameQuery;

	// delete once done
	// ========
	private UMDBuildings stampBuilding;
	private final LatLng UMD = new LatLng(38.989822, -76.940637);
	//private Resources context = getResources();

	// ========

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "In main");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
			
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		setupParse();
		setupMap();
		setupFAB();

	} // end of onCreate


	@Override
	public void onActivityResult(int requestCode, int resultCode,
			Intent resultIntent) {
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			// resultIntent will have a key "eventID" with value a event object
			// to add
			String eventObjectID = resultIntent.getStringExtra("eventID");

			// TO-DO: query from database the gps coordinates using the
			// building name from the objectID from the intent

			ParseQuery<EventObject> query = ParseQuery
					.getQuery(EventObject.class);

			// Retrieve the object by id
			query.getInBackground(eventObjectID,
					new GetCallback<EventObject>() {
						public void done(EventObject eventObject,
								ParseException e) {
							if (e == null) {

								buildingNameQuery = new String(eventObject
										.getBuildingName());

							}
						}
					});

			Log.i(TAG, "this should show" + buildingNameQuery);

		}
	}

	private void setupParse() {
		ParseObject.registerSubclass(UMDBuildings.class);
		ParseObject.registerSubclass(EventObject.class);
		Parse.initialize(this, this.getString(R.string.parse_app_id),
				this.getString(R.string.parse_client_key));
		
		/* adding current events to map */
		/* check also if date is past and remove from database and don't add */
		ParseQuery<EventObject> query = ParseQuery.getQuery(EventObject.class);
		query.whereExists("name");
		query.setLimit(200);
		query.findInBackground(new FindCallback<EventObject>() {

			@Override
			public void done(List<EventObject> arg0, ParseException arg1) {

			}
		});
	}

	
	/**
	 * Sets up the Map to center location on UMD campus and add markers to all
	 * buildings
	 */
	private void setupMap() {
		// Centering Map on UMD
		// LatLngBounds UMD = new LatLngBounds(new LatLng(38.981257, -76.95687),
		// new LatLng(39.000962, -76.932355));
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UMD, 15));
		mMap.getUiSettings().setZoomControlsEnabled(false);

		// TODO - populate all buildings with markers (and change
		// colors/visibility) once database of buildings is made

		// Example Building and marker
		/*
		 * stampBuilding = new UMDBuildings("SSU",
		 * "Adele H. Stamp Student Union Building", 163, 38.9880489,
		 * -76.9444026); addMarkers(stampBuilding);
		 */
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				// TODO open up list view with events from building specified in
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
				.withDrawable(
						getResources().getDrawable(R.drawable.ic_action_star))
				.withButtonColor(Color.RED)
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 16).create();
		fabButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO - add more buttons when clicked
				// TODO - implement material design animations
				Intent intent = new Intent(MainActivity.this,
						AddEventActivity.class);
				startActivity(intent);
			}
		});

	}

	// Add a marker for specified building
	private void addMarkers(UMDBuildings building) {
		// for (BuildingLocationRec rec : result) {}
		mMap.addMarker(new MarkerOptions()
				.position(new LatLng(Double.parseDouble(building.getLat()), Double.parseDouble(building.getLng())))
				.title(building.getName())
				.snippet("Events: " + String.valueOf(building.getNumEvents()))
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

	}

	/**
	 * Centers map on current location. If current location can not be resolved,
	 * it defaults to UMD location.
	 */
	private void centerMapOnMyLocation() {

		mMap.setMyLocationEnabled(true);
		mMap.setMyLocationEnabled(true);

		Location location = mMap.getMyLocation();
		LatLng myLocation = UMD;

		if (location != null) {
			myLocation = new LatLng(location.getLatitude(),
					location.getLongitude());
		}
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
	}
}
