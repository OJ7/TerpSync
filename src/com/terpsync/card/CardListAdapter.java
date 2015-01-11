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
	 * TODO - Add documentation
	 */
	// View holder pattern for list adapter
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
	 * TODO - Add documentation
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		// ViewHolder object
		ViewHolder vh;
		// Get current card
		EventObject currCard = getItem(position);

		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			// This is first time setting up the layout
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

		} else {
			// View already exists, just get the tag
			vh = (ViewHolder) convertView.getTag();
		}

		// Update the image, title, description
		vh.event_title.setText(currCard.getEventName());
		vh.organization.setText(currCard.getOrgName());
		vh.location.setText(currCard.getBuildingName());

		// set background image
		String resourceLocation = getBitMapLocation(currCard.getBuildingName());
		// System.out.println(resourceLocation);

		Drawable mapImage = mContext.getResources().getDrawable(
				mContext.getResources().getIdentifier(resourceLocation, "drawable",
						mContext.getPackageName()));

		if (mapImage == null) {
			// Set to default picture
			vh.cardBackGround.setBackground(mContext.getResources().getDrawable(
					R.drawable.adele_h_stamp_student_union_building));
			// vh.card.setBackground(mContext.getResources().getDrawable(R.drawable.adele_h_stamp_student_union_building));
		} else {
			vh.cardBackGround.setBackground(mapImage);
			// vh.card.setBackground(mContext.getResources().getDrawable(mContext.getResources().getIdentifier(resourceLocation,
			// "drawable", mContext.getPackageName())));
		}

		// display date
		if (currCard.getStartDate().equals(currCard.getEndDate())) {
			vh.date.setText(currCard.getEndDate());
		} else {
			vh.date.setText(currCard.getStartDate() + " - " + currCard.getEndDate());
		}

		// display time
		vh.time.setText(currCard.getStartTime() + " - " + currCard.getEndTime());

		// Animation is coming soon

		return convertView;

	}

	/**
	 * TODO - Add documentation
	 */
	// this will take in the building name and return the corresponding png file associated
	private String getBitMapLocation(String buildingName) {

		buildingName = buildingName.toLowerCase();
		buildingName = buildingName.replaceAll("[.]", "");
		buildingName = buildingName.replaceAll("\\s+", " ");
		buildingName = buildingName.replaceAll("[^a-z0-9\\s]", " ");
		buildingName = buildingName.replaceAll("\\s", "_");

		return buildingName;
	}

}
