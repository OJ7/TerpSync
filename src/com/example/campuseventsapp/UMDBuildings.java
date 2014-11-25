package com.example.campuseventsapp;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("UMDBuildings")
public class UMDBuildings extends ParseObject {

	private int numEvents;

	public UMDBuildings() {

	}

	public String getLat() {
		return getString("latitude");
	}

	public String getLng() {
		return getString("Latitude");
	}

	public String getBuildNum() {
		return getString("Number");
	}

	public String getBuildAbrev() {
		return getString("code");
	}

	public String getName() {
		return getString("name");
	}

	/**
	 * @return the number of events happening in that building during the
	 *         current day
	 */
	public int getNumEvents() {
		return numEvents;
	}

	/**
	 * Returns string in form of: "(BuildingCode) BuildingName"
	 */
	public String toString() {
		return "(" + getBuildAbrev() + ") " + getName();
	}

}
