package com.cpsc310.treespotter.client;

import java.io.Serializable;

public class InvalidFieldException extends Exception implements Serializable {

	private static final long serialVersionUID = 3989704000264500797L;

	public InvalidFieldException() {
		super();
	}

	public InvalidFieldException(String message) {
		super(message);
	}

}
