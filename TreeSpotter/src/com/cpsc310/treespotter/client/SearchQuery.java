package com.cpsc310.treespotter.client;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import com.cpsc310.treespotter.client.SearchQueryInterface;

public abstract class SearchQuery implements SearchQueryInterface {
  private ArrayList<SearchParam> paramList = new ArrayList<SearchParam>();
	
  public List<SearchParam> getSearchParams(){
	  return Collections.unmodifiableList(paramList);
  }
  
  public Iterator<SearchParam> iterator(){
	  return Collections.unmodifiableList(paramList).iterator();
  }
  
  /**
   *  to be used like: addSearchParam(new SearchParam(SearchFieldID::SPECIES, species_string));
   * 
   * @param searchParam
   */
  public void addSearchParam(SearchParam searchParam){
	  paramList.add(searchParam);
  }
  
  public void addSearchParam(SearchFieldID fieldID, String fieldString){
	  paramList.add(new SearchParam(fieldID, fieldString));
  }
  
  public void setSearchParams(List<SearchParam> params){
	  paramList.clear();
	  paramList.addAll(params);
  }
  
  /**
 * Basic search query creator, to be re-implemented in advanced and basic search.
 * I'm not even sure what the method name/signature should be for this
 * So feel free to change it as needed
 */
  public abstract void CreateSearchParams();
}
