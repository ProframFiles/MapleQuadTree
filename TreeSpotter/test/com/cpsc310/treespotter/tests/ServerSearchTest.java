package com.cpsc310.treespotter.tests;

import java.util.ArrayList;

import javax.jdo.PersistenceManager;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.cpsc310.treespotter.server.LocationProcessor;
import com.cpsc310.treespotter.server.TreeData;
import com.cpsc310.treespotter.server.TreeDataServiceImpl;
import com.cpsc310.treespotter.server.PMF;
import com.cpsc310.treespotter.client.AdvancedSearch;
import com.cpsc310.treespotter.client.ClientTreeData;
import com.cpsc310.treespotter.client.KeywordSearch;
import com.cpsc310.treespotter.client.SearchFieldID;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerSearchTest {

	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	private TreeDataServiceImpl dataService = null;
	
	@Before
	public void setUp() throws Exception {
		helper.setUp();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(makeTestTree("JIM", 1));
			pm.makePersistent(makeTestTree("BOB", 2));
			pm.makePersistent(makeTestTree("MARY", 3));
			pm.makePersistent(makeTestTree("RASTAPOUPOULOS", 4));
			TreeData highbury_tree = makeTestTree("HIGHBURY TREE", 5);
			highbury_tree.setStreet("HIGHBURY ST");
			highbury_tree.setCivicNumber(2632);
			pm.makePersistent(highbury_tree);
		}
		finally{
			pm.close();
		}
		dataService = new TreeDataServiceImpl();
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}

	@Test
	public void testKeywordSearch() {
		KeywordSearch q = new KeywordSearch();
		q.addSearchParam(SearchFieldID.KEYWORD, "MARY");
		ArrayList<ClientTreeData> results = dataService.searchTreeData(q);
		assertEquals(1,results.size() );
		assertTrue(results.get(0).getCommonName().equalsIgnoreCase("MARY") );
	}
	
	@Test
	public void testLocationSearch() {
		
		LocationProcessor lp = new LocationProcessor();
		lp.doPost(null, null);
		
		
		AdvancedSearch loc_query = new AdvancedSearch();
		loc_query.addSearchParam(SearchFieldID.LOCATION, "49.2626,-123.1878,200");
		ArrayList<ClientTreeData> results = dataService.searchTreeData(loc_query);
		assertEquals(1,results.size() );
		assertTrue(results.get(0).getCommonName().equalsIgnoreCase("HIGHBURY TREE") );
	}
	
	private TreeData makeTestTree(String common, int id){
		TreeData tree = new TreeData("U", id);
		tree.setSpecies("AFAKESPECIES");
		tree.setStreet("THE CRESCENT");
		tree.setCivicNumber(240);
		tree.setNeighbourhood("THE BRONX");
		tree.setCultivar("GILDED LILY");
		tree.setGenus("RUGOSA");
		tree.setCommonName(common);
		return tree;
	}
	
}
