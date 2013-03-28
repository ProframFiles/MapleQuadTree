/**
 * 
 */
package com.cpsc310.treespotter.server;

import com.cpsc310.treespotter.shared.ISharedTreeData;
import com.cpsc310.treespotter.shared.TransmittedTreeData;

/**
 * @author maple-quadtree
 *
 */
public class TreeFactory {
	static public ISharedTreeData makeUserTreeData(TreeData tree_data) {
		TransmittedTreeData user_data = new TransmittedTreeData();
		user_data.setTreeID(tree_data.getID().toString());
		user_data.setCivicNumber(tree_data.getCivicNumber());
		user_data.setNeighbourhood(tree_data.getNeighbourhood());
		user_data.setStreet(tree_data.getStdStreet());
		user_data.setHeightRange(tree_data.getHeightRange());
		user_data.setDiameter(tree_data.getDiameter());
		user_data.setPlanted(tree_data.getPlanted());
		user_data.setCultivar(tree_data.getCultivar());
		user_data.setGenus(tree_data.getGenus());
		user_data.setSpecies(tree_data.getSpecies());
		user_data.setCommonName(tree_data.getCommonName());
		user_data.setLatLong(tree_data.getLatLong());
		return user_data;
	}

	static public TreeData makeTreeData(ISharedTreeData tree_data, int id_number, String prefix) {
		TreeData server_data = new TreeData(prefix, id_number);
		server_data.setCivicNumber(tree_data.getCivicNumber());
		server_data.setNeighbourhood(tree_data.getNeighbourhood());
		server_data.setStreet(tree_data.getStreet());
		server_data.setStdStreet(tree_data.getStreet());
		server_data.setHeightRange(tree_data.getHeightRange());
		server_data.setDiameter(tree_data.getDiameter());
		server_data.setPlanted(tree_data.getPlanted());
		server_data.setCultivar(tree_data.getCultivar());
		server_data.setGenus(tree_data.getGenus());
		server_data.setSpecies(tree_data.getSpecies());
		server_data.setCommonName(tree_data.getCommonName());
		server_data.setLatLong(tree_data.getLatLong());
		return server_data;
	}
	
	static public TreeData makeTreeData2(String id_prefix, String[] values){
		//thanks for this handy method, Samantha
		TreeData tree = new TreeData(id_prefix, Integer.parseInt(values[0]) );
		tree.setCivicNumber(Integer.parseInt(values[1]));
		tree.setStdStreet(values[2]);
		tree.setNeighbourhood(values[3]);
		tree.setCell(Integer.parseInt(values[4]));
		tree.setStreet(values[5]);
		tree.setStreetBlock(values[6]);
		tree.setStreetSideName(values[7]);
		if(values[8].length() > 0){
			tree.setAssigned(valueToBoolean(values[8]));
		}
		
		tree.setHeightRange(Integer.parseInt(values[9]));
		tree.setDiameter(Float.parseFloat(values[10]));
		tree.setPlanted(null);
		tree.setPlantArea(values[12]);
		if(values[13].length() > 0){
			tree.setRootBarrier(valueToBoolean(values[13]));
		}
		if(values[14].length() > 0){
			tree.setCurb(valueToBoolean(values[14]));
		}
		tree.setCultivar(values[15]);
		tree.setGenus(values[16]);
		if(values[17].endsWith("X")){
			tree.setSpecies(values[17].substring(0,values[17].length() - 1 ).trim());
		}
		else{
			tree.setSpecies(values[17]);
		}
		tree.setCommonName(values[18]);
		//tree.makeKeywordString();
		return tree;
	}
	static private boolean valueToBoolean(String b) {
		if (b.equals("Y")){
			return true;
		}
		else if (b.equals("N")){
			return false;
		}
			
		throw new RuntimeException("can't get a boolean from " + b);
	}

	
	static public TreeData makeTestTree(String id){
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
	
}
