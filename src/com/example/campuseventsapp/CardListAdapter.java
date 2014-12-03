package com.example.campuseventsapp;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CardListAdapter extends ArrayAdapter<Card>{
	
	//Context
	Context mContext;
	
	//Constructor
	public CardListAdapter(Context context, int resourceId, List<Card> item){
		super(context, resourceId, item);
		this.mContext = context;
	}
	
	//View holder pattern for list adapter
	public class ViewHolder{
		ImageView imageView;
		TextView cardTitle;
		TextView cardDescription;
		LinearLayout card;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		//ViewHolder object
		ViewHolder vh;
		//Get current card
		Card currCard = getItem(position);
		
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if(convertView == null){
			//This is first time setting up the layout
			convertView = inflater.inflate(R.layout.card, null);
			vh = new ViewHolder();
			vh.card = (LinearLayout) convertView.findViewById(R.id.card);
			vh.imageView = (ImageView)convertView.findViewById(R.id.card_image);
			vh.cardTitle = (TextView)convertView.findViewById(R.id.card_title);
			vh.cardDescription = (TextView)convertView.findViewById(R.id.card_description);
			convertView.setTag(vh);
			
		}else{
			//View already exists, just get the tag
			vh = (ViewHolder)convertView.getTag();
		}
		//Update the image, title, description
		vh.imageView.setImageResource(currCard.getImageId());
		vh.cardTitle.setText(currCard.getTitle());
		vh.cardDescription.setText(currCard.getDescription());
		
		//Animation is coming soon
		
		return convertView;
		
	}

}
