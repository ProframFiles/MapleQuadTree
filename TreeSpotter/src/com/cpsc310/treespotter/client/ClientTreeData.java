package com.cpsc310.treespotter.client;

import java.util.Date;
import java.io.Serializable;

public class ClientTreeData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1953579997954856450L;
	
	private int civicNumber = -1;
	private String treeID;
	private String neighbourhood;
	private String street;
	private int heightRange = -1;
	private float diameter = -1.0f;
	private String planted;
	private String cultivar;
	private String genus;
	private String species;
	private String commonName;

	public ClientTreeData(){
		
	}
	
	public String getID(){
		return treeID;
	}

	public String getLocation(){
		String num = civicNumber < 0 ? "" : civicNumber + " ";
		return num + street;
	}

	// depends on if we use the geocoded LatLng data
	// TODO (someone) turn this into actual coordinates
/*	public String getCoordinates(){
		return getLocation();
	}*/

	public int getCivicNumber() {
		return civicNumber;
	}

	public void setCivicNumber(int civicNumber) {
		this.civicNumber = civicNumber;
	}

	public String getTreeID() {
		return treeID;
	}

	public void setTreeID(String treeID) {
		this.treeID = treeID;
	}

	public String getNeighbourhood() {
		return neighbourhood;
	}

	public void setNeighbourhood(String neighbourhood) {
		this.neighbourhood = neighbourhood;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public int getHeightRange() {
		return heightRange;
	}

	public void setHeightRange(int heightRange) {
		this.heightRange = heightRange;
	}

	public float getDiameter() {
		return diameter;
	}

	public void setDiameter(float diameter) {
		this.diameter = diameter;
	}

	public String getPlanted() {
		return planted;
	}

	public void setPlanted(String planted) {
		this.planted = planted;
	}

	public String getCultivar() {
		return cultivar;
	}

	public void setCultivar(String cultivar) {
		this.cultivar = cultivar;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}


}
