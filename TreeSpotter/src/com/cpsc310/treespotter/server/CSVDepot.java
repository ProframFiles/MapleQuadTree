package com.cpsc310.treespotter.server;

import static com.cpsc310.treespotter.server.OfyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.cpsc310.treespotter.shared.CSVFile;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;


@Entity
@Cache
public class CSVDepot {
	static CSVDepot instance;
	@Id String id;
	@Unindex Map<String, Ref<CSVFile>> csvFiles;
	private static final Logger LOG = Logger.getLogger("Tree");
	
	CSVDepot() {}
	
	private CSVDepot(String id) {
		this.id = id;
		csvFiles = new HashMap<String, Ref<CSVFile>>();
	}
	
	static CSVDepot csvDepot() {
		return csvDepot("CSVDepot");
	}
	
	private static synchronized CSVDepot csvDepot(final String id) {
		if( instance != null){
			return instance;
		}
		instance = ofy().transact(new Work<CSVDepot>() {
		    public CSVDepot run() {
		    	
		    	CSVDepot depot =  ofy().load().key(Key.create(CSVDepot.class, id)).getValue();
		    	
		    	return depot;
		    }
		});
		
		if(instance == null){
			instance = new CSVDepot(id);
			saveDepotState(instance);
		}
		return instance;
	}

	private static void saveDepotState(final CSVDepot depot) {
		ofy().transact(new VoidWork() {
		    public void vrun() {
		    	ofy().save().entity(depot);
		    }
		});
	}
	
	
	public synchronized void addCSVFile(final CSVFile csv) {
		
		checkForNullFiles();
		
		String email = csv.getEmail();
		String f_email = formatEmail(email);
		Ref<CSVFile> csvRef = csvFiles.get(f_email);
		
		// Replace old CSV with new one if user has already submitted a file
		if(csvRef != null) {
			Ref<CSVFile> oldCSV = ofy().load().ref(csvRef);
			ofy().delete().entity(oldCSV.get()).now();
			csvFiles.remove(f_email);
		}
		
		ofy().save().entity(csv).now();
		csvFiles.put(f_email, Ref.create(csv));
		saveDepotState(instance);
	}
	

	public CSVFile getCSVFile(final String email) {
		
		checkForNullFiles();
		
		String f_email = formatEmail(email);
		Ref<CSVFile> csvRef = csvFiles.get(f_email);
		if (csvRef != null) {
			ofy().load().ref(csvRef);
			return csvRef.get();
		}
		
		return new CSVFile();
	}
	
	public ArrayList<CSVFile> getAllCSVFiles() {
		checkForNullFiles();
		
		ArrayList<CSVFile> allCSVs = new ArrayList<CSVFile>();
		Collection<Ref<CSVFile>> allCSVRefs = csvFiles.values();
		
		for (Ref<CSVFile> csvRef : allCSVRefs) {
			ofy().load().ref(csvRef);
			allCSVs.add(csvRef.get());
		}
		
		return allCSVs;
	}
	
	public void deleteCSV(CSVFile csv) {
		checkForNullFiles();
		
		String email = csv.getEmail();
		String f_email = formatEmail(email);
		Ref<CSVFile> csvRef = csvFiles.get(f_email);
		if (csvRef != null) {
			ofy().delete().entity(csvRef.get()).now();
			csvFiles.remove(f_email);
			saveDepotState(instance);
		}
		
	}
	
	private String formatEmail(String email) {
		String str = email.replace('.', '\0');
		return str;
	}
	
	private void checkForNullFiles() {
		if (csvFiles == null) {
			LOG.info("Something bad happened");
			csvFiles = new HashMap<String, Ref<CSVFile>>();
		}
	}
}
