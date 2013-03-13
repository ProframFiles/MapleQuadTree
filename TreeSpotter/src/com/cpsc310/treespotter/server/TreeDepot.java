/**
 * 
 */
package com.cpsc310.treespotter.server;
import static com.cpsc310.treespotter.server.OfyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Embed;
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
public class TreeDepot {
	static TreeDepot instance = null;
	@Id private String id;
	@Ignore private TreesIndexedByString speciesIndex;
	@Load Ref<TreesIndexedByString> speciesIndexRef;
	
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
		speciesIndex = new TreesIndexedByString("speciesIndex");
		Init();
	}
	
	@OnLoad
	private void Init(){
		if(speciesIndex == null){
			speciesIndex = speciesIndexRef.safeGet();
		}
		speciesIndex.setStringProvider(TreeToStringFactory.getTreeToSpecies());
	}
	
	public void putTrees(Collection<TreeData2> trees){
		speciesIndex.addTrees(trees);
	}
	
	public void saveTrees(){
		speciesIndex.serializeMapping();
		ofy().save().entity(speciesIndex).now();
		speciesIndexRef = Ref.create(speciesIndex);
		//speciesIndex.serializeMapping();
		ofy().save().entity(this).now();
	}
	
	public SortedSet<TreeData2> getTreesMatchingSpecies(String species){
		return Collections.unmodifiableSortedSet(speciesIndex.getMatchingTrees(species.toUpperCase()));
	}
	
	public Set<String> getSpeciesSet(){
		return Collections.unmodifiableSet(speciesIndex.getKeySet());
	}

}
