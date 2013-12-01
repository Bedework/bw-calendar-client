<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>outbox</page>

<%--  Display outbox --%>
<bean:define id="boxInfo" name="calForm" property="outBoxInfoRefreshed" />
<outbox>
  <%@include file="/docs/schedule/inoutbox.jsp"%>
</outbox>

<%@include file="/docs/footer.jsp"%>

</bedework>
