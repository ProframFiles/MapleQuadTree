package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
	
	@UiField(provided=true)
	VerticalPanel commentsPanel; 

	@UiField(provided=true)
	HorizontalPanel shareLinks;

	// HashMap of the flags and their flag status
	// true if flagged as inaccurate
	LinkedHashMap<String, Boolean> flags = new LinkedHashMap<String, Boolean>();
	LinkedHashMap<String, Image> flagImages = new LinkedHashMap<String, Image>();
	
	interface MyUiBinder extends UiBinder<Widget, RegularTreeInfoPage> {}
	static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	
	public RegularTreeInfoPage(TreeSpotter parent, ClientTreeData tree) {
		treeInfoTable = new FlexTable();
		infoMapPanel = new VerticalPanel();
		commentsPanel = new VerticalPanel();
		shareLinks = new HorizontalPanel();
		
		setGeocoder(parent.geo);
		populateTreeInfoTable(treeInfoTable, tree);
		setTreeInfoMap(infoMapPanel, tree);
		setShareLinks(shareLinks, tree);
		
		ArrayList<TreeComment> list = new ArrayList<TreeComment>();
		// TODO
		list.add(new TreeComment(1234, "Tree Guy", "Mar 12", "This tree is my favourite."));
		list.add(new TreeComment(1234, "Tree Guy #2", "Mar 12", "Me too!"));
		displayComments(commentsPanel, list);
		initWidget(uiBinder.createAndBindUi(this));
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
		Image img = new Image(HTMLResource.INSTANCE.flag());
		img.setStyleName("unflagged");
		Label fld = new Label(field);
		fld.setStyleName("tree-info-field");

		if (value == null || value.equals("-1") || value.equals("-1.0 inches")) {
			value = "Not available";
		}
		
		img.addClickHandler(setFlag(field));
		flags.put(field, false);
		flagImages.put(field, img);
		
		treeInfoTable.setWidget(rowNum, 0, img);
		treeInfoTable.setWidget(rowNum, 1, fld);
		treeInfoTable.setWidget(rowNum, 2, new HTML(value));
	}

	 private ClickHandler setFlag(final String field) {
		 return new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				boolean newState = !flags.get(field);
				flags.put(field, newState);		
				
				if (newState) {
					flagImages.get(field).setStyleName("flagged");
				} else {
					flagImages.get(field).setStyleName("unflagged");
				}
				
//				for (String flag: flags.keySet()) {
//					System.out.println(flag + ": " + flags.get(flag));
//				}		
			}			 
		 };
	 }
	 
	 public void addImageTooltips() {
		 for (String flag: flagImages.keySet()) {
			 addTooltip(flag, flagImages.get(flag));
		 }
	 }
	 
	 // note: called after init, need page to be added to TreeSpotter to get the proper coordinates
	 private void addTooltip(String field, Image img) { 
		Tooltip tip = new Tooltip(img, "Flag " + field + " as inaccurate.",
				img.getAbsoluteLeft() + infoMapPanel.getOffsetWidth(), 
				img.getAbsoluteTop() - 50);
		
		img.addMouseOverHandler(tip);
		img.addMouseOutHandler(tip);
	 }
}
