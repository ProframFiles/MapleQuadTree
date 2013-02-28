package com.cpsc310.treespotter.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.cpsc310.treespotter.client.AdminTreeData;
import com.cpsc310.treespotter.client.ClientTreeData;
import com.cpsc310.treespotter.client.SearchParam;
import com.cpsc310.treespotter.client.SearchQueryInterface;
import com.cpsc310.treespotter.client.TreeDataService;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;

public class TreeDataServiceImpl extends RemoteServiceServlet implements
		TreeDataService {
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = Logger.getLogger(TreeDataServiceImpl.class.getName());
	
	// the initial area for the street block search
	// this works out to ~200 meters
	private static double LATRANGE = 0.002*0.65;
	private static double LONGRANGE = 0.002;
	
	// as it says: we'll only ever return this many
	private static int MAXRESULTS = 1000;

	public TreeDataServiceImpl(){
		LOG.setLevel(Level.FINER);
	
		//(aleksy) uncomment this to fetch data about street block locations on startup
		//it will only do the full parse if the current data isn't up to date
		//QueueFactory.getDefaultQueue().add(withUrl("/treespotter/tasks/streetblockupdate"));
		
	}
	
	@Override
	public void importFromSite(String url) {
		QueueFactory.getDefaultQueue().add(withUrl("/treespotter/tasks/streetblockupdate"));
	}

	@Override
	public void addTree(ClientTreeData info) {

	}

	@Override
	public ClientTreeData getTreeData(String queryID, String userType) {
		ClientTreeData ret = null;
		LOG.fine("Trying to find tree with id " + queryID);
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(TreeData.class, "treeID == id");
			q.declareParameters("string id");
			q.setUnique(true); 
			
			LOG.fine("about to  make query: " + q.toString());
			TreeData query_result = (TreeData) q.execute(queryID);

			if (query_result != null) {
				LOG.info("tree " + queryID + " found, creating ClientTreeData");
				if (userType != null && userType.equals("user")) {
					ret = makeUserTreeData(query_result);
				}
				if (userType != null && userType.equals("admin")) {
					ret = makeAdminTreeData(query_result);
				}
			}
			else{
				LOG.info("tree " + queryID + " not found in DB");
			}
		} finally {
			pm.close();
		}
		return ret;
	}

	@Override
	public ArrayList<ClientTreeData> searchTreeData(SearchQueryInterface query) {
		LOG.setLevel(Level.FINER);
		ArrayList<ClientTreeData> results = null;
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			
			Query q = makeDBQueryFromSearch(pm, query);
			
			LOG.fine("About to execute query:\n\t" + q.toString());
			
			@SuppressWarnings("unchecked")
			Collection<TreeData> tree_list = (Collection<TreeData>)q.execute();
			int total_results = tree_list.size();
			LOG.info("\tFound " + total_results + " tree results in the DB");
			
			if (total_results > 0) {
				results = new ArrayList<ClientTreeData>();
				int result_count = 0;
				for (TreeData server_tree : tree_list) {
					results.add(makeUserTreeData(server_tree));
					result_count++;
					if(result_count > MAXRESULTS){
						LOG.warning("\n\tNumber of results exceeded maximum, returning first" + MAXRESULTS);
						break;
					}
				}
			}
		} finally {
			pm.close();
		}
		
		return results;
	}

	private ClientTreeData makeUserTreeData(TreeData tree_data) {
		ClientTreeData user_data = new ClientTreeData();
		user_data.setTreeID(tree_data.getID().toString());
		user_data.setCivicNumber(tree_data.getCivicNumber());
		user_data.setNeighbourhood(tree_data.getNeighbourhood());
		user_data.setStreet(tree_data.getStreet());
		user_data.setHeightRange(tree_data.getHeightRange());
		user_data.setDiameter(tree_data.getDiameter());
		user_data.setPlanted(tree_data.getPlanted());
		user_data.setCultivar(tree_data.getCultivar());
		user_data.setGenus(tree_data.getGenus());
		user_data.setSpecies(tree_data.getSpecies());
		user_data.setCommonName(tree_data.getCommonName());
		return user_data;
	}

	private AdminTreeData makeAdminTreeData(TreeData tree_data) {
		// TODO: differentiate this from user data
		AdminTreeData admin_data = new AdminTreeData();
		admin_data.setTreeID(tree_data.getID().toString());
		admin_data.setCivicNumber(tree_data.getCivicNumber());
		admin_data.setNeighbourhood(tree_data.getNeighbourhood());
		admin_data.setStreet(tree_data.getStreet());
		admin_data.setHeightRange(tree_data.getHeightRange());
		admin_data.setDiameter(tree_data.getDiameter());
		admin_data.setPlanted(tree_data.getPlanted());
		admin_data.setCultivar(tree_data.getCultivar());
		admin_data.setGenus(tree_data.getGenus());
		admin_data.setSpecies(tree_data.getSpecies());
		admin_data.setCommonName(tree_data.getCommonName());
		return admin_data;
	}

	private Query makeDBQueryFromSearch(PersistenceManager pm, SearchQueryInterface search_params) {
		//TODO aleksy: implement extra search query enums once they're there
		StringBuilder sb = new StringBuilder();
		String sort_order = "";
		String prefix = "(";
		for (SearchParam param : search_params) {
			LOG.finer("search param:\n\t(" + param.fieldID.toString() +", " + param.value + ")");
			sb.append(prefix);
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
				sb.append(d_range.getBottom());
				sb.append(" && diameter <= ");
				sb.append(d_range.getTop());
				sort_order = "diameter ascending,";
				break;
			case HEIGHT:
				IntegerRange h_range = new IntegerRange(param.value);
				sb.append("height >= ");
				sb.append(h_range.getBottom());
				sb.append(" && height <= ");
				sb.append(h_range.getTop());
				sort_order = "height ascending,";
				break;
			case GENUS:
				sb.append("genus == \"" + param.value.toUpperCase() + "\"");
				break;
			case LOCATION:
				sb.append(makeLocationQueryString(pm, param.value));
				sort_order = "civicNumber ascending,";
				break;
			case ADDRESS:
				StreetBlock address_block = new StreetBlock(param.value);
				sb.append(makeStreetBlockQuery(address_block));
				sort_order = "civicNumber ascending,";
				break;
			case SPECIES:
				sb.append("species == \"" + param.value.toUpperCase() + "\"");
				break;
			default:
				break;
			}

			sb.append(")");
			LOG.finer("current query string:\n\t" + sb.toString());
			prefix = " && (";
		}
		Query q = pm.newQuery(TreeData.class, sb.toString());
		q.setOrdering(sort_order + "treeID descending");
		q.setRange(search_params.getResultsOffset(), search_params.getResultsOffset() + search_params.getNumResults());
		return q;
	}

	@SuppressWarnings("unchecked")
	private String makeLocationQueryString(PersistenceManager pm, String location) {
		//TODO tune the lat+ long ranges.
		//TODO return more than one blocks worth of trees
		//TODO actually use the given radius
		LOG.fine("Performing block lookup");
		String address_search = null;
		int firstCommaLocation = location.indexOf(',');
		int lastCommaLocation = location.lastIndexOf(',');
		double latitude = Double.parseDouble(location.substring(0, firstCommaLocation));
		double longitude = Double.parseDouble(location.substring(firstCommaLocation+1, lastCommaLocation));
		Query longQuery = pm.newQuery(StreetBlock.class
				, "longitude <= " + Double.toString(longitude + LONGRANGE) + "&&"
				+ "longitude >= " + Double.toString(longitude - LONGRANGE));
		//Query latQuery = pm.newQuery(StreetBlock.class
		//		, "latitude <= " + Double.toString(latitude + LATRANGE) + "&&"  
		//		+ "latitude >= " + Double.toString(latitude - LATRANGE));
		StreetBlockDistanceComparator comparator = new StreetBlockDistanceComparator(latitude, longitude);
		SortedSet<StreetBlock> block_set = new TreeSet<StreetBlock>(comparator);
		block_set.addAll((Collection<StreetBlock>)longQuery.execute());
		//block_set.addAll((Collection<StreetBlock>)latQuery.execute());
		if(!block_set.isEmpty()){
			LOG.fine(block_set.size() + " blocks found around lat/long point");
			StreetBlock street_block = block_set.iterator().next();
			address_search = makeStreetBlockQuery(street_block);
		}
		else{
			LOG.log(Level.FINE, "no blocks found around lat/long point");
			address_search = "civicNumber == -1";
		}
		return address_search;
	}
	private String makeStreetBlockQuery(StreetBlock street_block){
		return "civicNumber >= " +  street_block.getBlockStart() 
		+ " && civicNumber <= " + street_block.getBlockEnd()
		+ " && street == \"" +street_block.getStreetName().toUpperCase()+ "\"";
	}
}