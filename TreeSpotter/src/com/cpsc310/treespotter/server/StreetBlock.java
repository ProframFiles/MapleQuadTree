package com.cpsc310.treespotter.server;
import java.util.List;
import java.util.logging.Logger;

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
	private double	latitude = 0.0;
	
	@Persistent
	private double	longitude = 0.0;
	
	public StreetBlock(){
		
	}
	
	public StreetBlock(String placemarkID, String blockString, List<Double> coords){
		String block_string = blockString.trim();
		blockID = KeyFactory.createKey("StreetBlock",placemarkID + block_string);
		parseAddress(block_string);
		parseCoords(coords);	
	}
	public StreetBlock(String blockString){
		String block_string = blockString.trim();
		parseAddress(block_string);	
		blockEnd -= 100;
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
	public double getLatitudeRadians(){
		return Math.toRadians(latitude);
	}
	public double getLongitudeRadians(){
		return Math.toRadians(longitude);
	}
	private void parseAddress(String block_string){
		int block_name_split = block_string.indexOf(' ');
		if(block_name_split == -1){
			throw new RuntimeException("Badly formed block string:\n\t\"" + block_string + "\"");
		}
		// the number part
		String address_part = block_string.substring(0, block_name_split);
		

		if(address_part.length() > 0 && address_part.matches("\\d+(|-\\d+)")){
			String[] address_range = address_part.split("-");
			int top_index = address_range.length - 1;
			blockStart = Integer.parseInt(address_range[0]);
			blockEnd = Integer.parseInt(address_range[top_index]) + 100;
		}
		else if(address_part.length() > 0 && address_part.charAt(0) == '-'){
			blockStart = 0;
			blockEnd = 10000000;
		}
		else{
			blockStart = 0;
			blockEnd =  10000000;
			block_name_split = 0;
		}
		//the name part
		streetName = block_string.substring(block_name_split).trim();
	}
	private void parseCoords(List<Double> coords ){
		//TODO (aleksy) make this fancier than a simple mean
				if(coords.size() == 0 || coords.size()%3 != 0){
					throw new RuntimeException("coordinate list must have 3n coordinates, n > 0");
				}
				
				int index = 0;
				double lat_sum = 0.0;
				double long_sum = 0.0;
				double divisor = 1.0;
				for(double coord: coords){
					if(index == 3){
						index = 0;
						divisor += 1.0;
					}
					if(index == 0){
						long_sum += coord;
					}
					else if(index == 1){
						lat_sum += coord;
					}
					index++;
				}
				latitude = lat_sum /= divisor;
				longitude = long_sum /= divisor;
	}
}
