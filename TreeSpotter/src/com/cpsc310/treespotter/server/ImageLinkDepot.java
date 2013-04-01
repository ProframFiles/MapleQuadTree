package com.cpsc310.treespotter.server;

import static com.cpsc310.treespotter.server.OfyService.ofy;

import java.util.HashMap;
import java.util.Map;

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
		ofy().transact(new VoidWork() {
		    public void vrun() {
		    	ofy().save().entity(depot);
		    }
		});
	} 
	
	
	public void addImageLink(final String treeId, final String link) {
		String treeID = originalTreeID(treeId);
		final TreeImageList imageList = getImageList(treeID);
		ofy().transact(new Work<TreeImageList>() {
			
			@Override
			public TreeImageList run() {
				imageList.addImageLink(link);
				ofy().save().entity(imageList);
				saveDepotState(instance);
				return imageList;
			}
		});
		
	}
	
	public synchronized TreeImageList getImageList(final String treeId) {
		TreeImageList treeImages = ofy().transact(new Work<TreeImageList>() {

			@Override
			public TreeImageList run() {
				String treeID = originalTreeID(treeId);
				Ref<TreeImageList> imageRef = treeImageLinks.get(treeID);
				TreeImageList treeImages;
				if (imageRef == null) {
					treeImages = new TreeImageList(treeID);
					ofy().save().entity(treeImages);
					treeImageLinks.put(treeId, Ref.create(treeImages));
					saveDepotState(instance);
				} else {
					ofy().load().ref(imageRef);
					treeImages = imageRef.get();
				}
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
