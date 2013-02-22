package com.cpsc310.treespotter.client;

import java.io.Serializable;

public class AdminTreeData implements ClientTreeData, Serializable {
  
  public String getID() {
    return null;
  }
  
  public String getLocation() {
    return null;
  }
  
  // depends on if we use the geocoded LatLng data
  public String getCoordinates() {
    return null;
  }
  
  public String getSpecies() {
    return null;
  }
  
  public int getDiameter() {
    return 0;
  }
  
  // would this be height class instead?
  public int getHeight() {
    return 0;
  }
}
