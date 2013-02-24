package com.cpsc310.treespotter.server;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LocationKMLSAXHandler extends DefaultHandler {

	private PersistenceManager cachedPM;
	private static Logger LOG = Logger.getLogger(LocationKMLSAXHandler.class.getName());
	private boolean inPlacemark = false;
	private boolean inName = false;
	private boolean inCoordinates = false;
	private int blockCount = 0;
	private ArrayList<Double> blockCoords = new ArrayList<Double>();
	private String blockName = null;
	private String placemarkID = null;
	private ArrayList<StreetBlock> cached_blocks = new ArrayList<StreetBlock>();
	private int max_cached_blocks = 256;

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
		LOG.setLevel(Level.FINE);
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
		//System.out.println(qName);
		if(!inPlacemark && qName.equals("Placemark")){
			inPlacemark = true;
			int id_index = attr.getIndex("id");
			if(id_index != -1){
				placemarkID=attr.getValue(id_index);
			}
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
		try{
			if(inName){
				blockName = new String(ch, start, length);
			}
			else if(inCoordinates){
				//System.out.println(new String(ch,start, length));
				int stringStart = start;
				for (int i = start; i < start+length; i++) {
					if(ch[i] == ',' || ch[i] == ' '){
						blockCoords.add(Double.parseDouble(new String(ch, stringStart, i-stringStart )));
						stringStart = i + 1;
					}
				}
				if (stringStart < start+ length){
					blockCoords.add(Double.parseDouble(new String(ch, stringStart, start+length-stringStart )));
				}
			}
		}
		catch(NumberFormatException e){
		}
		
		catch(Exception e){
			throw new SAXBadDataException("error parsing kml file", e);
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
			try{
				LOG.finer("parsing street block "+ placemarkID +": " + blockName);
				StreetBlock streetBlock = new StreetBlock(placemarkID, blockName, blockCoords);
				cached_blocks.add(streetBlock);
				if(cached_blocks.size() == max_cached_blocks){
					cachedPM.makePersistentAll(cached_blocks);
					cached_blocks.clear();
				}
				blockCount++;
			}
			catch(RuntimeException e){
				LOG.warning("Problem parsing:\n\t" + e.getMessage() + ".\n\tIgnoring and resuming." );
			}
			resetState();
		}
		else if(inPlacemark && qName.equals("name")){
			inName = false;
		}
		else if(inPlacemark && qName.equals("coordinates")){
			inCoordinates = false;
		}
		else if(qName.equals("Document")){
			cachedPM.makePersistentAll(cached_blocks);
			cached_blocks.clear();
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
		placemarkID = "kml_";
	}


	public int getBlockCount() {
		return blockCount;
	}

}
