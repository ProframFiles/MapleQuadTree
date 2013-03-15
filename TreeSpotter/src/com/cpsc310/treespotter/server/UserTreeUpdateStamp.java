/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.util.Date;


import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * @author maple-quadtree
 *
 */
@Entity
public class UserTreeUpdateStamp {

	@Id private String key;
	
	private String treeID;
	Date timeStamp;
	
	// for objectify
	@SuppressWarnings("unused")
	private UserTreeUpdateStamp(){
		
	}
	public UserTreeUpdateStamp(String key) {
		timeStamp = new Date();
		treeID = "U0";
		this.key = key;
	}
	public UserTreeUpdateStamp(String key, String id) {
		timeStamp = new Date();
		treeID = id;
		this.key = key;
	}
	/**
	 * @return the treeID
	 */
	public String getTreeID() {
		return treeID;
	}

	/**
	 * @param treeID the treeID to set
	 */
	public void setTreeID(String treeID) {
		this.treeID = treeID;
	}

	/**
	 * @return the timeStamp
	 */
	public Date getTimeStamp() {
		return timeStamp;
	}
	/**
	 * Set the timestamp to now
	 */
	public void updateTimeStamp(){
		timeStamp = new Date();
	}
	
	
	
}
