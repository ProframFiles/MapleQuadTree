package com.cpsc310.treespotter.client;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.maps.client.geom.LatLng;

public class ParseUtils {
	
	/**
	 * Validates LatLng coordinates
	 * 
	 * @param c
	 *            LatLng to verify
	 * @return false if invalid (contains NaN), true if valid
	 */
	public static boolean validCoordinates(LatLng c) {
		if (Double.isNaN(c.getLatitude()) || Double.isNaN(c.getLongitude()))
			return false;
		return true;
	}

	public static String capitalize(String str, boolean species) {
		String cap = str.toLowerCase();
		if (!species) {
			String[] words = cap.split("\\s+");
			cap = "";
			for (String w : words) {
				String first = w.substring(0, 1);
				String rest = w.substring(1);
				cap += first.toUpperCase() + rest + " ";
			}
		} else {
			String first = cap.substring(0, 1);
			String rest = cap.substring(1);
			cap = first.toUpperCase() + rest + " ";
		}
		return cap;
	}
	
	/**
	 * 
	 * formats:
	 * (%d-%d %s)
	 * (%d %s)
	 * (%f,%f,%f)
	 * 
	 * @param input string to be formatted
	 * @return
	 * @throws InvalidFieldException 
	 * 				if input string is not in an accepted format
	 */
	public static String formatSearchLocation(String input) 
		throws InvalidFieldException {
		String str = "";
		str = input.trim();
		//remove extra whitespaces
		str = str.replaceAll("\\s+", " ");
		// %f,%f,%f
		if (str.matches("-?\\s*\\d+(\\.\\d+)?\\s*,\\s*-?\\s*\\d+(\\.\\d+)?\\s*,\\s*-?\\s*\\d+(\\.\\d+)?")) {
			// remove all whitespace
			str = str.replaceAll("\\s", "");
		}
		// (%d-%d %s)
		else if (str.matches("\\d+\\s*-\\s*\\d+\\s+.+")) {
			String[] words = input.split("-", 2);
			str = words[0].trim() + "-" + words[1].trim();
		}
		// (%d %s)
		else if (str.matches("\\d+\\s+.+")) {
			// no change needed, extra whitespace already removed
		}
		// invalid
		else {
			throw new InvalidFieldException("Invalid location search format");
		}
		
		return str;
	}
	
	/**
	 * Check if string input is a LOCATION or ADDRESS search
	 * 
	 * @pre string is properly formatted (use formatSearchLocation)
	 * @param str
	 * @return True if LOCATION search, false otherwise
	 */
	public static boolean isLocationSearch(String str) {
		str = str.trim();
		//remove extra whitespaces
		str = str.replaceAll("\\s+", " ");
		// not %f,%f,%f
		if (str.matches("-?\\s*\\d+(\\.\\d+)?\\s*,\\s*-?\\s*\\d+(\\.\\d+)?\\s*,\\s*-?\\s*\\d+(\\.\\d+)?")) {
			return true;
		}
		return false;
	}
	
	public static String formatDate(String str) 
		throws InvalidFieldException {
		try {
			DateTimeFormat dtf = DateTimeFormat.getFormat("dd MMM yyyy");
			str = dtf.format(dtf.parse(str));
			return str;
		} catch (Exception e) {
			throw new InvalidFieldException("Invalid field: Date");
		}
	}
	
	public static int getHeightRange(String str) 
		throws Exception {
		try {
			int h = (int) Double.parseDouble(str); // just in case it's a float
			int range = -1;
			if (h < 0) {
				range = 0;
			} else if (h < 100) {
				range = h / 10;
			} else {
				range = 10;
			}
			return range;
		} catch (Exception e) {
			throw e;
		}
	}

}
