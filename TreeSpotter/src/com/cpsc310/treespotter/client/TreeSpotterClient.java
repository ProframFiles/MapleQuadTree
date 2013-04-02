package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


import com.cpsc310.treespotter.shared.ISharedTreeData;
import com.cpsc310.treespotter.shared.TransmittedTreeData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;

public class TreeSpotterClient {
	private TransmittedTreeData addTree = null;
	private Address geoAddr = null;
	private TreeSpotter parent = null;
	private LinkedHashMap<Label, TextBox> addFormMap = null;
	
	public TreeSpotterClient(TreeSpotter parent) {
		this.parent = parent;
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
	protected void populateAddData(TransmittedTreeData t, LinkedHashMap<Label, TextBox> form) 
			throws InvalidFieldException {
		addFormMap = form;
		boolean parseLoc = true;
		if (t == null) {
			addTree = new TransmittedTreeData();
		} else {
			addTree = t;
			parseLoc = false;
		}

		for (Map.Entry<Label, TextBox> entry : addFormMap.entrySet()) {
			String key = entry.getKey().getText().split("\\s[*]")[0];
			String input = entry.getValue().getValue().trim();

			// this assumes valid location/coords in form
			// #### Street Name or #, #
			if (key.equalsIgnoreCase(TreeSpotter.LOCATION)) {
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
						parent.geo.getLocations(pt, new LocationCallback() {
							public void onFailure(int e) {
								parent.handleError(new InvalidFieldException(
										"Invalid field: Location"));
							}

							public void onSuccess(JsArray<Placemark> p) {
								if (p.length() <= 0) {
									parent.handleError(new InvalidFieldException(
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
										populateAddData(addTree, addFormMap);
									} catch (Exception e) {
										parent.handleError(e);
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
			} else if (key.equalsIgnoreCase(TreeSpotter.GENUS)) {
				addTree.setGenus(input);
			} else if (key.equalsIgnoreCase(TreeSpotter.SPECIES)) {
				addTree.setSpecies(input);
			} else if (key.equalsIgnoreCase(TreeSpotter.COMMON)) {
				addTree.setCommonName(input);
			} else if (key.equalsIgnoreCase(TreeSpotter.NEIGHBOUR) && !input.isEmpty()) {
				addTree.setNeighbourhood(input);
			} else if (key.equalsIgnoreCase(TreeSpotter.HEIGHT) && !input.isEmpty()) {
				try {
					// TODO: need a setHeight field
					// t.setHeight(Double.parseDouble(input));
					addTree.setHeightRange(ParseUtils.getHeightRange(input));
				} catch (Exception e) {
					throw new InvalidFieldException("Invalid field: Height");
				}
			} else if (key.equalsIgnoreCase(TreeSpotter.DIAMETER) && !input.isEmpty()) {
				try {
					addTree.setDiameter((int) Double.parseDouble(input));
				} catch (Exception e) {
					throw new InvalidFieldException("Invalid field: Diameter");
				}
			} else if (key.equalsIgnoreCase(TreeSpotter.PLANTED) && !input.isEmpty()) {
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
	private void sendAddTreeData(TransmittedTreeData t) {
		parent.treeDataService.addTree(t, new AsyncCallback<ISharedTreeData>() {
			public void onFailure(Throwable error) {
				parent.handleError(error);
			}

			public void onSuccess(ISharedTreeData result) {
				if (result != null) {
					Window.alert(HTMLResource.ADD_TREE_SUCCESS);
					parent.initSearchOracles();
				} else {
					Window.alert(HTMLResource.ADD_TREE_FAIL); 
				}
				parent.displayTreeInfoPage(new ClientTreeData(result));
			}
		});
	}

	
	/**
	 * export search results into csv
	 * @param rlist
	 */
	
	public void exportData(ArrayList<ClientTreeData> rlist) {
		
		StringBuilder stringBuilder = buildCSV(rlist);
		sendToServer(stringBuilder);
		
	}

	private StringBuilder buildCSV(ArrayList<ClientTreeData> rlist) {
		StringBuilder stringBuilder = new StringBuilder();
		
		// Columns
		stringBuilder.append("\"" + "Tree ID" + "\",");
		stringBuilder.append("\"" + TreeSpotter.LOCATION + "\",");
		stringBuilder.append("\"" + TreeSpotter.NEIGHBOUR + "\",");
		stringBuilder.append("\"" + TreeSpotter.GENUS + "\",");
		stringBuilder.append("\"" + TreeSpotter.SPECIES + "\",");
		stringBuilder.append("\"" + TreeSpotter.COMMON + "\",");
		stringBuilder.append("\"" + TreeSpotter.DIAMETER + "\",");
		stringBuilder.append("\"" + TreeSpotter.HEIGHT + "\",");
		stringBuilder.append("\"" + TreeSpotter.PLANTED + "\",");
		
		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		stringBuilder.append("/n");
		
		// Data
		for (int i=0; i<rlist.size(); i++) {
			ClientTreeData tree = rlist.get(i);
			stringBuilder.append("\"" + tree.getID() + "\",");
			stringBuilder.append("\"" + tree.getLocation() + "\",");
			stringBuilder.append("\"" + emptyOrValue(tree.getNeighbourhood()) + "\",");
			stringBuilder.append("\"" + tree.getGenus() + "\",");
			stringBuilder.append("\"" + tree.getSpecies() + "\",");
			stringBuilder.append("\"" + tree.getCommonName() + "\",");
			stringBuilder.append("\"" + convertToDiameter(tree.getDiameter()) + "\",");
			stringBuilder.append("\"" + convertToHeight(tree.getHeightRange()) + "\",");
			stringBuilder.append("\"" + emptyOrValue(tree.getPlanted()) + "\"");
			
			stringBuilder.append("/n");
		}
		return stringBuilder;
	}

	private void sendToServer(StringBuilder csvText) {
		
		FormPanel formPanel = new FormPanel();
		TextBox textBox = new TextBox();
		
		textBox.setName("textbox");
		textBox.setValue(csvText.toString());
		
		formPanel.setAction(GWT.getModuleBaseURL() + "export");
		formPanel.setMethod(FormPanel.METHOD_POST);
		formPanel.setWidget(textBox);
		
		formPanel.submit();
	}

	private String convertToHeight(int heightRange) {
		if (heightRange < 0) return "";
		if (0 <= heightRange && heightRange <= 10) {
			int low = heightRange * 10;
			int high = low + 10;
			return String.valueOf(low) + "-" + String.valueOf(high);
		}
		return "100+";
	}

	private String convertToDiameter(float diameter) {
		if (diameter < 0) return "";
		return String.valueOf(diameter);
	}

	private String emptyOrValue(String s) {
		if (s == null) return "";
		return s;
	}
	
	
}
