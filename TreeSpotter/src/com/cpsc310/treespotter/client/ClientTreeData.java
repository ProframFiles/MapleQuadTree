package com.cpsc310.treespotter.client;

public interface ClientTreeData {

  public String getID();
  
  public String getLocation();
  
  // depends on if we use the geocoded LatLng data
  public String getCoordinates();
  
  public String getSpecies();
  
  public int getDiameter();
  
  // would this be height class instead?
  public int getHeight();
}
