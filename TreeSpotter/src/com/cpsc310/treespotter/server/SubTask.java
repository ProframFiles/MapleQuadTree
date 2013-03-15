package com.cpsc310.treespotter.server;

import java.io.Serializable;

class SubTask implements Serializable{
	SubTask(){
		
	}
	SubTask(String name){
		progress = 0;
		this.name = name;
	}
	
	private static final long serialVersionUID = 1L;
	public String name;
	public int progress;
}