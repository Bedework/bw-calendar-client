<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>calendarListForExport</page>

<%-- list the public calendars; we can get subscriptions and myCalendars
     from the header xml --%>

<calendars>
  <logic:present name="bw_public_collection_list" scope="session">
    <bean:define id="calendar" name="bw_public_collection_list" scope="session"
                 toScope="session" />
    <%@include file="/docs/calendar/emitCalendar.jsp"%>
  </logic:present>
</calendars>

<formElements>
  <genurl:form action="event/editEvent">
    <start>
      <month>
        <html:select property="eventStartDate.month">
          <html:options labelProperty="eventStartDate.monthLabels"
                        property="eventStartDate.monthVals"/>
        </html:select>
      </month>
      <day>
        <html:select property="eventStartDate.day">
          <html:options labelProperty="eventStartDate.dayLabels"
                        property="eventStartDate.dayVals"/>
        </html:select>
      </day>
      <yearText>
        <html:text property="eventStartDate.year" size="4"/>
      </yearText>
    </start>
    <end>
      <month>
        <html:select property="eventEndDate.month">
            <html:options labelProperty="eventEndDate.monthLabels"
                          property="eventEndDate.monthVals"/>
        </html:select>
      </month>
      <day>
        <html:select property="eventEndDate.day">
          <html:options labelProperty="eventEndDate.dayLabels"
                        property="eventEndDate.dayVals"/>
        </html:select>
      </day>
      <yearText>
        <html:text property="eventEndDate.year" size="4"/>
      </yearText>
    </end>
  </genurl:form>
</formElements>

<%@include file="/docs/footer.jsp"%>
</bedework>
