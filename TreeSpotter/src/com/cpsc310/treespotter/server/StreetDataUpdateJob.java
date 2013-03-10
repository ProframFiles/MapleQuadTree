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
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.jdo.PersistenceManager;

import com.cpsc310.treespotter.shared.FilteredCSVReader;
import com.cpsc310.treespotter.shared.Util;
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
		ObjectifyService.register(this.getClass());
	}


	@Override
	public void setOptions(String option_name, String option_value) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected byte[] preProcessDataFiles(ArrayList<byte[]> byte_arrays) {
		
		byte[] out_array = null;
		try {
			
			ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
			ZipOutputStream zip_out = new ZipOutputStream(byte_stream);
			zip_out.setLevel(8);
			
			FilterAddressesToZip(byte_arrays.get(0), zip_out);
			FilterTreesToZip(byte_arrays.get(1), zip_out);
			byte_stream.flush();
			out_array = byte_stream.toByteArray();
			
		} catch (IOException e) {
			throw new RuntimeException("Preprocessing of file failed: " + e, e);
		}
		return out_array;
	}
	
	private void FilterAddressesToZip(byte[] in_array, ZipOutputStream zip_out ) throws IOException{
		String file_name = "ICIS_AddressBC.csv";
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
		
		LOG.info("keeping these columns:\n\t"+first);
		
		ZipEntry address_csv = new ZipEntry(file_name);
		zip_out.putNextEntry(address_csv);
		
		int written = Util.ReaderToOutputStream(filt, zip_out, 1024);
		zip_out.closeEntry();
		LOG.info("wrote chars to byte array from " + file_name + "\n\tuncompressed size = " +written+ "\n\t  compressed size = " + address_csv.getCompressedSize());
		zip_out.flush();
	}
	
	private void FilterTreesToZip(byte[] in_array, ZipOutputStream zip_out ) throws IOException{
		ZipEntry tree_csv = new ZipEntry("street_trees.csv");
		zip_out.putNextEntry(tree_csv);
		int written =0;
		
		ZipInputStream unzipper = new ZipInputStream(new ByteArrayInputStream(in_array));
		ZipEntry zip_entry = unzipper.getNextEntry();
		while (zip_entry != null) {
			BufferedReader filt =  new BufferedReader(new InputStreamReader(new BufferedInputStream(unzipper)));
			String first = filt.readLine();
			LOG.info("keeping these columns:\n\t"+first);
			written += Util.ReaderToOutputStream(filt, zip_out, 1024);
			zip_entry = unzipper.getNextEntry();
		}
		zip_out.closeEntry();
		LOG.info("wrote chars to zip entry " + tree_csv.getName() + "\n\tuncompressed size = " +written+ "\n\t  compressed size = " + tree_csv.getCompressedSize());
		zip_out.flush();
	}
	
	// TODO (aleksy) change this to a map
	private Set<String> createStreetSetFromCSV(BufferedReader reader) throws IOException{
		Set<String> ret = new HashSet<String>();
		StringBuilder street_name = new StringBuilder();
		String line_string;
		while((line_string = reader.readLine()) != null ){
			street_name.setLength(0);
			String[] split_line = line_string.split(",");
			if(split_line[2].length() >0){
				street_name.append(split_line[2]);
				street_name.append(" ");
			}
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
			ret.add(street_name.toString());
		}
		return ret;
	}
	
	private void testForStreetMatch(BufferedReader reader, Set<String> streets) throws IOException{
		String line_string;
		Set<String> missing_strings = new HashSet<String>();
		assert(streets != null);
		int missing = 0;
		while((line_string = reader.readLine()) != null ){
			String[] split_line = line_string.split(",");
			String to_search = split_line[1] + " " +split_line[2];
			if(!streets.contains(to_search)){
				//if(!(split_line[2].startsWith("N ") && !streets.contains(split_line[2].substring(2,split_line[2].length())+" NORTH"))
					//&& !(split_line[2].startsWith("S ") && !streets.contains(split_line[2].substring(2,split_line[2].length())+" SOUTH"))	){
				missing_strings.add(to_search);
				missing++;
				
			}
		}
		LOG.info("\n\t" + missing + " addressless trees total");
		LOG.info("\n\t" + missing_strings.size() + " missing streets total");
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
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		int max_records = 3000;
		int count = 0;
		try {

			ZipInputStream unzipper = new ZipInputStream(is);
			ZipEntry zip_entry;
			Set<String> street_set = null;
			while ((zip_entry = unzipper.getNextEntry()) != null) {
				if(zip_entry.getName().equals("street_trees.csv")){
					BufferedReader csv_reader =  new BufferedReader(new InputStreamReader(new BufferedInputStream(unzipper)));
					testForStreetMatch(csv_reader, street_set);
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
		} finally {
			pm.close();
		}
		// this means we're done
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
