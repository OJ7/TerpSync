package com.terpsync;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.terpsync.R;
import com.terpsync.card.CardListAdapter;
import com.terpsync.parse.EventObject;
import com.terpsync.parse.ParseConstants;
import com.terpsync.parse.UMDBuildings;
import com.google.android.gms.internal.el;
import com.parse.FindCallback;
import com.parse.ParseQuery;
import com.parse.ParseException;
import com.parse.SaveCallback;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

	private static final String TAG = "EditEventFragement";

	TextView orgNameText, startDateText, startTimeText, endDateText, endTimeText, dollarSignText;
	AutoCompleteTextView eventLocationText;
	EditText eventNameText, eventDescriptionText, costText;
	RadioGroup admissionRadioGroup;
	RadioButton freeButton, paidButton;
	Button saveButton;
	String currentOrganization;
	ArrayList<String> buildingNames;

	private EventObject mEvent;
	private Calendar myCalendar = Calendar.getInstance();
	private LinearLayout rootView;
	private FragmentActivity fragAct;
	private Intent mIntent;
	private int mYear = myCalendar.get(Calendar.YEAR), mMonth = myCalendar.get(Calendar.MONTH),
			mDay = myCalendar.get(Calendar.DAY_OF_MONTH), mHour = myCalendar
					.get(Calendar.HOUR_OF_DAY), mMinute = myCalendar.get(Calendar.MINUTE);
	private boolean isNewEvent; // determines whether to create new event or edit exisiting one

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragAct = super.getActivity();
		rootView = (LinearLayout) inflater
				.inflate(R.layout.fragment_manage_event, container, false);

		mIntent = fragAct.getIntent();
		cacheWidgets();
		queryAndFillAutoCompleteView();
		setViewListeners();
		setSubmitButtonListener();

		currentOrganization = mIntent.getStringExtra(ParseConstants.admin_org_name);
		orgNameText.setText(currentOrganization);

		isNewEvent = mIntent.getBooleanExtra("isNew", false);

		// check if adding or editing
		if (isNewEvent) { // Creating Event
			Log.i(TAG, "Adding event as:" + currentOrganization);
			mEvent = new EventObject();
		} else { // Editing Event
			Log.i(TAG, "Editing event as:" + currentOrganization);
			// Getting the EventObject and assigning to mEvent
			String id = mIntent.getStringExtra(ParseConstants.event_object_id);
			try {
				ParseQuery<EventObject> eventsQuery = ParseQuery.getQuery(EventObject.class);
				mEvent = eventsQuery.get(id);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			loadEventData();
		}

		return rootView;
	}

	public ManageEventFragment() {

	}

	public ManageEventFragment(EventObject event, boolean readOnly, Intent intent) {
		mEvent = event;
		mIntent = intent;
		setHasOptionsMenu(true);
	}

	/**
	 * Get references to all the views
	 */
	private void cacheWidgets() {
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
	 * TODO - update documentation
	 * 
	 * This method initializes a Query that populates the AutoCompleteTextView with all the
	 * Buildings on Campus in a background thread.
	 */
	private void queryAndFillAutoCompleteView() {
		ParseQuery<UMDBuildings> query = ParseQuery.getQuery(UMDBuildings.class);
		query.whereExists("name"); // will get all the buildings
		query.setLimit(200); // setting max num of queries
		query.findInBackground(new FindCallback<UMDBuildings>() {

			@Override
			public void done(List<UMDBuildings> arg0, ParseException arg1) {
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
			return "";
		}
	}

	/**
	 * Used with eventLocationTextView to validate the input in field after view focus is changed.
	 * 
	 * @author Gooner
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
	 * TODO - Add documentation
	 */
	private void setViewListeners() {
		// RadioGroup Listeners
		admissionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
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
		startDateText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(fragAct, mStartDateListener, mYear, mMonth, mDay).show();

			}
		});
		endDateText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(fragAct, mEndDateListener, mYear, mMonth, mDay).show();
			}
		});
		startTimeText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new TimePickerDialog(fragAct, mStartTimeListener, mHour, mMinute, false).show();
			}
		});
		endTimeText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new TimePickerDialog(fragAct, mEndTimeListener, mHour, mMinute, false).show();
			}
		});

	}

	/**
	 * TODO - update documentation
	 * 
	 * The Save Button listener will check to make sure if all the users input is correct before
	 * adding the new event to the database. If any fields are invalid it will display toast
	 * messages to the user.
	 * 
	 * It will then query the GPS coordinates based on the building selected by the user and include
	 * that in the mEvent information for easy data base retrieval when adding a marker in
	 * onActivityResult
	 */
	private void setSubmitButtonListener() {

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				boolean formFilled = true;

				// TODO (major) - make sure location from list is chosen
				if (orgNameText.getText().toString().isEmpty()
						|| eventNameText.getText().toString().isEmpty()
						|| startDateText.getText().toString().isEmpty()
						|| endDateText.getText().toString().isEmpty()
						|| startTimeText.getText().toString().isEmpty()
						|| endTimeText.getText().toString().isEmpty()
						|| eventLocationText.getText().toString().isEmpty()
						|| (paidButton.isChecked() && costText.getText().toString().isEmpty())
						|| (!paidButton.isChecked() && !freeButton.isChecked())) {

					formFilled = false;
					Toast.makeText(fragAct, "Please fill all required fields", Toast.LENGTH_LONG)
							.show();

				}

				// All valid input from User, create parse object and add to DB
				if (formFilled) {

					mEvent.setOrgName(orgNameText.getText().toString());
					mEvent.setEventName(eventNameText.getText().toString());
					mEvent.setBuildingName(eventLocationText.getText().toString());
					mEvent.setDescription(eventDescriptionText.getText().toString());

					if (paidButton.isChecked()) {
						mEvent.setAdmission(costText.getText().toString());
					} else {
						mEvent.setAdmission("FREE");
					}

					/*
					 * saveInBackground uploads the parse object to the DB creating a new save
					 * callback which waits until it is refreshed so we can extract its newly
					 * created object ID
					 */
					mEvent.saveInBackground(new SaveCallback() {

						@Override
						public void done(ParseException arg0) {
							fragAct.setResult(
									Activity.RESULT_OK,
									new Intent().putExtra("addBuildingName",
											mEvent.getBuildingName()));
							Log.i(TAG, "Event successfully added.");
							Toast.makeText(fragAct, "Created event successfully",
									Toast.LENGTH_SHORT).show();
							fragAct.finish();
						}
					});
				}

			}
		});

	}

	// TODO (minor) - manage default behavior for date/time
	/**
	 * TODO - update documentation
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
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDateLabel(startDateText);
			Date date = null;
			try {
				date = new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(String.format(
						"%02d/%02d/%04d", monthOfYear + 1, dayOfMonth, year)); // Adding 1 to month
																				// because it starts
																				// at index 0
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
				mEvent.setStartDate(sdf.format(date));
			} catch (java.text.ParseException e) {

				e.printStackTrace();
			}
		}
	};

	/**
	 * TODO - Add documentation
	 */
	DatePickerDialog.OnDateSetListener mEndDateListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDateLabel(endDateText);
			Date date = null;
			try {
				date = new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(String.format(
						"%02d/%02d/%04d", monthOfYear + 1, dayOfMonth, year)); // Adding 1 to month
																				// because it starts
																				// at index 0
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
				mEvent.setEndDate(sdf.format(date));
			} catch (java.text.ParseException e) {

				e.printStackTrace();
			}
		}
	};

	/**
	 * TODO - Add documentation
	 */
	TimePickerDialog.OnTimeSetListener mStartTimeListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			myCalendar.set(Calendar.MINUTE, minute);
			updateTimeLabel(startTimeText);
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
	 * TODO - Add documentation
	 */
	TimePickerDialog.OnTimeSetListener mEndTimeListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			myCalendar.set(Calendar.MINUTE, minute);
			updateTimeLabel(endTimeText);
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
	 * TODO - update documentation
	 * 
	 * Updates the associated Date TextView
	 * 
	 * @param tv
	 *            The TextView (either starting date or ending date) to be updated
	 */
	private void updateDateLabel(TextView tv) {
		String myFormat = "EEEE, MMM dd, yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
		tv.setText(sdf.format(myCalendar.getTime()));
	}

	/**
	 * TODO - update documentation
	 * 
	 * Updates the associated Time TextView
	 * 
	 * @param tv
	 *            The TextView (either starting time or ending time) to be updated
	 */
	private void updateTimeLabel(TextView tv) {
		String myFormat = "hh:mm aa";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
		tv.setText(sdf.format(myCalendar.getTime()));
	}

	/**
	 * TODO - add doc
	 * 
	 * @param mEvent
	 */
	private void loadEventData() {
		try {
			eventNameText.setText(mEvent.getEventName());
			startDateText.setText(mEvent.getStartDate());
			startTimeText.setText(mEvent.getStartTime());
			endDateText.setText(mEvent.getEndDate());
			endTimeText.setText(mEvent.getEndTime());
			eventLocationText.setText(mEvent.getBuildingName());
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
			// TODO: handle exception
			// Event is null, can not edit event
		}
	}

}
