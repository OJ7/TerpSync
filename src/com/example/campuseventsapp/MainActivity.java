package com.example.campuseventsapp;

import com.example.campuseventsapp.FloatingActionButton;
import com.example.campuseventsapp.R;
import com.google.android.gms.internal.mm;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		GoogleMapOptions options = new GoogleMapOptions();
		options.camera(CameraPosition.fromLatLngZoom(new LatLng(38.986918,
				-76.942554), 16));

		// Centering Map on UMD
		LatLngBounds UMD = new LatLngBounds(new LatLng(38.981257, -76.95687),
				new LatLng(39.000962, -76.932355));

		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(38.989822,
				-76.940637), 14));

		mMap.getUiSettings().setZoomControlsEnabled(false);

		fabButton = new FloatingActionButton.Builder(this)
				.withDrawable(
						getResources().getDrawable(R.drawable.ic_action_star))
				.withButtonColor(Color.RED)
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 16).create();
		
		
		fabButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
				startActivity(intent);

			}
		});

		

	}


}
