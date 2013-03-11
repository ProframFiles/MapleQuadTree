package com.cpsc310.treespotter.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import com.cpsc310.treespotter.client.ClientTreeData;
import com.cpsc310.treespotter.client.SearchFieldID;
import com.cpsc310.treespotter.client.SearchQueryInterface;
import com.cpsc310.treespotter.client.TreeComment;
import com.cpsc310.treespotter.client.TreeDataService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;

public class TreeDataServiceImpl extends RemoteServiceServlet implements
		TreeDataService {
	private static final long serialVersionUID = 1L; 
	
	private static final Key LAST_USER_TREE_STAMP_KEY = KeyFactory.createKey("UserTreeUpdateStamp", "last user tree id");
	private static final Logger LOG = Logger.getLogger(TreeDataServiceImpl.class.getName());
	
	// as it says: we'll only ever return this many 
	private static int MAXRESULTS = 1000;

	public TreeDataServiceImpl(){
		LOG.setLevel(Level.FINER);
	
		//(aleksy) uncomment this to fetch data about street block locations on startup
		//it will only do the full parse if the current data isn't up to date
		//QueueFactory.getDefaultQueue().add(withUrl("/treespotter/tasks/streetblockupdate"));
		
		//and uncomment this to force tree data parsing on server start  
		//QueueFactory.getDefaultQueue().add(withUrl("/treespotter/import").method(TaskOptions.Method.GET)); 
	}
	
	@Override
	public void importFromSite(String url) {
		QueueFactory.getDefaultQueue().add(withUrl("/treespotter/tasks/streetblockupdate"));
		QueueFactory.getDefaultQueue().add(withUrl("/treespotter/import").method(TaskOptions.Method.GET));
	}

	@Override
	public ClientTreeData addTree(ClientTreeData info) {
		LOG.info("\n\trecieved call to create new user tree.");
		ClientTreeData return_tree = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try { 
			tx.begin();
			// find the last update stamp, if there is one
			Query last_update_query = pm.newQuery(UserTreeUpdateStamp.class, "key == id");
			last_update_query.setUnique(true);
			last_update_query.declareParameters("com.google.appengine.api.datastore.Key id");
			LOG.info("\n\tFetching last user id from datastore.");
			LOG.fine("\n\tAbout to execute query\n\t"+last_update_query.toString());
			
			UserTreeUpdateStamp last_stamp = (UserTreeUpdateStamp)last_update_query.execute(LAST_USER_TREE_STAMP_KEY);
			// create a new stamp if we didn't find one, die if we found a malformed stamp
			if(last_stamp == null){
				LOG.info("\n\tNo user tree adds found. This will be the first.");
				last_stamp = new UserTreeUpdateStamp(LAST_USER_TREE_STAMP_KEY);
			}
			else if(!last_stamp.getTreeID().toUpperCase().matches("U\\d+")){
				throw new RuntimeException("something is wrong with the stored last user id: \"" + last_stamp.getTreeID() +"\"");
			}
			else{
				LOG.info("\n\tFound: last id = \"" + last_stamp.getTreeID() + "\" added " + last_stamp.getTimeStamp() );
			}
			
			//increment the last ID
			int id_number = Integer.parseInt(last_stamp.getTreeID().substring(1)) + 1;
			
			//make the new tree, server style
			TreeData new_tree;
			if(info != null){
				new_tree = TreeFactory.makeTreeData(info, id_number);
			}
			else{
				throw new RuntimeException("Can't create an empty tree (null tree data)");
			}
			//persist the new tree
			pm.makePersistent(new_tree);

			
			//refresh the update stamp 
			last_stamp.setTreeID(new_tree.getID());
			last_stamp.updateTimeStamp();
			pm.makePersistent(last_stamp);
			tx.commit();
			LOG.info("\n\tDone adding new tree \"" +new_tree.getID() + "\"");
			
			//everything went better than expected, set return to non-null
			return_tree = TreeFactory.makeUserTreeData(new_tree);
		}
		catch (Exception e){
			LOG.severe("Unexpected exception during adding of user tree:\n\t\"" + e.getMessage() + "\"\n\tStack trace follows.");
			StringBuilder sb = new StringBuilder();
			for(StackTraceElement ste: e.getStackTrace()){
				sb.append("\n" + ste.toString());
				if(ste.getMethodName() == "addTree"){
					break;
				}
			}
			LOG.severe("StackTrace from this method:\n" + sb.toString() + "\n");
		}
		finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}
		
		return return_tree;
	}

	@Override
	public ClientTreeData getTreeData(String queryID, String userType) {
		ClientTreeData ret = null;
		LOG.fine("Trying to find tree with id " + queryID);
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(TreeData.class, "treeID == id");
			q.declareParameters("string id");
			q.setUnique(true); 
			
			LOG.fine("about to  make query: " + q.toString());
			TreeData query_result = (TreeData) q.execute(queryID);

			if (query_result != null) {
				LOG.info("tree " + queryID + " found, creating ClientTreeData");
				if (userType != null && userType.equals("user")) {
					ret = TreeFactory.makeUserTreeData(query_result);
				}
				if (userType != null && userType.equals("admin")) {
					ret = TreeFactory.makeAdminTreeData(query_result);
				}
			}
			else{
				LOG.info("tree " + queryID + " not found in DB");
			}
		} finally {
			pm.close();
		}
		return ret;
	}

	@Override
	public ArrayList<ClientTreeData> searchTreeData(SearchQueryInterface query) {
		LOG.setLevel(Level.FINER);
		ArrayList<ClientTreeData> results = null;
		
		if(query == null || query.getSearchParams().isEmpty()){
			LOG.info("recieved empty search query, returning.");
			return null;
		}
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			SearchQueryProcessor sqp = new SearchQueryProcessor(pm);
			
			Set<TreeData> result_set = sqp.executeNonSpatialQueries(query);
			Set<TreeData> spatial_set = sqp.executeSpatialQueries(query);
			if(spatial_set != null && result_set!=null){
				result_set.retainAll(spatial_set);
			}
			else if(result_set == null && spatial_set != null){
				result_set = spatial_set;
			}
			else if(result_set == null){
				result_set = new HashSet<TreeData>();
			}
			int total_results = result_set.size();
			LOG.info("\tFound " + total_results + " tree results in the DB");
			results = new ArrayList<ClientTreeData>();
			if (total_results > 0) {
				int result_count = 0;
				for (TreeData server_tree : result_set) {
					results.add(TreeFactory.makeUserTreeData(server_tree));
					result_count++;
					if(result_count > MAXRESULTS){
						LOG.warning("\n\tNumber of results exceeded maximum, returning first" + MAXRESULTS);
						break;
					}
				}
			}
		} 
		catch(Exception e){
			LOG.severe("Unexpected exception during search process:\n\t\"" + e.getMessage() + "\"\n\tReturning no results, stack trace follows.");
			StringBuilder sb = new StringBuilder();
			for(StackTraceElement ste: e.getStackTrace()){
				sb.append("\n" + ste.toString());
				if(ste.getMethodName() == "searchTreeData"){
					break;
				}
			}
			LOG.severe("StackTrace from this method:\n" + sb.toString() + "\n");
			results = null;
		}
		finally {
			pm.close();
		}
		
		return results;
	}

	@Override
	public ClientTreeData modifyTree(ClientTreeData info) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String flagTreeData(String treeID, String fieldName, String reason) {
		// TODO Auto-generated method stub
		return "Not Implemented yet";
	}

	@Override
	public ClientTreeData addTreeComment(String treeID, TreeComment comment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getSearchSuggestions(SearchFieldID field_id,
			String hint) {
		// TODO Auto-generated method stub
		return new ArrayList<String>();
	}

	@Override
	public String exportCSV(String csv) {
		// TODO Auto-generated method stub
		return null;
	}



}