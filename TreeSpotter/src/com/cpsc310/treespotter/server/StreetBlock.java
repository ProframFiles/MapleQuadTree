package com.cpsc310.treespotter.server;
import java.util.List;


import com.cpsc310.treespotter.shared.LatLong;
import com.cpsc310.treespotter.shared.LatLongProvider;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class StreetBlock implements LatLongProvider {
	
	private int	blockStart;
	

	private int	blockEnd;
	

	private String streetName;

	private double	latitude = 0.0;

	private double	longitude = 0.0;
	
	public StreetBlock(){
		
	}
	
	public StreetBlock(String blockString){
		String block_string = blockString.trim();
		parseAddress(block_string);	
	}
	public String getBlockStart(){
		return Integer.toString(blockStart);
	}
	public String getBlockEnd(){
		return Integer.toString(blockEnd);
	}
	public void setBlockRange(int low, int high){
		blockEnd = high;
		blockStart = low;
	}
	public int getAddressTop()
	{
		return (blockEnd);
	}
	public int getAddressBottom()
	{
		return (blockStart);
	}
	public String getStreetName(){
		return streetName;
	}
	public LatLong getLatLong(){
		return new LatLong(latitude, longitude);
	}
	public double getLatitudeRadians(){
		return Math.toRadians(latitude);
	}
	public double getLongitudeRadians(){
		return Math.toRadians(longitude);
	}
	public double getLatitude(){
		return latitude;
	}
	public double getLongitude(){
		return longitude;
	}
	private void parseAddress(String block_string){
		int block_name_split = block_string.indexOf(' ');
		if(block_name_split == -1){
			throw new RuntimeException("Badly formed block string:\n\t\"" + block_string + "\"");
		}
		// the number part
		String address_part = block_string.substring(0, block_name_split);
		

		if(address_part.length() > 0 && address_part.matches("\\d+(-\\d+)?")){
			String[] address_range = address_part.split("-");
			int top_index = address_range.length - 1;
			blockStart = Integer.parseInt(address_range[0]);
			blockEnd = Integer.parseInt(address_range[top_index]);
		}
		else if(address_part.length() > 1 && address_part.charAt(0) == '-'){
			blockStart = 0;
			blockEnd = 10000000;
		}
		else{
			blockStart = -1;
			blockEnd =  -1;
			block_name_split = 0;
		}
		//the name part
		streetName = block_string.substring(block_name_split).trim();
	}
	
}
