package com.cpsc310.treespotter.client;

/**
 * @author maple-quadtree
 * for some reason even an enum has to be in it's own file in Java... I don't get it
 */
public enum SearchFieldID
{
	  KEYWORD, // use for uncategorized strings
	  ID,
	  NEIGHBOUR,
//	  these would work better as range searches, lower priority
//	  HEIGHT, 
//	  DIAMETER,
	  GENUS,
	  SPECIES,
	  COMMON,
	  LOCATION, // is this address or coordinates?
	  ADD_OTHERS_AS_NEEDED
}