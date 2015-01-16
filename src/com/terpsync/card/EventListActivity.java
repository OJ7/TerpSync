package com.terpsync.card;

import java.util.ArrayList;
import java.util.List;

import com.terpsync.FloatingActionButton;
import com.terpsync.R;
import com.terpsync.parse.EventObject;
import com.terpsync.parse.ParseConstants;
import com.google.android.gms.drive.internal.x;
import com.google.android.gms.internal.ma;
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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class EventListActivity extends Activity {

	private static final String TAG = "EventListActivity";
	private FloatingActionButton returnFAB, filterFAB, buildingFAB, orgFAB, priceFAB;
	CardListAdapter mAdapter;
	ListView lv; // List for all the event cards
	List<EventObject> fullEventList, filteredEventList;
	AlertDialog.Builder action_builder, delete_builder;
	View view = null;
	boolean isDeleted = false, filterMenuOpen = false, orgFiltered = false,
			buildingFiltered = false;
	int priceFiltered = 0; // 0 = All, 1 = Free, 2 = Paid
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
			orgFiltered = true;
			setActionDialog();
		} else if (filterType.equals(ParseConstants.event_location)) { // Filter by building name
			getEventsAndCreateList(filterType, filterName);
			buildingFiltered = true;
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
					fullEventList = events;
					mAdapter = new CardListAdapter(getApplicationContext(), R.layout.card, events);
					lv.setAdapter(mAdapter);
				} else { // object retrieval failed throw exception -- fail fast
					e.printStackTrace();
				}
			}
		});
		// filteredEventList = new ArrayList<EventObject>(fullEventList);

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
															mAdapter.mEventsList.remove(pos);
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
	 * Sets up the following Floating Action Buttons: returnFAB and filterFAB.
	 */
	private void setupFAB() {
		returnFABListener();
		filterFABListener();
	}

	/**
	 * Creates returnFAB and handles clicks on it: _____________
	 */
	private void returnFABListener() {
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
	}

	/**
	 * Creates filterFAB and handles clicks on it: ___________
	 */
	private void filterFABListener() {
		filterFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_filter))
				.withButtonColor(Color.BLUE).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 86).create();
		filterFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
	 * Expands the filter menu showing filter type buttons: buildingFAB, orgFAB, priceFAB
	 */
	private void openFilterMenu() {
		buildingFABListener();
		orgFABListener();
		priceFABListner();
	}

	/**
	 * Collapses the filter menu by hiding the filter type buttons
	 */
	private void closeFilterMenu() {
		buildingFAB.hideFloatingActionButton();
		orgFAB.hideFloatingActionButton();
		priceFAB.hideFloatingActionButton();
		updateAdapter();
	}

	/**
	 * Creates buildingFAB and handles click on it: either showing a popup to choose building to
	 * filter by or unfiltering the list (if filtered)
	 */
	private void buildingFABListener() {
		buildingFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_building))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 226, 86).create();
		setBuildingFABState();
		buildingFAB.hideFloatingActionButton();
		buildingFAB.showFloatingActionButton();
		if (!filterType.equals(ParseConstants.event_location)) { // Don't allow clicks when
																	// pre-filtered
			buildingFAB.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (buildingFiltered) {
						// TODO - unfilter
						buildingFiltered = false;
						refilterList();
					} else {
						buildingFiltered = true;
						filterByBuilding();
					}
					setBuildingFABState();
				}
			});
		}
	}

	/**
	 * Creates orgFAB and handles click on it: either showing a popup to choose organization to
	 * filter by or unfiltering the list (if filtered)
	 */
	private void orgFABListener() {
		orgFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_crowd))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT).withMargins(0, 0, 156, 86).create();
		setOrgFABState();
		orgFAB.hideFloatingActionButton();
		orgFAB.showFloatingActionButton();

		if (!filterType.equals(ParseConstants.event_org_name)) { // Don't allow clicks when
																	// pre-filtered
			orgFAB.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (orgFiltered) {
						// TODO - unfilter
						orgFiltered = false;
						refilterList();
					} else {
						orgFiltered = true;
						filterByOrganization();
					}
					setOrgFABState();
				}
			});
		}
	}

	/**
	 * Creates priceFAB and handles click on it: toggles between free, paid, or all events
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
				switch (priceFiltered) {
				case 0: // Filtering by free
					// TODO - implement filter
					priceFiltered++;
					filterByPrice();
					break;
				case 1: // Filtering by paid
					// TODO - implement filter
					priceFiltered++;
					filterByPrice();
					break;
				case 2: // Unfiltering
					// TODO - unfilter
					priceFiltered = 0;
					refilterList();
					break;
				}
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
		switch (priceFiltered) {
		case 0: // All
			priceFAB.setFloatingActionButtonColor(Color.GRAY);
			priceFAB.setFloatingActionButtonDrawable(getResources().getDrawable(
					R.drawable.ic_action_paid));
			break;
		case 1: // Free
			priceFAB.setFloatingActionButtonColor(Color.RED);
			priceFAB.setFloatingActionButtonDrawable(getResources().getDrawable(
					R.drawable.ic_action_free));
			break;
		case 2: // Paid
			priceFAB.setFloatingActionButtonColor(Color.GREEN);
			priceFAB.setFloatingActionButtonDrawable(getResources().getDrawable(
					R.drawable.ic_action_paid));
			break;
		}
	}

	/**
	 * Gets a list of all buildings in current events, allows user to choose building and filters
	 * the filteredEventList by the building specified.
	 */
	private void filterByBuilding() {
		// Get all buildings in list
		// ArrayList<String> buildingList = getValuesFromFields("building");
		// Create dialog box to choose building to filter

		// filter filteredEventList
		mAdapter.getFilter().filter("0.Adele H. Stamp Student Union Building");
		updateAdapter();
	}

	/**
	 * Gets a list of all organizations in current events, allows user to choose organization and
	 * filters the filteredEventList by the organization specified.
	 */
	private void filterByOrganization() {
		// Get all organizations in list
		// ArrayList<String> orgList = getValuesFromFields("organization");
		// Create dialog box to choose organization to filter

		// filter filteredEventList
		mAdapter.getFilter().filter("1.Club OJ");
		updateAdapter();
	}

	private void filterByPrice() {
		// filter by either free or paid (check priceFiltered)
		if (priceFiltered == 1) {
			mAdapter.getFilter().filter("2.FREE");
		} else if (priceFiltered == 2) {
			mAdapter.getFilter().filter("3.FREE");
		}
		updateAdapter();
	}

	/**
	 * Finds all the values in the list of events for the specified field
	 * 
	 * @param field
	 *            the field from which to get all the values from (either building or organization)
	 * @return ArrayList of Strings of all the values in the specified field
	 */
	private ArrayList<String> getValuesFromFields(String field) {

		ArrayList<String> list = new ArrayList<String>();
		for (EventObject x : filteredEventList) {
			if (field.equals("building")) {
				if (!list.contains(x.getBuildingName())) {
					list.add(x.getBuildingName());
				}
			} else { // field.equals("organization") //if (!list.contains(x.getOrgName())) {
				list.add(x.getOrgName());
			}
		}
		return list;

	}

	/**
	 * Reverts the filteredEventList back to the fullEventList and attempts to re-filter the events
	 * by the enabled filter types
	 */
	private void refilterList() {
		resetAdapter();
		if (!filterType.equals(ParseConstants.event_location) && buildingFiltered) {
			filterByBuilding();
		}
		if (!filterType.equals(ParseConstants.event_org_name) && orgFiltered) {
			filterByOrganization();
		}
		if (priceFiltered > 0) {
			filterByPrice();
		}
		updateAdapter();
	}

	/**
	 * Updates the CardListAdapter with the updated list of events
	 */
	private void updateAdapter() {
		Log.i(TAG, "updating adapter");
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * Resets the CardListAdapter to the un-filtered data list of events
	 */
	private void resetAdapter() {
		mAdapter.resetData();
	}

}
