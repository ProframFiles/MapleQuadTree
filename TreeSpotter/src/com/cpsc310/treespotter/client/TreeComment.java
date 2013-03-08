/**
 * 
 */
package com.cpsc310.treespotter.client;

import java.io.Serializable;

/**
 * @author maple-quadtree
 *
 */
public class TreeComment implements Serializable {
	private String commentText;
	private long commentID;
	// please fill in stuff as you see fit
	// user ?
	// comment date ?
	
	private static final long serialVersionUID = 1L;
	public TreeComment(){
		
	}
	public String getCommentText() {
		return commentText;
	}
	public void setCommentText(String commentText) {
		this.commentText = commentText;
	}

}
