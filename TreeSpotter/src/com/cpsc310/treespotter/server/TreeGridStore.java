/**
 * 
 */
package com.cpsc310.treespotter.server;

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
	@Override
	public String treeToString(TreeDataProvider tree) {
		int lat = Math.min(Math.max((int) (GRID_SIZE*((tree.getLatLong().getLatitude()-LAT_MIN)/LAT_RANGE)), 0), GRID_SIZE);
		int lon = Math.min(Math.max((int) (GRID_SIZE*((tree.getLatLong().getLongitude()-LON_MIN)/LON_RANGE)), 0), GRID_SIZE);
		return String.format("%1$2d_%2$2d",lat, lon);
	}

}
