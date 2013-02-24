/**
 * 
 */
package com.cpsc310.treespotter.server;

import javax.jdo.PersistenceManager;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author maple-quadtree
 * 
 */
public class LocationProcessor {
	static public int ParseLocationFile(String file) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		int count = 0;
		try {
			LocationKMLSAXHandler handler = new LocationKMLSAXHandler(pm);
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(handler);
			reader.parse("./" + file);
			count = handler.getBlockCount();
		} catch (Exception e) {
			throw new RuntimeException("Parsing of " + file + " failed: " + e, e);
		} finally {
			pm.close();
		}
		return count;
	}
}
