<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%--  Generate events --%>
<bean:define id="detailView" value="true" toScope="request"/>
<events>
  <logic:present name="calForm" property="formattedEvents">
    <logic:iterate id="eventFormatter" name="calForm" property="formattedEvents">
      <%@include file="/docs/event/emitEventForList.jsp"%>
    </logic:iterate>
  </logic:present>
</events>
