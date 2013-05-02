<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<calendars>
  <logic:present name="calForm" property="publicCalendars">
    <bean:define id="calendar" name="calForm" property="publicCalendars"
               toScope="session" />
    <%@include file="/docs/calendar/emitCalendar.jsp"%>
  </logic:present>
</calendars>
