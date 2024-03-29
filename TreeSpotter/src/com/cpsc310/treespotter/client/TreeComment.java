/**
 * 
 */
package com.cpsc310.treespotter.client;

import java.io.Serializable;

import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * @author maple-quadtree
 *
 */
public class TreeComment implements Serializable, Comparable<TreeComment> {
	private static final long serialVersionUID = 1L;
	
	public static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss ZZZZ yyyy";
	
	private long commentID;
	private String treeID;	// hopefully not redundant; set on client side
	private String user;
	private String date;
	private String commentText;
	
	public TreeComment() {
		
	}
	
	public TreeComment(String tid, String user, String date, String text){
		treeID = tid;
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
	
	public String getTreeID() {
		return treeID;
	}

	public void setTreeID(String treeID) {
		this.treeID = treeID;
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
	
	@Override
	public int compareTo(TreeComment c) {
		DateTimeFormat df = DateTimeFormat.getFormat(DATE_FORMAT);
		int comp = 0;
		try {
			comp = df.parse(date).compareTo(df.parse(c.getDate()));
		} catch (Exception e) {
			// TODO what to do with error?
		}
		return comp;
	}
	
}
