package com.cpsc310.treespotter.client;

/**
 * @author maple-quadtree
 * these are the enums that accompany each SearchParam that's sent to the server
 * for single strings, the search is case-insensitive, but otherwise exact.<br>
 * In other words, to do a keyword search for both maple AND birch, send over two search 
 * params: (KEYWORD, "maple"), (KEYWORD, "birch"), NOT 1 combined(KEYWORD, "maple birch")<br>
 * all string searches are case insensitive
 */
public enum SearchFieldID
{
	/**
	 * does an OR search over genus, species, neighbourhood, commonName,
	 *  cultivar, and street
	 */
	KEYWORD,
	  /**
	 * (a bit broken at the moment)
	 * Primary tree ID:<br> 
	 * Format: currently an integer in string form
	 */
	ID,
	/**
	 * find trees within a given neighbourhood<br>
	 * Format : exact string 
	 */
	NEIGHBOUR,
	/**
	 * Find trees within a given height range (in meters)<br>
	 * Format: ("%d-%d", range_bottom, range_top)
	 */
	HEIGHT,
	/**
	 * Find trees within a given diameter range (in cm)<br>
	 * Format: ("%d-%d",range_bottom, range_top)
	 */
	DIAMETER,
	/**
	 * tree genus<br>
	 * Format: exact string
	 */
	GENUS,
	/**
	 * tree species<br>
	 * Format: exact string
	 */
	SPECIES,
	/**
	 * tree common name<br>
	 * Format: exact string
	 */
	COMMON, 
	/**
	 * find trees within an address range<br>
	 * Format: ("%d-%d %s",low_address, high_address, street_name)
	 */
	ADDRESS,
	/**
	 * find trees around a given lat/long point<br>
	 * Format: ("%f,%f,%f",latitude, longitude, radius_meters)
	 */
	LOCATION
}