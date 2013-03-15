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
	@Ignore HashMap<String, ArrayList<Ref<PersistentFile>>> keywordRefs;
	
	@Ignore private TreesIndexedByString speciesIndex;
	Ref<TreesIndexedByString> speciesIndexRef;
	
	@Ignore private TreesIndexedByString streetIndex;
	Ref<TreesIndexedByString> streetIndexRef;
	
	@Ignore private TreesIndexedByString commonNameIndex;
	Ref<TreesIndexedByString> commonNameIndexRef;
	
	@Ignore private TreesIndexedByString neighbourhoodIndex;
	Ref<TreesIndexedByString> neighbourhoodIndexRef;
	
	@Ignore private TreesIndexedByString genusIndex;
	Ref<TreesIndexedByString> genusIndexRef;
	
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
	
	
	private void AddAllRefsToKeyWords(Set<Entry<String, Ref<PersistentFile>>> entries){
		for(Entry<String, Ref<PersistentFile>> entry: entries){
			if(entry.getKey() != null){
				AddRefToKeywords(entry.getKey(), entry.getValue());
			}
		}
	}
	
	private void AddRefToKeywords(String key, Ref<PersistentFile> ref){
		ArrayList<Ref<PersistentFile>> list =  keywordRefs.get(key);
		if(list == null){
			list = new ArrayList<Ref<PersistentFile>>();
			keywordRefs.put(key, list);
		}
		if(!list.contains(ref)){
			list.add(ref);
		}
	}
	Collection<Ref<PersistentFile>> getAllRefsWithKeyword(String key){
		Set<Ref<PersistentFile>> futuresSet = new HashSet<Ref<PersistentFile>>();
		for(Entry<String, ArrayList<Ref<PersistentFile>>> entry: keywordRefs.entrySet()){
			if(entry.getKey().contains(key)){
				futuresSet.addAll(entry.getValue());
			}
		}
		return futuresSet;
	}
	
	public void addAllRefsToRequest( Set<Ref<PersistentFile>> currentRequest ){
		for(Entry<String, ArrayList<Ref<PersistentFile>>> entry: keywordRefs.entrySet()){
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
	
		speciesIndex.setStringProvider(TreeToStringFactory.getTreeToSpecies());
		streetIndex.setStringProvider(TreeToStringFactory.getTreeToStdStreet());
		genusIndex.setStringProvider(TreeToStringFactory.getTreeToGenus());
		commonNameIndex.setStringProvider(TreeToStringFactory.getTreeToCommonName());
		neighbourhoodIndex.setStringProvider(TreeToStringFactory.getTreeToNeighbourhood());
		
		stringIndices = new ArrayList<TreesIndexedByString>();
		
		stringIndices.add(speciesIndex);
		stringIndices.add(streetIndex);
		stringIndices.add(genusIndex);
		stringIndices.add(commonNameIndex);
		stringIndices.add(neighbourhoodIndex);
		
		keywordRefs = new HashMap<String, ArrayList<Ref<PersistentFile>>>();
		for(TreesIndexedByString index: stringIndices){
			AddAllRefsToKeyWords(index.getRefEntries());
		}
		
	}
	
	public void putTree(TreeData2 tree){
		for(TreesIndexedByString index: stringIndices){
			String key = index.getKeyForTree(tree);
			Ref<PersistentFile> ref = index.addTree(tree);
			AddRefToKeywords(key, ref);
		}
		saveTrees();
	}
	public void putTreesByGenus(Collection<TreeData2> trees){
		genusIndex.addTrees(trees);
		AddAllRefsToKeyWords(genusIndex.getRefEntries());
		saveTrees();
	}
	public void putTreesBySpecies(Collection<TreeData2> trees){
		speciesIndex.addTrees(trees);
		AddAllRefsToKeyWords(speciesIndex.getRefEntries());
		saveTrees();
	}
	public void putTreesByStreet(Collection<TreeData2> trees){
		streetIndex.addTrees(trees);
		AddAllRefsToKeyWords(streetIndex.getRefEntries());
		saveTrees();
	}
	public void putTreesByCommonName(Collection<TreeData2> trees){
		commonNameIndex.addTrees(trees);
		AddAllRefsToKeyWords(commonNameIndex.getRefEntries());
		saveTrees();
	}
	public void putTreesByNeighbourhood(Collection<TreeData2> trees){
		neighbourhoodIndex.addTrees(trees);
		AddAllRefsToKeyWords(neighbourhoodIndex.getRefEntries());
		saveTrees();
	}
	
	static public void saveTrees(final TreeDepot depot){
		ofy().transact(new VoidWork() {
			public void vrun() {
				ofy().save().entity(depot).now();
			}
		});
	}
	
	public void saveTrees(){
    	speciesIndexRef = Ref.create(speciesIndex);
		streetIndexRef = Ref.create(streetIndex);
		genusIndexRef = Ref.create(genusIndex);
		commonNameIndexRef = Ref.create(commonNameIndex);
		neighbourhoodIndexRef = Ref.create(neighbourhoodIndex);
		saveTrees(this);
	}
	
	public TreeRequest newRequest(){
		return new TreeRequest(this);
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
	
	Collection<Ref<PersistentFile>> getAllKeywordRefsWith(String kw){
		return getAllRefsWithKeyword(kw);
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
		return Collections.unmodifiableSet(keywordRefs.keySet());
	}
	
	@SuppressWarnings("unchecked")
	public static SortedSet<TreeData2> deSerializeAllRefs( Collection <Ref<PersistentFile>> reflist, int max_results){
		SortedSet<TreeData2> ret = new TreeSet<TreeData2>(); 
		for(Ref<PersistentFile> ref: reflist ){
			PersistentFile pFile = ref.get();
			byte[] b = pFile.load();
			try {
				ByteArrayInputStream byte_stream = new ByteArrayInputStream(b);
				InflaterInputStream inflater = new InflaterInputStream(byte_stream);
				InputStream source = inflater;
				ObjectInputStream in = new ObjectInputStream(source);
				SortedSet<TreeData2> treeSet = (SortedSet<TreeData2>) in.readObject();
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
	public static SortedSet<TreeData2> deSerializeRef( Ref<PersistentFile> ref){
		PersistentFile pFile = ref.get();
		byte[] b = pFile.load();
		SortedSet<TreeData2> ret = null;
		try {
			ByteArrayInputStream byte_stream = new ByteArrayInputStream(b);
			InflaterInputStream inflater = new InflaterInputStream(byte_stream);
			InputStream source = inflater;
			ObjectInputStream in = new ObjectInputStream(source);
			ret = (SortedSet<TreeData2>) in.readObject();
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