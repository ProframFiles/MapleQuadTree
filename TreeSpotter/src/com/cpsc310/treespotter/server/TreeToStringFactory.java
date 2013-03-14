/**
 * 
 */
package com.cpsc310.treespotter.server;

/**
 * @author aleksy
 *
 */
public class TreeToStringFactory {
	static TreeStringProvider getTreeToGenus(){
		TreeStringProvider tsp = new TreeStringProvider(){

			@Override
			public String treeToString(TreeDataProvider tree) {
				return tree.getGenus();
			}
			
		};
		return tsp;
	}
	static TreeStringProvider getTreeToSpecies(){
		TreeStringProvider tsp = new TreeStringProvider(){

			@Override
			public String treeToString(TreeDataProvider tree) {
				return tree.getSpecies();
			}
			
		};
		return tsp;
	}
	static TreeStringProvider getTreeToNeighbourhood(){
		TreeStringProvider tsp = new TreeStringProvider(){

			@Override
			public String treeToString(TreeDataProvider tree) {
				return tree.getNeighbourhood();
			}
			
		};
		return tsp;
	}
	static TreeStringProvider getTreeToStdStreet(){
		TreeStringProvider tsp = new TreeStringProvider(){

			@Override
			public String treeToString(TreeDataProvider tree) {
				return tree.getStdStreet();
			}
			
		};
		return tsp;
	}
	static TreeStringProvider getTreeToCommonName(){
		TreeStringProvider tsp = new TreeStringProvider(){

			@Override
			public String treeToString(TreeDataProvider tree) {
				return tree.getCommonName();
			}
			
		};
		return tsp;
	}
	static TreeStringProvider getTreeToKeywords(){
		TreeStringProvider tsp = new TreeStringProvider(){

			@Override
			public String treeToString(TreeDataProvider tree) {
				return tree.getKeywordString();
			}
			
		};
		return tsp;
	}
}
