<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@ include file="header.jsp" %>

<%
try {
%>

<page>alarmOptions</page>

<alarmoptionsform>
  <genurl:form action="alarm/setAlarm">
    <alarmdate>
      <html:select property="eventState.triggerDateTime.month">
       <html:options labelProperty="eventState.triggerDateTime.monthLabels"
                      property="eventState.triggerDateTime.monthVals"/>
      </html:select>
      <html:select property="eventState.triggerDateTime.day">
        <html:options labelProperty="eventState.triggerDateTime.dayLabels"
                      property="eventState.triggerDateTime.dayVals"/>
      </html:select>
      <html:select property="eventState.triggerDateTime.year">
        <html:options property="yearVals"/>
      </html:select>
    </alarmdate>
    <alarmtime>
      <html:select property="eventState.triggerDateTime.hour">
        <html:options labelProperty="eventState.triggerDateTime.hourLabels"
                      property="eventState.triggerDateTime.hourVals"/>
      </html:select>
      <html:select property="eventState.triggerDateTime.minute">
        <html:options labelProperty="eventState.triggerDateTime.minuteLabels"
                      property="eventState.triggerDateTime.minuteVals"/>
      </html:select>
      <logic:notEqual name="calForm" property="hour24" value="true" >
        <html:select property="eventState.triggerDateTime.ampm">
          <html:options property="eventState.triggerDateTime.ampmLabels"/>
        </html:select>
      </logic:notEqual>
    </alarmtime>
    <alarmTriggerSelectorDate>
      <html:radio name="calForm" property="alarmTriggerByDate"
                   value="true" />
    </alarmTriggerSelectorDate>
    <alarmTriggerSelectorDuration>
      <html:radio name="calForm" property="alarmTriggerByDate"
                      value="false" />
    </alarmTriggerSelectorDuration>
    <alarmduration>
      <bean:define id="td" name="calForm" property="eventState.triggerDuration" />
      <days><html:text size="5" maxlength="5" name="td" property="daysStr"/></days>
      <hours><html:text size="3" maxlength="3" name="td" property="hoursStr"/></hours>
      <minutes><html:text size="3" maxlength="3" name="td" property="minutesStr"/></minutes>
      <seconds><html:text size="3" maxlength="3" name="td" property="secondsStr"/></seconds>
      <weeks><html:text size="3" maxlength="3" name="td" property="weeksStr"/></weeks>
    </alarmduration>
    <alarmDurationBefore>
      <html:radio name="calForm" property="eventState.triggerDuration.negative"
                      value="true" />
    </alarmDurationBefore>
    <alarmDurationAfter>
      <html:radio name="calForm" property="eventState.triggerDuration.negative"
                      value="false" />
    </alarmDurationAfter>
    <alarmDurationRelStart>
      <html:radio name="calForm" property="eventState.alarmRelStart"
                      value="true" />
    </alarmDurationRelStart>
    <alarmDurationRelEnd>
      <html:radio name="calForm" property="eventState.alarmRelStart"
                      value="false" />
    </alarmDurationRelEnd>
    <email><html:text name="calForm" property="eventState.email"/></email>
    <subject><html:text name="calForm" property="eventState.subject" /></subject>
  </genurl:form>
</alarmoptionsform>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

<%@ include file="footer.jsp" %>

</bedework>
