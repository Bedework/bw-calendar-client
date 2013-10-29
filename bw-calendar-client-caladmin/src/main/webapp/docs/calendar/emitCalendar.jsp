<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<calendar>
  <bw:emitCollection name="calendar" indent="  " full="true" noTag="true" />

  <bw:emitCurrentPrivs name="calendar" property="currentAccess" />
  <bw:emitAcl name="calendar" property="currentAccess" />

  <c:choose>
    <c:when test="${requestScope.fullTree == 'true'}">
      <%-- a request has been made by the calling page to descend down the tree
           and ignore the open or closed state of calendars  --%>
      <bw:getChildren id="children" name="calendar" form="calForm" />
      <logic:iterate name="children" id="cal">
        <bean:define id="calendar" name="cal" toScope="session" />
          <%--
        <c:if test="${requestScope.stopDescent == 'true'}">
          < % - - an explicit request has been made to stop descending the full tree - - % >
          <bean:define id="fullTree" toScope="request">false</bean:define>
        </c:if>
        --%>
        <c:if test="${!((requestScope.stopDescentAtAliases == 'true') and (internalAlias == 'true' or externalSub == 'true'))}">
          <jsp:include page="/docs/calendar/emitCalendar.jsp" />
        </c:if>
      </logic:iterate>
    </c:when>
    <c:otherwise>
      <%-- the default behavior is to descend down the tree
           based on the open or closed state of calendars  --%>
      <logic:equal name="calendar" property="open" value="true">
        <bw:getChildren id="children" name="calendar" form="calForm" />
        <logic:iterate name="children" id="cal">
          <bean:define id="calendar" name="cal" toScope="session" />
          <jsp:include page="/docs/calendar/emitCalendar.jsp" />
        </logic:iterate>
      </logic:equal>
    </c:otherwise>
  </c:choose>
</calendar>

