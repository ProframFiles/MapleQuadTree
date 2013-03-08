/**
 * 
 */
package com.cpsc310.treespotter.shared;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

/**
 * @author maple-quadtree
 *
 */
public class ZippedCSVFilter {
	ArrayList<Integer> selectedIndices;
	public ZippedCSVFilter(ArrayList<Integer> indices){
		selectedIndices = indices;
	}
	
	public byte[] filter(InputStream is) throws IOException{
		final BufferedReader in = new BufferedReader(new InputStreamReader(is));
		ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
		GZIPOutputStream out_stream = new GZIPOutputStream(byte_stream);
		OutputStreamWriter out = new OutputStreamWriter(out_stream);
		StringBuilder sb = new StringBuilder();
		String line_string;
		while((line_string = in.readLine()) != null){
			String[] split_line = line_string.split("[ *, *]");
			String prefix = "";
			for(int i: selectedIndices){
				sb.append(prefix);
				sb.append(split_line[i]);
				prefix = ",";
			}
			sb.append("\n");
			out.write(sb.toString());
			sb.setLength(0);
		}
		out_stream.finish();
		
		return byte_stream.toByteArray();
		
	}
	
}
