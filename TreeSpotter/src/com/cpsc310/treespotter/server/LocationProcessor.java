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
	public void ParseLocationFile(String file) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			
			XMLReader reader = XMLReaderFactory.createXMLReader();
			LocationKMLSAXHandler handler = new LocationKMLSAXHandler(pm);
			reader.setContentHandler(handler);

			reader.parse("./" + file);

		} catch (Exception e) {
			throw new RuntimeException("Parsing of " + file + " failed: " + e, e);
		} finally {
			pm.close();
		}
	}
}
