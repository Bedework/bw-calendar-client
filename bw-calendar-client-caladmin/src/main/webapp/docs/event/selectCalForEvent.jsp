<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<bedework>
<%@ include file="/docs/header.jsp" %>

<page>selectCalForEvent</page>
<tab>main</tab>

<% /* Used when selecting a calendar while adding or editing an event.

      This page will be called when
      a) we add an event by date with no specific calendar selected
      b) we import an event
      c) we add an event ref
      d) we edit an event and change it's calendar (or change it while adding)

      The intention is to load the calendar listing in a pop-up window as a
      tree of writable calendars.
      */ %>

<calendars>
  <bean:define id="calendar" name="bw_collection_list" scope="session"
             toScope="session" />
  <%@include file="/docs/calendar/emitCalendar.jsp"%>
</calendars>

<%@ include file="/docs/footer.jsp" %>
</bedework>
