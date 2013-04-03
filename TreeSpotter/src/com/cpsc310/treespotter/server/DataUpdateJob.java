package com.cpsc310.treespotter.server;
import static com.cpsc310.treespotter.server.TreeDepot.treeDepot;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import com.cpsc310.treespotter.shared.LatLong;
import com.cpsc310.treespotter.shared.Util;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.EntitySubclass;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Unindex;
import static com.cpsc310.treespotter.server.OfyService.ofy;

@EntitySubclass
public class DataUpdateJob extends Job {
	private static final Logger LOG = Logger.getLogger(DataUpdateJob.class.getName());
	@Unindex private Ref<PersistentFile> treeDataRef;
	@Ignore private ArrayList<TreeData> cachedTrees = null;
	@Ignore ArrayList<SubTask> tasksToStart = new ArrayList<SubTask>();
	@Ignore private boolean sorted = false;
	@Ignore private boolean forceTasks = false;
	@Ignore String treeFile = "http://www.ugrad.cs.ubc.ca/~q0b7/csv_street_trees.zip";
	int userTreeNumber = -1;
	
	// this is here for objectify
	@SuppressWarnings("unused")
	private DataUpdateJob(){
		
	}
	
	public DataUpdateJob(String job_name){
		super(job_name);
	}

	@Override
	public void setLogLevel(Level level){
		LOG.setLevel(level);
		super.setLogLevel(level);
	}
	
	public void setBinaryTreeData(byte[] b){
		PersistentFile treeData = new PersistentFile(getJobID());
		ZipEntry tree_csv = new ZipEntry("user_trees_start_"+Integer.toString(userTreeNumber)+".csv");
		ByteArrayOutputStream zipped_bytes = new ByteArrayOutputStream();
		ZipOutputStream zip_out = new ZipOutputStream(zipped_bytes);
		try {
			zip_out.putNextEntry(tree_csv);
			Util.streamTostream(new ByteArrayInputStream(b), zip_out, 1024);
			zip_out.closeEntry();
		} catch (IOException e) {
			throw new RuntimeException("Error adding user binary data", e);
		}
		treeData.save(new ByteArrayInputStream(zipped_bytes.toByteArray()));
		treeDataRef = Ref.create(treeData);
		saveJobState(this);
	}
	
	@Override
	public void setOptions(String option_name, String option_value) {
		if(option_name!=null){
			if(option_name.equalsIgnoreCase("force tasks")){
				LOG.info("\n\tSetting option \"force updates\"");
				forceTasks = true;
			}
			else if(option_name.equalsIgnoreCase("add task")){
				LOG.info("\n\tForcing task \""+ option_value + "\"");
				tasksToStart.add(new SubTask(option_value));
				forceTasks = true;
			}
			else if(option_name.equalsIgnoreCase("tree file")){
				treeFile = option_value;
			}
			else if(option_name.equalsIgnoreCase("user trees")){
				forceTasks = true;
				userTreeNumber = Integer.parseInt(option_value);
			}
			else{
				LOG.warning("Invalid option: \"" + option_name+ "\", ignoring");
			}
		}
		else{
			LOG.warning("Null task option is invalid");
		}
	}
	protected boolean forceNewTasks() {
		return forceTasks ;
	}
	@Override
	public byte[] preProcessDataFiles(ArrayList<byte[]> byte_arrays) {
		
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
			zip_out.close();
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
	
	private ArrayList<TreeData> createTreesFromCSV(BufferedReader reader, Map<String, Street> streets) throws IOException{
		String line_string;
		ArrayList<TreeData> tree_list = new ArrayList<TreeData>();
		Set<String> missing_strings = new HashSet<String>();

		assert(streets != null);
		int missing = 0;
		LatLong max_ll = new LatLong(-300.0, -300.0);
		LatLong min_ll = new LatLong(300, 300);
		while((line_string = reader.readLine()) != null ){
			String[] split_line = line_string.split(",");
			TreeData new_tree;
			if(userTreeNumber >=0){
				String[] array = Arrays.copyOf(split_line, split_line.length);
				array[0] = Integer.toString(userTreeNumber);
				new_tree = TreeFactory.makeTreeData2("U", array);
				userTreeNumber ++;
			}
			else{
				new_tree = TreeFactory.makeTreeData2("V",  Arrays.copyOf(split_line, split_line.length));
			}
			
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
		LOG.info("\n\t" + tree_list.size() + " treeData objects created" + "\n\tskipped " + missing + " unlocatable trees on " + missing_strings.size() + " unfound streets");

		return tree_list;
	}
	
	@Override
	protected ArrayList<SubTask> createSubTasks() {
		if(tasksToStart.isEmpty()){
			tasksToStart.add(new SubTask("species"));
			tasksToStart.add(new SubTask("genus"));
			tasksToStart.add(new SubTask("street"));
			tasksToStart.add(new SubTask("commonName"));
			tasksToStart.add(new SubTask("neighbourhood"));
			tasksToStart.add(new SubTask("indices"));
			tasksToStart.add(new SubTask("spatial bins"));
		}
		return tasksToStart;
	}


	@Override
	protected int processSubTask(InputStream is, final SubTask st) {
		LOG.info("START: \""+st.name + "\", progress = " + st.progress);
		int max_records = 10000;
		int count = 0;
		if(cachedTrees == null){
			LOG.info("No cached trees, making them now");
			makeAllTrees(is, st);
		}
		//treeDepot().putTrees(trees);
		if(st.name.equals("species")){
			TreeComparator tc = new TreeComparator(TreeToStringFactory.getTreeToSpecies());
			if(!sorted){
				Collections.sort(cachedTrees, tc);
				sorted = true;
			}
			int last_index = Math.min(st.progress+max_records, cachedTrees.size()-1);
			treeDepot().putTreesBySpecies(cachedTrees.subList(st.progress, last_index));
			count = last_index - st.progress;
		}
		if(st.name.equals("street")){
			TreeComparator tc = new TreeComparator(TreeToStringFactory.getTreeToStdStreet());
			if(!sorted){
				Collections.sort(cachedTrees, tc);
				sorted = true;
			}
			int last_index = Math.min(st.progress+max_records, cachedTrees.size()-1);
			treeDepot().putTreesByStreet(cachedTrees.subList(st.progress, last_index));
			count = last_index - st.progress;
		}
		if(st.name.equals("neighbourhood")){
			TreeComparator tc = new TreeComparator(TreeToStringFactory.getTreeToNeighbourhood());
			if(!sorted){
				Collections.sort(cachedTrees, tc);
				sorted = true;
			}
			final int last_index = Math.min(st.progress+max_records, cachedTrees.size()-1);
			treeDepot().putTreesByNeighbourhood(cachedTrees.subList(st.progress, last_index));
			count = last_index - st.progress;
		}
		if(st.name.equals("commonName")){
			TreeComparator tc = new TreeComparator(TreeToStringFactory.getTreeToCommonName());
			if(!sorted){
				Collections.sort(cachedTrees, tc);
				sorted = true;
			}
			int last_index = Math.min(st.progress+max_records, cachedTrees.size()-1);
			treeDepot().putTreesByCommonName(cachedTrees.subList(st.progress, last_index));
			count = last_index - st.progress;
		}
		if(st.name.equals("genus")){
			TreeComparator tc = new TreeComparator(TreeToStringFactory.getTreeToGenus());
			if(!sorted){
				Collections.sort(cachedTrees, tc);
				sorted = true;
			}
			int last_index = Math.min(st.progress+max_records, cachedTrees.size()-1);
			treeDepot().putTreesByGenus(cachedTrees.subList(st.progress, last_index));
			count = last_index - st.progress;
		}
		if(st.name.equals("indices")){
			TreeComparator tc = new TreeComparator(TreeToStringFactory.getTreeToChunkedID());
			if(!sorted){
				Collections.sort(cachedTrees, tc);
				sorted = true;
			}
			int last_index = Math.min(st.progress+max_records, cachedTrees.size()-1);
			treeDepot().putTreesByName(cachedTrees.subList(st.progress, last_index));
			count = last_index - st.progress;
		}
		if(st.name.equals("spatial bins")){
			TreeComparator tc = new TreeComparator(TreeToStringFactory.getBinner());
			if(!sorted){
				Collections.sort(cachedTrees, tc);
				sorted = true;
			}
			int last_index = Math.min(st.progress+max_records, cachedTrees.size()-1);
			treeDepot().putTreesBySpatialBin(cachedTrees.subList(st.progress, last_index));
			count = last_index - st.progress;
		}
		LOG.info("END: \""+st.name + "\", progress = " + (st.progress + count));
		if(count  < max_records){
			sorted = false;
			return 0;
		}
		return st.progress + count;
	}

	private void makeAllTrees(InputStream is, final SubTask st) {
		try {

			ZipInputStream unzipper = new ZipInputStream(is);
			ZipEntry zip_entry;
			Map<String, Street> street_set = null;
			while ((zip_entry = unzipper.getNextEntry()) != null) {
				if(zip_entry.getName().equals("street_trees.csv")){
					BufferedReader csv_reader =  new BufferedReader(new InputStreamReader(new BufferedInputStream(unzipper)));
					cachedTrees = createTreesFromCSV(csv_reader, street_set);
					street_set = null;
					
				}
				else if (zip_entry.getName().equals("ICIS_AddressBC.csv")){
					BufferedReader csv_reader =  new BufferedReader(new InputStreamReader(new BufferedInputStream(unzipper)));
					street_set = createStreetSetFromCSV(csv_reader);
					LOG.info(street_set.size() +" streets in the set");
				}
			}
			unzipper.close();

		} catch (IOException e) {
			LOG.severe(e.getMessage());
			throw new RuntimeException("Reading of " + st.name
					+ " failed: " + e, e);
		}catch (NullPointerException e) {
			throw new RuntimeException("Parsing of " + st.name + " failed: "
					+ e, e);
		} 
		catch (Exception e) {
			LOG.severe(e.getMessage());
			throw new RuntimeException("Parsing of " + st.name + " failed: "
					+ e, e);
		}
	}

	@Override
	public ArrayList<String> getFileUrls() {
		ArrayList<String> ret = new ArrayList<String>();
		ret.add("http://www.ugrad.cs.ubc.ca/~q0b7/ICIS_AddressBC_csv_all.zip");
		if(userTreeNumber<0){
			ret.add(treeFile);
		}
		
		return ret;
	}

	@Override
	public String getJobID() {
		return this.getClass().toString();
	}
	
	@Override
	public ArrayList< byte[]> fetchFileData(ArrayList<String> urls){
		if(userTreeNumber >= 0 && treeDataRef == null){
			LOG.severe("whoa, where's my file ref?");
			throw new RuntimeException("Should have had a user file ref, but it's not there...");
		}
		ArrayList<byte[]> ret = super.fetchFileData(urls);
		if(ret.size() <2 && treeDataRef != null){
			ofy().load().ref(treeDataRef);
			ret.add(treeDataRef.safeGet().load());
		}
		return ret;
	}
	
}
