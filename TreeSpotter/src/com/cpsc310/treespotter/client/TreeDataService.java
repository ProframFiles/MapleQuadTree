package com.cpsc310.treespotter.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("treedata")
public interface TreeDataService extends RemoteService {
  
  public void importFromSite(String url);

  public void addTree(ClientTreeData info);
  
  // UML said int, but shouldn't it be ClientTreeData for display?
  public ClientTreeData getTreeData(String id, String userType);
  
  public ArrayList<ClientTreeData> searchTreeData(SearchQueryInterface query);
}