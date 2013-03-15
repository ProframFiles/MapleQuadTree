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
import com.googlecode.objectify.Work;

import static com.cpsc310.treespotter.server.OfyService.ofy;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

/**
 * @author maple-quadtree
 *
 */


public class DataUpdater extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(DataUpdater.class.getName());
	private static final String JOB_NAME = "street data update job";
	
	public DataUpdater(){
	
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response){
		init();
		LOG.info("recieved StreetDataUpdater request, about to fetch job record...");
		Job job = getJob(JOB_NAME);
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
		ObjectifyService.register(DataUpdateJob.class);
		ObjectifyService.register(PersistentFile.class);
		ObjectifyService.register(ByteArrayEntity.class);
	}
	
	private static Job getJob(final String job_name){
		
		Job job = ofy().transact(new Work<Job>() {
		    public Job run() {
		    	Job in_job = ofy().load().type(Job.class).id(job_name).getValue();
				if(in_job == null){
					LOG.info("\n\tjob not found, creating a new job with id \"" + job_name + "\"");
					in_job = new DataUpdateJob(job_name);
					ofy().save().entity(in_job);
				}
				else{
					LOG.info("\n\tjob found: " + in_job.getJobID() + " with " + in_job.getNumTasks() + " remaining subtasks.");
				}
				return in_job;
		    }
		});
		return job;
	}
	
	static public void queueThisTask(){
		QueueFactory.getDefaultQueue().add(withUrl("/treespotter/tasks/fetchandprocessdata"));
	}
}
