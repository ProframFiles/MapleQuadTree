package com.cpsc310.treespotter.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

public class UploadImage extends HttpServlet {
	
	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) 
			throws ServletException, IOException {

		System.out.println("Received Upload Request on Server");
		
		
		@SuppressWarnings("deprecation")
		Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
		BlobKey blobKey = blobs.get("image");
		
		if (blobKey == null) {
			System.out.println("Blobkey is null");
			return;
		} else {
			
			ImagesService imagesService = ImagesServiceFactory.getImagesService();
			ServingUrlOptions urlOpts =  ServingUrlOptions.Builder.withBlobKey(blobKey);
			String imageUrl = imagesService.getServingUrl(urlOpts);
			System.out.println(imageUrl);
			
			res.sendRedirect("/upload?imageUrl="+imageUrl);
			
		}
	}
	
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
 
        String imageUrl = req.getParameter("imageUrl");
        resp.setHeader("Content-Type", "text/html");
 
        System.out.println("doGet URL: " + imageUrl);
        resp.getWriter().println(imageUrl);
 
    }
	
}
