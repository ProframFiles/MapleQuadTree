/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.io.Serializable;
import java.util.ArrayList;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Unindex;

/**
 * @author maple-quadtree
 *
 */
@Entity
public abstract class Job {
	@Id String id;
	@Unindex PersistentFile fileData;
	@Serialize public ArrayList<SubTask> subTasks;
	
	
	public Job(String job_name){
		id = job_name;
		SubTask st = new SubTask();
		subTasks.add(st);
	}
	
	abstract public boolean run();
	abstract public void setOptions(String option_name, String option_value);
	abstract protected void populateTasks();

	private class SubTask implements Serializable{
		private static final long serialVersionUID = -735284495675164L;
		//private String task_string;
		//private int task_progress;
	}
}
