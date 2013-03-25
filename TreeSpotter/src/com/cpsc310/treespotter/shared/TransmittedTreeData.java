/**
 * 
 */
package com.cpsc310.treespotter.shared;

import java.io.Serializable;

/**
 * @author maple-quadtree
 *
 */
public class TransmittedTreeData implements Serializable, ISharedTreeData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1L;
	
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
	private LatLong latLong;

	public TransmittedTreeData(){
		
	}
	
	public TransmittedTreeData(String id) {
		treeID = id;
	}
	
	/* (non-Javadoc)
	 * @see com.cpsc310.treespotter.shared.ISharedTreeData#getID()
	 */
	@Override
	public String getID(){
		return treeID;
	}

	/* (non-Javadoc)
	 * @see com.cpsc310.treespotter.shared.ISharedTreeData#getLocation()
	 */
	@Override
	public String getLocation(){
		String num = civicNumber < 0 ? "" : civicNumber + " ";
		return num + street;
	}

	// depends on if we use the geocoded LatLng data
	// TODO (someone) turn this into actual coordinates
/*	public String getCoordinates(){
		return getLocation();
	}*/

	/* (non-Javadoc)
	 * @see com.cpsc310.treespotter.shared.ISharedTreeData#getCivicNumber()
	 */
	@Override
	public int getCivicNumber() {
		return civicNumber;
	}

	public void setCivicNumber(int civicNumber) {
		this.civicNumber = civicNumber;
	}

	/* (non-Javadoc)
	 * @see com.cpsc310.treespotter.shared.ISharedTreeData#getTreeID()
	 */
	@Override
	public String getTreeID() {
		return treeID;
	}

	public void setTreeID(String treeID) {
		this.treeID = treeID;
	}

	/* (non-Javadoc)
	 * @see com.cpsc310.treespotter.shared.ISharedTreeData#getNeighbourhood()
	 */
	@Override
	public String getNeighbourhood() {
		return neighbourhood;
	}

	public void setNeighbourhood(String neighbourhood) {
		this.neighbourhood = neighbourhood;
	}

	/* (non-Javadoc)
	 * @see com.cpsc310.treespotter.shared.ISharedTreeData#getStreet()
	 */
	@Override
	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	/* (non-Javadoc)
	 * @see com.cpsc310.treespotter.shared.ISharedTreeData#getHeightRange()
	 */
	@Override
	public int getHeightRange() {
		return heightRange;
	}

	public void setHeightRange(int heightRange) {
		this.heightRange = heightRange;
	}

	/* (non-Javadoc)
	 * @see com.cpsc310.treespotter.shared.ISharedTreeData#getDiameter()
	 */
	@Override
	public float getDiameter() {
		return diameter;
	}

	public void setDiameter(float diameter) {
		this.diameter = diameter;
	}

	/* (non-Javadoc)
	 * @see com.cpsc310.treespotter.shared.ISharedTreeData#getPlanted()
	 */
	@Override
	public String getPlanted() {
		return planted;
	}

	public void setPlanted(String planted) {
		this.planted = planted;
	}

	/* (non-Javadoc)
	 * @see com.cpsc310.treespotter.shared.ISharedTreeData#getCultivar()
	 */
	@Override
	public String getCultivar() {
		return cultivar;
	}

	public void setCultivar(String cultivar) {
		this.cultivar = cultivar;
	}

	/* (non-Javadoc)
	 * @see com.cpsc310.treespotter.shared.ISharedTreeData#getGenus()
	 */
	@Override
	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	/* (non-Javadoc)
	 * @see com.cpsc310.treespotter.shared.ISharedTreeData#getSpecies()
	 */
	@Override
	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	/* (non-Javadoc)
	 * @see com.cpsc310.treespotter.shared.ISharedTreeData#getCommonName()
	 */
	@Override
	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	/* (non-Javadoc)
	 * @see com.cpsc310.treespotter.shared.ISharedTreeData#getLatLong()
	 */
	@Override
	public LatLong getLatLong() {
		return latLong;
	}
	
	public void setLatLong(LatLong latLong) {
		this.latLong = latLong;
	}
	
}
