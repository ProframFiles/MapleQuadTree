package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cpsc310.treespotter.shared.ISharedTreeData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Unit;
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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
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
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TreeSpotter implements EntryPoint {
	private LoginInfo loginInfo = null;
	protected static final String wikipediaSearchURL = "http://en.wikipedia.org/wiki/Special:Search/";

	// list of fields, used for the Add Tree Form and the Advanced Search
	protected static final String LOCATION = "Location";
	protected static final String GENUS = "Genus";
	protected static final String SPECIES = "Species";
	protected static final String COMMON = "Common Name";
	protected static final String NEIGHBOUR = "Neighbourhood";
	protected static final String HEIGHT = "Height";
	protected static final String DIAMETER = "Diameter";
	protected static final String PLANTED = "Date Planted";
	private String[] basicFields = { LOCATION, GENUS, SPECIES, COMMON };
	private String[] optionalFields = { NEIGHBOUR, HEIGHT, DIAMETER, PLANTED };
	private String[] advancedSearch = { LOCATION, GENUS, SPECIES, COMMON, NEIGHBOUR };

	private MultiWordSuggestOracle keywordOracle;
	private MultiWordSuggestOracle speciesOracle;
	private MultiWordSuggestOracle commonOracle;
	private MultiWordSuggestOracle neighbourOracle;
	private MultiWordSuggestOracle genusOracle;
	private MultiWordSuggestOracle addressOracle;
	
	private boolean isBasicSearch = true;
	private SuggestBox basicSearch = null;
	private VerticalPanel advancedForm = null;

	// list of the input boxes, so the values can be retrieved
	private LinkedHashMap<String, SuggestBox> advancedSearchMap = new LinkedHashMap<String, SuggestBox>();
	private LinkedHashMap<Label, TextBox> addFormMap = new LinkedHashMap<Label, TextBox>();

	protected final TreeDataServiceAsync treeDataService = GWT.create(TreeDataService.class);
	private TreeSpotterClient clientHelper = null;

	// variables for loading map view
	private final VerticalPanel searchMapPanel = new VerticalPanel();
	private final String SEARCH_MAP_SIZE = "600px";
	protected Geocoder geo = null;
	private static final int ZOOM_LVL = 15;
	private TreeSearchMap searchMap;
	private int listIndex;
	private int listOffset;
	private Icon icon;
	private Icon offPageIcon;
	private final String greenIconURL = "http://maps.gstatic.com/mapfiles/ridefinder-images/mm_20_green.png";
	private ArrayList<Marker> markers = new ArrayList<Marker>();
	private LatLng start;

	private List<ClientTreeData> treeResults = new ArrayList<ClientTreeData>();
	private LatLngBounds searchMapBound;
	
	private ClientTreeData displayTree;
	private TreeInfoPage treePage; 

	// for adding tree
	private Address geoAddr;
	private ClientTreeData addTree;

	// in order to be accessed by inner classes this has to be a member
	private ArrayList<ClientTreeData> treeList = new ArrayList<ClientTreeData>();

	// tab panel container for search results
	private TabPanel searchResultsPanel = null;
	private final int SEARCH_PAGE_SIZE = 25;
	
	// for uploading images
	private final String[] validImageExtns = {".gif", ".jpg", ".jpeg", ".png"};

	// is there a way to get a list of neighbourhoods from the dataset?
	// from the file names or do a batch query on everything (ugh)
	// private ArrayList<String> neighbourhoods = new ArrayList<String>(50);

	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {

			GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void onUncaughtException(Throwable e) {
						
						StringBuilder sb = new StringBuilder();
						StackTraceElement[] ste_array = e.getStackTrace();
						if(ste_array!=null){
							for(StackTraceElement ste: e.getStackTrace()){
								sb.append("\n" + ste.toString());
								if(ste.getClassName().contains("Tree")){
									break;
								}
							}
						}
						else{
							sb.append("No stackTrace available");
						}
						System.out.print("Unexpected exception in treespotter:\n\"" + e.getMessage() + "\"\n" + sb.toString() + "\n");
						Window.alert("Unexpected exception in treespotter:\n\t\"" + e.getMessage() + "\"\n" + sb.toString());
				}
			});
			
		
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
		
		clientHelper = new TreeSpotterClient(this);

		// Reminder: replace with API key once deployed
		// Load Google Maps API asynchronously
		Maps.loadMapsApi("", "2", true, new Runnable() {
			public void run() {
				System.out.println("Google Maps API loaded");
				geo = new Geocoder();
				icon = Icon.newInstance(greenIconURL);
				offPageIcon = Icon.newInstance("image/icon_blend.png");
				offPageIcon.setIconAnchor(Point.newInstance(32, 32));
				icon.setIconAnchor(Point.newInstance(6, 20));
				start = LatLng.newInstance(49.26102, -123.249339);
				searchMap = new TreeSearchMap();
				
				History.fireCurrentHistoryState();
			}
		});
		
		initFacebookAPI();
		initHomePage();
		initSearchOracles();
		initButtons();
		initLoginLogout();
		initAdminButton();
				
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) { 
				String historyToken = event.getValue(); 
				String role = ""; 
				
				if (loginInfo == null || !loginInfo.isAdmin()) {
					role = "user"; 
				} 
				else { 
					role = "admin"; 
				}
				  
				// TODO: not working yet, need to check getTreeInfo() 
				// Parse the history token 
				try { 
					if (!historyToken.isEmpty() && historyToken.substring(0, 4).equalsIgnoreCase("tree")) { 
						String treeId = historyToken.substring(4); 
						treeId = treeId.split("&", 2)[0];	// facebook redirects add &post
						System.out.println("Attempt display treeID: " + treeId);
						if (displayTree == null || !treeId.equals(displayTree.getID())) 
							getTreeInfo(treeId, role);
					} 
				} catch (Exception e) { 
					// what exception is returned if no tree matches ID?
					Window.alert("Tree ID is invalid. Please check your URL."); 
				} 
			} 
		});
		
		if (geo != null) { // Maps API loaded
			History.fireCurrentHistoryState();
		}
	
	}

	protected void handleError(Throwable error) {
		Window.alert(error.getMessage());
		if (error instanceof NotLoggedInException) {
			Window.Location.replace(loginInfo.getLogoutUrl());
		}
	}
	
	public String loggedInUser() {
		if (loginInfo != null && loginInfo.isLoggedIn())
			return loginInfo.getNickname();
		else
			return null;
	}

	private native String initFacebookAPI()
	/*-{
		$wnd.FB.init({
			'appId': "438492076225696", 
			'status': true, 
			'cookie': true, 
			'xfbml': true});
	}-*/;
	
	protected static native String initSocialMedia() 
	/*-{
		$wnd.FB.XFBML.parse();
	}-*/;
	
	/*
	 * add click handlers to buttons, search form
	 */
	private void initHomePage() {
		/* clear everything first */
		RootPanel searchDiv = RootPanel.get("main-search");
		searchDiv.clear();

		/* set up basic search elements */
		HorizontalPanel searchPanel = new HorizontalPanel();
		speciesOracle = new MultiWordSuggestOracle();
		final SuggestBox searchInput = new SuggestBox();
		Button searchBtn = new Button("Find my tree!");
		searchInput.setStyleName("main-search");
		searchBtn.setStyleName("main-search");
		
		searchPanel.add(searchInput);
		searchPanel.add(searchBtn);
		searchPanel.setStyleName("main-search-box");

		/* store search text box to retrieve search terms later */
		basicSearch = searchInput;

		/* set up advanced search elements */
		advancedForm = new VerticalPanel();
		advancedForm.setStyleName("main-search-fields");
		createAdvancedSearch();
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
					addSearchTooltips();
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

			for (Map.Entry<String, SuggestBox> entry : advancedSearchMap.entrySet()) {
				String key = entry.getKey();
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
			loadLoadingBar();
			treeDataService.searchTreeData(q,
					new AsyncCallback<ArrayList<ISharedTreeData>>() {
						@Override
						public void onFailure(Throwable error) {
							handleError(error);
						}

						@Override
						public void onSuccess(ArrayList<ISharedTreeData> result) {
							if (result == null) {
								RootPanel.get("content").clear();
								RootPanel.get("content").add(
										new Label("No results were found."));
							}
							if (result != null) {
								treeList = new ArrayList<ClientTreeData>();
								for (ISharedTreeData data : result) {
									//System.out.println(data.getCommonName());
									treeList.add(new ClientTreeData(data));
								}
								displaySearchResults(treeList, SEARCH_PAGE_SIZE);

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
	private void displaySearchResults(final ArrayList<ClientTreeData> rlist, final int page_size) {
		RootPanel content = RootPanel.get("content");
		content.clear();
		
		// TODO: Remove this after you are done
		Button exportButton = new Button("Export Results");
		content.add(exportButton);
		exportButton.addClickHandler(new ClickHandler() {
			public void onClick (ClickEvent event) {
				clientHelper.exportData(rlist);
			}
		});
		

		if (rlist.isEmpty()) {
			Label noResults = new Label("No results were found.");
			content.add(noResults);

		}  else {
			searchResultsPanel = new TabPanel();
			int first_page_size = Math.min(rlist.size(), page_size-1);
			FlexTable resultsTable = createSearchPage(0, rlist.subList(0, first_page_size));
			searchResultsPanel.add(resultsTable, "1");
			
			content.add(searchMapPanel);
			searchMapPanel.clear();
			searchMapPanel.add(searchMap.getMap());
			searchMapPanel.setStyleName("results-map");
			searchMap.newSearchResults(rlist);
			content.add(searchResultsPanel);
			// set map points to the same set
			SearchTabSelectionHandler handler = new SearchTabSelectionHandler(rlist);
			searchResultsPanel.addSelectionHandler(handler);
			searchResultsPanel.setVisible(true);
			searchResultsPanel.selectTab(0);
			
			
		
		}
	}
	class SearchTabSelectionHandler implements SelectionHandler<Integer> {
		ArrayList<ClientTreeData> trees =null;
		boolean done_the_rest = false;
		SearchTabSelectionHandler(ArrayList<ClientTreeData> trees){
			this.trees = trees;
		}
		
		public void onSelection(SelectionEvent<Integer> event) {
			searchMap.onTabPageChanged(event.getSelectedItem());
			if(!done_the_rest){
				doTheRest();
				done_the_rest=true;
			}
		}
		private void doTheRest(){
			for (int i = SEARCH_PAGE_SIZE; i < trees.size(); i = i + SEARCH_PAGE_SIZE) {
				int end = Math.min(trees.size(), i + SEARCH_PAGE_SIZE-1);
				FlexTable resultTable = createSearchPage(i, trees.subList(i, end));
				searchResultsPanel.add(resultTable, Integer.toString((i / SEARCH_PAGE_SIZE) + 1));
				System.out.println("Flex table result page: " + i);
			}
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
				new AsyncCallback<ISharedTreeData>() {
					public void onFailure(Throwable error) {
						handleError(error);
					}

					public void onSuccess(ISharedTreeData data) {
						displayTreeInfoPage(new ClientTreeData(data));
					}
				});
	}

	/**
	 * Replaces content panel with details from the given ClientTreeData
	 * 
	 * @param t
	 *            ClientTreeData to display details of
	 */
	protected void displayTreeInfoPage(ClientTreeData t) {		
		if (t == null || t.dataIsNull()) {
			Window.alert("Tree not found for display");
			return;
		}
		
		// get the regular tree info page if not logged in
		boolean isLoggedIn = loginInfo != null && loginInfo.isLoggedIn();
		treePage = isLoggedIn ? new LoggedInTreeInfoPage(this, t) : new RegularTreeInfoPage(this, t); 
		
		// create tab panel
		TabPanel tabs = new TabPanel();
		tabs.add(treePage, "Details");
		createGalleryPage(tabs, t);
		//tabs.add(createGalleryPage(t), "Gallery");
		tabs.selectTab(0);
		tabs.setWidth("100%");
		tabs.getTabBar().setStyleName("tree-info-tab-bar");
		// TODO: image gallery class
		
		RootPanel.get("content").clear();
		RootPanel.get("content").add(tabs);
		
		// need the widget to be placed on the page before adding tooltips
		if (!isLoggedIn) {
			((RegularTreeInfoPage) treePage).addImageTooltips();
		}
		displayTree = t;
		
		// TODO: enable history token when getTreeData implemented on server
		History.newItem("tree" + t.getID());
		// triggers value changed
	}
	
	private void createGalleryPage(TabPanel tabs, ClientTreeData t) {
		
		HorizontalPanel panel = new HorizontalPanel();
		final FormPanel form = new FormPanel();
		
		form.setAction(GWT.getModuleBaseURL() + "uploadImage");
		form.setMethod(FormPanel.METHOD_POST);
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setWidget(panel);
		
		Label selectLabel = new Label("Upload an image");
		
		final FileUpload fileUpload = new FileUpload();
		fileUpload.setName("image");
		
		final Button uploadButton = new Button();
		uploadButton.setText("Loading...");
		uploadButton.setEnabled(false);
		
		final TextBox treeInfo = new TextBox();
		treeInfo.setVisible(false);
		treeInfo.setName(t.getID());
		
		panel.add(treeInfo);
		panel.add(selectLabel);
		panel.add(fileUpload);
		panel.add(uploadButton);
		
		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				Window.alert("Upload Complete");
				form.reset();
				startNewBlobstoreSession(form, treeInfo, uploadButton, fileUpload);
			}
		});
		
		tabs.add(panel, "Gallery");
		
		startNewBlobstoreSession(form, treeInfo, uploadButton, fileUpload);
	}
	
	private void startNewBlobstoreSession(final FormPanel form, final TextBox treeInfo, 
			final Button uploadButton, final FileUpload fileUpload) {
		treeDataService.getBlobstoreUploadUrl(new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				System.out.println(caught.getStackTrace());
			}

			@Override
			public void onSuccess(String result) {
				treeInfo.setText(result);
				uploadButton.setText("Upload");
				uploadButton.setEnabled(true);
				
				uploadButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						String fileName = fileUpload.getFilename();				
						if (fileName.length() == 0) {
							Window.alert("No file specified");
							return;
						}
						String extn = fileName.substring(fileName.lastIndexOf('.'), fileName.length());
						if (!isImageExtn(extn)) {
							Window.alert("Not an image");
							return;
						}
						else {
							form.submit();
						}
					}
				});
			}
		});
	}
	
	private boolean isImageExtn(String ext) {
		
		for (String extn : validImageExtns) {
			if (ext.equals(extn)) 
				return true;
		}
		
		return false;
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
						} else if (name.equalsIgnoreCase(PLANTED)) {
							try {
								ParseUtils.formatDate(input);
							} catch (InvalidFieldException e) {
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
						clientHelper.populateAddData(null, addFormMap);
					} catch (Exception e) {
						handleError(e);
					}

				} else {
					String errorMsg = "";
					for (String fld : invalidFields) {
						if (fld.equalsIgnoreCase(LOCATION)) {
							errorMsg = errorMsg
									+ "Location must be a valid address or coordinates.\n";
						} else if (fld.equalsIgnoreCase(HEIGHT)) {
							errorMsg = errorMsg + "Height must be a number.\n";
						} else if (fld.equalsIgnoreCase(DIAMETER)) {
							errorMsg = errorMsg
									+ "Diameter must be a number.\n";
						} else if (fld.equalsIgnoreCase(PLANTED)){
							errorMsg = errorMsg + "Date Planted must be in the correct format.\n";
						} else {
							String trimmed = fld.contains("*") ? fld.substring(0, fld.indexOf("*")) : fld;
							errorMsg = errorMsg + trimmed + " cannot be empty.\n";
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
		
		addForm.add(addTreesCSV());
		
		addPanel.add(addForm);
		addPanel.setWidth("350px");
		addPanel.setStyleName("add-popup");
		addPanel.center();
		addFormTooltips();
	}
	
	
	/**
	 * Helper to create the upload CSV part for add-tree form
	 * @return FormPanel with upload functionality
	 */
	
	private FormPanel addTreesCSV() {
		HorizontalPanel panel = new HorizontalPanel();
		final FormPanel form = new FormPanel();

		form.setAction(GWT.getModuleBaseURL() + "importCSV");
		form.setMethod(FormPanel.METHOD_POST);
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setWidget(panel);
		
		// FileUpload widget
		final FileUpload fileUpload = new FileUpload();
		fileUpload.setName("upload");
		
		// Label
		Label selectLabel = new Label("Select a CSV file:");
		
		// Submit button
		Button uploadButton = new Button("Upload");
		uploadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String fileName = fileUpload.getFilename();				
				if (fileName.length() == 0) {
					Window.alert("No file specified!");
					return;
				}
				String extn = fileName.substring(fileName.lastIndexOf('.'), fileName.length());
				if (!extn.equals(".csv")) {
					Window.alert("Not an CSV file!");
				}
				else {
					form.submit();
				}
			}
		});
		
		// TextBox for user information
		TextBox userInfo = new TextBox();
		userInfo.setVisible(false);
		String user = loginInfo.getNickname();
		if (user == null) {
			userInfo.setName("Test"); 
			userInfo.setText("Test");
		}
		else {
			userInfo.setName(user);
			userInfo.setText(loginInfo.getEmailAddress());
		}
		
		// Adding all widgets to panel
		panel.add(userInfo);
		panel.add(selectLabel);
		panel.add(fileUpload);
		panel.add(uploadButton);
		
		
		form.setWidth("100%");
		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				Window.alert(event.getResults());				
			}
		});
		return form;
		
	}

	private void addComment() {
		// TODO: implement
	}
	
	/**
	 * Helper function for initHomePage() Creates a panel with a text box for an
	 * advanced search field
	 * 
	 * @param text
	 *            String for the text box label
	 * @return Panel containing one row for the advanced search panel
	 */
	private void createAdvancedSearch() {
		for (String fld : advancedSearch) {
			HorizontalPanel panel = new HorizontalPanel();
			final SuggestBox tb = new SuggestBox();
			tb.setStyleName("disabled");

			HTML label = new HTML(fld);
			label.setStyleName("advanced-search-panel");

			// textbox will be enabled when clicked
			// disabled if empty when clicking away
			tb.getValueBox().addFocusHandler(new FocusHandler() {
				public void onFocus(FocusEvent event) {
					tb.setStyleName("enabled");
				}
			});

			tb.getValueBox().addBlurHandler(new BlurHandler() {
				@Override
				public void onBlur(BlurEvent event) {
					if (tb.getValue().trim().equals("")) {
						tb.setStyleName("disabled");
					}
				}

			});

			/* add text box to the list */
			advancedSearchMap.put(fld, tb);

			/* add all elements to the panel */
			panel.add(label);
			panel.add(tb);
			advancedForm.add(panel);
		}
	}

	private void initSearchOracles() {
		// Keyword Search
		treeDataService.getSearchSuggestions(SearchFieldID.KEYWORD, "", new AsyncCallback<ArrayList<String>>() {
			@Override
			public void onFailure(Throwable caught) {
				System.out.println("Failure initiating keyword search.");
				System.out.println(caught.toString());
				handleError(caught);
				initAdressOracle();
			}
			@Override
			public void onSuccess(ArrayList<String> result) {
				keywordOracle = (MultiWordSuggestOracle) basicSearch.getSuggestOracle();				
				keywordOracle.addAll(result);
				System.out.println("Finished initiating keyword search.");
				System.out.println(result.toString());
				initAdressOracle();
			}			
		});
	}
	private void initAdressOracle(){
		// Address Search
		treeDataService.getSearchSuggestions(SearchFieldID.ADDRESS, "", new AsyncCallback<ArrayList<String>>() {
			@Override
			public void onFailure(Throwable caught) {
				handleError(caught);
				initCommonNameOracle();
			}
			@Override
			public void onSuccess(ArrayList<String> result) {
				addressOracle = (MultiWordSuggestOracle) advancedSearchMap.get(LOCATION).getSuggestOracle();				
				addressOracle.addAll(result);
				initCommonNameOracle();
			}	
		});
	}
	private void initCommonNameOracle(){
		// Common Name Search
		treeDataService.getSearchSuggestions(SearchFieldID.COMMON, "", new AsyncCallback<ArrayList<String>>() {
			@Override
			public void onFailure(Throwable caught) {
				handleError(caught);
				initGenusOracle();
			}
			@Override
			public void onSuccess(ArrayList<String> result) {
				commonOracle = (MultiWordSuggestOracle) advancedSearchMap.get(COMMON).getSuggestOracle();				
				commonOracle.addAll(result);
				initGenusOracle();
			}	
		});
	}
	private void initGenusOracle(){
		// Genus Search
		treeDataService.getSearchSuggestions(SearchFieldID.GENUS, "", new AsyncCallback<ArrayList<String>>() {
			@Override
			public void onFailure(Throwable caught) {
				handleError(caught);
				initSpeciesOracle();
			}
			@Override
			public void onSuccess(ArrayList<String> result) {
				genusOracle = (MultiWordSuggestOracle) advancedSearchMap.get(GENUS).getSuggestOracle();				
				genusOracle.addAll(result);
				initSpeciesOracle();
			}	
		});
	}
	private void initSpeciesOracle(){
		// Species Search
		treeDataService.getSearchSuggestions(SearchFieldID.SPECIES, "", new AsyncCallback<ArrayList<String>>() {
			@Override
			public void onFailure(Throwable caught) {
				handleError(caught);
				initNeighbourhoodOracle();
			}
			@Override
			public void onSuccess(ArrayList<String> result) {
				speciesOracle = (MultiWordSuggestOracle) advancedSearchMap.get(SPECIES).getSuggestOracle();				
				speciesOracle.addAll(result);
				initNeighbourhoodOracle();
			}	
		});
	}
	private void initNeighbourhoodOracle(){
		// Neighbourhood Search
		treeDataService.getSearchSuggestions(SearchFieldID.NEIGHBOUR, "", new AsyncCallback<ArrayList<String>>() {
			@Override
			public void onFailure(Throwable caught) {
				handleError(caught);
			}
			@Override
			public void onSuccess(ArrayList<String> result) {
				neighbourOracle = (MultiWordSuggestOracle) advancedSearchMap.get(NEIGHBOUR).getSuggestOracle();				
				neighbourOracle.addAll(result);
				System.out.println("Finished initiating neighbourhood search.");
				System.out.println(result.toString());
			}	
		});
	}
	
	private void initLoginLogout() {
		final Anchor loginLink = Anchor.wrap(Document.get().getElementById(
				"login-link"));
		
		if (loginInfo != null && loginInfo.isLoggedIn()) {
			loginLink.setText("Log out");
			loginLink.setHref(loginInfo.getLogoutUrl());
		}
		
		
		LoginServiceAsync loginService = GWT.create(LoginService.class);
		loginService.login(GWT.getHostPageBaseURL(),
				new AsyncCallback<LoginInfo>() {
					public void onFailure(Throwable error) {
						handleError(error);
					}

					public void onSuccess(LoginInfo result) {
						loginInfo = result;
						if (loginInfo.isLoggedIn()) {
							// log in was successful, set the log out link
							loginLink.setText("Log out");
							loginLink.setHref(loginInfo.getLogoutUrl());
							
							// check if the user is an admin
							initAdminButton();
						} else {
							loginLink.setHref(loginInfo.getLoginUrl());							
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
				displaySearchResults(treeList, SEARCH_PAGE_SIZE);
			}
		});

		/* wrap add tree button with click handler */
		Button addButton = Button.wrap(Document.get().getElementById(
				"add-tree-button"));
		
		addTooltip(
				addButton,
				HTMLResource.ADD_TREE_BUTTON_TOOLTIP,
				addButton.getAbsoluteLeft(), 
				addButton.getAbsoluteTop() + addButton.getOffsetHeight() + 10);
		
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addUserTree();
			}
		});
	}
	
	private void initAdminButton() {
		if (loginInfo != null && loginInfo.isAdmin()) {
			RootPanel top = RootPanel.get("top");
			Button adminBtn = new Button("Admin");
			adminBtn.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					loadAdminPage();
				}			
			});			
			top.add(adminBtn);
		}
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

	private void loadAdminPage() {
		// put another check, since possible to force button to show
		AdminButtonPage.load(loginInfo, treeDataService);
	}

	private void loadLoadingBar() {
		HTMLPanel htmlPanel = new HTMLPanel(HTMLResource.INSTANCE.getLoadingBar()
				.getText());
		RootPanel.get("content").clear();
		RootPanel.get("content").add(htmlPanel);	
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
	 * Adds a tooltip to the given object
	 * @param obj Widget to hover over
	 * @param txt Text to display in the tooltip
	 * @param left position from left
	 * @param top position from top
	 */
	private void addTooltip(Widget obj, String txt, int left, int top) {
		Tooltip tip = new Tooltip(obj, txt, left, top);
		((FocusWidget) obj).addMouseOverHandler(tip);
		((FocusWidget) obj).addMouseOutHandler(tip);
	}
	
	// different one for suggest boxes
	private void addTooltip(SuggestBox obj, String txt, int left, int top) {
		Tooltip tip = new Tooltip(obj, txt, left, top);
		obj.getValueBox().addMouseOverHandler(tip);
		obj.getValueBox().addMouseOutHandler(tip);
	}
	
	private void addFormTooltips() {
		for (Map.Entry<Label, TextBox> entry : addFormMap.entrySet()) {
			TextBox tb = entry.getValue();
			String key = entry.getKey().getText().split("\\s[*]")[0];
			int top = tb.getAbsoluteTop();
			int left = tb.getAbsoluteLeft() + tb.getOffsetWidth() + 30;
			String text = null;
			
			if (key.equalsIgnoreCase(LOCATION)) {
				text = HTMLResource.ADD_LOCATION_TOOLTIP;
			} else if (key.equalsIgnoreCase(GENUS)) {
				text = HTMLResource.ADD_GENUS_TOOLTIP;
			} else if (key.equalsIgnoreCase(SPECIES)) { 
				text = HTMLResource.ADD_SPECIES_TOOLTIP;
			} else if (key.equalsIgnoreCase(COMMON)) { 
				text = HTMLResource.ADD_COMMON_TOOLTIP;
			} else if (key.equalsIgnoreCase(NEIGHBOUR)) { 
				text = HTMLResource.ADD_NEIGHBOURHOOD_TOOLTIP;
			} else if (key.equalsIgnoreCase(HEIGHT)) { 
				text = HTMLResource.ADD_HEIGHT_TOOLTIP;
			} else if (key.equalsIgnoreCase(DIAMETER)) {
				text = HTMLResource.ADD_DIAMETER_TOOLTIP;
			} else if (key.equalsIgnoreCase(PLANTED)) {
				text = HTMLResource.ADD_PLANTED_TOOLTIP;
			} 

			if (text != "") {
				addTooltip(tb, text, left, top);
			}		
		}
	}
	
	private void addSearchTooltips() {
		for (Map.Entry<String, SuggestBox> entry : advancedSearchMap.entrySet()) {
			SuggestBox tb = entry.getValue();
			String key = entry.getKey().split("\\s[*]")[0];
			int top = tb.getAbsoluteTop();
			int left = tb.getAbsoluteLeft() + tb.getOffsetWidth() + 10;
			String text = null;
			
			if (key.equalsIgnoreCase(LOCATION)) {
				text = HTMLResource.SEARCH_LOCATION_TOOLTIP;
			} else if (key.equalsIgnoreCase(GENUS)) {
				text = HTMLResource.SEARCH_GENUS_TOOLTIP;
			} else if (key.equalsIgnoreCase(SPECIES)) { 
				text = HTMLResource.SEARCH_SPECIES_TOOLTIP;
			} else if (key.equalsIgnoreCase(COMMON)) { 
				text = HTMLResource.SEARCH_COMMON_TOOLTIP;
			} else if (key.equalsIgnoreCase(NEIGHBOUR)) { 
				text = HTMLResource.SEARCH_NEIGHBOURHOOD_TOOLTIP;
			}
			
			if (text != "") {
				addTooltip(tb, text, left, top);
			}	
		}
	}
	
}
