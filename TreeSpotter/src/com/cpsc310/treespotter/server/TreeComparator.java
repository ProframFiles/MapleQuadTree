/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.util.Comparator;

/**
 * @author aleksy
 *
 */
public class TreeComparator implements Comparator<TreeData> {

	TreeStringProvider tsp;
	
	TreeComparator(TreeStringProvider tsp){
		this.tsp = tsp;
	}
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(TreeData o1, TreeData o2) {
		return tsp.treeToString(o1).compareTo(tsp.treeToString(o2));
	}

}
