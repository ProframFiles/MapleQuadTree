package com.cpsc310.treespotter.server;

import java.util.Date;
import java.util.HashSet;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class TreeData {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private String treeID;
	
	@Persistent
	private int civicNumber;
	
	@Persistent
	private String stdStreet;
	
	@Persistent
	private String neighbourhood;
	
	@Persistent
	private int cell;
	
	@Persistent
	private String street;
	
	@Persistent
	private String streetBlock;
	
	@Persistent
	private String streetSideName;
	
	@Persistent
	private Boolean assigned;
	
	@Persistent
	private int heightRange;
	
	@Persistent
	private float diameter;
	
	@Persistent
	private Date planted;
	
	@Persistent
	private String plantArea;
	
	@Persistent
	private Boolean rootBarrier;
	
	@Persistent
	private Boolean curb;
	
	@Persistent
	private String cultivar;
	
	@Persistent
	private String genus;
	
	@Persistent
	private String species;
	
	@Persistent
	private String commonName;
	
	@Persistent(defaultFetchGroup="true")
	private HashSet<String> keywords = new HashSet<String>(8, 0.5f);
	
	public TreeData(String user, int treeID) {
		setID(user, treeID);
	}
	
	public TreeData(String user, int treeID, int civicNumber, String stdStreet, 
			String neighbourhood, int cell, String street, String streetBlock,
			String streetSideName, Boolean assigned, int heightRange, float diameter,
			Date planted, String plantArea, Boolean rootBarrier, Boolean curb,
			String cultivar, String genus, String species, String commonName) {
		this.treeID = dataToID(user, treeID);
		this.civicNumber = civicNumber;
		this.stdStreet = stdStreet;
		this.neighbourhood = neighbourhood;
		this.cell = cell;
		this.street = street;
		this.streetBlock = streetBlock;
		this.streetSideName = streetSideName;
		this.assigned = assigned;
		this.heightRange = heightRange;
		this.diameter = diameter;
		this.planted = planted;
		this.plantArea = plantArea;
		this.rootBarrier = rootBarrier;
		this.curb = curb;
		this.cultivar = cultivar;
		this.genus = genus;
		this.species = species;
		this.commonName = commonName;
	}

	private String dataToID(String user, int treeID) {
		String key;
		if (user.equals("admin")) {
			key = "V" + String.valueOf(treeID);
		}
		else
			key = "U" + String.valueOf(treeID);
		return key;
	}

	public String getID() {
		return treeID;
	}
	public void setID(String user, int treeID) {
		this.treeID = dataToID(user, treeID);
	}
	
	public int getCivicNumber() {
		return civicNumber;
	}
	public void setCivicNumber(int civicNumber) {
		this.civicNumber = civicNumber;
	}
	
	public String getStdStreet() {
		return stdStreet;
	}
	public void setStdStreet(String stdStreet) {
		replaceKeyword(this.stdStreet, stdStreet);
		this.stdStreet = stdStreet;
	}
	
	public String getNeighbourhood() {
		return neighbourhood;
	}
	public void setNeighbourhood(String neighbourhood) {
		replaceKeyword(this.neighbourhood, neighbourhood);
		this.neighbourhood = neighbourhood;
	}
	
	public int getCell() {
		return cell;
	}
	public void setCell(int cell) {
		this.cell = cell;
	}
	
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		replaceKeyword(this.street, street);
		this.street = street;
	}
	
	public String getStreetBlock() {
		return streetBlock;
	}
	public void setStreetBlock(String streetBlock) {
		this.streetBlock = streetBlock;
	}
	
	public String getStreetSideName() {
		return streetSideName;
	}
	public void setStreetSideName(String streetSideName) {
		this.streetSideName = streetSideName;
	}
	
	public Boolean getAssigned() {
		return assigned;
	}
	public void setAssigned(Boolean assigned) {
		this.assigned = assigned;
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
	
	public Date getPlanted() {
		return planted;
	}
	public void setPlanted(Date planted) {
		this.planted = planted;
	}
	
	public String getPlantArea() {
		return plantArea;
	}
	public void setPlantArea(String plantArea) {
		this.plantArea = plantArea;
	}
	
	public Boolean getRootBarrier() {
		return rootBarrier;
	}
	public void setRootBarrier(Boolean rootBarrier) {
		this.rootBarrier = rootBarrier;
	}
	
	public Boolean getCurb() {
		return curb;
	}
	public void setCurb(Boolean curb) {
		this.curb = curb;
	}
	
	public String getCultivar() {
		return cultivar;
	}
	public void setCultivar(String cultivar) {
		replaceKeyword(this.cultivar, cultivar);
		this.cultivar = cultivar;
	}
	
	public String getGenus() {
		return genus;
	}
	public void setGenus(String genus) {
		replaceKeyword(this.genus, genus);
		this.genus = genus;
	}
	
	public String getSpecies() {
		return species;
	}
	public void setSpecies(String species) {
		replaceKeyword(this.species, species);
		this.species = species;
	}
	
	public String getCommonName() {
		return commonName;
	}
	public void setCommonName(String commonName) {
		replaceKeyword(this.commonName, commonName);
		this.commonName = commonName;
	}
	private void replaceKeyword(String kw_old, String kw_new ){
		if(kw_old != null){
			int count = numKeywordsMatch(kw_old);
			if(count <= 1){
				keywords.remove(kw_old);
			}
		}
		if(kw_new != null){
			keywords.add(kw_new);
		}
	}
	private int numKeywordsMatch(String s){
		int count = 0;
		if(commonName != null && commonName.equals(s)) count++;
		if(cultivar != null && cultivar.equals(s)) count++;
		if(genus != null && genus.equals(s)) count++;
		if(species != null && species.equals(s)) count++;
		if(neighbourhood != null && neighbourhood.equals(s)) count++;
		if(street != null && street.equals(s)) count++;
		if(stdStreet != null && stdStreet.equals(s)) count++;
		
		return count;
	}
}
