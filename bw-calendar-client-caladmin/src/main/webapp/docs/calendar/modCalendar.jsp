<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<%@include file="/docs/header.jsp"%>

<page>modCalendar</page>
<tab>system</tab>

<creating><bean:write name="calForm" property="addingCalendar"/></creating>

<%@include file="/docs/calendar/displayCalendarCommon.jsp"%>

<%@include file="/docs/calendar/emitCalendars.jsp"%>

<categories>
  <all>
    <logic:iterate id="category" name="calForm" property="categories">
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


