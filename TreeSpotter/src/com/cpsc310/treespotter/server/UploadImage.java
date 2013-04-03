package com.cpsc310.treespotter.server;

import static com.cpsc310.treespotter.server.ImageLinkDepot.imageLinkDepot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

public class UploadImage extends HttpServlet {
	
	private static final Logger LOG = Logger.getLogger("Tree");
	private static final long serialVersionUID = 1L;
	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) 
			throws ServletException, IOException {

		LOG.info("Received Upload Request on Server");
		
		
		@SuppressWarnings("deprecation")
		Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
		BlobKey blobKey = blobs.get("image");
		
		if (blobKey == null) {
			LOG.info("Uh oh. Blobkey is null");
			return;
		} else {
			
			ImagesService imagesService = ImagesServiceFactory.getImagesService();
			ServingUrlOptions urlOpts =  ServingUrlOptions.Builder.withBlobKey(blobKey);
			String imageUrl = imagesService.getServingUrl(urlOpts);
			LOG.info("URL: " + imageUrl);	
			
			LOG.info("Grabbing TreeID");
			String treeID = req.getParameter("treeID");

			LOG.info("TreeID is " + treeID);
			if (!treeID.equals(""))
				imageLinkDepot().addImageLink(treeID, imageUrl);
			
			res.sendRedirect("/upload?imageUrl="+imageUrl);
			
		}
	}
	
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
 
        String imageUrl = req.getParameter("imageUrl");
        resp.setHeader("Content-Type", "text/html");
 
        LOG.info("doGet URL: " + imageUrl);
        resp.getWriter().println(imageUrl);
 
    }
	
}
