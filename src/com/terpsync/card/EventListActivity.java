package com.terpsync.card;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.terpsync.FloatingActionButton;
import com.terpsync.R;
import com.terpsync.events.AddEventActivity;
import com.terpsync.events.EditEventActivity;
import com.terpsync.parse.EventObject;
import com.terpsync.parse.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
	public static final String PREFS_NAME = "MyPrefsFile";

	// Global variable strings used for preferences
	private final String signedInPref = "isSignedIn", currentOrgPref = "currentOrganization";

	private FloatingActionButton createFAB, filterFAB, buildingFAB, orgFAB, priceFAB;
	private Intent mResultIntent = new Intent();
	CardListAdapter mAdapter;
	ListView lv; // List for all the event cards
	List<EventObject> fullEventList;
	int numEvents = 0;
	AlertDialog.Builder action_builder, delete_builder;
	View view = null;
	boolean isDeleted = false, isSignedIn = false, filterMenuOpen = false, orgFiltered = false,
			buildingFiltered = false;
	int priceFiltered = 0; // 0 = All, 1 = Free, 2 = Paid
	String deletedBuildings = "", addedBuildings = "";
	String filterType, filterName;
	String buildingFilterName, orgFilterName;
	String[] actionOptions = { "Edit Event", "Delete Event" };
	String currentOrganization = "";
	private ActionBar actionBar;
	protected ProgressDialog proDialog;
	private final int FAB_SPACING = 70; // Spacing between FABs
	int spacingAmount = 0; // used to add padding to FAB if (+) is visible

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_event_list);
		action_builder = new AlertDialog.Builder(this);
		delete_builder = new AlertDialog.Builder(this);
		actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00A0B0")));
		lv = (ListView) findViewById(R.id.event_list);
		restorePreferences();
		if (isSignedIn) {
			spacingAmount = FAB_SPACING;
		}

		// Getting information from intent
		Intent intent = getIntent();
		filterType = intent.getStringExtra("FilterType");
		filterName = intent.getStringExtra(filterType);

		setupFAB();
		determineFilterAndCreateList();
	}

	/**
	 * Restores information if the user was previously signed in.
	 */
	private void restorePreferences() {
		Log.i(TAG, "Restoring preferences");
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		isSignedIn = settings.getBoolean(signedInPref, false);
		currentOrganization = settings.getString(currentOrgPref, "");
	}

	/**
	 * Decides how to initially filter the list and creates it.
	 * 
	 * List can be filtered by: Organization, Building, or None.
	 */
	private void determineFilterAndCreateList() {
		if (filterType.equals("All")) { // Un-filtered, all events
			actionBar.setTitle("All Events");
			getEventsAndCreateList(filterType, "");
		} else if (filterType.equals(ParseConstants.event_org_name)) { // Filter by org name
			actionBar.setTitle("Events by " + filterName);
			getEventsAndCreateList(filterType, filterName);
			orgFiltered = true;
		} else if (filterType.equals(ParseConstants.event_location)) { // Filter by building name
			actionBar.setTitle("Events in " + filterName);
			getEventsAndCreateList(filterType, filterName);
			buildingFiltered = true;
		}
		if (isSignedIn) {
			setActionDialog();
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
	private void getEventsAndCreateList(final String filterType, String filterName) {
		// Create the Parse Query object
		ParseQuery<EventObject> eventsQuery = ParseQuery.getQuery(EventObject.class);
		// Sort events by Start Date
		eventsQuery.orderByAscending("StartDate");
		// Checks if events need to be filtered
		if (!filterType.equals("All")) {
			eventsQuery.whereContains(filterType, filterName);
		}
		// Initiate a background thread, retrieve all Event Objects
		startLoading();
		eventsQuery.findInBackground(new FindCallback<EventObject>() {
			@Override
			public void done(List<EventObject> events, ParseException e) {
				if (e == null) { // All events were successfully returned
					fullEventList = events;
					numEvents = fullEventList.size();
					Collections.sort(fullEventList, new DateTimeComparator());
					mAdapter = new CardListAdapter(getApplicationContext(), R.layout.card, events);
					lv.setAdapter(mAdapter);
				} else { // object retrieval failed throw exception -- fail fast
					e.printStackTrace();
				}
				// updateActionBarColor();
				stopLoading();
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
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adaptView, View v, int position, long id) {
				final int pos = position;
				final EventObject x = (EventObject) adaptView.getItemAtPosition(pos);

				// Don't add dialog to events by other organizations
				if (!(currentOrganization.equals(x.getOrgName()))) {
					Log.i(TAG, "Clicked on another organization's event: " + x.getEventName());
					return;
				}
				Log.i(TAG, "Clicked on your event: " + x.getEventName());
				// Create alert dialog
				action_builder.setTitle("Please select an option")
						.setItems(actionOptions, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int item) {
								switch (item) {
									case 0: // Edit Event
										Log.i(TAG, "Clicked on Edit Event");
										Intent intent = new Intent(EventListActivity.this,
												EditEventActivity.class);
										intent.putExtra(ParseConstants.admin_org_name,
												x.getOrgName());
										intent.putExtra("isNewEvent", false);
										intent.putExtra(ParseConstants.event_object_id,
												x.getObjectId());
										startActivityForResult(intent, 0);
										break;

									case 1: // Delete Event
										Log.i(TAG, "Clicked on Delete Event, Confirm?");
										delete_builder
												.setTitle(
														"Delete Event? (Warning: this cannot be undone!)")
												.setPositiveButton("Delete",
														new DialogInterface.OnClickListener() {

															@Override
															public void onClick(
																	DialogInterface dialog,
																	int which) {
																Log.i(TAG, "Deleting event...");
																if (deletedBuildings == "") {
																	deletedBuildings = x
																			.getBuildingName();
																} else {
																	deletedBuildings += ";"
																			+ x.getBuildingName();
																}

																isDeleted = true;
																mAdapter.mEventsList.remove(pos);
																mAdapter.notifyDataSetChanged();
																x.deleteInBackground();
																updateIntent();
																Toast.makeText(getBaseContext(),
																		"Event Deleted",
																		Toast.LENGTH_LONG).show();
															}
														})
												.setNegativeButton("Cancel",
														new DialogInterface.OnClickListener() {
															@Override
															public void onClick(
																	DialogInterface dialog,
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
		if (isSignedIn) {
			createFABListener();
		}
		filterFABListener();
	}

	/**
	 * Updates the ActionBar color depending on the number of events.
	 * 
	 * 0 = Blue, 1-2 = Yellow, 3-5 = Orange, 6+ = Red
	 */
	private void updateActionBarColor() {
		switch (numEvents) {
			case 0: // Blue Gray
				actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#607D8B")));
				break;
			case 1:
			case 2: // Yellow
				actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFC107")));
				break;
			case 3:
			case 4:
			case 5: // Orange
				actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF9800")));
				break;
			default: // Red
				actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F44336")));
		}

	}

	/**
	 * Creates createFAB and handles clicks on it: starts AddEventActivity to create a new event
	 */
	private void createFABListener() {
		createFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_add))
				.withButtonColor(Color.RED).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 16).create();
		createFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EventListActivity.this, AddEventActivity.class);
				intent.putExtra(ParseConstants.admin_org_name, currentOrganization);
				intent.putExtra("isNewEvent", true);
				startActivityForResult(intent, 0);
			}
		});
	}

	/**
	 * Creates filterFAB and handles clicks on it: expands/collapses filter type FABs.
	 */
	private void filterFABListener() {
		filterFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_filter))
				.withButtonColor(Color.BLUE).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 16 + spacingAmount).create();
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
	}

	/**
	 * Creates buildingFAB and handles click on it: either showing a popup to choose building to
	 * filter by or un-filtering the list (if filtered)
	 */
	private void buildingFABListener() {
		buildingFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_building))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 226 + spacingAmount).create();
		setBuildingFABState();
		buildingFAB.hideFloatingActionButton();
		buildingFAB.showFloatingActionButton();
		if (!filterType.equals(ParseConstants.event_location)) { // Don't allow clicks when
																	// pre-filtered
			buildingFAB.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (buildingFiltered) {
						buildingFiltered = false;
						buildingFilterName = "none";
						refilterList();
					} else {
						filterByBuilding();
						buildingFiltered = true;
					}
					setBuildingFABState();
				}
			});
		}
	}

	/**
	 * Creates orgFAB and handles click on it: either showing a popup to choose organization to
	 * filter by or un-filtering the list (if filtered)
	 */
	private void orgFABListener() {
		orgFAB = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_crowd))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 156 + spacingAmount).create();
		setOrgFABState();
		orgFAB.hideFloatingActionButton();
		orgFAB.showFloatingActionButton();

		if (!filterType.equals(ParseConstants.event_org_name)) { // Don't allow clicks when
																	// pre-filtered
			orgFAB.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (orgFiltered) {
						orgFiltered = false;
						orgFilterName = "none";
						refilterList();
					} else {
						filterByOrganization();
						orgFiltered = true;
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
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 86 + spacingAmount).create();
		setPriceFABState();
		priceFAB.hideFloatingActionButton();
		priceFAB.showFloatingActionButton();

		priceFAB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (priceFiltered) {
					case 0: // Filtering by free
						priceFiltered++;
						filterByPrice();
						break;
					case 1: // Filtering by paid
						priceFiltered++;
						refilterList();
						break;
					case 2: // Unfiltering
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
	 * Gets a list of all buildings in current events, allows user to choose building to filter by.
	 */
	private void filterByBuilding() {
		// Get all buildings in list
		final ArrayList<String> buildingList = mAdapter.getValuesFromFields("building");
		CharSequence[] list = buildingList.toArray(new CharSequence[buildingList.size()]);
		Log.i(TAG, "Number of Buildings found: " + buildingList.size());

		if (buildingList.size() > 0) { // Only filter if events exist
			// Create dialog box to choose building to filter
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Filter by building");
			builder.setItems(list, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					buildingFilterName = "0." + buildingList.get(id);
					Log.i(TAG, "Filtering by *Building: " + buildingList.get(id) + "*");
					mAdapter.getFilter().filter(buildingFilterName);
				}
			});
			builder.setCancelable(false);
			AlertDialog alert = builder.create();
			if (!buildingFiltered) { // do not show dialog box if refiltering
				alert.show();
			} else { // refilter with existing filter
				mAdapter.getFilter().filter(buildingFilterName);
			}
		}
	}

	/**
	 * Gets a list of all organizations in current events, allows user to choose organization to
	 * filter by.
	 */
	private void filterByOrganization() {
		// Get all organizations in list
		final ArrayList<String> orgList = mAdapter.getValuesFromFields("organization");
		CharSequence[] list = orgList.toArray(new CharSequence[orgList.size()]);
		Log.i(TAG, "Number of Organizations found: " + orgList.size());

		if (orgList.size() > 0) { // Only filter if events exist
			// Create dialog box to choose organization to filter
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Filter by organization");
			builder.setItems(list, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					orgFilterName = "1." + orgList.get(id);
					Log.i(TAG, "Filtering by *Organization: " + orgList.get(id) + "*");
					mAdapter.getFilter().filter(orgFilterName);
				}
			});
			builder.setCancelable(false);
			AlertDialog alert = builder.create();
			if (!orgFiltered) { // do not show dialog box if refiltering
				alert.show();
			} else { // refilter with existing filter
				mAdapter.getFilter().filter(orgFilterName);
			}
		}
	}

	/**
	 * Filters by either free or paid events determined using priceFiltered.
	 */
	private void filterByPrice() {
		if (priceFiltered == 1) {
			Log.i(TAG, "Filtering by *Free Events*");
			mAdapter.getFilter().filter("2.FREE"); // Free Event
		} else if (priceFiltered == 2) {
			Log.i(TAG, "Filtering by *Paid Events*");
			mAdapter.getFilter().filter("3.FREE"); // Paid Event
		}
	}

	/**
	 * Resets the adapter's data and refilters the events based on the enabled filter types.
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
	 * Compares EventObjects by startDate and startTime.
	 * 
	 * TODO - If an event is more than one day long, it gets counted as the end of the day in order
	 * to avoid it taking up the space at the top of the list.
	 * 
	 * @author OJ
	 * 
	 */
	public class DateTimeComparator implements Comparator<EventObject> {
		@Override
		public int compare(EventObject o1, EventObject o2) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
			SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
			try {
				Date date1 = dateFormat.parse(o1.getStartDate());
				Date date2 = dateFormat.parse(o2.getStartDate());

				// If start dates are equal, compare by start time
				if (o1.getStartDate().compareTo(o2.getStartDate()) == 0) {
					Date time1 = timeFormat.parse(o1.getStartTime()), time2 = timeFormat.parse(o2
							.getStartTime());
					return time1.compareTo(time2);
				} else {
					return date1.compareTo(date2);
				}
			} catch (Exception e) {
				e.printStackTrace();
				// default return if above fails (shouldn't get here)
				return o1.getStartDate().compareTo(o2.getStartDate());
			}
		}
	}

	/**
	 * Shows loading dialog. For use when list is loading.
	 */
	protected void startLoading() {
		proDialog = new ProgressDialog(this);
		proDialog.setMessage("Loading events...");
		proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		proDialog.setCancelable(false);
		proDialog.show();
	}

	/**
	 * Stops the loading dialog.
	 */
	protected void stopLoading() {
		if (proDialog != null && proDialog.isShowing()) {
			proDialog.dismiss();
		}
		proDialog = null;
	}

	/**
	 * Updates the CardListAdapter with the updated list of events.
	 */
	private void updateAdapter() {
		Log.i(TAG, "updating adapter");
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * Resets the CardListAdapter to the un-filtered data list of events.
	 */
	private void resetAdapter() {
		Log.i(TAG, "resetting adapter");
		mAdapter.resetData();
	}

	/**
	 * Updates the result storing an intent if event(s)/building(s) were added or deleted.
	 */
	private void updateIntent() {
		Log.i(TAG, "Updating intent");

		if (!addedBuildings.equals("")) {
			mResultIntent.putExtra("addedNames", addedBuildings);
		}
		if (!deletedBuildings.equals("")) {
			mResultIntent.putExtra("deletedNames", deletedBuildings);
		}
		if (getParent() == null) {
			setResult(Activity.RESULT_OK, mResultIntent);
		} else {
			getParent().setResult(Activity.RESULT_OK, mResultIntent);
		}
	}

	public void onBackPressed() {
		if (filterMenuOpen) {
			closeFilterMenu();
			filterMenuOpen = false;
		} else {
			finish();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
		Log.i(TAG, "Getting activity result");
		if (requestCode == 0 && resultCode == Activity.RESULT_OK && resultIntent != null) {
			// NOTE: this is costly since it makes lots of query calls and can be improved
			// Recreating the list since event(s) were changed
			determineFilterAndCreateList();

			// Checking if new building(s) were added
			if (resultIntent.getStringExtra("addedNames") != null) {
				Log.i(TAG, "Got result - building added");
				if (addedBuildings == "") {
					addedBuildings = resultIntent.getStringExtra("addedNames");
				} else {
					addedBuildings += ";" + resultIntent.getStringExtra("addedNames");
				}
			}
			// Checking if building(s) were deleted
			if (resultIntent.getStringExtra("deletedNames") != null) {
				Log.i(TAG, "Got result - building deleted");
				isDeleted = true;
				if (deletedBuildings == "") {
					deletedBuildings = resultIntent.getStringExtra("deletedNames");
				} else {
					deletedBuildings += ";" + resultIntent.getStringExtra("deletedNames");
				}
			}
			Log.i(TAG, "Building(s) added: " + addedBuildings);
			Log.i(TAG, "Building(s) deleted: " + deletedBuildings);

			updateIntent();
		} else {
			Log.i(TAG, "No activity result");
		}
	}

}
