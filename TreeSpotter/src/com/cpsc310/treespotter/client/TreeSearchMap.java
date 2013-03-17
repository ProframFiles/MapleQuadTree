package com.cpsc310.treespotter.client;

import java.util.ArrayList;

import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.MarkerOptions.ZIndexProcess;
import com.google.gwt.user.client.Window;

public class TreeSearchMap{
	public static final int ZOOM_LVL = 15;
	//(aleksy) these are from the actual data, padded a bit
	static final double LAT_MAX = 49.2943;
	static final double LAT_MIN = 49.2011;
	static final double LON_MAX = -123.0237;
	static final double LON_MIN = -123.2244;
	private ArrayList<OnPageMarker> markers = new ArrayList<OnPageMarker>();
	private ArrayList<ClientTreeData> treeList;
	private Geocoder geo = null;
	private final String greenIconURL = "http://maps.gstatic.com/mapfiles/ridefinder-images/mm_20_green.png";
	private Icon icon;
	private Icon offPageIcon;
	private MapWidget map;
	private LatLng start;
	private int pageSize = 25;
	private int currentPage = 0;
	private LatLngBounds searchMapBound;

	public TreeSearchMap() {
	    start = LatLng.newInstance((LAT_MAX+LAT_MIN)*0.5, (LON_MAX+LON_MIN)*0.5);
		geo = new Geocoder();
		icon = Icon.newInstance(greenIconURL);
		icon.setIconAnchor(Point.newInstance(6, 20));
		offPageIcon = Icon.newInstance("image/icon_blend.png");
		offPageIcon.setIconAnchor(Point.newInstance(32, 32));
		
		map = new MapWidget();
		map.setSize("600px", "600px");
		map.setUIToDefault();
		createOnPageMarkers();
	}
	
	private void createOnPageMarkers(){
	}
	
	public void reset(){
		map.setCenter(start, ZOOM_LVL);
		map.clearOverlays();
	}
	
	public MapWidget getMap() {
		return map;
	}

	public void onTabPageChanged(int page){
		for(int i = pageSize*currentPage; i < (pageSize)*(currentPage+1) && i < treeList.size(); i++){
			ClientTreeData tree = treeList.get(i);
		}
		
		currentPage = page;
		for (OnPageMarker marker: markers){
			marker.clear();
		}

		markers.clear();
		
		for(int i = pageSize*currentPage; i < (pageSize)*(currentPage+1) && i < treeList.size(); i++){
			//System.out.println("Here: "+ i);
			ClientTreeData tree = treeList.get(i);
			if (tree.getMapMarker()!=null){
			//	tree.getMapMarker().setVisible(false);
				MarkerOptions options = MarkerOptions.newInstance();
				options.setIcon(icon);
				options.setTitle(Integer.toString(i+1));
				options.setZIndexProcess(new ZIndex(0.01));
				markers.add(new OnPageMarker(start, options, tree, i+1 ));
			}
		}
		
		for (OnPageMarker marker: markers){
			addMarker(marker);
			//marker.requestGoogleLocation();
		}
		
	}
	private class ZIndex implements ZIndexProcess{
		double index;
		ZIndex(double index){
			this.index=index;
		}
		@Override
		public double computeZIndex(Marker marker) {
			// TODO Auto-generated method stub
			return index;
		}
		
	}
	
	public void newSearchResults(ArrayList<ClientTreeData> list){
		reset();
		treeList = list;
		searchMapBound = LatLngBounds.newInstance();
		for(ClientTreeData tree: treeList){
			
			LatLng  ll =tree.getLatLng();
			//checking for NaN
			if(ll.getLatitude()==ll.getLatitude() && ll.getLongitude() == ll.getLongitude()){
				if(tree.getMapMarker() == null){
					MarkerOptions options = MarkerOptions.newInstance();
					options.setTitle(ParseUtils.capitalize(tree.getCommonName(), false) +
							"\n"+ ParseUtils.capitalize(tree.getGenus(), false) + " " + 
							ParseUtils.capitalize(tree.getSpecies(), true) + "\n" 
							+ ParseUtils.capitalize(tree.getLocation(), false));
					options.setIcon(offPageIcon);
					tree.setMapMarker(new Marker(tree.getLatLng(), options));
				}
				addMarker(tree.getMapMarker());
			}
		}
		
		
	}
	
	private void addMarker(Marker m) {
		
		map.addOverlay(m);
		searchMapBound.extend(m.getLatLng());
		if (!searchMapBound.isEmpty()) {
			// getBoundsZoomLevel returns ridiculously zoomed out values
			int zoom = map.getBoundsZoomLevel(searchMapBound);
			//zoom = ZOOM_LVL < zoom ? ZOOM_LVL : zoom;
			
			map.setCenter(searchMapBound.getCenter(), zoom);
		}

	}
	
	protected void handleError(Throwable error) {
		Window.alert(error.getMessage());
	}
	
	private class OnPageMarker extends Marker implements MarkerClickHandler{
			
		ClientTreeData tree = null;
		int index = -1;
		
		public OnPageMarker(LatLng point, MarkerOptions options, ClientTreeData tree, int index) {
			super(point, options);
			this.tree = tree;
			this.index = index;
			addMarkerClickHandler(this);
			
			LatLng  ll =tree.getLatLng();
			//checking for NaN
			if(ll.getLatitude()==ll.getLatitude() && ll.getLongitude() == ll.getLongitude()){
				setLatLng(ll);
				this.setVisible(true);
			}
			else{
				this.setVisible(false);
			}
		}
		public void clear(){
			setVisible(false);
			remove();
		}
		
		public void requestGoogleLocation(){
			String loc = tree.getLocation() + ", Vancouver, BC";
			geo.getLatLng(loc, new LatLngCallback() {
				public void onFailure() {
					//ignore
				}
	
				public void onSuccess(LatLng pt) {
					setLatLng(pt);
					setVisible(true);
				}
			});
		}
		
		@Override
		public void onClick(MarkerClickEvent event) {
			if(tree==null || index == -1){
				return;
			}
			LatLng pt = getLatLng();
			try {
				InfoWindowContent info = new InfoWindowContent("<p>" + index + ". "
						+ ParseUtils.capitalize(tree.getCommonName(), false) + "<br/>"
						+ ParseUtils.capitalize(tree.getLocation(), false) + "<br/>"
						+ pt.getLatitude() + ", " + pt.getLongitude()
						+ "</p>");
				getMap().getInfoWindow().open(pt,info);
			} catch (Exception e) {
				handleError(e);
			}
			
		}
		
	}
}
