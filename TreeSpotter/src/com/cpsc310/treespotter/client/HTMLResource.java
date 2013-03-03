package com.cpsc310.treespotter.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface HTMLResource extends ClientBundle {
	public static final HTMLResource INSTANCE = GWT.create(HTMLResource.class);

	@Source("/res/home.html")
	public TextResource getHomeHtml();
	
	public final String LOCATION_TOOLTIP = "<html>Location as address or coordinates (Vancouver only)<br>"
											+ "eg. \"123 Elm St\" or \"49.259207, -123.243173\"</html>";
	
	public final String GENUS_TOOLTIP = "";
	
	public final String SPECIES_TOOLTIP = "";
	
	public final String COMMON_TOOLTIP = "";
	
	public final String NEIGHBOURHOOD_TOOLTIP = "Neighbourhood in which tree is located";
	
	public final String HEIGHT_TOOLTIP = "Tree height (feet)";
	
	public final String DIAMETER_TOOLTIP = "Diameter of the tree at breast height (inches)";
	
	public final String PLANTED_TOOLTIP = "Date format: dd mmm yyyy";

}
