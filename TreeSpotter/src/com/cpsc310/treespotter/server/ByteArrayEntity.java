/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;

/**
 * @author maple-quadtree
 *
 */
@Entity
public class ByteArrayEntity {
	// limit is a MB, but we'll play it safe for now, and go with 0.5MB
	static final int MAX_NUM_BYTES = 524288;
	static final int INITIAL_BUFFER_SIZE = 1024;
	@Id String key;
	@Unindex byte b[];

	public ByteArrayEntity(){
		
	}
	
	public ByteArrayEntity(String id_string){
		setKey(id_string);
	}
	
	public void setKey(String id){
		key = id;
	}
	
	public byte[] getBytes(){
		return b;
	}
	
	public void setBytes(byte[] bytes_in){
		if(bytes_in.length > MAX_NUM_BYTES){
			throw new RuntimeException("Can't persist this many bytes in a single entity. bytes: "
										+ bytes_in.length + ", max allowed: " + MAX_NUM_BYTES);
		}
		b = bytes_in;
	}
	
	public int copyBytes(InputStream in_stream) throws IOException{
		b = null;
		ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
	    int len;
	    while (byte_stream.size() < MAX_NUM_BYTES && (len =in_stream.read(buffer)) > -1 ) {
	        byte_stream.write(buffer, 0, len);
	    }
	    byte_stream.flush();
	    b = byte_stream.toByteArray();
		return byte_stream.size();
	}

}
