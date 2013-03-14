/**
 * 
 */
package com.cpsc310.treespotter.server;

/**
 * @author maple-quadtree
 *
 */
public interface TreeDataProvider {
	public String getID();
	
	public void setID(String prefix, int treeID);
	
	public int getCivicNumber();
	public void setCivicNumber(int civicNumber);
	
	public String getStdStreet();
	public void setStdStreet(String stdStreet);
	
	public String getNeighbourhood();
	public void setNeighbourhood(String neighbourhood);
	
	public int getCell();
	public void setCell(int cell);
	
	public String getStreet();
	public void setStreet(String street);
	
	public String getStreetBlock();
	public void setStreetBlock(String streetBlock);
	
	public String getStreetSideName();
	public void setStreetSideName(String streetSideName);
	
	public Boolean getAssigned();
	public void setAssigned(Boolean assigned);
	
	public int getHeightRange();
	public void setHeightRange(int heightRange);
	
	public float getDiameter();
	public void setDiameter(float diameter);
	
	public String getPlanted();
	public void setPlanted(String planted);
	
	public String getPlantArea();
	public void setPlantArea(String plantArea);
	
	public Boolean getRootBarrier();
	public void setRootBarrier(Boolean rootBarrier);
	
	public Boolean getCurb();
	public void setCurb(Boolean curb);
	
	public String getCultivar();
	public void setCultivar(String cultivar);
	
	public String getGenus();
	public void setGenus(String genus);
	
	public String getSpecies();
	public void setSpecies(String species);
	
	public String getCommonName();
	public void setCommonName(String commonName);
	
	public String getKeywordString();
}
