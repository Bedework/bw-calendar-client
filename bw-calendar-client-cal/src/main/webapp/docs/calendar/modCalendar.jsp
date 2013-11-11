<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>modCalendar</page>
<creating><bean:write name="calForm" property="addingCalendar"/></creating>

<%@include file="/docs/calendar/displayCalendarCommon.jsp"%>

<%@include file="/docs/calendar/emitCalendars.jsp"%>

<publicCalendars>
  <bean:define id="calendar" name="bw_public_collection_list" scope="session"
             toScope="session" />
  <%@include file="/docs/calendar/emitCalendar.jsp"%>
</publicCalendars>

<%@include file="/docs/synchInfo.jsp"%>

<%@include file="/docs/footer.jsp"%>


</bedework>
