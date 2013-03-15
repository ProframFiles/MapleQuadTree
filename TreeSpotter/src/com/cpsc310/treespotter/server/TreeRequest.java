/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.googlecode.objectify.Ref;

/**
 * @author maple-quadtree
 *
 */
public class TreeRequest {
	Set<Ref<PersistentFile>> currentRequest;
	boolean anyFilters = false;
	
	TreeDepot depot;
	
	TreeRequest(TreeDepot parent){
		depot = parent;
		currentRequest = new TreeSet<Ref<PersistentFile>>();
		depot.addAllRefsToRequest(currentRequest);
		
	}
	
	public Collection<TreeData2> fetch(){
		if(!anyFilters){
			return Collections.emptyList();
		}
		TreeDepot.loadAllRefs(currentRequest);
		Collection<TreeData2> ret = TreeDepot.deSerializeAllRefs(currentRequest, 10000);
		currentRequest.clear();
		return ret;
	}
	
	public TreeRequest onlyTreesWithKeyword(String kw){
		currentRequest.retainAll(depot.getAllKeywordRefsWith(kw));
		anyFilters = true;
		return this;
	}
	
	public TreeRequest onlyTreesWithSpecies(String species){
		currentRequest.retainAll(depot.getAllSpeciesRefsWith(species.toUpperCase()));
		anyFilters = true;
		return this;
	}
	
	public TreeRequest onlyTreesWithStreet(String street){
		currentRequest.retainAll(depot.getAllStreetRefsWith(street.toUpperCase()));
		anyFilters = true;
		return this;
	}
	
	public TreeRequest onlyTreesWithGenus(String street){
		currentRequest.retainAll(depot.getAllGenusRefsWith(street.toUpperCase()));
		anyFilters = true;
		return this;
	}

	public TreeRequest onlyTreesWithCommonName(String street){
		currentRequest.retainAll(depot.getAllCommonNameRefsWith(street.toUpperCase()));
		anyFilters = true;
		return this;
	}
	
	public TreeRequest onlyTreesWithNeighbourhood(String street){
		currentRequest.retainAll(depot.getAllNeighbourhoodRefsWith(street.toUpperCase()));
		anyFilters = true;
		return this;
	}
}
