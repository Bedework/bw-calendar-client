<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>inbox</page>

<%--  Display inbox --%>
<bean:define id="boxInfo" name="calForm" property="inBoxInfo" />
<inbox>
  <%@include file="/docs/schedule/inoutbox.jsp"%>
</inbox>

<%@include file="/docs/footer.jsp"%>

</bedework>
