package com.cpsc310.treespotter.shared;

import java.io.Serializable;

/**
 * @author maple-quadtree
 *
 */
public class LatLong implements Serializable{
	private static final long serialVersionUID = 1L;
	LatLong(){
		latitude = 0.0;
		longitude = 0.0;
	}
	
	public LatLong(String latLongString){
		String number_regex = "-?\\d+(\\.\\d*)?";
		String comma_regex = "[ ]*,[ ]*";
		latLongString = latLongString.trim();
		if(latLongString.matches(number_regex + comma_regex + number_regex + comma_regex + number_regex)){
			try{
				int firstCommaLocation = latLongString.indexOf(',');
				int lastCommaLocation =latLongString.lastIndexOf(',');
				latitude = Double.parseDouble(latLongString.substring(0, firstCommaLocation));
				longitude = Double.parseDouble(latLongString.substring(firstCommaLocation+1, lastCommaLocation));
			}
			catch(Exception e){
				throw new RuntimeException("Error parsing lat long string \"" + latLongString +"\"\n\t"+ e.getMessage(),e);
			}
		}
		else{
			throw new RuntimeException("Error parsing lat long string \"" + latLongString +"\"\n\tno regex match, wrong format?");
		}
	}
	
	public LatLong(double latcoord, double longcoord){
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
