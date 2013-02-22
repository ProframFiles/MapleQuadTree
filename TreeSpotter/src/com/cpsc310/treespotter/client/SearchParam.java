package com.cpsc310.treespotter.client;
import java.io.Serializable;

import com.cpsc310.treespotter.client.SearchFieldID;


  /**
 * @author maple-quadtree
 * A small "struct" to be used to pass search terms around
 * tying the string to an enum representing it's actual role is on purpose:
 * we shouldn't have loose strings passed in and around without knowing what
 * they actually represent
 */
public class SearchParam implements Serializable {
		
	  /**
	 * 
	 */
	private static final long serialVersionUID = 6375372449896643153L;
	public SearchParam(){
	}
	public SearchParam(SearchFieldID searchFieldID, String stringValue ){
		  value = stringValue;
		  fieldID = searchFieldID;
	  }
	  public SearchFieldID fieldID;
	  public String value;
  }