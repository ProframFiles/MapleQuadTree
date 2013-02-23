package com.cpsc310.treespotter.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.cpsc310.treespotter.client.AdminTreeData;
import com.cpsc310.treespotter.client.ClientTreeData;
import com.cpsc310.treespotter.client.SearchParam;
import com.cpsc310.treespotter.client.SearchQueryInterface;
import com.cpsc310.treespotter.client.TreeDataService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TreeDataServiceImpl extends RemoteServiceServlet implements
		TreeDataService {
	private static final long serialVersionUID = 1L;
	private static double latRange = 1.0;
	private static double longRange = 1.0;

	public TreeDataServiceImpl(){
		//place some test trees in the database
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(makeTestTree("JIM", 1));
			pm.makePersistent(makeTestTree("BOB", 2));
			pm.makePersistent(makeTestTree("MARY", 3));
			pm.makePersistent(makeTestTree("RASTAPOUPOULOS", 4));
		}
		finally{
			pm.close();
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
			Key lookup_key = KeyFactory.createKey("user", Integer.parseInt(id.trim()));
			
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

			Query q = makeDBQueryFromSearch(pm, query);

			@SuppressWarnings("unchecked")
			Collection<TreeData> tree_list = (Collection<TreeData>)q.execute();

			if (tree_list.size() > 0) {
				results = new ArrayList<ClientTreeData>();
				for (TreeData server_tree : tree_list) {
					results.add(makeUserTreeData(server_tree));
				}
			}
			else{
				//TODO: get rid of this test stuff
				results = new ArrayList<ClientTreeData>();
				results.add(makeUserTreeData(makeTestTree("PERIWINKLE", 1234)));
				results.add(makeUserTreeData(makeTestTree("PAUL", 2234)));
				results.add(makeUserTreeData(makeTestTree("PETER", 2224)));
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
		int commaLocation = location.indexOf(',');
		double latitude = Double.parseDouble(location.substring(0, commaLocation));
		double longitude = Double.parseDouble(location.substring(commaLocation+1));
		Query blockQuery = pm.newQuery(StreetBlock.class
				, "latitude <= " + Double.toString(latitude + latRange) + "&&"  
				+ "latitude >= " + Double.toString(latitude - latRange) + "&&"
				+ "latitude <= " + Double.toString(longitude + longRange) + "&&"
				+ "latitude >= " + Double.toString(longitude - longRange));
		@SuppressWarnings("unchecked")
		Collection<StreetBlock> block_list = (Collection<StreetBlock>)blockQuery.execute();
		if(block_list.size() > 0){
			//TODO tune the lat+ long ranges and sort this list based on distances, 
			//rather than just taking the first value
			StreetBlock street_block = block_list.iterator().next();
			return "(civicNumber >= " +  street_block.getBlockStart() 
					+ " && civicNumber <= " + street_block.getBlockStart()
					+ " && street == \"" +street_block.getStreetName()+ "\")";
		}
		return "";
	}
	private TreeData makeTestTree(String common, int id){
		TreeData tree = new TreeData("test", id);
		tree.setSpecies("AFAKESPECIES");
		tree.setStreet("THE CRESCENT");
		tree.setNeighbourhood("THE BRONX");
		tree.setCultivar("GILDED LILY");
		tree.setGenus("RUGOSA");
		tree.setCommonName(common);
		return tree;
	}
}