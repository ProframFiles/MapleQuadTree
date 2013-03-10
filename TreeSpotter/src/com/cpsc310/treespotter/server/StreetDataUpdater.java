/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.googlecode.objectify.ObjectifyService;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * @author maple-quadtree
 *
 */


public class StreetDataUpdater extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(StreetDataUpdater.class.getName());
	private static final String JOB_NAME = "street data update job";
	
	StreetDataUpdater(){
		LOG.setLevel(Level.FINE);
		ObjectifyService.register(StreetDataUpdateJob.class);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response){
		Job job = ofy().load().type(Job.class).id(JOB_NAME).get();
		if(job == null){
			job = new StreetDataUpdateJob(JOB_NAME);
		}
		boolean has_more_work = job.run();
		if(has_more_work){
			queueThisTask();
		}
	}
	
	static public void queueThisTask(){
		QueueFactory.getDefaultQueue().add(withUrl("/treespotter/tasks/streetdataupdate"));
	}
}
