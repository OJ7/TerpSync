package com.terpsync.card;

import java.util.List;

import com.terpsync.FloatingActionButton;
import com.terpsync.R;
import com.terpsync.parse.EventObject;
import com.terpsync.parse.ParseConstants;
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

	private FloatingActionButton returnFAB, filterFAB, buildingFAB, orgFAB, priceFAB;
	CardListAdapter mAdapter;
	ListView lv; // List for all the event cards
	AlertDialog.Builder action_builder, delete_builder;
	View view = null;
	boolean isDeleted = false, filterMenuOpen = false, orgFiltered = false,
			buildingFiltered = false, priceFiltered = false;
	String deletedBuildingName = "";
	String filterType, filterName;
	String[] actionOptions = { "Edit Event", "Delete Event" };

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_list);
		action_builder = new AlertDialog.Builder(this);
		delete_builder = new AlertDialog.Builder(this);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00A0B0")));
		lv = (ListView) findViewById(R.id.event_list);

		// Determine filter options
		Intent intent = getIntent();
		filterType = intent.getStringExtra("FilterType");
		filterName = intent.getStringExtra(filterType);

		setupFAB();
		determineFilterAndCreateList();
	}

	/**
	 * Decides how to initially filter the list and creates it.
	 * 
	 * List can be filtered by: Organization, Building, or None.
	 */
	private void determineFilterAndCreateList() {
		if (filterType.equals("All")) { // Un-filtered, all events
			getEventsAndCreateList(filterType, "");
		} else if (filterType.equals(ParseConstants.event_org_name)) { // Filter by organization
																		// name
			getEventsAndCreateList(filterType, filterName);
			setActionDialog();
		} else if (filterType.equals(ParseConstants.event_location)) { // Filter by building name
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
	 * Creates a dialog box to allow the following actions on an event: edit and delete
	 */
	private void setActionDialog() {
		// TODO - change this so it only adds dialog to current user's events
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adaptView, View v, int position, long id) {
				final int pos = position;
				final AdapterView<?> pView = adaptView;
				final EventObject x = (EventObject) pView.getItemAtPosition(pos);

				// Create alert dialog
				action_builder.setTitle("Please select an option")
						.setItems(actionOptions, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int item) {
								switch (item) {
								case 0: // Edit Event
									Toast.makeText(getBaseContext(), "Implement Editing Event",
											Toast.LENGTH_LONG).show();
									break;

								case 1: // Delete Event
									deletedBuildingName = x.getBuildingName();
									delete_builder
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
													}).create().show();
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
	 * Sets up the Floating Action Buttons on the List Screen.
	 */
	private void setupFAB() {
		returnFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_undo))
				.withButtonColor(Color.RED).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 16).create();
		returnFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isDeleted) {
					setResult(Activity.RESULT_OK,
							new Intent().putExtra("deleteBuildingName", deletedBuildingName));
				} else {
					setResult(Activity.RESULT_OK);
				}
				finish();
			}
		});

		filterFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_filter))
				.withButtonColor(Color.BLUE).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 86).create();

		filterFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO - When clicked, popup shows to filter by building name, org name, etc
				if (filterMenuOpen) {
					closeFilterMenu();
				} else {
					openFilterMenu();
				}
				filterMenuOpen = !filterMenuOpen;
			}
		});

	}

	/**
	 * TODO - add doc
	 */
	private void openFilterMenu() {
		buildingFABListener();
		orgFABListener();
		priceFABListner();
	}

	/**
	 * TODO - add doc
	 */
	private void closeFilterMenu() {
		buildingFAB.hideFloatingActionButton();
		orgFAB.hideFloatingActionButton();
		priceFAB.hideFloatingActionButton();
	}

	/**
	 * TODO - add doc
	 */
	private void buildingFABListener() {
		buildingFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_building))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 226, 86).create();
		setBuildingFABState();
		buildingFAB.hideFloatingActionButton();
		buildingFAB.showFloatingActionButton();

		buildingFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO - filter by organization
				if (buildingFiltered) {
					// TODO - unfilter

				} else {
					// TODO - implement filter
					
				}
				buildingFiltered = !buildingFiltered;
				setBuildingFABState();
			}
		});
	}

	/**
	 * TODO - add doc
	 */
	private void orgFABListener() {
		orgFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_crowd))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 156, 86).create();
		setOrgFABState();
		orgFAB.hideFloatingActionButton();
		orgFAB.showFloatingActionButton();

		orgFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO - filter by org
				if (orgFiltered) {
					// TODO - unfilter

				} else {
					// TODO - implement filter
					
				}
				orgFiltered = !orgFiltered;
				setOrgFABState();
			}
		});

	}

	/**
	 * TODO - add doc
	 */
	private void priceFABListner() {
		priceFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_paid))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 86, 86).create();
		setPriceFABState();
		priceFAB.hideFloatingActionButton();
		priceFAB.showFloatingActionButton();

		priceFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO - filter by price
				if (priceFiltered) {
					// TODO - unfilter

				} else {
					// TODO - implement filter

				}
				priceFiltered = !priceFiltered;
				setPriceFABState();
			}
		});
	}

	/**
	 * Sets the color of buildingFAB to indicate state. 
	 * 
	 * Colored = Filtered, Gray = Unfiltered
	 */

	private void setBuildingFABState() {
		if (buildingFiltered) {
			buildingFAB.setFloatingActionButtonColor(Color.CYAN);
		} else {
			buildingFAB.setFloatingActionButtonColor(Color.GRAY);
		}
	}

	/**
	 * Sets the color of orgFAB to indicate state. 
	 * 
	 * Colored = Filtered, Gray = Unfiltered
	 */

	private void setOrgFABState() {
		if (orgFiltered) {
			orgFAB.setFloatingActionButtonColor(Color.YELLOW);
		} else {
			orgFAB.setFloatingActionButtonColor(Color.GRAY);
		}
	}

	/**
	 * Sets the color of priceFAB to indicate state. 
	 * 
	 * Colored = Filtered, Gray = Unfiltered
	 */
	private void setPriceFABState() {
		// TODO - change so this has three states: unfiltered, free, paid
		if (priceFiltered) {
			priceFAB.setFloatingActionButtonColor(Color.GREEN);
			priceFAB.setFloatingActionButtonDrawable(getResources().getDrawable(
					R.drawable.ic_action_free));
		} else {
			priceFAB.setFloatingActionButtonColor(Color.GRAY);
			priceFAB.setFloatingActionButtonDrawable(getResources().getDrawable(
					R.drawable.ic_action_paid));
		}
	}

}
