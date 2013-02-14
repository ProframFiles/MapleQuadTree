package com.cpsc310.treespotter.client;

import java.util.ArrayList;

public interface SearchQuery {
  
  public String getSearchType();
  
  // need to properly define what params are
  public ArrayList<String> getSearchParams();
  
  public void setSearchType(String type);
  
  public void setSearchParams(ArrayList<String> params);
}
