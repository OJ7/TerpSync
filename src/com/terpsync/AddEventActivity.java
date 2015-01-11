package com.terpsync;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import com.terpsync.R;
import com.terpsync.parse.EventObject;
import com.terpsync.parse.ParseConstants;
import com.terpsync.parse.UMDBuildings;
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
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddEventActivity extends Activity {

	private static final String TAG = "AddEventActivity";

	EventObject eventObject;
	Calendar myCalendar = Calendar.getInstance();

	TextView orgNameTextView, startDateTextView, startTimeTextView, endDateTextView,
			endTimeTextView, dollarSignTextView;
	AutoCompleteTextView eventLocationTextView;
	EditText eventNameEditText, eventDescriptionEditText, costEditText;
	RadioGroup admissionRadioGroup;
	RadioButton freeButton, paidButton;
	Button saveButton;
	String currentOrganization;

	// TODO - once AdminActivity created, change this class's parent Activity in
	// AndroidManifest.xml

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_event);
		ActionBar actionBar = getActionBar();
		actionBar.setTitle("New Event");
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00A0B0")));
		Intent intent = getIntent();
		currentOrganization = intent.getStringExtra(ParseConstants.admin_org_name);
		Log.i(TAG, "Adding event as:" + currentOrganization);

		eventObject = new EventObject();

		cacheWidgets();
		queryAndFillAutoCompleteView();
		setViewListeners();
		setSubmitButtonListener();

		orgNameTextView.setText(currentOrganization);

	} // end of onCreate

	/**
	 * TODO - update documentation Get references to all the views
	 */
	private void cacheWidgets() {
		orgNameTextView = (TextView) findViewById(R.id.studentOrgName);
		eventNameEditText = (EditText) findViewById(R.id.eventTitle);
		startDateTextView = (TextView) findViewById(R.id.eventStartDate);
		endDateTextView = (TextView) findViewById(R.id.eventEndDate);
		startTimeTextView = (TextView) findViewById(R.id.eventStartTime);
		endTimeTextView = (TextView) findViewById(R.id.eventEndTime);
		eventLocationTextView = (AutoCompleteTextView) findViewById(R.id.eventLocation);
		eventDescriptionEditText = (EditText) findViewById(R.id.eventDescription);
		admissionRadioGroup = (RadioGroup) findViewById(R.id.eventAdmissionRadioGroup);
		freeButton = (RadioButton) findViewById(R.id.eventFree);
		paidButton = (RadioButton) findViewById(R.id.eventPaid);
		costEditText = (EditText) findViewById(R.id.eventCost);
		saveButton = (Button) findViewById(R.id.save_event_button);
		dollarSignTextView = (TextView) findViewById(R.id.eventCost_textView);
	}

	/**
	 * TODO - update documentation This method initializes a Query that populates the
	 * AutoCompleteTextView with all the Buildings on Campus in a background thread.
	 */
	private void queryAndFillAutoCompleteView() {
		ParseQuery<UMDBuildings> query = ParseQuery.getQuery(UMDBuildings.class);
		query.whereExists("name"); // will get all the buildings
		query.setLimit(200); // setting max num of queries
		query.findInBackground(new FindCallback<UMDBuildings>() {

			@Override
			public void done(List<UMDBuildings> arg0, ParseException arg1) {
				ArrayList<String> buildingNames = new ArrayList<String>(arg0.size());

				for (UMDBuildings building : arg0) {
					buildingNames.add(building.getName());
				}

				// Create an ArrayAdapter containing country names
				ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(AddEventActivity.this,
						R.layout.list_item, buildingNames.toArray());

				// Set the adapter for the AutoCompleteTextView
				eventLocationTextView.setAdapter(adapter);
			}
		});

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
					costEditText.setVisibility(View.INVISIBLE);
					dollarSignTextView.setVisibility(View.INVISIBLE);
				} else {
					// This is has an admission fee
					costEditText.setVisibility(View.VISIBLE);
					dollarSignTextView.setVisibility(View.VISIBLE);
				}

			}
		});

		// OnClickListeners for Date and Time pickers
		startDateTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(AddEventActivity.this, startDatePicker, myCalendar
						.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar
						.get(Calendar.DAY_OF_MONTH)).show();

			}
		});
		endDateTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(AddEventActivity.this, endDatePicker, myCalendar
						.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar
						.get(Calendar.DAY_OF_MONTH)).show();
			}
		});
		startTimeTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new TimePickerDialog(AddEventActivity.this, startTimePicker, myCalendar
						.get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), false).show();
			}
		});
		endTimeTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new TimePickerDialog(AddEventActivity.this, endTimePicker, myCalendar
						.get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), false).show();
			}
		});

	}

	/**
	 * TODO - update documentation The Save Button listener will check to make sure if all the users
	 * input is correct before adding the new event to the database. If any fields are invalid it
	 * will display toast messages to the user.
	 * 
	 * It will then query the GPS coordinates based on the building selected by the user and include
	 * that in the events information for easy data base retrieval when adding a marker in
	 * onActivityResult
	 */
	private void setSubmitButtonListener() {

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				boolean formFilled = true;

				// TODO (major) - make sure location from list is chosen
				if (orgNameTextView.getText().toString().isEmpty()
						|| eventNameEditText.getText().toString().isEmpty()
						|| startDateTextView.getText().toString().isEmpty()
						|| endDateTextView.getText().toString().isEmpty()
						|| startTimeTextView.getText().toString().isEmpty()
						|| endTimeTextView.getText().toString().isEmpty()
						|| eventLocationTextView.getText().toString().isEmpty()
						|| (paidButton.isChecked() && costEditText.getText().toString().isEmpty())
						|| (!paidButton.isChecked() && !freeButton.isChecked())) {

					formFilled = false;
					Toast.makeText(getApplicationContext(), "Please fill all fields",
							Toast.LENGTH_LONG).show();

				}

				// All valid input from User, create parse object and add to DB
				if (formFilled) {

					eventObject.setOrgName(orgNameTextView.getText().toString());
					eventObject.setEventName(eventNameEditText.getText().toString());
					eventObject.setBuildingName(eventLocationTextView.getText().toString());
					eventObject.setDescription(eventDescriptionEditText.getText().toString());

					if (paidButton.isChecked()) {
						eventObject.setAdmission(costEditText.getText().toString());
					} else {
						eventObject.setAdmission("FREE");
					}

					/*
					 * saveInBackground uploads the parse object to the DB creating a new save
					 * callback which waits until it is refreshed so we can extract its newly
					 * created object ID
					 */
					eventObject.saveInBackground(new SaveCallback() {

						@Override
						public void done(ParseException arg0) {
							setResult(
									Activity.RESULT_OK,
									new Intent().putExtra("addBuildingName",
											eventObject.getBuildingName()));
							finish();
						}
					});
				}

			}
		});

	}

	// TODO (minor) - manage default behavior for date/time
	/**
	 * TODO - update documentation If startDate or startTime not set, default to current day and
	 * current time rounded up to nearest hour.
	 * 
	 * If endDate or endTime not set, default to one hour past startDate/Time If endDate/Time
	 * changed to before startDate/Time, change to difference in shift in endDate/Time
	 */
	DatePickerDialog.OnDateSetListener startDatePicker = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDateLabel(startDateTextView);
			Date date = null;
			try {
				date = new SimpleDateFormat("MM/dd/yyyy").parse(String.format("%02d/%02d/%04d",
						monthOfYear, dayOfMonth, year));
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				eventObject.setStartDate(sdf.format(date));
			} catch (java.text.ParseException e) {

				e.printStackTrace();
			}
		}
	};

	/**
	 * TODO - Add documentation
	 */
	DatePickerDialog.OnDateSetListener endDatePicker = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDateLabel(endDateTextView);
			Date date = null;
			try {
				date = new SimpleDateFormat("MM/dd/yyyy").parse(String.format("%02d/%02d/%04d",
						monthOfYear, dayOfMonth, year));
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				eventObject.setEndDate(sdf.format(date));
			} catch (java.text.ParseException e) {

				e.printStackTrace();
			}
		}
	};

	/**
	 * TODO - Add documentation
	 */
	TimePickerDialog.OnTimeSetListener startTimePicker = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			myCalendar.set(Calendar.MINUTE, minute);
			updateTimeLabel(startTimeTextView);
			Date date = null;
			try {
				date = new SimpleDateFormat("hh:mm").parse(String.format("%02d:%02d", hourOfDay,
						minute));
				SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
				eventObject.setStartTime(sdf.format(date));
			} catch (java.text.ParseException e) {

				e.printStackTrace();
			}
		}
	};

	/**
	 * TODO - Add documentation
	 */
	TimePickerDialog.OnTimeSetListener endTimePicker = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			myCalendar.set(Calendar.MINUTE, minute);
			updateTimeLabel(endTimeTextView);
			Date date = null;
			try {
				date = new SimpleDateFormat("hh:mm").parse(String.format("%02d:%02d", hourOfDay,
						minute));
				SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
				eventObject.setEndTime(sdf.format(date));
			} catch (java.text.ParseException e) {

				e.printStackTrace();
			}
		}
	};

	/**
	 * TODO - update documentation Updates the associated Date TextView
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
	 * TODO - update documentation Updates the associated Time TextView
	 * 
	 * @param tv
	 *            The TextView (either starting time or ending time) to be updated
	 */
	private void updateTimeLabel(TextView tv) {
		String myFormat = "hh:mm aa";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
		tv.setText(sdf.format(myCalendar.getTime()));
	}

}
