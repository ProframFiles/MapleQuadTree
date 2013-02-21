package com.cpsc310.treespotter.server;

import java.sql.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable
public class TreeData {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key treeID;
	
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
	private int diameter;
	
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
	
	public TreeData(String user, int treeID) {
		Key key = KeyFactory.createKey(user, treeID);
		this.treeID = key;
	}
	
	public Key getID() {
		return treeID;
	}
	public void setID(String user, int treeID) {
		Key key = KeyFactory.createKey(user, treeID);
		this.treeID = key;
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
		this.stdStreet = stdStreet;
	}
	
	public String getNeighbourhood() {
		return neighbourhood;
	}
	public void setNeighbourhood(String neighbourhood) {
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
	
	public int getDiameter() {
		return diameter;
	}
	public void setDiameter(int diameter) {
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
