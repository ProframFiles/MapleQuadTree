/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.ArrayList;

import com.cpsc310.treespotter.shared.Util;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.DeadlineExceededException;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Unindex;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * @author maple-quadtree
 *
 */
@Entity
public abstract class Job {
	private static final double DAY_MS = 86400000.0;
	protected static int FILE_CHECK_PERIOD = 6;
	
	@Id private String id;
	@Unindex @Load private Ref<PersistentFile> fileDataRef;
	@Serialize private ArrayList<SubTask> subTasks;
	@Ignore private String urlString;
	@Ignore private PersistentFile fileData;
	@Ignore private double meanSubTaskTime;
	@Ignore private int numSubTasksCompleted = 0;
	public Job(){
		
	}
	
	public Job(String job_name, String fetch_url){
		id = job_name;
		urlString = fetch_url;
		ObjectifyService.register(this.getClass());
	}
	
	abstract protected int processSubTask(InputStream is, SubTask st);
	
	
	public boolean run()
	{
		if(fileDataRef != null){
			fileData = fileDataRef.getValue();
		}
		else{
			fileData = null;
		}
		if(shouldFetchFile()){
			byte[] b = fetchFileData();
			b = preProcessDataFile(b);
			fileData.save(new ByteArrayInputStream(b));
			fileDataRef = Ref.create(fileData);
			subTasks = createSubTasks(new ByteArrayInputStream(b));
			ofy().save().entity(this).now();
			return true;
		}
		// if we made it here, that means we already have up-to-date persisted data
		// so we retrieve it from the dataStore and start processing
		byte[] b = fileData.load();
		try{
			while(subTasks.size() > 0 && !needToStop()){
				InputStream is = new ByteArrayInputStream(b);
				SubTask st = subTasks.get(subTasks.size()-1);
				int progress;
				while((progress = processSubTask(is, st)) > 0 && !needToStop()){
					st.task_progress = progress;
					ofy().save().entity(this).now();
					is = new ByteArrayInputStream(b);
				}
				if(progress == 0){
					subTasks.remove(subTasks.size()-1);
				}
				ofy().save().entity(this).now();
			}
		}
		catch(DeadlineExceededException e){
			ofy().save().entity(this).now();
		}
		if(subTasks.isEmpty()){
			return false;
		}
		
		return true;
	}

	protected byte[] preProcessDataFile(byte[] b) {
		// TODO Auto-generated method stub
		return b;
	}

	private boolean needToStop(){
		long rem = ApiProxy.getCurrentEnvironment().getRemainingMillis();
		return rem < 30000;
	}
	
	private byte[] fetchFileData(){
		try {
			URL url = new URL(urlString);
			InputStream url_stream = url.openStream();
		    byte[] b = Util.streamToByteArray(url_stream);
			return b;
		} catch (MalformedURLException e) {
			throw new RuntimeException("Malformed url while getting job data\n\t" + e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException("IO error while opening job url stream\n\t" + e.getMessage(), e);
		}
	}
	
	private boolean shouldFetchFile()
	{
		//the persisted file is not the one we have saved, or we don't have one saved
		if (fileData == null || !fileData.getName().equals(urlString)){
			fileData = new PersistentFile(urlString);
			return true;
		}
		
		Date now_time = new Date();
		double ms_passed = (now_time.getTime() - fileData.getTimeStamp().getTime());
		double days_passed = ms_passed/DAY_MS;
		return days_passed > FILE_CHECK_PERIOD;
	}
	abstract public void setOptions(String option_name, String option_value);
	abstract protected ArrayList<SubTask> createSubTasks(InputStream is);
}
