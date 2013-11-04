<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%--  Generate events --%>
<bean:define id="detailView" value="true" toScope="request"/>

<events>
  <bw:emitText name="calForm" property="eventRegAdminToken"/>
  <logic:iterate id="sre" name="bw_search_list" scope="request">
    <logic:equal name="sre" property="docType" value="event">
      <bean:define id="eventFormatter"
                   name="eventFmt" property="eventInfo" toScope="request"  />
      <%@include file="/docs/event/emitEvent.jsp"%>
    </logic:equal>
  </logic:iterate>
</events>


