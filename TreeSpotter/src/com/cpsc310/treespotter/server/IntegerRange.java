/**
 * 
 */
package com.cpsc310.treespotter.server;

/**
 * @author maple-quadtree
 *
 */
public class IntegerRange {
	private int top = 0;
	private int bottom = 0;
	public IntegerRange(String pair){
		int split = pair.indexOf('-');
		if( split > 0 ){
			setBottom(Integer.parseInt(pair.substring(0, split)));
			setTop(Integer.parseInt(pair.substring(split)));
		}
	}
	public int getTop() {
		return top;
	}
	public void setTop(int top) {
		this.top = top;
	}
	public int getBottom() {
		return bottom;
	}
	public void setBottom(int bottom) {
		this.bottom = bottom;
	}
}
