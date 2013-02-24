package com.cpsc310.treespotter.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.cpsc310.treespotter.client.AdminTreeData;
import com.cpsc310.treespotter.client.ClientTreeData;
import com.cpsc310.treespotter.client.KeywordSearch;
import com.cpsc310.treespotter.client.SearchFieldID;
import com.cpsc310.treespotter.client.SearchParam;
import com.cpsc310.treespotter.client.SearchQueryInterface;
import com.cpsc310.treespotter.client.TreeDataService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TreeDataServiceImpl extends RemoteServiceServlet implements
		TreeDataService {
	private static final long serialVersionUID = 1L;
	// the initial area for the street block search
	// this works out to ~200 meters
	private static double latRange = 0.005*0.65;
	private static double longRange = 0.005;

	public TreeDataServiceImpl(){
		//place some test trees in the database
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(makeTestTree("JIM", 1));
			pm.makePersistent(makeTestTree("BOB", 2));
			pm.makePersistent(makeTestTree("MARY", 3));
			pm.makePersistent(makeTestTree("RASTAPOUPOULOS", 4));
			TreeData highbury_tree = makeTestTree("HIGHBURY TREE", 5);
			highbury_tree.setStreet("HIGHBURY ST");
			highbury_tree.setCivicNumber(2632);
			pm.makePersistent(highbury_tree);
		}
		finally{
			pm.close();
		}
		try {
			LocationProcessor.ParseLocationFile("./public_streets_truncated.kml");
		}	
		catch(RuntimeException e){
			System.out.println(e.getMessage());
		}
	}
	
	@Override
	public void importFromSite(String url) {

	}

	@Override
	public void addTree(ClientTreeData info) {

	}

	@Override
	public ClientTreeData getTreeData(String id, String userType) {
		//TODO change this back to null
		ClientTreeData ret = makeUserTreeData(makeTestTree("TEST_TREE", 12234));
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(TreeData.class, "treeID == id");
			q.declareParameters("com.google.appengine.api.datastore.Key id");
			q.setUnique(true);
			Key lookup_key = KeyFactory.createKey("TreeData", Integer.parseInt(id.trim()));
			
			TreeData query_result = (TreeData) q.execute(lookup_key);

			if (query_result != null) {
				if (userType != null && userType.equals("user")) {
					ret = makeUserTreeData(query_result);
				}
				if (userType != null && userType.equals("admin")) {
					ret = makeAdminTreeData(query_result);
				}
			}
		} finally {
			pm.close();
		}
		return ret;
	}

	@Override
	public ArrayList<ClientTreeData> searchTreeData(SearchQueryInterface query) {
		ArrayList<ClientTreeData> results = null;
		
		 PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			//loc_query.addSearchParam(SearchFieldID.LOCATION, "49.2626,-123.1878,200");
			Query q = makeDBQueryFromSearch(pm, query);
			
			@SuppressWarnings("unchecked")
			Collection<TreeData> tree_list = (Collection<TreeData>)q.execute();

			if (tree_list.size() > 0) {
				results = new ArrayList<ClientTreeData>();
				for (TreeData server_tree : tree_list) {
					results.add(makeUserTreeData(server_tree));
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
		String prefix = "(";
		for (SearchParam param : search_params) {
			sb.append(prefix);
			switch (param.fieldID) {
			case KEYWORD:
				sb.append("keywords.contains(\"");
				sb.append(param.value);
				sb.append("\")");
				break;
			case COMMON:
				sb.append("commonName == \"");
				sb.append(param.value);
				sb.append('"');
				break;
			case GENUS:
				sb.append("genus == \"");
				sb.append(param.value);
				sb.append('"');
				break;
			case LOCATION:
				sb.append(makeLocationQueryString(pm, param.value));
				break;
			case SPECIES:
				sb.append("species == \"");
				sb.append(param.value);
				sb.append('"');
				break;
			default:
				break;
			}

			sb.append(")");
			prefix = " && (";
		}

		return pm.newQuery(TreeData.class, sb.toString());
	}

	private String makeLocationQueryString(PersistenceManager pm, String location) {
		//TODO tune the lat+ long ranges.
		//TODO return more than one blocks worth of trees
		//TODO actually use the given radius
		System.out.println("Performing block lookup");
		String address_search = null;
		int firstCommaLocation = location.indexOf(',');
		int lastCommaLocation = location.lastIndexOf(',');
		double latitude = Double.parseDouble(location.substring(0, firstCommaLocation));
		double longitude = Double.parseDouble(location.substring(firstCommaLocation+1, lastCommaLocation));
		Query longQuery = pm.newQuery(StreetBlock.class
				, "longitude <= " + Double.toString(longitude + longRange) + "&&"
				+ "longitude >= " + Double.toString(longitude - longRange));
		Query latQuery = pm.newQuery(StreetBlock.class
				, "latitude <= " + Double.toString(latitude + latRange) + "&&"  
				+ "latitude >= " + Double.toString(latitude - latRange));
		StreetBlockDistanceComparator comparator = new StreetBlockDistanceComparator(latitude, longitude);
		SortedSet<StreetBlock> block_set = new TreeSet<StreetBlock>(comparator);
		block_set.addAll((Collection<StreetBlock>)longQuery.execute());
		block_set.addAll((Collection<StreetBlock>)latQuery.execute());
		if(!block_set.isEmpty()){
			System.out.println(block_set.size() + " blocks found around lat/long point");
			StreetBlock street_block = block_set.iterator().next();
			address_search = "(civicNumber >= " +  street_block.getBlockStart() 
					+ " && civicNumber <= " + street_block.getBlockEnd()
					+ " && street == \"" +street_block.getStreetName()+ "\")";
		}
		else{
			System.out.println("no blocks found around lat/long point");
			address_search = "civicNumber == -1";
		}
		System.out.println("created tree sub query:" + address_search);
		return address_search;
	}
	private TreeData makeTestTree(String common, int id){
		TreeData tree = new TreeData("TreeData", id);
		tree.setSpecies("AFAKESPECIES");
		tree.setStreet("THE CRESCENT");
		tree.setCivicNumber(240);
		tree.setNeighbourhood("THE BRONX");
		tree.setCultivar("GILDED LILY");
		tree.setGenus("RUGOSA");
		tree.setCommonName(common);
		return tree;
	}
}