package com.cpsc310.treespotter.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cpsc310.treespotter.server.TreeData;

public class DataFetcher extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	final static private String URL_STRING = "http://www.ugrad.cs.ubc.ca/~q0b7/csv_street_trees.zip"; // change here
	final static private Logger LOG = Logger.getLogger(DataFetcher.class.getName());
	
	private ArrayList<TreeData> curr_trees = new ArrayList<TreeData>();
	private int MAX_BLOCK_SIZE = 32;
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) 
			throws ServletException, IOException {
		LOG.setLevel(Level.FINE);
		LOG.info("Attemping to update tree database");
		
		try {
			final URL url = new URL(URL_STRING);
			ZipInputStream zis = new ZipInputStream(url.openStream());
			
			ZipEntry zip;
			
			LOG.info("Unzipping files");
			
			while ((zip = zis.getNextEntry()) != null) {
				if(zip.getName().equals("StreetTrees_Shaughnessy.csv")){
					parseFile(zis);
					LOG.info("Parsed file: "+zip.getName());
				}
			}
			
			zis.close();
		} catch (final Exception e){
			
			LOG.info("Error with update: "+e);
			
		}
		
		LOG.info("Updating tree data base is complete");
		
	}
	
	private void parseFile(ZipInputStream zis) throws IOException {
		
		final BufferedReader in = new BufferedReader(new InputStreamReader(zis));
		
		String line;
		in.readLine(); // to skip header row

		while((line = in.readLine()) != null) {
			parseLine(line);
			if (curr_trees.size() == MAX_BLOCK_SIZE) {
				persistBlock(curr_trees);
			}
		}
		// catch the trees left in the unfilled buffer at the end
		if(!curr_trees.isEmpty()){
			persistBlock(curr_trees);
		}
	} 
	
	private void parseLine(String line) {
		String[] values = line.split(",");
		addTree(values);
	}
	
	
	private void addTree(String[] values) {
		LOG.finer("Parsing tree: treeID "+values[0]);
		TreeData tree = createNewTree(values);
		curr_trees.add(tree);
	}
	
	private void persistBlock(ArrayList<TreeData> trees){
	//	PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
		//	pm.makePersistentAll(curr_trees);
			curr_trees.clear();
		} finally {
	//		pm.close();
		}
	}
	
	private TreeData createNewTree(String[] values) {

		int treeID = Integer.parseInt(values[0]);
		TreeData tree = new TreeData("admin", treeID);
		
		tree.setCivicNumber(Integer.parseInt(values[1]));
		tree.setStdStreet(values[2]);
		tree.setNeighbourhood(values[3]);
		tree.setCell(Integer.parseInt(values[4]));
		tree.setStreet(values[5]);
		tree.setStreetBlock(values[6]);
		tree.setStreetSideName(values[7]);
		tree.setHeightRange(Integer.parseInt(values[9]));
		tree.setDiameter(Float.parseFloat(values[10]));
		tree.setPlanted(null);
		tree.setPlantArea(values[12]);
		//tree.setRootBarrier(valueToBoolean(values[13]));
		tree.setCultivar(values[15]);
		tree.setGenus(values[16]);
		tree.setSpecies(values[17]);
		tree.setCommonName(values[18]);
		
		return tree;
	}

}
