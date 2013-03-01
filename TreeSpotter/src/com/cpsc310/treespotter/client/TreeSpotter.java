package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler.MarkerClickEvent;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
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
	private static final String LOCATION = "Location";
	private static final String GENUS = "Genus";
	private static final String SPECIES = "Species";
	private static final String COMMON = "Common Name";
	private static final String NEIGHBOUR = "Neighbourhood";
	private static final String HEIGHT = "Height";
	private static final String DIAMETER = "Diameter";
	private static final String PLANTED = "Date Planted";
	private String[] basicFields = { LOCATION, GENUS, SPECIES, COMMON };
	private String[] optionalFields = { NEIGHBOUR, HEIGHT, DIAMETER, PLANTED };

	private boolean isBasicSearch = true;
	private TextBox basicSearch = null;

	// list of the input boxes, so the values can be retrieved
	private LinkedHashMap<Label, TextBox> advancedSearchMap = new LinkedHashMap<Label, TextBox>();
	private LinkedHashMap<Label, TextBox> addFormMap = new LinkedHashMap<Label, TextBox>();

	private final TreeDataServiceAsync treeDataService = GWT
			.create(TreeDataService.class);
	
	// variables for loading map view
	private final VerticalPanel infoMapPanel = new VerticalPanel();
	private final VerticalPanel searchMapPanel = new VerticalPanel();
	private Geocoder geo;
	private Label invalidLoc = new Label("Tree location could not be displayed");
	private static final int ZOOM_LVL = 15;
	private MapWidget searchMap;
	private int listIndex;
	private boolean isPopulated = true;
	private Icon icon;
	private final String greenIconURL = "http://maps.gstatic.com/mapfiles/ridefinder-images/mm_20_green.png";
	private ArrayList<Marker> markers = new ArrayList<Marker>();
	private LatLng start;
	private ArrayList<ClientTreeData> treeResults = new ArrayList<ClientTreeData>();
	
	// for adding tree
	private Address geoAddr;
	private ClientTreeData addTree;
	private boolean doneParse = true;
	private boolean asyncCall = false;
	
	private DateTimeFormat dtf = DateTimeFormat.getFormat("d MMM yyyy");
	private static final String ADMIN = "admin";
	// in order to be accessed by inner classes this has to be a member
	private ArrayList<ClientTreeData> treeList = new ArrayList<ClientTreeData>();
	
	// table for TreeInfo
	private FlexTable treeInfoTable = null;

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
//				searchMap = new TreeSearchMap();
				icon = Icon.newInstance(greenIconURL);
				icon.setIconAnchor(Point.newInstance(6, 20));
			    start = LatLng.newInstance(49.26102, -123.249339);

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
		searchInput.setWidth("450px");
		searchBtn.setStyleName("main-search");

		searchPanel.add(searchInput);
		searchPanel.add(searchBtn);

		/* store search text box to retrieve search terms later */
		basicSearch = searchInput;

		/* set up advanced search elements */
		final VerticalPanel advancedForm = new VerticalPanel();
		advancedForm.setStyleName("main-search");
		for (String field : basicFields) {
			HorizontalPanel advtb = createSearchPanel(field); 
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
		
		loadHomePage();
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
			q.addSearchParam(SearchFieldID.KEYWORD, basicSearch.getValue());

		} else {
			/* perform advanced search */
			System.out.println("Advanced Search:");
			q = new AdvancedSearch();
			String input = basicSearch.getValue().trim();
			if (!input.isEmpty()) {
				q.addSearchParam(SearchFieldID.KEYWORD, input);
				System.out.println("Keyword: " + input);
			}

			for (Map.Entry<Label, TextBox> entry : advancedSearchMap.entrySet()) {
				String key = entry.getKey().getText();
				input = entry.getValue().isEnabled() ? entry.getValue().getValue().trim() : "";
				if (!input.isEmpty()) {
					System.out.println(key + ": " + input);
					if (key.equalsIgnoreCase(LOCATION)) {
						q.addSearchParam(SearchFieldID.LOCATION, input);
					} else if (key.equalsIgnoreCase(GENUS)) {
						q.addSearchParam(SearchFieldID.GENUS, input);
					} else if (key.equalsIgnoreCase(SPECIES)) {
						q.addSearchParam(SearchFieldID.SPECIES, input);
					} else if (key.equalsIgnoreCase(COMMON)) {
						q.addSearchParam(SearchFieldID.COMMON, input);
					}
				}
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
						RootPanel.get("content").add(new Label("No results were found."));
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
		if (rlist.isEmpty()) {
			Label noResults = new Label("No results were found.");
			content.add(noResults);
		} else {
			setPoints(rlist);
			FlexTable resultsTable = new FlexTable();
			resultsTable.setWidth("100%");

			for (final ClientTreeData tree : rlist) {
				HorizontalPanel panel = new HorizontalPanel();
				Anchor species = new Anchor(tree.getCommonName());
				Label location = new Label(tree.getLocation());

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
			content.add(searchMapPanel);
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
		HTMLPanel panel = new HTMLPanel("");
		panel.setStyleName("treeinfo");
		
		/* create the map */
		setTreeInfoMap(t);

		/* create table with all the data */
		treeInfoTable = new FlexTable();
		treeInfoTable.setStyleName("treedata");
		treeInfoTable.setCellPadding(10);
		treeInfoTable.setWidth("400px");

		createResultDataRow("Species", t.getSpecies());
		createResultDataRow("Genus", t.getGenus());
		createResultDataRow("Common Name", t.getCommonName());
		createResultDataRow("Location", t.getCivicNumber() + " " + t.getStreet());
		createResultDataRow("Neighbourhood", t.getNeighbourhood());
		createResultDataRow("Height", Integer.toString(t.getHeightRange()));
		// TODO: actually convert it....units?
		createResultDataRow("Diameter", Integer.toString(t.getHeightRange()));
		//createResultDataRow("Date Planted", t.getPlanted().toString());
		// can be null?
		
		
		
		panel.add(infoMapPanel);
		panel.add(treeInfoTable);
		
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
			VerticalPanel addtb = createAddTreeRow(fld, true);
			addForm.add(addtb);
		}
		for (String fld : optionalFields) {
			VerticalPanel addtb = createAddTreeRow(fld, false);
			addForm.add(addtb);
		}

		/* add submit button */
		Button submitBtn = new Button("Add Tree");
		submitBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// only sends if all required fields are not empty
				// and location is valid
				Set<Label> addFields = addFormMap.keySet();
				List<String> optional = Arrays.asList(optionalFields);
				ArrayList<String> invalidFields = new ArrayList<String>();		
			
				for (Label fld : addFields) {
					String input = addFormMap.get(fld).getValue();
					String name = fld.getText();
					if (optional.contains(name)) {
						// optional field, so don't add it as invalid if its empty
						String a = input.trim();
						if (a == "") {
							// do nothing for optional fields
						}
						else if (name.equalsIgnoreCase(HEIGHT) || name.equalsIgnoreCase(DIAMETER)) {
							// check that height/diameter is a number
							if (!a.matches("[0-9]+(\\.[0-9]+)?")) {
								invalidFields.add(name);
							}	
						} else if (name.contains("Date")) {
							try {
								dtf.parse(input);
							} catch (Exception e) {
								invalidFields.add(name);
							}
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
						addPanel.hide();
						populateAddData(addFormMap);						
					} catch (Exception e) {
						handleError(e);
					}
					
				} else {
					String errorMsg = "";
					for (String fld : invalidFields) {
						if (fld.equalsIgnoreCase(LOCATION)) {
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
		tb.setEnabled(false);
		tb.setReadOnly(true);

		Label label = new Label();
		label.setText(text);
		label.setStyleName("advanced-search");

		/* add handler to enable/disable text box based on check box */
		cb.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (!cb.getValue()) {
					tb.setEnabled(false);
					tb.setReadOnly(true);
					tb.setText("");
					tb.setStyleName("disabled");
				} else {
					tb.setEnabled(true);
					tb.setReadOnly(false);
					tb.setStyleName("enabled");
				}
			}
		});

		/* add text box to the list */
		advancedSearchMap.put(label, tb);

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
				loadHomePage();
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
	 *  Loads home.html into the content panel
	 */
	private void loadHomePage() {
		HTMLPanel htmlPanel = new HTMLPanel(HTMLResource.INSTANCE.getHomeHtml().getText());
		RootPanel.get("content").clear();
		RootPanel.get("content").add(htmlPanel);
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
	private void createResultDataRow(String field, String value) {
		int rowNum = treeInfoTable.getRowCount();
		Label fld = new Label(field);
		fld.setStyleName("info-field");
		treeInfoTable.setWidget(rowNum, 0, fld);
		treeInfoTable.setWidget(rowNum, 1, new Label(value));
	}

	/**
	 * Helper method to create fields for the add tree popup
	 * 
	 * @param text
	 * @return
	 */
	private VerticalPanel createAddTreeRow(String text, boolean req) {
		VerticalPanel row = new VerticalPanel();
		row.setWidth("100%");
		
		if (req) {
			text = "<b>" + text + " * </b>";
		}
		
		HTML lbl = new HTML(text);
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
			String key = entry.getKey().getText().split("\\s[*]")[0];
			String input = entry.getValue().getValue().trim();

			// TODO: decide to keep or discard coordinate storing in database
			// this assumes valid location/coords in form
			// #### Street Name or #, #
			if (key.equalsIgnoreCase(LOCATION)) {
				boolean isAddr = true;
				String[] loc = input.split("[,]");
				if (loc.length == 2) {
					isAddr = false;
				}
				// try parsing as address
				if (isAddr) {
					geoAddr = new Address(input);
					if (!geoAddr.isValid()) {
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
			} else if (key.equalsIgnoreCase(GENUS)) {
				addTree.setGenus(input);
			} else if (key.equalsIgnoreCase(SPECIES)) {
				addTree.setSpecies(input);
			} else if (key.equalsIgnoreCase(COMMON)) {
				addTree.setCommonName(input);
			} else if (key.equalsIgnoreCase(NEIGHBOUR) && !input.isEmpty()) {
				addTree.setNeighbourhood(input);
			} else if (key.equalsIgnoreCase(HEIGHT) && !input.isEmpty()) {
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
			} else if (key.equalsIgnoreCase(DIAMETER) && !input.isEmpty()) {
				try {
					addTree.setDiameter((int) Double.parseDouble(input));
				} catch (Exception e) {
					throw new InvalidFieldException("Invalid field: Diameter");
				}
			} else if (key.equalsIgnoreCase(PLANTED) && !input.isEmpty()) {
				try {
					Date d = dtf.parse(input);
					addTree.setPlanted(d);
				} catch (Exception e) {
					throw new InvalidFieldException("Invalid field: Date Planted");
				}
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
		treeDataService.addTree(t, new AsyncCallback<ClientTreeData>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}
			public void onSuccess(ClientTreeData result) {
				if(result != null){
					Window.alert("Tree " + result.getID() + " added.");
				}
				else{
					Window.alert("Tree not added");
				}
				displayTreeInfoPage(result); // debugging
				// TODO
				// maybe it'd be nice to be redirected to newly added tree info page?
				// would require TreeDataService to return ClientTreeData from server								
			}					
		});
	}

	private void setTreeInfoMap(ClientTreeData data) {
		infoMapPanel.clear();
		infoMapPanel.setStyleName("map");
		infoMapPanel.setSize("400px", "400px");

		if (data == null) {
			infoMapPanel.add(invalidLoc);
			return;
		}
		// just in case city is required in search
		String loc =  ((data.getCivicNumber() >= 0) ? data.getCivicNumber() + " " : "")
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
	    Marker m = new Marker(pt);
	    map.addOverlay(m);
		infoMapPanel.add(map);
	}
	
	private void setSearchInfoMap() {
		searchMapPanel.clear();
		searchMap = new MapWidget();
		searchMap.setSize("400px", "400px");
		searchMap.setUIToDefault();

		// TODO: find middle of all points for centre
		searchMap.setCenter(start, ZOOM_LVL);
		
		for (Marker m : markers) {
			searchMap.addOverlay(m);
		}
		searchMapPanel.add(searchMap);
	}
	
	public void setPoints(ArrayList<ClientTreeData> list) {
		treeResults = list;
		listIndex = 0;
		markers.clear();
		isPopulated = false;
		getNextPoint(listIndex);
	}
	
	private void getNextPoint(int idx) {
		if (idx >= treeResults.size()) {
			isPopulated = true;
			setSearchInfoMap();
			return;
		}
		
		ClientTreeData t = treeResults.get(idx);
		String loc = ((t.getCivicNumber() >= 0) ? t.getCivicNumber() + " " : "")
				+ t.getStreet() + ", Vancouver, BC";
		geo.getLatLng(loc, new LatLngCallback() {
			public void onFailure() {
				// skip to next marker
				getNextPoint(++listIndex);
			}

			public void onSuccess(LatLng pt) {
				addPoint(pt);
			}
		});
	}

	private void addPoint(LatLng pt) {
		MarkerOptions options = MarkerOptions.newInstance();
		options.setIcon(icon);
		// TODO: replace with tree info
		options.setTitle("index: " + listIndex++);
		Marker mark = new Marker(pt, options);
		mark.addMarkerClickHandler(new MarkerClickHandler() {
			public void onClick(MarkerClickEvent event) {
				clickMarker(event.getSender());
			}
		});
		markers.add(mark);

		getNextPoint(listIndex);
	}

	private void clickMarker(Marker m) {
		LatLng pt = m.getLatLng();
		searchMap.getInfoWindow().open(
				pt,
				new InfoWindowContent("<p>" + pt.getLatitude() + ", "
						+ pt.getLongitude() + "<br/>" + m.getTitle() + "</p>"));
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
		private boolean valid;
		
		
		/**
		 * Parses String input to get number and street address
		 * @param input
		 */
		public Address(String input) {
			String[] addr = input.split("[,]", 2);
			addr = addr[0].split("\\s+", 2);
			if (addr.length < 1) {
				num = -1;
				street = "";
				valid = false;
				return;
			}
			try {
				num = Integer.parseInt(addr[0]);
				street = addr[1].trim();
				valid = true;
			} catch (Exception e) {
				num = -1;  // possibly no street number
				street = addr[0] + " " + addr[1];
				valid = true;
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
		
		public boolean isValid() {
			return valid;
		}
	}
	
}
