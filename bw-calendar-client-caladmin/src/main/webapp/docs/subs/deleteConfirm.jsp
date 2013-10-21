<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<%@include file="/docs/header.jsp"%>

<page>deleteSubConfirm</page>
<tab>calsuite</tab>

<creating><bean:write name="calForm" property="addingCalendar"/></creating>

<%@include file="/docs/calendar/displayCalendarCommon.jsp"%>

<!-- subscriptions are a subset of calendars; there is no difference to the underlying code. -->
<calendars>
  <bean:define id="calendar" name="bw_user_collection_list" scope="session"
             toScope="session" />
  <%@include file="/docs/calendar/emitCalendar.jsp"%>
</calendars>

<publicCalendars>
  <bean:define id="calendar" name="bw_public_collection_list" scope="session"
             toScope="session" />

  <%-- open up the calendars to descend down the tree --%>
  <bean:define id="fullTree" toScope="request">true</bean:define>

  <%@include file="/docs/calendar/emitCalendar.jsp"%>
</publicCalendars>

<categories>
  <all>
    <logic:iterate id="category" name="bw_categories_list"
                   scope="session">
      <%@include file="/docs/category/emitCategory.jsp"%>
    </logic:iterate>
  </all>
  <current>
    <logic:present name="calForm" property="calendar.categories">
      <logic:iterate id="category" name="calForm" property="calendar.categories">
        <%@include file="/docs/category/emitCategory.jsp"%>
      </logic:iterate>
    </logic:present>
  </current>
</categories>

<%@include file="/docs/footer.jsp"%>
