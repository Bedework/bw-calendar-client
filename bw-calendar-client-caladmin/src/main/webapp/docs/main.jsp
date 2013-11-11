<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<bedework>
<%@include file="header.jsp"%>

<page>main</page>
<tab>main</tab>

<%--  Generate form elements to be exposed.
      These can be manipulated in the xslt if desired.
<formElements>
  <genurl:form action="event/fetchForDisplay.do">
    <html:text name="calForm" property="eventId" size="6" />
    <html:submit value="go"/>
  </genurl:form>
</formElements>
--%>

<%@include file="footer.jsp"%>
</bedework>

