package com.example.campuseventsapp.card;


import java.util.ArrayList;
import java.util.List;

import com.example.campuseventsapp.FloatingActionButton;
import com.example.campuseventsapp.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.Toast;


public class EventListActivity extends Activity{

	private static final String TAG = "ListActivity";
	private FloatingActionButton fabButton;
	//private int toggle = 0; // 0 = hidden, 1 = shown

	//List of cards
	private ArrayList<EventObject> cardList;
	CardListAdapter mAdapter;
	ListView lv;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//create list layout file
		setContentView(R.layout.activity_list);


		setupFAB();


		//Setting up list view
		lv = (ListView)findViewById(R.id.event_list);
		cardList = new ArrayList<EventObject>();


		Intent intent = getIntent();

		if (intent.getStringExtra("ListType") != null) {
			getAllEvents();
		}else {
			String buildingName = intent.getStringExtra("MarkerList");
			getAllEventsInBuilding(buildingName);
		}



		//TODO = DEFINE ACTIONS FOR LIST VIEW 
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				Toast.makeText(getApplicationContext(), "clicked item", Toast.LENGTH_LONG).show();
			}
		});
	} 



	private void getAllEventsInBuilding(String name) {

		// create the Parse Query object
		ParseQuery<EventObject> eventsQuery = ParseQuery.getQuery(EventObject.class);
		eventsQuery.whereContains("BuildingName", name);
		// initiate a background thread, retrieve all Event Objects 
		eventsQuery.findInBackground(new FindCallback<EventObject>() {
			@Override
			public void done(List<EventObject> events, ParseException e) {

				// all events were successfully returned
				if (e == null) {

					mAdapter = new CardListAdapter(getApplicationContext(), R.layout.card,events);
					lv.setAdapter(mAdapter);	
				}
				else {
					// object retrieval failed throw exception -- fail fast
					e.printStackTrace();
				}
			}
		});	

	}



	private void getAllEvents() {

		// create the Parse Query object
		ParseQuery<EventObject> eventsQuery = ParseQuery.getQuery(EventObject.class);

		// initiate a background thread, retrieve all Event Objects 
		eventsQuery.findInBackground(new FindCallback<EventObject>() {
			@Override
			public void done(List<EventObject> events, ParseException e) {

				// all events were successfully returned
				if (e == null) {

					mAdapter = new CardListAdapter(getApplicationContext(), R.layout.card,events);
					lv.setAdapter(mAdapter);	
				}
				else {
					// object retrieval failed throw exception -- fail fast
					e.printStackTrace();
				}
			}
		});	
	}



	/**
	 * Sets up the Floating Action Button the Map Screen
	 */
	private void setupFAB() {
		fabButton = new FloatingActionButton.Builder(this)
		.withDrawable(getResources().getDrawable(R.drawable.ic_action_undo))
		.withButtonColor(Color.RED).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
		.withMargins(0, 0, 16, 16).create();
		fabButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// end activity and return to previous actions
				finish();

			}
		});
	}



}
