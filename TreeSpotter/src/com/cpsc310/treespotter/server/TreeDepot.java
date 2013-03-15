/**
 * 
 */
package com.cpsc310.treespotter.server;
import static com.cpsc310.treespotter.server.OfyService.ofy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.InflaterInputStream;
import java.util.Set;
import java.util.SortedSet;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;


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
	@Ignore HashMap<String, Set<String>> keywordBins;
	@Ignore HashMap<String, Set<Ref<PersistentFile>>> keywordRefs;
	
	
	@Ignore private TreesIndexedByString speciesIndex;
	Ref<TreesIndexedByString> speciesIndexRef;
	
	@Ignore private TreesIndexedByString spatialIndex;
	Ref<TreesIndexedByString> spatialIndexRef;
	
	@Ignore private TreesIndexedByString streetIndex;
	Ref<TreesIndexedByString> streetIndexRef;
	
	@Ignore private TreesIndexedByString commonNameIndex;
	Ref<TreesIndexedByString> commonNameIndexRef;
	
	@Ignore private TreesIndexedByString neighbourhoodIndex;
	Ref<TreesIndexedByString> neighbourhoodIndexRef;
	
	@Ignore private TreesIndexedByString genusIndex;
	Ref<TreesIndexedByString> genusIndexRef;
	
	@Ignore private TreesIndexedByString nameIndex;
	Ref<TreesIndexedByString> nameIndexRef;
	
	@Ignore ArrayList<TreesIndexedByString> stringIndices;
	
	
	static TreeDepot treeDepot(){
		return treeDepot("TreeDepot");
	}
	static TreeDepot treeDepot(final String id){
		if( instance != null){
			return instance;
		}
		instance = ofy().transact(new Work<TreeDepot>() {
		    public TreeDepot run() {
		    	return ofy().load().key(Key.create(TreeDepot.class, id)).getValue();
		    }
		});
		
		if(instance == null){
			instance = new TreeDepot(id);
			instance.saveTrees();
		}
		else{
			instance.Init();
		}
		
		
		return instance; 
	}
	
	static public void reset(){
		instance = null;
	}
	
	private TreeDepot(){
		
	}
	
	private TreeDepot(String id){
		this.id = id;
		Init();
	}
	
	
	private void AddAllBinsToKeyWords(Set<Entry<String, Set<String> >> entries){
		for(Entry<String, Set<String>> entry: entries){
			if(entry.getKey() != null){
				AddBinToKeywords(entry.getKey(), entry.getValue());
			}
		}
	}
	
	private void AddBinToKeywords(String key, Set<String> bins){
		Set<String> list =  keywordBins.get(key);
		if(list == null){
			list = new HashSet<String>();
			keywordBins.put(key, list);
		}
		
		list.addAll(bins);
		
	}
	private void AddSingleBinToKeywords(String key, String bin){
		Set<String> list =  keywordBins.get(key);
		if(list == null){
			list = new HashSet<String>();
			keywordBins.put(key, list);
		}
		
		list.add(bin);
		
	}
	
	private void AddAllRefsToKeyWords(Set<Entry<String, Ref<PersistentFile> >> entries){
		for(Entry<String, Ref<PersistentFile>> entry: entries){
			if(entry.getKey() != null){
				AddRefToKeywords(entry.getKey(), entry.getValue());
			}
		}
	}
	
	private void AddRefToKeywords(String key, Ref<PersistentFile> ref){
		Set<Ref<PersistentFile>> list =  keywordRefs.get(key);
		if(list == null){
			list = new HashSet<Ref<PersistentFile>>();
			keywordRefs.put(key, list);
		}
		
		list.add(ref);
		
	}
	
	Set<String> getAllBinsWithKeyword(String key){
		Set<String> futuresSet = new HashSet<String>();
		for(Entry<String, Set<String>> entry: keywordBins.entrySet()){
			if(entry.getKey().contains(key)){
				futuresSet.addAll(entry.getValue());
			}
		}
		return futuresSet;
	}
	Set<Ref<PersistentFile>> getAllRefsWithKeyword(String key){
		Set<Ref<PersistentFile>> futuresSet = new HashSet<Ref<PersistentFile>>();
		for(Entry<String, Set<Ref<PersistentFile>>> entry: keywordRefs.entrySet()){
			if(entry.getKey().contains(key)){
				futuresSet.addAll(entry.getValue());
			}
		}
		return futuresSet;
	}
	
	public void addAllBinsToRequest( Set<String> currentRequest ){
		for(Entry<String, Set<String>> entry: keywordBins.entrySet()){
			currentRequest.addAll(entry.getValue());
		}
	}
	
	private void loadRefs(){
		if(speciesIndexRef != null){
			speciesIndex = ofy().transact(new Work<TreesIndexedByString>() {
			    public TreesIndexedByString run() {
			    	return ofy().load().ref(speciesIndexRef).getValue();
			    }
			});
		}
		if(streetIndexRef != null){
			streetIndex = ofy().transact(new Work<TreesIndexedByString>() {
			    public TreesIndexedByString run() {
			    	return ofy().load().ref(streetIndexRef).getValue();
			    }
			});
		}
		if(neighbourhoodIndexRef != null){
			neighbourhoodIndex = ofy().transact(new Work<TreesIndexedByString>() {
			    public TreesIndexedByString run() {
			    	return ofy().load().ref(neighbourhoodIndexRef).getValue();
			    }
			});
		}
		if(commonNameIndexRef != null){
			commonNameIndex = ofy().transact(new Work<TreesIndexedByString>() {
			    public TreesIndexedByString run() {
			    	return ofy().load().ref(commonNameIndexRef).getValue();
			    }
			});
		}
		if(genusIndexRef != null){
			genusIndex = ofy().transact(new Work<TreesIndexedByString>() {
			    public TreesIndexedByString run() {
			    	return ofy().load().ref(genusIndexRef).getValue();
			    }
			});
		}
		if(spatialIndexRef != null){
			spatialIndex = ofy().transact(new Work<TreesIndexedByString>() {
			    public TreesIndexedByString run() {
			    	return ofy().load().ref(spatialIndexRef).getValue();
			    }
			});
		}
		//load the name refs on demand, not now
		
	}
	private void loadNameIndex(){
		if(nameIndexRef != null){
			nameIndex = ofy().transact(new Work<TreesIndexedByString>() {
			    public TreesIndexedByString run() {
			    	return ofy().load().ref(nameIndexRef).getValue();
			    }
			});
		}
		if(nameIndex == null){
			nameIndex = new TreesIndexedByString("nameIndex");
		}
		nameIndex.setStringProvider(TreeToStringFactory.getTreeToChunkedID());
		nameIndex.setBinTracking(false);
	}
	
	private void Init(){
		LOG.setLevel(Level.INFO);
		loadRefs();
		if(speciesIndex == null){
			speciesIndex = new TreesIndexedByString("speciesIndex");
		}
		if(streetIndex == null){
			streetIndex = new TreesIndexedByString("streetIndex");
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
		if(spatialIndex == null){
			spatialIndex = new TreesIndexedByString("spatialIndex");
		}
	
		speciesIndex.setStringProvider(TreeToStringFactory.getTreeToSpecies());
		streetIndex.setStringProvider(TreeToStringFactory.getTreeToStdStreet());
		genusIndex.setStringProvider(TreeToStringFactory.getTreeToGenus());
		commonNameIndex.setStringProvider(TreeToStringFactory.getTreeToCommonName());
		neighbourhoodIndex.setStringProvider(TreeToStringFactory.getTreeToNeighbourhood());
		spatialIndex.setStringProvider(TreeToStringFactory.getBinner());
		spatialIndex.setBinTracking(false);
		
		stringIndices = new ArrayList<TreesIndexedByString>();
		
		stringIndices.add(speciesIndex);
		stringIndices.add(streetIndex);
		stringIndices.add(genusIndex);
		stringIndices.add(commonNameIndex);
		stringIndices.add(neighbourhoodIndex);
		stringIndices.add(spatialIndex);
		
		keywordBins = new HashMap<String, Set<String>>();
		keywordRefs = new HashMap<String, Set<Ref<PersistentFile>>>();
		for(TreesIndexedByString index: stringIndices){
			AddAllBinsToKeyWords(index.getBinEntries());
			AddAllRefsToKeyWords(index.getRefEntries());
		}
		
	}
	
	public void putTree(TreeData tree){
		for(TreesIndexedByString index: stringIndices){
			String key = index.getKeyForTree(tree);
			String bin = TreeToStringFactory.getBinner().treeToString(tree);
			Ref<PersistentFile> ref = index.addTree(tree);
			if(!key.equals(bin)){
				AddSingleBinToKeywords(key, bin);
				AddRefToKeywords(key, ref);
			}
		}
		saveTrees();
	}
	public void putTreesByGenus(Collection<TreeData> trees){
		genusIndex.addTrees(trees);
		AddAllBinsToKeyWords(genusIndex.getBinEntries());
		AddAllRefsToKeyWords(genusIndex.getRefEntries());
		saveTrees();
	}
	public void putTreesBySpecies(Collection<TreeData> trees){
		speciesIndex.addTrees(trees);
		AddAllBinsToKeyWords(speciesIndex.getBinEntries());
		AddAllRefsToKeyWords(speciesIndex.getRefEntries());
		saveTrees();
	}
	public void putTreesByStreet(Collection<TreeData> trees){
		streetIndex.addTrees(trees);
		AddAllBinsToKeyWords(streetIndex.getBinEntries());
		AddAllRefsToKeyWords(streetIndex.getRefEntries());
		saveTrees();
	}
	public void putTreesByCommonName(Collection<TreeData> trees){
		commonNameIndex.addTrees(trees);
		AddAllBinsToKeyWords(commonNameIndex.getBinEntries());
		AddAllRefsToKeyWords(commonNameIndex.getRefEntries());
		saveTrees();
	}
	public void putTreesByNeighbourhood(Collection<TreeData> trees){
		neighbourhoodIndex.addTrees(trees);
		AddAllBinsToKeyWords(neighbourhoodIndex.getBinEntries());
		AddAllRefsToKeyWords(neighbourhoodIndex.getRefEntries());
		saveTrees();
	}
	public void putTreesBySpatialBin(Collection<TreeData> trees){
		spatialIndex.addTrees(trees);
		saveTrees();
	}
	public void putTreesByName(Collection<TreeData> trees){
		if(nameIndex == null){
			loadNameIndex();
		}
		nameIndex.addTrees(trees);
		saveTrees();
	}
	
	static public void saveTrees(final TreeDepot depot){
		ofy().transact(new VoidWork() {
			public void vrun() {
				ofy().save().entity(depot).now();
			}
		});
	}
	
	public Set<String> getBinSet(){
		return spatialIndex.getKeySet();
	}
	
	public void saveTrees(){
    	speciesIndexRef = Ref.create(speciesIndex);
		streetIndexRef = Ref.create(streetIndex);
		genusIndexRef = Ref.create(genusIndex);
		commonNameIndexRef = Ref.create(commonNameIndex);
		neighbourhoodIndexRef = Ref.create(neighbourhoodIndex);
		spatialIndexRef = Ref.create(spatialIndex);
		if(nameIndex != null){
			nameIndexRef = Ref.create(nameIndex);
		}
		saveTrees(this);
	}
	
	public TreeRequest newRequest(){
		return new TreeRequest(this);
	}
	
	public TreeData getTreeByID(String exact_id){
		if(nameIndex == null){
			loadNameIndex();
		}
		if(exact_id != null){
			SortedSet<TreeData> set = nameIndex.getAllTreesWith(TreeToStringFactory.chunkedID(exact_id));
			for(TreeData tree: set){
				if(tree.getID() != null && tree.getID().equals(exact_id)){
					return tree;
				}
			}
		}
		return null;
	}
	
	public Set<Ref<PersistentFile>> getAllRefsInBins(Set<String> bins){
		return Collections.unmodifiableSet(spatialIndex.getAllMatching(bins));
	}
	
	public SortedSet<TreeData> getTreesWithSpecies(String species){
		return Collections.unmodifiableSortedSet(speciesIndex.getAllTreesWith(species.toUpperCase()));
	}
	
	public SortedSet<TreeData> getTreesWithStreet(String street){
		return Collections.unmodifiableSortedSet(streetIndex.getAllTreesWith(street.toUpperCase()));
	}
	
	public SortedSet<TreeData> getTreesWithGenus(String street){
		return Collections.unmodifiableSortedSet(genusIndex.getAllTreesWith(street.toUpperCase()));
	}

	public SortedSet<TreeData> getTreesWithCommonName(String street){
		return Collections.unmodifiableSortedSet(commonNameIndex.getAllTreesWith(street.toUpperCase()));
	}
	
	public SortedSet<TreeData> getTreesWithNeighbourHood(String street){
		return Collections.unmodifiableSortedSet(neighbourhoodIndex.getAllTreesWith(street.toUpperCase()));
	}
	
	Collection<Ref<PersistentFile>>  getAllSpeciesRefsWith(String species){
		return speciesIndex.getAllRefsWith(species.toUpperCase());
	}
	
	Collection<Ref<PersistentFile>>  getAllStreetRefsWith(String street){
		return streetIndex.getAllRefsWith(street.toUpperCase());
	}
	
	Collection<Ref<PersistentFile>>  getAllGenusRefsWith(String street){
		return genusIndex.getAllRefsWith(street.toUpperCase());
	}

	Collection<Ref<PersistentFile>>  getAllCommonNameRefsWith(String street){
		return commonNameIndex.getAllRefsWith(street.toUpperCase());
	}
	
	Collection<Ref<PersistentFile>>  getAllNeighbourhoodRefsWith(String street){
		return neighbourhoodIndex.getAllRefsWith(street.toUpperCase());
	}
	
	Set<String>  getAllSpeciesBinsWith(String species){
		return speciesIndex.getAllBinsWith(species.toUpperCase());
	}
	
	Set<String>  getAllStreetBinsWith(String street){
		return streetIndex.getAllBinsWith(street.toUpperCase());
	}
	
	Set<String>  getAllGenusBinsWith(String street){
		return genusIndex.getAllBinsWith(street.toUpperCase());
	}

	Set<String>  getAllCommonNameBinsWith(String street){
		return commonNameIndex.getAllBinsWith(street.toUpperCase());
	}
	
	Set<String>  getAllNeighbourhoodBinsWith(String street){
		return neighbourhoodIndex.getAllBinsWith(street.toUpperCase());
	}
	
	public static void loadAllRefs(Collection<Ref<PersistentFile>> refs){
		ofy().load().refs(refs);
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
		return Collections.unmodifiableSet(keywordBins.keySet());
	}
	
	@SuppressWarnings("unchecked")
	public static SortedSet<TreeData> deSerializeAllRefs( Collection <Ref<PersistentFile>> reflist, int max_results){
		SortedSet<TreeData> ret = new TreeSet<TreeData>(); 
		for(Ref<PersistentFile> ref: reflist ){
			PersistentFile pFile = ref.get();
			byte[] b = pFile.load();
			try {
				ByteArrayInputStream byte_stream = new ByteArrayInputStream(b);
				InflaterInputStream inflater = new InflaterInputStream(byte_stream);
				InputStream source = inflater;
				ObjectInputStream in = new ObjectInputStream(source);
				SortedSet<TreeData> treeSet = (SortedSet<TreeData>) in.readObject();
				ret.addAll(treeSet);
				in.close();
				b = null;
				pFile = null;
			} catch (IOException e) {
				throw new RuntimeException("IOException while deserializing " + ref.toString() + "\n\t"+e.getMessage(), e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("ClassNotFoundException while deserializing " + ref.toString() + "\n\t"+e.getMessage(), e);
			}
			if(ret.size() > max_results)
			{
				break;
			}
		}
		//Collections.addAll(ret, kryo.readObject(input, TreeData2[].class));
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static SortedSet<TreeData> deSerializeAllRefsToSet( Collection <Ref<PersistentFile>> reflist, SortedSet<TreeData> tree_set){
		SortedSet<TreeData> ret = new TreeSet<TreeData>(); 
		for(Ref<PersistentFile> ref: reflist ){
			PersistentFile pFile = ref.get();
			byte[] b = pFile.load();
			try {
				ByteArrayInputStream byte_stream = new ByteArrayInputStream(b);
				InflaterInputStream inflater = new InflaterInputStream(byte_stream);
				InputStream source = inflater;
				ObjectInputStream in = new ObjectInputStream(source);
				SortedSet<TreeData> treeSet = (SortedSet<TreeData>) in.readObject();
				ret.addAll(treeSet);
				in.close();
				b = null;
				pFile = null;
			} catch (IOException e) {
				throw new RuntimeException("IOException while deserializing " + ref.toString() + "\n\t"+e.getMessage(), e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("ClassNotFoundException while deserializing " + ref.toString() + "\n\t"+e.getMessage(), e);
			}
		}
		//Collections.addAll(ret, kryo.readObject(input, TreeData2[].class));
		tree_set.retainAll(ret);
		return tree_set;
	}
	
	@SuppressWarnings("unchecked")
	public static SortedSet<TreeData> deSerializeRef( Ref<PersistentFile> ref){
		PersistentFile pFile = ref.get();
		byte[] b = pFile.load();
		SortedSet<TreeData> ret = null;
		try {
			ByteArrayInputStream byte_stream = new ByteArrayInputStream(b);
			InflaterInputStream inflater = new InflaterInputStream(byte_stream);
			InputStream source = inflater;
			ObjectInputStream in = new ObjectInputStream(source);
			ret = (SortedSet<TreeData>) in.readObject();
			in.close();
			b = null;
			pFile = null;
		} catch (IOException e) {
			throw new RuntimeException("IOException while deserializing " + ref.toString() + "\n\t"+e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("ClassNotFoundException while deserializing " + ref.toString() + "\n\t"+e.getMessage(), e);
		}
		//Collections.addAll(ret, kryo.readObject(input, TreeData2[].class));
		return ret;
	}
	
}
