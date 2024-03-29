/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cpsc310.treespotter.shared.DistanceToPointProvider;
import com.cpsc310.treespotter.shared.LatLong;
import com.googlecode.objectify.Ref;

/**
 * @author maple-quadtree
 *
 */
public class TreeRequest {
	private static final Logger LOG = Logger.getLogger("Tree");
	Set<Ref<PersistentFile>> currentRequest;
	Set<String> requestBins;
	SortedSet<TreeData> currentTrees = new TreeSet<TreeData>();
	ArrayList<TreeFilter> filters = new ArrayList<TreeFilter>(); 
	boolean anyFilters = false;
	boolean isSpatialSearch = false;
	int resultsMax = 1000;
	int resultsMin = 0;
	
	TreeDepot depot;
	
	TreeRequest(TreeDepot parent){
		LOG.setLevel(Level.FINE);
		depot = parent;
		currentRequest = new TreeSet<Ref<PersistentFile>>();
		requestBins = new HashSet<String>();
		requestBins.addAll(depot.getBinSet());
	}
	
	public Collection<TreeData> fetch(){
		Set<TreeData> ret = Collections.emptySet();
		if(!anyFilters){
			return ret;
		}
		else if((filters.size()>1 && requestBins.size() < currentRequest.size()*2) || isSpatialSearch){
			LOG.info("Doing a bin search with " + requestBins.size() + " bins instead of " + currentRequest.size() +" refs");
			Set<Ref<PersistentFile>> req = depot.getAllRefsInBins(requestBins);
			TreeDepot.loadAllRefs(req);
			ret = TreeDepot.deSerializeAllRefs(req, 10000);
			
		}
		else{
			LOG.info("Doing a ref search with " + currentRequest.size() + " refs instead of " + requestBins.size() +" bins");
			TreeDepot.loadAllRefs(currentRequest);
			if(filters.size()<=1){
				ret = TreeDepot.deSerializeAllRefs(currentRequest, resultsMax);
			}
			else{
				ret = TreeDepot.deSerializeAllRefs(currentRequest, 10000);
			}
		}
		LOG.info("retrieved " + ret.size() + " trees from the depot");
		ArrayList<String> mod_trees = new ArrayList<String>();
		for(TreeData tree: ret){
			if(tree.getID().startsWith("M")){
				mod_trees.add("V"+tree.getID().substring(1));
			}
		}
		if(!mod_trees.isEmpty()){
			filters.add(new TreeIDExcluder(mod_trees));
		}
		Set<TreeData> filtered = ret;
		if(filters.size()>1 || isSpatialSearch){
			LOG.info("now filtering with " + filters.size() + " filters");
			filtered = new HashSet<TreeData>();
			for(TreeData tree: ret){
				boolean match = true;
				for(TreeFilter filt: filters){
					if(!filt.isMatch(tree)){
						match=false;
						break;
					}
				}
				if(match){
					filtered.add(tree);
				}
			}
		}
		LOG.info("found " + filtered.size() + " results total");
		return filtered;
	}
	
	public TreeRequest onlyTreesWithKeyword(String kw){
		
		if(!anyFilters){
			currentRequest.addAll(depot.getAllRefsWithKeyword(kw.toUpperCase()));
		}
		requestBins.retainAll(depot.getAllBinsWithKeyword(kw.toUpperCase()));
		filters.add(new TreeMatcher(kw.toUpperCase(), TreeToStringFactory.getTreeToKeywords()));
		anyFilters = true;
		LOG.info("filtered request to " + requestBins.size() + " bins");
		return this;
	}
	
	public TreeRequest onlyTreesWithSpecies(String species){

		if(!anyFilters){
			currentRequest.addAll(depot.getAllSpeciesRefsWith(species.toUpperCase()));
		}
		requestBins.retainAll(depot.getAllSpeciesBinsWith(species.toUpperCase()));
		filters.add(new TreeMatcher(species.toUpperCase(), TreeToStringFactory.getTreeToSpecies()));
		anyFilters = true;
		LOG.info("filtered request to " + requestBins.size() + " bins");
		return this;
	}
	
	public TreeRequest onlyTreesWithinRect(LatLong southWest, LatLong northEast){
		requestBins.retainAll( TreeGridStore.getAllBinsWithin(southWest, northEast));
		anyFilters = true;
		isSpatialSearch = true;
		LOG.info("filtered request to " + requestBins.size() + " bins");
		return this;
	}
	
	public TreeRequest onlyTreesAroundPoint(LatLong center, double radius_meters){
		double long_degrees = Math.toDegrees(Math.cos(center.getLatitudeRadians())*radius_meters*(Math.PI/DistanceToPointProvider.R_EARTH));
		double lat_degrees = Math.toDegrees(radius_meters*(Math.PI/DistanceToPointProvider.R_EARTH));
		LatLong southWest = new LatLong(center.getLatitude()-lat_degrees, center.getLongitude()-long_degrees);
		LatLong northEast = new LatLong(center.getLatitude()+lat_degrees, center.getLongitude()+long_degrees);
		filters.add(new TreeRadiusFilter(center, radius_meters));
		return onlyTreesWithinRect(southWest, northEast);
	}
	
	public TreeRequest onlyTreesWithStreet(String street){

		if(!anyFilters){
			currentRequest.addAll(depot.getAllStreetRefsWith(street.toUpperCase()));
		}
		requestBins.retainAll(depot.getAllStreetBinsWith(street.toUpperCase()));
		filters.add(new TreeMatcher(street.toUpperCase(), TreeToStringFactory.getTreeToStdStreet()));
		anyFilters = true;
		LOG.info("filtered request to " + requestBins.size() + " bins");
		return this;
	}
	
	public TreeRequest onlyTreesWithStreetNumber(int addressBottom, int addressTop) {
		filters.add(new TreeAddressFilter(addressBottom, addressTop));
		anyFilters = true;
		return this;
	}
	
	
	public TreeRequest onlyTreesWithGenus(String genus){
		if(!anyFilters){
			currentRequest.addAll(depot.getAllGenusRefsWith(genus.toUpperCase()));
		}
		requestBins.retainAll(depot.getAllGenusBinsWith(genus.toUpperCase()));
		filters.add(new TreeMatcher(genus.toUpperCase(), TreeToStringFactory.getTreeToGenus()));
		anyFilters = true;
		LOG.info("filtered request to " + requestBins.size() + " bins");
		return this;
	}

	public TreeRequest onlyTreesWithCommonName(String name){
		if(!anyFilters){
			currentRequest.addAll(depot.getAllCommonNameRefsWith(name.toUpperCase()));
		}
		requestBins.retainAll(depot.getAllCommonNameBinsWith(name.toUpperCase()));
		filters.add(new TreeMatcher(name.toUpperCase(), TreeToStringFactory.getTreeToCommonName()));
		anyFilters = true;
		LOG.info("filtered request to " + requestBins.size() + " bins");
		return this;
	}
	public TreeRequest setResultsRange(int min, int max){
		resultsMin = min;
		resultsMax = max;
		return this;
	}
	
	public TreeRequest onlyTreesWithNeighbourhood(String neighbourhood){
		if(!anyFilters){
			currentRequest.addAll(depot.getAllNeighbourhoodRefsWith(neighbourhood.toUpperCase()));
		}
		requestBins.retainAll(depot.getAllNeighbourhoodBinsWith(neighbourhood.toUpperCase()));
		filters.add(new TreeMatcher(neighbourhood.toUpperCase(), TreeToStringFactory.getTreeToNeighbourhood()));
		anyFilters = true;
		LOG.info("filtered request to " + requestBins.size() + " bins");
		return this;
	}
	private class TreeMatcher implements TreeFilter{

		String matchString; 
		TreeStringProvider tsp;
		TreeMatcher(String matchString, TreeStringProvider converter){
			this.matchString = matchString;
			tsp = converter;
		}
		
		@Override
		public boolean isMatch(TreeData tree) {
			boolean match = false;
			if(tree != null){
				match = tsp.treeToString(tree).toUpperCase().contains(matchString);
				
			}
			if(match){
				//LOG.info("match for " + matchString + " in " + tsp.treeToString(tree));
			}
			else{
				//LOG.info("found match for " + matchString + " in " + tsp.treeToString(tree));
			}
			return match;
		}
		
	}
	private class TreeIDExcluder implements TreeFilter{

		ArrayList<String> matchStrings; 
		TreeIDExcluder(ArrayList<String> ids){
			this.matchStrings = ids;
		}
		
		@Override
		public boolean isMatch(TreeData tree) {
			boolean match = true;
			if(tree != null){
				for(String id: matchStrings){
					if(tree.getID().equals(id)){
						match = false;
					}
				}
			}
			if(match){
				//LOG.info("match for " + matchString + " in " + tsp.treeToString(tree));
			}
			else{
				//LOG.info("found match for " + matchString + " in " + tsp.treeToString(tree));
			}
			return match;
		}
		
	}
	private class TreeAddressFilter implements TreeFilter{

		int upper;
		int lower;
		public TreeAddressFilter(int lower, int upper){
			this.upper = upper;
			this.lower = lower;
		}
		
		@Override
		public boolean isMatch(TreeData tree) {
			boolean match = false;
			if(tree != null){
				match = lower < 0 || upper < 0 || (tree.getCivicNumber()>=lower && tree.getCivicNumber()<=upper);
				
			}
			if(match){
				//LOG.info("match for " + matchString + " in " + tsp.treeToString(tree));
			}
			else{
				//LOG.info("found match for " + matchString + " in " + tsp.treeToString(tree));
			}
			return match;
		}
	}
	private class TreeRadiusFilter implements TreeFilter{
		DistanceToPointProvider distanceMaker;
		double radiusMetersSq;
		TreeRadiusFilter(LatLong center, double radius_meters){
			distanceMaker = new DistanceToPointProvider(center);
			radiusMetersSq = radius_meters*radius_meters;
		}
		@Override
		public boolean isMatch(TreeData tree) {
			return radiusMetersSq >= distanceMaker.getDistanceSq(tree);
		}
		
	}
	
}
