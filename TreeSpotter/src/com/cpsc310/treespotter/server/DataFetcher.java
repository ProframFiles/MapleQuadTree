package com.cpsc310.treespotter.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
				res.getWriter().write("Parsed file: "+zip.getName()+"/n");
			}
			
			zis.close();
		} catch (final Exception e){
				res.getWriter().write("Error with paring/n");
		}
		
		res.getWriter().write("Update Complete/n");
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
		for(int i = 0; i<values.length; i++) {
			System.out.println(values[i]);
		}
	}
	
	
	private void addTree() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		TreeData tree = new TreeData("admin", 0);
		
	    try {
	        pm.makePersistent(tree);
	    } finally {
	        pm.close();
	    }
	}
}
