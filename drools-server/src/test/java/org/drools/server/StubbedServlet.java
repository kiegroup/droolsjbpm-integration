package org.drools.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StubbedServlet extends KnowledgeStatelessServlet {

	InputStream inputStream;
	OutputStream outputStream;

	@Override
	InputStream getInputStream(HttpServletRequest request)
			throws IOException {
		return inputStream;
	}


	@Override
	OutputStream getOutputStream(HttpServletResponse resp) throws IOException {
		return outputStream;
	}




}
