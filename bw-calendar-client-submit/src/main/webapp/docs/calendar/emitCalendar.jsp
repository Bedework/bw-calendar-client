<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>

<calendar>
  <bw:emitCollection name="calendar" indent="  " full="true" noTag="true" />
  
  <bw:emitCurrentPrivs name="calendar" property="currentAccess" />
  <bw:emitAcl name="calendar" property="currentAccess" />

  <bw:getChildren id="children" name="calendar" />
  <logic:iterate name="children" id="cal">
    <bean:define id="calendar" name="cal" toScope="session" />
    <jsp:include page="/docs/calendar/emitCalendar.jsp" />
  </logic:iterate>
</calendar>

