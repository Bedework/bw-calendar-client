<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<bedework>
<%@ include file="/docs/header.jsp" %>
<%
try {
%>
  <page>attendeeReply</page>
  <%-- Wrapper for a single event (emitEvent.jsp) --%>

  <guidcals>
  <logic:present  name="calForm" property="meetingCal">
    <bean:define id="cal" name="calForm" property="meetingCal"/>
    <calendar>
      <bw:emitText name="cal" property="name"/>
      <bw:emitText name="cal" property="path"/>
    </calendar>
  </logic:present>
  </guidcals>

  <bean:define id="allView" value="true" toScope="request"/>
  <bean:define id="eventFormatter"
               name="calForm"
               property="curEventFmt"
               toScope="request"/>

  <%@ include file="/docs/event/emitEvent.jsp" %>
<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

<%@ include file="/docs/footer.jsp" %>
</bedework>
