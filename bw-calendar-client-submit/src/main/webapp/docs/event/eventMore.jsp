<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<%@ include file="/docs/header.jsp" %>
<%
try {
%>
  <page>event</page>
  <%-- Wrapper for a single event (emitEvent.jsp) --%>
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
