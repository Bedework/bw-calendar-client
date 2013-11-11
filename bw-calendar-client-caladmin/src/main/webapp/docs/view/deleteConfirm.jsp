<%@ taglib uri='struts-bean' prefix='bean' %>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>deleteViewConfirm</page>
<tab>calsuite</tab>

<bean:define name="calForm" property="view" id="curView"/>
<currentView>
  <name><bean:write name="curView" property="name" /></name>
  <logic:iterate name="curView" property="collectionPaths" id="path">
    <path><bean:write name="path"/></path>
  </logic:iterate>
</currentView>

<%@include file="/docs/footer.jsp"%>
</bedework>
