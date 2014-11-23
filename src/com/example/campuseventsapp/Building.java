package com.example.campuseventsapp;


public class Building {
	private String code, name;
	private int buildingID, numEvents;
	private double lat, lng;

	protected Building(String code, String name, int buildingID, double lat, double lng) {
		super();
		this.code = code;
		this.name = name;
		this.buildingID = buildingID;
		this.lat = lat;
		this.lng = lng;
		this.numEvents = 5; // just a random number for now
	}

	
	public String getCode() {
		return code;
	}
	
	public String getName() {
		return name;
	}
	
	public int getNumEvents() {
		return numEvents;
	}
	
	
	public double getLat() {
		return lat;
	}

	public double getLng() {
		return lng;
	}
	
	public String toString() {
		return "(" + code + ") " + name;
	}

	
}
