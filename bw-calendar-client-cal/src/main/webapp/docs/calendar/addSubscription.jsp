<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<%@include file="/docs/header.jsp"%>

<page>addSubscription</page>
<creating><bean:write name="calForm" property="addingCalendar"/></creating>

<%@include file="/docs/calendar/emitCalendars.jsp"%>

<publicCalendars>
  <bean:define id="calendar" name="calForm" property="publicCalendars"
             toScope="session" />
  <%@include file="/docs/calendar/emitCalendar.jsp"%>
</publicCalendars>


<%@include file="/docs/footer.jsp"%>


