<%@ taglib uri='struts-bean' prefix='bean' %>

<%@include file="/docs/header.jsp"%>

<page>modView</page>
<tab>calsuite</tab>

<creating><bean:write name="calForm" property="addingView"/></creating>

<bean:define name="calForm" property="view" id="curView"/>
<currentView>
  <name><bean:write name="curView" property="name" /></name>
  <logic:present name="curView" property="collectionPaths">
    <logic:iterate name="curView" property="collectionPaths" id="path">
      <path><bean:write name="path"/></path>
    </logic:iterate>
  </logic:present>
</currentView>


<%-- subscriptions are a subset of calendars --%>
<calendars>
  <bean:define id="calendar" name="bw_user_collection_list" scope="session"
             toScope="session" />

  <%-- open up the calendars to descend down the tree, but stop after the root --%>
  <bean:define id="fullTree" toScope="request">true</bean:define>
  <bean:define id="stopDescent" toScope="request">true</bean:define>

  <%@include file="/docs/calendar/emitCalendar.jsp"%>
</calendars>

<%@include file="/docs/footer.jsp"%>
