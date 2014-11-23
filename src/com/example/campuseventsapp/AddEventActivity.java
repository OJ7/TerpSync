package com.example.campuseventsapp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.example.campuseventsapp.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

public class AddEventActivity extends Activity {

	/*
	// Used for storing extras in intent
	public static final String EXTRA_EVENT_TITLE = "event_title";
    public static final String EXTRA_EVENT_START_DATE = "event_start_date";
    public static final String EXTRA_EVENT_END_DATE = "event_end_date";
    public static final String EXTRA_EVENT_START_TIME = "event_start_time";
    public static final String EXTRA_EVENT_END_TIME= "event_end_time";
    public static final String EXTRA_EVENT_LOCATION = "event_location";
    public static final String EXTRA_EVENT_DESCRIPTION = "event_description";
    public static final String EXTRA_EVENT_ADMISSION = "event_admission_fee";
    
    // Values for event fields
    String mTitle, mStartDate, mStartTime, mEndDate, mEndTime, mLocation, mDescription;
    boolean mAdmissionFee;
    */
	
	Calendar myCalendar = Calendar.getInstance();

	// Variables for widgets in layout
	TextView orgNameTextView, titleTextView, startDateTextView, startTimeTextView, endDateTextView,
			endTimeTextView, locationTextView, descriptionTextView;
	RadioGroup admissionRadioGroup;
	Button saveButton;

	// TODO - once AdminActivity created, change parent Activity in AndroidManifest.xml 
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

		// cache all widgets
		orgNameTextView = (TextView) findViewById(R.id.studentOrgName);
		titleTextView = (TextView) findViewById(R.id.eventTitle);
		startDateTextView = (TextView) findViewById(R.id.eventStartDate);
		endDateTextView = (TextView) findViewById(R.id.eventEndDate);
		startTimeTextView = (TextView) findViewById(R.id.eventStartTime);
		endTimeTextView = (TextView) findViewById(R.id.eventEndTime);
		locationTextView = (TextView) findViewById(R.id.eventLocation);
		descriptionTextView = (TextView) findViewById(R.id.eventDescription);
		admissionRadioGroup = (RadioGroup) findViewById(R.id.eventAdmissionRadioGroup);
		saveButton = (Button) findViewById(R.id.save_event_button);

		
		// TODO - Restore settings (if editing event)
		
		
		// OnClickListeners for Date and Time pickers
		startDateTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(AddEventActivity.this, startDatePicker,
						myCalendar.get(Calendar.YEAR), myCalendar
								.get(Calendar.MONTH), myCalendar
								.get(Calendar.DAY_OF_MONTH)).show();
			}
		});
		endDateTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(AddEventActivity.this, endDatePicker,
						myCalendar.get(Calendar.YEAR), myCalendar
								.get(Calendar.MONTH), myCalendar
								.get(Calendar.DAY_OF_MONTH)).show();
			}
		});
		startTimeTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new TimePickerDialog(AddEventActivity.this, startTimePicker,
						myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar
								.get(Calendar.MINUTE), false).show();
			}
		});
		endTimeTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new TimePickerDialog(AddEventActivity.this, endTimePicker,
						myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar
								.get(Calendar.MINUTE), false).show();
			}
		});

	}

	DatePickerDialog.OnDateSetListener startDatePicker = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDateLabel(startDateTextView);
		}
	};
	DatePickerDialog.OnDateSetListener endDatePicker = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDateLabel(endDateTextView);
		}
	};
	TimePickerDialog.OnTimeSetListener startTimePicker = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			myCalendar.set(Calendar.MINUTE, minute);
			updateTimeLabel(startTimeTextView);
		}
	};
	TimePickerDialog.OnTimeSetListener endTimePicker = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			myCalendar.set(Calendar.MINUTE, minute);
			updateTimeLabel(endTimeTextView);
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
	
	/*
	protected void onSaveInstanceStat(Bundle b){
        b.putString(EXTRA_EVENT_TITLE, mTitle);
        b.putString(EXTRA_EVENT_START_DATE, mStartDate);
        b.putString(EXTRA_EVENT_START_TIME, mStartTime);
        b.putString(EXTRA_EVENT_END_DATE, mEndDate);
        b.putString(EXTRA_EVENT_END_TIME, mEndTime);
        b.putString(EXTRA_EVENT_LOCATION, mLocation);
        b.putString(EXTRA_EVENT_DESCRIPTION, mDescription);
        b.putBoolean(EXTRA_EVENT_ADMISSION, mAdmissionFee);
        
    }
    */

}
