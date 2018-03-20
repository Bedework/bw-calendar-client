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

<page>attendeesList</page>
  <logic:present  name="calForm" property="attendees">
    <attendees>
    <logic:iterate id="att" name="calForm" property="attendees.attendees">
	    <attendee>
	      <bw:emitText name="att" property="attendeeUri"/>
        <bw:emitText name="att" property="partstat"/>
        <bw:emitText name="att" property="cn"/>
        <bw:emitText name="att" property="role"/>
	    <attendee>
    </logic:iterate>
    </attendees>
  </logic:present>
  <logic:notPresent  name="calForm" property="attendees">
    <attendees>
    </attendees>
  </logic:notPresent>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

<%@ include file="/docs/footer.jsp" %>

</bedework>
