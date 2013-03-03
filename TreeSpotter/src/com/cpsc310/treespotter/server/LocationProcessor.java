/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Key;

/**
 * @author maple-quadtree
 * 
 */
public class LocationProcessor extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String URL_STRING = "http://data.vancouver.ca/download/kml/public_streets.kmz";
	private static final String FILE_NAME = "public_streets.kml";
	private static final Key LAST_TIMESTAMP_KEY = KeyFactory.createKey("StreetBlockUpdateTimeStamp", "last update");
	private static final Logger LOG = Logger.getLogger(LocationProcessor.class.getName());
	// days before we should try fetching the file again
	private static int UPDATE_PERIOD = 6;
	
	public LocationProcessor(){
		
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response){
		LOG.info("LocationProcessor:\n\tRecieved request to update street block data. Executing.");
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try{
			
			Query time_stamp_query = pm.newQuery(StreetBlockUpdateTimeStamp.class, "key == id");
			time_stamp_query.setUnique(true);
			time_stamp_query.declareParameters("com.google.appengine.api.datastore.Key id");
			StreetBlockUpdateTimeStamp time_stamp = (StreetBlockUpdateTimeStamp)time_stamp_query.execute(LAST_TIMESTAMP_KEY);
			// only fetch the file if enough time has passed since the last time we tried
			if(time_stamp == null || time_stamp.getDaysPassed() > UPDATE_PERIOD){
								
				LOG.info("LocationProcessor:\n\tFetching and unpacking .kmz file.");
				CheckedInputStream in_stream = FetchLocationFile();
				
				// only update the data if the file it's different than the last one we parsed
				if(time_stamp == null || !time_stamp.isChecksumEqual(in_stream.getChecksum().getValue()) ){
				
					LOG.info("LocationProcessor:\n\tParsing .kml file.");
					int blocks_parsed = ParseLocationFile(in_stream);
					
					LOG.info("LocationProcessor:\n\tDone Parsing " + blocks_parsed + " entries.");
					
					
					if(time_stamp == null){
						time_stamp = new StreetBlockUpdateTimeStamp(LAST_TIMESTAMP_KEY);
					}
				
					time_stamp.Update(blocks_parsed,in_stream.getChecksum().getValue() );
					pm.makePersistent(time_stamp);
				}
				else{
					LOG.info("LocationProcessor:\n\t.kml file has not changed since last parse; skipping parse.");
				}
			}
			else{
				LOG.info("LocationProcessor:\n\tData is only " + time_stamp.getDaysPassed() + " days old, no update needed");
			}
		}
		finally{
			pm.close();
		}
		
	}
	
	
	public CheckedInputStream FetchLocationFile() {

		CheckedInputStream ret = null;
		try {
			URL url = new URL(URL_STRING);
			ZipInputStream unzipper = new ZipInputStream(url.openStream());
			ZipEntry zip_entry = unzipper.getNextEntry();
			while (zip_entry != null
					&& !zip_entry.getName().equalsIgnoreCase(FILE_NAME)) {
				unzipper.getNextEntry();
			}
			if(zip_entry == null || !zip_entry.getName().equalsIgnoreCase(FILE_NAME)){
				throw new FileNotFoundException(FILE_NAME);
			}

			// found it...
			int file_size = (int) zip_entry.getSize();
			byte[] b = new byte[file_size];
			int bytes_read = 0;
			int last_bytes_read = 0;
			
			// read the whole file into a buffer
			// the streaming approach kept starving the SAX parser
			// TODO: see if a buffered stream would work ok
			while ((file_size - bytes_read != 0) && bytes_read != -1) {
				bytes_read += last_bytes_read;
				last_bytes_read = unzipper.read(b, bytes_read, file_size
						- bytes_read);
			}
			ret = new CheckedInputStream( new ByteArrayInputStream(b), new CRC32());
		} catch (MalformedURLException e) {
			LOG.severe(e.getMessage());
			throw new RuntimeException("Fetching of " + URL_STRING
					+ " failed: " + e, e);
		} catch (IOException e) {
			LOG.severe(e.getMessage());
			throw new RuntimeException("Unpacking of " + URL_STRING
					+ " failed: " + e, e);
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			throw new RuntimeException("Fetching of " + FILE_NAME + " failed: "
					+ e, e);
		}
		return ret;

	}

	public int ParseLocationFile(InputStream in_stream) {
		PersistenceManager pm = PMF.get().getPersistenceManager();

		int count = 0;
		try {

			InputSource sax_input = new InputSource(in_stream);
			LocationKMLSAXHandler handler = new LocationKMLSAXHandler(pm);
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(handler);
			reader.parse(sax_input);
			count = handler.getBlockCount();

		} catch (IOException e) {
			LOG.severe(e.getMessage());
			throw new RuntimeException("REading of " + FILE_NAME
					+ " failed: " + e, e);
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			throw new RuntimeException("Parsing of " + FILE_NAME + " failed: "
					+ e, e);
		} finally {
			pm.close();
		}
		return count;
	}
}
