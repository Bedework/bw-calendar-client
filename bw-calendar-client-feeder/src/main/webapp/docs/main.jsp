<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%-- Load the header common to all pages --%>
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
  <bean:define id="curTimeView" name="calForm" property="curTimeViewRefreshed"/>
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
                      <logic:iterate id="eventFmt" name="dayInfo"
                                     property="eventFormatters" >
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

<%-- Produce date and time form elements for personal calendar to be used
     for creating the personal event entry form.  Uncomment this code to
     use an entry form on a page other than the formal "add event" page. --%>

<%--
<logic:equal name="calForm" property="guest" value="false">
  <eventform>
    <genurl:form action="event/addEvent">
      <title>
        <html:text property="newEvent.summary"/>
      </title>
      <description>
        <html:textarea property="description"/>
      </description>
      <link>
        <html:text property="newEvent.link"/>
      </link>
      <location>
        <locationmenu>
          <html:select property="locationId">
            <html:optionsCollection property="locations"
                                    label="address"
                                    value="id"/>
          </html:select>
        </locationmenu>
        <locationtext>
          <html:text property="locationAddress.vaalue" />
        </locationtext>
      </location>
      <startdate>
        <html:select property="eventStartDate.month">
         <html:options labelProperty="eventStartDate.monthLabels"
                        property="eventStartDate.monthVals"/>
        </html:select>
        <html:select property="eventStartDate.day">
          <html:options labelProperty="eventStartDate.dayLabels"
                        property="eventStartDate.dayVals"/>
        </html:select>
        <html:select property="eventStartDate.year">
          <html:options property="yearVals"/>
        </html:select>
      </startdate>
      <starttime>
        <html:select property="eventStartDate.hour">
          <html:options labelProperty="eventStartDate.hourLabels"
                        property="eventStartDate.hourVals"/>
        </html:select>
        <html:select property="eventStartDate.minute">
          <html:options labelProperty="eventStartDate.minuteLabels"
                        property="eventStartDate.minuteVals"/>
        </html:select>
        <logic:notEqual name="calForm" property="hour24" value="true" >
          <html:select property="eventStartDate.ampm">
            <html:options property="eventStartDate.ampmLabels"/>
          </html:select>
        </logic:notEqual>
      </starttime>
      <enddate>
        <html:select property="eventEndDate.month">
          <html:options labelProperty="eventEndDate.monthLabels"
                        property="eventEndDate.monthVals"/>
        </html:select>
        <html:select property="eventEndDate.day">
          <html:options labelProperty="eventEndDate.dayLabels"
                        property="eventEndDate.dayVals"/>
        </html:select>
        <html:select property="eventEndDate.year">
          <html:options property="yearVals"/>
        </html:select>
      </enddate>
      <endtime>
        <html:select property="eventEndDate.hour">
          <html:options labelProperty="eventEndDate.hourLabels"
                        property="eventEndDate.hourVals"/>
        </html:select>
        <html:select property="eventEndDate.minute">
          <html:options labelProperty="eventEndDate.minuteLabels"
                        property="eventEndDate.minuteVals"/>
        </html:select>
        <logic:notEqual name="calForm" property="hour24" value="true" >
          <html:select property="eventEndDate.ampm">
            <html:options property="eventEndDate.ampmLabels"/>
          </html:select>
        </logic:notEqual>
      </endtime>
    </genurl:form>
  </eventform>
</logic:equal>
--%>

<%@ include file="/docs/footer.jsp" %>

