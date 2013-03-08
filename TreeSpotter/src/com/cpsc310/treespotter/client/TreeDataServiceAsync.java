package com.cpsc310.treespotter.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TreeDataServiceAsync {
  
  public void importFromSite(String url, AsyncCallback<Void> async);

  public void addTree(ClientTreeData info, AsyncCallback<ClientTreeData> async);
  
  public void modifyTree(ClientTreeData info, AsyncCallback<ClientTreeData> async);
  
  public void flagTreeData(String treeID, String fieldName, String reason, AsyncCallback<String> async);
  
  public void getTreeData(String id, String userType, AsyncCallback<ClientTreeData> async);
  
  public void addTreeComment(String treeID, TreeComment comment,  AsyncCallback<ClientTreeData> async);
  
  public void searchTreeData(SearchQueryInterface query, AsyncCallback<ArrayList<ClientTreeData>> async);
  
  public void getSearchSuggestions(SearchFieldID field_id, String hint, AsyncCallback<ArrayList<String>> async);

}