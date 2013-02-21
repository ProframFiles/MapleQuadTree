package com.cpsc310.treespotter.client;

import java.util.ArrayList;

public interface SearchQuery {
  public enum SearchFieldID
  {
	  KEYWORD, // use for uncategorized strings
	  SPECIES,
	  LOCATION,
	  ADD_OTHERS_AS_NEEDED
  }
  
  // A small "struct" to be used to pass search terms around
  // tying the string to an enum representing it's actual role is on purpose:
  // we shouldn't have loose strings passed in and around without knowing what
  // they actually represent
  public class SearchParam {
	  SearchParam(SearchFieldID searchFieldID, String stringValue ){
		  value = stringValue;
		  fieldID = searchFieldID;
	  }
	  public SearchFieldID fieldID;
	  public String value;
  }
  
  public ArrayList<SearchParam> getSearchParams();
  
  // to be used like: addSearchParam(new SearchParam(SearchFieldID::SPECIES, species_string));
  public void addSearchParam(SearchParam searchParam);
  
  public void addSearchParam(SearchFieldID fieldID, String fieldString);
  
  public void setSearchParams(ArrayList<SearchParam> params);
}
