package com.cpsc310.treespotter.tests;

import java.util.ArrayList;


import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.cpsc310.treespotter.server.DataFetcher;
import com.cpsc310.treespotter.server.Job;
import com.cpsc310.treespotter.server.DataUpdateJob;
import com.cpsc310.treespotter.server.DataUpdater;
import com.cpsc310.treespotter.server.TreeData;
import com.cpsc310.treespotter.server.TreeDataServiceImpl;
import com.cpsc310.treespotter.server.TreeDepot;
import com.cpsc310.treespotter.shared.ISharedTreeData;
import com.cpsc310.treespotter.client.AdvancedSearch;
import com.cpsc310.treespotter.client.KeywordSearch;
import com.cpsc310.treespotter.client.SearchFieldID;
import com.cpsc310.treespotter.client.SearchQuery;

import static com.googlecode.objectify.ObjectifyService.ofy;
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
		dataService = new TreeDataServiceImpl();
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}

	@Test
	public void testStreetUpdateJob() {
		DataUpdater updater = new DataUpdater();
		updater.init();
		Job job = ofy().load().type(Job.class).id("street data update job").get();
		if(job == null){
			job = new DataUpdateJob("street data update job");
		}
		boolean has_more_work = job.run();
		if(has_more_work){
			has_more_work = job.run();
		}
		
		TreeDepot.reset();
		
		SearchQuery loc_query = new AdvancedSearch();
		loc_query.addSearchParam(SearchFieldID.SPECIES, "Betulus");
		ArrayList<ISharedTreeData> results = dataService.searchTreeData(loc_query);
		assertEquals(results.size(), 100);
		loc_query.setNumResults(4700);
		results = dataService.searchTreeData(loc_query);
		assertEquals(results.size(), 4700);
		
		//loc_query.setNumResults(10000);
		//results = dataService.searchTreeData(loc_query);
		//assertEquals(results.size(), 4705);
		
		loc_query = new AdvancedSearch();
		loc_query.addSearchParam(SearchFieldID.ADDRESS, "123 high");
		loc_query.addSearchParam(SearchFieldID.SPECIES, "betulus");
		
		results = dataService.searchTreeData(loc_query);
		assertEquals(results.size(),53);
		
		ArrayList<String> suggestions = dataService.getSearchSuggestions(SearchFieldID.SPECIES, "be");
		assertEquals(3, suggestions.size());
		
		suggestions = dataService.getSearchSuggestions(SearchFieldID.SPECIES, "b");
		assertEquals(37, suggestions.size());
		
		suggestions = dataService.getSearchSuggestions(SearchFieldID.SPECIES, "");
		assertEquals(264, suggestions.size());
		
		for(String s: suggestions){
			System.out.println(s);
		}
		
		 testAddressSearch();	
	}
	
	
	public void testKeywordSearch() {
		ArrayList<ISharedTreeData> results;
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
	
	public void testLocationSearch() {
		AdvancedSearch loc_query;
		
		loc_query = new AdvancedSearch();
		loc_query.addSearchParam(SearchFieldID.LOCATION, "49.2626,-123.1878,200");
		ArrayList<ISharedTreeData> results = dataService.searchTreeData(loc_query);
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
	
	public void testAddressSearch() {
	
		AdvancedSearch query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.ADDRESS, "240 the crescent");
		ArrayList<ISharedTreeData> results = dataService.searchTreeData(query);
		assertEquals(53,results.size() );
		assertTrue(results.get(0).getStreet().equalsIgnoreCase("The Crescent") );
		
		results.clear();
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.ADDRESS, "230-250 the crescent");
		results = dataService.searchTreeData(query);
		assertEquals(53,results.size() );
		
		results.clear();
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.ADDRESS, "2600-2700 highbury st");
		results = dataService.searchTreeData(query);
		assertEquals(100,results.size() );
	}
	
	public void testHeightSearch() {
		
		AdvancedSearch query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.HEIGHT, "1-5");
		ArrayList<ISharedTreeData> results = dataService.searchTreeData(query);
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
	
	public void testDiameterSearch() {
	
		AdvancedSearch query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.DIAMETER, "1-5");
		ArrayList<ISharedTreeData> results = dataService.searchTreeData(query);
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
	
	static private TreeData makeTestTree(String id){
		TreeData tree = new TreeData(id);
		tree.setSpecies("AFAKESPECIES");
		tree.setStreet("BROKEN IMPLEMENTATION STREET");
		tree.setCivicNumber(240);
		tree.setNeighbourhood("THE BRONX");
		tree.setCultivar("GILDED LILY");
		tree.setGenus("RUGOSA");
		tree.setCommonName("test tree");
		tree.setHeightRange(3);
		tree.setDiameter(3);
		return tree;
	}
	
	private ArrayList<ISharedTreeData> doKeywordSearch(String keyword){
		KeywordSearch q_lower = new KeywordSearch();
		q_lower.addSearchParam(SearchFieldID.KEYWORD, keyword);
		return dataService.searchTreeData(q_lower);
	}
	
}
