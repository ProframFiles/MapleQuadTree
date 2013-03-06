package com.cpsc310.treespotter.server;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class StreetDataUpdateJob extends Job {

	StreetDataUpdateJob(String job_name){
		super(job_name);
	}
	
	@Override
	public boolean run() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setOptions(String option_name, String option_value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void populateTasks() {
		// TODO Auto-generated method stub
		
	}
	
}
