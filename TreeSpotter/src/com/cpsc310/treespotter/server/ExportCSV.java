package com.cpsc310.treespotter.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExportCSV extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest req, HttpServletResponse res) 
			throws ServletException, IOException{
		
		System.out.println("Received data to put in CSV");
		String csvLine = req.getParameter("textbox");
		String[] csvRaw = csvLine.split("/n");
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i<csvRaw.length; i++) {
			sb.append(csvRaw[i] + "\n");
		}
		
		try {
			String csv = sb.toString();
			byte[] bytes = csv.getBytes();
			sendCSV(res, bytes, "tree_data.csv");
			System.out.println("Sent CSV back to client");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendCSV(HttpServletResponse response, byte[] bytes, String name) throws IOException {
	    ServletOutputStream stream = null;

	    stream = response.getOutputStream();
	    response.setContentType("application/force-download");
	    response.addHeader("Content-Disposition", "inline; filename=" + name);
	    response.setContentLength(bytes.length);
	    stream.write(bytes);
	    stream.close();
	}
}
