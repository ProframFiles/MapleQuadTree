package com.cpsc310.treespotter.server;

import java.util.ArrayList;

import javax.jdo.PersistenceManager;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LocationKMLSAXHandler extends DefaultHandler {

	PersistenceManager cachedPM;

	private boolean inPlacemark = false;
	private boolean inName = false;
	private boolean inCoordinates = false;
	private ArrayList<Double> blockCoords = new ArrayList<Double>();
	private String blockName = null;

	/**
	 * Construct a new LocationKMLSAXHandler <br>
	 * <br>
	 * <b>Modifies:</b><br>
	 * Modifies this<br>
	 * <br>
	 * <b>Effects:</b> <br>
	 * creates a new LocationKMLSAXHandler
	 */
	public LocationKMLSAXHandler(PersistenceManager pm) {
		super();
		cachedPM = pm;
		resetState();
	}


	@Override
	/**
	 * Called by the SAX parser at the start of each tag Element. 
	 * 
	 * @param uri - unused
	 * @param localname - unused
	 * @param qName - qualified element (tag) name
	 * @param attr - Attributes object associated with tag
	 */
	public void startElement(String uri, String localName, String qName, Attributes attr)  {
		if(qName == null) return;
		if(!inPlacemark && qName.equals("Placemark")){
			inPlacemark = true;
		}
		else if(inPlacemark && qName.equals("name")){
			inName = true;
		}
		else if(inPlacemark && qName.equals("coordinates")){
			inCoordinates = true;
		}
	}

	@Override
	/**
	 * Called by the SAX parser when processing tag contents<br>
	 * <br>
	 * 
	 * @param char, content character array
	 * @param start, index of first relevant character in chars
	 * @param length, number of relevant characters after start
	 */
public void characters(char ch[], int start, int length) throws SAXBadDataException {
		if(inName){
			blockName = new String(ch, start, length);
		}
		else if(inCoordinates){
			int stringStart = start;
			for (int i = start; i < start+length; i++) {
				if(ch[i] == ','){
					blockCoords.add(Double.parseDouble(new String(ch, stringStart, i-stringStart )));
					stringStart = i + 1;
				}
			}
			if (stringStart < start+ length){
				blockCoords.add(Double.parseDouble(new String(ch, stringStart, start+length-stringStart )));
			}
		}
		
	}

	/**
	 * Called by the SAX parser at the end of an element tag. <br>
	 * <br>
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName == null) return;
		if( qName.equals("Placemark") ){
			inPlacemark = false;
			checkCreateBlockConditions();
			StreetBlock streetBlock = new StreetBlock(blockName, blockCoords);
			cachedPM.makePersistent(streetBlock);
		}
		else if(inPlacemark && qName.equals("name")){
			inName = false;
		}
		else if(inPlacemark && qName.equals("coordinates")){
			inCoordinates = false;
		}
		
	}




	/**
	 * Validates data collected for a new StreetBlock. Throws a
	 * SAXBadDataException if Validation fails
	 * 
	 */
	private void checkCreateBlockConditions() throws SAXBadDataException {
		if (blockName == null)
			throw new SAXBadDataException(
					"StreetBlock creation: name element required, but not found");
		if (blockCoords.size() == 0)
			throw new SAXBadDataException(
					"StreetBlock creation: coordinates element required, but not found");
	}


	/**
	 * resets the parser data in preparation for a new StreetBlock element <br>
	 * <br>
	 * <b>Modifies:</b><br>
	 * Modifies this<br>
	 * <br>
	 * <b>Effects:</b> <br>
	 * resets the parser data in preparation for a new StreetBlock element
	 */
	private void resetState() {
		blockCoords.clear();
		blockName = null;
	}

}
