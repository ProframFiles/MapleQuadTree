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
public class StreetBlockUpdateTimeStamp {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private int updateCount;
	
	@Persistent
	private Date timeStamp;
	
	public StreetBlockUpdateTimeStamp(Key key, int count) {
		timeStamp = new Date();
		this.key = key;
		updateCount = count;
	}
	
}
