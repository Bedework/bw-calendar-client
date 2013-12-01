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

<page>attendeeRespond</page>

<formElements>
  <bean:define id="event" name="calForm" property="event"/>

  <bw:emitText name="event" property="scheduleState" />
  <bw:emitText name="event" property="scheduleMethod" />

  <subscriptionId></subscriptionId>
  <guid><bean:write name="event" property="uid"/></guid>
  <recurrenceId><bean:write name="event" property="recurrenceId"/></recurrenceId>

  <guidcals>
  <logic:present  name="calForm" property="meetingCal">
    <bean:define id="cal" name="calForm" property="meetingCal"/>
    <calendar>
      <bw:emitText name="cal" property="name"/>
      <bw:emitText name="cal" property="path"/>
    </calendar>
  </logic:present>
  </guidcals>

  <logic:notPresent  name="event" property="location">
    <location>
      <address></address>
      <id></id><%--
        Value: integer - location id --%>
      <subaddress></subaddress><%--
        Value: string - more address information --%>
      <link></link><%--
        Value: URI - link to a web address for the location --%>
      <creator></creator><%--
        Value: string - location creator id --%>
    </location>
  </logic:notPresent>

  <%@ include file="/docs/schedule/emitEventProperties.jsp" %>

  <genurl:form action="schedule/attendeeRespond">
    <title>
      <html:text name="event" property="summary"/>
    </title>
    <bw:emitContainer name="event" indent="    " tagName="calendar" />
    <allDay><html:checkbox property="eventStartDate.dateOnly"/></allDay>
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
      <year>
        <html:select property="eventStartDate.year">
          <html:options property="yearVals"/>
        </html:select>
      </year>
      <yearText>
        <html:text property="eventStartDate.year" size="4"/>
      </yearText>
      <hour>
        <html:select property="eventStartDate.hour">
          <html:options labelProperty="eventStartDate.hourLabels"
                        property="eventStartDate.hourVals"/>
        </html:select>
      </hour>
      <minute>
        <html:select property="eventStartDate.minute">
          <html:options labelProperty="eventStartDate.minuteLabels"
                        property="eventStartDate.minuteVals"/>
        </html:select>
      </minute>
      <logic:notEqual name="calForm" property="hour24" value="true" >
        <ampm>
          <html:select property="eventStartDate.ampm">
            <html:options property="eventStartDate.ampmLabels"/>
          </html:select>
        </ampm>
      </logic:notEqual>
    </start>
    <end>
      <type><bean:write name="calForm" property="eventEndType"/></type>
      <dateTime>
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
        <year>
          <html:select property="eventEndDate.year">
            <html:options property="yearVals"/>
          </html:select>
          </year>
        <yearText>
          <html:text property="eventEndDate.year" size="4"/>
        </yearText>
        <hour>
          <html:select property="eventEndDate.hour">
            <html:options labelProperty="eventEndDate.hourLabels"
                          property="eventEndDate.hourVals"/>
          </html:select>
        </hour>
        <minute>
          <html:select property="eventEndDate.minute">
            <html:options labelProperty="eventEndDate.minuteLabels"
                          property="eventEndDate.minuteVals"/>
          </html:select>
        </minute>
        <ampm>
          <logic:notEqual name="calForm" property="hour24" value="true" >
            <html:select property="eventEndDate.ampm">
              <html:options property="eventEndDate.ampmLabels"/>
            </html:select>
          </logic:notEqual>
        </ampm>
      </dateTime>
      <duration>
        <days><html:text property="eventDuration.daysStr" size="2" /></days>
        <hours><html:text property="eventDuration.hoursStr" size="2" /></hours>
        <minutes><html:text property="eventDuration.minutesStr" size="2" /></minutes>
        <weeks><html:text property="eventDuration.weeksStr" size="2" /></weeks>
      </duration>
    </end>
    <desc><html:textarea name="event" property="description"></html:textarea></desc>
    <status><bean:write name="event" property="status"/></status>
    <transparency><bean:write name="event" property="transparency"/></transparency>
    <link><html:text name="event" property="link"/></link>
    <bean:define id="locations"
                 name="bw_locations_list" scope="session" />
    <location>
      <locationmenu>
        <html:select property="locationUid">
          <html:optionsCollection name="locations"
                                  label="address"
                                  value="uid"/>
        </html:select>
      </locationmenu>
      <locationtext><html:text property="locationAddress.value" /></locationtext>
    </location>
  </genurl:form>
</formElements>
<bean:define id="eventFormatter" name="calForm" property="curEventFmt"/>
<bw:emitText name="eventFormatter" property="xmlAccess" tagName="access"
                 filter="no"/>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

<%@ include file="/docs/footer.jsp" %>

</bedework>
