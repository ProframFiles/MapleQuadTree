package com.cpsc310.treespotter.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cpsc310.treespotter.server.PMF;
import com.cpsc310.treespotter.server.TreeData;

public class DataFetcher extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	final static private String URL_STRING = "https://dl.dropbox.com/u/23948817/maple%20quadtree/test.zip"; // change here
	final static private Logger LOG = Logger.getLogger(DataFetcher.class.getName());
	
	private ArrayList<TreeData> curr_trees = new ArrayList<TreeData>();
	private int MAX_BLOCK_SIZE = 256;
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) 
			throws ServletException, IOException {
		LOG.info("Attemping to update tree database");
		
		try {
			final URL url = new URL(URL_STRING);
			ZipInputStream zis = new ZipInputStream(url.openStream());
			
			ZipEntry zip;
			
			LOG.info("Unzipping files");
			
			while ((zip = zis.getNextEntry()) != null) {
				parseFile(zis);
				LOG.info("Parsed file: "+zip.getName());
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
		}
	} 
	
	private void parseLine(String line) {
		String[] values = line.split(",");
		addTree(values);
	}
	
	
	private void addTree(String[] values) {
		LOG.info("Parsing tree: treeID "+values[0]);
		TreeData tree = createNewTree(values);
		curr_trees.add(tree);
		
		if (curr_trees.size() == MAX_BLOCK_SIZE) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				pm.makePersistentAll(curr_trees);
				curr_trees.clear();
			} finally {
				pm.close();
			}
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
		tree.setAssigned(valueToBoolean(values[8]));
		tree.setHeightRange(Integer.parseInt(values[9]));
		tree.setDiameter(Float.parseFloat(values[10]));
		tree.setPlanted(null);
		tree.setPlantArea(values[12]);
		tree.setRootBarrier(valueToBoolean(values[13]));
		tree.setCurb(valueToBoolean(values[14]));
		tree.setCultivar(values[15]);
		tree.setGenus(values[16]);
		tree.setSpecies(values[17]);
		tree.setCommonName(values[18]);
		
		return tree;
	}


	private Boolean valueToBoolean(String b) {
		if (b.equals("Y"))
			return true;
		else if (b.equals("N"))
			return false;
		return null;
	}
}
