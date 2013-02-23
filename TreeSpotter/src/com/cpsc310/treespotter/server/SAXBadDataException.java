package com.cpsc310.treespotter.server;

import org.xml.sax.SAXException;

public class SAXBadDataException extends SAXException {
	/**
	 * Construct a new Bad Data SAX Exception.
	 * <br><br>
	 * <b>Modifies:</b><br>(Similar to all constructors, modifies this)<br><br>
	 * <b>Effects:</b> <br> creates a new SAXException with "Bad Data"
	 * prepended to the error string
	 * 
	 * @param string - An error description
	 */
	public SAXBadDataException(String string) {
		super("BAD DATA:" + string);
	}
	
	private static final long serialVersionUID = -7426039804697434148L;
}
