package com.cpsc310.treespotter.client;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class TreeInfoPage extends Composite {
	
	protected static final String wikipediaSearchURL = "http://en.wikipedia.org/wiki/Special:Search/";
	
	private final String INFO_MAP_SIZE = "500px";
	private static final int ZOOM_LVL = 15;
	
	private Label invalidLoc = new Label("Tree location could not be displayed");
	
	private Geocoder geo;
	private FlexTable treeInfoTable;
	private VerticalPanel infoMapPanel;
	
	public void setGeocoder(Geocoder aGeo) {
		this.geo = aGeo;
	}
	
	
	protected void populateTreeInfoTable(FlexTable treeInfoTable, ClientTreeData t) {
		this.treeInfoTable = treeInfoTable;
		treeInfoTable.removeAllRows();
		
		treeInfoTable.setStyleName("tree-info-table");
		treeInfoTable.setCellPadding(10);
		treeInfoTable.setSize("400px", "400px");

		createResultDataRow(TreeSpotter.GENUS, ParseUtils.capitalize(t.getGenus(), false));
		createResultDataRow(TreeSpotter.SPECIES, ParseUtils.capitalize(t.getSpecies(), true));
		String capName = ParseUtils.capitalize(t.getCommonName(), false);
		createResultDataRow(TreeSpotter.COMMON, "<a href='" + TreeSpotter.wikipediaSearchURL
				+ capName + "'>" + capName + "</a>");
		createResultDataRow(TreeSpotter.LOCATION, ParseUtils.capitalize(t.getLocation(), false));
		String neighbour = t.getNeighbourhood();
		neighbour = (neighbour == null) ? neighbour : neighbour.toUpperCase();
		createResultDataRow(TreeSpotter.NEIGHBOUR, neighbour);
		createResultDataRow(TreeSpotter.PLANTED, t.getPlanted());
		createResultDataRow(TreeSpotter.HEIGHT, t.getHeightRange());
		
	}
	
	/**
	 * Helper function to create rows of data for the TreeInfoPage
	 * 
	 * @param field
	 *            Data field
	 * @param value
	 *            Data value
	 * @return
	 */
	protected void createResultDataRow(String field, String value) {
		int rowNum = treeInfoTable.getRowCount();
		Label fld = new Label(field);
		
		fld.setStyleName("tree-info-field");
		treeInfoTable.setWidget(rowNum, 0, fld);

		if (value == null || value.equals("-1") || value.equals("-1.0 inches")) {
			value = "Not available";
		}
		
		treeInfoTable.setWidget(rowNum, 1, new HTML(value));
	}
	
	/**
	 * Helper function to create height data row for the TreeInfoPage
	 * 
	 * @param range
	 *            Tree height range
	 * 
	 * @return String of height range ie. "0 - 10 ft"
	 */
	 protected void createResultDataRow(String field, int range) {
		 	int rowNum = treeInfoTable.getRowCount();
		 
			Label fld = new Label(field);
			String value = "";
			fld.setStyleName("tree-info-field");
			treeInfoTable.setWidget(rowNum, 0, fld);

			if (range == -1) {
				value = "Not available";
			} else if (range == 10) {
				value = "Over 10 ft";
			} else {
				value = Integer.toString(range * 10) + " - "
						+ Integer.toString((range + 1) * 10) + " ft";
			}
			treeInfoTable.setWidget(rowNum, 1, new Label(value));
	 }

	
	
	/**
	 * Helper method for setTreeInfoMap. Sets map to coordinates, centred and
	 * zoomed
	 * 
	 * @param pt
	 *            passed in from async geocoding call
	 */
	private void setTreeInfoMap(LatLng pt) {
		if (pt == null) {
			infoMapPanel.add(invalidLoc);
			return;
		}
		MapWidget map = new MapWidget(pt, ZOOM_LVL);
		map.setSize(INFO_MAP_SIZE, INFO_MAP_SIZE);
		map.setUIToDefault();
		Marker m = new Marker(pt);
		map.addOverlay(m);
		infoMapPanel.add(map);
	}
	
	/**
	 * Sets the tree info map to the geocoded location
	 * 
	 * @param data
	 *            tree to be displayed
	 */
	protected void setTreeInfoMap(final VerticalPanel infoMapPanel, ClientTreeData data) {
		this.infoMapPanel = infoMapPanel;
		infoMapPanel.clear();
		infoMapPanel.setStyleName("tree-info-map");
		infoMapPanel.setSize(INFO_MAP_SIZE, INFO_MAP_SIZE);

		if (data == null) {
			infoMapPanel.add(invalidLoc);
			return;
		}
		// just in case city is required in search
		String loc = data.getLocation() + ", Vancouver, BC";
		System.out.println("location: " + loc);
		geo.getLatLng(loc, new LatLngCallback() {
			public void onFailure() {
				infoMapPanel.add(invalidLoc);
			}

			public void onSuccess(LatLng pt) {
				setTreeInfoMap(pt);
			}
		});

	}
}