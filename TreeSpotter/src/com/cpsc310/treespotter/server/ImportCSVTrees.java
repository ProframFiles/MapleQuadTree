package com.cpsc310.treespotter.server;


import static com.cpsc310.treespotter.server.CSVDepot.csvDepot;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import com.cpsc310.treespotter.shared.CSVFile;


public class ImportCSVTrees extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest req, HttpServletResponse res) 
			throws ServletException, IOException {
		
		StringBuilder sb = new StringBuilder();
		
		ServletFileUpload upload = new ServletFileUpload();
		try {
			FileItemIterator iterator = upload.getItemIterator(req);
			while (iterator.hasNext()) {
				FileItemStream item = iterator.next();
				String name = item.getFieldName();
				InputStream stream = item.openStream();
				
				if(item.isFormField()) {
					sb.append(name + "\n");
					sb.append(Streams.asString(stream) + "\n");
					
				}
				else {
					sb.append(Streams.asString(stream));
				}
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
		
		
		// create and persist file
		String message = createNewFile(sb.toString());
		res.setContentType("text/html");
		res.getWriter().print(message);
		
		
	}

	private String createNewFile(String string) {
		// TODO: Data checking?
		String[] strs = string.split("\n");
		
		String user = strs[0];
		String email = strs[1];
		// String header = strs[2]; // 1st line in CSV is header
		
		String[] contents = new String[strs.length - 3];
		for (int i=3; i<strs.length; i++) {
			contents[i-3] = (strs[i]);
		}
		
		CSVFile csvFile = new CSVFile(email, user, contents);
		
		csvDepot().addCSVFile(csvFile);
		
		return "Successfully uploaded";
	}

}
