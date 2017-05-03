<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@ include file="/docs/header.jsp" %>

<%
try {
%>

<page>modLocation</page>

<creating><bean:write name="calForm" property="addingCategory"/></creating>

<currentLocation>
  <bw:emitText name="calForm" property="location.addressField" tagName="address" />
  <bw:emitText name="calForm" property="location.Street" tagName="subaddress"/>
  <bw:emitText name="calForm" property="location.link" tagName="link"/>
  <bw:emitText name="calForm" property="location.uid" tagName="uid"/>
</currentLocation>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

<%@ include file="/docs/footer.jsp" %>

</bedework>
