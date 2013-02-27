package com.cpsc310.treespotter.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
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
	
	final private String urlString = "https://dl.dropbox.com/u/23948817/maple%20quadtree/test.zip"; // change here
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) 
			throws ServletException, IOException {
		try {
			final URL url = new URL(urlString);
			ZipInputStream zis = new ZipInputStream(url.openStream());
			
			ZipEntry zip;
			
			while ((zip = zis.getNextEntry()) != null) {
				parseFile(zis);
				res.getWriter().write("Parsed file: "+zip.getName()+"\n");
			}
			
			zis.close();
		} catch (final Exception e){
				res.getWriter().write("Error with paring"+"\n");
		}
		
		res.getWriter().write("Update Complete"+"\n");
		
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
		PersistenceManager pm = PMF.get().getPersistenceManager();
		TreeData tree = createNewTree(values);
		
	    try {
	        pm.makePersistent(tree);
	    } finally {
	        pm.close();
	    }
	}
	
	private TreeData createNewTree(String[] values) {
		int treeID = Integer.parseInt(values[0]);
		int civicNumber = Integer.parseInt(values[1]);
		String stdStreet = values[2];
		String neighbourhood = values[3];
		int cell = Integer.parseInt(values[4]); 
		String street = values[5]; 
		String streetBlock = values[6];
		String streetSideName = values[7]; 
		Boolean assigned = valueToBoolean(values[8]); 
		int heightRange = Integer.parseInt(values[9]);
		float diameter = Float.parseFloat(values[10]);
		Date planted = null; 
		String plantArea = values[12];
		Boolean rootBarrier = valueToBoolean(values[13]); 
		Boolean curb = valueToBoolean(values[14]);
		String cultivar = values[15];
		String genus = values[16];
		String species = values[17]; 
		String commonName = values[18];
		
		return new TreeData("admin", treeID, civicNumber, stdStreet, neighbourhood,
				cell, street, streetBlock, streetSideName, assigned, heightRange,
				diameter, planted, plantArea, rootBarrier, curb, cultivar, genus,
				species, commonName);
	}


	private Boolean valueToBoolean(String b) {
		if (b.equals("Y"))
			return true;
		else if (b.equals("N"))
			return false;
		return null;
	}
}
