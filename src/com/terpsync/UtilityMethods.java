package com.terpsync;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

public class UtilityMethods {

	/**
	 * Takes in a building name and returns the corresponding resource png file. Defaults to Stamp
	 * image if image now found.
	 */
	@SuppressLint("DefaultLocale")
	public Drawable getBitMapImage(Context mContext, String buildingName) {
		buildingName = buildingName.toLowerCase();
		buildingName = buildingName.replaceAll("\\(.*\\)", "").trim();
		buildingName = buildingName.replaceAll("[.]", "");
		buildingName = buildingName.replaceAll("\\s+", " ");
		buildingName = buildingName.replaceAll("[^a-z0-9\\s]", " ");
		buildingName = buildingName.replaceAll("\\s", "_");
		String resourceLocation = buildingName;
		Drawable mapImage = null;
		try {
			mapImage = mContext.getResources().getDrawable(
					mContext.getResources().getIdentifier(resourceLocation, "drawable",
							mContext.getPackageName()));
		} catch (Exception e) {
			// TODO: handle exception
			mapImage = null;
		}
		if (mapImage == null) { // If image not found, default to Stamp
			mapImage = mContext.getResources().getDrawable(
					R.drawable.adele_h_stamp_student_union_building);
		}
		return mapImage;
	}
}
