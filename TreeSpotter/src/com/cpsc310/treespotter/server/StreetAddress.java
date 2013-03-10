package com.cpsc310.treespotter.server;


class StreetAddress implements LatLongProvider, Comparable<StreetAddress>
{
	
	private LatLong latLong;
	Integer civicNumber;
	
	StreetAddress(int civicNumber, LatLong ll){
		this.civicNumber = civicNumber;
		latLong = ll;
	}
	
	StreetAddress(int civicNumber){
		this.civicNumber = civicNumber;
	}
	
	StreetAddress(int civicNumber, double latcoord, double longcoord){
		this.civicNumber = civicNumber;
		latLong = new LatLong(latcoord, longcoord);
	}
	
	public static LatLong lerpLatLong(StreetAddress a1, StreetAddress a2, int civicNumber){
		double num_diff = a2.civicNumber - a1.civicNumber;
		double factor2 = (civicNumber - a1.civicNumber)/num_diff;
		double factor1 = 1.0-factor2;
		LatLong ll1 = a1.getLatLong();
		LatLong ll2 = a2.getLatLong();
		return new LatLong(ll1.getLatitude()*factor1 + ll2.getLatitude()*factor2, ll1.getLongitude()*factor1 + ll2.getLongitude()*factor2 );
	}
	
	@Override
	public int compareTo(StreetAddress o) {
		return civicNumber.compareTo(o.civicNumber);
	}
	public void setLatLong(LatLong latLong) {
		this.latLong = latLong;
	}

	@Override
	public LatLong getLatLong() {
		return latLong;
	}
}