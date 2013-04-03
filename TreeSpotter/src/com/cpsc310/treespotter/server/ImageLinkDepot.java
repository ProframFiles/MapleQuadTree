package com.cpsc310.treespotter.server;

import static com.cpsc310.treespotter.server.OfyService.ofy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;

@Entity
@Cache
public class ImageLinkDepot {

	static ImageLinkDepot instance;
	@Id String id;
	@Unindex Map<String, Ref<TreeImageList>> treeImageLinks;
	
	private static Logger LOG  = Logger.getLogger("Tree");
	
	ImageLinkDepot() {}
	
	private ImageLinkDepot(String id) {
		this.id = id;
		treeImageLinks = new HashMap<String, Ref<TreeImageList>>();
	}
	
	static ImageLinkDepot imageLinkDepot() {
		return imageLinkDepot("imageLinkDepot");
	}
	
	private static synchronized ImageLinkDepot imageLinkDepot(final String id) {
		if( instance != null){
			return instance;
		}
		instance = ofy().transact(new Work<ImageLinkDepot>() {
		    public ImageLinkDepot run() {
		    	
		    	ImageLinkDepot depot =  ofy().load().key(Key.create(ImageLinkDepot.class, id)).getValue();
		    	
		    	return depot;
		    }
		});
		
		if(instance == null){
			instance = new ImageLinkDepot(id);
			saveDepotState(instance);
		}
		return instance;
	}

	private static void saveDepotState(final ImageLinkDepot depot) {
		LOG.info("Saving current depot state");
		ofy().transact(new VoidWork() {
		    public void vrun() {
		    	ofy().save().entity(depot);
		    }
		});
	} 
	
	
	public void addImageLink(final String treeId, final String link) {
		if (treeImageLinks == null) {
			LOG.info("Uh oh. Error in persisting earlier");
			treeImageLinks = new HashMap<String, Ref<TreeImageList>>();
		}
		LOG.info("Adding link " + link + " to tree " + treeId);
		String treeID = originalTreeID(treeId);
		final TreeImageList imageList = getImageList(treeID);
		ofy().transact(new Work<TreeImageList>() {
			
			@Override
			public TreeImageList run() {
				imageList.addImageLink(link);
				ofy().save().entity(imageList);
				saveDepotState(instance);
				LOG.info("Done adding link to " + treeId);
				return imageList;
			}
		});
		
	}
	
	public synchronized TreeImageList getImageList(final String treeId) {
		if (treeImageLinks == null) {
			LOG.info("Uh oh. Error in persisting earlier");
			treeImageLinks = new HashMap<String, Ref<TreeImageList>>();
		}
		LOG.info("Received request to grab image links from " + treeId);
		TreeImageList treeImages = ofy().transact(new Work<TreeImageList>() {

			@Override
			public TreeImageList run() {
				String treeID = originalTreeID(treeId);
				Ref<TreeImageList> imageRef = treeImageLinks.get(treeID);
				TreeImageList treeImages;
				
				if (imageRef == null) {
					LOG.info("No prior links");
					return new TreeImageList(treeID);
				} else {
					ofy().load().ref(imageRef);
					treeImages = imageRef.get();
				}
				@SuppressWarnings("unused")
				ArrayList<String> links = treeImages.getImageLinks();
				return treeImages;
			}
		});
		return treeImages;
	}
	
	
	private String originalTreeID(final String treeID) {
		String user = treeID.substring(0, 1);
		if (user.toUpperCase().equals("M")) {
			String num = treeID.substring(1);
			return "V" + num;
		}
		
		return treeID;
	}
	
}
