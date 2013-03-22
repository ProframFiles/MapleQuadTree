package com.cpsc310.treespotter.server;
import static com.cpsc310.treespotter.server.OfyService.ofy;
import static com.cpsc310.treespotter.server.TreeDepot.treeDepot;
import static com.cpsc310.treespotter.server.CommentDepot.commentDepot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cpsc310.treespotter.client.SearchFieldID;
import com.cpsc310.treespotter.client.SearchParam;
import com.cpsc310.treespotter.client.SearchQueryInterface;
import com.cpsc310.treespotter.client.TreeComment;
import com.cpsc310.treespotter.client.TreeDataService;
import com.cpsc310.treespotter.shared.ISharedTreeData;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;

public class TreeDataServiceImpl extends RemoteServiceServlet implements
		TreeDataService {
	private static final long serialVersionUID = 1L; 
	
	private static final String LAST_USER_TREE_STAMP_ID = "last user tree id";
	private static final Logger LOG = Logger.getLogger(TreeDataServiceImpl.class.getName());
	

	public TreeDataServiceImpl(){
		LOG.setLevel(Level.FINER);
	}
	
	@Override
	public void importFromSite(String options) {
		TaskOptions opt = withUrl(DataUpdater.TASK_URL);
		if(options != null && options.length()>0){
			LOG.info("setting option \"" +options+ "\"");
			if(options.equalsIgnoreCase("force tasks")){
				opt = opt.param("force tasks","true");
			}
			else if(options.startsWith("job=") && options.length() > 4){
				opt.param("job", options.substring(4));
			}
			else{
				String[] tasks = options.split(",");
				for(String task: tasks){
					opt = opt.param("add task", task.trim());
					LOG.info("setting task \"" +task+ "\"");
				}
			}
				
		}
		QueueFactory.getDefaultQueue().add(opt);
	}

	@Override
	public ISharedTreeData addTree(ISharedTreeData info) {
		LOG.info("\n\trecieved call to create new user tree.");
		ISharedTreeData return_tree = null;
		try { 
			LOG.info("\n\tFetching last user id from datastore.");
			UserTreeUpdateStamp last_stamp = ofy().transact(new Work<UserTreeUpdateStamp>() {
			    public UserTreeUpdateStamp run() {
			    	return ofy().load().key(Key.create(UserTreeUpdateStamp.class, LAST_USER_TREE_STAMP_ID)).getValue();
			    }
			});
			
			
			// create a new stamp if we didn't find one, die if we found a malformed stamp
			if(last_stamp == null){
				LOG.info("\n\tNo user tree adds found. This will be the first.");
				last_stamp = new UserTreeUpdateStamp(LAST_USER_TREE_STAMP_ID);
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
			
			treeDepot().putTree(new_tree);
			
			//refresh the update stamp 
			final UserTreeUpdateStamp new_stamp = new UserTreeUpdateStamp(LAST_USER_TREE_STAMP_ID, new_tree.getID());
			
			ofy().transact(new VoidWork() {
				public void vrun() {
					ofy().save().entity(new_stamp).now();
				}
			});
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
		
		return return_tree;
	}

	@Override
	public ISharedTreeData getTreeData(String queryID, String userType) {
		TreeData tree = treeDepot().getTreeByID(queryID);
		ISharedTreeData ret = null;
		if(tree != null){
			ret = TreeFactory.makeUserTreeData(tree);
		}
		return ret;
	}

	@Override
	public ArrayList<ISharedTreeData> searchTreeData(SearchQueryInterface query) {
		LOG.setLevel(Level.FINER);
		ArrayList<ISharedTreeData> results = null;
		
		if(query == null || query.getSearchParams().isEmpty()){
			LOG.info("recieved empty search query, returning.");
			return null;
		}
		
		try {
			TreeRequest req = treeDepot().newRequest();
			req.setResultsRange(0, query.getNumResults());
			for(SearchParam sp: query){
				LOG.info("Processing param\n\t" + sp.fieldID + " = \"" + sp.value + "\"");
				if(sp.fieldID == SearchFieldID.SPECIES){
					req.onlyTreesWithSpecies(sp.value.toUpperCase());
				}
				else if(sp.fieldID == SearchFieldID.KEYWORD){
					req.onlyTreesWithKeyword(sp.value.toUpperCase());
				}
				else if(sp.fieldID == SearchFieldID.ADDRESS){
					StreetBlock address_block = new StreetBlock(sp.value);
					req.onlyTreesWithStreet(address_block.getStreetName().toUpperCase());
					req.onlyTreesWithStreetNumber(address_block.getAddressBottom(), address_block.getAddressTop() );
				}
				else if(sp.fieldID == SearchFieldID.GENUS){
					req.onlyTreesWithGenus(sp.value.toUpperCase());
				}
				else if(sp.fieldID == SearchFieldID.COMMON){
					req.onlyTreesWithCommonName(sp.value.toUpperCase());
				}
				else if(sp.fieldID == SearchFieldID.NEIGHBOUR){
					req.onlyTreesWithNeighbourhood(sp.value.toUpperCase());
				}
			}
			Collection<TreeData> trees = req.fetch();
			LOG.info("\n\tFound: " + trees.size() + " trees matching query");
			results = new ArrayList<ISharedTreeData>();
			int counter = 0;
			
			for (TreeData server_tree : trees) {
				query.getResultsOffset();
				if(counter >= query.getResultsOffset()){
					results.add(TreeFactory.makeUserTreeData(server_tree));
				}
				if(results.size() >= query.getNumResults() ){
					break;
				}
				counter ++;
			}
			
		} 
		catch(Exception e){
			LOG.severe("Unexpected exception during search process:\n\t\"" + e.getMessage() + "\"\n\tReturning no results, stack trace follows.");
			StringBuilder sb = new StringBuilder();
			StackTraceElement[] ste_array = e.getStackTrace();
			if(ste_array!=null){
				for(StackTraceElement ste: e.getStackTrace()){
					sb.append("\n" + ste.toString());
					if(ste.getMethodName() == "searchTreeData"){
						break;
					}
				}
			}
			else{
				sb.append("No stackTrace available");
			}
			LOG.severe("StackTrace from this method:\n" + sb.toString() + "\n");
			results = null;
		}
		
		return results;
	}

	@Override
	public ISharedTreeData modifyTree(ISharedTreeData info) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String flagTreeData(String treeID, String fieldName, String reason) {
		// TODO Auto-generated method stub
		return "Not Implemented yet";
	}

	@Override
	public ArrayList<TreeComment> addTreeComment(String treeID, TreeComment comment) {
		return commentDepot().putComment(treeID, comment);
	}
	
	@Override
	public ArrayList<TreeComment> getTreeComments(String treeID) {
		return commentDepot().getComments(treeID);
	}

	@Override
	public ArrayList<String> getSearchSuggestions(SearchFieldID field_id, String hint) {
		LOG.info("Recieved request for search suggestions: \"" + field_id + "\" hint = \"" +hint+ "\"");
		ArrayList<String> ret = new ArrayList<String>();
		Set<String> all_set = null;
		if(field_id == SearchFieldID.SPECIES){
			all_set = treeDepot().getSpeciesSet();
		}
		else if(field_id == SearchFieldID.COMMON){
			all_set = treeDepot().getCommonNameSet();
		}
		else if(field_id == SearchFieldID.NEIGHBOUR){
			all_set = treeDepot().getNeighbourhoodSet();
		}
		else if(field_id == SearchFieldID.GENUS){
			all_set = treeDepot().getGenusSet();
		}
		else if(field_id == SearchFieldID.ADDRESS){
			all_set = treeDepot().getStreetSet();
		}
		else if(field_id == SearchFieldID.KEYWORD){
			all_set = treeDepot().getKeywordSet();
		}
		LOG.info("Found " + all_set.size() + " suggestions");
		if(all_set != null && hint != null && hint.length() > 0)
		{
			LOG.info("Now filtering list");
			Pattern regex = Pattern.compile(hint.toUpperCase());
			for(String s: all_set){
				Matcher matcher = regex.matcher(s);
				if(matcher.find()){
					ret.add(s);
				}
			}
		}
		else if(all_set != null){
			ret.addAll(all_set);
		}
		LOG.info("returning " + ret.size() + " suggestions");
		return ret;
	}

	@Override
	public String exportCSV(String csv) {
		// TODO Auto-generated method stub
		return null;
	}



}