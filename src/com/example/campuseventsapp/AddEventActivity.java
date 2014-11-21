package com.example.campuseventsapp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.example.campuseventsapp.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

public class AddEventActivity extends Activity {

	Calendar myCalendar = Calendar.getInstance();
	TextView dateTextView, startTimeTextView, endTimeTextView;

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

		dateTextView = (TextView) findViewById(R.id.eventDate);
		// TODO - setOnClickListener for both Time TextViews
		startTimeTextView = (TextView) findViewById(R.id.eventStartTime);
		endTimeTextView = (TextView) findViewById(R.id.eventEndTime);

		dateTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(AddEventActivity.this, date, myCalendar
						.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
						myCalendar.get(Calendar.DAY_OF_MONTH)).show();
			}
		});
	}

	DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDateLabel();
		}
	};

	private void updateDateLabel() {
		String myFormat = "EEEE, MMM dd, yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
		dateTextView.setText(sdf.format(myCalendar.getTime()));
	}
}
