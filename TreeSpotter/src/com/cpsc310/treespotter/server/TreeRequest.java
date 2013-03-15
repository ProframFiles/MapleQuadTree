/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.googlecode.objectify.Ref;

/**
 * @author maple-quadtree
 *
 */
public class TreeRequest {
	Set<Ref<PersistentFile>> currentRequest;
	SortedSet<TreeData> currentTrees = new TreeSet<TreeData>();
	boolean anyFilters = false;
	
	TreeDepot depot;
	
	TreeRequest(TreeDepot parent){
		depot = parent;
		currentRequest = new TreeSet<Ref<PersistentFile>>();
	}
	
	public Collection<TreeData> fetch(){
		if(!anyFilters){
			return Collections.emptyList();
		}
		updateRequest();
		//TreeDepot.loadAllRefs(currentRequest);
		//Collection<TreeData> ret = TreeDepot.deSerializeAllRefs(currentRequest, 10000);
		//currentRequest.clear();
		return currentTrees;
	}
	
	private void updateRequest(){
		if(!currentRequest.isEmpty() && !currentTrees.isEmpty()){
			TreeDepot.loadAllRefs(currentRequest);
			currentTrees = TreeDepot.deSerializeAllRefsToSet(currentRequest, currentTrees);
		}
		else if (!currentRequest.isEmpty() && currentTrees.isEmpty()){
			TreeDepot.loadAllRefs(currentRequest);
			currentTrees.addAll(TreeDepot.deSerializeAllRefs(currentRequest, 230000));
		}
		currentRequest.clear();
	}
	
	public TreeRequest onlyTreesWithKeyword(String kw){
		updateRequest();
		currentRequest.addAll(depot.getAllKeywordRefsWith(kw));
		anyFilters = true;
		return this;
	}
	
	public TreeRequest onlyTreesWithSpecies(String species){
		updateRequest();
		currentRequest.addAll(depot.getAllSpeciesRefsWith(species.toUpperCase()));
		anyFilters = true;
		return this;
	}
	
	public TreeRequest onlyTreesWithStreet(String street){
		updateRequest();
		currentRequest.addAll(depot.getAllStreetRefsWith(street.toUpperCase()));
		anyFilters = true;
		return this;
	}
	
	public TreeRequest onlyTreesWithGenus(String street){
		updateRequest();
		currentRequest.addAll(depot.getAllGenusRefsWith(street.toUpperCase()));
		anyFilters = true;
		return this;
	}

	public TreeRequest onlyTreesWithCommonName(String street){
		updateRequest();
		currentRequest.addAll(depot.getAllCommonNameRefsWith(street.toUpperCase()));
		anyFilters = true;
		return this;
	}
	
	public TreeRequest onlyTreesWithNeighbourhood(String street){
		updateRequest();
		currentRequest.addAll(depot.getAllNeighbourhoodRefsWith(street.toUpperCase()));
		anyFilters = true;
		return this;
	}
}
