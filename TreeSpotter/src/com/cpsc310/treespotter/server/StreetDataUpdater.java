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
	private static Logger LOG = Logger.getLogger(StreetDataUpdater.class.getName());
	private static final String JOB_NAME = "street data update job";
	
	public StreetDataUpdater(){
	
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response){
		init();
		LOG.info("recieved StreetDataUpdater request, about to fetch job record...");
		Job job = ofy().load().type(Job.class).id(JOB_NAME).get();
		if(job == null){
			LOG.info("\n\tjob not found, creating a new job with id \"" + JOB_NAME + "\"");
			job = new StreetDataUpdateJob(JOB_NAME);
		}
		else{
			LOG.info("\n\tjob found: " + job.getJobID() + " with " + job.getNumTasks() + " remaining subtasks.");
		}
		job.setLogLevel(LOG.getLevel());
		LOG.fine("\n\trunning the job.");
		boolean has_more_work = job.run();
		LOG.fine("\n\tDone this run portion.");
		if(has_more_work){
			LOG.info("\n\tJob still has more work, re-queueing.");
			queueThisTask();
		}
		else{
			LOG.info("\n\tJob is done, not re-queueing.");
		}
	}
	
	public void init(){
		LOG.setLevel(Level.FINE);
		ObjectifyService.register(StreetDataUpdateJob.class);
		ObjectifyService.register(PersistentFile.class);
		ObjectifyService.register(ByteArrayEntity.class);
	}
	
	static public void queueThisTask(){
		QueueFactory.getDefaultQueue().add(withUrl("/treespotter/tasks/tasktest"));
	}
}
