<%@ taglib uri='struts-bean' prefix='bean' %>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>modResource</page>
<tab>calsuite</tab>

<creating><bean:write name="calForm" property="addingResource"/></creating>

<bean:define name="calForm" property="calSuiteResource" id="curResource"/>
<currentResource>
  <name><bean:write name="curResource" property="name" /></name>
  <content-type><bean:write name="curResource" property="contentType" /></content-type>
  <class><bean:write name="curResource" property="rclass" /></class>
  <type><bean:write name="curResource" property="type" /></type>
  <path><bean:write name="curResource" property="path" /></path>
</currentResource>

<%@include file="/docs/footer.jsp"%>
</bedework>
