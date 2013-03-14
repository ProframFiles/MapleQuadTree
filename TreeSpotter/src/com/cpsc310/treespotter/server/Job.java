/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.cpsc310.treespotter.shared.Util;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.DeadlineExceededException;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Serialize;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * @author maple-quadtree
 *
 */
@Entity
public abstract class Job {
	private static Logger LOG = Logger.getLogger(Job.class.getName());
	private static final double DAY_MS = 86400000.0;
	protected static int FILE_CHECK_PERIOD = 6;
	
	@Id private String id;
	@Load private Ref<PersistentFile> fileDataRef;
	@Serialize private ArrayList<SubTask> subTasks;
	@Ignore private String urlString;
	@Ignore private PersistentFile fileData;
	@Ignore private double meanSubTaskTime;
	@Ignore private int numSubTasksCompleted = 0;
	@Ignore long initialMillis = ApiProxy.getCurrentEnvironment().getRemainingMillis();
	public Job(){
		
	}
	
	public Job(String job_name){
		id = job_name;
	}
	
	public void setLogLevel(Level level){
		LOG.setLevel(level);
	}
	
	public int getNumTasks(){
		if(subTasks != null){
			return subTasks.size();
		}
		return 0;
	}
	
	abstract protected int processSubTask(InputStream is, SubTask st);
	abstract protected ArrayList<String> getFileUrls();
	abstract public String getJobID();
	
	public boolean run()
	{
		if(fileDataRef != null){
			ofy().load().ref(fileDataRef);
			fileData = fileDataRef.getValue();
			LOG.fine("\n\tfound existing file data.");
		}
		else{
			fileData = null;
			LOG.fine("\n\tdid not find existing file data.");
		}
		if(shouldFetchData()){
			ArrayList<byte[]> file_blobs = fetchFileData(getFileUrls());
			byte[] b = preProcessDataFiles(file_blobs);
			LOG.info("Done preprocessing files.\n\tPersisting " + b.length +" bytes to datastore");
			fileData.save(new ByteArrayInputStream(b));
			fileDataRef = Ref.create(fileData);
			LOG.fine("\n\tCreating subtasks");
			subTasks = createSubTasks(new ByteArrayInputStream(b));
			LOG.fine("\n\tPersisting job state");
			ofy().save().entity(this).now();
			return true;
		}
		// if we made it here, that means we already have up-to-date persisted data
		// so we retrieve it from the dataStore and start processing
		byte[] b = fileData.load();
		LOG.info("\n\tRetrieved " + b.length + " bytes of file data from the datastore");
		try{
			while(subTasks.size() > 0 && !needToStop()){
				InputStream is = new ByteArrayInputStream(b);
				SubTask st = subTasks.get(subTasks.size()-1);
				int progress;
				while((progress = processSubTask(is, st)) > 0){
					st.task_progress = progress;
					ofy().save().entity(this).now();
					LOG.info("\n\tSave task" + st.task_string + " with progress "+ st.task_progress);
					if(needToStop()){
						LOG.info("\n\tNeed to stop!");
						break;
					}
					is = new ByteArrayInputStream(b);
				}
				if(progress == 0){
					subTasks.remove(subTasks.size()-1);
				}
				ofy().save().entity(this).now();
			}
		}
		catch(DeadlineExceededException e){
			LOG.info("\n\tDeadline Exceeded!");
			ofy().save().entity(this).now();
		}
		if(subTasks.isEmpty()){
			return false;
		}
		
		return true;
	}

	abstract protected byte[] preProcessDataFiles(ArrayList<byte[]> b) ;

	private boolean needToStop(){
		long rem = ApiProxy.getCurrentEnvironment().getRemainingMillis();
		LOG.info("\n\t"+rem/1000.0+" remaining seconds!");
		return (rem < 60000 && initialMillis > 120000);
	}
	
	private ArrayList< byte[]> fetchFileData(ArrayList<String> urls){
		ArrayList<byte[]> file_blobs = new ArrayList<byte[]>();
		for(String file_url: urls){
			try {
				LOG.info("\n\tFetching: \"" + file_url + "\"");
				URL url = new URL(file_url);
				InputStream url_stream = url.openStream();
				byte[] b = Util.streamToByteArray(url_stream);
			    file_blobs.add(b);
			    LOG.fine("\n\tDone Fetching " + b.length + " bytes");
			} catch (MalformedURLException e) {
				throw new RuntimeException("Malformed url while getting job data\n\t" + e.getMessage(), e);
			} catch (IOException e) {
				throw new RuntimeException("IO error while opening job url stream\n\t" + e.getMessage(), e);
			}
		}
		return file_blobs;
	}
	
	private boolean shouldFetchData()
	{
		//the persisted file is not the one we have saved, or we don't have one saved
		if (fileData == null){
			fileData = new PersistentFile(getJobID());
			LOG.fine("\n\t no existing file data. Must download it.");
			return true;
		}
		
		Date now_time = new Date();
		double ms_passed = (now_time.getTime() - fileData.getTimeStamp().getTime());
		double days_passed = ms_passed/DAY_MS;
		boolean ret = days_passed > FILE_CHECK_PERIOD;
		if(ret){
			LOG.fine("\n\t" + days_passed +" days since last file fetch. Will download again.");
		}
		else{
			LOG.fine("\n\t" + days_passed +" days since last file fetch. Download not needed.");
		}
		return ret;
	}
	abstract public void setOptions(String option_name, String option_value);
	abstract protected ArrayList<SubTask> createSubTasks(InputStream is);
}
