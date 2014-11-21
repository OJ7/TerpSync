package com.example.campuseventsapp;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.example.campuseventsapp.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
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

		ActionBar actionBar = getActionBar();
		actionBar.setTitle("New Event");

		dateTextView = (TextView) findViewById(R.id.eventDate);
		startTimeTextView = (TextView) findViewById(R.id.eventStartTime);
		endTimeTextView = (TextView) findViewById(R.id.eventEndTime);

		dateTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
			// TODO Auto-generated method stub
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDateLabel();
		}

	};

	private void updateDateLabel() {

		String myFormat = "EEEE, MMM dd, yyyy"; // In which you need put here
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

		dateTextView.setText(sdf.format(myCalendar.getTime()));
	}
}
