/**
 * 
 */
package com.cpsc310.treespotter.shared;

import java.io.BufferedReader;
import java.io.FilterReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author aleksy
 *
 */
public class FilteredCSVReader extends FilterReader {
	private ArrayList<Integer> columns;
	private final BufferedReader buffered_in;
	private boolean haveLine = false;
	private StringBuilder currentLine = new StringBuilder();
	private int lineIndex = -1;
	private int linesRead = 0;
	
	public FilteredCSVReader(BufferedReader bufferedReader, ArrayList<Integer> cols) {
		super(bufferedReader);
		buffered_in = bufferedReader;
		columns = cols;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterReader#read()
	 */
	@Override
	public int read() throws IOException {
		if((haveLine && lineIndex < currentLine.length()) || updateLine()){
			int now_index = lineIndex;
			lineIndex++;
			if(lineIndex >= currentLine.length()){
				haveLine = false;
				lineIndex = -1;
			}
			return currentLine.charAt(now_index);
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see java.io.FilterReader#read(char[], int, int)
	 */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int read_chars = 0;
		int desired_chars = 0;
		try{
			while( read_chars < len && ((haveLine && lineIndex < currentLine.length()) || updateLine()) ){
				desired_chars = Math.min(currentLine.length()-lineIndex, len-read_chars);
				currentLine.getChars(lineIndex, desired_chars+lineIndex, cbuf, off + read_chars);
				lineIndex += desired_chars;
				read_chars += desired_chars;
				if(lineIndex == currentLine.length()){
					lineIndex = -1;
					haveLine = false;
				}
			}
		}
		catch(StringIndexOutOfBoundsException e){
			throw e;
		}
		return read_chars;
	}

	public String readLine() throws IOException{
		if((haveLine && lineIndex < currentLine.length()) || updateLine()){
			int now_index = lineIndex;
			lineIndex = -1;
			haveLine = false;
			return currentLine.substring(now_index, currentLine.length()-1);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterReader#skip(long)
	 */
	@Override
	public long skip(long n) throws IOException {
		// this is really inefficient if skipping within a line
		return super.skip(n);
	}

	/* (non-Javadoc)
	 * @see java.io.FilterReader#markSupported()
	 */
	@Override
	public boolean markSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.io.FilterReader#reset()
	 */
	@Override
	public void reset() throws IOException {
		// TODO Auto-generated method stub
		super.reset();
		linesRead = 0;
		setUnparsed();
	}
	
	public int getLinesRead(){
		return linesRead;
	}
	
	private boolean updateLine() throws IOException{
		String line_string = buffered_in.readLine();
		if(line_string == null){
			haveLine = false;
			lineIndex = -1;
			return false;
		}
		currentLine.setLength(0);
		String[] split_line = line_string.split(" *, *");
		String prefix = "";
		for(int i: columns){
			currentLine.append(prefix);
			currentLine.append(split_line[i]);
			prefix = ",";
		}
		currentLine.append("\n");
		haveLine = true;
		lineIndex = 0;
		linesRead++;
		//System.out.println(currentLine.toString());
		return true;
	}
	
	private void setUnparsed(){
		haveLine = false;
		lineIndex = -1;
	}

}
