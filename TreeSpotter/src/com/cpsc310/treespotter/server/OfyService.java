/**
 * 
 */
package com.cpsc310.treespotter.server;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

/**
 * @author maple-quadtree
 * 
 */
public class OfyService {
	static {
		factory().register(PersistentFile.class);
		factory().register(UserTreeUpdateStamp.class);
		factory().register(TreesIndexedByString.class);
		factory().register(ByteArrayEntity.class);
		factory().register(TreeCommentList.class);
		factory().register(CommentDepot.class);
		factory().register(TreeDepot.class);
		factory().register(Job.class);
		factory().register(DataUpdateJob.class);
		factory().register(CSVFile.class);
		factory().register(CSVDepot.class);
	}

	public static Objectify ofy() {
		return ObjectifyService.ofy();
	}

	public static ObjectifyFactory factory() {
		return ObjectifyService.factory();
	}
}
