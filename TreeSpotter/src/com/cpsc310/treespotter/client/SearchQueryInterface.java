/**
 * 
 */
package com.cpsc310.treespotter.client;

import java.util.List;


/**
 * @author maple-quadtree
 *
 */
public interface SearchQueryInterface {
	public enum SearchFieldID
	  {
		  KEYWORD, // use for uncategorized strings
		  SPECIES,
		  LOCATION,
		  ADD_OTHERS_AS_NEEDED
	  }
	  
	  public List<SearchParam> getSearchParams();
	  
	  /**
	   *  to be used like: addSearchParam(new SearchParam(SearchFieldID::SPECIES, species_string));
	   * 
	   * @param searchParam
	   */
	public void addSearchParam(SearchParam searchParam);
	  
	  public void addSearchParam(SearchFieldID fieldID, String fieldString);
	  
	  public void setSearchParams(List<SearchParam> params);
}
