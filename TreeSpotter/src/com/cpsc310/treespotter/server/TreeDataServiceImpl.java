package com.cpsc310.treespotter.server;

import java.util.ArrayList;

import com.cpsc310.treespotter.client.ClientTreeData;
import com.cpsc310.treespotter.client.SearchQuery;
import com.cpsc310.treespotter.client.TreeDataService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TreeDataServiceImpl extends RemoteServiceServlet implements
TreeDataService {
  public void importFromSite(String url) {
    
  }

  public void addTree(ClientTreeData info) {
    
  }
  
  // UML said int, but shouldn't it be ClientTreeData for display?
  public ClientTreeData getTreeData(String id, String userType) {
    return null;
  }
  
  public ArrayList<ClientTreeData> searchTreeData(SearchQuery query) {
    return null;
  }


}