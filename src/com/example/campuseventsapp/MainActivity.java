package com.example.campuseventsapp;

import com.example.campuseventsapp.FloatingActionButton;
import com.example.campuseventsapp.R;
import com.google.android.gms.internal.mm;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {

	private GoogleMap mMap;
	private FloatingActionButton fabButton;
	private Building stampBuilding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		// Centering Map on UMD
		// LatLngBounds UMD = new LatLngBounds(new LatLng(38.981257, -76.95687),
		// new LatLng(39.000962, -76.932355));
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(38.989822,
				-76.940637), 15));
		mMap.getUiSettings().setZoomControlsEnabled(false);

		// TODO - populate all buildings with markers (and change
		// colors/visibility) once database of buildings is made

		// Example Building and marker
		stampBuilding = new Building("SSU",
				"Adele H. Stamp Student Union Building", 163, 38.9880489,
				-76.9444026);
		addMarkers(stampBuilding);

		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				// TODO open up list view with events from building specified in
				// marker
				String buildingName = marker.getTitle();

			}
		});

		// Floating Action Button
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
	private void addMarkers(Building building) {
		// for (BuildingLocationRec rec : result) {}

		mMap.addMarker(new MarkerOptions()
				.position(new LatLng(building.getLat(), building.getLng()))
				.title(building.getName())
				.snippet("Events: " + String.valueOf(building.getNumEvents()))
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

	}
}
