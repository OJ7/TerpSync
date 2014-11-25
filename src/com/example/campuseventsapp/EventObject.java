package com.example.campuseventsapp;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("EventObject")
public class EventObject extends ParseObject {

	public EventObject() {

	}

	public void setOrgName(String OrgName) {
		put("OrganizationName", OrgName);
	}

	public void setEventName(String EventName) {
		put("EventName", EventName);
	}

	public void setBuildingName(String BuildName) {
		put("BuildingName", BuildName);
	}

	public void setDescription(String desc) {
		put("EventDescription", desc);
	}

	public void setStartDate(String date) {
		put("StartDate", date);
	}

	public void setEndDate(String date) {
		put("EndDate", date);
	}

	public void setEndTime(String time) {
		put("EndTime", time);
	}

	public void setStartTime(String time) {
		put("StartTime", time);
	}

	// this will be "FREE" if free, or a string representation of cost if paid
	public void setAdmission(String cost) {
		put("Admission", cost);
	}

	public String getAdmission() {
		return getString("Admission");
	}

	public String getOrgName() {
		return getString("OrganizatioName");
	}

	public String getEventName() {
		return getString("EventName");
	}

	public String getBuildingName() {
		return getString("BuildingName");
	}

	public String getDescription() {
		return getString("EventDescription");
	}

	public String getStartDate() {
		return getString("StartDate");
	}

	public String getEndDate() {
		return getString("EndDate");
	}

	public String getStartTime() {
		return getString("StartTime");
	}

	public String getEndTime() {
		return getString("EndTime");
	}

}