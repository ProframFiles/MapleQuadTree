package com.cpsc310.treespotter.server;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.cpsc310.treespotter.client.AdminTreeData;
import com.cpsc310.treespotter.client.ClientTreeData;
import com.cpsc310.treespotter.client.SearchParam;
import com.cpsc310.treespotter.client.SearchQueryInterface;
import com.cpsc310.treespotter.client.TreeDataService;
import com.cpsc310.treespotter.client.UserTreeData;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TreeDataServiceImpl extends RemoteServiceServlet implements
		TreeDataService {
	private static final long serialVersionUID = 1L;

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
			q.declareParameters("Key id");
			@SuppressWarnings("unchecked")
			//TODO: (aleksy) make it so this id thing makes sense: string? key? int?
			List<TreeData> tree_list = (List<TreeData>) q.execute(id);

			// tree list should only have length of 0 or 1
			if (tree_list.size() == 1) {
				if (userType != null && userType.equals("user")) {
					ret = makeUserTreeData(tree_list.get(0));
				}
				if (userType != null && userType.equals("admin")) {
					ret = makeAdminTreeData(tree_list.get(0));
				}
			}

			else if (tree_list.size() > 1) {
				// TODO aleksy
				// this wouldn't be good, throw something?
			}
		} finally {
			pm.close();
		}
		return ret;
	}

	@Override
	public ArrayList<ClientTreeData> searchTreeData(SearchQueryInterface query) {
		ArrayList<ClientTreeData> results = null;
		
		// some dummy test data
		results = new ArrayList<ClientTreeData>();
		results.add(makeUserTreeData(makeTestTree("JIM", 1234)));
		results.add(makeUserTreeData(makeTestTree("BOB", 2234)));
		results.add(makeUserTreeData(makeTestTree("MARY", 2224)));
	
		// this is a bit broken right now, thanks to OR's not working
		/*
		 * PersistenceManager pm = PMF.get().getPersistenceManager();
		try {

			Query q = makeDBQueryFromSearch(pm, query);

			@SuppressWarnings("unchecked")
			List<TreeData> tree_list = (List<TreeData>) q.execute();

			if (tree_list.size() > 0) {
				results = new ArrayList<ClientTreeData>();
				for (TreeData server_tree : tree_list) {
					results.add(makeUserTreeData(server_tree));
				}
			}
		} finally {
			pm.close();
		}
		*/
		return results;
	}

	private UserTreeData makeUserTreeData(TreeData tree_data) {
		UserTreeData user_data = new UserTreeData();
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
		// TODO: aleksy
		return new AdminTreeData();
	}

	private Query makeDBQueryFromSearch(PersistenceManager pm, SearchQueryInterface search_params) {
		//TODO aleksy: implement extra search query enums once they're there
		StringBuilder sb = new StringBuilder();
		String prefix = "( ";
		for (SearchParam param : search_params) {
			sb.append(prefix);
			switch (param.fieldID) {
			case KEYWORD:
				sb.append("species == \"");
				sb.append(param.value);
				sb.append("\" || ");
				sb.append("stdStreet == \"");
				sb.append(param.value);
				sb.append("\" || ");
				sb.append("commonName == \"");
				sb.append(param.value);
				sb.append("\" || ");
				sb.append("genus == \"");
				sb.append(param.value);
				sb.append("\" || ");
				sb.append("street == \"");
				sb.append(param.value);
				sb.append("\" || ");
				sb.append("cultivar == \"");
				sb.append(param.value);
				sb.append("\" ");
				break;
			case LOCATION:
				sb.append(makeLocationQueryString(param.value));
				break;
			case SPECIES:
				sb.append("species == \"");
				sb.append(param.value);
				sb.append('"');
				break;
			default:
				break;
			}

			sb.append(" )");
			prefix = " && (";
		}

		return pm.newQuery(TreeData.class, sb.toString());
	}

	private String makeLocationQueryString(String location) {
		return location;
	}
	private TreeData makeTestTree(String common, int id){
		TreeData tree = new TreeData("user", 1337);
		tree.setSpecies("AFAKESPECIES");
		tree.setStreet("THE CRESCENT");
		tree.setNeighbourhood("THE BRONX");
		tree.setCultivar("GILDED LILY");
		tree.setGenus("RUGOSA");
		tree.setCommonName(common);
		return tree;
	}
}