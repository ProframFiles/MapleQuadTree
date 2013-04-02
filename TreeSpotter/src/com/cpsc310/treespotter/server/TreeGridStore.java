/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.util.HashSet;
import java.util.Set;

import com.cpsc310.treespotter.shared.LatLong;

/**
 * @author maple-quadtree
 *
 */
public class TreeGridStore implements TreeStringProvider{
	static final int GRID_SIZE = 64;
	static final double LAT_MAX = 49.2943;
	static final double LAT_MIN = 49.2011;
	static final double LAT_RANGE = LAT_MAX-LAT_MIN;
	static final double LON_MAX = -123.0237;
	static final double LON_MIN = -123.2244;
	static final double LON_RANGE = LON_MAX-LON_MIN;
	public static Set<String> getAllBinsWithin(LatLong southWest, LatLong northEast){
		Set<String> ret = new HashSet<String>();
		double lat_spacing = LAT_RANGE/GRID_SIZE;
		double lat_min = LAT_MIN + lat_spacing;
		while(lat_min < southWest.getLatitude()){
			lat_min += lat_spacing;
		}
		lat_min -= lat_spacing;
		double lat_max = LAT_MAX - lat_spacing;
		while(lat_max > northEast.getLatitude()){
			lat_max -= lat_spacing;
		}
		lat_max += lat_spacing;
		double lon_spacing = LON_RANGE/GRID_SIZE;
		double lon_min = LON_MIN + lon_spacing;
		while(lon_min < southWest.getLongitude()){
			lon_min += lon_spacing;
		}
		lon_min -= lon_spacing;
		double lon_max = LON_MAX - lon_spacing;
		while(lon_max > northEast.getLongitude()){
			lon_max -= lon_spacing;
		}
		lon_max += lon_spacing;
		
		for(double lat = lat_min + lat_spacing/2 ; lat < lat_max; lat+= lat_spacing){
			for(double lon = lon_min + lon_spacing/2 ; lon < lon_max; lon+= lon_spacing){
				ret.add(latLongToString(new LatLong(lat, lon)));
			}
		}
		return ret;
	}
	
	@Override
	public String treeToString(TreeDataProvider tree) {
		return latLongToString(tree.getLatLong());
	}
	
	static private String latLongToString(LatLong ll){
		int lat = 0;
		int lon = 0;
		if(ll != null ){
			lat = Math.min(Math.max((int) (GRID_SIZE*((ll.getLatitude()-LAT_MIN)/LAT_RANGE)), 0), GRID_SIZE);
			lon = Math.min(Math.max((int) (GRID_SIZE*((ll.getLongitude()-LON_MIN)/LON_RANGE)), 0), GRID_SIZE);
		}
		return String.format("%1$2d_%2$2d",lat, lon);
	}

}
