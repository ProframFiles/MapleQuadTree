/**
 * 
 */
package com.cpsc310.treespotter.server;

import static com.cpsc310.treespotter.server.OfyService.ofy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Result;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Unindex;


/**
 * @author maple-quadtree
 * This class represents a File that comes from an external Source, 
 * And is persisted into the google datastore
 *
 */
@Entity
public class PersistentFile {
	@Id private  String name;
	@Unindex private ArrayList<Ref<ByteArrayEntity>> chunkIds;
	@Unindex private int numBytes;
	@Unindex private long checksum;
	@Unindex private Date dateStamp;
	@Ignore private ArrayList<Result<Key<ByteArrayEntity>>> asyncSaveResults;
	
	// objectify needs a no-arg constructor
	@SuppressWarnings("unused")
	private PersistentFile(){
		
	}
	
	public PersistentFile(String file_name){
		name = file_name;
		chunkIds = new ArrayList<Ref<ByteArrayEntity>>();
		ObjectifyService.register(this.getClass());
		
		//there must be a better way to get this class name...
		ByteArrayEntity temp_blob = new ByteArrayEntity("");
		ObjectifyService.register(temp_blob.getClass());
	}
	public void saveAsync(InputStream source){
		asyncSaveResults = saveImpl(source);
	}
	public  Result<Key<PersistentFile>> completeSave(){
		for(Result<Key<ByteArrayEntity>> r: asyncSaveResults){
			
			chunkIds.add(Ref.create(r.now()));
		}
		Result<Key<PersistentFile>>  r=ofy().save().entity(this);
		asyncSaveResults = null;
		return r;
	}
	
	private ArrayList<Result<Key<ByteArrayEntity>>> saveImpl(InputStream source){
		CRC32 check_crc = new CRC32();
		InputStream in_stream = new CheckedInputStream(source, check_crc);
		String blob_id = name;
		int blob_index = 0;
		ByteArrayEntity blob = new ByteArrayEntity(blob_id+blob_index);
		numBytes = 0;
		int written = 0;
		ArrayList<Result<Key<ByteArrayEntity>>> results = new ArrayList<Result<Key<ByteArrayEntity>>>();
		try {
			
			written = blob.copyBytes(in_stream);
			while(written > 0){
				// persist this chunk of the file
				results.add(ofy().save().entity(blob));
				// save a reference to the saved chunk
				
				// that chunks done, update numBytes
				numBytes += written; 
				
				// create a new chunk and try to copy bytes to it
				blob_index++;
				blob = new ByteArrayEntity(blob_id+blob_index);
				written = blob.copyBytes(in_stream);
			}
			
			dateStamp = new Date();
			checksum = check_crc.getValue();
			
		} catch (IOException e) {
			throw new RuntimeException("IOError while trying to persist file " + name + " from stream:\n\t" + e.getMessage(), e);
		}
		return results;
	}
	
	public void save(InputStream source){
		asyncSaveResults = saveImpl(source);
		completeSave().now();
	}
	
	public byte[] load(){
		if(dateStamp == null){
			throw new RuntimeException("Attempting to unpersist file " + name + " but it hasn't been persisted" );
		}
		ofy().load().refs(chunkIds);
		ByteArrayOutputStream retrieved_bytes = new ByteArrayOutputStream(numBytes);
		CRC32 retrieved_crc = new CRC32();
		CheckedOutputStream byte_wrapper = new CheckedOutputStream(retrieved_bytes, retrieved_crc);
		int current_index = 0;
		long retrieved_long = 0L;
		try {
			for( final Ref<ByteArrayEntity> chunk_ref: chunkIds){
				ByteArrayEntity blob =  chunk_ref.safeGet();
				byte_wrapper.write(blob.getBytes());
				current_index += blob.getBytes().length;
			}
			byte_wrapper.flush();
			retrieved_long = byte_wrapper.getChecksum().getValue();
		} catch (IOException e) {
			throw new RuntimeException("IOError while trying to unpersist file " + name + " from datastore:\n\t" + e.getMessage(), e);
		}
		// all done, check the crc and length
		if(checksum != retrieved_long || current_index != numBytes){
			throw new RuntimeException("CRC32 mismatch: Attempted to unpersist file " + name + " but it came back... wrong." );
		}
		
		return retrieved_bytes.toByteArray();
		
	}
	
	public Date getTimeStamp(){
		return dateStamp;
	}
	
	public String getName(){
		return name;
	}
	
}
