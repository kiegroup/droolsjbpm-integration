package org.drools.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class MockHTTPResponse implements HttpServletResponse {

	String contentType;
	Map<String, String> headers = new HashMap<String, String>();
	int errorCode;
	StringWriter stringWriter = new StringWriter();
	private PrintWriter writer = new PrintWriter(stringWriter);
	String redirect;


	public MockHTTPResponse() {

	}

	public void addCookie(Cookie arg0) {
		// TODO Auto-generated method stub

	}

	public void addDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub

	}

	public void addHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public void addIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public boolean containsHeader(String a) {

		return this.headers.containsKey(a);
	}

	public String encodeRedirectURL(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String encodeURL(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String encodeUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendError(int i) throws IOException {
		this.errorCode = i;

	}

	public void sendError(int arg0, String arg1) throws IOException {
		this.errorCode = arg0;
	}

	public void sendRedirect(String arg0) throws IOException {
		this.redirect = arg0;

	}

	public void setDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub

	}

	public void setHeader(String k, String v) {
		this.headers.put(k, v);

	}

	public void setIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public void setStatus(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub

	}

	public int getBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}

	public PrintWriter getWriter() throws IOException {

		return writer ;
	}

	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return false;
	}

	public void reset() {
		// TODO Auto-generated method stub

	}

	public void resetBuffer() {
		// TODO Auto-generated method stub

	}

	public void setBufferSize(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setContentType(String s) {
		this.contentType = s;

	}

	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub

	}



}
