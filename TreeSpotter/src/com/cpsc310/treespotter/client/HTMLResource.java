package com.cpsc310.treespotter.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.TextResource;

public interface HTMLResource extends ClientBundle {
	public static final HTMLResource INSTANCE = GWT.create(HTMLResource.class);
	
	@Source("res/flag.png")
	ImageResource flag();

	@Source("/res/home.html")
	public TextResource getHomeHtml();
	
	@Source("/res/loadingbar.html")
	public TextResource getLoadingBar();
	
	/* Popup messages for Add Tree */
	public final String ADD_TREE_SUCCESS = "Your tree has been added to the database.";
	
	public final String ADD_TREE_FAIL = "Something went worng! The tree could not be added to the database.";

	/* Tooltips for the Advanced Options */
	public final String SEARCH_LOCATION_TOOLTIP = "<html>Location search supports the following:<br>"
			+ "<ul><li>Address block: 100-200 Oak Ave<li>Coordinates with range (meters): 49.259207, -123.243173, 400<li>"
			+ "Single Address (range search): 142 Oak Ave</li></html>";

	public final String SEARCH_GENUS_TOOLTIP = "";

	public final String SEARCH_SPECIES_TOOLTIP = "";

	public final String SEARCH_COMMON_TOOLTIP = "";

	public final String SEARCH_NEIGHBOURHOOD_TOOLTIP = "<html> Search for trees in a particular area of Vancouver"
			+ "<br> eg. \"Oakridge\"</html>";

	/* Tooltips for the Add Tree form */
	public final String ADD_TREE_BUTTON_TOOLTIP = "Don't see your favourite tree? Help improve the TreeSpotter "
			+ "database by adding new trees.";

	public final String ADD_LOCATION_TOOLTIP = "<html>Location as address or coordinates (Vancouver only)<br>"
			+ "eg. \"123 Elm St\" or \"49.259207, -123.243173\"</html>";

	public final String ADD_GENUS_TOOLTIP = "";

	public final String ADD_SPECIES_TOOLTIP = "";

	public final String ADD_COMMON_TOOLTIP = "";

	public final String ADD_NEIGHBOURHOOD_TOOLTIP = "Neighbourhood in which tree is located";

	public final String ADD_HEIGHT_TOOLTIP = "Tree height (feet)";

	public final String ADD_DIAMETER_TOOLTIP = "Diameter of the tree at breast height (inches)";

	public final String ADD_PLANTED_TOOLTIP = "<html> Date format: dd mmm yyyy <br> eg. \"1 January 2012\" </html>";

}
