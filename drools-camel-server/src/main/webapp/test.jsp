<%@ page import="org.drools.server.CamelServerApp" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html>
  <head>
    <title>Test Server</title>
  </head>
  <body>
    <h1>Sending Test Message</h1>
    <%
        CamelServerApp test = new CamelServerApp();
        String msg = request.getParameter("msg");

        if ( msg == null ) {
            msg = "Hello World";
        }
    %>

    Sending Message: "<%=msg%>"<br/>
    <%
        String res = test.send( msg );
    %>
    Response: "<%=res%>"
  </body>
</html>
