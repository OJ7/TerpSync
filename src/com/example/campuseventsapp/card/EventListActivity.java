package com.example.campuseventsapp.card;


import java.util.ArrayList;
import java.util.List;
import com.example.campuseventsapp.FloatingActionButton;
import com.example.campuseventsapp.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;


public class EventListActivity extends Activity{

	private FloatingActionButton fabButton;
	CardListAdapter mAdapter;
	ListView lv;
	AlertDialog.Builder delete_builder;
	View view = null;
	boolean isDeleted = false;
	String deletedBuildingName = "";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//create list layout file
		setContentView(R.layout.activity_list);
		delete_builder = new AlertDialog.Builder(this);
		
		setupFAB();

		//Setting up list view
		lv = (ListView)findViewById(R.id.event_list);
		view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null);

		Intent intent = getIntent();

		if (intent.getStringExtra("ListType") != null) {
			getAllEvents();
		} else if(intent.getStringExtra("SeeAll") != null){ 
			String orgname = intent.getStringExtra("SeeAll");
			getAllEventsForOrganization(orgname);
		} else if(intent.getStringExtra("Delete") != null) {
			String orgname = intent.getStringExtra("Delete");
			getAllEventsForDelete(orgname);
		} else {
			String buildingName = intent.getStringExtra("MarkerList");
			getAllEventsInBuilding(buildingName);
		}

	} 


	private void getAllEventsForDelete(String name) {
		// create the Parse Query object
		ParseQuery<EventObject> eventsQuery = ParseQuery.getQuery(EventObject.class);
		eventsQuery.whereContains("OrganizationName", name);
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

		//TODO = DEFINE ACTIONS FOR Deleting 
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adaptView, View v,
					int position, long id) {

				final int pos = position;
				final AdapterView<?> pView = adaptView;
				
				final EventObject x = (EventObject) pView.getItemAtPosition(pos);

				deletedBuildingName = x.getBuildingName();
				delete_builder.setView(view).setTitle("Delete Event?")
				.setPositiveButton("Delete!", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						isDeleted = true;
						
						mAdapter.list.remove(pos);
						mAdapter.notifyDataSetChanged();						
						x.deleteInBackground();
						
						((ViewGroup) view.getParent()).removeView(view);
						dialog.cancel();
						dialog.dismiss();
						
					}	
				})

				.setNegativeButton("Don't Delete!", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						((ViewGroup) view.getParent()).removeView(view);
						dialog.cancel();
						dialog.dismiss();	
					}
				})
				.create()
				.show();

			}
		});
	}


	private void getAllEventsForOrganization(String name) {
		// create the Parse Query object
		ParseQuery<EventObject> eventsQuery = ParseQuery.getQuery(EventObject.class);
		eventsQuery.whereContains("OrganizationName", name);
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
				
				if (isDeleted) {
					setResult(Activity.RESULT_OK, new Intent().putExtra("buildName", deletedBuildingName ));
				}
				// end activity and return to previous actions
				finish();

			}
		});
	}



}
