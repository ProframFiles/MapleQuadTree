package com.cpsc310.treespotter.server;

import java.util.ArrayList;

import com.cpsc310.treespotter.client.ClientTreeData;
import com.cpsc310.treespotter.client.SearchQuery;
import com.cpsc310.treespotter.client.SearchQueryInterface;
import com.cpsc310.treespotter.client.TreeDataService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TreeDataServiceImpl extends RemoteServiceServlet implements
TreeDataService {
	public void importFromSite(String url) {

	}

	public void addTree(ClientTreeData info) {

	}
  
	public ClientTreeData getTreeData(String id, String userType) {
		return null;
	}
  

	public ArrayList<ClientTreeData> searchTreeData(SearchQueryInterface query) {
		// TODO Auto-generated method stub
		return null;
	}


}