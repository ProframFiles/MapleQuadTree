package com.cpsc310.treespotter.server;
/**
 * @author maple-quadtree
 *
 */
public class LatLong {
	LatLong(){
		latitude = 0.0;
		longitude = 0.0;
	}
	
	LatLong(double latcoord, double longcoord){
		latitude = latcoord;
		longitude = longcoord;
	}
	
	/**
	 * @return the latitude in degrees
	 */
	public double getLatitude() {
		return latitude;
	}
	/**
	 * @param latitude the latitude to set in degrees
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	/**
	 * @return the longitude in degrees
	 */
	public double getLongitude() {
		return longitude;
	}
	/**
	 * @param longitude the longitude to set, in degrees
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitudeRadians(){
		return Math.toRadians(latitude);
	}
	public double getLongitudeRadians(){
		return Math.toRadians(longitude);
	}
	
	
	private double latitude;
	private double longitude;
	
}
