/**
 * 
 */
package com.cpsc310.treespotter.client;

import java.io.Serializable;
import java.util.List;
import com.cpsc310.treespotter.client.SearchParam;


/**
 * @author maple-quadtree
 *
 */
public interface SearchQueryInterface extends Iterable<SearchParam>, Serializable {

	  public List<SearchParam> getSearchParams();

}
