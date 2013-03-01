/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/**
 * @author maple-quadtree
 *
 */
@PersistenceCapable
public class UserTreeUpdateStamp {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private String treeID;
	
	@Persistent
	Date timeStamp;
	
	public UserTreeUpdateStamp(Key key) {
		timeStamp = new Date();
		treeID = "U0";
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
