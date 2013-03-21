package com.cpsc310.treespotter.client;

import com.cpsc310.treespotter.shared.ISharedTreeData;
import com.cpsc310.treespotter.shared.LatLong;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.ui.FlexTable;

public class ClientTreeData implements ISharedTreeData {
	/**
	 * 
	 */
	private ISharedTreeData data;
	private Marker mapMarker = null;
	private LatLng ll = null;
	private int index = -1;
	private Marker onPageMarker = null;

	public ClientTreeData(ISharedTreeData data){
		this.data=data;
	}
	
	public String getID(){
		return data.getID();
	}

	public String getLocation(){
		String num = data.getCivicNumber() < 0 ? "" : data.getCivicNumber() + " ";
		return num + data.getStreet();
	}

	public int getCivicNumber() {
		return data.getCivicNumber();
	}

	public String getTreeID() {
		return data.getTreeID();
	}

	public String getNeighbourhood() {
		return data.getNeighbourhood();
	}


	public int getHeightRange() {
		return data.getHeightRange();
	}

	public float getDiameter() {
		return data.getDiameter();
	}

	public String getPlanted() {
		return data.getPlanted();
	}

	public String getCultivar() {
		return data.getCultivar();
	}

	public String getGenus() {
		return data.getGenus();
	}

	public String getSpecies() {
		return data.getSpecies();
	}

	public String getCommonName() {
		return data.getCommonName();
	}

	public LatLong getLatLong() {
		return data.getLatLong();
	}

	@Override
	public String getStreet() {
		return data.getStreet();
	}

	public LatLng getLatLng(){
		if(ll == null){
			ll = LatLng.newInstance(data.getLatLong().getLatitude(),data.getLatLong().getLongitude());
		}
		return ll;
	}

	public Marker getMapMarker() {
		return mapMarker;
	}

	public void setMapMarker(Marker mapMarker) {
		this.mapMarker = mapMarker;
	}

	public Marker getOnPageMapMarker() {
		return onPageMarker;
	}

	public void setOnPageMapMarker(Marker mapMarker) {
		this.onPageMarker = mapMarker;
	}
	
	public boolean dataIsNull() {
		return (data == null);
	}

}
