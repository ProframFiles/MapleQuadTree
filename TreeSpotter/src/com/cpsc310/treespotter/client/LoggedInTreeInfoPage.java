package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
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
				for (Entry<String, TextBox> row : treeDetails.entrySet()) {
					System.out.println(row.getKey() + ": " + row.getValue().getValue());
				}		
				
				// TODO: send data over, then show the new data on callback
				populateTreeInfoTable(treeInfoTable);
				editButtonsBar.remove(saveButton);
				editButtonsBar.remove(cancelButton);
				editButtonsBar.add(editButton);
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
	
	
	// TODO: will comment ID be generated server side or client side?
	// assuming server side for now, so will sort comments by date when returned?
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
	
}
