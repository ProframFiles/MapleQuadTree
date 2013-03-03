package com.cpsc310.treespotter.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface HTMLResource extends ClientBundle {
	public static final HTMLResource INSTANCE = GWT.create(HTMLResource.class);

	@Source("/res/home.html")
	public TextResource getHomeHtml();
	
	public final String LOCATION_TOOLTIP = "location";
	
	public final String GENUS_TOOLTIP = "";
	
	public final String SPECIES_TOOLTIP = "";
	
	public final String COMMON_TOOLTIP = "";
	
	public final String NEIGHBOURHOOD_TOOLTIP = "Neighborhood in which the tree is located";
	
	public final String HEIGHT_TOOLTIP = "The height of the tree in feet";
	
	public final String DIAMETER_TOOLTIP = "Diameter of the tree at breast heaight (in inches)";
	
	public final String PLANTED_TOOLTIP = "Date of planting";

}
