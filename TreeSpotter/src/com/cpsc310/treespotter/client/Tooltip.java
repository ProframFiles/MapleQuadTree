package com.cpsc310.treespotter.client;

import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class Tooltip extends Composite implements MouseOverHandler, MouseOutHandler, MouseMoveHandler {
	PopupPanel panel;
	int left;
	int top;
	
	public Tooltip(Widget obj, String txt, int aleft, int atop) {
		left = aleft;
		top = atop;
		createTooltip(txt);
		obj.addHandler(this, MouseOverEvent.getType());
	}
	
	private void createTooltip(String txt) {
		panel = new PopupPanel();
		HTML lbl = new HTML(txt);
		lbl.setWordWrap(true);		
		panel.add(lbl);
		panel.setStyleName("tooltip");
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		panel.setPopupPosition(left, top);	
		panel.show();
	}	

	@Override
	public void onMouseOut(MouseOutEvent event) {
		panel.hide();
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		panel.show();		
	}
	
}
