/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.cpsc310.treespotter.client.SearchFieldID;
import com.cpsc310.treespotter.client.SearchParam;
import com.cpsc310.treespotter.client.SearchQueryInterface;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * @author maple-quadtree
 *
 */
public class SearchQueryProcessor {

	// the initial area for the street block search
	// this works out to ~200 meters
	private static double LONGRANGE = 0.004;
	
	PersistenceManager pm = null;
	private static final Logger LOG = Logger.getLogger(SearchQueryProcessor.class.getName());
	
	SearchQueryProcessor(PersistenceManager pm){
		LOG.setLevel(Level.FINER);
		this.pm = pm;
	}
	
	public Set<TreeData> executeNonSpatialQueries(SearchQueryInterface search_params){
		
		Collection<Query> q_list = makeNonSpatialQueries(search_params);
		
		
		if(q_list != null){
			LOG.fine("Parsed search into "+ q_list.size() +" non-spatial queries.");
			return executeQueryList(q_list);
		}
		else{
			LOG.fine("Found no non-spatial queries.");
		}
		
		return null;
		
	}
	
	Set<TreeData> executeQueryList(Collection<Query> q_list){
		Set<TreeData> result_set = new HashSet<TreeData>();
		Set<TreeData> temp_set = new HashSet<TreeData>();
		if(q_list == null){
			return result_set;
		}
		for (Query q: q_list){
			LOG.fine("About to execute query:\n\t" + q.toString());
			
			@SuppressWarnings("unchecked")
			Collection<TreeData> temp_results = (Collection<TreeData>)q.execute();
			if(result_set.isEmpty()){
				result_set.addAll(temp_results);
			}
			else{
				temp_set.addAll(temp_results);
				result_set.retainAll(temp_set);
				temp_set.clear();
			}
		}
		return result_set;
	} 
	
	public Set<TreeData> executeSpatialQueries(SearchQueryInterface search_params){
		
		String location =  getLocationSearchString(search_params);
		if(location == null){
			return null;
		}
		LatLongRange ll = getLatLongFromLocationString(location);
		if(ll == null){
			return new HashSet<TreeData>();
		}
		Collection<Query> q_list = makeBlockQueriesFromLatLong(ll);
		
		LOG.fine("Parsed search into " + q_list.size() + " spatial queries.");
		
		return executeQueryList(q_list);
		
	
	}
	private String getLocationSearchString(SearchQueryInterface search_params){
		for(SearchParam param : search_params){
			if(param.fieldID == SearchFieldID.LOCATION){
				return param.value;
			}
		}
		return null;
	}
	
	public ArrayList<Query> makeNonSpatialQueries(SearchQueryInterface search_params) {
		//TODO aleksy: implement extra search query enums once they're there
		StringBuilder sb = new StringBuilder();
		String sort_order = "";
		String prefix = "(";
		boolean has_search = false;
		for (SearchParam param : search_params) {
			LOG.finer("search param:\n\t(" + param.fieldID.toString() +", " + param.value + ")");
			sb.append(prefix);
			boolean is_valid = true;
			switch (param.fieldID) {
			case ID:
				sb.append("treeID == \"");
				sb.append(KeyFactory.createKey("TreeData", Integer.parseInt(param.value)));
				sb.append('"');
			case KEYWORD:
				sb.append("keywords.contains(\"");
				sb.append(param.value.toUpperCase());
				sb.append("\")");
				break;
			case COMMON:
				sb.append("commonName == \"" + param.value.toUpperCase() + "\"");
				break;
			case NEIGHBOUR:
				sb.append("neighbourhood == \"" + param.value.toUpperCase() + "\"");
				break;
			case DIAMETER:
				IntegerRange d_range = new IntegerRange(param.value);
				sb.append("diameter >= ");
				sb.append((double)d_range.getBottom());
				sb.append(" && diameter <= ");
				sb.append((double)d_range.getTop());
				sort_order = "diameter ascending,";
				break;
			case HEIGHT:
				IntegerRange h_range = new IntegerRange(param.value);
				sb.append("heightRange >= ");
				sb.append(h_range.getBottom());
				sb.append(" && heightRange <= ");
				sb.append(h_range.getTop());
				sort_order = "heightRange ascending,";
				break;
			case GENUS:
				sb.append("genus == \"" + param.value.toUpperCase() + "\"");
				break;
			case ADDRESS:
				StreetBlock address_block = new StreetBlock(param.value);
				sb.append(makeStreetBlockTreeQuery(address_block));
				sort_order = "civicNumber ascending,";
				break;
			case SPECIES:
				sb.append("species == \"" + param.value.toUpperCase() + "\"");
				break;
			default:
				is_valid = false;
				break;
			}
			if(is_valid){
				sb.append(")");
				LOG.finer("current query string:\n\t" + sb.toString());
				prefix = " && (";
				has_search = true;
			}
			else{
				sb.delete(sb.length()-prefix.length(), sb.length());
			}
		}
		ArrayList<Query> ret = null;
		if(has_search){
			ret = new ArrayList<Query>();
			Query q = pm.newQuery(TreeData.class, sb.toString());
			q.setOrdering(sort_order + "treeID descending");
			q.setRange(search_params.getResultsOffset(), search_params.getResultsOffset() + search_params.getNumResults());
			ret.add(q);
		}
		return ret;
	}
	
	private LatLongRange getLatLongFromLocationString( String location){
		
		location = location.trim();
		LatLongRange ll;
		String number_regex = "-?\\d+(\\.\\d*)?";
		String comma_regex = "[ ]*,[ ]*";
		
		if(location.matches(number_regex + comma_regex + number_regex + comma_regex + number_regex)){
			ll= new LatLongRange(location);
		}
		
		else{
			// we have an address search, hopefully
			StreetBlock address_block;
			try{
				address_block = new StreetBlock(location);
				int central_location = (address_block.getAddressTop() + address_block.getAddressBottom() )/2;
				address_block.setBlockRange(central_location-49, central_location+50);
				
			}
			catch(RuntimeException e){
				throw new RuntimeException("Incorrect LOCATION query string: \"" + location + "\". Parsing failed with \n\t" + e.getMessage());
			}
			ll = getLatLongFromAddress(address_block);
		}
		return ll;
	}
	
	private LatLongRange getLatLongFromAddress(StreetBlock address){
		Query q = pm.newQuery(StreetBlock.class, makeStreetBlockQuery(address));
		
		@SuppressWarnings("unchecked")
		Collection<StreetBlock> result_list= (Collection<StreetBlock>)q.execute();
		
		if(result_list != null && !result_list.isEmpty())
		{
			StreetBlock block = result_list.iterator().next();
			return new LatLongRange(block.getLatitude(),block.getLongitude(), 200.0) ;
		}
		//TODO (aleksy) finish this function
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<Query> makeBlockQueriesFromLatLong(LatLongRange ll){
		LOG.fine("Performing block lookup");
		ArrayList<Query> q_list = new ArrayList<Query>();
		String address_search;
		
		Query longQuery = pm.newQuery(StreetBlock.class
				, "longitude <= " + Double.toString(ll.longitude + LONGRANGE) + "&&"
				+ "longitude >= " + Double.toString(ll.longitude - LONGRANGE));
		StreetBlockDistanceComparator comparator = new StreetBlockDistanceComparator(ll.latitude, ll.longitude);
		SortedSet<StreetBlock> block_set = new TreeSet<StreetBlock>(comparator);
		block_set.addAll((Collection<StreetBlock>)longQuery.execute());
		if(!block_set.isEmpty()){
			LOG.fine(block_set.size() + " blocks found around lat/long point");
			StreetBlock street_block = block_set.iterator().next();
			address_search = makeStreetBlockTreeQuery(street_block);
			Query q = pm.newQuery(TreeData.class, address_search);
			q.setOrdering("civicNumber descending");
			q_list.add(q);
		}
		else{
			LOG.log(Level.FINE, "no blocks found around lat/long point");
		}
		
		return q_list;
	}
	
	private String makeStreetBlockTreeQuery(StreetBlock street_block){
		return "civicNumber >= " +  street_block.getBlockStart() 
		+ " && civicNumber <= " + street_block.getBlockEnd()
		+ " && street == \"" +street_block.getStreetName().toUpperCase()+ "\"";
	}
	
	private String makeStreetBlockQuery(StreetBlock street_block){
		return "blockCenter >= " +  street_block.getBlockStart() 
		+ " && blockCenter <= " + street_block.getBlockEnd()
		+ " && streetName == \"" +street_block.getStreetName().toUpperCase()+ "\"";
	}
	
	private class LatLongRange
	{
		public double latitude;
		public double longitude;
		@SuppressWarnings("unused")
		public double range;
		public LatLongRange(String location){
			int firstCommaLocation = location.indexOf(',');
			int lastCommaLocation = location.lastIndexOf(',');
			latitude = Double.parseDouble(location.substring(0, firstCommaLocation));
			longitude = Double.parseDouble(location.substring(firstCommaLocation+1, lastCommaLocation));
			range = 400.0;
		}
		public LatLongRange(double latitude, double longitude, double range){
			this.latitude = latitude;
			this.longitude = longitude;
			this.range = range;
		}
	}
	
}
