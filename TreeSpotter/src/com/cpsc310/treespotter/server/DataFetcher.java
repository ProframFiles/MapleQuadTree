package com.cpsc310.treespotter.server;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;

public class DataFetcher extends HttpServlet {
	
	
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
