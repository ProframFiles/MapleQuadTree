/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.util.Comparator;

/**
 * @author maple-quadtree
 * compares a StreetBlock instances on the basis of how far away they are from a 
 * given lat/long point.<br>
 * Makes the assumption that the earth is locally flat, as in: we don't calculate
 * the great circle distance, but the 2D Euclidean distance.
 */
public class StreetBlockDistanceComparator implements Comparator<StreetBlock> {

	private double baseLatitude;
	private double baseLongitude;
	
	private double longDistanceFactor;
	
	/**
	 * Initializes the comparator with a base point.
	 * @param latitude<br>
	 * Base point latitude in degrees
	 * @param longitude
	 * Base point longitude in degrees
	 */
	StreetBlockDistanceComparator(double latitude, double longitude){
		baseLatitude = Math.toRadians(latitude);
		baseLongitude =  Math.toRadians(longitude);
		longDistanceFactor = Math.cos(baseLatitude);
	}
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(StreetBlock o1, StreetBlock o2) {
		double d1 = sqrDistanceFromBase(o1);
		double d2 = sqrDistanceFromBase(o2);
		return Double.compare(d1, d2);
	}
	
	private double sqrDistanceFromBase(StreetBlock sb){
		double ns = (sb.getLatitudeRadians()-baseLatitude);
		// the higher we are on the globe, the less
		double ew = (sb.getLongitudeRadians()-baseLongitude)*longDistanceFactor;
		return ns*ns+ew*ew;
	}

}
