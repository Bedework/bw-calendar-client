<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%-- Load the header common to all pages --%>
<bedework>
<%@ include file="/docs/header.jsp" %>

<page>home</page>
<!-- introductory page; text of page in "home" xsl template  -->

<%@ include file="/docs/footer.jsp" %>

</bedework>
