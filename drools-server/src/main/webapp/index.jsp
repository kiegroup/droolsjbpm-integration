<%@ page import="org.drools.server.*" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html>
	<head>
		<title>Drools execution server</title>
	</head>
	<body>
		<h1>Rule execution server</h1>
		This service allows you to execute rules/knowledge bases remotely using a RESTful interface.
		<h2>Stateless services</h2>
		<h3>URL:</h3><%=request.getRequestURI() + "knowledgebase/{configurationName}" %><br/>
		The {configurationName} is the name of a configured agent properties file (not incliding the .properties extension).
		This configuration then declares what packages are used, where the repository is etc.
		This properties file must be in the classpath for this war - in the WEB-INF/classes directory.
		<h3>Sample request content:</h3>
		<textarea>
		</textarea>

		<h3>Sample response content:</h3>

		<h3>General instructions<h3>
		By default XML will be used, if you pass in a Content-Type header of application/json, then it will use JSON instead.
		JSON is both a more compact and more performant format.
		HTTP POST is used to access this service.



	</body>
</html>