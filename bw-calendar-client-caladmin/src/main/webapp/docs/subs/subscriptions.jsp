<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<%@include file="/docs/header.jsp"%>

<page>subscriptions</page>
<tab>calsuite</tab>

<!-- subscriptions are a subset of calendars; there is no difference to the underlying code. -->
<calendars>
  <bean:define id="calendar" name="bw_user_collection_list" scope="session"
             toScope="session" />
  <%@include file="/docs/calendar/emitCalendar.jsp"%>
</calendars>

<%@include file="/docs/footer.jsp"%>
