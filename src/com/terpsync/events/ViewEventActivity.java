package com.terpsync.events;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.terpsync.R;
import com.terpsync.UtilityMethods;
import com.terpsync.parse.EventObject;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewEventActivity extends Activity {

	private static final String TAG = "ViewEventActivity";
	private static UtilityMethods utils = new UtilityMethods();

	ImageView mEventImage;
	TextView mEventName, mOrgName, mEventDate, mEventTime, mLocation, mAdmission;
	EventObject mEvent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_event);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment())
					.commit();
		}

		cacheWidgets();
		getEvent();
		loadEventData();

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_view_event, container, false);
			return rootView;
		}
	}

	/**
	 * TODO - add doc
	 */
	private void cacheWidgets() {
		Log.i(TAG, "Caching widgets");
		mEventImage = (ImageView) findViewById(R.id.event_image);
		mEventName = (TextView) findViewById(R.id.event_name);
		mOrgName = (TextView) findViewById(R.id.organization_name);
		mEventDate = (TextView) findViewById(R.id.event_date);
		mEventTime = (TextView) findViewById(R.id.event_time);
		mLocation = (TextView) findViewById(R.id.building_name);
		mAdmission = (TextView) findViewById(R.id.event_admission);
	}

	/**
	 * TODO - add doc
	 */
	private void getEvent() {
		Log.i(TAG, "Getting event");
		Intent intent = getIntent();
		String objectID = intent.getStringExtra("objectID");
		try {
			ParseQuery<EventObject> eventsQuery = ParseQuery.getQuery(EventObject.class);
			mEvent = eventsQuery.get(objectID);
			Log.i(TAG, "Found event with objectID: " + objectID);
		} catch (ParseException e1) {
			Log.i(TAG, "Failed to find event with objectID: " + objectID);
			mEvent = null;
			e1.printStackTrace();
		}
	}

	/**
	 * TODO - add doc
	 */
	private void loadEventData() {
		Log.i(TAG, "Loading event data");

		try {
			// Get background image
			Drawable mapImage = utils.getBitMapImage(getApplicationContext(),
					mEvent.getBuildingName());
			// Fill info in views
			mEventImage.setImageDrawable(mapImage);
			mEventName.setText(mEvent.getEventName());
			mOrgName.setText(mEvent.getOrgName());
			if (mEvent.getStartDate().equals(mEvent.getEndDate())) {
				mEventDate.setText(mEvent.getEndDate());
			} else {
				mEventDate.setText(mEvent.getStartDate() + " - " + mEvent.getEndDate());
			}
			// Set time
			mEventTime.setText(mEvent.getStartTime() + " - " + mEvent.getEndTime());
			mLocation.setText(mEvent.getBuildingName());
			if (mEvent.getAdmission().equals("FREE")) {
				mAdmission.setText("Free");
			} else {
				mAdmission.setText("$" + mEvent.getAdmission());
			}
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(getApplicationContext(), "Failed to load event information",
					Toast.LENGTH_SHORT).show();
			finish();
		}

	}
}
