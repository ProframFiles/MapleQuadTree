package com.cpsc310.treespotter.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.cpsc310.treespotter.server.DataUpdateJob;
import com.cpsc310.treespotter.server.DataUpdater;
import com.cpsc310.treespotter.server.TreeData;
import com.cpsc310.treespotter.server.TreeDataServiceImpl;
import com.cpsc310.treespotter.server.TreeDepot;
import com.cpsc310.treespotter.shared.ISharedTreeData;
import com.cpsc310.treespotter.shared.Util;
import com.cpsc310.treespotter.client.AdvancedSearch;
import com.cpsc310.treespotter.client.KeywordSearch;
import com.cpsc310.treespotter.client.SearchFieldID;
import com.cpsc310.treespotter.client.SearchQuery;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerTests {

	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	private TreeDataServiceImpl dataService = null;
	
	@Before
	public void setUp() throws Exception {
		helper.setSimulateProdLatencies(false);
		helper.setUp();
		
		DataUpdater updater = new DataUpdater();
		updater.init();
		DataUpdateJob	job = new DataUpdateJob("street data update job");
		//job.setLogLevel(Level.FINE);
		//job.setOptions("tree file", "http://www.ugrad.cs.ubc.ca/~q0b7/streettrees_small.zip");
		//ArrayList<byte[]> blobs = job.fetchFileData(job.getFileUrls());
		//byte[] processed = job.preProcessDataFiles(blobs);
		System.out.println("Setting external Job data to avoid downloads during testing");
		FileInputStream inStream;
		byte[] processed = null;
		try {
			inStream = new FileInputStream("./parsetest.zip");
			processed = Util.streamToByteArray(inStream);
			inStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		job.setBinaryDataSource(processed);
		
		boolean has_more_work = job.run();
		while (has_more_work){
			has_more_work = job.run();
		}
		System.out.println("Done PreLoading");
		
		dataService = new TreeDataServiceImpl();
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}

	private void testSingleSearch(SearchFieldID field, String term, int numExpected){
		SearchQuery query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.KEYWORD, "");
		query.setNumResults(100000);
		ArrayList<ISharedTreeData> results = dataService.searchTreeData(query);
		assertEquals(numExpected, results.size());
	}
	
	@Test
	public void generalParseTest() {
		TreeDepot.reset();
		ArrayList<String> results;
		
		results = dataService.getSearchSuggestions(SearchFieldID.KEYWORD, "");
		assertEquals(534,results.size());
		results = dataService.getSearchSuggestions(SearchFieldID.NEIGHBOUR, "");
		assertEquals(1,results.size());
		results = dataService.getSearchSuggestions(SearchFieldID.ADDRESS, "");
		assertEquals(62,results.size());
		results = dataService.getSearchSuggestions(SearchFieldID.COMMON, "");
		assertEquals(265,results.size());
		results = dataService.getSearchSuggestions(SearchFieldID.GENUS, "");
		assertEquals(62,results.size());
		results = dataService.getSearchSuggestions(SearchFieldID.SPECIES, "");
		assertEquals(145,results.size());
		testSingleSearch(SearchFieldID.KEYWORD, "", 6839);
		
		//720,1708,W 40TH AV,SHAUGHNESSY,24,MARGUERITE ST,5600,ODD,N,1,3.00,,10,N,Y,QUEEN ELIZABETH,ACER,CAMPESTRE,QUEEN ELIZABETH MAPLE
		
		ISharedTreeData tree = dataService.getTreeData("V720", "");
		assertTrue(tree.getCivicNumber() == 1708);
		assertTrue(tree.getCommonName().equalsIgnoreCase("QUEEN ELIZABETH MAPLE"));
		assertTrue(tree.getGenus().equalsIgnoreCase("ACER"));
		assertTrue(tree.getSpecies().equalsIgnoreCase("CAMPESTRE"));
		assertTrue(tree.getStreet().equalsIgnoreCase("W 40TH AV"));
		assertTrue(tree.getNeighbourhood().equalsIgnoreCase("SHAUGHNESSY"));
	
		//110163,4897,SELKIRK ST,SHAUGHNESSY,23,W 33RD AV,1200,ODD,N,2,11.75,,12,N,Y,ATROPURPUREUM,PRUNUS,CERASIFERA,PISSARD PLUM
		
		tree = dataService.getTreeData("V110163", "");
		assertTrue(tree.getCivicNumber() == 4897);
		assertTrue(tree.getCommonName().equalsIgnoreCase("PISSARD PLUM"));
		assertTrue(tree.getGenus().equalsIgnoreCase("PRUNUS"));
		assertTrue(tree.getSpecies().equalsIgnoreCase("CERASIFERA"));
		assertTrue(tree.getStreet().equalsIgnoreCase("SELKIRK ST"));
		assertTrue(tree.getNeighbourhood().equalsIgnoreCase("SHAUGHNESSY"));
	}

	@Test
	public void testFlagging() {
		String id = "V4374";
		String id2 = "V110163";
		ArrayList<String> flagged;
		dataService.flagTreeData(id,"Genus", "why not");
		flagged = dataService.getFlaggedTreeIDs();
		assertEquals(flagged.size(),1);
		dataService.flagTreeData(id,"Species", "why not again");
		flagged = dataService.getFlaggedTreeIDs();
		assertEquals(flagged.size(),1);
		dataService.flagTreeData(id,"Genus", "Genus again?");
		flagged = dataService.getFlaggedTreeIDs();
		assertEquals(flagged.size(),1);
		dataService.flagTreeData(id2,"Genus", "this genus is wrong too");
		flagged = dataService.getFlaggedTreeIDs();
		assertEquals(flagged.size(),2);
		dataService.clearTreeFlags(id);
		flagged = dataService.getFlaggedTreeIDs();
		assertEquals(flagged.size(),1);
		dataService.clearTreeFlags(id2);
		flagged = dataService.getFlaggedTreeIDs();
		assertEquals(flagged.size(),0);
	}
	
	@Test
	public void testKeywordSearch() {
		ArrayList<String> suggestions;
		
		testSingleSearch(SearchFieldID.KEYWORD, "", 6839);
		suggestions = dataService.getSearchSuggestions(SearchFieldID.KEYWORD, "");
		
		for(String s: suggestions){
			SearchQuery query = new AdvancedSearch();
			query.addSearchParam(SearchFieldID.KEYWORD, s);
			query.setNumResults(100000);
			ArrayList<ISharedTreeData> results = dataService.searchTreeData(query);
			assertTrue(!results.isEmpty());
		}	
	}
	
	@Test
	public void testAddressSearch() {
	
		AdvancedSearch query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.ADDRESS, "0-10000 the crescent");
		ArrayList<ISharedTreeData> results = dataService.searchTreeData(query);
		assertEquals(53,results.size() );
		assertTrue(results.get(0).getStreet().equalsIgnoreCase("The Crescent") );
		
		results.clear();
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.ADDRESS, "0-10000 the cre");
		results = dataService.searchTreeData(query);
		assertEquals(53,results.size() );
		
		results.clear();
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.ADDRESS, "230-250 the crescent");
		results = dataService.searchTreeData(query);
		assertEquals(0,results.size() );
		
		results.clear();
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.ADDRESS, "1000-2000 the crescent");
		results = dataService.searchTreeData(query);
		assertEquals(42,results.size() );
		assertTrue(results.get(0).getStreet().equalsIgnoreCase("The Crescent") );
		
		results.clear();
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.ADDRESS, "1000-4000 the crescent");
		results = dataService.searchTreeData(query);
		assertEquals(53,results.size() );
		assertTrue(results.get(0).getStreet().equalsIgnoreCase("The Crescent") );
		
		results.clear();
		query = new AdvancedSearch();
		query.addSearchParam(SearchFieldID.ADDRESS, "2600-2700 highbury st");
		results = dataService.searchTreeData(query);
		assertEquals(0,results.size() );
	}

	
	@SuppressWarnings("unused")
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
	
	@SuppressWarnings("unused")
	private ArrayList<ISharedTreeData> doKeywordSearch(String keyword){
		KeywordSearch q_lower = new KeywordSearch();
		q_lower.addSearchParam(SearchFieldID.KEYWORD, keyword);
		return dataService.searchTreeData(q_lower);
	}
	
}
