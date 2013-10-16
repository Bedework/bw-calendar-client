<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%--  Generate events --%>
<bean:define id="detailView" value="true" toScope="request"/>
<events>
  <logic:present name="bw_event_list" scope="request">
    <logic:iterate id="eventFormatter" name="bw_event_list" scope="request">
      <%@include file="/docs/event/emitEventForList.jsp"%>
    </logic:iterate>
  </logic:present>
</events>
