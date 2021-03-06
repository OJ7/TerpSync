package com.terpsync.card;

import java.util.ArrayList;
import java.util.List;
import com.terpsync.R;
import com.terpsync.UtilityMethods;
import com.terpsync.parse.EventObject;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CardListAdapter extends ArrayAdapter<EventObject> implements Filterable {

	private static final String TAG = "CardListAdapter";
	private Context mContext;
	private final Object lock = new Object();
	private ArrayList<EventObject> mOriginalEvents;
	private static UtilityMethods utils = new UtilityMethods();

	List<EventObject> mEventsList;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param resourceId
	 * @param item
	 */
	public CardListAdapter(Context context, int resourceId, List<EventObject> item) {
		super(context, resourceId, item);
		this.mContext = context;
		this.mEventsList = (ArrayList<EventObject>) item;
		if (mOriginalEvents == null) {
			synchronized (lock) {
				mOriginalEvents = new ArrayList<EventObject>(mEventsList);
			}
		}
	}

	/**
	 * View holder pattern for list adapter
	 */
	public class ViewHolder {
		TextView event_title;
		TextView organization;
		TextView location;
		TextView date;
		TextView time;
		RelativeLayout card;
		ImageView cardBackGround;
	}

	/**
	 * Updates and displays information on the card associated with the event
	 */
	@SuppressLint("InflateParams")
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh; // ViewHolder object
		// Gets the tag for the reusable View if available. Otherwise, sets up the ViewHolder.
		if (convertView == null) { // first time setting up the layout
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			vh = new ViewHolder();
			convertView = inflater.inflate(R.layout.card, null);
			vh.card = (RelativeLayout) convertView.findViewById(R.id.card);
			vh.event_title = (TextView) convertView.findViewById(R.id.card_event_title);
			vh.organization = (TextView) convertView.findViewById(R.id.card_organization);
			vh.location = (TextView) convertView.findViewById(R.id.card_location);
			vh.date = (TextView) convertView.findViewById(R.id.card_date);
			vh.time = (TextView) convertView.findViewById(R.id.card_time);
			vh.cardBackGround = (ImageView) convertView.findViewById(R.id.cardBackground);
			convertView.setTag(vh);
		} else { // view already exists, just get the tag
			vh = (ViewHolder) convertView.getTag();
		}

		EventObject currCard = mEventsList.get(position); // Get current card

		// Set the title, organization, and location
		vh.event_title.setText(currCard.getEventName());
		vh.organization.setText(currCard.getOrgName());
		vh.location.setText(currCard.getBuildingName());
		// Get background image
		Drawable mapImage = utils.getBitMapImage(mContext, currCard.getBuildingName());
		// Set background image for card
		vh.cardBackGround.setBackground(mapImage);
		// Set date
		if (currCard.getStartDate().equals(currCard.getEndDate())) {
			vh.date.setText(currCard.getEndDate());
		} else {
			vh.date.setText(currCard.getStartDate() + " - " + currCard.getEndDate());
		}
		// Set time
		vh.time.setText(currCard.getStartTime() + " - " + currCard.getEndTime());

		// TODO - Animation is coming soon

		return convertView;
	}

	@Override
	public int getCount() {
		return this.mEventsList.size();
	};

	@Override
	public EventObject getItem(final int position) {
		return this.mEventsList.get(position);
	}

	@Override
	public Filter getFilter() {
		Filter myFilter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();

				if (mOriginalEvents == null) {
					synchronized (lock) {
						mOriginalEvents = new ArrayList<EventObject>(mEventsList);
					}
				}

				if (constraint == null || constraint.length() == 0) {
					synchronized (lock) {
						ArrayList<EventObject> list = new ArrayList<EventObject>(mOriginalEvents);
						filterResults.values = list;
						filterResults.count = list.size();
					}
				} else {
					List<EventObject> filteredEvents = new ArrayList<EventObject>();
					int i = Integer.parseInt(constraint.toString().substring(0, 1));
					String filterName = constraint.subSequence(2, constraint.length()).toString();
					int numEvents = 0;
					for (EventObject eventObject : mEventsList) {
						switch (i) {
							case 0: // filter by building
								// Log.i(TAG, "Filtering by Building: " + filterName);
								if (eventObject.getBuildingName().equals(filterName)) {
									filteredEvents.add(eventObject);
									Log.i(TAG, "Number of events added: " + ++numEvents);
								}
								break;
							case 1: // filter by organization
								// Log.i(TAG, "Filtering by Organization: " + filterName);
								if (eventObject.getOrgName().equals(filterName)) {
									filteredEvents.add(eventObject);
									Log.i(TAG, "Number of events added: " + ++numEvents);
								}
								break;
							case 2: // filter by free
								// Log.i(TAG, "Filtering by Free Events");
								Log.i(TAG, "Event Cost: " + eventObject.getAdmission());
								if (eventObject.getAdmission().equals(filterName)) {
									filteredEvents.add(eventObject);
									Log.i(TAG, "Number of events added: " + ++numEvents);
								}
								break;
							case 3: // filter by paid
								Log.i(TAG, "Event Cost: " + eventObject.getAdmission());
								if (!(eventObject.getAdmission().equals(filterName))) {
									filteredEvents.add(eventObject);
									Log.i(TAG, "Number of events added: " + ++numEvents);
								}
								break;
							default:
								break;
						}

					}
					// Now assign the values and count to the FilterResults object
					filterResults.values = filteredEvents;
					filterResults.count = filteredEvents.size();
				}
				return filterResults;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence contraint, FilterResults results) {
				mEventsList = (List<EventObject>) results.values;

				if (results != null && results.count > 0) {
					notifyDataSetChanged();
					Log.i(TAG, "Called notifyDataSetChanged()");
				} else {
					notifyDataSetInvalidated();
					Log.i(TAG, "Called notifyDataSetInvalidated()");
				}
			}
		};
		return myFilter;
	}

	public ArrayList<String> getValuesFromFields(String field) {
		ArrayList<String> list = new ArrayList<String>();
		for (EventObject x : mEventsList) {
			if (field.equals("building")) {
				if (!list.contains(x.getBuildingName())) {
					list.add(x.getBuildingName());
				}
			} else { // field.equals("organization")
				if (!list.contains(x.getOrgName())) {
					list.add(x.getOrgName());
				}
			}
		}
		return list;
	}

	/**
	 * Replaces the current list of events with the one specified. Useful when filtering events.
	 * 
	 * @param newList
	 *            the list of events to replace with
	 */
	public void updateData(List<EventObject> newList) {
		this.mEventsList = (ArrayList<EventObject>) newList;
		notifyDataSetChanged();
	}

	/**
	 * Resets the mEventsList to restore all original events from mOriginalEvents
	 */
	public void resetData() {
		mEventsList.clear();
		mEventsList.addAll(mOriginalEvents);
		notifyDataSetChanged();
	}

	/**
	 * Replaces the original list of events with the one specified. Useful when events were
	 * modified.
	 * 
	 * @param newList
	 *            the list of events to replace with
	 */
	public void replaceData(List<EventObject> newList) {
		this.mOriginalEvents = (ArrayList<EventObject>) newList;
		notifyDataSetChanged();
	}
}
