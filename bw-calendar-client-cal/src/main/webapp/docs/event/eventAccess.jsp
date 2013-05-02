<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%@ include file="/docs/header.jsp" %>

<page>eventAccess</page>

<eventAccess>
  <subscriptionId></subscriptionId>
  <guid><bean:write name="calForm" property="event.uid"/></guid>
  <recurrenceId><bean:write name="calForm" property="event.recurrenceId"/></recurrenceId>
  <bw:emitContainer name="calForm" property="event"
                    indent="  " tagName="calendar" />

  <logic:present name="calForm" property="curEventFmt">
    <bean:define id="eventFormatter" name="calForm" property="curEventFmt"/>
    <bw:emitText name="eventFormatter" property="xmlAccess" tagName="access"
                 filter="no"/>
  </logic:present>
</eventAccess>



<%@ include file="/docs/footer.jsp" %>

