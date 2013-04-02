/**
 * 
 */
package com.cpsc310.treespotter.shared;

/**
 * @author maple-quadtree
 *
 */
public class DistanceToPointProvider {
	LatLong ll;
	public static double R_EARTH = 6371*1000.0;
	double latFactor;
	public DistanceToPointProvider(LatLongProvider llp){
		if (llp == null){
			throw new RuntimeException("Null Latlong provider...");
		}
		ll = llp.getLatLong();
		if(ll == null){
			throw new RuntimeException("Null Latlong is not valid...");
		}
		if(ll.getLatitude() != ll.getLatitude() || ll.getLongitude() != ll.getLongitude()){
			throw new RuntimeException("Can't compare against a NaN...");
		}
		latFactor = Math.cos(ll.getLatitudeRadians());
	}
	public DistanceToPointProvider(LatLong ll){
		if(ll == null){
			throw new RuntimeException("Null Latlong is not valid...");
		}
		if(ll.getLatitude() != ll.getLatitude() || ll.getLongitude() != ll.getLongitude()){
			throw new RuntimeException("Can't compare against a NaN...");
		}
		this.ll = ll;
		latFactor = Math.cos(ll.getLatitudeRadians());
	}
	public double getDistanceSq(LatLongProvider llp){
		LatLong lat_long = llp.getLatLong();
		if(lat_long.getLatitude() != lat_long.getLatitude() || lat_long.getLongitude() != lat_long.getLongitude()){
			throw new RuntimeException("Can't compare against a NaN...");
		}
		double east_west = (lat_long.getLongitudeRadians()-ll.getLongitudeRadians()) *latFactor*R_EARTH; 
		double north_south = (lat_long.getLatitudeRadians()-ll.getLatitudeRadians()) *R_EARTH;
		return east_west*east_west + north_south*north_south;
	}
	public double getDistance(LatLongProvider llp){
		return Math.sqrt(getDistanceSq(llp));
	}
}
