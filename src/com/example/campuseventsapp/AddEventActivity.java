package com.example.campuseventsapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.example.campuseventsapp.R;
import com.parse.FindCallback;
import com.parse.ParseQuery;
import com.parse.ParseException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

public class AddEventActivity extends Activity {

	private static final String TAG = "AddEventActivity";

	public EventObject eventObject;
	Calendar myCalendar = Calendar.getInstance();

	// Variables for widgets in layout
	TextView orgNameView, startDateView, startTimeView, endDateView,
			endTimeView;
	AutoCompleteTextView eventLocView;
	EditText eventNameView, eventDescrView, eventCostView;
	RadioGroup admissionRadioGroup;
	Button saveButton;

	// TODO - once AdminActivity created, change parent Activity in
	// AndroidManifest.xml
	// TODO - once done with this activity, create EditEventActivity

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_add_event);
		// TODO - Update action bar to include X button instead of back
		// TODO - Update action bar to include save button and remove from
		// layout
		ActionBar actionBar = getActionBar();
		actionBar.setTitle("New Event");

		eventObject = new EventObject();

		// TODO - fix parse stuff
		parseStuff();

		// cache all widgets
		cacheWidgets();

		// TODO - Restore settings (if editing event)

		setViewListeners();

	} // end of onCreate

	private void parseStuff() {
		ParseQuery<UMDBuildings> query = ParseQuery
				.getQuery(UMDBuildings.class);
		query.whereExists("name");
		query.setLimit(200);
		query.findInBackground(new FindCallback<UMDBuildings>() {

			@Override
			public void done(List<UMDBuildings> arg0, ParseException arg1) {

				ArrayList<String> buildNames = new ArrayList<String>(arg0
						.size());

				for (UMDBuildings a : arg0) {

					/*
					 * if (!a.getBuildAbrev().equals("EEE")) {
					 * buildNames.add(a.getName() + " " + a.getBuildAbrev()); }
					 * else { buildNames.add(a.getName()); }
					 */

					buildNames.add(a.getName());
				}

				Log.i(TAG,
						"size of buildNames is: "
								+ Integer.toString(buildNames.size()));
				// Get a reference to the AutoCompleteTextView
				AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.eventLocation);

				// Create an ArrayAdapter containing country names
				ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(
						AddEventActivity.this, R.layout.list_item, buildNames
								.toArray());

				// Set the adapter for the AutoCompleteTextView
				textView.setAdapter(adapter);

			}
		});

	}

	private void setViewListeners() {

		// Note: we don't need a listener for orgNameView because we should be
		// setting it automatically when they are logged in

		// OnClickListeners for Date and Time pickers
		startDateView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(AddEventActivity.this, startDatePicker,
						myCalendar.get(Calendar.YEAR), myCalendar
								.get(Calendar.MONTH), myCalendar
								.get(Calendar.DAY_OF_MONTH)).show();
			}
		});
		endDateView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(AddEventActivity.this, endDatePicker,
						myCalendar.get(Calendar.YEAR), myCalendar
								.get(Calendar.MONTH), myCalendar
								.get(Calendar.DAY_OF_MONTH)).show();
			}
		});
		startTimeView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new TimePickerDialog(AddEventActivity.this, startTimePicker,
						myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar
								.get(Calendar.MINUTE), false).show();
			}
		});
		endTimeView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new TimePickerDialog(AddEventActivity.this, endTimePicker,
						myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar
								.get(Calendar.MINUTE), false).show();
			}
		});

		// OnClickListeners for Event Name, Location, and Description EditText
		eventNameView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eventObject.setEventName(eventNameView.getText().toString());
			}
		});

		eventLocView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eventObject.setBuildingName(eventLocView.getText().toString());
			}
		});

		eventDescrView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eventObject.setDescription(eventDescrView.getText().toString());
			}
		});

		admissionRadioGroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == -1) {
							// no item selected
						} else if (checkedId == R.id.eventFree) {
							eventObject.setAdmission("FREE");
							eventCostView.setVisibility(View.INVISIBLE);
						} else {
							// event paid
							eventCostView.setVisibility(View.VISIBLE);
						}

					}
				});

		// TODO: need to engage the done/submit button on keyboard
		eventCostView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eventObject.setAdmission(eventCostView.getText().toString());
			}
		});

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				eventObject.saveInBackground();
				Intent intent = new Intent();
				intent.putExtra("eventID", eventObject.getObjectId());
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});

	}

	private void cacheWidgets() {
		orgNameView = (TextView) findViewById(R.id.studentOrgName);
		orgNameView.setText("Student Org Name");
		eventNameView = (EditText) findViewById(R.id.eventTitle);
		startDateView = (TextView) findViewById(R.id.eventStartDate);
		endDateView = (TextView) findViewById(R.id.eventEndDate);
		startTimeView = (TextView) findViewById(R.id.eventStartTime);
		endTimeView = (TextView) findViewById(R.id.eventEndTime);
		eventLocView = (AutoCompleteTextView) findViewById(R.id.eventLocation);
		eventDescrView = (EditText) findViewById(R.id.eventDescription);
		admissionRadioGroup = (RadioGroup) findViewById(R.id.eventAdmissionRadioGroup);
		eventCostView = (EditText) findViewById(R.id.eventCost);
		saveButton = (Button) findViewById(R.id.save_event_button);

	}

	// TODO - manage default behaviour for date/time
	// If startDate or startTime not set, default to current day and current time rounded up to nearest hour.
	// If endDate or endTime not set, default to one hour past startDate/Time
	// If endDate/Time changed to before startDate/Time, change to difference in shift in endDate/Time
	
	DatePickerDialog.OnDateSetListener startDatePicker = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDateLabel(startDateView);
			eventObject.setStartDate(Integer.toString(monthOfYear) + "/"
					+ Integer.toString(dayOfMonth) + "/"
					+ Integer.toString(year));
		}
	};
	DatePickerDialog.OnDateSetListener endDatePicker = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDateLabel(endDateView);
			eventObject.setEndDate(Integer.toString(monthOfYear) + "/"
					+ Integer.toString(dayOfMonth) + "/"
					+ Integer.toString(year));
		}
	};
	TimePickerDialog.OnTimeSetListener startTimePicker = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			myCalendar.set(Calendar.MINUTE, minute);
			updateTimeLabel(startTimeView);
			if (myCalendar.get(Calendar.AM_PM) == Calendar.AM) {
				eventObject.setStartTime(Integer.toString(hourOfDay) + ":"
						+ Integer.toString(minute) + " AM");
			} else {
				eventObject.setStartTime(Integer.toString(hourOfDay) + ":"
						+ Integer.toString(minute) + " PM");
			}
		}
	};
	TimePickerDialog.OnTimeSetListener endTimePicker = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			myCalendar.set(Calendar.MINUTE, minute);
			updateTimeLabel(endTimeView);
			if (myCalendar.get(Calendar.AM_PM) == Calendar.AM) {
				eventObject.setEndTime(Integer.toString(hourOfDay) + ":"
						+ Integer.toString(minute) + " AM");
			} else {
				eventObject.setEndTime(Integer.toString(hourOfDay) + ":"
						+ Integer.toString(minute) + " PM");
			}
		}
	};

	/**
	 * Updates the associated Date TextView
	 * 
	 * @param tv
	 *            The TextView (either starting date or ending date) to be
	 *            updated
	 */
	private void updateDateLabel(TextView tv) {
		String myFormat = "EEEE, MMM dd, yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
		tv.setText(sdf.format(myCalendar.getTime()));
	}

	/**
	 * Updates the associated Time TextView
	 * 
	 * @param tv
	 *            The TextView (either starting time or ending time) to be
	 *            updated
	 */
	private void updateTimeLabel(TextView tv) {
		String myFormat = "hh:mm aa";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
		tv.setText(sdf.format(myCalendar.getTime()));
	}

}
