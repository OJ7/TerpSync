package com.example.campuseventsapp;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


public class ListActivity extends Activity {
	
	private static final String TAG = "ListActivity";
	private FloatingActionButton fabButton, item1, item2, item3;
	private int toggle = 0; // 0 = hidden, 1 = shown
	private ArrayList<EventObject> events = new ArrayList<EventObject>();
	private ListView list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_list);
		//TODO - create list layout file
		//IDEA - use similar card style used in new material-designed Google Calendar app
		setupFAB();
		
		
		CustomList adapter = new CustomList(ListActivity.this, events);
		list = (ListView) findViewById(R.id.eventsList);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(ListActivity.this, "You Clicked at " + position, Toast.LENGTH_SHORT).show();
            }
        });
		
		
	} // end of onCreate
	
	
	
	/**
	 * Sets up the Floating Action Button the Map Screen
	 */
	private void setupFAB() {
		fabButton = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_action_star))
				.withButtonColor(Color.RED).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 16).create();
		fabButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO (minor) - implement material design animations
				if (toggle == 0) {
					toggle = 1;
					showFABMenu();
				} else {
					toggle = 0;
					hideFABMenu();
				}

			}
		});
	}

	private void hideFABMenu() {
		item1.hideFloatingActionButton();
		item2.hideFloatingActionButton();
		item3.hideFloatingActionButton();
	}

	private void showFABMenu() {

		showItem1();
		showItem2();
		showItem3();
	}

	private void showItem1() {
		item1 = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_launcher))
				.withButtonColor(Color.BLUE).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 86).create();
		item1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "clicked item 1", Toast.LENGTH_SHORT)
						.show();

				Intent intent = new Intent(ListActivity.this, AddEventActivity.class);
				startActivityForResult(intent, 0);
			}

		});
	}

	private void showItem2() {
		item2 = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_launcher))
				.withButtonColor(Color.GREEN).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 156).create();
		item2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "clicked item 2", Toast.LENGTH_SHORT)
						.show();
				Intent intent = new Intent(ListActivity.this, MainActivity.class);
				startActivity(intent);

			}

		});
	}

	private void showItem3() {
		item3 = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_launcher))
				.withButtonColor(Color.GRAY).withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 226).create();
		item3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "clicked item 3", Toast.LENGTH_SHORT)
						.show();
			}

		});
	}

	
}
