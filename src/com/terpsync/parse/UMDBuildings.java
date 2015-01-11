package com.terpsync.parse;

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
		return getNumber("number").toString();
	}

	public String getLat() {
		return getNumber("latitude").toString();
	}

	public String getLng() {
		return getNumber("longitude").toString();
	}
}
