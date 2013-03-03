package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TreeSpotter implements EntryPoint {
	private LoginInfo loginInfo = null;
	private static final String wikipediaSearchURL = "http://en.wikipedia.org/wiki/Special:Search/";

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
	private final String INFO_MAP_SIZE = "500px";
	private final String SEARCH_MAP_SIZE = "600px";
	private Geocoder geo;
	private Label invalidLoc = new Label("Tree location could not be displayed");
	private static final int ZOOM_LVL = 12;
	private MapWidget searchMap;
	private int listIndex;
	private Icon icon;
	private final String greenIconURL = "http://maps.gstatic.com/mapfiles/ridefinder-images/mm_20_green.png";
	private ArrayList<Marker> markers = new ArrayList<Marker>();
	private LatLng start;

	private List<ClientTreeData> treeResults = new ArrayList<ClientTreeData>();
	private LatLngBounds searchMapBound;

	// for adding tree
	private Address geoAddr;
	private ClientTreeData addTree;

	// in order to be accessed by inner classes this has to be a member
	private ArrayList<ClientTreeData> treeList = new ArrayList<ClientTreeData>();

	// table for TreeInfo
	private FlexTable treeInfoTable = null;

	// tab panel container for search results
	private TabPanel searchResultsPanel = null;

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
				icon = Icon.newInstance(greenIconURL);
				icon.setIconAnchor(Point.newInstance(6, 20));
				start = LatLng.newInstance(49.26102, -123.249339);
			}
		});

		initHomePage();
		initButtons();
		initLoginLogout();

		/*
		 * History.addValueChangeHandler(new ValueChangeHandler<String>() {
		 * public void onValueChange(ValueChangeEvent<String> event) { String
		 * historyToken = event.getValue(); String role = ""; if (loginInfo ==
		 * null) { role = "user"; } else if (loginInfo.isAdmin()) { role =
		 * "admin"; }
		 * 
		 * // TODO: not working yet, need to check getTreeInfo() // Parse the
		 * history token try { if (historyToken.substring(0, 4).equals("tree"))
		 * { String treeId = historyToken.substring(4); getTreeInfo(treeId,
		 * role); } } catch (Exception e) { // what exception is returned if no
		 * tree matches ID?
		 * Window.alert("Tree ID is invalid. Please check your URL."); } } });
		 */
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
		searchPanel.setStyleName("main-search-box");

		/* store search text box to retrieve search terms later */
		basicSearch = searchInput;

		/* set up advanced search elements */
		final VerticalPanel advancedForm = new VerticalPanel();
		advancedForm.setStyleName("main-search-fields");
		for (String field : basicFields) {
			HorizontalPanel advtb = createSearchPanel(field);
			advancedForm.add(advtb);
		}
		for (String field : optionalFields) {
			if (field.equals(PLANTED) || field.equals(DIAMETER)
					|| field.equals(HEIGHT)) {
				// not supported for now
			} else {
				HorizontalPanel advtb = createSearchPanel(field);
				advancedForm.add(advtb);
			}
		}
		advancedForm.setVisible(false);

		/* set up advanced search link */
		Anchor advancedLink = new Anchor("Advanced Options");
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
		 * add doSearch to search button
		 */
		searchBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				doSearch();
			}
		});
		
		basicSearch.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					doSearch();
				}
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
				input = entry.getValue().isEnabled() ? entry.getValue()
						.getValue().trim() : "";
				if (!input.isEmpty()) {
					System.out.println(key + ": " + input);
					if (key.equalsIgnoreCase(LOCATION)) {
						try {
							input = ParseUtils.formatSearchLocation(input);
							SearchFieldID sid = ParseUtils.isLocationSearch(input) ?
										SearchFieldID.LOCATION : SearchFieldID.ADDRESS;
							q.addSearchParam(sid, input);
						} catch (InvalidFieldException e) {
							handleError(e);
						}
					} else if (key.equalsIgnoreCase(GENUS)) {
						q.addSearchParam(SearchFieldID.GENUS, input);
					} else if (key.equalsIgnoreCase(SPECIES)) {
						q.addSearchParam(SearchFieldID.SPECIES, input);
					} else if (key.equalsIgnoreCase(COMMON)) {
						q.addSearchParam(SearchFieldID.COMMON, input);
					} else if (key.equalsIgnoreCase(NEIGHBOUR)) {
						q.addSearchParam(SearchFieldID.NEIGHBOUR, input);
					}
				}
			}
		}

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
								RootPanel.get("content").add(
										new Label("No results were found."));
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
	private void displaySearchResults(final ArrayList<ClientTreeData> rlist) {
		RootPanel content = RootPanel.get("content");
		content.clear();

		if (rlist.isEmpty()) {
			Label noResults = new Label("No results were found.");
			content.add(noResults);

		} else if (rlist.size() <= 25) {
			// don't bother with tabs if less than 25 results
			FlexTable resultsTable = createSearchPage(0, rlist);
			setPoints(rlist);
			content.add(searchMapPanel);
			content.add(resultsTable);

		} else {
			searchResultsPanel = new TabPanel();

			for (int i = 0; i < rlist.size(); i = i + 25) {
				// create the table for each set of 25 results
				int end = Math.min(rlist.size(), i + 25);
				List<ClientTreeData> rsublist = rlist.subList(i, end);
				FlexTable resultsTable = createSearchPage(i, rsublist);
				searchResultsPanel.add(resultsTable,
						Integer.toString((i / 25) + 1));
			}

			// set map points to the same set
			searchResultsPanel
					.addSelectionHandler(new SelectionHandler<Integer>() {
						public void onSelection(SelectionEvent<Integer> event) {
							int start = event.getSelectedItem() * 25;
							int end = Math.min(rlist.size(), start + 24);
							List<ClientTreeData> pageList = rlist.subList(
									start, end);
							setPoints(pageList);
						}
					});

			// add the tree search map
			content.add(searchMapPanel);
			searchResultsPanel.selectTab(0);
			content.add(searchResultsPanel);
		}
	}

	private FlexTable createSearchPage(int start, List<ClientTreeData> rlist) {
		FlexTable resultsTable = new FlexTable();
		resultsTable.setWidth("480px");
		resultsTable.setCellPadding(2);
		int index = start;

		// set table headers
		resultsTable.setWidget(0, 0, new HTML("Result"));
		resultsTable.setWidget(0, 1, new HTML("Common Name"));
		resultsTable.setWidget(0, 2, new HTML("Address"));
		resultsTable.setWidget(0, 3, new HTML("Neighbourhood"));

		for (final ClientTreeData tree : rlist) {
			Anchor num = new Anchor(Integer.toString(++index));
			HTML common = new HTML(ParseUtils.capitalize(tree.getCommonName(), false));
			HTML addr = new HTML(ParseUtils.capitalize(tree.getLocation(), false));
			String thood = tree.getNeighbourhood()==null ? "" : ParseUtils.capitalize(tree.getNeighbourhood(), false);
			HTML hood = new HTML(thood);

			/* add link to the tree info page */
			num.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					displayTreeInfoPage(tree);
				}
			});
			common.setWordWrap(true);
			addr.setWordWrap(true);
			int rows = resultsTable.getRowCount();
			resultsTable.setWidget(rows, 0, num);
			resultsTable.setWidget(rows, 1, common);
			resultsTable.setWidget(rows, 2, addr);
			resultsTable.setWidget(rows, 3, hood);

			if (rlist.indexOf(tree) % 2 != 0) {
				resultsTable.getRowFormatter().setStyleName(rows,
						"results-row-alt");
			}

		}

		// set styles
		resultsTable.setStyleName("results-table");
		resultsTable.getRowFormatter().setStyleName(0, "results-header");

		return resultsTable;
	}

	/**
	 * Retrieves and displays information on a single tree
	 * 
	 * @param id
	 *            ID of the tree
	 * @param user
	 *            permission of user requesting tree info (admin or user)
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
		/* create the map */
		setTreeInfoMap(t);

		/* create table with all the data */
		treeInfoTable = new FlexTable();
		treeInfoTable.setStyleName("treedata");
		treeInfoTable.setCellPadding(10);
		treeInfoTable.setSize("400px", "400px");

		createResultDataRow("Species", ParseUtils.capitalize(t.getSpecies(), true));
		createResultDataRow("Genus", ParseUtils.capitalize(t.getGenus(), false));
		String capName = ParseUtils.capitalize(t.getCommonName(), false);
		createResultDataRow("Common Name", "<a href='" + wikipediaSearchURL
				+ capName + "'>" + capName + "</a>");
		createResultDataRow("Location", ParseUtils.capitalize(t.getLocation(), false));
		String neighbour = t.getNeighbourhood();
		neighbour = (neighbour == null) ? neighbour : neighbour.toUpperCase();
		createResultDataRow("Neighbourhood", neighbour);
		createResultDataRow("Date Planted", t.getPlanted());
		createResultDataRow("Height", t.getHeightRange());

		// need to check if the diameter is -1
		String dm = t.getDiameter() == -1.0 ? null : Double.toString(t
				.getDiameter()) + " inches";
		createResultDataRow("Diameter", dm);

		RootPanel content = RootPanel.get("content");
		content.clear();
		content.add(infoMapPanel);
		content.add(treeInfoTable);

		/* history support */
		// TODO: put this back when history works
		//History.newItem("tree" + t.getID());

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
				Set<Label> addFields = addFormMap.keySet();
				List<String> optional = Arrays.asList(optionalFields);
				ArrayList<String> invalidFields = new ArrayList<String>();

				for (Label fld : addFields) {
					String input = addFormMap.get(fld).getValue();
					String name = fld.getText();
					if (optional.contains(name)) {
						// optional field, so don't add it as invalid if its
						// empty
						String a = input.trim();
						if (a == "") {
							// do nothing for optional fields
						} else if (name.equalsIgnoreCase(HEIGHT)
								|| name.equalsIgnoreCase(DIAMETER)) {
							// check that height/diameter is a number
							if (!a.matches("[0-9]+(\\.[0-9]+)?")) {
								invalidFields.add(name);
							}
					} else {
						// check for non-empty input
						if (input.trim() == "") {
							invalidFields.add(name);
						}
					}
					}
				}

				if (invalidFields.isEmpty()) {
					try {
						addPanel.hide();
						populateAddData(null);
					} catch (Exception e) {
						handleError(e);
					}

				} else {
					String errorMsg = "";
					for (String fld : invalidFields) {
						if (fld.equalsIgnoreCase(LOCATION)) {
							errorMsg = errorMsg
									+ "Location must be a valid address or coordinates.\n";
						} else if (fld.equalsIgnoreCase("Height")) {
							errorMsg = errorMsg + "Height must be a number.\n";
						} else if (fld.equalsIgnoreCase("Diameter")) {
							errorMsg = errorMsg
									+ "Diameter must be a number.\n";
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
		final TextBox tb = new TextBox();
		tb.setStyleName("disabled");

		HTML label = new HTML(text);
		label.setStyleName("advanced-search");

		// textbox will be enabled when clicked
		// disabled if empty when clicking away
		tb.addFocusHandler(new FocusHandler() {
			public void onFocus(FocusEvent event) {
				tb.setStyleName("enabled");
			}
		});

		tb.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				if (tb.getValue().trim().equals("")) {
					tb.setStyleName("disabled");
				}
			}

		});

		/* add text box to the list */
		advancedSearchMap.put(label, tb);

		/* add all elements to the panel */
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

		/* wrap add tree button with click handler */
		Button addButton = Button.wrap(Document.get().getElementById(
				"add-tree-button"));
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addUserTree();
			}
		});
	}

	/**
	 * Loads home.html into the content panel
	 */
	private void loadHomePage() {
		HTMLPanel htmlPanel = new HTMLPanel(HTMLResource.INSTANCE.getHomeHtml()
				.getText());
		RootPanel.get("content").clear();
		RootPanel.get("content").add(htmlPanel);
	}

	/**
	 * Replaces main content panel with a loading bar
	 */
	private void loadLoadingBar() {
		HTMLPanel htmlPanel = new HTMLPanel(HTMLResource.INSTANCE
				.getLoadingbar().getText());
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

		if (value == null || value.equals("-1") || value.equals("-1.0 inches")) {
			value = "Not available";
		}
		treeInfoTable.setWidget(rowNum, 1, new HTML(value));
	}

	/**
	 * Helper function to create height data row for the TreeInfoPage
	 * 
	 * @param range
	 *            Tree height range
	 * 
	 * @return String of height range ie. "0 - 10 ft"
	 */
	private void createResultDataRow(String field, int range) {
		int rowNum = treeInfoTable.getRowCount();
		Label fld = new Label(field);
		String value = "";
		fld.setStyleName("info-field");
		treeInfoTable.setWidget(rowNum, 0, fld);

		if (range == -1) {
			value = "Not available";
		} else if (range == 10) {
			value = "Over 10 ft";
		} else {
			value = Integer.toString(range * 10) + " - "
					+ Integer.toString((range + 1) * 10) + " ft";
		}
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
	 * Helper method for addUserTree. Parses and populates a ClientTreeData
	 * object from add tree form.
	 * 
	 * @param t
	 *            if null, location not yet parsed else, contains
	 *            reverse-geocoded location
	 * @throws InvalidFieldException
	 *             thrown if input is not in valid format for any field
	 */
	private void populateAddData(ClientTreeData t) throws InvalidFieldException {
		boolean parseLoc = true;
		if (t == null) {
			addTree = new ClientTreeData();
		} else {
			addTree = t;
			parseLoc = false;
		}

		for (Map.Entry<Label, TextBox> entry : addFormMap.entrySet()) {
			String key = entry.getKey().getText().split("\\s[*]")[0];
			String input = entry.getValue().getValue().trim();

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
						throw new InvalidFieldException(
								"Invalid field: Location");
					}
					addTree.setCivicNumber(geoAddr.getNumber());
					addTree.setStreet(geoAddr.getStreet());
				}
				// try parsing as coordinates
				else if (parseLoc) {
					try {
						LatLng pt = LatLng.fromUrlValue(input);
						if (!ParseUtils.validCoordinates(pt)) {
							throw new InvalidFieldException(
									"Invalid field: Location");
						}
						// reverse geocode coordinates -> address
						geo.getLocations(pt, new LocationCallback() {
							public void onFailure(int e) {
								handleError(new InvalidFieldException(
										"Invalid field: Location"));
							}

							public void onSuccess(JsArray<Placemark> p) {
								if (p.length() <= 0) {
									handleError(new InvalidFieldException(
											"Invalid field: Location"));
								}
								// uses first placemark result only
								// getAddress has format ### Street, Vancouver,
								// BC postal_code, Canada
								else {
									geoAddr = new Address(p.get(0).getAddress());
									addTree.setCivicNumber(geoAddr.getNumber());
									addTree.setStreet(geoAddr.getStreet());
									try {
										populateAddData(addTree);
									} catch (Exception e) {
										handleError(e);
									}
								}
							}
						});
						return;
					} catch (Exception e) {
						throw new InvalidFieldException(
								"Invalid field: Location");
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
					int h = (int) Double.parseDouble(input); // just in case
																// it's a float
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
					addTree.setPlanted(ParseUtils.formatDate(input));
				} catch (Exception e) {
					throw new InvalidFieldException(
							"Invalid field: Date Planted");
				}
			}
		}
		sendAddTreeData(addTree);
	}

	/**
	 * Helper method for addUserTree. Sends ClientTreeData populated from add
	 * tree form to server
	 * 
	 * @param t
	 *            ClientTreeData to be sent for persistence
	 */
	private void sendAddTreeData(ClientTreeData t) {
		treeDataService.addTree(t, new AsyncCallback<ClientTreeData>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}

			public void onSuccess(ClientTreeData result) {
				if (result != null) {
					Window.alert("Tree " + result.getID() + " added.");
				} else {
					Window.alert("Tree not added");
				}
				displayTreeInfoPage(result);
			}
		});
	}

	/**
	 * Sets the tree info map to the geocoded location
	 * 
	 * @param data
	 *            tree to be displayed
	 */
	private void setTreeInfoMap(ClientTreeData data) {
		infoMapPanel.clear();
		infoMapPanel.setStyleName("map");
		infoMapPanel.setSize(INFO_MAP_SIZE, INFO_MAP_SIZE);

		if (data == null) {
			infoMapPanel.add(invalidLoc);
			return;
		}
		// just in case city is required in search
		String loc = data.getLocation() + ", Vancouver, BC";
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

	/**
	 * Helper method for setTreeInfoMap. Sets map to coordinates, centred and
	 * zoomed
	 * 
	 * @param pt
	 *            passed in from async geocoding call
	 */
	private void setTreeInfoMap(LatLng pt) {
		if (pt == null) {
			infoMapPanel.add(invalidLoc);
			return;
		}
		MapWidget map = new MapWidget(pt, ZOOM_LVL);
		map.setSize(INFO_MAP_SIZE, INFO_MAP_SIZE);
		map.setUIToDefault();
		Marker m = new Marker(pt);
		map.addOverlay(m);
		infoMapPanel.add(map);
	}

	/**
	 * Once search results have been made into markers, generate a map panned
	 * and zoomed to contain all markers, then add it to SearchMapPanel
	 */
	private void setSearchInfoMap() {
		searchMapPanel.clear();
		searchMap = new MapWidget();
		searchMap.setSize(SEARCH_MAP_SIZE, SEARCH_MAP_SIZE);
		searchMap.setUIToDefault();
		searchMapBound = LatLngBounds.newInstance();

		// set this as default, should be updated in loop
		searchMap.setCenter(start, ZOOM_LVL);

		for (Marker m : markers) {
			searchMap.addOverlay(m);
			searchMapBound.extend(m.getLatLng());
		}
		if (!searchMapBound.isEmpty()) {
			// getBoundsZoomLevel returns ridiculously zoomed out values
			int zoom = searchMap.getBoundsZoomLevel(searchMapBound) + 9;
			zoom = ZOOM_LVL < zoom ? ZOOM_LVL : zoom;
			searchMap.setCenter(searchMapBound.getCenter(), zoom);
		}
		
		searchMapPanel.add(searchMap);
		searchMapPanel.setStyleName("results-map");
	}

	/**
	 * Parse search results and generate points to place markers on search
	 * results map
	 * 
	 * @param list
	 *            search results returned from server
	 */
	private void setPoints(List<ClientTreeData> list) {
		treeResults = list;
		listIndex = 0;
		markers.clear();
		getNextPoint(listIndex);
	}

	/**
	 * Helper method for setPoints. Makes an async call to geocode location
	 * 
	 * @param idx
	 *            index of next ClientTreeData to process in search results list
	 */
	private void getNextPoint(int idx) {
		if (idx >= treeResults.size()) {
			setSearchInfoMap();
			return;
		}

		ClientTreeData t = treeResults.get(idx);
		String loc = t.getLocation() + ", Vancouver, BC";
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

	/**
	 * Helper method for setPoints. Creates a Maker from a LatLng point and
	 * calls getNextPoint
	 * 
	 * @param pt
	 *            geocoded coordinates to place on map
	 */
	private void addPoint(LatLng pt) {
		MarkerOptions options = MarkerOptions.newInstance();
		options.setIcon(icon);
		options.setTitle(Integer.toString(++listIndex));
		Marker mark = new Marker(pt, options);
		mark.addMarkerClickHandler(new MarkerClickHandler() {
			public void onClick(MarkerClickEvent event) {
				clickMarker(event.getSender());
			}
		});
		markers.add(mark);

		getNextPoint(listIndex);
	}

	/**
	 * Display tree info window in map when marker is clicked
	 * 
	 * @param m
	 *            marker for tree location in search results map
	 */
	private void clickMarker(Marker m) {
		LatLng pt = m.getLatLng();
		try {
			int idx = Integer.parseInt(m.getTitle());
			ClientTreeData t = treeResults.get(idx - 1);
			searchMap.getInfoWindow().open(
					pt,
					new InfoWindowContent("<p>" + idx + ". "
							+ ParseUtils.capitalize(t.getCommonName(), false) + "<br/>"
							+ ParseUtils.capitalize(t.getLocation(), false) + "<br/>"
							+ pt.getLatitude() + ", " + pt.getLongitude()
							+ "</p>"));
		} catch (Exception e) {
			handleError(e);
		}
	}

}
