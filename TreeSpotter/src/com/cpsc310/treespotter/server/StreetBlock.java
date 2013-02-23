package com.cpsc310.treespotter.server;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable
public class StreetBlock {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key blockID;
	
	@Persistent
	private int	blockStart;
	
	@Persistent
	private int	blockEnd;
	
	@Persistent
	private String streetName;
	
	@Persistent
	private double	latitude;
	
	@Persistent
	private double	longitude;
	
	public StreetBlock(){
		
	}
	
	public StreetBlock(String blockString, List<Double> coords ){
		blockID = KeyFactory.createKey("StreetBlock", blockString);
		//TODO (aleksy) actually complete this
	}
	
	public String getBlockStart(){
		return Integer.toString(blockStart);
	}
	public String getBlockEnd(){
		return Integer.toString(blockEnd);
	}
	public String getStreetName(){
		return streetName;
	}
}
