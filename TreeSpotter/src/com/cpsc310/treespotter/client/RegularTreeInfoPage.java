package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.swing.JFrame;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
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

	// HashMap of the fields and their flag status
	// true if flagged as inaccurate
	LinkedHashMap<String, Boolean> flags = new LinkedHashMap<String, Boolean>();
	// HashMap of fields and the reason for the flag
	LinkedHashMap<String, String> flagReasons = new LinkedHashMap<String, String>();
	// HashMap of flag and their images
	LinkedHashMap<String, Image> flagImages = new LinkedHashMap<String, Image>();
	
	interface MyUiBinder extends UiBinder<Widget, RegularTreeInfoPage> {}
	static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	
	public RegularTreeInfoPage(TreeSpotter parent, ClientTreeData tree) {
		treeInfoTable = new FlexTable();
		infoMapPanel = new VerticalPanel();
		commentsPanel = new VerticalPanel();
		shareLinks = new HorizontalPanel();
		
		setTreeSpotter(parent);
		populateTreeInfoTable(treeInfoTable, tree);
		setTreeInfoMap(infoMapPanel, tree);
		setShareLinks(shareLinks, tree);
		
		ArrayList<TreeComment> list = new ArrayList<TreeComment>();
		// TODO replace with fetchComments() when server side complete
		// fetchComments(tree.getID());
		list.add(new TreeComment(tree.getID(), "Troll", "12 Apr 2013", "This tree is ugly"));
		list.add(new TreeComment(tree.getID(), "Tree Guy", "12 Mar 2013", "This tree is my favourite."));
		list.add(new TreeComment(tree.getID(), "Tree Guy #2", "14 Mar 2013", "Me too!"));
		Collections.sort(list);	// debug testing comments sorting
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
				if (flags.get(field)) {
					// if already flagged, unflag
					removeFlag(field);					
				} else {
					// otherwise, open popup
					createFlagPopup(field);
				}
			}			 
		 };
	 } 
		
	private void fetchComments(String treeID) {
		getTreeSpotter().treeDataService.getTreeComments(treeID, new AsyncCallback<ArrayList<TreeComment>>() {
			public void onFailure(Throwable error){
				getTreeSpotter().handleError(error);
			}
			
			public void onSuccess(ArrayList<TreeComment> comments) {
				displayComments(commentsPanel, comments);
			}
		});
	}
	
	// TODO: not sure how we want to send it over, hash map for now
	private void createFlagPopup(final String field) {
		final DialogBox pop = new DialogBox();
		HTMLPanel panel = new HTMLPanel("");
		final TextArea ta = new TextArea();
		Button save = new Button("Save");
		Button cancel = new Button("Cancel");
		
		// click handler for save button
		save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (ta.getText().length() != 0) {
					// flag the field
					System.out.println(ta.getText());
					setFlagged(field, ta.getText());
					pop.hide();
				} else {
					// if nothing entered
					pop.hide();
				}
			}			
		});
		
		// click handler for cancel button
		cancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				pop.hide();				
			}
			
		});
		
		// add style names
		pop.setStyleName("flag-popup");
		
		// add elements to the panel
		panel.add(new HTML("Reason:"));
		panel.add(ta);
		panel.add(save);
		panel.add(cancel);
		
		// add the panel to the popup and show
		pop.add(panel);
		pop.center();
		
	}
	
	// sets flag reason and image
	private void setFlagged(String field, String reason) {
		if (!reason.isEmpty()) {
			flags.put(field, true);
			flagReasons.put(field, reason);
			flagImages.get(field).setStyleName("flagged");
		}
	}
	
	private void removeFlag(String field) {
		flags.put(field, false);
		flagReasons.remove(field);
		flagImages.get(field).setStyleName("unflagged");
	}
	
	 public void addImageTooltips() {
		 for (String flag: flagImages.keySet()) {
			 addTooltip(flag, flagImages.get(flag));
		 }
	 }
	 
	 // note: called after init, need page to be added to TreeSpotter to get the proper coordinates
	 private void addTooltip(String field, Image img) { 
		Tooltip tip = new Tooltip(img, "Flag " + field + " as inaccurate.",
				img.getAbsoluteLeft(), 
				img.getAbsoluteTop() - 50);
		
		img.addMouseOverHandler(tip);
		img.addMouseOutHandler(tip);
		img.addMouseDownHandler(tip);
	 }
	
}
