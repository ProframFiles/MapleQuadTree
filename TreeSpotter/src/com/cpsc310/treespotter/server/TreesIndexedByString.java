/**
 * 
 */
package com.cpsc310.treespotter.server;
import static com.cpsc310.treespotter.server.OfyService.ofy;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Result;
import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.OnSave;
import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Unindex;


/**
 * @author maple-quadtree
 * meant to act as a sort of a handle to the serialized tree blobs
 */
@Embed
public class TreesIndexedByString {
	
	@Id String id;
	@Unindex Map<String, Ref<PersistentFile>> blobRefs;
	@Ignore Map<String, SortedSet<TreeData2>> treeMap = new LinkedHashMap<String, SortedSet<TreeData2>>();
	@Ignore Kryo kryoInstance = null;
	@Ignore TreeStringProvider tsp = null;
	
	//for use by objectify
	@SuppressWarnings("unused")
	private TreesIndexedByString(){
		
	}
	
	public TreesIndexedByString(String id){
		this.id = id;
		blobRefs = new LinkedHashMap<String, Ref<PersistentFile>>();
	}
	
	public void setStringProvider(TreeStringProvider provider){
		tsp = provider;
	}
	
	
	public void addTrees(Collection<TreeData2> trees){
		
		for(TreeData2 tree: trees){
			String key = tsp.treeToString(tree);
			SortedSet<TreeData2> tree_set = null;
			tree_set = loadOrAddList(key);
			boolean added = tree_set.add(tree);
			if(!added){
				tree_set.remove(tree);
				tree_set.add(tree);
			}
			
		}
	}
	
	public void addTree(TreeData2 tree){
		
	}
	
	public void modifyTree(TreeData2 tree){
		
	}
	
	public Set<String> getKeySet(){
		return blobRefs.keySet();
	}
	
	public SortedSet<TreeData2> getMatchingTrees(String s){
		return loadList(s);
	}
	
	// need to perform unchecked conversions when deserializing 
	// Collections
	private SortedSet<TreeData2> loadOrAddList(String key){
		SortedSet<TreeData2> ret = null;
		ret = treeMap.get(key);
		if(ret == null){
			ret = loadList(key);
			treeMap.put(key, ret);
		}
		return ret;
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
		Kryo kryo = getKryo();
		PersistentFile pFile = ref.get();
		byte[] b = pFile.load();
		Input input = new Input(new ByteArrayInputStream(b));
		
		SortedSet<TreeData2> ret = new TreeSet<TreeData2>();
		Collections.addAll(ret, kryo.readObject(input, TreeData2[].class));
		return ret;
	}
	
	@OnSave
	public void serializeMapping(){
		Kryo kryo = getKryo();
		ArrayList<Result<Key<PersistentFile>>> results = new ArrayList<Result<Key<PersistentFile>>>();
		for(Entry<String,SortedSet<TreeData2>> tree_entry: treeMap.entrySet()){
			if(tree_entry.getValue()!=null && !tree_entry.getValue().isEmpty()){
				ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
				Output output = new Output(byte_stream);
				int set_size = tree_entry.getValue().size();
				kryo.writeObject(output, tree_entry.getValue().toArray(new TreeData2[set_size]));
				output.close();
				byte[] b = byte_stream.toByteArray();
				PersistentFile f = new PersistentFile(id+tree_entry.getKey());
				f.save(new ByteArrayInputStream(b));
				blobRefs.put(tree_entry.getKey(),Ref.create(f));
				results.add(ofy().save().entity(f));
			}
		}
		for(Result<Key<PersistentFile>> r: results){
			r.now();
		}
	}
	
	private Kryo getKryo(){
		if(kryoInstance == null){
			kryoInstance = new Kryo();
			kryoInstance.register(TreeData2.class);
			kryoInstance.register(TreeData2[].class);
		}
		return kryoInstance;
	}
	
}
