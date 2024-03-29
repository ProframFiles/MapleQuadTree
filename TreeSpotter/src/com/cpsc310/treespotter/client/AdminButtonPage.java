/**
 * 
 */
package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedMap;

import com.cpsc310.treespotter.shared.CSVFile;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author maple-quadtree
 *
 */
public class AdminButtonPage {
	
	private static HTMLPanel buttonPanel;
	private static HTMLPanel contentPanel;
	private final static String ADMIN_CONSOLE_LINK = "https://appengine.google.com/dashboard?&app_id=s~quadtreetest";
	
	static public void load(LoginInfo loginInfo, final TreeDataServiceAsync treeDataService) {
		// put another check, since possible to force button to show
		if (loginInfo == null || !loginInfo.isAdmin()) {
			final HTMLPanel panel = new HTMLPanel("<h1> GO AWAY </h1>");
			RootPanel.get("content").clear();
			RootPanel.get("content").add(panel);	
			return;
		}
		
		System.out.println("Info: isAdmin? " +loginInfo.isAdmin()); 
		
		buttonPanel = new HTMLPanel("<center><h1><img src=\"image/banana-on-computer.gif\" /> Admin Page <img src=\"image/banana-on-computer.gif\" /></h1></center>");
		contentPanel = new HTMLPanel("");

		buttonPanel.add(new HTML("<a href=\"" + ADMIN_CONSOLE_LINK + "\"target=\"__blank\"> Admin Console </a>"));
		
		addButton(treeDataService, "Try Update", "");
		addButton(treeDataService, "Force update tasks re-run", "force tasks");
		addButton(treeDataService, "Force task \"indices\"", "indices");
		addButton(treeDataService, "Force \"indices\" and \"genus\"", "indices,genus");
		
		addCSVButton(treeDataService);
		addFlaggedButton(treeDataService);
	
		RootPanel.get("content").clear();
		RootPanel.get("content").add(buttonPanel);	
		RootPanel.get("content").add(contentPanel);	
	}
	
	private static void addFlaggedButton(final TreeDataServiceAsync treeDataService) {

		Button btn = new Button("See Flagged Trees");

		btn.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				contentPanel.clear();
				treeDataService
						.getFlaggedTreeIDs(new AsyncCallback<ArrayList<String>>() {

							@Override
							public void onFailure(Throwable caught) {
								System.out.println(Arrays.toString(caught.getStackTrace()));
							}

							@Override
							public void onSuccess(ArrayList<String> results) {
								
								if (results.isEmpty()) {
									contentPanel.clear();
									contentPanel.add(new HTML("No trees have been flagged."));
								}
								
								for (String tree : results) {
									System.out.println("Flagged Tree: " + tree);
									contentPanel.add(createFlaggedTreeItem(tree, treeDataService));
								}
							}
						});
			}
		});
		
		buttonPanel.add(btn);
	}
	
	private static HTMLPanel createFlaggedTreeItem(final String treeID, final TreeDataServiceAsync treeDataService) {
		final HTMLPanel content = new HTMLPanel("");
		
		Anchor link = new Anchor(treeID);
		link.setHref(Window.Location.getPath() + Window.Location.getQueryString() + "#tree" + treeID);
		link.setTarget("__blank");
		content.add(link);
		
		treeDataService.getTreeFlagData(treeID, new AsyncCallback<SortedMap<String, String>>() {

			@Override
			public void onFailure(Throwable caught) {
				System.out.println(Arrays.toString(caught.getStackTrace()));
			}

			@Override
			public void onSuccess(SortedMap<String, String> result) {
				for (String flag: result.keySet()) {
					System.out.println(flag);
					content.add(new HTML(flag + ": " + result.get(flag) + " <br>"));
				}
			}
		});	
	
		// add button to clear flags
		Button clearFlagsBtn = new Button("Clear Flags");
		clearFlagsBtn.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				treeDataService.clearTreeFlags(treeID, new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						System.out.println(Arrays.toString(caught.getStackTrace()));
					}

					@Override
					public void onSuccess(Void result) {
						Window.alert("The flags for " + treeID + " have been cleared.");
						contentPanel.remove(content);
						printFlags(treeDataService);
					}			
				});
			}
		});
		
		
		content.add(clearFlagsBtn);
		content.setStyleName("flag-tree-item");
		return content;
	}

	private static void addCSVButton(final TreeDataServiceAsync treeDataService) {
		Button button = new Button("Get CSV files");
		button.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				System.out.println("Fetching CSV Files");
				treeDataService.getCSVFiles(new AsyncCallback<ArrayList<CSVFile>>() {

					@Override
					public void onFailure(Throwable caught) {
						System.out.println(Arrays.toString(caught.getStackTrace()));
						
					}

							@Override
							public void onSuccess(ArrayList<CSVFile> result) {
								contentPanel.clear();
								if (result.isEmpty()) {
									contentPanel.add(new HTML("No CSV files pending."));
								} else {
									for (CSVFile csv : result) {
										VerticalPanel panel = renderCSVTable(csv, treeDataService);
										contentPanel.add(panel);
									}
								}
							}

						});
			}
			
		});
		buttonPanel.add(button);
		
	}
	
	private static void addButton(final TreeDataServiceAsync treeDataService, final String label,  final String options){
		Button button = new Button(label);
		button.addClickHandler(makeHandler(treeDataService,options));
		buttonPanel.add(button);
	}
			
	private static ClickHandler makeHandler(final TreeDataServiceAsync treeDataService, final String options){
		return new ClickHandler() {
			public void onClick(ClickEvent event) {
				contentPanel.clear();
				treeDataService.importFromSite(options, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						contentPanel.add(new HTML("Something went wrong! <br>" + caught.getMessage()));						
					}

					@Override
					public void onSuccess(Void result) {
						contentPanel.add(new HTML("Import task queued"));						
					}					
				});
			}			
		};
	}
	
	private static VerticalPanel renderCSVTable(CSVFile csv, final TreeDataServiceAsync treeDataService) {
		VerticalPanel panel = new VerticalPanel();
		
		FlexTable table = new FlexTable();
		for (String tree : csv.getContents()) {
			int row = table.getRowCount();
			table.getRowFormatter().setStyleName(row, "csv-row");
			
			// add CSV content
			HTML html = new HTML(tree);
			table.setWidget(row, 0, html);
		}
		
		// add button
		Button btn = new Button("Approve");
		btn.addClickHandler(approveTreeHandler(csv, panel, treeDataService));
		
		// cancel button
		Button cbtn = new Button("Reject");
		cbtn.addClickHandler(rejectTreeHandler(csv, panel, treeDataService));
		
		// buttons panel
		HTMLPanel buttonPanel = new HTMLPanel("");
		buttonPanel.add(btn);
		buttonPanel.add(cbtn);
		
		// user info panel
		HTML userPanel = new HTML("User: " + csv.getUser());
	
		panel.setStyleName("csv-table");
		panel.add(userPanel);
		panel.add(table);
		panel.add(buttonPanel);
		
		return panel;
	}

	/**
	 * Method to add the approved tree to the database
	 * @param treeDataService 
	 * @param tree
	 * @return ClickHandler for the Approve button
	 */
	private static ClickHandler approveTreeHandler(final CSVFile csv, final VerticalPanel panel, final TreeDataServiceAsync treeDataService) {
		return new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				System.out.println("Approved: Parsing CSV now");
				treeDataService.parseCSV(csv, new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable e) {
						System.out.println(Arrays.toString(e.getStackTrace()));
						
					}

					@Override
					public void onSuccess(Void result) {
						Window.alert("Approved.");
						panel.removeFromParent();
					}
					
				});
				
			}
			
		};
	}
	
	/**
	 * Method to remove the tree from the pending list
	 * @param treeDataService 
	 * @param tree
	 * @return ClickHandler for the Reject button
	 */
	private static ClickHandler rejectTreeHandler(final CSVFile csv, final VerticalPanel panel, final TreeDataServiceAsync treeDataService) {
		return new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				System.out.println("Rejected: Deleting from server");
				treeDataService.deleteCSV(csv, new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable e) {
						System.out.println(Arrays.toString(e.getStackTrace()));
						
					}

					@Override
					public void onSuccess(Void result) {
						Window.alert("Rejected.");
						panel.removeFromParent();
					}
					
				});

			}
			
		};
	}
	
	// for debugging
	// TODO: remember to remove
	private static void printFlags(final TreeDataServiceAsync treeDataService) {
		treeDataService
		.getFlaggedTreeIDs(new AsyncCallback<ArrayList<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				System.out.println(Arrays.toString(caught.getStackTrace()));
			}

			@Override
			public void onSuccess(ArrayList<String> results) {
				System.out.println(results);
			}
		});
	}
}
