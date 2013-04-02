package com.cpsc310.treespotter.shared;

public class LatLongRange extends LatLong {
	private static final long serialVersionUID = 1L;
	public LatLongRange(String latLongString){
		String number_regex = "-?\\d+(\\.\\d*)?";
		String comma_regex = "[ ]*,[ ]*";
		latLongString = latLongString.trim();
		if(latLongString.matches(number_regex + comma_regex + number_regex + comma_regex + number_regex)){
			try{
				int firstCommaLocation = latLongString.indexOf(',');
				int lastCommaLocation =latLongString.lastIndexOf(',');
				latitude = Double.parseDouble(latLongString.substring(0, firstCommaLocation));
				longitude = Double.parseDouble(latLongString.substring(firstCommaLocation+1, lastCommaLocation));
				range = Double.parseDouble(latLongString.substring(lastCommaLocation+1));
			}
			catch(Exception e){
				throw new RuntimeException("Error parsing lat long string \"" + latLongString +"\"\n\t"+ e.getMessage(),e);
			}
		}
		else{
			throw new RuntimeException("Error parsing lat long string \"" + latLongString +"\"\n\tno regex match, wrong format?");
		}
	}
	public double getRange() {
		return range;
	}
	public void setRange(double range) {
		this.range = range;
	}
	private double range;
}
