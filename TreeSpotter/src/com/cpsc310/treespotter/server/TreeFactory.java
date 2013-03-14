/**
 * 
 */
package com.cpsc310.treespotter.server;

import com.cpsc310.treespotter.client.AdminTreeData;
import com.cpsc310.treespotter.client.ClientTreeData;

/**
 * @author maple-quadtree
 *
 */
public class TreeFactory {
	static public ClientTreeData makeUserTreeData(TreeDataProvider tree_data) {
		ClientTreeData user_data = new ClientTreeData();
		user_data.setTreeID(tree_data.getID().toString());
		user_data.setCivicNumber(tree_data.getCivicNumber());
		user_data.setNeighbourhood(tree_data.getNeighbourhood());
		user_data.setStreet(tree_data.getStreet());
		user_data.setHeightRange(tree_data.getHeightRange());
		user_data.setDiameter(tree_data.getDiameter());
		user_data.setPlanted(tree_data.getPlanted());
		user_data.setCultivar(tree_data.getCultivar());
		user_data.setGenus(tree_data.getGenus());
		user_data.setSpecies(tree_data.getSpecies());
		user_data.setCommonName(tree_data.getCommonName());
		return user_data;
	}
	static public  TreeData makeTreeData(ClientTreeData tree_data, int id_number) {
		TreeData server_data = new TreeData("U", id_number);
		server_data.setCivicNumber(tree_data.getCivicNumber());
		server_data.setNeighbourhood(tree_data.getNeighbourhood());
		server_data.setStreet(tree_data.getStreet());
		server_data.setHeightRange(tree_data.getHeightRange());
		server_data.setDiameter(tree_data.getDiameter());
		server_data.setPlanted(tree_data.getPlanted());
		server_data.setCultivar(tree_data.getCultivar());
		server_data.setGenus(tree_data.getGenus());
		server_data.setSpecies(tree_data.getSpecies());
		server_data.setCommonName(tree_data.getCommonName());
		return server_data;
	}
	
	static public TreeData2 makeTreeData2(String[] values){
		//thanks for this handy method, Samantha
		TreeData2 tree = new TreeData2();
		tree.setID("V",Integer.parseInt(values[0]) );
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
		tree.makeKeywordString();
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

	static public AdminTreeData makeAdminTreeData(TreeDataProvider tree_data) {
		// TODO: differentiate this from user data
		AdminTreeData admin_data = new AdminTreeData();
		admin_data.setTreeID(tree_data.getID().toString());
		admin_data.setCivicNumber(tree_data.getCivicNumber());
		admin_data.setNeighbourhood(tree_data.getNeighbourhood());
		admin_data.setStreet(tree_data.getStreet());
		admin_data.setHeightRange(tree_data.getHeightRange());
		admin_data.setDiameter(tree_data.getDiameter());
		admin_data.setPlanted(tree_data.getPlanted());
		admin_data.setCultivar(tree_data.getCultivar());
		admin_data.setGenus(tree_data.getGenus());
		admin_data.setSpecies(tree_data.getSpecies());
		admin_data.setCommonName(tree_data.getCommonName());
		return admin_data;
	}
}
