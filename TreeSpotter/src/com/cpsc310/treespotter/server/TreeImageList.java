package com.cpsc310.treespotter.server;

import java.util.ArrayList;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Serialize;

@Entity
public class TreeImageList {

	@Id private String id;
	@Serialize private ArrayList<String> imageLinks;
	
	TreeImageList() {}
	
	public TreeImageList(String id) {
		this.id = id;
		imageLinks = new ArrayList<String>();
	}
	
	public ArrayList<String> getImageLinks() {
		return imageLinks;
	}
	
	public void addImageLink(String link) {
		if (link != null)
			imageLinks.add(link);
	}
}
