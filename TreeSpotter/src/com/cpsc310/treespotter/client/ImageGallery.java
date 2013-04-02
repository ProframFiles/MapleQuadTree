package com.cpsc310.treespotter.client;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;

public class ImageGallery extends Composite {
	
	// for uploading images
	private final String[] validImageExtns = {".gif", ".jpg", ".jpeg", ".png"};
	
	private ClientTreeData tree;
	private TreeDataServiceAsync treeDataService;
	
	private VerticalPanel mainPanel;
	private HorizontalPanel uploadPanel;
	private FlowPanel galleryPanel;
	
	
	public ImageGallery(ClientTreeData tree, boolean isLoggedIn, final TreeDataServiceAsync treeDataService) {
		this.tree = tree;
		this.treeDataService = treeDataService;
		
		mainPanel = new VerticalPanel();
		uploadPanel = new HorizontalPanel();
		galleryPanel = new FlowPanel();
		
		// only allow image uploads if user is logged in
		if (isLoggedIn)
			addGalleryUpload(tree);
		addGalleryImages();
		
		initWidget(mainPanel);	
	}
	
	private void addGalleryImages() {
		galleryPanel.clear();
		treeDataService.getTreeImages(tree.getID(), new AsyncCallback<ArrayList<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();				
			}

			@Override
			public void onSuccess(ArrayList<String> result) {
				System.out.println("Image URL:");
				for (String link : result) {
					System.out.println(link);
					galleryPanel.add(createImage(link));
				}
			}
		});
		mainPanel.add(galleryPanel);
	}
	
	
	protected Image createImage(String link) {
		// create the thumbnail
		Image img = new Image();
		img.setUrlAndVisibleRect(link, 0, 0, 200, 200);
		img.setStyleName("tree-image");
		
		// create dialog box for showing full size image
		final DialogBox dialog = new DialogBox();
		dialog.setStyleName("image-dialog");
		VerticalPanel panel = new VerticalPanel();
		
		Button btn = new Button("Close");
		btn.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				dialog.hide();	
			}
		});
		
		panel.add(new Image(link));
		panel.add(btn);
		dialog.add(panel);
		dialog.setAnimationEnabled(true);
		dialog.setGlassEnabled(true); 
	    dialog.setAutoHideEnabled(true); 
		
		// add click handler to the thumbnail
		img.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				dialog.center();		
			}
			
		});	
		return img;
	}

	private void addGalleryUpload(ClientTreeData t) {
		final FormPanel form = new FormPanel();
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);
		form.setWidget(uploadPanel);
		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				Window.alert("Upload Complete");
				form.reset();
				addGalleryImages();
			}
		});
		
		final FileUpload fileUpload = new FileUpload();
		fileUpload.setName("image");
		
		final Button uploadButton = new Button();
		uploadButton.setText("Upload");
			
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
					treeDataService.getBlobstoreUploadUrl(new AsyncCallback<String>() {
	
						@Override
						public void onFailure(Throwable caught) {
							System.out.println(Arrays.toString(caught.getStackTrace()));
						}
	
						@Override
						public void onSuccess(String result) {
							form.setAction(result);
							form.submit();
							form.reset();
						}
					});	
				}
			}
		});
		final TextBox treeInfo = new TextBox();
		treeInfo.setName(t.getID());
		treeInfo.setVisible(false);
		
		uploadPanel.add(treeInfo);
		uploadPanel.add(new HTML("Select an image to upload: "));
		uploadPanel.add(fileUpload);
		uploadPanel.add(uploadButton);
		uploadPanel.setSpacing(10);
		mainPanel.add(form);
	}
	
	private boolean isImageExtn(String ext) {
		for (String extn : validImageExtns) {
			if (ext.equals(extn)) 
				return true;
		}
		return false;
	}
	
}
