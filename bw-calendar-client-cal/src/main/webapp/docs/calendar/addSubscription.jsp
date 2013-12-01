<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>addSubscription</page>
<creating><bean:write name="calForm" property="addingCalendar"/></creating>

<%@include file="/docs/calendar/emitCalendars.jsp"%>

<publicCalendars>
  <bean:define id="calendar" name="bw_public_collection_list" scope="session"
             toScope="session" />
  <%@include file="/docs/calendar/emitCalendar.jsp"%>
</publicCalendars>


<%@include file="/docs/footer.jsp"%>


</bedework>
