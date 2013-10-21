<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<calendars>
  <logic:present name="bw_public_collection_list" scope="session">
    <bean:define id="calendar" name="bw_public_collection_list" scope="session"
               toScope="session" />
    <%@include file="/docs/calendar/emitCalendar.jsp"%>
  </logic:present>
</calendars>
