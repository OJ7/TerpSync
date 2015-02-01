package com.terpsync.events;

import com.terpsync.R;
import com.terpsync.R.layout;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class AddEventActivity extends FragmentActivity {

	private static final String TAG = "AddEventActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "Entering onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_event);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00A0B0")));
		actionBar.setTitle("Add Event");
	}

}
