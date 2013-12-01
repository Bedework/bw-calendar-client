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
  <page>addEventSub</page>
  <%-- This page used for displaying, confirming, and setting a
       calendar for a public event reference added to the personal
       calendar web client. --%>
  <%-- Wrapper for a single event (emitEvent.jsp)--%>
    <bean:define id="allView" value="true" toScope="request"/>
    <bean:define id="eventFormatter"
                 name="calForm"
                 property="curEventFmt"
                 toScope="request"/>

    <%@ include file="/docs/event/emitEvent.jsp" %>

    <formElements>
     <form>
       <!-- user's writable calendars -->
       <calendars>
         <bean:define id="addContentCalendarCollections"
                      name="bw_addcontent_collection_list" scope="session" />
         <html:select name="calForm" property="calendarId">
           <html:optionsCollection name="addContentCalendarCollections"
                                         label="path"
                                         value="path"/>
         </html:select>
       </calendars>
     </form>
    </formElements>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

<%@ include file="/docs/footer.jsp" %>

</bedework>
