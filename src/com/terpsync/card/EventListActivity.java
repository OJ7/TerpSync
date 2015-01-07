package com.terpsync.card;

import java.util.ArrayList;
import java.util.List;

import com.terpsync.FloatingActionButton;
import com.terpsync.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class EventListActivity extends Activity {

	private FloatingActionButton fabButton;
	CardListAdapter mAdapter;
	ListView lv;
	AlertDialog.Builder delete_builder;
	View view = null;
	boolean isDeleted = false;
	String deletedBuildingName = "";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// create list layout file
		setContentView(R.layout.activity_list);
		delete_builder = new AlertDialog.Builder(this);

		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00A0B0")));
		setupFAB();

		// Setting up list view
		lv = (ListView) findViewById(R.id.event_list);
		view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null);

		Intent intent = getIntent();

		// ListTypes:
		// - My Org's Events
		// - All Events (Filter list by building or org, date/time?)
		// - Building's Events

		// Add On-Click Listeners for Cards
		// - if click on card, expand cards (show detailed view w/button to edit/delete)
		// - if click on org, show profile page for org
		// - if click on building, show filtered events by building

		String filterType = intent.getStringExtra("FilterType"), filterName;

		if (filterType.equals("All")) { // Un-filtered, all events
			getEventsAndCreateList(filterType, "");
		} else if (filterType.equals("OrganizationName")) { // Filter by organization name
			filterName = intent.getStringExtra("organization");
			getEventsAndCreateList(filterType, filterName);
			setDeleteDialog(); // TODO - change this so it only adds dialog to current user's events
		} else if (filterType.equals("BuildingName")){ // Filter by building name
			filterName = intent.getStringExtra("MarkerList");
			getEventsAndCreateList(filterType, filterName);
		}

	}

	/**
	 * This method gets all the events (either filtered or un-filtered) from the Parse database and
	 * adds them to the CardListAdapater to display.
	 * 
	 * @param filterType
	 *            Determines whether to filter or not. Use "All" to get all events,
	 *            "OrganizationName" to filter by organization, or "BuildingName" to filter by
	 *            building.
	 * @param filterName
	 *            If events are being filtered, this variable specifies the value to filter by.
	 */
	private void getEventsAndCreateList(String filterType, String filterName) {
		// Create the Parse Query object
		ParseQuery<EventObject> eventsQuery = ParseQuery.getQuery(EventObject.class);

		// Checks if events need to be filtered
		if (!filterType.equals("All")) {
			eventsQuery.whereContains(filterType, filterName);
		}

		// Initiate a background thread, retrieve all Event Objects
		eventsQuery.findInBackground(new FindCallback<EventObject>() {
			@Override
			public void done(List<EventObject> events, ParseException e) {
				if (e == null) { // All events were successfully returned
					mAdapter = new CardListAdapter(getApplicationContext(), R.layout.card, events);
					lv.setAdapter(mAdapter);
				} else { // object retrieval failed throw exception -- fail fast
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * TODO - add documentation
	 */
	private void setDeleteDialog() {
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adaptView, View v, int position, long id) {

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
						}).create().show();

			}
		});
	}

	/**
	 * Sets up the Floating Action Button the List Screen If the list is not already filtered,
	 * another FAB will appear to allow filtering events.
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
					setResult(Activity.RESULT_OK,
							new Intent().putExtra("deleteBuildingName", deletedBuildingName));
				} else {
					setResult(Activity.RESULT_OK);
				}
				// end activity and return to previous actions
				finish();

			}
		});

		// Create another FAB Button for filtering
		// When clicked, popup shows to filter by building name, org name, etc
	}

}
