package com.example.campuseventsapp;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("UMDBuildings")
public class UMDBuildings extends ParseObject {

	public UMDBuildings() {

	}

	public String getName() {
		return getString("name");
	}

	public String getBuildAbrev() {
		return getString("code");
	}

	public String getBuildNum() {
		return getString("Number");
	}

	public String getLat() {
		return getString("latitude");
	}

	public String getLng() {
		return getString("longitude");
	}
}
