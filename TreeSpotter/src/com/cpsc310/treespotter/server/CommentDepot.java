/**
 * 
 */
package com.cpsc310.treespotter.server;

import static com.cpsc310.treespotter.server.OfyService.ofy;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import com.cpsc310.treespotter.client.TreeComment;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;

/**
 * @author maple-quadtree
 *
 */
@Entity
@Cache
public class CommentDepot {
	private static final Logger LOG = Logger.getLogger("Tree");
	static CommentDepot instance;
	@Id String id;
	@Unindex Map<String, Ref<TreeCommentList>> commentRefs;
	long lastCommentID;
	
	public static void saveDepotState(final CommentDepot depot){
		ofy().transact(new VoidWork() {
		    public void vrun() {
		    	ofy().save().entity(depot);
		    }
		});
	}
	
	static CommentDepot commentDepot(){
		return commentDepot("TreeDepot");
	}
	static private synchronized CommentDepot commentDepot(final String id){
		if( instance != null){
			return instance;
		}
		instance = ofy().transact(new Work<CommentDepot>() {
		    public CommentDepot run() {
		    	
		    	CommentDepot depot =  ofy().load().key(Key.create(CommentDepot.class, id)).getValue();
		    	
		    	return depot;
		    }
		});
		
		if(instance == null){
			instance = new CommentDepot(id);
			saveDepotState(instance);
		}
		
		return instance; 
	}

	CommentDepot(){
		
	}
	
	CommentDepot(String id){
		this.id = id;
		commentRefs = new HashMap<String, Ref<TreeCommentList>>();
		lastCommentID = 0L;
	}
	
	synchronized String generateCommentID(TreeComment comment){
		MessageDigest md;
		String algorithm = "SHA1"; 
		try {
			md = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("No such algorithm: \""+ algorithm + "\"", e);
		}
		md.update(comment.getTreeID().getBytes());
		md.update(comment.getDate().getBytes());
		md.update(comment.getCommentText().getBytes());
		String hex = (new HexBinaryAdapter()).marshal(md.digest());
		return hex;
	}
	public synchronized TreeCommentList getCommentList(final String treeID){
		final String lookupID = getMungedID(treeID);
		TreeCommentList ret = ofy().transact(new Work<TreeCommentList>() {
		    public TreeCommentList run() {
		    	Ref<TreeCommentList> comment_ref = commentRefs.get(lookupID);
		    	TreeCommentList ret;
		    	if(comment_ref == null){
		    		ret = new TreeCommentList(lookupID);
		    		ofy().save().entity(ret);
		    		commentRefs.put(lookupID, Ref.create(ret));
		    		saveDepotState(instance);
		    	}
		    	else{
		    		ofy().load().ref(comment_ref);
		    		ret = comment_ref.get();
		    	}
		    	return ret;
		    }
		});
		return ret;
	}
	public ArrayList<TreeComment> putComment(final String treeID, final TreeComment comment){
		Date date = new Date();
		comment.setDate(date.toString());
		String lookupID = getMungedID(treeID);
		final TreeCommentList comment_list = getCommentList(lookupID);
		ArrayList<TreeComment> ret = ofy().transact(new Work<ArrayList<TreeComment>>() {
		    public ArrayList<TreeComment> run() {
		    	comment_list.addComment(comment);
		    	ofy().save().entity(comment_list);
		    	saveDepotState(instance);
		    	return comment_list.getComments();
		    }
		});
		
		return ret;
		
	}

	/**
	 * @param treeID
	 * @return
	 */
	private String getMungedID(final String treeID) {
		String lookupID = treeID;
		if(lookupID !=null && treeID.startsWith("M")){
			lookupID = "V" + treeID.substring(1);
		}
		return lookupID;
	}

	
	ArrayList<TreeComment> getComments(String treeID){
		if(commentRefs==null){
			commentRefs = new HashMap<String, Ref<TreeCommentList>>();
			LOG.warning("Comment depot was not properly persited last time, hopefully everything is ok...");
		}
		String lookupID = getMungedID(treeID);
		Ref<TreeCommentList> comment_ref = commentRefs.get(lookupID);
		
		if(comment_ref != null){
			ofy().load().ref(comment_ref);
			TreeCommentList list = comment_ref.get();
			return list.getComments();
		}
		
		return new ArrayList<TreeComment>();
		
	}
}
