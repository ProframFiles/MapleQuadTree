package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.cpsc310.treespotter.shared.ISharedTreeData;
import com.cpsc310.treespotter.shared.LatLong;
import com.cpsc310.treespotter.shared.TransmittedTreeData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * LoggedInTreeInfoPage
 * User can edit the tree details
 *
 */

public class LoggedInTreeInfoPage extends TreeInfoPage {
	
	private LinkedHashMap<String, TextBox> treeDetails = new LinkedHashMap<String, TextBox>();
	TransmittedTreeData editedTree;
	
	@UiField(provided=true)
	FlexTable treeInfoTable; 
	
	@UiField(provided=true)
	VerticalPanel infoMapPanel; 
	
	@UiField(provided=true)
	VerticalPanel commentsPanel; 
	
	@UiField(provided=true)
	VerticalPanel commentsEditor; 
	
	@UiField(provided=true)
	HorizontalPanel shareLinks;
	
	@UiField
	HTMLPanel infoPanel;
	
	@UiField
	Anchor openEditorAnchor;
	
	@UiField(provided=true)
	HorizontalPanel editButtonsBar;
	
	Button editButton; 
	Button cancelButton; 
	Button saveButton; 

	
	interface MyUiBinder extends UiBinder<Widget, LoggedInTreeInfoPage> {}
	static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	public LoggedInTreeInfoPage(TreeSpotter parent, ClientTreeData tree) {
		treeInfoTable = new FlexTable();
		infoMapPanel = new VerticalPanel();
		commentsPanel = new VerticalPanel();
		shareLinks = new HorizontalPanel();
		commentsEditor = new VerticalPanel();
		editButtonsBar = new HorizontalPanel();
		
		setTreeSpotter(parent);
		setTree(tree);
		createCommentsEditor();
		
		populateTreeInfoTable(treeInfoTable);
		setTreeInfoMap(infoMapPanel, tree);
		setShareLinks(shareLinks, tree);
		
		editButton = new Button("Edit Details");
		editButton.addClickHandler(editClickHandler());
		editButtonsBar.add(editButton);
		
		setTreeSpotter(parent);
		fetchComments(tree.getID());
				
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	
	private ClickHandler editClickHandler() {
		return new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				createEditForm();
				addSaveButton();
				addCancelButton();
			}
		};
	}
	
	@UiHandler("openEditorAnchor")
	void handleClickEditor(ClickEvent e) {
		commentsEditor.setVisible(!commentsEditor.isVisible());
	}
	
	private void createEditForm() {
		ClientTreeData tree = getTree();
		
		treeInfoTable.removeAllRows();
		treeDetails.clear();
		
		treeInfoTable.setStyleName("tree-info-table");
		treeInfoTable.setCellPadding(10);
		treeInfoTable.setSize("400px", "400px");

		createEditRow(TreeSpotter.GENUS, ParseUtils.capitalize(tree.getGenus(), false));
		createEditRow(TreeSpotter.SPECIES, ParseUtils.capitalize(tree.getSpecies(), true));
		String capName = ParseUtils.capitalize(tree.getCommonName(), false);
		createEditRow(TreeSpotter.COMMON, capName);
		createEditRow(TreeSpotter.LOCATION, ParseUtils.capitalize(tree.getLocation(), false));
		String neighbour = tree.getNeighbourhood();
		neighbour = (neighbour == null) ? neighbour : neighbour.toUpperCase();
		createEditRow(TreeSpotter.NEIGHBOUR, neighbour);
		createEditRow(TreeSpotter.PLANTED, tree.getPlanted());
		createEditRow(TreeSpotter.HEIGHT, Integer.toString(tree.getHeightRange()));
	}
	
	private void createCommentsEditor() {
		final RichTextArea textarea = new RichTextArea();
		commentsEditor.setStyleName("comment-editor");
		RichTextToolbar toolbar = new RichTextToolbar(textarea);
		Button postCommentBtn = new Button("Submit"); 
		postCommentBtn.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				System.out.println(textarea.getText());		
				try {
					addComment(getTree().getID(), textarea.getText(), getTreeSpotter().loggedInUser());
					textarea.setHTML("");
				} catch (Exception e) {
					getTreeSpotter().handleError(e);
				}
			}
			
		});
		
		commentsEditor.setVisible(false);
		commentsEditor.add(toolbar);
		commentsEditor.add(textarea);
		commentsEditor.add(postCommentBtn);
	}
	
	private void createEditRow(String field, String value) {
		int rowNum = treeInfoTable.getRowCount();
		Label fld = new Label(field);	
		TextBox tb = new TextBox();
		
		if (value == null || value.equals("-1") || value.equals("-1.0 inches")) {
			value = null;
		}
		
		tb.setText(value);
		treeDetails.put(field, tb);
		
		fld.setStyleName("tree-info-field");
		treeInfoTable.setWidget(rowNum, 0, fld);
		treeInfoTable.setWidget(rowNum, 1, tb);
	}
	
	private void addCancelButton() {
		cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				treeInfoTable.removeAllRows();
				editButtonsBar.remove(cancelButton);
				editButtonsBar.remove(saveButton);
				editButtonsBar.add(editButton);
				populateTreeInfoTable(treeInfoTable);
			}		
		});
		
		editButtonsBar.add(cancelButton);
	}
	
	private void addSaveButton() {
		saveButton = new Button("Save");
		saveButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				// TODO debug only
				for (Entry<String, TextBox> row : treeDetails.entrySet()) {
					System.out.println(row.getKey() + ": " + row.getValue().getValue());
				}		
				try {
					sendEditedTree(null);
				} catch (InvalidFieldException e) {
					getTreeSpotter().handleError(e);
				}
			}
		});
		
		editButtonsBar.remove(editButton);
		editButtonsBar.add(saveButton);		
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

		if (value == null || value.equals("-1") || value.equals("-1.0 inches")) {
			value = "Not available";
		}
		
		fld.setStyleName("tree-info-field");
		treeInfoTable.setWidget(rowNum, 0, fld);
		treeInfoTable.setWidget(rowNum, 1, new HTML(value));
	}
	
	
	private void addComment(String treeID, String comment, String user)
		throws NotLoggedInException {
		if (user == null) {
			throw new NotLoggedInException();
		}
		Date now = new Date();
		TreeComment comm = new TreeComment(treeID, user, now.toString(), comment);
		getTreeSpotter().treeDataService.addTreeComment(treeID, comm, new AsyncCallback<ArrayList<TreeComment>>() {
			public void onFailure(Throwable error) {
				getTreeSpotter().handleError(error);
			}
			
			public void onSuccess(ArrayList<TreeComment> comments) {
				Collections.sort(comments);
				displayComments(commentsPanel, comments);
			}
		});
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
	
	private void sendEditedTree(TransmittedTreeData td) throws InvalidFieldException {
		ClientTreeData t = getTree();
		boolean parseLoc = true;
		if (td == null) {
			editedTree = new TransmittedTreeData(t.getID());
			editedTree.setCultivar(t.getCultivar());
			editedTree.setLatLong(t.getLatLong());
		} else {
			editedTree = td;
			parseLoc = false;
		}
		
		for (Entry<String, TextBox> row : treeDetails.entrySet()) {
			// copied and modified from populateAddData
			String input = row.getValue().getText().trim();
			String key = row.getKey();

			// this assumes valid location/coords in form
			// #### Street Name or #, #
			if (key.equalsIgnoreCase(TreeSpotter.LOCATION) && parseLoc) {
				if (input.isEmpty()) {
					throw new InvalidFieldException(key + " cannot be empty");
				}
				
				boolean isAddr = true;
				String[] loc = input.split("[,]");
				if (loc.length == 2) {
					isAddr = false;
				}
				// try parsing as address
				if (isAddr) {
					Address addr = new Address(input);
					if (!addr.isValid()) {
						throw new InvalidFieldException(
								"Location must be a valid address");
					}
					editedTree.setCivicNumber(addr.getNumber());
					editedTree.setStreet(addr.getStreet());
					getTreeSpotter().geo.getLatLng(input + ", Vancouver, BC", new LatLngCallback() {
						public void onFailure(){}
						
						public void onSuccess(LatLng pt) {
							editedTree.setLatLong(new LatLong(pt.getLatitude(), pt.getLongitude()));
							try {
								sendEditedTree(editedTree);
							} catch (Exception e) {
								getTreeSpotter().handleError(e);
							}
						}
					});
					return;
				}
			} else if (key.equalsIgnoreCase(TreeSpotter.GENUS)) {
				if (input.isEmpty()) {
					throw new InvalidFieldException(key + " cannot be empty");
				}
				editedTree.setGenus(input);
			} else if (key.equalsIgnoreCase(TreeSpotter.SPECIES)) {
				if (input.isEmpty()) {
					throw new InvalidFieldException(key + " cannot be empty");
				}
				editedTree.setSpecies(input);
			} else if (key.equalsIgnoreCase(TreeSpotter.COMMON)) {
				if (input.isEmpty()) {
					throw new InvalidFieldException(key + " cannot be empty");
				}
				editedTree.setCommonName(input);
			} else if (key.equalsIgnoreCase(TreeSpotter.NEIGHBOUR)) {
				if (input.isEmpty()) {
					editedTree.setNeighbourhood("Not available");
					continue;
				}
				editedTree.setNeighbourhood(input);
			} else if (key.equalsIgnoreCase(TreeSpotter.HEIGHT)) {
				if (input.isEmpty()) {
					editedTree.setHeightRange(-1);
					continue;
				}
					
				int h;
				try {
					h = Integer.parseInt(input);
				} catch (Exception e) {
					throw new InvalidFieldException("Height must be integer between 1-10");
				}
				if (h < 1 || h > 10) {
					throw new InvalidFieldException("Height must be integer between 1-10");
				}
				editedTree.setHeightRange(h);
			} else if (key.equalsIgnoreCase(TreeSpotter.DIAMETER)) {
				if (input.isEmpty()) {
					editedTree.setDiameter(-1);
					continue;
				}
				
				try {
					editedTree.setDiameter((int) Double.parseDouble(input));
				} catch (Exception e) {
					throw new InvalidFieldException("Diameter must be a number");
				}
			} else if (key.equalsIgnoreCase(TreeSpotter.PLANTED)) {
				if (input.isEmpty()) {
					editedTree.setPlanted(null);
					continue;
				}
				
				try {
					editedTree.setPlanted(ParseUtils.formatDate(input));
				} catch (Exception e) {
					throw new InvalidFieldException(
							"Date Planted must be in the correct format");
				}
			}
		}
		
		getTreeSpotter().treeDataService.modifyTree(editedTree, new AsyncCallback<ISharedTreeData>() {
			public void onFailure(Throwable error) {
				getTreeSpotter().handleError(error);
				populateTreeInfoTable(treeInfoTable);
			}
			
			public void onSuccess(ISharedTreeData tree) {
				Window.alert("Tree successfully modified");
				setTree(new ClientTreeData(tree));
				populateTreeInfoTable(treeInfoTable);
				setTreeInfoMap(infoMapPanel, new ClientTreeData(tree));
				setShareLinks(shareLinks, new ClientTreeData(tree));
				editButtonsBar.remove(saveButton);
				editButtonsBar.remove(cancelButton);
				editButtonsBar.add(editButton);
			}
		});
	}


	@Override
	protected void createWikiLink(String field, String value) {
		int rowNum = treeInfoTable.getRowCount();
		Label fld = new Label(field);

		if (value == null) {
			value = "Not available";
		}
		
		Anchor link = new Anchor(value);
		link.setHref(wikipediaSearchURL + value);
		link.setTarget("__blank");
		
		fld.setStyleName("tree-info-field");
		treeInfoTable.setWidget(rowNum, 0, fld);
		treeInfoTable.setWidget(rowNum, 1, link);
	}
	
}
