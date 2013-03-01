package com.cpsc310.treespotter.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface HTMLResource extends ClientBundle {
	public static final HTMLResource INSTANCE = GWT.create(HTMLResource.class);

	@Source("/res/home.html")
	public TextResource getHomeHtml();
	
	@Source("/res/about.html")
	public TextResource getAboutHtml();
	
	@Source("/res/loadingbar.html")
	public TextResource getLoadingbar();
}
