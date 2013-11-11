<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<%-- Load the header common to all pages --%>
<bedework>
<%@ include file="/docs/header.jsp" %>

<%-- the <page> element allows us to branch in our XSLT based on what page
    (what "state") we are in.  The value is hard coded into the top-level
    JSP pages for the calendar.  It has four possible values:

      1. eventscalendar - the event calendar tree from which we build our
                          day, week, month, and year views (main.jsp)
      2. event          - a single event (see eventMore.jsp)
      3. calendars      - the listing of calendars (see calendars.jsp)
      4. other          - an arbitrary page (see showPage.jsp)
--%>
<page>eventscalendar</page>
<bean:define id="dayViewName" name="calForm" property="viewTypeName[1]"/>

<%-- The events listing in a calendar tree --%>
<eventscalendar>
  <%--
  <bean:define id="timeInfo" name="calForm"
               property="curTimeView.timePeriodInfo"/>
               --%>
  <bean:define id="curTimeView" name="moduleState" property="curTimeView"/>
  <bean:define id="timeInfo" name="curTimeView" property="timePeriodInfo"/>
  <logic:iterate id="yearInfo" name="timeInfo" >
    <year>
      <value><bean:write name="yearInfo" property="year"/></value>
      <logic:iterate id="monthInfo" name="yearInfo" property="entries" >
        <month>
          <value><bean:write name="monthInfo" property="month"/></value>
          <longname><bean:write name="monthInfo" property="monthName"/></longname>
          <shortname><bean:write name="monthInfo" property="monthName"/></shortname>
          <logic:iterate id="weekInfo" name="monthInfo" property="entries" >
            <week>
              <value><bean:write name="weekInfo" property="weekOfYear"/></value>
              <logic:iterate id="dayInfo" name="weekInfo" property="entries" >
                <day>
                  <filler><bean:write name="dayInfo" property="filler"/></filler>
                  <%/* Fillers currently have no information */%>
                  <logic:equal name="dayInfo" property="filler" value="false">
                    <value><bean:write name="dayInfo" property="dayOfMonth"/></value>
                    <name><bean:write name="dayInfo" property="dayName"/></name>
                    <date><bean:write name="dayInfo" property="date"/></date>
                    <longdate><bean:write name="dayInfo" property="dateLong"/></longdate>
                    <shortdate><bean:write name="dayInfo" property="dateShort"/></shortdate>
                    <%-- Do not produce events if we are in the year view
                    <logic:equal name="calForm"
                                 property="curTimeView.showData" value="true">
                                 --%>
                    <logic:equal name="curTimeView" property="showData" value="true">
                      <logic:iterate id="eventFmt" name="dayInfo" property="events">
                        <bean:define id="eventFormatter" name="eventFmt"
                                     toScope="request" />
                        <jsp:include page="/docs/event/emitEvent.jsp" />
                      </logic:iterate>
                    </logic:equal>
                  </logic:equal>
                </day>
              </logic:iterate>
            </week>
          </logic:iterate>
        </month>
      </logic:iterate>
    </year>
  </logic:iterate>
</eventscalendar>

<%@ include file="/docs/footer.jsp" %>

</bedework>
