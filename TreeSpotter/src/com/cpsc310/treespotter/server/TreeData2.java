/**
 * 
 */
package com.cpsc310.treespotter.server;


/**
 * @author aleksy
 *
 */
public class TreeData2 implements TreeDataProvider, LatLongProvider {

	private String treeID;
	private int civicNumber;
	private String stdStreet;
	private String neighbourhood;
	private int cell;
	private String street;
	private String streetBlock;
	private String streetSideName;
	private Boolean assigned;
	private int heightRange;
	private float diameter;
	private String planted;
	private String plantArea;
	private Boolean rootBarrier;
	private Boolean curb;
	private String cultivar;
	private String genus;
	private String species;
	private String commonName;
	private LatLong latLong;
	
	
	public void setLatLong(LatLong ll){
		latLong = ll;
	}
	
	@Override
	public LatLong getLatLong() {
		return latLong;
	}


	@Override
	public String getID() {
		return treeID;
	}

	private String dataToID(String user, int id_num) {
		String key;
		if (user.equals("admin") || user.toUpperCase().equals("V")) {
			key = "V" + String.valueOf(id_num);
		}
		else if(user.equals("user") || user.toUpperCase().equals("U")){
			key = "U" + String.valueOf(id_num);
		}
		else {
			throw new RuntimeException("Invalid tree ID prefix: " + user);
		}
		return key;
	}
	
	@Override
	public void setID(String user, int id_num) {
		treeID = dataToID(user, id_num);

	}

	@Override
	public int getCivicNumber() {
		return civicNumber;
	}

	@Override
	public void setCivicNumber(int civicNumber) {
		this.civicNumber = civicNumber;
	}

	@Override
	public String getStdStreet() {
		return stdStreet;
	}

	@Override
	public void setStdStreet(String stdStreet) {
		this.stdStreet = stdStreet;
	}

	@Override
	public String getNeighbourhood() {
		return neighbourhood;
	}

	@Override
	public void setNeighbourhood(String neighbourhood) {
		this.neighbourhood = neighbourhood;
	}

	@Override
	public int getCell() {
		return cell;
	}

	@Override
	public void setCell(int cell) {
		this.cell = cell;
	}

	@Override
	public String getStreet() {
		return street;
	}

	@Override
	public void setStreet(String street) {
		this.street = street;
	}

	@Override
	public String getStreetBlock() {
		return streetBlock;
	}

	@Override
	public void setStreetBlock(String streetBlock) {
		this.streetBlock = streetBlock;
	}

	@Override
	public String getStreetSideName() {
		return streetSideName;
	}

	@Override
	public void setStreetSideName(String streetSideName) {
		this.streetSideName = streetSideName;
	}

	@Override
	public Boolean getAssigned() {
		return assigned;
	}

	@Override
	public void setAssigned(Boolean assigned) {
		this.assigned = assigned;
	}

	@Override
	public int getHeightRange() {
		return heightRange;
	}

	@Override
	public void setHeightRange(int heightRange) {
		this.heightRange = heightRange;
	}

	@Override
	public float getDiameter() {
		return diameter;
	}

	@Override
	public void setDiameter(float diameter) {
		this.diameter = diameter;
	}

	@Override
	public String getPlanted() {
		return planted;
	}

	@Override
	public void setPlanted(String planted) {
		this.planted = planted;
	}

	@Override
	public String getPlantArea() {
		return plantArea;
	}

	@Override
	public void setPlantArea(String plantArea) {
		this.plantArea = plantArea;
	}

	@Override
	public Boolean getRootBarrier() {
		return rootBarrier;
	}

	@Override
	public void setRootBarrier(Boolean rootBarrier) {
		this.rootBarrier = rootBarrier;
	}

	@Override
	public Boolean getCurb() {
		return curb;
	}

	@Override
	public void setCurb(Boolean curb) {
		this.curb = curb;
	}

	@Override
	public String getCultivar() {
		return cultivar;
	}

	@Override
	public void setCultivar(String cultivar) {
		this.cultivar = cultivar;
	}

	@Override
	public String getGenus() {
		return genus;
	}

	@Override
	public void setGenus(String genus) {
		this.genus = genus;
	}

	@Override
	public String getSpecies() {
		return species;
	}

	@Override
	public void setSpecies(String species) {
		this.species = species;
	}

	@Override
	public String getCommonName() {
		return commonName;
	}

	@Override
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
}