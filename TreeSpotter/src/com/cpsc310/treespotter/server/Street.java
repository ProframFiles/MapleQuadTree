/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.util.ArrayList;
import java.util.Collections;

import com.cpsc310.treespotter.shared.LatLong;

/**
 * @author maple-quadtree
 *
 */
public class Street {
	
	ArrayList<StreetAddress> places = new ArrayList<StreetAddress>();
	// lazily sorted
	boolean sorted = false;
	
	public void addLocation(int civicNumber, double latcoord, double longcoord){
		StreetAddress place = new StreetAddress(civicNumber, latcoord, longcoord);
		
		places.add(place);
		Collections.sort(places);
	}
	
	private void sort(){
		Collections.sort(places);
		sorted = true;
	}
	
	public LatLong getLatLong(int civicNumber){
		if(!sorted){
			sort();
		}
		int pos = Collections.binarySearch(places, new StreetAddress(civicNumber));
		if(pos >= 0){
			return places.get(pos).getLatLong();
		}
		else if(places.size() >= 2) {
			pos = -(pos + 1);
			boolean is_odd = ((civicNumber+2)/2 == 1);
			
			int best_lower = Math.max(pos-1,0);
			int best_upper = Math.min(places.size()-1, pos);
			while((best_lower) > 0){
				int this_num = places.get(best_lower).civicNumber;
				boolean this_is_odd = ((this_num+2)/2 == 1);
				if(this_is_odd == is_odd){
					break;
				}
				best_lower--;
			}
			while((best_upper) < places.size()-1){
				int this_num = places.get(best_upper).civicNumber;
				boolean this_is_odd = ((this_num+2)/2 == 1);
				if(this_is_odd == is_odd){
					break;
				}
				best_upper++;
			}
			
			return StreetAddress.lerpLatLong(places.get(best_lower), places.get(best_upper), civicNumber);
		}
		else if(places.size() == 1) {
			return places.get(0).getLatLong();
		}
		
		return null;
	}
}
