package com.cpsc310.treespotter.client;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class TreeSpotterClient {
	ClientTreeData addTree = null;
	Address geoAddr = null;
	TreeSpotter parent = null;
	LinkedHashMap<Label, TextBox> addFormMap = null;
	
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
	protected void populateAddData(ClientTreeData t, LinkedHashMap<Label, TextBox> form) 
			throws InvalidFieldException {
		addFormMap = form;
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
	private void sendAddTreeData(ClientTreeData t) {
		parent.treeDataService.addTree(t, new AsyncCallback<ClientTreeData>() {
			public void onFailure(Throwable error) {
				parent.handleError(error);
			}

			public void onSuccess(ClientTreeData result) {
				if (result != null) {
					Window.alert(HTMLResource.ADD_TREE_SUCCESS);
				} else {
					Window.alert(HTMLResource.ADD_TREE_FAIL);
				}
				parent.displayTreeInfoPage(result);
			}
		});
	}
	
	
}