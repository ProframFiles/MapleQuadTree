package com.cpsc310.treespotter.client;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class Tooltip extends Composite implements MouseOverHandler, MouseOutHandler, MouseDownHandler {
	PopupPanel panel;
	Widget obj;
	String text;
	int left;
	int top;
	
	public Tooltip(Widget aobj, String txt, int aleft, int atop) {
		left = aleft;
		top = atop;
		obj = aobj;
		createTooltip(txt);
	}
	
	private void createTooltip(String txt) {
		panel = new PopupPanel();
		text = txt;
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
	
	
	public void setText(String newText) {
		text = newText;
		HTML lbl = new HTML(text);
		lbl.setWordWrap(true);		
		panel.add(lbl);
	}

	@Override
	// added to make sure tooltip closes if a dialog is open
	public void onMouseDown(MouseDownEvent event) {
		panel.hide();
	}	

	
}
