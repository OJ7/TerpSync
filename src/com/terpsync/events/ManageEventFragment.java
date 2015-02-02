package com.terpsync.events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.terpsync.R;
import com.terpsync.R.id;
import com.terpsync.R.layout;
import com.terpsync.parse.EventObject;
import com.terpsync.parse.ParseConstants;
import com.terpsync.parse.UMDBuildings;
import com.parse.FindCallback;
import com.parse.ParseQuery;
import com.parse.ParseException;
import com.parse.SaveCallback;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class ManageEventFragment extends Fragment {

	private static final String TAG = "ManageEventFragement";

	TextView orgNameText, startDateText, startTimeText, endDateText, endTimeText, dollarSignText;
	AutoCompleteTextView eventLocationText;
	EditText eventNameText, eventDescriptionText, costText;
	RadioGroup admissionRadioGroup;
	RadioButton freeButton, paidButton;
	Button saveButton;
	String currentOrganization;
	ArrayList<String> buildingNames;

	private EventObject mEvent; // The event to be created (or edited)
	private Calendar myCalendar = Calendar.getInstance(), startCal = Calendar.getInstance(),
			endCal = Calendar.getInstance();
	private LinearLayout rootView;
	private FragmentActivity fragAct; // The FragmentActivity this fragment is attached too
	private Intent mIntent;
	private int mYear = myCalendar.get(Calendar.YEAR), mMonth = myCalendar.get(Calendar.MONTH),
			mDay = myCalendar.get(Calendar.DAY_OF_MONTH), mHour = myCalendar
					.get(Calendar.HOUR_OF_DAY), mMinute = myCalendar.get(Calendar.MINUTE);
	private boolean isNewEvent; // determines whether to create new event or edit existing one
	private boolean isLocationChanged; // determines if location was changed when editing event
	private String originalBuilding; // the building from the eventObject (if editing event)
	private String objectID; // the objectID of eventObject (if editing event)

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, "Entering onCreateView()");

		fragAct = super.getActivity();
		rootView = (LinearLayout) inflater
				.inflate(R.layout.fragment_manage_event, container, false);

		mIntent = fragAct.getIntent();
		cacheWidgets();
		queryAndFillAutoCompleteView();
		setViewListeners();

		currentOrganization = mIntent.getStringExtra(ParseConstants.admin_org_name);
		orgNameText.setText(currentOrganization);

		isNewEvent = mIntent.getBooleanExtra("isNewEvent", false);

		// check if Creating or Editing Event+Loading Data
		if (isNewEvent) { // Creating Event
			Log.i(TAG, "Adding event as:" + currentOrganization);
			mEvent = new EventObject();
		} else { // Editing Event
			Log.i(TAG, "Editing event as:" + currentOrganization);
			// Getting the EventObject and assigning to mEvent
			objectID = mIntent.getStringExtra(ParseConstants.event_object_id);
			try {
				ParseQuery<EventObject> eventsQuery = ParseQuery.getQuery(EventObject.class);
				mEvent = eventsQuery.get(objectID);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			loadEventData();
		}

		return rootView;
	}

	public ManageEventFragment() {
		// nothing here
	}

	/**
	 * Get references to all the views
	 */
	private void cacheWidgets() {
		Log.i(TAG, "cacheWidgets()");
		orgNameText = (TextView) rootView.findViewById(R.id.studentOrgName);
		eventNameText = (EditText) rootView.findViewById(R.id.eventTitle);
		startDateText = (TextView) rootView.findViewById(R.id.eventStartDate);
		endDateText = (TextView) rootView.findViewById(R.id.eventEndDate);
		startTimeText = (TextView) rootView.findViewById(R.id.eventStartTime);
		endTimeText = (TextView) rootView.findViewById(R.id.eventEndTime);
		eventLocationText = (AutoCompleteTextView) rootView.findViewById(R.id.eventLocation);
		eventDescriptionText = (EditText) rootView.findViewById(R.id.eventDescription);
		admissionRadioGroup = (RadioGroup) rootView.findViewById(R.id.eventAdmissionRadioGroup);
		freeButton = (RadioButton) rootView.findViewById(R.id.eventFree);
		paidButton = (RadioButton) rootView.findViewById(R.id.eventPaid);
		costText = (EditText) rootView.findViewById(R.id.eventCost);
		saveButton = (Button) rootView.findViewById(R.id.save_event_button);
		dollarSignText = (TextView) rootView.findViewById(R.id.eventCost_textView);
	}

	/**
	 * This method initializes a Query that populates the AutoCompleteTextView with all the
	 * buildings on campus in a background thread. A validator is attache to the eventLocationText
	 * to make sure a building was chosen from the list.
	 */
	private void queryAndFillAutoCompleteView() {
		Log.i(TAG, "Entering queryAndFillAutoCompleteView()");

		ParseQuery<UMDBuildings> query = ParseQuery.getQuery(UMDBuildings.class);
		query.whereExists("name"); // will get all the buildings
		query.setLimit(200); // setting max num of queries
		query.findInBackground(new FindCallback<UMDBuildings>() {

			@Override
			public void done(List<UMDBuildings> arg0, ParseException arg1) {
				if (arg1 == null) {
					buildingNames = new ArrayList<String>(arg0.size());

					for (UMDBuildings building : arg0) {
						buildingNames.add(building.getName());
					}

					// Create an ArrayAdapter containing country names
					ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(fragAct,
							R.layout.list_item, buildingNames.toArray());

					// Set the adapter for the AutoCompleteTextView and add Validator
					eventLocationText.setAdapter(adapter);
					eventLocationText.setValidator(new Validator());
					eventLocationText.setOnFocusChangeListener(new FocusListener());
				} else {
					arg1.printStackTrace();
				}

			}
		});
	}

	/**
	 * Validator class used with the AutoCompleteTextView for eventLocationTextView. Verifies the
	 * location string exists in buildingNames.
	 * 
	 * @author OJ
	 * 
	 */
	class Validator implements AutoCompleteTextView.Validator {

		@Override
		public boolean isValid(CharSequence text) {
			Log.v(TAG, "Checking if location field valid: " + text);
			if (buildingNames.contains(text.toString())) {
				return true;
			}
			return false;
		}

		@Override
		public CharSequence fixText(CharSequence invalidText) {
			Log.v(TAG, "Clearing location field");
			Toast.makeText(fragAct, "Choose building from list", Toast.LENGTH_SHORT).show();
			return "";
		}
	}

	/**
	 * Used with eventLocationTextView to validate the input in field after view focus is changed.
	 * 
	 * @author OJ
	 * 
	 */
	class FocusListener implements View.OnFocusChangeListener {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			Log.v(TAG, "Focus changed away from eventLocationTextView");
			if (v.getId() == R.id.eventLocation && !hasFocus) {
				Log.v(TAG, "Performing validation");
				((AutoCompleteTextView) v).performValidation();
			}
		}
	}

	/**
	 * Sets the associated listeners for the admissionRadioGroup and the (start/end)-(date/time)
	 * TextViews.
	 */
	private void setViewListeners() {
		Log.i(TAG, "Entering setViewListeners()");
		// RadioGroup Listeners
		Log.i(TAG, "Setting admissionRadioGroupListener");
		admissionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				locationClearFocus();
				if (checkedId == -1) {
					// No item selected
				} else if (checkedId == R.id.eventFree) {
					// This is a free event
					costText.setVisibility(View.INVISIBLE);
					dollarSignText.setVisibility(View.INVISIBLE);
				} else {
					// This is paid event
					costText.setVisibility(View.VISIBLE);
					dollarSignText.setVisibility(View.VISIBLE);
				}
			}
		});

		// OnClickListeners for Date and Time pickers
		Log.i(TAG, "Setting Listeners for Date and Time Pickers");
		startDateText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(fragAct, mStartDateListener, mYear, mMonth, mDay).show();
				startDateText.setError(null);
			}
		});
		endDateText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(fragAct, mEndDateListener, mYear, mMonth, mDay).show();
				endDateText.setError(null);
			}
		});
		startTimeText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new TimePickerDialog(fragAct, mStartTimeListener, mHour, mMinute, false).show();
				startTimeText.setError(null);
			}
		});
		endTimeText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new TimePickerDialog(fragAct, mEndTimeListener, mHour, mMinute, false).show();
				endTimeText.setError(null);
			}
		});

		setSubmitButtonListener();
	}

	/**
	 * The submitButtonListener will check to make sure if all the users input is correct before
	 * adding the new event (or updating) to the database. If any fields are invalid it will display
	 * toast messages to the user.
	 */
	private void setSubmitButtonListener() {
		Log.i(TAG, "Setting SubmitButtonListener");
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				locationClearFocus();

				// Reset errors.
				eventNameText.setError(null);
				startDateText.setError(null);
				startTimeText.setError(null);
				endDateText.setError(null);
				endTimeText.setError(null);
				eventLocationText.setError(null);
				costText.setError(null);

				boolean cancel = false;
				View focusView = null;

				// Checking if required fields are filled
				if ((!paidButton.isChecked() && !freeButton.isChecked())) {
					cancel = true;
				}
				if (paidButton.isChecked() && costText.getText().toString().isEmpty()) {
					costText.setError(getString(R.string.error_field_required));
					focusView = costText;
					cancel = true;
				}
				if (eventLocationText.getText().toString().isEmpty()) {
					eventLocationText.setError(getString(R.string.error_field_required));
					focusView = eventLocationText;
					cancel = true;
				}
				if (endTimeText.getText().toString().isEmpty()
						|| endDateText.getText().toString().isEmpty()) {
					endTimeText.setError(getString(R.string.error_field_required));
					focusView = endTimeText;
					cancel = true;
				}
				if (startTimeText.getText().toString().isEmpty()
						|| startDateText.getText().toString().isEmpty()) {
					startTimeText.setError(getString(R.string.error_field_required));
					focusView = startTimeText;
					cancel = true;
				}
				if (eventNameText.getText().toString().isEmpty()) {
					eventNameText.setError(getString(R.string.error_field_required));
					focusView = eventNameText;
					cancel = true;
				}

				/*
				 * boolean formFilled = true; if (orgNameText.getText().toString().isEmpty() ||
				 * eventNameText.getText().toString().isEmpty() ||
				 * startDateText.getText().toString().isEmpty() ||
				 * endDateText.getText().toString().isEmpty() ||
				 * startTimeText.getText().toString().isEmpty() ||
				 * endTimeText.getText().toString().isEmpty() ||
				 * eventLocationText.getText().toString().isEmpty() || (paidButton.isChecked() &&
				 * costText.getText().toString().isEmpty()) || (!paidButton.isChecked() &&
				 * !freeButton.isChecked())) { formFilled = false; Log.i(TAG,
				 * "All required fields not filled out"); Toast.makeText(fragAct,
				 * "Please fill all required fields", Toast.LENGTH_LONG) .show(); }
				 */

				if (cancel) {
					// There was an error; don't save event and focus the first
					// form field with an error.
					focusView.requestFocus();
					Toast.makeText(fragAct, "Please fill all required fields", Toast.LENGTH_SHORT)
							.show();
				} else {
					Log.i(TAG, "Form filled out, setting eventObject fields");
					mEvent.setOrgName(orgNameText.getText().toString());
					mEvent.setEventName(eventNameText.getText().toString());
					mEvent.setBuildingName(eventLocationText.getText().toString());
					mEvent.setDescription(eventDescriptionText.getText().toString());

					if (paidButton.isChecked()) {
						mEvent.setAdmission("$" + costText.getText().toString());
					} else {
						mEvent.setAdmission("FREE");
					}

					if ((!mEvent.getBuildingName().equals(originalBuilding)))
						isLocationChanged = true;

					/*
					 * saveInBackground uploads the parse object to the DB creating a new save
					 * callback which waits until it is refreshed so we can extract its newly
					 * created object ID
					 */
					mEvent.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException arg0) {
							setResult();
						}
					});
				}

				/*
				 * // All valid input from User, create parse object and add to DB if (formFilled) {
				 * Log.i(TAG, "Form filled out, setting eventObject fields");
				 * mEvent.setOrgName(orgNameText.getText().toString());
				 * mEvent.setEventName(eventNameText.getText().toString());
				 * mEvent.setBuildingName(eventLocationText.getText().toString());
				 * mEvent.setDescription(eventDescriptionText.getText().toString());
				 * 
				 * if (paidButton.isChecked()) { mEvent.setAdmission(costText.getText().toString());
				 * } else { mEvent.setAdmission("FREE"); }
				 * 
				 * if ((!mEvent.getBuildingName().equals(originalBuilding))) isLocationChanged =
				 * true;
				 * 
				 * 
				 * saveInBackground uploads the parse object to the DB creating a new save callback
				 * which waits until it is refreshed so we can extract its newly created object ID
				 * 
				 * mEvent.saveInBackground(new SaveCallback() {
				 * 
				 * @Override public void done(ParseException arg0) { setResult(); } }); }
				 */
			}
		});
	}

	// TODO (minor) - manage default behavior for date/time
	/**
	 * Listener gets the date from DatePicker and updates the associated startDate TextView
	 * 
	 * TODO - implement below behaviors.
	 * 
	 * If startDate or startTime not set, default to current day and current time rounded up to
	 * nearest hour.
	 * 
	 * If endDate or endTime not set, default to one hour past startDate/Time If endDate/Time
	 * changed to before startDate/Time, change to difference in shift in endDate/Time
	 */
	DatePickerDialog.OnDateSetListener mStartDateListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			startCal.set(Calendar.YEAR, year);
			startCal.set(Calendar.MONTH, monthOfYear);
			startCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDateLabel(startDateText, 0);
			Date date = null;
			try {
				// Adding 1 to month because it starts at index 0
				date = new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(String.format(
						"%02d/%02d/%04d", monthOfYear + 1, dayOfMonth, year));
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
				mEvent.setStartDate(sdf.format(date));
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * Listener gets the date from DatePicker and updates the associated endDate TextView
	 */
	DatePickerDialog.OnDateSetListener mEndDateListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			endCal.set(Calendar.YEAR, year);
			endCal.set(Calendar.MONTH, monthOfYear);
			endCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDateLabel(endDateText, 1);
			Date date = null;
			try {
				// Adding 1 to month because it starts at index 0
				date = new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(String.format(
						"%02d/%02d/%04d", monthOfYear + 1, dayOfMonth, year));
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
				mEvent.setEndDate(sdf.format(date));
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * Listener gets the time from TimePicker and updates the associated startTime TextView
	 */
	TimePickerDialog.OnTimeSetListener mStartTimeListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			startCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
			startCal.set(Calendar.MINUTE, minute);
			updateTimeLabel(startTimeText, 0);
			Date date = null;
			try {
				date = new SimpleDateFormat("hh:mm", Locale.US).parse(String.format("%02d:%02d",
						hourOfDay, minute));
				SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
				mEvent.setStartTime(sdf.format(date));
			} catch (java.text.ParseException e) {

				e.printStackTrace();
			}
		}
	};

	/**
	 * Listener gets the time from TimePicker and updates the associated endTime TextView
	 */
	TimePickerDialog.OnTimeSetListener mEndTimeListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			endCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
			endCal.set(Calendar.MINUTE, minute);
			updateTimeLabel(endTimeText, 1);
			Date date = null;
			try {
				date = new SimpleDateFormat("hh:mm", Locale.US).parse(String.format("%02d:%02d",
						hourOfDay, minute));
				SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
				mEvent.setEndTime(sdf.format(date));
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * Updates the associated Date TextView with the date set with the DatePicker
	 * 
	 * @param tv
	 *            The TextView (either starting date or ending date) to be updated
	 * @param startEnd
	 *            Determines which Calendar to use: 0 = start 1 = end
	 */
	private void updateDateLabel(TextView tv, int startEnd) {
		Log.i(TAG, "Entering updateDateLabel()");
		String myFormat = "EEEE, MMM dd, yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
		if (startEnd == 0) {
			tv.setText(sdf.format(startCal.getTime()));
		} else {
			tv.setText(sdf.format(endCal.getTime()));
		}
	}

	/**
	 * Updates the associated Time TextView with the time set with the TimePicker
	 * 
	 * @param tv
	 *            The TextView (either starting time or ending time) to be updated
	 * @param startEnd
	 *            Determines which Calendar to use: 0 = start 1 = end
	 */
	private void updateTimeLabel(TextView tv, int startEnd) {
		Log.i(TAG, "Entering updateTimeLabel()");
		String myFormat = "hh:mm aa";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
		if (startEnd == 0) {
			tv.setText(sdf.format(startCal.getTime()));
		} else {
			tv.setText(sdf.format(endCal.getTime()));
		}
	}

	/**
	 * Clears focus from eventLocationText in order to ensure validation of building
	 */
	private void locationClearFocus() {
		if (eventLocationText.isFocused()) {
			eventLocationText.clearFocus();
		}
	}

	/**
	 * Loads all the information into the form from the eventObject that is being edited.
	 */
	private void loadEventData() {
		Log.i(TAG, "Entering loadEventData()");
		try {
			eventNameText.setText(mEvent.getEventName());
			startDateText.setText(mEvent.getStartDate());
			startTimeText.setText(mEvent.getStartTime());
			endDateText.setText(mEvent.getEndDate());
			endTimeText.setText(mEvent.getEndTime());
			originalBuilding = mEvent.getBuildingName();
			eventLocationText.setText(originalBuilding);
			if (mEvent.getDescription() != "") {
				eventDescriptionText.setText(mEvent.getDescription());
			}
			if (mEvent.getAdmission().equals("FREE")) {
				freeButton.setChecked(true);
			} else {
				paidButton.setChecked(true);
				costText.setText(mEvent.getAdmission());
			}
		} catch (Exception e) {
			// Event is null, can not edit event
		}
	}

	/**
	 * Sets the result of the FragementActivity by passing an intent with information about
	 * buildings added/modified in order to update the map.
	 */
	private void setResult() {
		Log.i(TAG, "Entering setResult()");

		Intent intent = new Intent();
		if (isNewEvent || isLocationChanged) { // Building added or changed to
			intent.putExtra("addedNames", mEvent.getBuildingName());
		}
		if (isLocationChanged) { // Building removed from event
			intent.putExtra("deletedNames", originalBuilding);
		}
		if (!isNewEvent) { // ObjectID if editing event
			intent.putExtra("objectID", objectID);
		}
		fragAct.setResult(Activity.RESULT_OK, intent);
		if (isNewEvent) {
			Log.i(TAG, "Event successfully added.");
			Toast.makeText(fragAct, "Event Created", Toast.LENGTH_SHORT).show();
		} else {
			Log.i(TAG, "Event successfully updated.");
			Toast.makeText(fragAct, "Event Updated", Toast.LENGTH_SHORT).show();
		}
		fragAct.finish(); // finish activity after submitting event
	}
}
