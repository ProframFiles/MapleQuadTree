package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
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
	private LinkedHashMap<String, Boolean> flags = new LinkedHashMap<String, Boolean>();
	// HashMap of flag and their images
	private LinkedHashMap<String, Image> flagImages = new LinkedHashMap<String, Image>();
	// list of flagged fields from server
	private ArrayList<String> markedFlags;
	
	// fields for flag pop-up
	private DialogBox popup = new DialogBox();
	private TextArea flagText = new TextArea();
	private String flagField;
		
	interface MyUiBinder extends UiBinder<Widget, RegularTreeInfoPage> {}
	static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	
	public RegularTreeInfoPage(TreeSpotter parent, ClientTreeData tree) {
		treeInfoTable = new FlexTable();
		infoMapPanel = new VerticalPanel();
		commentsPanel = new VerticalPanel();
		shareLinks = new HorizontalPanel();
		setTreeSpotter(parent);
		setTree(tree);

		parent.treeDataService.getTreeFlags(tree.getID(), new AsyncCallback<ArrayList<String>>() {
			public void onFailure(Throwable error) {
				getTreeSpotter().handleError(error);
			}
			
			public void onSuccess(ArrayList<String> marked) {
				markedFlags = marked;
				System.out.println(marked);
				populateTreeInfoTable(treeInfoTable);
				addImageTooltips();
			}
			
		});
		
		setTreeInfoMap(infoMapPanel, tree);
		setShareLinks(shareLinks, tree);
		
		fetchComments(tree.getID());

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
		Label fld = new Label(field);
		fld.setStyleName("tree-info-field");

		if (value == null || value.equals("-1") || value.equals("-1.0 inches")) {
			value = "Not available";
		}
		
		img.addClickHandler(setFlag(field));
		if (markedFlags.contains(field)) {
			img.setStyleName("flagged");
			flags.put(field, true);
		} else {
			img.setStyleName("unflagged");
			flags.put(field, false);
		}
		flagImages.put(field, img);
		
		treeInfoTable.setWidget(rowNum, 0, img);
		treeInfoTable.setWidget(rowNum, 1, fld);
		treeInfoTable.setWidget(rowNum, 2, new HTML(value));
	}
	
	protected void createWikiLink(String field, String value) {
		int rowNum = treeInfoTable.getRowCount();
		Image img = new Image(HTMLResource.INSTANCE.flag());
		Label fld = new Label(field);
		
		if (value == null) {
			value = "Not available";
		}

		img.addClickHandler(setFlag(field));
		if (markedFlags.contains(field)) {
			img.setStyleName("flagged");
			flags.put(field, true);
		} else {
			img.setStyleName("unflagged");
			flags.put(field, false);
		}
		flagImages.put(field, img);
		
		Anchor link = new Anchor(value);
		link.setHref(wikipediaSearchURL + value);
		link.setTarget("__blank");
		
		fld.setStyleName("tree-info-field");
		treeInfoTable.setWidget(rowNum, 0, img);
		treeInfoTable.setWidget(rowNum, 1, fld);
		treeInfoTable.setWidget(rowNum, 2, link);
	}

	 private ClickHandler setFlag(final String field) {
		 return new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				createFlagPopup(field);
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
		popup.clear();
		flagText.setText("");
		flagField = field;
		
		HTMLPanel panel = new HTMLPanel("");
		Button save = new Button("Save");
		Button cancel = new Button("Cancel");
		
		// click handler for save button
		save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (flagText.getText().trim().length() != 0) {
					// flag the field
					System.out.println(flagText.getText());
					setFlagged(flagField);
					addTreeFlag();
					popup.hide();
				} else {
					// if nothing entered
					Window.alert("Please fill in a reason for why this is inaccurate");
				}
			}			
		});
		
		// click handler for cancel button
		cancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				popup.hide();				
			}
			
		});
		
		// add style names
		popup.setStyleName("flag-popup");
		
		// add elements to the panel
		panel.add(new HTML("You've flagged the " + field + " as inaccurate. <br> Please give a reason:"));
		panel.add(flagText);
		panel.add(save);
		panel.add(cancel);
		
		// add the panel to the popup and show
		popup.add(panel);
		popup.center();
		
	}
	
	// sets flag reason and image
	private void setFlagged(String field) {
			flags.put(field, true);
			flagImages.get(field).setStyleName("flagged");
	}
	// shouldn't allow unflagging once field is flag. Only Admin should be able to unflag it.
/*	private void removeFlag(String field) {
		flags.put(field, false);
		flagReasons.remove(field);
		flagImages.get(field).setStyleName("unflagged");
	}*/ 
	
	private void addTreeFlag() {
		System.out.println("Flag: tree '" + getTree().getID() + "' inaccurate at " + flagField 
							+ " because '" + flagText.getText().trim() + "'");
		getTreeSpotter().treeDataService.flagTreeData(getTree().getID(), flagField, flagText.getText().trim(), 
				new AsyncCallback<String>() {
			public void onFailure(Throwable error) {
				getTreeSpotter().handleError(error);
			}
			
			public void onSuccess(String str){
				// TODO: what do we need back from the server for this?
				//(aleksy) nothing really, I just wasn't sure when I made the stub
				
			}
		});
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
