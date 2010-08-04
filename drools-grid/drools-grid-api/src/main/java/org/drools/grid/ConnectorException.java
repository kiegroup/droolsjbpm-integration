package org.drools.grid;

public class ConnectorException extends Exception {

	public ConnectorException() {
	}

	public ConnectorException(String message) {
		super(message);
	}

	public ConnectorException(Throwable cause) {
		super(cause.getMessage(),cause);
	}

	public ConnectorException(String message, Throwable cause) {
		super(message, cause);
	}

}
