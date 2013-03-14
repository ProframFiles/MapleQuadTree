/**
 * 
 */
package com.cpsc310.treespotter.server;
import static com.cpsc310.treespotter.server.OfyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.SortedSet;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.OnLoad;

/**
 * @author maple-quadtree
 *
 */
@Entity
@Cache
public class TreeDepot {
	private static final Logger LOG = Logger.getLogger("Tree");
	static TreeDepot instance = null;
	@Id private String id;
	@Ignore private TreesIndexedByString speciesIndex;
	@Load Ref<TreesIndexedByString> speciesIndexRef;
	
	@Ignore private TreesIndexedByString streetIndex;
	@Load Ref<TreesIndexedByString> streetIndexRef;
	
	@Ignore private TreesIndexedByString keywordIndex;
	@Load Ref<TreesIndexedByString> keywordIndexRef;
	
	@Ignore private TreesIndexedByString commonNameIndex;
	@Load Ref<TreesIndexedByString> commonNameIndexRef;
	
	@Ignore private TreesIndexedByString neighbourhoodIndex;
	@Load Ref<TreesIndexedByString> neighbourhoodIndexRef;
	
	@Ignore private TreesIndexedByString genusIndex;
	@Load Ref<TreesIndexedByString> genusIndexRef;
	
	@Ignore ArrayList<TreesIndexedByString> stringIndices;
	
	
	static TreeDepot treeDepot(){
		return treeDepot("TreeDepot");
	}
	static TreeDepot treeDepot(String id){
		if( instance != null){
			return instance;
		}
		Key.create(TreeDepot.class, id);
		TreeDepot depot = ofy().load().key(Key.create(TreeDepot.class, id)).getValue();
		if(depot == null){
			depot = new TreeDepot(id);
			depot.saveTrees();
		}
		instance = depot;
		return instance; 
	}
	
	static public void reset(){
		instance = null;
	}
	
	private TreeDepot(){
		
	}
	
	private TreeDepot(String id){
		this.id = id;
		createContainers();
		Init();
	}
	
	private void createContainers(){
		speciesIndex = new TreesIndexedByString("speciesIndex");
		streetIndex = new TreesIndexedByString("streetIndex");
		keywordIndex = new TreesIndexedByString("keywordIndex");
		genusIndex = new TreesIndexedByString("genusIndex");
		commonNameIndex = new TreesIndexedByString("commonNameIndex");
		neighbourhoodIndex = new TreesIndexedByString("neighbourhoodIndex");
	}
	
	@OnLoad
	private void Init(){
		LOG.setLevel(Level.INFO);
		if(speciesIndex == null){
			speciesIndex = speciesIndexRef.get();
		}
		if(streetIndex == null){
			streetIndex = streetIndexRef.get();
		}
		if(keywordIndex == null){
			keywordIndex = keywordIndexRef.get();
		}
		if(genusIndex == null){
			genusIndex = genusIndexRef.get();
		}
		if(commonNameIndex == null){
			commonNameIndex = commonNameIndexRef.get();
		}
		if(neighbourhoodIndex == null){
			neighbourhoodIndex = neighbourhoodIndexRef.get();
		}
		if(speciesIndex == null){
			speciesIndex = new TreesIndexedByString("speciesIndex");
		}
		if(streetIndex == null){
			streetIndex = new TreesIndexedByString("streetIndex");
		}
		if(keywordIndex == null){
			keywordIndex = new TreesIndexedByString("keywordIndex");
		}
		if(genusIndex == null){
			genusIndex = new TreesIndexedByString("genusIndex");
		}
		if(commonNameIndex == null){
			commonNameIndex = new TreesIndexedByString("commonNameIndex");
		}
		if(neighbourhoodIndex == null){
			neighbourhoodIndex = new TreesIndexedByString("neighbourhoodIndex");
		}
	
	
		speciesIndex.setStringProvider(TreeToStringFactory.getTreeToSpecies());
		streetIndex.setStringProvider(TreeToStringFactory.getTreeToStdStreet());
		keywordIndex.setStringProvider(TreeToStringFactory.getTreeToKeywords());
		genusIndex.setStringProvider(TreeToStringFactory.getTreeToGenus());
		commonNameIndex.setStringProvider(TreeToStringFactory.getTreeToCommonName());
		neighbourhoodIndex.setStringProvider(TreeToStringFactory.getTreeToNeighbourhood());
		
		stringIndices = new ArrayList<TreesIndexedByString>();
		
		stringIndices.add(speciesIndex);
		stringIndices.add(streetIndex);
		stringIndices.add(keywordIndex);
		stringIndices.add(genusIndex);
		stringIndices.add(commonNameIndex);
		stringIndices.add(neighbourhoodIndex);
	}
	
	public void putTrees(Collection<TreeData2> trees){
		for(TreesIndexedByString entry: stringIndices){
			entry.addTrees(trees);
		}
	}
	public void putTreesByGenus(Collection<TreeData2> trees){
		genusIndex.addTrees(trees);
		saveTrees();
	}
	public void putTreesBySpecies(Collection<TreeData2> trees){
		speciesIndex.addTrees(trees);
		saveTrees();
	}
	public void putTreesByStreet(Collection<TreeData2> trees){
		streetIndex.addTrees(trees);
		saveTrees();
	}
	public void putTreesByCommonName(Collection<TreeData2> trees){
		commonNameIndex.addTrees(trees);
		saveTrees();
	}
	public void putTreesByNeighbourhood(Collection<TreeData2> trees){
		neighbourhoodIndex.addTrees(trees);
		saveTrees();
	}
	public void putTreesByKeywords(Collection<TreeData2> trees){
		keywordIndex.addTreesSplit(trees);
		saveTrees();
	}
	
	public void saveTrees(){
		speciesIndexRef = Ref.create(speciesIndex);
		streetIndexRef = Ref.create(streetIndex);
		keywordIndexRef = Ref.create(keywordIndex);
		genusIndexRef = Ref.create(genusIndex);
		commonNameIndexRef = Ref.create(commonNameIndex);
		neighbourhoodIndexRef = Ref.create(neighbourhoodIndex);
		
		ofy().save().entity(this).now();
	}
	
	public SortedSet<TreeData2> getTreesWithSpecies(String species){
		return Collections.unmodifiableSortedSet(speciesIndex.getAllTreesWith(species.toUpperCase()));
	}
	
	public SortedSet<TreeData2> getTreesWithStreet(String street){
		return Collections.unmodifiableSortedSet(streetIndex.getAllTreesWith(street.toUpperCase()));
	}
	
	public SortedSet<TreeData2> getTreesWithGenus(String street){
		return Collections.unmodifiableSortedSet(genusIndex.getAllTreesWith(street.toUpperCase()));
	}

	public SortedSet<TreeData2> getTreesWithCommonName(String street){
		return Collections.unmodifiableSortedSet(commonNameIndex.getAllTreesWith(street.toUpperCase()));
	}
	
	public SortedSet<TreeData2> getTreesWithNeighbourHood(String street){
		return Collections.unmodifiableSortedSet(neighbourhoodIndex.getAllTreesWith(street.toUpperCase()));
	}
	
	public SortedSet<TreeData2> getTreesWithKeyword(String kw){
		return Collections.unmodifiableSortedSet(keywordIndex.getAllTreesWith(kw.toUpperCase()));
	}

	
	public Set<String> getStreetSet(){
		return Collections.unmodifiableSet(streetIndex.getKeySet());
	}
	
	public Set<String> getGenusSet(){
		return Collections.unmodifiableSet(genusIndex.getKeySet());
	}
	
	public Set<String> getSpeciesSet(){
		return Collections.unmodifiableSet(speciesIndex.getKeySet());
	}
	
	public Set<String> getCommonNameSet(){
		return Collections.unmodifiableSet(commonNameIndex.getKeySet());
	}
	
	public Set<String> getNeighbourhoodSet(){
		return Collections.unmodifiableSet(neighbourhoodIndex.getKeySet());
	}
	
	public Set<String> getKeywordSet(){
		return Collections.unmodifiableSet(keywordIndex.getKeySet());
	}
}
