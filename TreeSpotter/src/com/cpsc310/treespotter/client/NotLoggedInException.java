package com.cpsc310.treespotter.client;

import java.io.Serializable;

public class NotLoggedInException extends Exception implements Serializable {

	private static final long serialVersionUID = 7632881285110785247L;

public NotLoggedInException() {
    super();
  }

  public NotLoggedInException(String message) {
    super(message);
  }

}