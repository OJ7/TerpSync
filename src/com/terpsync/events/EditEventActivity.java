package com.terpsync.events;

import com.terpsync.R;
import com.terpsync.R.layout;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class EditEventActivity extends FragmentActivity {

	private static final String TAG = "EditEventActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "Entering onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_event);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00A0B0")));
		actionBar.setTitle("Edit Event");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
		Log.i(TAG, "Getting activity result");
		if (requestCode == 0 && resultCode == Activity.RESULT_OK && resultIntent != null) {
			Log.i(TAG, "Setting the resultIntent");
			setResult(Activity.RESULT_OK, resultIntent);
		} else {
			Log.i(TAG, "No activity result");
		}
	}

}
