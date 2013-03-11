package com.cpsc310.treespotter.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Regular TreeInfo Page
 * User cannot edit any of the tree details
 *
 */

public class RegularTreeInfoPage extends TreeInfoPage {
	
	@UiField(provided=true)
	FlexTable treeInfoTable; 
	
	@UiField(provided=true)
	VerticalPanel infoMapPanel; 

	
	interface MyUiBinder extends UiBinder<Widget, RegularTreeInfoPage> {}
	static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	
	public RegularTreeInfoPage(TreeSpotter parent, ClientTreeData tree) {
		treeInfoTable = new FlexTable();
		infoMapPanel = new VerticalPanel();
		
		setGeocoder(parent.geo);
		populateTreeInfoTable(treeInfoTable, tree);
		setTreeInfoMap(infoMapPanel, tree);
		
		initWidget(uiBinder.createAndBindUi(this));
	}
}
