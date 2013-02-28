package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.History;
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

	// list of fields, used for the Add Tree Form and the Advanced Search
	private String[] basicFields = { "Location", "Genus", "Species",
			"Common Name" };
	private String[] optionalFields = { "Neighbourhood", "Height", "Diameter",
			"Date Planted" };

	private boolean isBasicSearch = true;
	private TextBox basicSearch = null;

	// list of the input boxes, so the values can be retrieved
	private List<TextBox> advancedSearchList = new ArrayList<TextBox>();
	// private List<TextBox> addFormList = new ArrayList<TextBox>();
	private LinkedHashMap<Label, TextBox> addFormMap = new LinkedHashMap<Label, TextBox>();

	private final TreeDataServiceAsync treeDataService = GWT
			.create(TreeDataService.class);
	
	// variables for loading map view
	private final VerticalPanel infoMapPanel = new VerticalPanel();
	private Geocoder geo;
	private Label invalidLoc = new Label("Tree location could not be displayed");
	private static final int ZOOM_LVL = 15;
	
	// for adding tree
	private Address geoAddr;
	private ClientTreeData addTree;
	private boolean doneParse = true;
	private boolean asyncCall = false;
	
	private DateTimeFormat dtf = DateTimeFormat.getFormat("d MMM yyyy");
	private static final String ADMIN = "admin";
	// in order to be accessed by inner classes this has to be a member
	private ArrayList<ClientTreeData> treeList = new ArrayList<ClientTreeData>();

	// is there a way to get a list of neighbourhoods from the dataset?
	// from the file names or do a batch query on everything (ugh)
	// private ArrayList<String> neighbourhoods = new ArrayList<String>(50);

	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {

		/*
		 * to see specific tree info page, use GET parameters eg.
		 * treespotter.appspot.com/viewTree.html?id=xxx uncomment the following
		 * to get the parameter and pass to server
		 */
		/*
		 * String tid = Window.Location.getParameter("id"); // how to determine
		 * user type to pass in? Shouldn't be via GET // dependent on how we
		 * keep track of user privileges
		 * 
		 * String user = ADMIN; getTreeInfo(tid, user);
		 */

		// Reminder: replace with API key once deployed
		// Load Google Maps API asynchronously 
		Maps.loadMapsApi("", "2", true, new Runnable() {
			public void run() {
				geo = new Geocoder();
			}
		});
		
		initHomePage();
		initButtons();
		initLoginLogout();

		History.addValueChangeHandler(new ValueChangeHandler<String>() {
		      public void onValueChange(ValueChangeEvent<String> event) {
		        String historyToken = event.getValue();
	            String role = "";
	            if (loginInfo == null) {
	            	role = "user";
	            } else if (loginInfo.isAdmin()) {
	            	role = "admin";
	            }
	            
	            // TODO: not working yet, need to check getTreeInfo()
		        // Parse the history token
//		        try {
//		          if (historyToken.substring(0, 4).equals("tree")) {
//		            String treeId = historyToken.substring(4);
//		            getTreeInfo(treeId, role);		            		        	
//		          }
//		        } catch (Exception e) {
//		        	// what exception is returned if no tree matches ID? 
//		        	Window.alert("Tree ID is invalid. Please check your URL.");
//		        }
		      }
		    });
		
	}

	private void handleError(Throwable error) {
		Window.alert(error.getMessage());
		if (error instanceof NotLoggedInException) {
			Window.Location.replace(loginInfo.getLogoutUrl());
		}
	}

	/*
	 * add click handlers to buttons, search form 
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

		/*
		 * add doSearch to search button call doSearch
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

	/*
	 * get search terms and make a call to the server if Advanced Search is
	 * open, use the Advanced Search values else use the Basic Search value call
	 * displaySearchResults to display the returned list of results
	 */
	private void doSearch() {
		treeList.clear();
		SearchQuery q = null;
		if (isBasicSearch) {
			/* perform basic search */
			System.out.println("Basic Search:");
			System.out.println(basicSearch.getValue());
			q = new KeywordSearch();			
			q.addSearchParam(SearchFieldID.KEYWORD, "MARY");

		} else {
			/* perform advanced search */
			System.out.println("Advanced Search:");
			for (TextBox tb : advancedSearchList) {
				if (!tb.getValue().equals(""))
					System.out.println(tb.getValue());
			}
		}

		// (aleksy) this is just a fake test search: remove as desired
		if (q != null) {
			treeDataService.searchTreeData(q,
					new AsyncCallback<ArrayList<ClientTreeData>>() {
				@Override
				public void onFailure(Throwable error) {
					handleError(error);
				}

				@Override
				public void onSuccess(ArrayList<ClientTreeData> result) {
					if (result == null) {
						RootPanel.get("content").clear();
						RootPanel.get("content").add(new Label("No matches were found."));
					}
					if (result != null) {
						treeList = result;
						for (ClientTreeData data : result) {
							System.out.println(data.getCommonName());
						}
						displaySearchResults(treeList);

					}
				}
			});

		}
	}

	/**
	 * Replaces content panel with search results
	 * 
	 * @param rlist
	 *            List of ClientTreeData search results from the server
	 */
	private void displaySearchResults(ArrayList<ClientTreeData> rlist) {
		RootPanel content = RootPanel.get("content");
		content.clear();
		
		if (rlist == null) {
			Label noResults = new Label("No results were found.");
			content.add(noResults);
		} else {
			FlexTable resultsTable = new FlexTable();
			resultsTable.setWidth("100%");

			for (final ClientTreeData tree : rlist) {
				HorizontalPanel panel = new HorizontalPanel();
				Anchor species = new Anchor(tree.getCommonName());
				Label location = new Label(tree.getNeighbourhood());

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

			content.add(resultsTable);	
		}
	}

	/**
	 * Retrieves and displays information on a single tree
	 * 
	 * @param id 
	 * 			ID of the tree
	 * @param user 
	 * 			permission of user requesting tree info (admin or user)
	 */
	private void getTreeInfo(String id, String user) {
		treeDataService.getTreeData(id, user,
				new AsyncCallback<ClientTreeData>() {
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
	 * 
	 * @param t
	 *            ClientTreeData to display details of
	 */
	private void displayTreeInfoPage(ClientTreeData t) {
		System.out.println("ID number: " + t.getID());
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
		data.add(createResultDataRow("Species", t.getSpecies()));
		data.add(createResultDataRow("Type", "Rock"));
		data.add(createResultDataRow("Location", t.getCivicNumber() + " " + t.getStreet()));
		data.add(createResultDataRow("Height", "1.2m"));
		data.add(createResultDataRow("Weight", "38.0kg"));

		panel.add(infoMapPanel);
		panel.add(data);
		
		RootPanel.get("content").clear();
		RootPanel.get("content").add(panel);
		
		/* history support */
		History.newItem("tree"+ t.getID());

	}

	/**
	 * Open a popup for a form to add a tree to the database
	 */
	private void addUserTree() {

		final PopupPanel addPanel = new PopupPanel();
		VerticalPanel addForm = new VerticalPanel();
		addFormMap.clear();

		/* create text box and label for each field */
		for (String fld : basicFields) {
			VerticalPanel addtb = createAddTreeRow(fld);
			addForm.add(addtb);
		}
		for (String fld : optionalFields) {
			VerticalPanel addtb = createAddTreeRow(fld);
			addForm.add(addtb);
		}

		/* add submit button */
		Button submitBtn = new Button("Add Tree");
		submitBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// only sends if all required fields are not empty
				// and location is valid
				Set<Label> addFields = addFormMap.keySet();
				ArrayList<String> invalidFields = new ArrayList<String>();
			
				for (Label fld : addFields) {
					String input = addFormMap.get(fld).getValue();
					String name = fld.getText();
					if (name.equalsIgnoreCase("Location")) {
						// kchen: location checking is tricky, let backend code handle it
						/*
						// check for valid location
						LatLng pt = LatLng.fromUrlValue(input);
						if (!validCoordinates(pt)) {
							invalidFields.add(name);
						}*/						
					} else if (name.equalsIgnoreCase("Height") || name.equalsIgnoreCase("Diameter")) {
						// check that height/diameter is a number
						String a = input.trim();
						if (!a.matches("[0-9]+(\\.[0-9]+)?")) {
							invalidFields.add(name);
						}	
					} else if (name.contains("Date")) {
						try {
							dtf.parse(input);
						} catch (Exception e) {
							invalidFields.add(name);
						}
					} else {
						// check for non-empty input
						if (input.trim() == "") {
							invalidFields.add(name);
						}
					}
				}

				if (invalidFields.isEmpty()) {
					try {
						Window.alert("Valid.");
						populateAddData(addFormMap);						
					} catch (Exception e) {
						handleError(e);
					}
					
				} else {
					String errorMsg = "";
					for (String fld : invalidFields) {
						if (fld.equalsIgnoreCase("Location")) {
							errorMsg = errorMsg + "Location must be a valid address or coordinates.\n";
						} else if (fld.contains("Date")) {
							errorMsg = errorMsg + "Date must be in format: 31 January 2012.\n";
						}
						else if (fld.equalsIgnoreCase("Height")) {
							errorMsg = errorMsg + "Height must be a number.\n";
						} else if (fld.equalsIgnoreCase("Diameter")) {
							errorMsg = errorMsg + "Diameter must be a number.\n";
						} else {
							errorMsg = errorMsg + fld + " cannot be empty.\n";				
						}
					}
					Window.alert(errorMsg);
				}
			}
		});

		/* add cancel button to close popup */
		Anchor cancel = new Anchor("Cancel");
		cancel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addPanel.hide();
			}
		});

		HorizontalPanel btnsPanel = new HorizontalPanel();
		btnsPanel.add(submitBtn);
		btnsPanel.add(cancel);
		btnsPanel.setWidth("100%");
		
		addForm.add(btnsPanel);
		addPanel.add(addForm);
		addPanel.setWidth("350px");
		addPanel.setStyleName("add-popup");
		addPanel.center();
	}

	/**
	 * Helper function for initHomePage() Creates a panel with a text box for an
	 * advanced search field
	 * 
	 * @param text
	 *            String for the text box label
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
	 * Adds click handlers to buttons Only button not initialised here is the
	 * search button
	 */
	private void initButtons() {
		/* wrap home button with click handler */
		Button homeButton = Button.wrap(Document.get().getElementById(
				"home-button"));
		homeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// TODO: call home page
			}
		});

		/*
		 * wrap search button with click handler call displaySearchResults with
		 * the list of ClientTreeData returned from the server (not a new
		 * search)
		 */
		Button searchButton = Button.wrap(Document.get().getElementById(
				"search-button"));
		searchButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ClientTreeData tree = new ClientTreeData();
				ArrayList<ClientTreeData> treeList = new ArrayList<ClientTreeData>();
				treeList.add(tree);
				treeList.add(tree);

				displaySearchResults(treeList);
			}
		});

		/* wrap about button with click handler */
		Button aboutButton = Button.wrap(Document.get().getElementById(
				"about-button"));
		aboutButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// call about page
			}
		});

		Button addButton = Button.wrap(Document.get().getElementById(
				"add-tree-button"));
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addUserTree();
			}
		});
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
	 * 
	 * @param text
	 * @return
	 */
	private VerticalPanel createAddTreeRow(String text) {
		VerticalPanel row = new VerticalPanel();
		row.setWidth("100%");
		Label lbl = new Label(text);
		TextBox tb = new TextBox();
		addFormMap.put(lbl, tb);

		row.add(lbl);
		row.add(tb);
		row.setCellHeight(row, "30px");
		row.setCellHeight(tb, "50px");
		return row;
	}

	
	/**
	 * Creates a new ClientTreeData based on info from Add Tree form
	 * 
	 * @param list
	 * 			list of fields/input from form
	 * @return	ClientTreeData with the corresponding input
	 * @throws InvalidFieldException
	 * 			thrown when input is not properly formatted
	 */
	private void populateAddData(LinkedHashMap<Label, TextBox> list) 
			throws InvalidFieldException {
		addTree = new ClientTreeData();
		asyncCall = false;
		doneParse = false;
		for (Map.Entry<Label, TextBox> entry : list.entrySet()) {
			String key = entry.getKey().getText();
			String input = entry.getValue().getValue();
			input = input.trim();

			// TODO: decide to keep or discard coordinate storing in database
			// this assumes valid location/coords in form
			// #### Street Name or #, #
			if (key.equals("Location")) {
				boolean isAddr = true;
				String[] loc = input.split("[,]");
				if (loc.length == 2) {
					isAddr = false;
				}
				// try parsing as address
				if (isAddr) {
					geoAddr = new Address(input);
					if (geoAddr.getNumber() < 0) {
						throw new InvalidFieldException("Invalid field: Location");
					}
					addTree.setCivicNumber(geoAddr.getNumber());
					addTree.setStreet(geoAddr.getStreet());
				}
				// try parsing as coordinates
				else {
					try {
						LatLng pt = LatLng.fromUrlValue(input);
						if (!validCoordinates(pt)) {
							throw new InvalidFieldException("Invalid field: Location");
						}
						// reverse geocode coordinates -> address
						asyncCall = true;
						geo.getLocations(pt, new LocationCallback() {
							public void onFailure(int e) {
								handleError(new InvalidFieldException("Invalid field: Location"));
							}

							public void onSuccess(JsArray<Placemark> p) {
								if (p.length() <= 0) {
									handleError(new InvalidFieldException("Invalid field: Location"));
								}
								// uses first placemark result only
								// getAddress has format ### Street, Vancouver, BC postal_code, Canada
								else {
									geoAddr = new Address(p.get(0).getAddress());
									addTree.setCivicNumber(geoAddr.getNumber());
									addTree.setStreet(geoAddr.getStreet());
									sendAddTreeData(addTree);
								}
							}
						});
					} catch (Exception e) {
						throw new InvalidFieldException("Invalid field: Location");
					}
				}
			} else if (key.equals("Genus")) {
				addTree.setGenus(input);
			} else if (key.equals("Species")) {
				addTree.setSpecies(input);
			} else if (key.equals("Common Name")) {
				addTree.setCommonName(input);
			} else if (key.equals("Neighbourhood")) {
				addTree.setNeighbourhood(input);
			} else if (key.equals("Height")) {
				try {
					// TODO: need a setHeight field
					// t.setHeight(Double.parseDouble(input));
					int h = (int) Double.parseDouble(input); // just in case it's a float
					int range = -1;
					if (h < 0) {
						range = 0;
					} else if (h < 100) {
						range = h / 10;
					} else {
						range = 10;
					}
					addTree.setHeightRange(range);
				} catch (Exception e) {
					throw new InvalidFieldException("Invalid field: Height");
				}
			} else if (key.equals("Diameter")) {
				try {
					addTree.setDiameter((int) Double.parseDouble(input));
				} catch (Exception e) {
					throw new InvalidFieldException("Invalid field: Diameter");
				}
			} else if (key.equals("Date Planted")) {
				addTree.setPlanted(dtf.parse(input));
			}
		}
		doneParse = true;
		if (!asyncCall) {
			sendAddTreeData(addTree);
		}
	}
	
	private void sendAddTreeData(ClientTreeData t) {
		// only execute if done parsing
		if (!doneParse) {
			asyncCall = false;
			return;
		}
		treeDataService.addTree(t, new AsyncCallback<Void>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}
			public void onSuccess(Void result) {
				Window.alert("Tree added.");
				displayTreeInfoPage(addTree); // debugging
				// TODO
				// maybe it'd be nice to be redirected to newly added tree info page?
				// would require TreeDataService to return ClientTreeData from server								
			}					
		});
	}

	private void setTreeInfoMap(ClientTreeData data) {
		infoMapPanel.clear();
		infoMapPanel.setSize("400px", "400px");

		if (data == null) {
			infoMapPanel.add(invalidLoc);
			return;
		}
		// just in case city is required in search
		String loc =  ((data.getCivicNumber() == 0) ? data.getCivicNumber() + " " : "")
						+ data.getStreet() + ", Vancouver, BC"; 
		System.out.println("location: " + loc);
		geo.getLatLng(loc, new LatLngCallback() {
			public void onFailure() {
				infoMapPanel.add(invalidLoc);
			}

			public void onSuccess(LatLng pt) {
				setTreeInfoMap(pt);
			}
		});

	}

	private void setTreeInfoMap(LatLng pt) {
		if (pt == null) {
			infoMapPanel.add(invalidLoc);
			return;
		}
		MapWidget map = new MapWidget(pt, ZOOM_LVL);
		map.setSize("400px", "400px");
	    map.setUIToDefault();
		infoMapPanel.add(map);
	}

	private boolean validCoordinates(LatLng c) {
		if (Double.isNaN(c.getLatitude()) || Double.isNaN(c.getLongitude()))
			return false;
		return true;
	}
	
	/**
	 * Inner class to hold a street number, street address pair
	 *
	 */
	private class Address {
		private int num;
		private String street;
		
		
		/**
		 * Parses String input to get number and street address
		 * @param input
		 */
		public Address(String input) {
			String[] addr = input.split("[,]", 2);
			addr = addr[0].split("\\s+", 2);
			if (addr.length != 2) {
				num = -1;
				street = "";
				return;
			}
			try {
				num = Integer.parseInt(addr[0]);
				street = addr[1].trim();
			} catch (Exception e) {
				num = 0;  // possibly no street number
				street = addr[0] + " " + addr[1];
			}
		}
		
		public Address(int n, String s) {
			num = n;
			street = s;
		}
		
		public int getNumber() {
			return num;
		}
		
		public String getStreet() {
			return street;
		}
	}
	
}
