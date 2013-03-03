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
	static public ClientTreeData makeUserTreeData(TreeData tree_data) {
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

	static public AdminTreeData makeAdminTreeData(TreeData tree_data) {
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
