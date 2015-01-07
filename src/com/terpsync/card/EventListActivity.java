package com.terpsync.card;

import java.util.List;
import com.terpsync.FloatingActionButton;
import com.terpsync.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class EventListActivity extends Activity {

	private FloatingActionButton fabButton;
	CardListAdapter mAdapter;
	ListView lv;
	AlertDialog.Builder action_builder, delete_builder;
	View view = null;
	boolean isDeleted = false;
	String deletedBuildingName = "";
	String filterType, filterName;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// create list layout file
		setContentView(R.layout.activity_list);
		action_builder = new AlertDialog.Builder(this);
		delete_builder = new AlertDialog.Builder(this);

		// Determine filter options
		Intent intent = getIntent();
		filterType = intent.getStringExtra("FilterType");
		filterName = intent.getStringExtra(filterType);

		// Setting up UI
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00A0B0")));
		setupFAB();

		// Setting up list view
		lv = (ListView) findViewById(R.id.event_list);

		determineFilter();

	}

	/**
	 * TODO - add documentation
	 */
	private void determineFilter(){
		if (filterType.equals("All")) { // Un-filtered, all events
			getEventsAndCreateList(filterType, "");
		} else if (filterType.equals("OrganizationName")) { // Filter by organization name
			getEventsAndCreateList(filterType, filterName);
			setActionDialog();
		} else if (filterType.equals("BuildingName")) { // Filter by building name
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
		
		// TODO - Add On-Click Listeners for Cards
				// - if click on card, expand cards (show detailed view w/button to edit/delete)
				// - if click on org, show profile page for org
				// - if click on building, show filtered events by building
	}

	/**
	 * TODO - add documentation
	 */
	// TODO - change this so it only adds dialog to current user's events
	// TODO - fix bug when tapping outside dialog box and tapping on event again
	private void setActionDialog() {
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adaptView, View v, int position, long id) {

				final int pos = position;
				final AdapterView<?> pView = adaptView;

				final EventObject x = (EventObject) pView.getItemAtPosition(pos);

				// Event Action Dialog

				String[] arr = { "Edit Event", "Delete Event" };

				// create alert dialog
				AlertDialog actionDialog = action_builder.setTitle("Please select an option")
						.setItems(arr, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int item) {

								switch (item) {

								case 0: // Edit Event
									Toast.makeText(getBaseContext(), "Implement Editing Event",
											Toast.LENGTH_LONG).show();
									break;

								case 1: // Delete Event
									deletedBuildingName = x.getBuildingName();
									AlertDialog deleteDialog = delete_builder
											.setTitle(
													"Delete Event? (Warning: this cannot be undone!)")
											.setPositiveButton("Delete",
													new DialogInterface.OnClickListener() {

														@Override
														public void onClick(DialogInterface dialog,
																int which) {
															isDeleted = true;
															mAdapter.list.remove(pos);
															mAdapter.notifyDataSetChanged();
															x.deleteInBackground();
														}
													})
											.setNegativeButton("Cancel",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog,
																int which) {
															dialog.cancel();
														}
													}).create();
									deleteDialog.show();
									break;

								default:
									break;
								}
							}
						}).create();
				actionDialog.show();
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

		// TODO - Create another FAB Button for filtering (when showing all buildings)
		// TODO - When clicked, popup shows to filter by building name, org name, etc
	}

}
