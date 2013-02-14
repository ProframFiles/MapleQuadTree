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
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TreeSpotter implements EntryPoint {

  private LoginInfo loginInfo = null;
  private VerticalPanel loginPanel = new VerticalPanel();
  private Anchor signInLink = new Anchor("Sign In");
  private Anchor signOutLink = new Anchor("Sign Out");

  
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

  }
  
  private void handleError(Throwable error) {
    Window.alert(error.getMessage());
    if (error instanceof NotLoggedInException) {
      Window.Location.replace(loginInfo.getLogoutUrl());
    }
  }

}
