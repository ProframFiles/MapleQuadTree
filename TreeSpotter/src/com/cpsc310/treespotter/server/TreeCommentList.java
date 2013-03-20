/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.util.ArrayList;

import com.cpsc310.treespotter.client.TreeComment;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Serialize;

/**
 * @author maple-quadtree
 *
 */
@Entity
public class TreeCommentList {
	@Id private String id;
	@Serialize private ArrayList<TreeComment> comments;
	
	TreeCommentList(){
		
	}
	
	TreeCommentList(String treeID){
		id = treeID;
		comments = new ArrayList<TreeComment>();
	}
	
	ArrayList<TreeComment> getComments(){
		return comments;
	}
	
	public void addComment(TreeComment comment){
		if(comment != null){
			comments.add(comment);
		}
	}
	
}
