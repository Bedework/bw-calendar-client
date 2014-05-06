<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<calendar>
  <bw:emitCollection name="calendar" indent="  " full="true" noTag="true" />

  <logic:equal name="calForm" property="guest" value="false">
    <%-- don't publish privs and acls to the public client --%>
    <bw:emitCurrentPrivs name="calendar" property="currentAccess" />
    <bw:emitAcl name="calendar" property="currentAccess" />
  </logic:equal>

  <bw:getChildren id="children" name="calendar" />
  <logic:present name="children">
    <logic:iterate name="children" id="cal">
      <bean:define id="calendar" name="cal" toScope="session" />
      <jsp:include page="/docs/calendar/emitCalendar.jsp" />
    </logic:iterate>
  </logic:present>
</calendar>

