package com.cpsc310.treespotter.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("treedata")
public interface TreeDataService extends RemoteService {
  
  public void importFromSite(String url);

  public ClientTreeData addTree(ClientTreeData info);
  
  public ClientTreeData getTreeData(String id, String userType);
  
  public ArrayList<ClientTreeData> searchTreeData(SearchQueryInterface query);
}