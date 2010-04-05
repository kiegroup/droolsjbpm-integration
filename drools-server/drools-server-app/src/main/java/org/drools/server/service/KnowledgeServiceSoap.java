package org.drools.server.service;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.drools.CheckedDroolsException;

@WebService
public interface KnowledgeServiceSoap {

	String execute(@WebParam(name="command") String command) throws CheckedDroolsException;

}