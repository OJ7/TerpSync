package com.example.campuseventsapp;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.Toast;


public class ListActivity extends Activity {
	
	private static final String TAG = "ListActivity";
	private FloatingActionButton fabButton, item1, item2, item3;
	private int toggle = 0; // 0 = hidden, 1 = shown
	
	//List of cards
	private List<Card> cardList;
	CardListAdapter mAdapter;
	private List<String> cardTitles = new ArrayList<String>();
	private List<String> cardDescriptions = new ArrayList<String>();
	private List<Integer> images = new ArrayList<Integer>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_list);
		setupFAB();
		//TODO - create list layout file
		
	
		
		//IDEA - use similar card style used in new material-designed Google Calendar app

		//Setting up list view
		ListView lv = (ListView)findViewById(R.id.myList);
		cardList = new ArrayList<Card>();
		
		//TODO - Arrays below should be filled with card titles and descriptions
		//I am just inserting mock data
		cardTitles.add("Card1");
		cardTitles.add("Card2");
		cardTitles.add("Card3");
		
		cardDescriptions.add("This is card1");
		cardDescriptions.add("This is card2");
		cardDescriptions.add("This is card3");
		
		images.add(R.drawable.ic_action_star);
		images.add(R.drawable.ic_action_star);
		images.add(R.drawable.ic_action_star);
		
		//TODO - Populate the cardList
		populateCardList();
		
		//Set the adapter on the listView
		mAdapter = new CardListAdapter(getApplicationContext(), R.layout.card,cardList);
		lv.setAdapter(mAdapter);
		
		//Listener for the cards
		lv.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), "Selected", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
	} // end of onCreate
	
	/*
	 * Populate Card List
	 */
	private void populateCardList(){
		for(int i =0; i < cardTitles.size(); i++){
			Card currCard = new Card(images.get(i), cardTitles.get(i), cardDescriptions.get(i));
			cardList.add(currCard);
		}
	}
	
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

	//Add event intent
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

	//This FAB goes back to main activity
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

	//Not yet implemented
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
