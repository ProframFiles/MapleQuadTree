/**
 * 
 */
package com.cpsc310.treespotter.client;

import java.util.ArrayList;

import com.cpsc310.treespotter.shared.CSVFile;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author maple-quadtree
 *
 */
public class AdminButtonPage {
	static public void load(LoginInfo loginInfo, final TreeDataServiceAsync treeDataService) {
		// put another check, since possible to force button to show
		if (loginInfo == null || !loginInfo.isAdmin()) {
			final HTMLPanel panel = new HTMLPanel("<h1> GO AWAY </h1>");
			RootPanel.get("content").clear();
			RootPanel.get("content").add(panel);	
			return;
		}
		
		System.out.println("Info: isAdmin? " +loginInfo.isAdmin()); 
		
		final HTMLPanel panel = new HTMLPanel("<img src=\"image/banana-on-computer.gif\" />\n<h1> Admin Page </h1>");
		addButton(panel, treeDataService, "Try Update", "");
		addButton(panel, treeDataService, "Force update tasks re-run", "force tasks");
		addButton(panel, treeDataService, "Force task \"indices\"", "indices");
		addButton(panel, treeDataService, "Force \"indices\" and \"genus\"", "indices,genus");
		
		addCSVButton(panel, treeDataService);
		
		RootPanel.get("content").clear();
		RootPanel.get("content").add(panel);	
	}
	
	private static void addCSVButton(final HTMLPanel panel, final TreeDataServiceAsync treeDataService) {
		Button button = new Button("Get CSV files");
		button.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				System.out.println("Sending to server");
				treeDataService.getCSVFiles(new AsyncCallback<ArrayList<CSVFile>>() {

					@Override
					public void onFailure(Throwable caught) {
						System.out.println(caught.getStackTrace());
						
					}

					@Override
					public void onSuccess(ArrayList<CSVFile> result) {
						System.out.println("Yay got response from server");
						for (CSVFile csv : result) {
							csv.printContents();
						}
					}
					
				});
			}
			
		});
		panel.add(button);
		
	}
	
	private static void addButton(final HTMLPanel panel, final TreeDataServiceAsync treeDataService, final String label,  final String options){
		Button button = new Button(label);
		button.addClickHandler(makeHandler(panel, treeDataService,options));
		panel.add(button);
	}
			
	private static ClickHandler makeHandler(final HTMLPanel panel, final TreeDataServiceAsync treeDataService, final String options){
		return new ClickHandler() {
			public void onClick(ClickEvent event) {
				treeDataService.importFromSite(options, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						panel.add(new HTML("Something went wrong! <br>" + caught.getMessage()));						
					}

					@Override
					public void onSuccess(Void result) {
						panel.add(new HTML("Import task queued"));						
					}					
				});
			}			
		};
	}
}
