package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.SortedMap;

import com.cpsc310.treespotter.shared.CSVFile;
import com.cpsc310.treespotter.shared.ISharedTreeData;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("treedata")
public interface TreeDataService extends RemoteService {
  
  public void importFromSite(String url);

  public ISharedTreeData addTree(ISharedTreeData info);
  
  public ISharedTreeData modifyTree(ISharedTreeData info);
  
  public String flagTreeData(String treeID, String fieldName, String reason);
  
  public ArrayList<String> getTreeFlags(String treeID);
  
  public ArrayList<TreeComment> addTreeComment(String treeID, TreeComment comment );
  
  public ArrayList<TreeComment> getTreeComments(String treeID);
  
  public ISharedTreeData getTreeData(String id, String userType);
  
  public ArrayList<ISharedTreeData> searchTreeData(SearchQueryInterface query);
  
  public ArrayList<String> getSearchSuggestions(SearchFieldID field_id, String hint);

  public ArrayList<CSVFile> getCSVFiles();
  
  public void parseCSV(CSVFile csv);
  
  public void deleteCSV(CSVFile csv);
  
  public String getBlobstoreUploadUrl();
  
  public ArrayList<String> getTreeImages(String treeID);

  public ArrayList<String> getFlaggedTreeIDs();

  public SortedMap<String, String> getTreeFlagData(String treeID);

  public void clearTreeFlags(String TreeID);
}