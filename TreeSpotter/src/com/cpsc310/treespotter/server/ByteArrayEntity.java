/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;

/**
 * @author maple-quadtree
 *
 */
@Entity
public class ByteArrayEntity<T> {
	// limit is a MB, but we'll play it safe for now, and go with 0.5MB
	static final int MAX_NUM_BYTES = 524288;
	static final int INITIAL_BUFFER_SIZE = 1024;
	@Id String key;
	@Unindex byte b[];

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
		int read_bytes = 0;
		b = null;
		do{
			if(b == null){
				b = new byte[INITIAL_BUFFER_SIZE];
			}
			else{
				b = Arrays.copyOf(b, b.length*2);
			}
			int this_read = 0;
			do{
				read_bytes += this_read;
				this_read = in_stream.read(b, read_bytes, b.length-read_bytes);
			}while( this_read >0 );
		}while(b.length <= MAX_NUM_BYTES/2);
		return read_bytes;
	}

}
