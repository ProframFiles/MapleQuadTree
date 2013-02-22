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

	public void importFromSite(String url) {

	}

	public void addTree(ClientTreeData info) {

	}

	public ClientTreeData getTreeData(String id, String userType) {
		ClientTreeData ret = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {

			Query q = pm.newQuery(TreeData.class, "treeID == id");
			q.declareParameters("Key id");
			@SuppressWarnings("unchecked")
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

	public ArrayList<ClientTreeData> searchTreeData(SearchQueryInterface query) {
		ArrayList<ClientTreeData> results = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {

			Query q = makeDBQueryFromSearch(pm, query);

			@SuppressWarnings("unchecked")
			List<TreeData> tree_list = (List<TreeData>) q.execute();

			// tree list should only have length of 0 or 1
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

	private UserTreeData makeUserTreeData(TreeData tree_data) {
		// TODO: aleksy
		return new UserTreeData();
	}

	private AdminTreeData makeAdminTreeData(TreeData tree_data) {
		// TODO: aleksy
		return new AdminTreeData();
	}

	private Query makeDBQueryFromSearch(PersistenceManager pm, SearchQueryInterface search_params) {
		//TODO aleksy: implement extra search query enums once they're there
		StringBuilder sb = new StringBuilder();
		String prefix = "(";
		for (SearchParam param : search_params) {
			sb.append(prefix);
			switch (param.fieldID) {
			case KEYWORD:
				sb.append("( ");
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
				sb.append("\" )");
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
}