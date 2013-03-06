package com.cpsc310.treespotter.server;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jdo.PersistenceManager;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class StreetDataUpdateJob extends Job {
	private static final Logger LOG = Logger.getLogger(StreetDataUpdateJob.class.getName());
	
	private StreetDataUpdateJob(){
		
	}
	
	public StreetDataUpdateJob(String job_name, String fetch_url){
		super(job_name, fetch_url);
		ObjectifyService.register(this.getClass());
	}


	@Override
	public void setOptions(String option_name, String option_value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ArrayList<SubTask> createSubTasks(InputStream is) {
		ArrayList<SubTask> task_list = new ArrayList<SubTask>();
		SubTask the_task = new SubTask();
		the_task.task_string = "public_streets.kml";
		the_task.task_progress = 0;
		task_list.add(the_task);
		return task_list;
	}


	@Override
	protected int processSubTask(InputStream is, SubTask st) {
		
		
		
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		int max_records = 3000;
		int count = 0;
		try {

			ZipInputStream unzipper = new ZipInputStream(is);
			ZipEntry zip_entry = unzipper.getNextEntry();
			while (zip_entry != null
					&& !zip_entry.getName().equalsIgnoreCase(st.task_string)) {
				unzipper.getNextEntry();
			}
			if(zip_entry == null || !zip_entry.getName().equalsIgnoreCase(st.task_string)){
				throw new FileNotFoundException("File Not Found:  " + st.task_string);
			}
			
			
			InputSource sax_input = new InputSource(new BufferedInputStream(unzipper, 4096));
			LocationKMLSAXHandler handler = new LocationKMLSAXHandler(pm, st.task_progress, max_records);
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(handler);
			reader.parse(sax_input);
			count = handler.getBlockCount();

		} catch (IOException e) {
			LOG.severe(e.getMessage());
			throw new RuntimeException("Reading of " + st.task_string
					+ " failed: " + e, e);
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			throw new RuntimeException("Parsing of " + st.task_string + " failed: "
					+ e, e);
		} finally {
			pm.close();
		}
		// this means we're done
		if(count  < max_records){
			return 0;
		}
		return st.task_progress + count;
	}
	
}
