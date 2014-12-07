package com.example.campuseventsapp.card;

import java.util.List;
import java.util.regex.Pattern;

import com.example.campuseventsapp.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CardListAdapter extends ArrayAdapter<EventObject>{
	
	//Context
	Context mContext;
	
	//Constructor
	public CardListAdapter(Context context, int resourceId, List<EventObject> item){
		super(context, resourceId, item);
		this.mContext = context;
	}
	
	//View holder pattern for list adapter
	public class ViewHolder{
		TextView event_title;
		TextView organization;
		TextView location;
		TextView date;
		TextView time;
		LinearLayout card;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		//ViewHolder object
		ViewHolder vh;
		//Get current card
		EventObject currCard = getItem(position);
		
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if(convertView == null){
			//This is first time setting up the layout
			convertView = inflater.inflate(R.layout.card, null);
			vh = new ViewHolder();
			vh.card = (LinearLayout) convertView.findViewById(R.id.card);
			vh.event_title = (TextView)convertView.findViewById(R.id.card_event_title);
			vh.organization = (TextView)convertView.findViewById(R.id.card_organization);
			vh.location = (TextView)convertView.findViewById(R.id.card_location);
			vh.date = (TextView)convertView.findViewById(R.id.card_date);
			vh.time = (TextView)convertView.findViewById(R.id.card_time);
						
			convertView.setTag(vh);
			
		}else{
			//View already exists, just get the tag
			vh = (ViewHolder)convertView.getTag();
		}
		
		//Update the image, title, description
		vh.event_title.setText(currCard.getEventName());
		vh.organization.setText(currCard.getOrgName());
		vh.location.setText(currCard.getBuildingName());
		
		// set background image
		String resourceLocation = getBitMapLocation(currCard.getBuildingName());
		
		// TODO: YUSIK DO WORK HERE
		
		// display date
		if (currCard.getStartDate().equals(currCard.getEndDate())){
			vh.date.setText(currCard.getEndDate());
		}
		else{
			vh.date.setText(currCard.getStartDate() + " - " + currCard.getEndDate());
		}
		
		//display time 
		vh.time.setText(currCard.getStartTime() + " - " + currCard.getEndTime());
		
		//Animation is coming soon
		
		return convertView;
		
	}

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
