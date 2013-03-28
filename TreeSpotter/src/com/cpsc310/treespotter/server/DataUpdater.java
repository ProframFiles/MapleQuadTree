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
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Work;

import static com.cpsc310.treespotter.server.OfyService.ofy;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

/**
 * @author maple-quadtree
 *
 */


public class DataUpdater extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger("Update task");
	public static final String TASK_URL = "/treespotter/tasks/fetchandprocessdata";
	private static final String JOB_NAME = "street data update job";
	
	public DataUpdater(){
	
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response){
		init();
		LOG.info("recieved StreetDataUpdater request, about to fetch job record...");
		String job_name = JOB_NAME;
		String[] job_name_option = request.getParameterValues("job");
		if(job_name_option != null && job_name_option.length >0 &&job_name_option[0] !=null){
			job_name = job_name_option[0];
		}
		Job job = getJob(job_name);
		job.setLogLevel(LOG.getLevel());
		String[] force = request.getParameterValues("force tasks");
		if(force != null && force.length>0){
			job.setOptions("force tasks", "true");
		}
		String[] tasks = request.getParameterValues("add task");
		if(tasks != null && tasks.length>0){
			for(String task: tasks){
				if (task!=null){
					job.setOptions("add task", task);
				}
			}
		}
		String[] file = request.getParameterValues("tree file");
		if(file != null && file.length >0 &&file[0] !=null){
			job.setOptions("tree file", file[0]);
		}
		LOG.fine("\n\trunning the job.");
		boolean has_more_work = job.run();
		LOG.fine("\n\tDone this run portion.");
		if(has_more_work){
			LOG.info("\n\tJob still has more work, re-queueing.");
			queueThisTask(job_name);
		}
		else{
			LOG.info("\n\tJob is done, not re-queueing.");
		}
	}
	
	public void init(){
		LOG.setLevel(Level.FINE);
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
	
	static private void queueThisTask(String job_name){
	    TaskOptions opt = withUrl(DataUpdater.TASK_URL);
	    opt = opt.param("job", job_name);
	    QueueFactory.getDefaultQueue().add(opt);
	}
}
