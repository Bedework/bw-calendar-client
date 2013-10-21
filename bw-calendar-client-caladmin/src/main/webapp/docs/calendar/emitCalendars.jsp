<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<calendars>
  <bean:define id="calendar" name="bw_collection_list" scope="session"
             toScope="session" />
  <%@include file="/docs/calendar/emitCalendar.jsp"%>
</calendars>


