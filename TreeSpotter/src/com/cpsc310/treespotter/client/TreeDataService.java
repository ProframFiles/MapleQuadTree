package com.cpsc310.treespotter.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("treedata")
public interface TreeDataService extends RemoteService {
  
  public void importFromSite(String url);

  public ClientTreeData addTree(ClientTreeData info);
  
  public ClientTreeData modifyTree(ClientTreeData info);
  
  public String flagTreeData(String treeID, String fieldName, String reason);
  
  public ClientTreeData addTreeComment(String treeID, TreeComment comment );
  
  public ClientTreeData getTreeData(String id, String userType);
  
  public ArrayList<ClientTreeData> searchTreeData(SearchQueryInterface query);
  
  public ArrayList<String> getSearchSuggestions(SearchFieldID field_id, String hint);

  public String exportCSV(String csv);
}