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
	
	private static final double DAY_MS = 86400000.0;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private int updateCount;
	
	@Persistent
	private Date timeStamp;
	
	@Persistent
	private long fileChecksum;
	
	public StreetBlockUpdateTimeStamp(Key key) {
		timeStamp = null;
		fileChecksum = 0;
		updateCount = 0;
		this.key = key;
	}
	
	public void Update(int records_read, long check_sum){
		timeStamp = new Date();
		updateCount = records_read;
		fileChecksum = check_sum;
	}
	
	public boolean isChecksumEqual(long check_sum){
		return (fileChecksum != 0 && check_sum!= 0 && check_sum == fileChecksum);
	}
	
	public double getDaysPassed(){
		Date now_time = new Date();
		return (now_time.getTime()-timeStamp.getTime())/DAY_MS;
	}
	
}
