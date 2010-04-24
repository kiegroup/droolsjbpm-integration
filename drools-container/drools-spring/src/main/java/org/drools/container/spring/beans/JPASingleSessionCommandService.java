package org.drools.container.spring.beans;

import org.drools.command.SingleSessionCommandService;


public interface JPASingleSessionCommandService  {
	public SingleSessionCommandService createNew();
	public SingleSessionCommandService load(int sessionId);
}
