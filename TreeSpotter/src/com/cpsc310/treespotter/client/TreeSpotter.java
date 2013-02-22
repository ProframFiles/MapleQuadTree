package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TreeSpotter implements EntryPoint {
  private LoginInfo loginInfo = null;
  
  //list of fields, used for the Add Tree Form and the Advanced Search
  private String[] basicFields = {"Species", "Location", "Height", "Radius"};
  
  private boolean isBasicSearch = true;
  private TextBox basicSearch = null;
  
  //list of the input boxes, so the values can be retrieved
  private List<TextBox> advancedSearchList = new ArrayList<TextBox>();
  private List<TextBox> addFormList = new ArrayList<TextBox>();

  private final TreeDataServiceAsync treeDataService = GWT.create(TreeDataService.class);
  private final VerticalPanel infoMapPanel = new VerticalPanel();
  private Geocoder geo;
  private Label invalidLoc = new Label("Tree location could not be displayed");
  private static final int ZOOM_LVL = 12;
  private static final String ADMIN = "admin";
  
  
  /**
   * Entry point method.
   */
  public void onModuleLoad() {

      // do we care if they're logged in?
//      LoginServiceAsync loginService = GWT.create(LoginService.class);
//      loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
//        public void onFailure(Throwable error) {
//            handleError(error);
//        }
//
//        public void onSuccess(LoginInfo result) {
//          loginInfo = result;
//          if(loginInfo.isLoggedIn()) {
//        	  initHomePage();
//          } else {
//        	  initHomePage();
//          }
//        }
//      });

    /* to see specific tree info page, use GET parameters
    eg. treespotter.appspot.com/viewTree.html?id=xxx
    uncomment the following to get the parameter and pass to server */
    /*String tid = Window.Location.getParameter("id");
    // how to determine user type to pass in? Shouldn't be via GET
    // dependent on how we keep track of user privileges

    String user = ADMIN;
    getTreeInfo(tid, user);*/
    
    Maps.loadMapsApi("", "2", true, new Runnable() {
      public void run() {
        initHomePage(); 
        initButtons();
        initLoginLogout();
      }
    });

  }

private void handleError(Throwable error) {
    Window.alert(error.getMessage());
    if (error instanceof NotLoggedInException) {
      Window.Location.replace(loginInfo.getLogoutUrl());
    }
  }

  /* add click handlers to buttons, search form 
   * TODO: way to get a dropdown of some fields instead? 
   */
  private void initHomePage() {
	  /* clear everything first */
	  RootPanel searchDiv = RootPanel.get("main-search");
	  searchDiv.clear();

	  /* set up basic search elements */
	  HorizontalPanel searchPanel = new HorizontalPanel();
	  final TextBox searchInput = new TextBox();
	  Button searchBtn = new Button("Find my tree!");
	  searchInput.setStyleName("main-search");
	  searchBtn.setStyleName("main-search");
	  
	  searchPanel.add(searchInput);
	  searchPanel.add(searchBtn);

	  /* store search text box to retrieve search terms later */
	  basicSearch = searchInput;
	  
	  /* set up advanced search elements */
	  final VerticalPanel advancedForm = new VerticalPanel();
	  advancedForm.setStyleName("main-search");	  
	  for (String field : basicFields) {
		  HorizontalPanel advtb = createSearchPanel(field + ":");
		  advancedForm.add(advtb);
	  }
	  advancedForm.setVisible(false);
	  
	  /* set up advanced search link */
	  Anchor advancedLink = new Anchor("Advanced Search");
	  advancedLink.setStyleName("main-search");
	  advancedLink.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {
			if (advancedForm.isVisible()) {
				advancedForm.setVisible(false);
				isBasicSearch = true;
			} else {
				advancedForm.setVisible(true);
				isBasicSearch = false;
			}
		}
		  
	  });
	  
	  /* add doSearch to search button 
	   * call doSearch 
	   */
	  searchBtn.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {
			doSearch();
		}		  
	  });
	    	  
	  /* add elements to the page */
	  searchDiv.add(searchPanel);
	  searchDiv.add(advancedLink);
	  searchDiv.add(advancedForm);	  
  }
  
  /* get search terms and make a call to the server
   * if Advanced Search is open, use the Advanced Search values
   * else use the Basic Search value
   * call displaySearchResults to display the returned list of results
   */
  private void doSearch() {
	  if (isBasicSearch) {
		  /* perform basic search */
		  System.out.println("Basic Search:");
		  System.out.println(basicSearch.getValue());
	  } else {
		  /* perform advanced search */
		  System.out.println("Advanced Search:");
		  for (TextBox tb : advancedSearchList) {
			  if (!tb.getValue().equals(""))
			  System.out.println(tb.getValue());
		  }
	  }
	  
	  ClientTreeData tree = new UserTreeData();		
	  ArrayList<ClientTreeData> treeList = new ArrayList<ClientTreeData>();
	  treeList.add(tree);
	  treeList.add(tree);
	  displaySearchResults(treeList);
  }
 
  /**
   * Replaces content panel with search results 
   * @param rlist List of ClientTreeData search results from the server
   */
  private void displaySearchResults(ArrayList<ClientTreeData> rlist) {
	  System.out.println("Displaying search results");
	  FlexTable resultsTable = new FlexTable();
	  resultsTable.setWidth("100%");
	  
	  for (final ClientTreeData tree : rlist) {
		  HorizontalPanel panel = new HorizontalPanel();
		  Anchor species = new Anchor("Sudowoodo");
		  Label location = new Label("Route 20");
		  
		  /* add link to the tree info page */
		  species.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				displayTreeInfoPage(tree);			
			}			  
		  });
		  
		  panel.add(species);
		  panel.add(location);
		  panel.setStyleName("result");
		  
		  int rows = resultsTable.getRowCount();
		  resultsTable.setWidget(rows, 0, panel);
	  }
	  
	  RootPanel.get("content").clear();
	  RootPanel.get("content").add(resultsTable);
  }
  
  private void getTreeInfo(String id, String user) {
    treeDataService.getTreeData(id, user, new AsyncCallback<ClientTreeData>() {
      public void onFailure(Throwable error) {
        handleError(error);
      }
      public void onSuccess(ClientTreeData data) {
        displayTreeInfoPage(data);
      }
    });
  }

  /**
   * Replaces content panel with details from the given ClientTreeData
   * @param t ClientTreeData to display details of
   */
  private void displayTreeInfoPage(ClientTreeData t) {
	  HorizontalPanel panel = new HorizontalPanel();
	  panel.setSpacing(50);
	  
	  /* create the map */
    setTreeInfoMap(t); 

	  /* create panel with all the data */
	  VerticalPanel data = new VerticalPanel();
	  data.setStyleName("treedata");
	  data.setWidth("400px");
	  data.setHeight("400px");
	  
	  // TODO: replace values with t.getSpecies() 
	  data.add(createResultDataRow("Species", "Sudowoodo"));
	  data.add(createResultDataRow("Type", "Rock"));
	  data.add(createResultDataRow("Location", "Route 20"));
	  data.add(createResultDataRow("Height", "1.2m"));
	  data.add(createResultDataRow("Weight", "38.0kg"));
	  
	  panel.add(infoMapPanel);
	  panel.add(data);
	  
	  RootPanel.get("content").clear();
	  RootPanel.get("content").add(panel); 

  }
  
  /**
   * Open a popup for a form to add a tree to the database
   */
  private void addUserTree() {

	  final PopupPanel addPanel = new PopupPanel();
	  VerticalPanel addForm = new VerticalPanel();
	  
	  /* create text box and label for each field */
	  for (String fld : basicFields) {
		  VerticalPanel addtb = createAddTreeRow(fld);
		  addForm.add(addtb);	  
	  }
	  
	  /* add submit button */
	  // TODO: hook up to add tree service
	  Button submitBtn = new Button("Add Tree");
	  submitBtn.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
//  	    for (TextBox tb: addFormList) {
//          System.out.println(tb.getValue());
//        }  
	      UserTreeData addTree = new UserTreeData();
	      //TODO: set form data to populate UserTreeData
//	      treeDataService.
	      treeDataService.addTree(addTree, new AsyncCallback<Void>() {
	        public void onFailure(Throwable error) {
	          handleError(error);
	        }
	        public void onSuccess(Void v) {
//	          displayTreeInfoPage(data);
	          // TODO
	          // maybe it'd be nice to be redirected to newly added tree info page?
	          // would require TreeDataService to return ClientTreeData from server
	        }
	      });
	    }
	  });
	  
	  /* add cancel button to close popup */
	  Button cancelBtn = new Button("Cancel");
	  cancelBtn.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {
			addPanel.hide();		
		}	  
	  });

	  addForm.add(submitBtn);
	  addForm.add(cancelBtn);
	  
	  addPanel.add(addForm);
	  addPanel.setWidth("400px");
	  addPanel.setStyleName("add-popup");
	  addPanel.center();
  }
  
 /**
  * Helper function for initHomePage()
  * Creates a panel with a text box for an advanced search field 
  * @param text String for the text box label
  * @return Panel containing one row for the advanced search panel
  */
	private HorizontalPanel createSearchPanel(String text) {
		HorizontalPanel panel = new HorizontalPanel();
		final CheckBox cb = new CheckBox();
		final TextBox tb = new TextBox();
		tb.setStyleName("disabled");

		Label label = new Label();
		label.setText(text);
		label.setStyleName("advanced-search");

		/* add handler to enable/disable text box based on check box */
		cb.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (!cb.getValue()) {
					tb.setEnabled(false);
					tb.setText("");
					tb.setStyleName("disabled");
				} else {
					tb.setEnabled(true);
					tb.setStyleName("enabled");
				}
			}
		});

		/* add text box to the list */
		advancedSearchList.add(tb);

		/* add all elements to the panel */
		panel.add(cb);
		panel.add(label);
		panel.add(tb);
		return panel;
	}
	
	private void initLoginLogout() {
		final Anchor loginLink = Anchor.wrap(Document.get().getElementById(
				"login-link"));
		final Anchor logoutLink = Anchor.wrap(Document.get().getElementById(
				"logout-link"));
		LoginServiceAsync loginService = GWT.create(LoginService.class);
		loginService.login(GWT.getHostPageBaseURL(),
				new AsyncCallback<LoginInfo>() {
					public void onFailure(Throwable error) {
						handleError(error);
					}
					public void onSuccess(LoginInfo result) {
						loginInfo = result;
						if (loginInfo.isLoggedIn()) {
							// log in was successful, don't do anything
						} else {
							loginLink.setHref(loginInfo.getLoginUrl());
							logoutLink.setHref(loginInfo.getLogoutUrl());
						}
					}
				});
	}
	
	/**
	 * Adds click handlers to buttons
	 * Only button not initialised here is the search button  
	 */
	private void initButtons() {
		  /* wrap home button with click handler */
		  Button homeButton = Button.wrap(Document.get().getElementById("home-button"));
		  homeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// TODO: call home page
			}		  
		  });
		  
		  /* wrap search button with click handler 
		   * call displaySearchResults with the list of ClientTreeData returned from the server
		   * (not a new search)
		   */
		  Button searchButton = Button.wrap(Document.get().getElementById("search-button"));
		  searchButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ClientTreeData tree = new UserTreeData();		
				ArrayList<ClientTreeData> treeList = new ArrayList<ClientTreeData>();
				treeList.add(tree);
				treeList.add(tree);
				
				displaySearchResults(treeList);
			}		  
		  });
		  
		  /* wrap about button with click handler */
		  Button aboutButton = Button.wrap(Document.get().getElementById("about-button"));
		  aboutButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// call about page			
			}		  
		  });
		  
		  Button addButton = Button.wrap(Document.get().getElementById("add-tree-button"));
		  addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addUserTree();			
			}		  
		  });
	}
	
	/**
	 * Helper function to create rows of data for the TreeInfoPage
	 * @param field Data field
	 * @param value Data value
	 * @return
	 */
	private HorizontalPanel createResultDataRow(String field, String value) {
		HorizontalPanel panel = new HorizontalPanel();
		Label fld = new Label(field);
		fld.addStyleName("field");
		fld.setWidth("150px");
		
		Label val = new Label(value);
		
		panel.add(fld);
		panel.add(val);
		panel.addStyleName("treedata");
		return panel;
	}
	
	/**
	 * Helper method to create fields for the add tree popup
	 * @param text
	 * @return
	 */
	private VerticalPanel createAddTreeRow(String text) {
		VerticalPanel row = new VerticalPanel();
		Label lbl = new Label(text);
		TextBox tb = new TextBox();
		addFormList.add(tb);
		
		row.add(lbl);
		row.add(tb);
		row.setCellHeight(row, "30px");
		row.setCellHeight(tb, "50px");
		return row;
	}

private void setTreeInfoMap(ClientTreeData data) {
  infoMapPanel.clear();
  // TODO: add infoMapPanel to tree info page
  infoMapPanel.setSize("400px", "400px");
  infoMapPanel.setStyleName("map");
  if (data.getCoordinates() == null) {
    infoMapPanel.add(invalidLoc);
    return;
  }
    
  System.out.println(data.getCoordinates());
  LatLng coords = LatLng.fromUrlValue(data.getCoordinates());
  if (!validCoordinates(coords)) {  // geocode location
    
    geo.getLatLng(data.getLocation(), new LatLngCallback() {
      public void onFailure() {
        infoMapPanel.add(invalidLoc);
      }
      
      public void onSuccess(LatLng pt) {
        setTreeInfoMap(pt);
        // TODO: add new coordinates back to server database
      }
    });
  }
  else {
    setTreeInfoMap(coords);
  }

}

private void setTreeInfoMap(LatLng pt) {
  MapWidget map = new MapWidget(pt, ZOOM_LVL);
  infoMapPanel.add(map);
}

private boolean validCoordinates(LatLng c) {
  if (Double.isNaN(c.getLatitude()) || Double.isNaN(c.getLongitude()))
    return false;
  return true;
}
}
