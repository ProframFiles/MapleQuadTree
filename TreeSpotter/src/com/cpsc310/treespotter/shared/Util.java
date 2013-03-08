/**
 * 
 */
package com.cpsc310.treespotter.shared;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author maple-quadtree
 *
 */
public class Util {
	public static byte[] streamToByteArray(InputStream is) throws IOException{
		return streamToByteArray(is, 1024);
	}
	public static byte[] streamToByteArray(InputStream is, int read_buffer_size) throws IOException{
		ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
		byte[] buffer = new byte[read_buffer_size];
	    int len;
	    while ((len = is.read(buffer)) > -1 ) {
	        byte_stream.write(buffer, 0, len);
	    }
	    byte_stream.flush();
		return byte_stream.toByteArray();
	}
}
