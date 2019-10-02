<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@ include file="/docs/header.jsp" %>

<%
  try {
%>

  <page>managePolls</page>

  <vpoll>
    <bw:emitText property="requestedUid" name="calForm" tagName="uid" />
    <tab><logic:present name="bw_req_vpoll_tab" scope="session" ><bean:write name="bw_req_vpoll_tab" scope="session" /></logic:present></tab>
    <defaultCalendarPath><logic:present name="bw_default_event_calendar" scope="session" ><bean:write name="bw_default_event_calendar" scope="session" /></logic:present></defaultCalendarPath>
  </vpoll>

<%
  } catch (final Throwable t) {
    t.printStackTrace();
  }
%>

<%@ include file="/docs/footer.jsp" %>

</bedework>