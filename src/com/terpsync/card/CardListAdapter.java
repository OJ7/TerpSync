package com.terpsync.card;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.terpsync.R;
import com.terpsync.parse.EventObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CardListAdapter extends ArrayAdapter<EventObject> {

	Context mContext;
	ArrayList<EventObject> list;

	// Constructor
	public CardListAdapter(Context context, int resourceId, List<EventObject> item) {
		super(context, resourceId, item);
		this.mContext = context;
		this.list = (ArrayList<EventObject>) item;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh = setupViewHolder(convertView); // ViewHolder object
		EventObject currCard = getItem(position); // Get current card

		// Set the title, organization, and location
		vh.event_title.setText(currCard.getEventName());
		vh.organization.setText(currCard.getOrgName());
		vh.location.setText(currCard.getBuildingName());
		// Get background image
		String resourceLocation = getBitMapLocation(currCard.getBuildingName());
		Drawable mapImage = mContext.getResources().getDrawable(
				mContext.getResources().getIdentifier(resourceLocation, "drawable",
						mContext.getPackageName()));
		// Set background image for card
		if (mapImage == null) { // set to default picture
			vh.cardBackGround.setBackground(mContext.getResources().getDrawable(
					R.drawable.adele_h_stamp_student_union_building));
		} else {
			vh.cardBackGround.setBackground(mapImage);
		}
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

	/**
	 * Gets the tag for the reusable View if available. Otherwise, sets up the ViewHolder.
	 * 
	 * @param convertView
	 *            The old view to reuse, if possible.
	 * @return either a new ViewHolder or the one from the reusable View.
	 */
	private ViewHolder setupViewHolder(View convertView) {
		ViewHolder vh;
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) { // first time setting up the layout
			convertView = inflater.inflate(R.layout.card, null);
			vh = new ViewHolder();
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
		return vh;
	}

	/**
	 * Takes in a building name and returns the corresponding resource name for the png file
	 */
	private String getBitMapLocation(String buildingName) {
		buildingName = buildingName.toLowerCase();
		buildingName = buildingName.replaceAll("[.]", "");
		buildingName = buildingName.replaceAll("\\s+", " ");
		buildingName = buildingName.replaceAll("[^a-z0-9\\s]", " ");
		buildingName = buildingName.replaceAll("\\s", "_");
		return buildingName;
	}
}
