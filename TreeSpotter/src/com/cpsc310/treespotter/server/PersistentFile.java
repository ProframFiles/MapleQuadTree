/**
 * 
 */
package com.cpsc310.treespotter.server;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;


/**
 * @author maple-quadtree
 * This class represents a File that comes from an external Source, 
 * And is persisted into the google datastore
 *
 */
@Entity
public class PersistentFile {
	@Id String name;
	@Unindex ArrayList<Ref<ByteArrayEntity<byte[]>>> chunkIds;
	@Unindex int numBytes;
	@Unindex CRC32 checksum;
	@Unindex Date dateStamp;

	public PersistentFile(String file_name){
		name = file_name;
		ObjectifyService.register(this.getClass());
		
		//there must be a better way to get this class name...
		ByteArrayEntity<byte[]> temp_blob = new ByteArrayEntity<byte[]>("");
		ObjectifyService.register(temp_blob.getClass());
	}
	
	public void save(InputStream source){
		checksum = new CRC32();
		InputStream in_stream = new CheckedInputStream(source, checksum);
		String blob_id = name;
		int blob_index = 0;
		ByteArrayEntity<byte[]> blob = new ByteArrayEntity<byte[]>(blob_id+blob_index);
		numBytes = 0;
		int written = 0;
		
		try {
			written = blob.copyBytes(in_stream);
			while(written > 0){
				// persist this chunk of the file
				ofy().save().entity(blob).now();
				// save a reference to the saved chunk
				chunkIds.add(Ref.create(blob));
				// that chunks done, update numBytes
				numBytes += written;
				
				// create a new chunk and try to copy bytes to it
				blob_index++;
				blob = new ByteArrayEntity<byte[]>(blob_id+blob_index);
				written = blob.copyBytes(in_stream);
			}
			
			dateStamp = new Date();
			ofy().save().entity(this).now();
		} catch (IOException e) {
			throw new RuntimeException("IOError while trying to persist file " + name + " from stream:\n\t" + e.getMessage(), e);
		}
	}
	
	public byte[] load(){
		if(dateStamp == null){
			throw new RuntimeException("Attempting to unpersist file " + name + " but it hasn't been persisted" );
		}
		ByteArrayOutputStream retrieved_bytes = new ByteArrayOutputStream(numBytes);
		CRC32 retrieved_crc = new CRC32();
		OutputStream byte_wrapper = new CheckedOutputStream(retrieved_bytes, retrieved_crc);
		int current_index = 0;
		try {
			for( Ref<ByteArrayEntity<byte[]>> chunk_ref: chunkIds){
				ofy().load().ref(chunk_ref);
				ByteArrayEntity<byte[]> blob = chunk_ref.get();
				byte_wrapper.write(blob.getBytes());
				current_index += blob.getBytes().length;
			}
		} catch (IOException e) {
			throw new RuntimeException("IOError while trying to unpersist file " + name + " from datastore:\n\t" + e.getMessage(), e);
		}
		// all done, check the crc and length
		if(checksum.equals(retrieved_crc) || current_index != numBytes){
			throw new RuntimeException("CRC32 mismatch: Attempted to unpersist file " + name + " but it came back... wrong." );
		}
		return retrieved_bytes.toByteArray();
		
	}
	
	public CRC32 getCheckSum(){
		return checksum;
	}
	
	public Date getTimeStamp(){
		return dateStamp;
	}
	
}