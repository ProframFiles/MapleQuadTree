package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.SortedMap;

import com.cpsc310.treespotter.shared.CSVFile;
import com.cpsc310.treespotter.shared.ISharedTreeData;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TreeDataServiceAsync {
  
  public void importFromSite(String url, AsyncCallback<Void> async);

  public void addTree(ISharedTreeData info, AsyncCallback<ISharedTreeData> async);
  
  public void modifyTree(ISharedTreeData info, AsyncCallback<ISharedTreeData> async);
  
  public void flagTreeData(String treeID, String fieldName, String reason, AsyncCallback<String> async);
  
  public void getTreeFlags(String treeID, AsyncCallback<ArrayList<String>> async);
  
  public void getTreeData(String id, String userType, AsyncCallback<ISharedTreeData> async);
  
  public void addTreeComment(String treeID, TreeComment comment,  AsyncCallback<ArrayList<TreeComment>> async);
  
  public void getTreeComments(String treeID, AsyncCallback<ArrayList<TreeComment>> async);
  
  public void searchTreeData(SearchQueryInterface query, AsyncCallback<ArrayList<ISharedTreeData>> async);
  
  public void getSearchSuggestions(SearchFieldID field_id, String hint, AsyncCallback<ArrayList<String>> async);

  public void getCSVFiles(AsyncCallback<ArrayList<CSVFile>> async);

  public void parseCSV(CSVFile csv, AsyncCallback<Void> async);

  public void deleteCSV(CSVFile csv, AsyncCallback<Void> async);

  public void getBlobstoreUploadUrl(AsyncCallback<String> async);

  public void getTreeImages(String treeID, AsyncCallback<ArrayList<String>> callback);

  public void getFlaggedTreeIDs(AsyncCallback<ArrayList<String>> callback);
  
  public void getTreeFlagData(String treeID, AsyncCallback<SortedMap<String, String>> callback);
  
  public void clearTreeFlags(String treeID, AsyncCallback<Void> callback);
}