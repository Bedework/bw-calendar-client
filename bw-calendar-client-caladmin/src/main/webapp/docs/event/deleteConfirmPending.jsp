<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%@include file="/docs/header.jsp"%>

<page>deleteEventConfirmPending</page>
<tab>pending</tab>

<bean:define id="allView" value="true" toScope="request"/>
<bean:define id="eventFormatter"
                 name="calForm"
                 property="curEventFmt"
                 toScope="request"/>

<%@ include file="/docs/event/emitEvent.jsp" %>

<%@include file="/docs/footer.jsp"%>

