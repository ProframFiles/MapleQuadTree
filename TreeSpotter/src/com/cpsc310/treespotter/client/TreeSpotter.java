package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.Date;

import com.cpsc310.treespotter.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
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
  private VerticalPanel loginPanel = new VerticalPanel();
  private Anchor signInLink = new Anchor("Sign In");
  private Anchor signOutLink = new Anchor("Sign Out");

  private final TreeDataServiceAsync treeDataService = GWT.create(TreeDataService.class);
  private final VerticalPanel infoMapPanel = new VerticalPanel();
  private Geocoder geo;
  private Label invalidLoc = new Label("Tree location could not be displayed");
  private static final int ZOOM_LVL = 12;

  
  
  /**
   * Entry point method.
   */
  public void onModuleLoad() {
      // Check login status using login service.
      LoginServiceAsync loginService = GWT.create(LoginService.class);
      loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
        public void onFailure(Throwable error) {
            handleError(error);

        }

        public void onSuccess(LoginInfo result) {
          loginInfo = result;
          if(loginInfo.isLoggedIn()) {
//            loadStockWatcher();
          } else {
//            loadLogin();
          }
        }
      });
      
      
      /* to see specific tree info page, use GET parameters
         eg. treespotter.appspot.com/viewTree.html?id=xxx
         uncomment the following to get the parameter and pass to server */
      /*String tid = Window.Location.getParameter("id");
      // how to determine user type to pass in? Shouldn't be via GET
      // dependent on how we keep track of user privileges
      // User class does not provide enough info, we may have to create
      // some kind of JDO wrapper class around it or add another field to
      // LoginInfo
      String user = "admin";
      getTreeInfo(tid, user);*/
      
      
      // TODO: add infoMapPanel to tree info page
      infoMapPanel.setSize("250px", "250px");
      

  }
  
  private void handleError(Throwable error) {
    Window.alert(error.getMessage());
    if (error instanceof NotLoggedInException) {
      Window.Location.replace(loginInfo.getLogoutUrl());
    }
  }
  
  private void doSearch() {
    
  }
  
  private void displaySearchResults(ArrayList<ClientTreeData> rlist) {
    
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
  
  private void displayTreeInfoPage(ClientTreeData t) {
    // uncomment this line to set infoMapPanel to display tree with ID
    // setTreeInfoMap(t); 
  }
  
  private void addUserTree() {
    // TODO: wait for UI form design for exact input field names
    String location; // street no + street addr
    String coords;  // possibly NULL
    String neighbourhood; // possibly enum
    int height; // possibly negative
    double diameter; // possibly negative
    int year; // year planted, possibly negative, possibly date
    String genus;
    String species;
    String common;
    String user = 
    
  }
  
  private void setTreeInfoMap(ClientTreeData data) {
    infoMapPanel.clear();
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
