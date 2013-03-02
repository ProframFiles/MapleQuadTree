package com.cpsc310.treespotter.client;

import java.util.ArrayList;

import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler.MarkerClickEvent;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.user.client.ui.*;

public class TreeSearchMap {
	public static final int ZOOM_LVL = 15;
	private ArrayList<Label> markerLabels;
	private ArrayList<Marker> markers = new ArrayList<Marker>();
	private ArrayList<ClientTreeData> treeList;
	private Geocoder geo = null;
	private int listIndex = 0;
	private final String greenIconURL = "http://maps.gstatic.com/mapfiles/ridefinder-images/mm_20_green.png";
	private Icon icon;
	private MapWidget map;
	private boolean isPopulated = true;
	private LatLng start;

	public TreeSearchMap() {
	    start = LatLng.newInstance(49.26102, -123.249339);
		geo = new Geocoder();
		icon = Icon.newInstance(greenIconURL);
		icon.setIconAnchor(Point.newInstance(6, 20));
	}

	public MapWidget getMap() {
		// not done reading treeList
		// TODO check for race condition
		if (!isPopulated) {
			map = new MapWidget(start, ZOOM_LVL);
		}
		else {
			map = new MapWidget();
			map.setSize("400px", "400px");
			map.setUIToDefault();
	
			// TODO: find middle of all points for centre
			map.setCenter(start, ZOOM_LVL);
			
			for (Marker m : markers) {
				map.addOverlay(m);
			}
		}
		return map;
	}

	public void setPoints(ArrayList<ClientTreeData> list) {
		treeList = list;
		listIndex = 0;
		isPopulated = false;
		getNextPoint(listIndex);
	}
	
	private void getNextPoint(int idx) {
		if (idx >= treeList.size()) {
			isPopulated = true;
			return;
		}
		
		ClientTreeData t = treeList.get(idx);
		String loc = t.getLocation() + ", Vancouver, BC";
		geo.getLatLng(loc, new LatLngCallback() {
			public void onFailure() {
				// skip to next marker
				getNextPoint(++listIndex);
			}

			public void onSuccess(LatLng pt) {
				addPoint(pt);
				// listIndex++;
			}
		});
	}

	private void addPoint(LatLng pt) {
		MarkerOptions options = MarkerOptions.newInstance();
		options.setIcon(icon);
		// TODO: replace with tree info
		options.setTitle("index: " + listIndex++);
		Marker mark = new Marker(pt, options);
		// mark.setImage("http://maps.gstatic.com/mapfiles/ridefinder-images/mm_20_green.png");
		mark.addMarkerClickHandler(new MarkerClickHandler() {
			public void onClick(MarkerClickEvent event) {
				clickMarker(event.getSender());
			}
		});
		markers.add(mark);

		getNextPoint(listIndex);
	}

	private void clickMarker(Marker m) {
		LatLng pt = m.getLatLng();
		map.getInfoWindow().open(
				pt,
				new InfoWindowContent("<p>" + pt.getLatitude() + ", "
						+ pt.getLongitude() + "<br/>" + m.getTitle() + "</p>"));
	}

}
