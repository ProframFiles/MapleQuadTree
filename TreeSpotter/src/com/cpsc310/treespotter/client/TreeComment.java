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
	private static final long serialVersionUID = 1L;
	
	private long commentID;
	private String user;
	private String date;
	private String commentText;
	
	public TreeComment() {
		
	}
	
	public TreeComment(long id, String user, String date, String text){
		commentID = id;
		this.user = user;
		this.date = date;
		commentText = text;
	}

	public long getCommentID() {
		return commentID;
	}

	public void setCommentID(long commentID) {
		this.commentID = commentID;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCommentText() {
		return commentText;
	}

	public void setCommentText(String commentText) {
		this.commentText = commentText;
	}
	
	
}
