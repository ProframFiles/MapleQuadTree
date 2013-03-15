/**
 * 
 */
package com.cpsc310.treespotter.server;
import static com.cpsc310.treespotter.server.OfyService.ofy;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;

import com.esotericsoftware.kryo.Kryo;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Unindex;


/**
 * @author maple-quadtree
 * meant to act as a sort of a handle to the serialized tree blobs
 */
@Entity
@Cache
public class TreesIndexedByString {
	private static final Logger LOG = Logger.getLogger("TreeIndex");
	@Id String id;
	@Unindex Map<String, Ref<PersistentFile>> blobRefs;
	@Ignore Map<String, SortedSet<TreeData>> bulkMap;
	@Ignore Map<String, SortedSet<TreeData>> treeMap = new TreeMap<String, SortedSet<TreeData>>();
	@Ignore Kryo kryoInstance = null;
	@Ignore TreeStringProvider tsp = null;
	@Ignore  ArrayList<Ref<PersistentFile>> futuresList = null;
	
	static public void saveIndexState(final TreesIndexedByString indx){
		ofy().transact(new VoidWork() {
			public void vrun() {
				ofy().save().entity(indx).now();
			}
		});
	}
	
	//for use by objectify
	@SuppressWarnings("unused")
	private TreesIndexedByString(){
		LOG.setLevel(Level.FINE);
	}
	
	public TreesIndexedByString(String id){
		LOG.setLevel(Level.FINE);
		this.id = id;
		blobRefs = new LinkedHashMap<String, Ref<PersistentFile>>();
		//numStored = new LinkedHashMap<String, Integer>();
	}
	
	public void setStringProvider(TreeStringProvider provider){
		tsp = provider;
	}
	
	private int addSingleToMap(TreeData tree){
		int ret = 0;
		String key = filterKey(tsp.treeToString(tree));
		if(key != null){
			SortedSet<TreeData> tree_set = null;
			tree_set = treeMap.get(key);
			
			if(tree_set == null){
				tree_set = new TreeSet<TreeData>();
				treeMap.put(key, tree_set);
				ret ++;
			}
			boolean added = tree_set.add(tree);
			if(!added){
				tree_set.remove(tree);
				tree_set.add(tree);
			}
		}
		return ret;
	}
	
	public void addTrees(Collection<TreeData> trees){
		int create_count = 0;
		for(TreeData tree: trees){
			create_count += addSingleToMap(tree);
		}
		mergeWithDatastore();
		serializeMapping();
		LOG.fine("\n\tAdded to " + create_count + " keys for " + id + "\n\t" + blobRefs.size() +" keys total.");
		saveIndexState(this);
	}

	
	private String filterKey(String s){
		if (s == null){
			return null;
		}
		return s.replace('.','_');
	}
	public String getKeyForTree(TreeData tree){
		return filterKey(tsp.treeToString(tree));
	}
	
	public Ref<PersistentFile> addTree(TreeData tree){
		int create_count = addSingleToMap(tree);
		if(create_count >0){
			mergeWithDatastore();
			serializeMapping();
			LOG.fine("\n\tAdded to " + create_count + " keys for " + id + "\n\t" + blobRefs.size() +" keys total.");
			saveIndexState(this);
		}
		return blobRefs.get(getKeyForTree(tree));
	}
	
	public void modifyTree(TreeData tree){
		//TODO: implement
	}
	
	public Set<String> getKeySet(){
		return blobRefs.keySet();
	}
	
	public SortedSet<TreeData> getMatchingTrees(String s){
		if(s == null){
			return null;
		}
		return loadList(filterKey(s));
	}
	
	public Set< Entry<String, Ref<PersistentFile>>> getRefEntries(){
		return blobRefs.entrySet();
	}
	
	public Set<Ref<PersistentFile>> getAllRefsWith(String s){
		String key = filterKey(s);
		Set<Ref<PersistentFile>> refSet = new LinkedHashSet<Ref<PersistentFile>>();
		for(Entry<String, Ref<PersistentFile>> entry: blobRefs.entrySet()){
			if(entry.getValue()!=null && entry.getKey().contains(key) ){
				refSet.add(entry.getValue());
			}
		}
		return refSet;
	}
	
	public void getAllTreesWithAsync(String s){
		if(s == null){
			return;
		}
		String key = filterKey(s);
		futuresList = new ArrayList<Ref<PersistentFile>>();
		for(Entry<String, Ref<PersistentFile>> entry: blobRefs.entrySet()){
			if(entry.getKey().contains(key)){
				futuresList.add(entry.getValue());
			}
		}
		TreeDepot.loadAllRefs(futuresList);
	}
	
	public SortedSet<TreeData> getAllTreesWith(String s){
		getAllTreesWithAsync(s);
		return completeRequest();
	}
	
	public SortedSet<TreeData> completeRequest(){
		SortedSet<TreeData> ret = new TreeSet<TreeData>();
		if(futuresList != null){
			for( Ref<PersistentFile> ref: futuresList ){
				ret.addAll(TreeDepot.deSerializeRef(ref));
			}
			futuresList.clear();
		}
		return ret;
	}
	
	

	
	// need to perform unchecked conversions when deserializing 
	// Collections
	private void mergeWithDatastore(){
		ArrayList<Ref<PersistentFile>> reflist = new ArrayList<Ref<PersistentFile>>();
		ArrayList<String> keylist = new ArrayList<String>();
		for(String key: treeMap.keySet()){
			Ref<PersistentFile> r = blobRefs.get(key);
			if(r != null){
				reflist.add(r);
				keylist.add(key);
			}
		}
		ofy().load().refs(reflist);
		
		for(int i =0; i< reflist.size(); ++i){
			SortedSet<TreeData> set = TreeDepot.deSerializeRef(reflist.get(i));
			treeMap.get(keylist.get(i)).addAll(set); 
		}
	}
	
	private SortedSet<TreeData> loadList(String key){
		Ref<PersistentFile> ref = blobRefs.get(key);
		if(ref != null){
			return loadList(ref);
		}
		else{
			return new TreeSet<TreeData>();
		}
	}
	
	private SortedSet<TreeData> loadList(Ref<PersistentFile> ref){
		ofy().load().ref(ref);
		return TreeDepot.deSerializeRef(ref);
	}
	
	public void serializeMapping(){
		serializeMapping(0, Integer.MAX_VALUE);
	}
	
	public int serializeMapping(int start, int num){
		int count = 0;
		int written = 0;
		int max_bytes = 1;
		final Map<String, PersistentFile> keys_w_files = new HashMap<String, PersistentFile>();
		for(Entry<String,SortedSet<TreeData>> tree_entry: treeMap.entrySet()){
			if(tree_entry.getValue()!=null && !tree_entry.getValue().isEmpty()){
				if(count >= start && count < start+num){
					ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
					DeflaterOutputStream deflater = new DeflaterOutputStream(byte_stream);
					OutputStream sink = deflater;
					ObjectOutputStream out;
					byte[] b;
					try {
						out = new ObjectOutputStream(sink);
						out.writeObject(tree_entry.getValue());
						out.close();
						byte_stream.flush();
						b= byte_stream.toByteArray();
						deflater.finish();
						out.close();
					}
					catch (IOException e) {
						throw new RuntimeException("IOException while serializing " + tree_entry.getKey() + "\n\t"+e.getMessage(), e);
					}
					 
					PersistentFile f = new PersistentFile(id+tree_entry.getKey());
					//
					f.saveAsync(new ByteArrayInputStream(b));
					keys_w_files.put(tree_entry.getKey(),f);
					written += b.length;
					if(written > max_bytes){
						ofy().transact(new VoidWork() {
						    public void vrun() {
						    	for(Entry<String, PersistentFile> entry: keys_w_files.entrySet()){
									blobRefs.put(entry.getKey(), Ref.create(entry.getValue().completeSave().now()));
								}
						    }
						});
						//LOG.fine("\n\tSerialized " + written +" bytes in " + keys_w_files.size() + " files for " + id);		
						keys_w_files.clear();
						written = 0;
					}
				}
				count++;
			}
			
		}
		if(written > 0){
			ofy().transact(new VoidWork() {
			    public void vrun() {
			    	for(Entry<String, PersistentFile> entry: keys_w_files.entrySet()){
						blobRefs.put(entry.getKey(), Ref.create(entry.getValue().completeSave().now()));
					}
			    }
			});
			//LOG.info("\n\tSerialized " + written +" bytes in " + keys_w_files.size() + " files for " + id);
			keys_w_files.clear();
			written = 0;
		}
		//int remaining = Math.max( treeMap.size() - start+num, 0);
		treeMap.clear();
		return written;
	}
	
}
