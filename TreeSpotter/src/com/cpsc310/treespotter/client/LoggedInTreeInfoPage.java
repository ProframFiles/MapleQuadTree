package com.cpsc310.treespotter.client;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * LoggedInTreeInfoPage
 * User can edit the tree details
 *
 */

public class LoggedInTreeInfoPage extends TreeInfoPage {
	
	private ClientTreeData tree;
	private LinkedHashMap<String, TextBox> treeDetails = new LinkedHashMap<String, TextBox>();
	
	@UiField(provided=true)
	FlexTable treeInfoTable; 
	
	@UiField(provided=true)
	VerticalPanel infoMapPanel; 
	
	@UiField(provided=true)
	VerticalPanel commentsPanel; 
	
	@UiField
	HTMLPanel mainPanel;
	
	@UiField
	Button editButton; 

	Button cancelButton; 
	Button saveButton; 

	
	interface MyUiBinder extends UiBinder<Widget, LoggedInTreeInfoPage> {}
	static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	public LoggedInTreeInfoPage(TreeSpotter parent, ClientTreeData tree) {
		treeInfoTable = new FlexTable();
		infoMapPanel = new VerticalPanel();
		commentsPanel = new VerticalPanel();
		this.tree = tree;
		
		setGeocoder(parent.geo);
		
		populateTreeInfoTable(treeInfoTable, tree);
		setTreeInfoMap(infoMapPanel, tree);
		
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	
	@UiHandler("editButton")
	void handleClick(ClickEvent e) {
		createEditForm();
		addSaveButton();
		addCancelButton();
	}
	
	private void createEditForm() {
		treeInfoTable.removeAllRows();
		
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
		createEditRow(TreeSpotter.HEIGHT, intToHeightRange(tree.getHeightRange()));
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
				mainPanel.remove(cancelButton);
				mainPanel.remove(saveButton);
				mainPanel.add(editButton);
				populateTreeInfoTable(treeInfoTable, tree);
			}		
		});
		
		mainPanel.add(cancelButton);
	}
	
	private void addSaveButton() {
		saveButton = new Button("Save");
		saveButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				for (Entry<String, TextBox> row : treeDetails.entrySet()) {
					System.out.println(row.getKey() + ": " + row.getValue().getValue());
				}		
				
				// TODO: send data over, then show the new data on callback
				populateTreeInfoTable(treeInfoTable, tree);
				mainPanel.remove(saveButton);
				mainPanel.remove(cancelButton);
				mainPanel.add(editButton);
			}
		});
		
		mainPanel.remove(editButton);
		mainPanel.add(saveButton);		
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
}