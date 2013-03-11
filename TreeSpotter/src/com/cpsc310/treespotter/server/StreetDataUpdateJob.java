package com.cpsc310.treespotter.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.cpsc310.treespotter.shared.FilteredCSVReader;
import com.cpsc310.treespotter.shared.Util;
import com.esotericsoftware.kryo.Kryo;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class StreetDataUpdateJob extends Job {
	private static final Logger LOG = Logger.getLogger(StreetDataUpdateJob.class.getName());
	
	// this is here for objectify
	@SuppressWarnings("unused")
	private StreetDataUpdateJob(){
		
	}
	
	public StreetDataUpdateJob(String job_name){
		super(job_name);
	}

	@Override
	public void setLogLevel(Level level){
		LOG.setLevel(level);
		super.setLogLevel(level);
	}
	
	@Override
	public void setOptions(String option_name, String option_value) {
		throw new RuntimeException("Invalid Option Name or value.\n\tname:" + option_name + "\n\tvalue: " + option_value);
	}
	
	@Override
	protected byte[] preProcessDataFiles(ArrayList<byte[]> byte_arrays) {
		
		byte[] out_array = null;
		try {
			LOG.info("\n\tPreprocessing source files into persistent form");
			ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
			ZipOutputStream zip_out = new ZipOutputStream(byte_stream);
			zip_out.setLevel(8);
			
			FilterAddressesToZip(byte_arrays.get(0), zip_out);
			FilterTreesToZip(byte_arrays.get(1), zip_out);
			byte_stream.flush();
			out_array = byte_stream.toByteArray();
			LOG.info("\n\tDone preprocessing. " + out_array.length + " compressed bytes ready for persistence");
		} catch (IOException e) {
			throw new RuntimeException("Preprocessing of file failed: " + e, e);
		}
		return out_array;
	}
	
	private void FilterAddressesToZip(byte[] in_array, ZipOutputStream zip_out ) throws IOException{
		
		String file_name = "ICIS_AddressBC.csv";
		LOG.info("\n\tFiltering unneeded data from file \""+ file_name +"\"");
		ArrayList<Integer> selectedIndices = new ArrayList<Integer>();
		selectedIndices.add(2);
		selectedIndices.add(3);
		selectedIndices.add(8);
		selectedIndices.add(10);
		selectedIndices.add(11);
		selectedIndices.add(12);
		selectedIndices.add(13);

		ZipInputStream unzipper = new ZipInputStream(new ByteArrayInputStream(in_array));
		ZipEntry zip_entry = unzipper.getNextEntry();
		while (zip_entry != null
				&& !zip_entry.getName().equalsIgnoreCase(file_name)) {
			zip_entry = unzipper.getNextEntry();
		}
		if(zip_entry == null || !zip_entry.getName().equalsIgnoreCase(file_name)){
			throw new FileNotFoundException("File Not Found:  " + file_name);
		}
		FilteredCSVReader filt = new FilteredCSVReader( new BufferedReader(new InputStreamReader(new BufferedInputStream(unzipper))), selectedIndices);
		String first = filt.readLine();
		
		LOG.fine("\n\t Only keeping these columns:\n\t"+first);
		
		ZipEntry address_csv = new ZipEntry(file_name);
		zip_out.putNextEntry(address_csv);
		
		int written = Util.ReaderToOutputStream(filt, zip_out, 1024);
		zip_out.closeEntry();
		LOG.fine("\n\tfiltered " + filt.getLinesRead() + " csv rows into byte array");
		LOG.info("wrote chars to byte array from " + file_name + "\n\tuncompressed size = " +written+ "\n\t  compressed size = " + address_csv.getCompressedSize());
		zip_out.flush();
	}
	
	private void FilterTreesToZip(byte[] in_array, ZipOutputStream zip_out ) throws IOException{
		LOG.info("\n\tCombining tree data into one csv");
		ZipEntry tree_csv = new ZipEntry("street_trees.csv");
		zip_out.putNextEntry(tree_csv);
		int written =0;
		
		ZipInputStream unzipper = new ZipInputStream(new ByteArrayInputStream(in_array));
		ZipEntry zip_entry = unzipper.getNextEntry();
		while (zip_entry != null) {
			LOG.info("\n\tappending file \"" + zip_entry.getName() +"\"");
			BufferedReader filt =  new BufferedReader(new InputStreamReader(new BufferedInputStream(unzipper)));
			//throw away the first line
			filt.readLine();
			written += Util.ReaderToOutputStream(filt, zip_out, 1024);
			zip_entry = unzipper.getNextEntry();
		}
		zip_out.closeEntry();
		LOG.info("combined all tree files into zip entry " + tree_csv.getName() + "\n\tuncompressed size = " +written+ "\n\t  compressed size = " + tree_csv.getCompressedSize());
		zip_out.flush();
	}
	
	private Map<String, Street> createStreetSetFromCSV(BufferedReader reader) throws IOException{
		HashMap<String, Street> ret = new HashMap<String, Street>();
		StringBuilder street_name = new StringBuilder();
		String line_string;
		while((line_string = reader.readLine()) != null ){
			street_name.setLength(0);
			String[] split_line = line_string.split(",");
			double lat_coord = Double.parseDouble(split_line[0]);
			double long_coord = Double.parseDouble(split_line[1]);
			int civicNumber = Integer.parseInt(split_line[2]);
			if(split_line[3].length() >0){
				street_name.append(split_line[3]);
				street_name.append(" ");
			}
			street_name.append(split_line[4]);
			if(split_line.length > 5 && split_line[5].length() >0){
				street_name.append(" ");
				street_name.append(split_line[5]);
			}
			if(split_line.length > 6 && split_line[6].length() >0){
				street_name.append(" ");
				street_name.append(split_line[6]);
			}
			Street mapped_street = ret.get(street_name.toString());
			if(mapped_street == null){
				mapped_street = new Street();
				ret.put(street_name.toString(), mapped_street);
			}
			mapped_street.addLocation(civicNumber, lat_coord, long_coord);
		}
		return ret;
	}
	
	private ArrayList<TreeData2> testForStreetMatch(BufferedReader reader, Map<String, Street> streets) throws IOException{
		String line_string;
		ArrayList<TreeData2> tree_list = new ArrayList<TreeData2>();
		Set<String> missing_strings = new HashSet<String>();
		assert(streets != null);
		int missing = 0;
		LatLong max_ll = new LatLong(-300.0, -300.0);
		LatLong min_ll = new LatLong(300, 300);
		while((line_string = reader.readLine()) != null ){
			String[] split_line = line_string.split(",");
			TreeData2 new_tree = TreeFactory.makeTreeData2(split_line);
			String street_name = new_tree.getStdStreet();
			if(!streets.containsKey(street_name)){
				if((street_name.startsWith("N ") && streets.containsKey(street_name.substring(2,street_name.length())+" NORTH"))){
					street_name = street_name.substring(2,street_name.length())+" NORTH";
					new_tree.setStdStreet(street_name);
				}
				else if((street_name.startsWith("S ") && streets.containsKey(street_name.substring(2,street_name.length())+" SOUTH"))){
					street_name = street_name.substring(2,street_name.length())+" SOUTH";
					new_tree.setStdStreet(street_name);
				}
				else {
					missing_strings.add(street_name);
					missing++;
				}
			}
			Street this_street = streets.get(street_name);
			if(this_street != null){
				LatLong ll = this_street.getLatLong(new_tree.getCivicNumber());
				if(ll.getLatitude() > max_ll.getLatitude()) max_ll.setLatitude(ll.getLatitude());
				if(ll.getLongitude() > max_ll.getLongitude()) max_ll.setLongitude(ll.getLongitude());
				if(ll.getLatitude() < min_ll.getLatitude()) min_ll.setLatitude(ll.getLatitude());
				if(ll.getLongitude() < min_ll.getLongitude()) min_ll.setLongitude(ll.getLongitude());
				new_tree.setLatLong(ll);
				tree_list.add(new_tree);
			}
		}
		LOG.info("\n\t" + missing + " addressless trees total");
		LOG.info("\n\t" + missing_strings.size() + " missing streets total");
		LOG.info("\n\t" + tree_list.size() + " treeData2 objects created (with locations)");
		LOG.info("\n\tMax lat,long = ( "+ max_ll.getLatitude()+", "+max_ll.getLongitude() + " )");
		LOG.info("\n\tMin lat,long = ( "+ min_ll.getLatitude()+", "+min_ll.getLongitude() + " )");
		LOG.info("\n\t" + missing + " addressless trees total");
		return tree_list;
		//for(String address: missing_strings){
			//LOG.info("could not find street:\n\t"+address);
		//}
	}
	
	@Override
	protected ArrayList<SubTask> createSubTasks(InputStream is) {
		ArrayList<SubTask> task_list = new ArrayList<SubTask>();
		SubTask the_task = new SubTask();
		the_task.task_string = "address_match";
		the_task.task_progress = 0;
		task_list.add(the_task);
		return task_list;
	}


	@Override
	protected int processSubTask(InputStream is, SubTask st) {
		LOG.info("starting subtask:\n\t"+st.task_string + "\n\t progress: " + st.task_progress);
		int max_records = 3000;
		int count = 0;
		try {

			ZipInputStream unzipper = new ZipInputStream(is);
			ZipEntry zip_entry;
			Map<String, Street> street_set = null;
			while ((zip_entry = unzipper.getNextEntry()) != null) {
				if(zip_entry.getName().equals("street_trees.csv")){
					BufferedReader csv_reader =  new BufferedReader(new InputStreamReader(new BufferedInputStream(unzipper)));
					ArrayList<TreeData2> trees = testForStreetMatch(csv_reader, street_set);
				}
				else if (zip_entry.getName().equals("ICIS_AddressBC.csv")){
					BufferedReader csv_reader =  new BufferedReader(new InputStreamReader(new BufferedInputStream(unzipper)));
					street_set = createStreetSetFromCSV(csv_reader);
					LOG.info(street_set.size() +" streets in the set");
				}
			}

		} catch (IOException e) {
			LOG.severe(e.getMessage());
			throw new RuntimeException("Reading of " + st.task_string
					+ " failed: " + e, e);
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			throw new RuntimeException("Parsing of " + st.task_string + " failed: "
					+ e, e);
		}
		LOG.info("done subtask chunk:\n\twork unit count = " + count);
		if(count  < max_records){
			return 0;
		}
		return st.task_progress + count;
	}

	@Override
	protected ArrayList<String> getFileUrls() {
		ArrayList<String> ret = new ArrayList<String>();
		ret.add("http://www.ugrad.cs.ubc.ca/~q0b7/ICIS_AddressBC_csv_all.zip");
		ret.add("http://www.ugrad.cs.ubc.ca/~q0b7/csv_street_trees.zip");
		//ret.add("http://data.vancouver.ca/download/kml/public_streets.kmz");
		return ret;
	}

	@Override
	public String getJobID() {
		return this.getClass().toString();
	}
	
}
