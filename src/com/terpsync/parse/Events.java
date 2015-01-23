package com.terpsync.parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Events")
public class Events extends ParseObject {

	public Events() {

	}

	public String getOrgName() {
		return getString("OrganizationName");
	}

	public void setOrgName(String orgName) {
		put("OrganizationName", orgName);
	}

	public String getEventName() {
		return getString("EventName");
	}

	public void setEventName(String eventName) {
		put("EventName", eventName);
	}

	public String getStartDate() {
		return getString("StartDate");
	}

	public void setStartDate(String date) {
		put("StartDate", date);
	}

	public String getStartTime() {
		return getString("StartTime");
	}

	public void setStartTime(String time) {
		put("StartTime", time);
	}

	public String getEndDate() {
		return getString("EndDate");
	}

	public void setEndDate(String date) {
		put("EndDate", date);
	}

	public String getEndTime() {
		return getString("EndTime");
	}

	public void setEndTime(String time) {
		put("EndTime", time);
	}

	public String getBuildingName() {
		return getString("BuildingName");
	}

	public void setBuildingName(String buildingName) {
		put("BuildingName", buildingName);
	}

	public String getDescription() {
		return getString("EventDescription");
	}

	public void setDescription(String desc) {
		put("EventDescription", desc);
	}

	public String getAdmission() {
		return getString("Admission");
	}

	// this will be "FREE" if free, or a string representation of cost if paid
	public void setAdmission(String cost) {
		put("Admission", cost);
	}

}