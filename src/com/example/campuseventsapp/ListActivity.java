package com.example.campuseventsapp;
import com.example.campuseventsapp.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;


public class ListActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_list);
		//TODO - create list layout file
		//IDEA - use similar card style used in new material-designed Google Calendar app
		
	}
}
