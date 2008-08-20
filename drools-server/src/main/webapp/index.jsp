<%@ page import="org.drools.server.*" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html>
	<head>
		<title>Drools execution server</title>
	</head>
	<body>
		<h1>Execution server is running</h1>
		This server allows you to execute rules/knowledge bases remotely using a RESTful interface. One service per RuleAgent configuration (you can have as many as needed).
		<h2>Stateless services</h2>
		<h3>URL:</h3>http://{server address, port etc}<%=request.getRequestURI() + "knowledgebase/{configurationName}" %><br/>
		a HTTP POST to this URL will perform a stateless execution of the knowledgebase/rules.
		<p/>
		The {configurationName} is the name of a configured rule agent properties file (not incliding the .properties extension).
		This configuration then declares what packages are used, where the repository is etc (this can work with both Guvnor, but also DRL files, etc).
		This properties file must be in the classpath for this war - in the WEB-INF/classes directory.
		<h3>Sample request content:</h3>
		<textarea rows=20 cols=80><%=KnowledgeStatelessServlet.getRequestExample(false) %></textarea>

		<h3>Sample response content:</h3>
		<textarea rows=20 cols=80><%=KnowledgeStatelessServlet.getResponseExample(false) %></textarea>

		<h3>General instructions</h3>
		By default XML will be used, if you pass in a Content-Type header of application/json, then it will use JSON instead.
		JSON is both a more compact and more performant format.
		HTTP POST is used to access this service.
		<p/>
		<h3>Sample JSON request:</h3>
		<textarea rows=10 cols=80><%=KnowledgeStatelessServlet.getRequestExample(true) %></textarea>

		<h3>Sample JSON response:</h3>
		<textarea rows=10 cols=80><%=KnowledgeStatelessServlet.getResponseExample(true) %></textarea>

		</h3>


	</body>
</html>
