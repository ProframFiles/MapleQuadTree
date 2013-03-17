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
	static TreeStringProvider getTreeToChunkedID(){
		TreeStringProvider tsp = new TreeStringProvider(){
			@Override
			public String treeToString(TreeDataProvider tree) {
				 return  chunkedID(tree.getID());
			}
			
		};
		return tsp;
	}
	static String chunkedID(String id){
		final int chunk_size = 1000;
		int int_portion = Integer.parseInt(id.substring(1));
		return id.substring(0, 1)+String.format("%0$3d",int_portion/chunk_size);
	}
	static TreeStringProvider getBinner(){
		return new TreeGridStore();
	}
}
