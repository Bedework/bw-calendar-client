<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>

  <logic:iterate id="msg" name="calForm" property="msg.msgList">
    <message>
      <id><bean:write name="msg" property="msgId" /></id>
      <logic:iterate id="param" name="msg" property="params" >
        <param><bean:write name="param" /></param>
      </logic:iterate>
    </message>
  </logic:iterate>

  <logic:iterate id="errBean" name="calForm" property="err.msgList">
    <error>
      <id><bean:write name="errBean" property="msgId" /></id>
      <logic:iterate id="param" name="errBean" property="params" >
        <param><bean:write name="param" /></param>
      </logic:iterate>
    </error>
  </logic:iterate>

  <bean:define id="presentationState"
               name="bw_presentationstate" scope="request" />
  <bw:emitText name="presentationState" property="appRoot" tagName="appRoot" />

  <page>attendeeWidget</page>

<%
try {
%>
  <logic:present  name="calForm" property="attendees.attendees">
    <attendees>
    <logic:iterate id="att" name="calForm" property="attendees.attendees">
	    <attendee>
	      <bw:emitText name="att" property="attendeeUri"/>
        <bw:emitText name="att" property="partstat"/>
        <bw:emitText name="att" property="cn"/>
        <bw:emitText name="att" property="cuType"/>
        <bw:emitText name="att" property="role"/>
        <bw:emitText name="att" property="delegatedFrom"/>
        <bw:emitText name="att" property="delegatedTo"/>
        <bw:emitText name="att" property="dir"/>
        <bw:emitText name="att" property="language"/>
        <bw:emitText name="att" property="rsvp"/>
        <bw:emitText name="att" property="sentBy"/>
	    </attendee>
    </logic:iterate>
    </attendees>
  </logic:present>
  <logic:notPresent  name="calForm" property="attendees.attendees">
    <attendees>
    </attendees>
  </logic:notPresent>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>
</bedework>
