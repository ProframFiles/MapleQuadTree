package com.cpsc310.treespotter.client;

import java.io.Serializable;

public class InvalidFieldException extends Exception implements Serializable {

	public InvalidFieldException() {
		super();
	}

	public InvalidFieldException(String message) {
		super(message);
	}

}
