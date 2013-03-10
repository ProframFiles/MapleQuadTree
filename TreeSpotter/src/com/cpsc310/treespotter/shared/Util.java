/**
 * 
 */
package com.cpsc310.treespotter.shared;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.CharBuffer;

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
	
	public static byte[] ReaderToByteArray(Reader is, int read_buffer_size) throws IOException{
		ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
		ReaderToOutputStream(is, byte_stream, read_buffer_size);
		return byte_stream.toByteArray();
	}
	
	public static int ReaderToOutputStream(Reader is, OutputStream os) throws IOException{
		return ReaderToOutputStream(is, os, 1024);
	}
	
	public static int ReaderToOutputStream(Reader is, OutputStream os, int read_buffer_size) throws IOException{
		OutputStreamWriter out_writer = new OutputStreamWriter(os);
		CharBuffer buffer = CharBuffer.allocate(read_buffer_size);
	    int len;
	    int total = 0;
	    while ((len = is.read(buffer)) > 0 ) {
	        out_writer.write(buffer.array(), 0, len);
	        total += len;
	        buffer.clear();
	    }
	    out_writer.flush();
	    return total;
	}
}
