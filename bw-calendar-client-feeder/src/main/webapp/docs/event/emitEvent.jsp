<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>

<bean:define id="eventFmt" name="eventFormatter" scope="request" />
<bean:define id="eventInfo" name="eventFmt" property="eventInfo" toScope="request"  />
<bean:define id="event" name="eventFmt" property="event" toScope="request"  />
<%-- Output a single event. This page handles fields common to all views --%>
<%@ include file="/docs/event/emitEventCommon.jsp" %>

