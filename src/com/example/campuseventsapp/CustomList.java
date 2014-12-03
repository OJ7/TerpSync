package com.example.campuseventsapp;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomList extends ArrayAdapter<String> {
	private final Activity context;
	private final ArrayList<EventObject> events;

	public CustomList(Activity context, ArrayList<EventObject> events) {
		super(context, R.layout.list_item);
		this.context = context;
		this.events = events;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.list_item, null, true);
		TextView eventName = (TextView) rowView.findViewById(R.id.listEventName), eventDate = (TextView) rowView
				.findViewById(R.id.listEventDate), eventTime = (TextView) rowView
				.findViewById(R.id.listEventTime), eventOrgName = (TextView) rowView
				.findViewById(R.id.listEventOrgName);
		eventName.setText("Event Name");
		eventDate.setText("Event Date");
		eventTime.setText("Event Time");
		eventOrgName.setText("Student Organization Name");

		return rowView;
	}
}