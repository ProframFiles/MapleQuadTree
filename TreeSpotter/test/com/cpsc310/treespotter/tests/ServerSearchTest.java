package com.cpsc310.treespotter.tests;

import java.util.ArrayList;

import javax.jdo.PersistenceManager;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.cpsc310.treespotter.server.DataFetcher;
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
		helper.setSimulateProdLatencies(true);
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
		ArrayList<ClientTreeData> results;
		results = doKeywordSearch("MARY");
		
		assertEquals(1,results.size() );
		assertTrue(results.get(0).getCommonName().equalsIgnoreCase("MARY") );
		results.clear();
		
		results = doKeywordSearch("mary");
		assertEquals(1,results.size() );
		assertTrue(results.get(0).getCommonName().equalsIgnoreCase("MARY") );
		
		results = doKeywordSearch("AFAKESPECIES");
		assertEquals(5,results.size() );
		assertTrue(results.get(0).getSpecies().equalsIgnoreCase("AFAKESPECIES") );
		
		results = doKeywordSearch("afakespecies");
		assertEquals(5,results.size() );
		assertTrue(results.get(0).getSpecies().equalsIgnoreCase("AFAKESPECIES") );
		
		results = doKeywordSearch("CRESCENT");
		assertEquals(4,results.size() );
		assertTrue(results.get(0).getStreet().equalsIgnoreCase("THE CRESCENT") );
		
		results = doKeywordSearch("crescent");
		assertEquals(4,results.size() );
		assertTrue(results.get(0).getStreet().equalsIgnoreCase("THE CRESCENT") );
		
	}
	
	@Test
	public void testLocationSearch() {
		LocationProcessor lp = new LocationProcessor();
		AdvancedSearch loc_query;
		lp.doPost(null, null);
		
		loc_query = new AdvancedSearch();
		loc_query.addSearchParam(SearchFieldID.LOCATION, "49.2626,-123.1878,200");
		ArrayList<ClientTreeData> results = dataService.searchTreeData(loc_query);
		assertEquals(1,results.size() );
		assertTrue(results.get(0).getCommonName().equalsIgnoreCase("HIGHBURY TREE") );
		
		
		results.clear();
		loc_query = new AdvancedSearch();
		loc_query.addSearchParam(SearchFieldID.LOCATION, "240 the crescent");
		results = dataService.searchTreeData(loc_query);
		assertEquals(0,results.size() );
		//assertTrue(results.get(0).getStreet().equalsIgnoreCase("THE CRESCENT") );
		
		results.clear();
		loc_query = new AdvancedSearch();
		loc_query.addSearchParam(SearchFieldID.LOCATION, "2600-2700 Highbury st");
		results = dataService.searchTreeData(loc_query);
		assertEquals(1,results.size() );
		assertTrue(results.get(0).getCommonName().equalsIgnoreCase("HIGHBURY TREE") );
		
		results.clear();
		loc_query = new AdvancedSearch();
		loc_query.addSearchParam(SearchFieldID.LOCATION, "2600 Highbury st");
		results = dataService.searchTreeData(loc_query);
		assertEquals(1,results.size() );
		assertTrue(results.get(0).getCommonName().equalsIgnoreCase("HIGHBURY TREE") );
	}
	
	@Test
	public void testAddressSearch() {
	
		AdvancedSearch query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.ADDRESS, "240 the crescent");
		ArrayList<ClientTreeData> results = dataService.searchTreeData(query);
		assertEquals(4,results.size() );
		assertTrue(results.get(0).getStreet().equalsIgnoreCase("The Crescent") );
		
		results.clear();
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.ADDRESS, "230-250 the crescent");
		results = dataService.searchTreeData(query);
		assertEquals(4,results.size() );
		assertTrue(results.get(0).getStreet().equalsIgnoreCase("The Crescent") );
		
		results.clear();
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.ADDRESS, "2600-2700 highbury st");
		results = dataService.searchTreeData(query);
		assertEquals(1,results.size() );
		assertTrue(results.get(0).getStreet().equalsIgnoreCase("highbury st") );
	}
	
	@Test
	public void testHeightSearch() {
		
		AdvancedSearch query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.HEIGHT, "1-5");
		ArrayList<ClientTreeData> results = dataService.searchTreeData(query);
		assertEquals(5,results.size() );
		assertEquals(results.get(0).getHeightRange(), 3);
		
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.HEIGHT, "3-3");
		results = dataService.searchTreeData(query);
		assertEquals(5,results.size() );
		assertEquals(results.get(0).getHeightRange(), 3);
		
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.HEIGHT, "6-7");
		results = dataService.searchTreeData(query);
		assertEquals(0,results.size() );
		
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.HEIGHT, "7-6");
		results = dataService.searchTreeData(query);
		assertEquals(0,results.size());
	}
	
	@Test
	public void importDataWithoutDyingTest() {
		//LocationProcessor lp = new LocationProcessor();
		//lp.doPost(null, null);
		
		try{
			DataFetcher df = new DataFetcher();
			df.doGet(null, null);
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testDiameterSearch() {
	
		AdvancedSearch query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.DIAMETER, "1-5");
		ArrayList<ClientTreeData> results = dataService.searchTreeData(query);
		assertEquals(5,results.size() );
		assertEquals(results.get(0).getHeightRange(), 3);
		
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.DIAMETER, "3-3");
		results = dataService.searchTreeData(query);
		assertEquals(5,results.size() );
		assertEquals(results.get(0).getHeightRange(), 3);
		
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.DIAMETER, "6-7");
		results = dataService.searchTreeData(query);
		assertEquals(0,results.size() );
		
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.DIAMETER, "7-6");
		results = dataService.searchTreeData(query);
		assertEquals(0,results.size());
	}
	
	static private TreeData makeTestTree(String common, int id){
		TreeData tree = new TreeData("U", id);
		tree.setSpecies("AFAKESPECIES");
		tree.setStreet("THE CRESCENT");
		tree.setCivicNumber(240);
		tree.setNeighbourhood("THE BRONX");
		tree.setCultivar("GILDED LILY");
		tree.setGenus("RUGOSA");
		tree.setCommonName(common);
		tree.setHeightRange(3);
		tree.setDiameter(3);
		return tree;
	}
	
	private ArrayList<ClientTreeData> doKeywordSearch(String keyword){
		KeywordSearch q_lower = new KeywordSearch();
		q_lower.addSearchParam(SearchFieldID.KEYWORD, keyword);
		return dataService.searchTreeData(q_lower);
	}
	
}
