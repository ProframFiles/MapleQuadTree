package com.cpsc310.treespotter.client;

import java.io.Serializable;

public class UserTreeData implements ClientTreeData, Serializable {
  
  public String getID() {
    return null;
  }
  
  public String getLocation() {
    return null;
  }
  
  // depends on if we use the geocoded LatLng data
  public String getCoordinates() {
//    return null;
    return "49.225838, -123.017374";
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
