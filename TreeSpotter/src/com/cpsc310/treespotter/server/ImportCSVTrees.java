package com.cpsc310.treespotter.server;


import static com.cpsc310.treespotter.server.CSVDepot.csvDepot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.cpsc310.treespotter.shared.CSVFile;


public class ImportCSVTrees extends HttpServlet {
	
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) 
			throws ServletException, IOException {
		
		StringBuilder sb = new StringBuilder();
		
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse request
		try {
			List<FileItem> items = upload.parseRequest(req);
			for (FileItem item : items) {
				if(item.isFormField()) {
					sb.append(item.getFieldName() + "\n");
					sb.append(item.getString() + "\n");
				}
				else {
					sb.append(item.getString());
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
		csvFile.printContents();
		
		csvDepot().addCSVFile(csvFile);
		CSVFile savedCSV = csvDepot().getCSVFile(email);
		savedCSV.printContents();
		
		return "Successfully uploaded";
	}

}
