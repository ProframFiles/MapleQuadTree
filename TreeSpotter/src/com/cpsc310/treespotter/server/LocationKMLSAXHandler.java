package com.cpsc310.treespotter.server;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index; 
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.StatusCode;

public class LocationKMLSAXHandler extends DefaultHandler {

	private PersistenceManager cachedPM;
	private static Logger LOG = Logger.getLogger(LocationKMLSAXHandler.class.getName());
	private boolean inPlacemark = false;
	private boolean inName = false;
	private boolean inCoordinates = false;
	private int blockCount = 0;
	private int maxBlocks = 20000;
	private int placeMarkStarts = 0;
	private int skipUntil = 0;
	private ArrayList<Double> blockCoords = new ArrayList<Double>();
	private String blockName = null;
	private String placemarkID = null;
	private ArrayList<StreetBlock> cached_blocks = new ArrayList<StreetBlock>();
	private int max_cached_blocks = 128;
	private ByteArrayOutputStream sink_stream = new ByteArrayOutputStream();
	private Kryo kryo = new Kryo();
	

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
		kryo.register(StreetBlock.class);
		LOG.setLevel(Level.FINE);
		cachedPM = pm;
		resetState();
	}
	public LocationKMLSAXHandler(PersistenceManager pm, int start, int num_to_read) {
		super();
		kryo.register(StreetBlock.class);
		LOG.setLevel(Level.FINE);
		cachedPM = pm;
		skipUntil = start;
		maxBlocks = num_to_read;
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
			placeMarkStarts ++;
			if(placeMarkStarts >= skipUntil && blockCount < maxBlocks){
				blockCount++;
				inPlacemark = true;
				int id_index = attr.getIndex("id");
				if(id_index != -1){
					placemarkID=attr.getValue(id_index);
				}
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
		if(inPlacemark && qName.equals("Placemark") ){

			inPlacemark = false;
			checkCreateBlockConditions();
			try{
				LOG.finer("parsing street block "+ placemarkID +": " + blockName);
				StreetBlock streetBlock = new StreetBlock(placemarkID, blockName, blockCoords);
				//addDocumentToIndex(streetBlock, 3);
				cached_blocks.add(streetBlock);
				if(cached_blocks.size() == max_cached_blocks){
					
					//writeToByteArray();
					cachedPM.makePersistentAll(cached_blocks);
					cached_blocks.clear();
				}
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
			//writeToByteArray();
			LOG.info("placing remaining "+ cached_blocks.size() + " in the DB ");
			cachedPM.makePersistentAll(cached_blocks);
			
			cached_blocks.clear();
			
		}
		
	}
	//(aleksy) testing method, leave me alone eclipse warnings
	@SuppressWarnings("unused")
	private void writeToByteArray(){
		
		
		Output out = new Output(sink_stream);
		for (StreetBlock block : cached_blocks) {
			kryo.writeObject(out, block);
		}
		out.close();
		LOG.fine("serialized_size is now " + sink_stream.size()/1024 +"kb"  );
	}
	
	//(aleksy) testing method, leave me alone eclipse warnings
	@SuppressWarnings("unused")
	private void addDocumentToIndex(StreetBlock block, int retries){
		GeoPoint geoPoint = new GeoPoint(block.getLatitude(), block.getLongitude());
		Document doc = Document
				.newBuilder()
				.addField( Field.newBuilder().setName("street").setText(block.getStreetName()))
				.addField( Field.newBuilder().setName("BlockHigh").setNumber(block.getAddressTop()))
				.addField( Field.newBuilder().setName("BlockLow").setNumber(block.getAddressBottom()))
				.addField( Field.newBuilder().setName("location").setGeoPoint(geoPoint)).build();
		
		try {
		    // Put the document.
		    getIndex().put(doc);
		} catch (PutException e) {
		    if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode()) && retries > 0) {
		    	addDocumentToIndex( block, retries-1);
		    }
		    
		}
	}
	public Index getIndex() {
		IndexSpec indexSpec = IndexSpec.newBuilder().setName("StreetBlockIndex").build();
		return SearchServiceFactory.getSearchService().getIndex(indexSpec);
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
