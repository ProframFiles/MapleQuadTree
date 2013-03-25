package com.cpsc310.treespotter.shared;

import java.io.Serializable;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Serialize;

@Entity
public class CSVFile implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id String email;
	String user;
	@Serialize String[] contents;
	
	public CSVFile() {}
	
	public CSVFile(String email, String user, String[] contents) {
		this.email = email;
		this.user = user;
		this.contents = contents;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getUser() {
		return user;
	}
	
	public String[] getContents() {
		return contents;
	}
	
	public void printContents() {
		System.out.println("User: " + user);
		System.out.println("Email: " + email);
		System.out.println("Contents");
		for (String s : contents) {
			System.out.println(s);
		}
	}
}
