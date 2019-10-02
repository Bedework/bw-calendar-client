<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-html' prefix='html' %>
<html:xhtml/>

<%
try {
%>
<bedework>
  <page>async</page>
  <status>error: see server log for details</status>
</bedework>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>