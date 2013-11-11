<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<bedework>
<%@include file="/docs/header.jsp"%>

<% /*  the same as calendarList.jsp, but will be treated differently  */ %>
<page>calendarDescriptions</page>

<%@include file="/docs/calendar/emitPublicCalendars.jsp"%>

<%@include file="/docs/footer.jsp"%>
</bedework>
