package com.cpsc310.treespotter.client;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class TreeInfoPage extends Composite {
	
	protected static final String wikipediaSearchURL = "http://en.wikipedia.org/wiki/Special:Search/";
	
	private final String INFO_MAP_SIZE = "500px";
	private static final int ZOOM_LVL = 15;
	protected final int INFO_TAB = 0;
	protected final int PHOTO_TAB = 1;
	
	private Label invalidLoc = new Label("Tree location could not be displayed");
	
	private TreeSpotter parent;
	private VerticalPanel infoMapPanel;
	private HorizontalPanel shareLinks;
	private ClientTreeData displayTree = null;
	
	protected void setTreeSpotter(TreeSpotter p) {
		parent = p;
	}
	
	protected TreeSpotter getTreeSpotter() {
		return parent;
	}
	
	protected void setTree(ClientTreeData t) {
		displayTree = t;
	}
	
	protected ClientTreeData getTree() {
		return displayTree;
	}
	
	protected void populateTreeInfoTable(FlexTable treeInfoTable) {
		treeInfoTable.removeAllRows();

		if (displayTree == null) 
			return;
		
		treeInfoTable.setStyleName("tree-info-table");
		treeInfoTable.setSize("400px", "400px");

		createResultDataRow(TreeSpotter.GENUS, ParseUtils.capitalize(displayTree.getGenus(), false));
		createResultDataRow(TreeSpotter.SPECIES, ParseUtils.capitalize(displayTree.getSpecies(), true));
		createWikiLink(TreeSpotter.COMMON, ParseUtils.capitalize(displayTree.getCommonName(), false));
		createResultDataRow(TreeSpotter.LOCATION, ParseUtils.capitalize(displayTree.getLocation(), false));
		String neighbour = displayTree.getNeighbourhood();
		neighbour = (neighbour == null) ? neighbour : ParseUtils.capitalize(neighbour, false);
		createResultDataRow(TreeSpotter.NEIGHBOUR, neighbour);
		createResultDataRow(TreeSpotter.PLANTED, displayTree.getPlanted());
		createResultDataRow(TreeSpotter.HEIGHT, intToHeightRange(displayTree.getHeightRange()));	
	}

	protected abstract void createResultDataRow(String field, String value);
	
	protected abstract void createWikiLink(String common, String commonName);

	protected void displayComments(VerticalPanel panel, ArrayList<TreeComment> comments) {
		panel.clear();
		CommentCell cell = new CommentCell();
		CellList<TreeComment> cellList = new CellList<TreeComment>(cell);
		cellList.setRowData(comments);
		panel.setStyleName("comments-panel");
		panel.add(cellList);
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
		map.checkResizeAndCenter();
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

		LatLng  ll =data.getLatLng();
		//checking for NaN
		if(ll != null && ll.getLatitude()==ll.getLatitude() && ll.getLongitude() == ll.getLongitude()){
			setTreeInfoMap(ll);
		}

	}
	
	 /**
	  * Helper to convert Height Range from tree into a String
	  * ie. 10 - 20ft
	  * @param i int
	  */
	 protected String intToHeightRange(int range) {
		 String value;
		 if (range == -1) {
			 value = "Not available";
		 } else if (range == 10) {
			 value = "Over 10 ft";
		 } else {
			 value = Integer.toString(range * 10) + " - "
					 + Integer.toString((range + 1) * 10) + " ft";
		 }
		 return value;
	 }

	 protected void setShareLinks(HorizontalPanel shareLinks, ClientTreeData t) {
		this.shareLinks = shareLinks;
		String baseURL = GWT.getHostPageBaseURL();
		String token = "tree" + t.getID();
		Button fbButton = new Button("Share on Facebook");
		
		fbButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String baseURL = GWT.getHostPageBaseURL();
				String url = baseURL + "#tree" + displayTree.getID();
				String name = "Vancouver Tree Spotter - " + 
							ParseUtils.capitalize(displayTree.getCommonName(), false);
				String caption = ParseUtils.capitalize(displayTree.getGenus(), false) + " " +
							ParseUtils.capitalize(displayTree.getSpecies(), true);
				String desc = "Located at " + displayTree.getCivicNumber() + " " + 
							ParseUtils.capitalize(displayTree.getStreet(), false);
				// TODO: use thumbnail of tree instead if possible
				String img = baseURL + "/image/facebook.png";
				postFacebook(url, name, caption, desc, img);
			}
		});

		shareLinks.add(fbButton);
		
	}
	 
	 @Override protected void onLoad() {
		super.onLoad();
		TreeSpotter.initSocialMedia();
	 }
	 
	 protected native void postFacebook(String url, String name, String cap, String desc, String img) /*-{
		$wnd.FB.ui({
			'appId': "438492076225696", 
			'method': "feed",
			'link': url,
			'picture': img,
			'name': name,
			'caption': cap,
			'description': desc,
			'redirect_uri': url
			});
	 }-*/;
}
