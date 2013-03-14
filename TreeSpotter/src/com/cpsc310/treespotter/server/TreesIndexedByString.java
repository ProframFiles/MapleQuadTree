/**
 * 
 */
package com.cpsc310.treespotter.server;
import static com.cpsc310.treespotter.server.OfyService.ofy;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esotericsoftware.kryo.Kryo;

import com.googlecode.objectify.Ref;
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
	@Ignore Map<String, SortedSet<TreeData2>> bulkMap;
	@Ignore Map<String, SortedSet<TreeData2>> treeMap = new TreeMap<String, SortedSet<TreeData2>>();
	@Ignore Kryo kryoInstance = null;
	@Ignore TreeStringProvider tsp = null;
	@Ignore  ArrayList<Ref<PersistentFile>> futuresList = null;
	
	//for use by objectify
	@SuppressWarnings("unused")
	private TreesIndexedByString(){
		LOG.setLevel(Level.FINE);
	}
	
	public TreesIndexedByString(String id){
		LOG.setLevel(Level.FINE);
		this.id = id;
		blobRefs = new LinkedHashMap<String, Ref<PersistentFile>>();
	}
	
	public void setStringProvider(TreeStringProvider provider){
		tsp = provider;
	}
	
	
	public void addTrees(Collection<TreeData2> trees){
		int create_count = 0;
		for(TreeData2 tree: trees){
			String key = filterKey(tsp.treeToString(tree));
			SortedSet<TreeData2> tree_set = null;
			tree_set = treeMap.get(key);
			
			if(tree_set == null){
				tree_set = new TreeSet<TreeData2>();
				treeMap.put(key, tree_set);
				create_count++;
			}
			boolean added = tree_set.add(tree);
			if(!added){
				tree_set.remove(tree);
				tree_set.add(tree);
			}
		}
		LOG.fine("\n\tCreated " + create_count + " new keys for " + id + "\n\t" + treeMap.size() +" to serialize");
		mergeWithDatastore();
		serializeMapping();
		ofy().save().entity(this).now();
	}
	
	public void addTreesSplit(Collection<TreeData2> trees){
		int create_count = 0;
		for(TreeData2 tree: trees){
			String[] keys = filterKey(tsp.treeToString(tree)).trim().split(" +");
			for(String key: keys){
				SortedSet<TreeData2> tree_set = null;
				tree_set = treeMap.get(key);
				
				if(tree_set == null){
					tree_set = new TreeSet<TreeData2>();
					treeMap.put(key, tree_set);
					create_count++;
				}
				boolean added = tree_set.add(tree);
				if(!added){
					tree_set.remove(tree);
					tree_set.add(tree);
				}
			}
		}
		LOG.fine("\n\tCreated " + create_count + " new keys for " + id + "\n\t" + treeMap.size() +" to serialize");
		mergeWithDatastore();
		serializeMapping();
		ofy().save().entity(this).now();
		LOG.fine("\n\tCurrently " + blobRefs.size() + " keys in the " + id +" index");
	}
	
	private String filterKey(String s){
		return s.replace('.','_');
	}
	
	public void addTree(TreeData2 tree){
		//TODO: implement
	}
	
	public void modifyTree(TreeData2 tree){
		//TODO: implement
	}
	
	public Set<String> getKeySet(){
		return blobRefs.keySet();
	}
	
	public SortedSet<TreeData2> getMatchingTrees(String s){
		if(s == null){
			return null;
		}
		return loadList(filterKey(s));
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
		ofy().load().refs(futuresList);
	}
	
	public SortedSet<TreeData2> getAllTreesWith(String s){
		getAllTreesWithAsync(s);
		return completeRequest();
	}
	
	public SortedSet<TreeData2> completeRequest(){
		SortedSet<TreeData2> ret = new TreeSet<TreeData2>();
		if(futuresList != null){
			for( Ref<PersistentFile> ref: futuresList ){
				ret.addAll(deSerializeRef(ref));
			}
			futuresList.clear();
		}
		return ret;
	}
	
	
	@SuppressWarnings("unchecked")
	private SortedSet<TreeData2> deSerializeRef( Ref<PersistentFile> ref){
		PersistentFile pFile = ref.get();
		byte[] b = pFile.load();
		SortedSet<TreeData2> ret = null;
		try {
		//	InflaterInputStream inflater = new InflaterInputStream;
			ObjectInputStream in = new ObjectInputStream((new ByteArrayInputStream(b)));
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
			SortedSet<TreeData2> set = deSerializeRef(reflist.get(i));
			treeMap.get(keylist.get(i)).addAll(set); 
		}
	}
	
	private SortedSet<TreeData2> loadList(String key){
		Ref<PersistentFile> ref = blobRefs.get(key);
		if(ref != null){
			return loadList(ref);
		}
		else{
			return new TreeSet<TreeData2>();
		}
	}
	
	private SortedSet<TreeData2> loadList(Ref<PersistentFile> ref){
		ofy().load().ref(ref);
		return deSerializeRef(ref);
	}
	
	public void serializeMapping(){
		serializeMapping(0, Integer.MAX_VALUE);
	}
	
	public int serializeMapping(int start, int num){
		int count = 0;
		int written = 0;
		int max_bytes = 4*1024;
		Map<String, PersistentFile> keys_w_files = new HashMap<String, PersistentFile>();
		for(Entry<String,SortedSet<TreeData2>> tree_entry: treeMap.entrySet()){
			if(tree_entry.getValue()!=null && !tree_entry.getValue().isEmpty()){
				if(count >= start && count < start+num){
					ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
					//DeflaterOutputStream deflater = new DeflaterOutputStream(byte_stream);
					ObjectOutputStream out;
					byte[] b;
					try {
						out = new ObjectOutputStream(byte_stream);
						out.writeObject(tree_entry.getValue());
						out.close();
						byte_stream.flush();
						b= byte_stream.toByteArray();
						byte_stream.close();
						//deflater.flush();
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
						for(Entry<String, PersistentFile> entry: keys_w_files.entrySet()){
							//LOG.fine("\n\tPutting " + entry.getKey() +" into datastore");
							blobRefs.put(entry.getKey(), Ref.create(entry.getValue().completeSave().now()));
						//	LOG.info("\n\tSerialized " + written +" bytes in " + keys_w_files.size() + " files for " + id);
						}
						
						keys_w_files.clear();
						written = 0;
					}
				}
				count++;
			}
			
		}
		if(written > 0){
			for(Entry<String, PersistentFile> entry: keys_w_files.entrySet()){
				//LOG.fine("\n\tPutting " + entry.getKey() +" into datastore");
				blobRefs.put(entry.getKey(), Ref.create(entry.getValue().completeSave().now()));
			}
			//LOG.info("\n\tSerialized " + written +" bytes in " + keys_w_files.size() + " files for " + id);
			keys_w_files.clear();
			written = 0;
		}
		//int remaining = Math.max( treeMap.size() - start+num, 0);
		treeMap.clear();
		return written;
	}
	
}
