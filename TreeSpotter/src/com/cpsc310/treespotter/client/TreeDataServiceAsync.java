package com.cpsc310.treespotter.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TreeDataServiceAsync {
  
  public void importFromSite(String url, AsyncCallback<Void> async);

  public void addTree(ClientTreeData info, AsyncCallback<ClientTreeData> async);
  
  public void getTreeData(String id, String userType, AsyncCallback<ClientTreeData> async);
  
  public void searchTreeData(SearchQueryInterface query, AsyncCallback<ArrayList<ClientTreeData>> async);

}